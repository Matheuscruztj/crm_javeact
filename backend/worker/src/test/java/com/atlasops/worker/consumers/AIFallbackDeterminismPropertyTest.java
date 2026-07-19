package com.atlasops.worker.consumers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.atlasops.ai.domain.DocumentAnalysisRequest;
import com.atlasops.ai.domain.ports.DocumentAnalysisPort;
import com.atlasops.worker.infrastructure.redis.StreamMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Property-based tests for AI fallback determinism.
 *
 * <p><b>Property 18: AI Fallback Determinism</b>
 *
 * <p><b>Validates: Requirements 12.3</b>
 */
@Tag("Feature: monorepo-sdd-harness, Property 18: AI Fallback Determinism")
class AIFallbackDeterminismPropertyTest {

  /**
   * Property: Fallback result for the same content type SHALL always produce the same category.
   *
   * <p>When AI is unavailable, the deterministic fallback must return consistent category based
   * solely on content type.
   */
  @Property(tries = 100)
  void should_produceSameCategory_forSameContentType_whenUsingFallback(
      @ForAll("contentTypes") String contentType,
      @ForAll @IntRange(min = 2, max = 5) int executionCount)
      throws Exception {

    // Arrange
    List<String> capturedCategories = new ArrayList<>();

    for (int i = 0; i < executionCount; i++) {
      StringRedisTemplate mockRedis = mock(StringRedisTemplate.class);
      @SuppressWarnings("unchecked")
      StreamOperations<String, Object, Object> mockStreamOps = mock(StreamOperations.class);
      when(mockRedis.opsForStream()).thenReturn(mockStreamOps);

      DocumentAnalysisPort mockAnalysisPort = mock(DocumentAnalysisPort.class);
      // Simulate AI unavailable
      when(mockAnalysisPort.analyze(any(DocumentAnalysisRequest.class)))
          .thenThrow(new RuntimeException("Ollama unavailable"));

      AIAnalysisConsumer consumer = new AIAnalysisConsumer(mockAnalysisPort, mockRedis);

      Map<String, String> payload = new HashMap<>();
      payload.put("documentId", "doc-" + i);
      payload.put("tenantId", "tenant-test");
      payload.put("extractedText", "Some text content for analysis");
      payload.put("contentType", contentType);
      payload.put("status", "TEXT_EXTRACTED");

      StreamMessage message =
          new StreamMessage("documents.ready_for_analysis", "msg-" + i, payload);

      // Act
      consumer.handle(message);

      // Capture the category from the published event
      @SuppressWarnings("unchecked")
      ArgumentCaptor<MapRecord<String, String, String>> captor =
          ArgumentCaptor.forClass(MapRecord.class);
      verify(mockStreamOps).add(captor.capture());

      MapRecord<String, String, String> record = captor.getValue();
      String category = record.getValue().get("category");
      capturedCategories.add(category);
    }

    // Assert - all categories should be identical
    String firstCategory = capturedCategories.get(0);
    assertThat(capturedCategories).allMatch(c -> c.equals(firstCategory));
  }

  /**
   * Property: Fallback SHALL always have confidence score of 0.0.
   *
   * <p>When using deterministic fallback, confidence must be 0.0 to indicate no AI analysis was
   * performed.
   */
  @Property(tries = 100)
  void should_haveZeroConfidence_whenUsingFallback(@ForAll("contentTypes") String contentType)
      throws Exception {

    // Arrange
    StringRedisTemplate mockRedis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    StreamOperations<String, Object, Object> mockStreamOps = mock(StreamOperations.class);
    when(mockRedis.opsForStream()).thenReturn(mockStreamOps);

    DocumentAnalysisPort mockAnalysisPort = mock(DocumentAnalysisPort.class);
    when(mockAnalysisPort.analyze(any(DocumentAnalysisRequest.class)))
        .thenThrow(new RuntimeException("Ollama unavailable"));

    AIAnalysisConsumer consumer = new AIAnalysisConsumer(mockAnalysisPort, mockRedis);

    Map<String, String> payload = new HashMap<>();
    payload.put("documentId", "doc-123");
    payload.put("tenantId", "tenant-test");
    payload.put("extractedText", "Document text content");
    payload.put("contentType", contentType);
    payload.put("status", "TEXT_EXTRACTED");

    StreamMessage message = new StreamMessage("documents.ready_for_analysis", "msg-1", payload);

    // Act
    consumer.handle(message);

    // Assert
    @SuppressWarnings("unchecked")
    ArgumentCaptor<MapRecord<String, String, String>> captor =
        ArgumentCaptor.forClass(MapRecord.class);
    verify(mockStreamOps).add(captor.capture());

    MapRecord<String, String, String> record = captor.getValue();
    String confidenceScore = record.getValue().get("confidenceScore");
    assertThat(Double.parseDouble(confidenceScore)).isEqualTo(0.0);
  }

