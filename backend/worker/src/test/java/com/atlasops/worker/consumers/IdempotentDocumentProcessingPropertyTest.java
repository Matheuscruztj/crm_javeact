package com.atlasops.worker.consumers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.atlasops.worker.infrastructure.redis.StreamMessage;
import com.atlasops.worker.infrastructure.retry.RetryConfig;
import com.atlasops.worker.infrastructure.retry.RetryExecutor;
import java.util.HashMap;
import java.util.Map;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Property-based tests for idempotent document processing.
 *
 * <p><b>Property 17: Idempotent Document Processing</b>
 *
 * <p><b>Validates: Requirements 11.7</b>
 */
@Tag("Feature: monorepo-sdd-harness, Property 17: Idempotent Document Processing")
class IdempotentDocumentProcessingPropertyTest {

  /**
   * Property: Processing a document multiple times with the same status SHALL produce the same
   * result.
   *
   * <p>If a document message is delivered multiple times (at-least-once semantics), the consumer
   * should skip processing if the document status indicates it has already been processed.
   */
  @Property(tries = 100)
  void should_skipProcessing_when_documentAlreadyProcessed(
      @ForAll("documentIds") String documentId,
      @ForAll("tenantIds") String tenantId,
      @ForAll("processedStatuses") String status,
      @ForAll @IntRange(min = 1, max = 5) int deliveryCount)
      throws Exception {

    // Arrange
    StringRedisTemplate mockRedis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    StreamOperations<String, Object, Object> mockStreamOps = mock(StreamOperations.class);
    when(mockRedis.opsForStream()).thenReturn(mockStreamOps);

    S3Client mockS3 = mock(S3Client.class);
    RetryExecutor retryExecutor = new RetryExecutor(RetryConfig.defaults(), mockRedis);

    TextExtractionConsumer consumer =
        new TextExtractionConsumer(retryExecutor, mockRedis, mockS3, "test-bucket");

    Map<String, String> payload = new HashMap<>();
    payload.put("documentId", documentId);
    payload.put("tenantId", tenantId);
    payload.put("storagePath", "path/to/doc.pdf");
    payload.put("contentType", "application/pdf");
    payload.put("status", status); // Already processed status

    // Act - simulate multiple deliveries
    for (int i = 0; i < deliveryCount; i++) {
      StreamMessage message = new StreamMessage("documents.uploaded", "msg-" + i, payload);
      // Should return without error due to idempotency check
      consumer.handle(message);
    }

    // Assert - S3 should never be called because idempotency check skips processing
    verify(mockS3, never())
        .getObject(any(software.amazon.awssdk.services.s3.model.GetObjectRequest.class));
  }

  /**
   * Property: A document with UPLOADED status SHALL be processed exactly once per unique message.
   *
   * <p>Documents in UPLOADED status should proceed to text extraction.
   */
  @Property(tries = 100)
  void should_processDocument_when_statusIsUploaded(
      @ForAll("documentIds") String documentId, @ForAll("tenantIds") String tenantId)
      throws Exception {

    // Arrange
    StringRedisTemplate mockRedis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    StreamOperations<String, Object, Object> mockStreamOps = mock(StreamOperations.class);
    when(mockRedis.opsForStream()).thenReturn(mockStreamOps);

    S3Client mockS3 = mock(S3Client.class);
    // S3 will throw because we're not setting up a real response
    when(mockS3.getObject(any(software.amazon.awssdk.services.s3.model.GetObjectRequest.class)))
        .thenThrow(new RuntimeException("Test - S3 called"));

    // Single attempt to avoid long retries during test
    RetryExecutor retryExecutor = new RetryExecutor(new RetryConfig(1, 1, 10, 2.0), mockRedis);

    TextExtractionConsumer consumer =
        new TextExtractionConsumer(retryExecutor, mockRedis, mockS3, "test-bucket");

    Map<String, String> payload = new HashMap<>();
    payload.put("documentId", documentId);
    payload.put("tenantId", tenantId);
    payload.put("storagePath", "path/to/doc.pdf");
    payload.put("contentType", "application/pdf");
    payload.put("status", "UPLOADED"); // Should be processed

    StreamMessage message = new StreamMessage("documents.uploaded", "msg-1", payload);

    // Act - consumer will try to process, S3 call will fail, retry executor will handle
    consumer.handle(message);

    // Assert - S3 should have been called because status is UPLOADED
    verify(mockS3).getObject(any(software.amazon.awssdk.services.s3.model.GetObjectRequest.class));
  }

