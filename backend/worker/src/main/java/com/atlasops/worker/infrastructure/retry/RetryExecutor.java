package com.atlasops.worker.infrastructure.retry;

import com.atlasops.worker.infrastructure.redis.StreamMessage;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Executes tasks with retry logic and DLQ support. After exhausting all retries, moves the failed
 * message to a DLQ stream.
 */
@Component
public class RetryExecutor {

  private static final Logger log = LoggerFactory.getLogger(RetryExecutor.class);
  private static final String DLQ_SUFFIX = ":dlq";

  private final RetryConfig config;
  private final StringRedisTemplate redisTemplate;

  public RetryExecutor(RetryConfig config, StringRedisTemplate redisTemplate) {
    this.config = config;
    this.redisTemplate = redisTemplate;
  }

  /**
   * Executes a task with retry logic. If all retries are exhausted, moves the original message to
   * the DLQ.
   *
   * @param taskId unique identifier for the task (for logging)
   * @param originalMessage the original stream message (for DLQ)
   * @param task the task to execute
   * @return the result of the execution
   */
  public TaskResult executeWithRetry(
      String taskId, StreamMessage originalMessage, RetryableTask task) {

    Throwable lastError = null;

    for (int attempt = 1; attempt <= config.maxAttempts(); attempt++) {
      long startTime = System.currentTimeMillis();

      try {
        task.execute();
        long duration = System.currentTimeMillis() - startTime;
        log.info("Task {} completed successfully on attempt {} in {}ms", taskId, attempt, duration);
        return new TaskResult.Success();
      } catch (Exception e) {
        long duration = System.currentTimeMillis() - startTime;
        lastError = e;

        log.warn(
            "Task {} failed on attempt {}/{} in {}ms: {}",
            taskId,
            attempt,
            config.maxAttempts(),
            duration,
            e.getMessage());

        if (attempt < config.maxAttempts()) {
          long delay = config.getDelayForAttempt(attempt);
          log.debug("Task {} will retry in {}ms", taskId, delay);
          sleep(delay);
        }
      }
    }

    // All retries exhausted - move to DLQ
    moveToDlq(originalMessage, lastError, config.maxAttempts());
    return new TaskResult.MovedToDlq(lastError, config.maxAttempts());
  }

  private void moveToDlq(StreamMessage originalMessage, Throwable lastError, int attemptCount) {
    String dlqStream = originalMessage.streamKey() + DLQ_SUFFIX;

    Map<String, String> dlqPayload = new HashMap<>(originalMessage.payload());
    dlqPayload.put("_original_stream", originalMessage.streamKey());
    dlqPayload.put("_original_message_id", originalMessage.messageId());
    dlqPayload.put("_attempt_count", String.valueOf(attemptCount));
    dlqPayload.put("_last_error", lastError != null ? lastError.getMessage() : "unknown");
    dlqPayload.put("_dlq_timestamp", Instant.now().toString());

    try {
      var record = StreamRecords.string(dlqPayload).withStreamKey(dlqStream);
      redisTemplate.opsForStream().add(record);

      log.warn(
          "Moved message {} to DLQ '{}' after {} attempts. Last error: {}",
          originalMessage.messageId(),
          dlqStream,
          attemptCount,
          lastError != null ? lastError.getMessage() : "unknown");
    } catch (Exception e) {
      log.error(
          "Failed to move message {} to DLQ '{}': {}",
          originalMessage.messageId(),
          dlqStream,
          e.getMessage());
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
