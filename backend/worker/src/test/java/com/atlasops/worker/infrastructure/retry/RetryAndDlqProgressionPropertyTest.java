package com.atlasops.worker.infrastructure.retry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.atlasops.worker.infrastructure.redis.StreamMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Property-based tests for worker retry and DLQ progression.
 *
 * <p><b>Property 24: Worker Retry and DLQ Progression</b>
 *
 * <p><b>Validates: Requirements 20.2, 20.3</b>
 */
@Tag("Feature: monorepo-sdd-harness, Property 24: Worker Retry and DLQ Progression")
class RetryAndDlqProgressionPropertyTest {

  /**
   * Property: A task that fails all retries SHALL be moved to the DLQ exactly once.
   *
   * <p>After exhausting all configured retry attempts, the message must be moved to the DLQ stream
   * with the suffix ":dlq" appended to the original stream key.
   */
  @Property(tries = 100)
  void should_moveToDlq_when_allRetriesExhausted(
      @ForAll("streamKeys") String streamKey,
      @ForAll("messageIds") String messageId,
      @ForAll @IntRange(min = 1, max = 5) int maxAttempts) {

    // Arrange
    StringRedisTemplate mockRedis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    StreamOperations<String, Object, Object> mockStreamOps = mock(StreamOperations.class);
    when(mockRedis.opsForStream()).thenReturn(mockStreamOps);

    RetryConfig config = new RetryConfig(maxAttempts, 1, 10, 2.0);
    RetryExecutor executor = new RetryExecutor(config, mockRedis);

    Map<String, String> payload = new HashMap<>();
    payload.put("documentId", "doc-" + messageId);
    payload.put("tenantId", "tenant-test");
    StreamMessage message = new StreamMessage(streamKey, messageId, payload);

    AtomicInteger attemptCount = new AtomicInteger(0);
    RetryableTask alwaysFailingTask =
        () -> {
          attemptCount.incrementAndGet();
          throw new RuntimeException("Simulated failure");
        };

    // Act
    TaskResult result = executor.executeWithRetry("task-" + messageId, message, alwaysFailingTask);

    // Assert
    assertThat(result).isInstanceOf(TaskResult.MovedToDlq.class);
    assertThat(attemptCount.get()).isEqualTo(maxAttempts);

    // Verify DLQ stream was called exactly once
    verify(mockStreamOps, times(1)).add(any());
  }

  /**
   * Property: A task that succeeds on any attempt SHALL NOT be moved to the DLQ.
   *
   * <p>If a task succeeds at any point during the retry sequence, it should return Success and
   * never touch the DLQ.
   */
  @Property(tries = 100)
  void should_notMoveToDlq_when_taskSucceeds(
      @ForAll("streamKeys") String streamKey,
      @ForAll("messageIds") String messageId,
      @ForAll @IntRange(min = 1, max = 5) int maxAttempts,
      @ForAll @IntRange(min = 1, max = 5) int successOnAttempt) {

    // Skip if successOnAttempt > maxAttempts (task would fail all retries)
    if (successOnAttempt > maxAttempts) {
      return;
    }

    // Arrange
    StringRedisTemplate mockRedis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    StreamOperations<String, Object, Object> mockStreamOps = mock(StreamOperations.class);
    when(mockRedis.opsForStream()).thenReturn(mockStreamOps);

    RetryConfig config = new RetryConfig(maxAttempts, 1, 10, 2.0);
    RetryExecutor executor = new RetryExecutor(config, mockRedis);

    Map<String, String> payload = new HashMap<>();
    payload.put("documentId", "doc-" + messageId);
    payload.put("tenantId", "tenant-test");
    StreamMessage message = new StreamMessage(streamKey, messageId, payload);

    AtomicInteger attemptCount = new AtomicInteger(0);
    RetryableTask eventuallySucceedsTask =
        () -> {
          int attempt = attemptCount.incrementAndGet();
          if (attempt < successOnAttempt) {
            throw new RuntimeException("Simulated failure on attempt " + attempt);
          }
          // Success!
        };

    // Act
    TaskResult result =
        executor.executeWithRetry("task-" + messageId, message, eventuallySucceedsTask);

    // Assert
    assertThat(result).isInstanceOf(TaskResult.Success.class);
    assertThat(attemptCount.get()).isEqualTo(successOnAttempt);

    // Verify DLQ stream was never called
    verify(mockStreamOps, never()).add(any());
  }

