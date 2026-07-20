package com.atlasops.worker.consumers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.worker.infrastructure.redis.StreamMessage;
import com.atlasops.worker.infrastructure.retry.RetryExecutor;
import com.atlasops.worker.infrastructure.retry.TaskResult;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Unit tests for TextExtractionConsumer.
 * Validates: Requirements 11.1, 11.2, 11.3, 11.4, 11.5, 11.7
 */
@ExtendWith(MockitoExtension.class)
class TextExtractionConsumerTest {

    @Mock private RetryExecutor retryExecutor;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private S3Client s3Client;
    @Mock private StreamOperations<String, Object, Object> streamOps;

    private TextExtractionConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new TextExtractionConsumer(retryExecutor, redisTemplate, s3Client, "test-bucket");
    }

    @Test
    void should_returnCorrectStreamKey() {
        assertThat(consumer.getStreamKey()).isEqualTo("documents.uploaded");
    }

    @Test
    void should_skipProcessing_when_documentStatusIsNotUploaded() throws Exception {
        // Already TEXT_EXTRACTED — idempotency guard
        Map<String, String> payload = new HashMap<>();
        payload.put("documentId", "doc-001");
        payload.put("tenantId", "tenant-alpha");
        payload.put("storagePath", "tenant-alpha/2025/01/doc-001/file.pdf");
        payload.put("contentType", "application/pdf");
        payload.put("status", "TEXT_EXTRACTED");

        StreamMessage message = new StreamMessage("documents.uploaded", "msg-1", payload);

        consumer.handle(message);

        verify(retryExecutor, never()).executeWithRetry(any(), any(), any());
    }

    @Test
    void should_callRetryExecutor_when_documentIsUploaded() throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("documentId", "doc-002");
        payload.put("tenantId", "tenant-alpha");
        payload.put("storagePath", "tenant-alpha/2025/01/doc-002/file.pdf");
        payload.put("contentType", "application/pdf");
        payload.put("status", "UPLOADED");

        StreamMessage message = new StreamMessage("documents.uploaded", "msg-2", payload);

        when(retryExecutor.executeWithRetry(any(), any(), any()))
                .thenReturn(new TaskResult.Success());

        consumer.handle(message);

        verify(retryExecutor).executeWithRetry(any(), any(), any());
    }

    @Test
    void should_publishStatusUpdate_when_retriesExhausted() throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("documentId", "doc-003");
        payload.put("tenantId", "tenant-alpha");
        payload.put("storagePath", "tenant-alpha/2025/01/doc-003/file.pdf");
        payload.put("contentType", "application/pdf");
        payload.put("status", "UPLOADED");

        StreamMessage message = new StreamMessage("documents.uploaded", "msg-3", payload);

        when(retryExecutor.executeWithRetry(any(), any(), any()))
                .thenReturn(new TaskResult.MovedToDlq(new RuntimeException("Tika failed"), 3));
        when(redisTemplate.opsForStream()).thenReturn(streamOps);

        consumer.handle(message);

        verify(redisTemplate).opsForStream();
    }

    @Test
    void should_notThrow_when_statusFieldMissing() throws Exception {
        // Missing status field — treated as UPLOADED (default processing path)
        Map<String, String> payload = new HashMap<>();
        payload.put("documentId", "doc-004");
        payload.put("tenantId", "tenant-alpha");
        payload.put("storagePath", "tenant-alpha/2025/01/doc-004/file.pdf");
        payload.put("contentType", "application/pdf");
        // no "status" field

        StreamMessage message = new StreamMessage("documents.uploaded", "msg-4", payload);

        when(retryExecutor.executeWithRetry(any(), any(), any()))
                .thenReturn(new TaskResult.Success());

        assertThatCode(() -> consumer.handle(message)).doesNotThrowAnyException();
    }
}
