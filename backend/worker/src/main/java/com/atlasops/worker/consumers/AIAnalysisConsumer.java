package com.atlasops.worker.consumers;

import com.atlasops.ai.domain.DocumentAnalysisRequest;
import com.atlasops.ai.domain.DocumentAnalysisResult;
import com.atlasops.ai.domain.ports.DocumentAnalysisPort;
import com.atlasops.worker.infrastructure.redis.MessageHandler;
import com.atlasops.worker.infrastructure.redis.StreamMessage;
import java.time.Instant;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Consumer for AI document analysis from the documents.ready_for_analysis stream. Invokes
 * DocumentAnalysisPort to analyze extracted text and publishes DocumentAnalyzedEvent.
 *
 * <p>On Ollama unavailable/timeout (30s): executes deterministic fallback (category from content
 * type, word count, empty risks, confidence 0.0, fallback=true).
 *
 * <p>Validates: Requirements 12.1, 12.2, 12.3, 12.4, 12.5
 */
@Component
public class AIAnalysisConsumer implements MessageHandler {

  private static final Logger log = LoggerFactory.getLogger(AIAnalysisConsumer.class);
  private static final String STREAM_KEY = "documents.ready_for_analysis";
  private static final String OUTPUT_STREAM = "documents.analyzed";
  private static final String DEFAULT_PROMPT_VERSION = "analysis:v1";
  private static final String DEFAULT_OUTPUT_SCHEMA = "standard-analysis";
  private static final String AI_ANALYSIS_DURATION = "ai_analysis_duration_seconds";
  private static final String DOCUMENT_PROCESSING_DURATION =
      "document_processing_duration_seconds";
  private static final String AI_FALLBACK_TOTAL = "ai_fallback_total";

  private final DocumentAnalysisPort documentAnalysisPort;
  private final StringRedisTemplate redisTemplate;
  private final MeterRegistry meterRegistry;

  public AIAnalysisConsumer(
      DocumentAnalysisPort documentAnalysisPort,
      StringRedisTemplate redisTemplate,
      MeterRegistry meterRegistry) {
    this.documentAnalysisPort = documentAnalysisPort;
    this.redisTemplate = redisTemplate;
    this.meterRegistry = meterRegistry;
  }

  public AIAnalysisConsumer(
      DocumentAnalysisPort documentAnalysisPort, StringRedisTemplate redisTemplate) {
    this(documentAnalysisPort, redisTemplate, new SimpleMeterRegistry());
  }

  public String getStreamKey() {
    return STREAM_KEY;
  }

  @Override
  public void handle(StreamMessage message) throws Exception {
    String documentId = message.getRequired("documentId");
    String tenantId = message.getRequired("tenantId");
    String extractedText = message.get("extractedText");
    String contentType = message.get("contentType");
    String currentStatus = message.get("status");

    // Idempotency check - skip if document is beyond TEXT_EXTRACTED status
    if (currentStatus != null && !currentStatus.equals("TEXT_EXTRACTED")) {
      log.info("Skipping document {} - already processed (status: {})", documentId, currentStatus);
      return;
    }

    log.info("Starting AI analysis for document {} (tenant: {})", documentId, tenantId);

    DocumentAnalysisResult result;
    Instant startedAt = Instant.now();
    boolean usedFallback;

    // Handle empty or image documents with deterministic fallback
    if (extractedText == null || extractedText.isBlank()) {
      log.info("Document {} has no extracted text, using deterministic fallback", documentId);
      result = createDeterministicFallback(contentType);
      usedFallback = true;
    } else {
      try {
        DocumentAnalysisRequest request =
            new DocumentAnalysisRequest(
                tenantId, documentId, extractedText, DEFAULT_PROMPT_VERSION, DEFAULT_OUTPUT_SCHEMA);

        result = documentAnalysisPort.analyze(request);
        usedFallback = result.fallback();
      } catch (Exception e) {
        log.warn(
            "AI analysis failed for document {}, using deterministic fallback: {}",
            documentId,
            e.getMessage());
        result = createDeterministicFallback(contentType);
        usedFallback = true;
      }
    }

    recordMetrics(startedAt, usedFallback);
    publishAnalyzedEvent(documentId, tenantId, result, extractedText);
    log.info(
        "AI analysis completed for document {} (fallback: {}, confidence: {})",
        documentId,
        result.fallback(),
        result.confidenceScore());
  }

  /**
   * Creates a deterministic fallback result when Ollama is unavailable or document has no text.
   *
   * @param contentType the document content type for category inference
   * @return a DocumentAnalysisResult with fallback=true
   */
  private DocumentAnalysisResult createDeterministicFallback(String contentType) {
    String category = inferCategoryFromContentType(contentType);

    return new DocumentAnalysisResult(
        "Document analysis unavailable - processed with fallback",
        category,
        List.of(), // no extracted fields
        List.of(), // no risks
        List.of(), // no missing information
        0.0, // confidence score = 0
        "deterministic-fallback:v1",
        true // fallback = true
        );
  }

  /**
   * Infers document category from content type.
   *
   * @param contentType the MIME type of the document
   * @return the inferred category
   */
  private String inferCategoryFromContentType(String contentType) {
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

  private void publishAnalyzedEvent(
      String documentId, String tenantId, DocumentAnalysisResult result, String extractedText) {

    Map<String, String> payload = new HashMap<>();
    payload.put("documentId", documentId);
    payload.put("tenantId", tenantId);
    payload.put("summary", result.summary());
    payload.put("category", result.category());
    payload.put("extractedFields", serializeKeyValuePairs(result.extractedFields()));
    payload.put("risks", String.join("|", result.risks()));
    payload.put("missingInformation", String.join("|", result.missingInformation()));
    payload.put("confidenceScore", String.valueOf(result.confidenceScore()));
    payload.put("providerMetadata", result.providerMetadata());
    payload.put("fallback", String.valueOf(result.fallback()));
    payload.put("wordCount", String.valueOf(countWords(extractedText)));
    payload.put("status", "ANALYZED");
    payload.put("timestamp", Instant.now().toString());

    var record = StreamRecords.string(payload).withStreamKey(OUTPUT_STREAM);
    redisTemplate.opsForStream().add(record);

    log.debug("Published DocumentAnalyzedEvent for document {}", documentId);
  }

  private void recordMetrics(Instant startedAt, boolean usedFallback) {
    Duration duration = Duration.between(startedAt, Instant.now());
    meterRegistry.timer(AI_ANALYSIS_DURATION).record(duration);
    meterRegistry.timer(DOCUMENT_PROCESSING_DURATION).record(duration);
    if (usedFallback) {
      meterRegistry.counter(AI_FALLBACK_TOTAL).increment();
    }
  }

  private String serializeKeyValuePairs(List<com.atlasops.ai.domain.KeyValuePair> pairs) {
    if (pairs == null || pairs.isEmpty()) {
      return "";
    }
    return pairs.stream().map(p -> p.key() + "=" + p.value()).collect(Collectors.joining("|"));
  }

  private int countWords(String text) {
    if (text == null || text.isBlank()) {
      return 0;
    }
    return text.trim().split("\\s+").length;
  }
}