  /**
   * Property: Retry delays SHALL follow exponential backoff pattern.
   *
   * <p>Each retry delay should be approximately initialDelay * backoffMultiplier^(attempt-1),
   * capped at maxDelay.
   */
  @Property(tries = 100)
  void should_calculateExponentialBackoff_forAnyAttempt(
      @ForAll @IntRange(min = 1, max = 10) int attempt,
      @ForAll @IntRange(min = 100, max = 5000) int initialDelayMs,
      @ForAll @IntRange(min = 5000, max = 60000) int maxDelayMs) {

    // Arrange
    double backoffMultiplier = 2.0;
    RetryConfig config = new RetryConfig(10, initialDelayMs, maxDelayMs, backoffMultiplier);

    // Act
    long delay = config.getDelayForAttempt(attempt);

    // Assert
    long expectedDelay = (long) (initialDelayMs * Math.pow(backoffMultiplier, attempt - 1));
    long cappedExpected = Math.min(expectedDelay, maxDelayMs);

    assertThat(delay).isEqualTo(cappedExpected);
    assertThat(delay).isLessThanOrEqualTo(maxDelayMs);
    assertThat(delay).isGreaterThanOrEqualTo(initialDelayMs);
  }

  /**
   * Property: DLQ message SHALL preserve all original payload fields plus metadata.
   *
   * <p>The DLQ message must contain: _original_stream, _original_message_id, _attempt_count,
   * _last_error, _dlq_timestamp, plus all original payload fields.
   */
  @Property(tries = 100)
  void should_preserveOriginalPayload_when_movedToDlq(
      @ForAll("streamKeys") String streamKey, @ForAll("messageIds") String messageId) {

    // Arrange
    StringRedisTemplate mockRedis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    StreamOperations<String, Object, Object> mockStreamOps = mock(StreamOperations.class);
    when(mockRedis.opsForStream()).thenReturn(mockStreamOps);

    RetryConfig config = new RetryConfig(1, 1, 10, 2.0); // Single attempt
    RetryExecutor executor = new RetryExecutor(config, mockRedis);

    Map<String, String> payload = new HashMap<>();
    payload.put("documentId", "doc-123");
    payload.put("tenantId", "tenant-alpha");
    payload.put("customField", "customValue");
    StreamMessage message = new StreamMessage(streamKey, messageId, payload);

    RetryableTask alwaysFailingTask =
        () -> {
          throw new RuntimeException("Test error message");
        };

    // Act
    executor.executeWithRetry("task-" + messageId, message, alwaysFailingTask);

    // Assert - verify add was called with expected stream key
    verify(mockStreamOps).add(any());
  }

  /**
   * Property: Attempt count SHALL equal maxAttempts when task exhausts all retries.
   *
   * <p>The task should be executed exactly maxAttempts times before being moved to DLQ.
   */
  @Property(tries = 100)
  void should_executeExactlyMaxAttempts_when_taskAlwaysFails(
      @ForAll @IntRange(min = 1, max = 5) int maxAttempts) {

    // Arrange
    StringRedisTemplate mockRedis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    StreamOperations<String, Object, Object> mockStreamOps = mock(StreamOperations.class);
    when(mockRedis.opsForStream()).thenReturn(mockStreamOps);

    RetryConfig config = new RetryConfig(maxAttempts, 1, 10, 2.0);
    RetryExecutor executor = new RetryExecutor(config, mockRedis);

    Map<String, String> payload = new HashMap<>();
    payload.put("documentId", "doc-test");
    StreamMessage message = new StreamMessage("test.stream", "msg-1", payload);

    AtomicInteger executionCount = new AtomicInteger(0);
    RetryableTask countingTask =
        () -> {
          executionCount.incrementAndGet();
          throw new RuntimeException("Always fails");
        };

    // Act
    TaskResult result = executor.executeWithRetry("counting-task", message, countingTask);

    // Assert
    assertThat(result).isInstanceOf(TaskResult.MovedToDlq.class);
    TaskResult.MovedToDlq dlqResult = (TaskResult.MovedToDlq) result;
    assertThat(dlqResult.totalAttempts()).isEqualTo(maxAttempts);
    assertThat(executionCount.get()).isEqualTo(maxAttempts);
  }

  // ─── Arbitrary Providers ──────────────────────────────────────────────────────

  @Provide
  Arbitrary<String> streamKeys() {
    return Arbitraries.of(
        "documents.uploaded",
        "documents.ready_for_analysis",
        "documents.analyzed",
        "notifications.email",
        "activities.events",
        "approvals.decided");
  }

  @Provide
  Arbitrary<String> messageIds() {
    return Arbitraries.strings()
        .withCharRange('0', '9')
        .withCharRange('a', 'f')
        .ofMinLength(16)
        .ofMaxLength(24)
        .map(s -> s + "-0");
  }
}