  /**
   * Property: Idempotency check SHALL occur before any side effects.
   *
   * <p>The status check must happen before any expensive operations (S3 fetch, Tika parsing, Redis
   * publish).
   */
  @Property(tries = 100)
  void should_checkIdempotency_beforeSideEffects(
      @ForAll("processedStatuses") String alreadyProcessedStatus) throws Exception {

    // Arrange
    StringRedisTemplate mockRedis = mock(StringRedisTemplate.class);
    S3Client mockS3 = mock(S3Client.class);
    RetryExecutor retryExecutor = new RetryExecutor(RetryConfig.defaults(), mockRedis);

    TextExtractionConsumer consumer =
        new TextExtractionConsumer(retryExecutor, mockRedis, mockS3, "test-bucket");

    Map<String, String> payload = new HashMap<>();
    payload.put("documentId", "doc-123");
    payload.put("tenantId", "tenant-alpha");
    payload.put("storagePath", "path/to/doc.pdf");
    payload.put("contentType", "application/pdf");
    payload.put("status", alreadyProcessedStatus);

    StreamMessage message = new StreamMessage("documents.uploaded", "msg-1", payload);

    // Act - should skip due to idempotency
    consumer.handle(message);

    // Assert - no side effects should have occurred
    verify(mockS3, never())
        .getObject(any(software.amazon.awssdk.services.s3.model.GetObjectRequest.class));
    verify(mockRedis, never()).opsForStream();
  }

  /**
   * Property: Documents without explicit status SHALL be processed (defensive behavior).
   *
   * <p>If status field is missing, the consumer should attempt processing (fail-safe).
   */
  @Property(tries = 100)
  void should_attemptProcessing_when_statusIsMissing(
      @ForAll("documentIds") String documentId, @ForAll("tenantIds") String tenantId)
      throws Exception {

    // Arrange
    StringRedisTemplate mockRedis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    StreamOperations<String, Object, Object> mockStreamOps = mock(StreamOperations.class);
    when(mockRedis.opsForStream()).thenReturn(mockStreamOps);

    S3Client mockS3 = mock(S3Client.class);
    when(mockS3.getObject(any(software.amazon.awssdk.services.s3.model.GetObjectRequest.class)))
        .thenThrow(new RuntimeException("Test - S3 called"));

    // Single attempt to avoid long retries during test
    RetryExecutor retryExecutor = new RetryExecutor(new RetryConfig(1, 1, 10, 2.0), mockRedis);

    TextExtractionConsumer consumer =
        new TextExtractionConsumer(retryExecutor, mockRedis, mockS3, "test-bucket");

    Map<String, String> payload = new HashMap<>();
    payload.put("documentId", documentId);
    payload.put("tenantId", tenantId);
    payload.put("storagePath", "path/to/doc.pdf");
    payload.put("contentType", "application/pdf");
    // No status field!

    StreamMessage message = new StreamMessage("documents.uploaded", "msg-1", payload);

    // Act - consumer will try to process (no status = treat as processable)
    consumer.handle(message);

    // Assert - processing should have been attempted (S3 called)
    verify(mockS3).getObject(any(software.amazon.awssdk.services.s3.model.GetObjectRequest.class));
  }

  // ─── Arbitrary Providers ──────────────────────────────────────────────────────

  @Provide
  Arbitrary<String> documentIds() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .withCharRange('0', '9')
        .withChars('-')
        .ofMinLength(8)
        .ofMaxLength(36)
        .filter(s -> !s.startsWith("-") && !s.endsWith("-"));
  }

  @Provide
  Arbitrary<String> tenantIds() {
    return Arbitraries.of(
        "tenant-alpha", "tenant-beta", "tenant-gamma", "tenant-001", "tenant-test");
  }

  @Provide
  Arbitrary<String> processedStatuses() {
    // Statuses that indicate the document has already been processed beyond UPLOADED
    return Arbitraries.of(
        "TEXT_EXTRACTED", "ANALYZED", "APPROVED", "REJECTED", "PROCESSING_FAILED");
  }
}