  /**
   * Property: Fallback SHALL always have fallback=true flag.
   *
   * <p>The fallback flag must be set to true to indicate deterministic fallback was used.
   */
  @Property(tries = 100)
  void should_setFallbackFlag_whenUsingFallback(@ForAll("contentTypes") String contentType)
      throws Exception {

    // Arrange
    StringRedisTemplate mockRedis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    StreamOperations<String, Object, Object> mockStreamOps = mock(StreamOperations.class);
    when(mockRedis.opsForStream()).thenReturn(mockStreamOps);

    DocumentAnalysisPort mockAnalysisPort = mock(DocumentAnalysisPort.class);
    when(mockAnalysisPort.analyze(any(DocumentAnalysisRequest.class)))
        .thenThrow(new RuntimeException("Ollama unavailable"));

    AIAnalysisConsumer consumer = new AIAnalysisConsumer(mockAnalysisPort, mockRedis);

    Map<String, String> payload = new HashMap<>();
    payload.put("documentId", "doc-123");
    payload.put("tenantId", "tenant-test");
    payload.put("extractedText", "Document text content");
    payload.put("contentType", contentType);
    payload.put("status", "TEXT_EXTRACTED");

    StreamMessage message = new StreamMessage("documents.ready_for_analysis", "msg-1", payload);

    // Act
    consumer.handle(message);

    // Assert
    @SuppressWarnings("unchecked")
    ArgumentCaptor<MapRecord<String, String, String>> captor =
        ArgumentCaptor.forClass(MapRecord.class);
    verify(mockStreamOps).add(captor.capture());

    MapRecord<String, String, String> record = captor.getValue();
    String fallback = record.getValue().get("fallback");
    assertThat(fallback).isEqualTo("true");
  }

  /**
   * Property: Fallback category mapping SHALL be deterministic for all known content types.
   *
   * <p>Each content type should map to a specific, predictable category.
   */
  @Property(tries = 100)
  void should_mapContentTypeToPredictableCategory(@ForAll("knownContentTypes") String contentType)
      throws Exception {

    // Arrange
    StringRedisTemplate mockRedis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    StreamOperations<String, Object, Object> mockStreamOps = mock(StreamOperations.class);
    when(mockRedis.opsForStream()).thenReturn(mockStreamOps);

    DocumentAnalysisPort mockAnalysisPort = mock(DocumentAnalysisPort.class);
    when(mockAnalysisPort.analyze(any(DocumentAnalysisRequest.class)))
        .thenThrow(new RuntimeException("Ollama unavailable"));

    AIAnalysisConsumer consumer = new AIAnalysisConsumer(mockAnalysisPort, mockRedis);

    Map<String, String> payload = new HashMap<>();
    payload.put("documentId", "doc-123");
    payload.put("tenantId", "tenant-test");
    payload.put("extractedText", "Document content");
    payload.put("contentType", contentType);
    payload.put("status", "TEXT_EXTRACTED");

    StreamMessage message = new StreamMessage("documents.ready_for_analysis", "msg-1", payload);

    // Act
    consumer.handle(message);

    // Assert
    @SuppressWarnings("unchecked")
    ArgumentCaptor<MapRecord<String, String, String>> captor =
        ArgumentCaptor.forClass(MapRecord.class);
    verify(mockStreamOps).add(captor.capture());

    MapRecord<String, String, String> record = captor.getValue();
    String category = record.getValue().get("category");

    // Verify expected category mapping
    String expectedCategory = getExpectedCategory(contentType);
    assertThat(category).isEqualTo(expectedCategory);
  }

