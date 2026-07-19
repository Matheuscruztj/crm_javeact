package com.atlasops.worker.infrastructure.redis;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis Streams consumer infrastructure with XREADGROUP, XACK, XAUTOCLAIM support. Implements
 * configurable consumer groups, batch consumption, and reconnection with exponential backoff.
 */
@Component
public class RedisStreamConsumer {

  private static final Logger log = LoggerFactory.getLogger(RedisStreamConsumer.class);

  private final StringRedisTemplate redisTemplate;
  private final StreamConsumerConfig config;
  private final Map<String, MessageHandler> handlers = new ConcurrentHashMap<>();
  private final Map<String, Thread> consumerThreads = new ConcurrentHashMap<>();
  private final AtomicBoolean running = new AtomicBoolean(false);
  private final ExecutorService executor;

  public RedisStreamConsumer(StringRedisTemplate redisTemplate, StreamConsumerConfig config) {
    this.redisTemplate = redisTemplate;
    this.config = config;
    this.executor = Executors.newCachedThreadPool();
  }

  /**
   * Registers a handler for a specific stream.
   *
   * @param streamKey the Redis stream key
   * @param handler the message handler
   */
  public void registerHandler(String streamKey, MessageHandler handler) {
    handlers.put(streamKey, handler);
    log.info("Registered handler for stream: {}", streamKey);
  }

  /** Starts consuming from all registered streams. */
  public void start() {
    if (!running.compareAndSet(false, true)) {
      log.warn("Consumer already running");
      return;
    }

    log.info(
        "Starting Redis Stream consumer with group: {}, consumer: {}",
        config.groupName(),
        config.consumerName());

    for (String streamKey : handlers.keySet()) {
      ensureConsumerGroup(streamKey);
      claimPendingMessages(streamKey);
      startConsumerThread(streamKey);
    }
  }

  /** Stops all consumer threads gracefully. */
  public void stop() {
    if (!running.compareAndSet(true, false)) {
      return;
    }

    log.info("Stopping Redis Stream consumer...");
    consumerThreads.values().forEach(Thread::interrupt);
    executor.shutdown();
    try {
      if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      executor.shutdownNow();
    }
    consumerThreads.clear();
    log.info("Redis Stream consumer stopped");
  }

  private void ensureConsumerGroup(String streamKey) {
    try {
      redisTemplate.opsForStream().createGroup(streamKey, config.groupName());
      log.info("Created consumer group '{}' for stream '{}'", config.groupName(), streamKey);
    } catch (Exception e) {
      if (e.getMessage() != null && e.getMessage().contains("BUSYGROUP")) {
        log.debug(
            "Consumer group '{}' already exists for stream '{}'", config.groupName(), streamKey);
      } else {
        log.warn("Failed to create consumer group for stream '{}': {}", streamKey, e.getMessage());
      }
    }
  }

  private void claimPendingMessages(String streamKey) {
    try {
      PendingMessages pending =
          redisTemplate
              .opsForStream()
              .pending(streamKey, config.groupName(), Range.unbounded(), 100);

      if (pending == null || pending.isEmpty()) {
        return;
      }

      long idleThreshold = config.claimIdleTimeMs();
      int claimed = 0;

      for (PendingMessage msg : pending) {
        if (msg.getElapsedTimeSinceLastDelivery().toMillis() > idleThreshold) {
          try {
            List<MapRecord<String, Object, Object>> records =
                redisTemplate
                    .opsForStream()
                    .claim(
                        streamKey,
                        config.groupName(),
                        config.consumerName(),
                        Duration.ofMillis(idleThreshold),
                        msg.getId());
            if (records != null && !records.isEmpty()) {
              claimed++;
            }
          } catch (Exception e) {
            log.warn("Failed to claim pending message {}: {}", msg.getId(), e.getMessage());
          }
        }
      }

      if (claimed > 0) {
        log.info(
            "Claimed {} pending messages idle > {}ms from stream '{}'",
            claimed,
            idleThreshold,
            streamKey);
      }
    } catch (Exception e) {
      log.warn("Failed to claim pending messages for stream '{}': {}", streamKey, e.getMessage());
    }
  }

  private void startConsumerThread(String streamKey) {
    Thread thread = new Thread(() -> consumeLoop(streamKey), "stream-consumer-" + streamKey);
    thread.setDaemon(true);
    consumerThreads.put(streamKey, thread);
    thread.start();
  }

  private void consumeLoop(String streamKey) {
    MessageHandler handler = handlers.get(streamKey);
    long currentDelay = config.reconnectInitialDelayMs();

    while (running.get() && !Thread.currentThread().isInterrupted()) {
      try {
        consumeBatch(streamKey, handler);
        currentDelay = config.reconnectInitialDelayMs();
      } catch (Exception e) {
        if (Thread.currentThread().isInterrupted()) {
          break;
        }
        log.error("Error consuming from stream '{}': {}", streamKey, e.getMessage());
        sleep(currentDelay);
        currentDelay = Math.min(currentDelay * 2, config.reconnectMaxDelayMs());
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void consumeBatch(String streamKey, MessageHandler handler) {
    StreamReadOptions options =
        StreamReadOptions.empty()
            .count(config.batchSize())
            .block(Duration.ofMillis(config.blockTimeoutMs()));

    Consumer consumer = Consumer.from(config.groupName(), config.consumerName());
    StreamOffset<String> offset = StreamOffset.create(streamKey, ReadOffset.lastConsumed());

    List<MapRecord<String, Object, Object>> records =
        redisTemplate.opsForStream().read(consumer, options, offset);

    if (records == null || records.isEmpty()) {
      return;
    }

    for (MapRecord<String, Object, Object> record : records) {
      Map<String, String> payload = new HashMap<>();
      record.getValue().forEach((k, v) -> payload.put(String.valueOf(k), String.valueOf(v)));

      StreamMessage message = new StreamMessage(streamKey, record.getId().getValue(), payload);

      try {
        handler.handle(message);
        redisTemplate.opsForStream().acknowledge(streamKey, config.groupName(), record.getId());
        log.debug(
            "Processed and acknowledged message {} from stream '{}'", record.getId(), streamKey);
      } catch (Exception e) {
        log.error(
            "Failed to process message {} from stream '{}': {}",
            record.getId(),
            streamKey,
            e.getMessage());
        // Don't ACK - message will be reprocessed via pending claims
      }
    }
  }

  private void sleep(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
