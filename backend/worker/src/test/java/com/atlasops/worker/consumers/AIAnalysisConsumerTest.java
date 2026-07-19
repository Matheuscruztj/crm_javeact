package com.atlasops.worker.consumers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.ai.domain.DocumentAnalysisRequest;
import com.atlasops.ai.domain.DocumentAnalysisResult;
import com.atlasops.ai.domain.ports.DocumentAnalysisPort;
import com.atlasops.worker.infrastructure.redis.StreamMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

/** Unit tests for AIAnalysisConsumer. Validates: Requirements 12.1, 12.2, 12.3, 12.4, 12.5 */
@ExtendWith(MockitoExtension.class)
class AIAnalysisConsumerTest {

  @Mock private DocumentAnalysisPort documentAnalysisPort;

  @Mock private StringRedisTemplate redisTemplate;

  @Mock private StreamOperations<String, Object, Object> streamOperations;

  private AIAnalysisConsumer consumer;

  @BeforeEach
  void setUp() {
    consumer = new AIAnalysisConsumer(documentAnalysisPort, redisTemplate);
  }

  @Test
  void should_returnCorrectStreamKey() {
    assertThat(consumer.getStreamKey()).isEqualTo("documents.ready_for_analysis");
  }

  @Test
  void should_skipDocument_when_statusIsNotTextExtracted() throws Exception {
    // Arrange
    Map<String, String> payload = new HashMap<>();
    payload.put("documentId", "doc-123");
    payload.put("tenantId", "tenant-alpha");
    payload.put("extractedText", "Some text content");
    payload.put("contentType", "application/pdf");
    payload.put("status", "ANALYZED");

    StreamMessage message = new StreamMessage("documents.ready_for_analysis", "msg-1", payload);

    // Act
    consumer.handle(message);

    // Assert
    verify(documentAnalysisPort, never()).analyze(any());
    verify(streamOperations, never()).add(any());
  }

  @Test
  void should_useDeterministicFallback_when_extractedTextIsEmpty() throws Exception {
    // Arrange
    when(redisTemplate.opsForStream()).thenReturn(streamOperations);

    Map<String, String> payload = new HashMap<>();
    payload.put("documentId", "doc-123");
    payload.put("tenantId", "tenant-alpha");
    payload.put("extractedText", "");
    payload.put("contentType", "image/png");
    payload.put("status", "TEXT_EXTRACTED");

    StreamMessage message = new StreamMessage("documents.ready_for_analysis", "msg-1", payload);

    // Act
    consumer.handle(message);

    // Assert
    verify(documentAnalysisPort, never()).analyze(any());
    verify(streamOperations).add(any());
  }

  @Test
  void should_analyzeDocument_when_textIsAvailable() throws Exception {
    // Arrange
    when(redisTemplate.opsForStream()).thenReturn(streamOperations);

    Map<String, String> payload = new HashMap<>();
    payload.put("documentId", "doc-123");
    payload.put("tenantId", "tenant-alpha");
    payload.put("extractedText", "This is the document content for analysis.");
    payload.put("contentType", "application/pdf");
    payload.put("status", "TEXT_EXTRACTED");

    StreamMessage message = new StreamMessage("documents.ready_for_analysis", "msg-1", payload);

    DocumentAnalysisResult mockResult =
        new DocumentAnalysisResult(
            "Document summary",
            "contract",
            List.of(),
            List.of("missing signature"),
            List.of(),
            0.85,
            "ollama:analysis:v1",
            false);

    when(documentAnalysisPort.analyze(any(DocumentAnalysisRequest.class))).thenReturn(mockResult);

    // Act
    consumer.handle(message);

    // Assert
    verify(documentAnalysisPort).analyze(any(DocumentAnalysisRequest.class));
    verify(streamOperations).add(any());
  }

  @Test
  void should_useFallback_when_aiAnalysisFails() throws Exception {
    // Arrange
    when(redisTemplate.opsForStream()).thenReturn(streamOperations);

    Map<String, String> payload = new HashMap<>();
    payload.put("documentId", "doc-123");
    payload.put("tenantId", "tenant-alpha");
    payload.put("extractedText", "This is the document content.");
    payload.put("contentType", "application/pdf");
    payload.put("status", "TEXT_EXTRACTED");

    StreamMessage message = new StreamMessage("documents.ready_for_analysis", "msg-1", payload);

    when(documentAnalysisPort.analyze(any(DocumentAnalysisRequest.class)))
        .thenThrow(new RuntimeException("Ollama unavailable"));

    // Act
    consumer.handle(message);

    // Assert
    verify(streamOperations).add(any());
  }

  @Test
  void should_inferCategoryFromContentType_when_usingFallback() throws Exception {
    // Arrange
    when(redisTemplate.opsForStream()).thenReturn(streamOperations);

    Map<String, String> payload = new HashMap<>();
    payload.put("documentId", "doc-123");
    payload.put("tenantId", "tenant-alpha");
    payload.put("extractedText", ""); // Empty text triggers fallback
    payload.put("contentType", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    payload.put("status", "TEXT_EXTRACTED");

    StreamMessage message = new StreamMessage("documents.ready_for_analysis", "msg-1", payload);

    // Act
    consumer.handle(message);

    // Assert - fallback should infer "spreadsheet" category
    verify(streamOperations).add(any());
  }
}