  /**
   * Property: Fallback for empty text SHALL use deterministic fallback.
   *
   * <p>When extracted text is empty (e.g., images), the consumer should use deterministic fallback
   * without calling AI.
   */
  @Property(tries = 100)
  void should_useFallback_whenTextIsEmpty(@ForAll("imageContentTypes") String contentType)
      throws Exception {

    // Arrange
    StringRedisTemplate mockRedis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    StreamOperations<String, Object, Object> mockStreamOps = mock(StreamOperations.class);
    when(mockRedis.opsForStream()).thenReturn(mockStreamOps);

    DocumentAnalysisPort mockAnalysisPort = mock(DocumentAnalysisPort.class);

    AIAnalysisConsumer consumer = new AIAnalysisConsumer(mockAnalysisPort, mockRedis);

    Map<String, String> payload = new HashMap<>();
    payload.put("documentId", "doc-123");
    payload.put("tenantId", "tenant-test");
    payload.put("extractedText", ""); // Empty text
    payload.put("contentType", contentType);
    payload.put("status", "TEXT_EXTRACTED");

    StreamMessage message = new StreamMessage("documents.ready_for_analysis", "msg-1", payload);

    // Act
    consumer.handle(message);

    // Assert - AI should not be called for empty text
    verify(mockAnalysisPort, never()).analyze(any());

    @SuppressWarnings("unchecked")
    ArgumentCaptor<MapRecord<String, String, String>> captor =
        ArgumentCaptor.forClass(MapRecord.class);
    verify(mockStreamOps).add(captor.capture());

    MapRecord<String, String, String> record = captor.getValue();
    assertThat(record.getValue().get("fallback")).isEqualTo("true");
    assertThat(record.getValue().get("confidenceScore")).isEqualTo("0.0");
  }

  /**
   * Property: Fallback SHALL have empty risks and extracted fields.
   *
   * <p>Deterministic fallback cannot identify risks or extract fields without AI.
   */
  @Property(tries = 100)
  void should_haveEmptyRisksAndFields_whenUsingFallback(@ForAll("contentTypes") String contentType)
      throws Exception {

    // Arrange
    StringRedisTemplate mockRedis = mock(StringRedisTemplate.class);
    @SuppressWarnings("unchecked")
    StreamOperations<String, Object, Object> mockStreamOps = mock(StreamOperations.class);
    when(mockRedis.opsForStream()).thenReturn(mockStreamOps);

    DocumentAnalysisPort mockAnalysisPort = mock(DocumentAnalysisPort.class);
    when(mockAnalysisPort.analyze(any(DocumentAnalysisRequest.class)))
        .thenThrow(new RuntimeException("Ollama unavailable"));

    AIAnalysisConsumer consumer = new AIAnalysisConsumer(mockAnalysisPort, mockRedis);

    Map<String, String> payload = new HashMap<>();
    payload.put("documentId", "doc-123");
    payload.put("tenantId", "tenant-test");
    payload.put("extractedText", "Document content");
    payload.put("contentType", contentType);
    payload.put("status", "TEXT_EXTRACTED");

    StreamMessage message = new StreamMessage("documents.ready_for_analysis", "msg-1", payload);

    // Act
    consumer.handle(message);

    // Assert
    @SuppressWarnings("unchecked")
    ArgumentCaptor<MapRecord<String, String, String>> captor =
        ArgumentCaptor.forClass(MapRecord.class);
    verify(mockStreamOps).add(captor.capture());

    MapRecord<String, String, String> record = captor.getValue();
    assertThat(record.getValue().get("risks")).isEmpty();
    assertThat(record.getValue().get("extractedFields")).isEmpty();
  }

  // ─── Helper Methods ───────────────────────────────────────────────────────────

  private String getExpectedCategory(String contentType) {
    if (contentType == null) {
      return "unknown";
    }
    return switch (contentType.toLowerCase()) {
      case "application/pdf" -> "pdf-document";
      case "application/msword",
              "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ->
          "word-document";
      case "application/vnd.ms-excel",
              "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ->
          "spreadsheet";
      case "image/png", "image/jpeg", "image/gif", "image/webp" -> "image";
      case "text/plain" -> "text-document";
      default -> "general";
    };
  }

  // ─── Arbitrary Providers ──────────────────────────────────────────────────────

  @Provide
  Arbitrary<String> contentTypes() {
    return Arbitraries.of(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "image/png",
        "image/jpeg",
        "text/plain",
        "application/octet-stream",
        "application/json",
        null);
  }

  @Provide
  Arbitrary<String> knownContentTypes() {
    return Arbitraries.of(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "image/png",
        "image/jpeg",
        "image/gif",
        "image/webp",
        "text/plain");
  }

  @Provide
  Arbitrary<String> imageContentTypes() {
    return Arbitraries.of("image/png", "image/jpeg", "image/gif", "image/webp");
  }
}
