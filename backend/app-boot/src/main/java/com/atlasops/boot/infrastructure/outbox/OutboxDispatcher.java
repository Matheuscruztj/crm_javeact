package com.atlasops.boot.infrastructure.outbox;

import com.atlasops.shared.domain.OutboxEvent;
import com.atlasops.shared.domain.ports.OutboxEventRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scheduled dispatcher that moves events from the outbox table to Redis Streams.
 * Runs every 500ms, fetching up to 50 pending events per batch.
 *
 * <p>Guarantees at-least-once delivery: events remain PENDING until successfully
 * published to Redis. Failed events are retried up to 5 times before being
 * marked as FAILED.
 *
 * <p>Validates: Requirements 10.4, 11.1 (Outbox Dispatcher)
 */
@Component
public class OutboxDispatcher {

  private static final Logger log = LoggerFactory.getLogger(OutboxDispatcher.class);
  private static final int BATCH_SIZE = 50;

  private final OutboxEventRepository outboxEventRepository;
  private final StringRedisTemplate redisTemplate;

  public OutboxDispatcher(
      OutboxEventRepository outboxEventRepository, StringRedisTemplate redisTemplate) {
    this.outboxEventRepository = outboxEventRepository;
    this.redisTemplate = redisTemplate;
  }

  @Scheduled(fixedDelay = 500)
  @Transactional
  public void dispatchPendingEvents() {
    List<OutboxEvent> pendingEvents = outboxEventRepository.findPendingEvents(BATCH_SIZE);

    if (pendingEvents.isEmpty()) {
      return;
    }

    log.debug("Dispatching {} pending outbox events", pendingEvents.size());

    for (OutboxEvent event : pendingEvents) {
      try {
        publishToRedis(event);
        outboxEventRepository.markPublished(event.getEventId());
      } catch (Exception e) {
        log.warn(
            "Failed to dispatch event [{}] (eventId={}): {}",
            event.getEventType(),
            event.getEventId(),
            e.getMessage());
        outboxEventRepository.markFailed(event.getEventId(), e.getMessage());
      }
    }
  }

  private void publishToRedis(OutboxEvent event) {
    Map<String, String> message = new HashMap<>();
    message.put("eventId", event.getEventId());
    message.put("eventType", event.getEventType());
    message.put("payload", event.getPayload());
    message.put("tenantId", event.getTenantId() != null ? event.getTenantId() : "");
    message.put("correlationId", event.getCorrelationId() != null ? event.getCorrelationId() : "");
    message.put("timestamp", event.getCreatedAt().toString());

    redisTemplate
        .opsForStream()
        .add(StreamRecords.string(message).withStreamKey(event.getStreamName()));

    log.info(
        "Dispatched event [{}] (eventId={}) to stream [{}]",
        event.getEventType(),
        event.getEventId(),
        event.getStreamName());
  }
}
