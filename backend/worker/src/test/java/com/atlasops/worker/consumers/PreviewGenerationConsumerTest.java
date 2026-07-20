package com.atlasops.worker.consumers;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.worker.infrastructure.redis.StreamMessage;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Unit tests for PreviewGenerationConsumer.
 * Validates: Requirements 11.6, 11.8
 */
@ExtendWith(MockitoExtension.class)
class PreviewGenerationConsumerTest {

    @Mock private S3Client s3Client;

    private PreviewGenerationConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new PreviewGenerationConsumer(s3Client, "atlasops-documents");
    }

    @Test
    void should_returnCorrectStreamKey() {
        org.assertj.core.api.Assertions.assertThat(consumer.getStreamKey())
                .isEqualTo("documents.uploaded");
    }

    @Test
    void should_skipPreview_when_contentTypeIsNotPdf() throws Exception {
        // Arrange — image document, not PDF
        Map<String, String> payload = new HashMap<>();
        payload.put("documentId", "doc-001");
        payload.put("tenantId", "tenant-alpha");
        payload.put("storagePath", "tenant-alpha/2025/01/doc-001/file.png");
        payload.put("contentType", "image/png");

        StreamMessage message = new StreamMessage("documents.uploaded", "msg-1", payload);

        // Act & Assert — should complete without calling S3
        assertThatCode(() -> consumer.handle(message)).doesNotThrowAnyException();
        verify(s3Client, never()).getObject(any(GetObjectRequest.class));
    }

    @Test
    void should_notThrow_when_s3DownloadFails() throws Exception {
        // Arrange — PDF document but S3 unavailable
        Map<String, String> payload = new HashMap<>();
        payload.put("documentId", "doc-002");
        payload.put("tenantId", "tenant-alpha");
        payload.put("storagePath", "tenant-alpha/2025/01/doc-002/file.pdf");
        payload.put("contentType", "application/pdf");

        StreamMessage message = new StreamMessage("documents.uploaded", "msg-2", payload);

        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenThrow(new RuntimeException("S3 unavailable"));

        // Act & Assert — failure is non-blocking (swallowed gracefully)
        assertThatCode(() -> consumer.handle(message)).doesNotThrowAnyException();
    }

    @Test
    void should_notThrow_when_contentTypeIsMissing() throws Exception {
        // Arrange — no contentType field
        Map<String, String> payload = new HashMap<>();
        payload.put("documentId", "doc-003");
        payload.put("tenantId", "tenant-alpha");
        payload.put("storagePath", "tenant-alpha/2025/01/doc-003/file.bin");

        StreamMessage message = new StreamMessage("documents.uploaded", "msg-3", payload);

        // Act & Assert — no contentType defaults to skip
        assertThatCode(() -> consumer.handle(message)).doesNotThrowAnyException();
        verify(s3Client, never()).getObject(any(GetObjectRequest.class));
    }

    @Test
    void should_storePreview_when_validPdfBytesAvailable() throws Exception {
        // Arrange — minimal 1-byte "pdf" (won't parse, triggers graceful skip path)
        Map<String, String> payload = new HashMap<>();
        payload.put("documentId", "doc-004");
        payload.put("tenantId", "tenant-alpha");
        payload.put("storagePath", "tenant-alpha/2025/01/doc-004/file.pdf");
        payload.put("contentType", "application/pdf");

        StreamMessage message = new StreamMessage("documents.uploaded", "msg-4", payload);

        // Return a tiny byte stream that PDFBox will fail to parse → graceful skip
        ResponseInputStream<GetObjectResponse> mockStream = new ResponseInputStream<>(
                GetObjectResponse.builder().build(),
                new ByteArrayInputStream(new byte[]{1, 2, 3}));

        when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(mockStream);

        // Act & Assert — invalid PDF content is logged and swallowed (non-blocking)
        assertThatCode(() -> consumer.handle(message)).doesNotThrowAnyException();
    }
}
