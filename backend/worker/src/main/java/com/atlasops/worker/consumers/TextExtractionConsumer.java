package com.atlasops.worker.consumers;

import com.atlasops.worker.infrastructure.redis.MessageHandler;
import com.atlasops.worker.infrastructure.redis.StreamMessage;
import com.atlasops.worker.infrastructure.retry.RetryExecutor;
import com.atlasops.worker.infrastructure.retry.TaskResult;
import java.io.InputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

/**
 * Consumer for document text extraction from the documents.uploaded stream. Extracts text using
 * Apache Tika and publishes DocumentReadyForAnalysisEvent.
 *
 * <p>Validates: Requirements 11.1, 11.2, 11.3, 11.4, 11.5, 11.7
 */
@Component
public class TextExtractionConsumer implements MessageHandler {

  private static final Logger log = LoggerFactory.getLogger(TextExtractionConsumer.class);
  private static final String STREAM_KEY = "documents.uploaded";
  private static final String OUTPUT_STREAM = "documents.ready_for_analysis";
  private static final int MAX_TEXT_LENGTH = 10 * 1024 * 1024; // 10MB
  private static final Set<String> SKIP_EXTRACTION_CONTENT_TYPES =
      Set.of("image/png", "image/jpeg", "image/gif", "image/webp");

  private final RetryExecutor retryExecutor;
  private final StringRedisTemplate redisTemplate;
  private final S3Client s3Client;
  private final String bucketName;
  private final Tika tika;

  public TextExtractionConsumer(
      RetryExecutor retryExecutor,
      StringRedisTemplate redisTemplate,
      S3Client s3Client,
      @Value("${atlasops.storage.bucket:atlasops-documents}") String bucketName) {
    this.retryExecutor = retryExecutor;
    this.redisTemplate = redisTemplate;
    this.s3Client = s3Client;
    this.bucketName = bucketName;
    this.tika = new Tika();
  }

  public String getStreamKey() {
    return STREAM_KEY;
  }

  @Override
  public void handle(StreamMessage message) throws Exception {
    String documentId = message.getRequired("documentId");
    String tenantId = message.getRequired("tenantId");
    String storagePath = message.getRequired("storagePath");
    String contentType = message.get("contentType");
    String currentStatus = message.get("status");

    // Idempotency check - skip if document is beyond UPLOADED status
    if (currentStatus != null && !currentStatus.equals("UPLOADED")) {
      log.info("Skipping document {} - already processed (status: {})", documentId, currentStatus);
      return;
    }

    log.info(
        "Starting text extraction for document {} (tenant: {}, contentType: {})",
        documentId,
        tenantId,
        contentType);

    TaskResult result =
        retryExecutor.executeWithRetry(
            "text-extraction:" + documentId,
            message,
            () -> extractAndPublish(documentId, tenantId, storagePath, contentType));

    if (result instanceof TaskResult.MovedToDlq dlq) {
      log.error(
          "Document {} text extraction failed after {} attempts, moved to DLQ",
          documentId,
          dlq.totalAttempts());
      publishStatusUpdate(documentId, tenantId, "PROCESSING_FAILED");
    }
  }

  private void extractAndPublish(
      String documentId, String tenantId, String storagePath, String contentType) throws Exception {

    String extractedText;

    if (SKIP_EXTRACTION_CONTENT_TYPES.contains(contentType)) {
      log.info("Skipping text extraction for image document {}", documentId);
      extractedText = "";
    } else {
      extractedText = extractText(storagePath);
    }

    publishReadyForAnalysis(documentId, tenantId, storagePath, contentType, extractedText);
    log.info(
        "Text extraction completed for document {} ({} chars)", documentId, extractedText.length());
  }

  private String extractText(String storagePath) throws Exception {
    GetObjectRequest request =
        GetObjectRequest.builder().bucket(bucketName).key(storagePath).build();

    try (InputStream inputStream = s3Client.getObject(request)) {
      Metadata metadata = new Metadata();
      String text = tika.parseToString(inputStream, metadata, MAX_TEXT_LENGTH);
      return text != null ? text : "";
    }
  }

  private void publishReadyForAnalysis(
      String documentId,
      String tenantId,
      String storagePath,
      String contentType,
      String extractedText) {

    Map<String, String> payload = new HashMap<>();
    payload.put("documentId", documentId);
    payload.put("tenantId", tenantId);
    payload.put("storagePath", storagePath);
    payload.put("contentType", contentType != null ? contentType : "");
    payload.put("extractedText", extractedText);
    payload.put("status", "TEXT_EXTRACTED");
    payload.put("timestamp", Instant.now().toString());

    var record = StreamRecords.string(payload).withStreamKey(OUTPUT_STREAM);
    redisTemplate.opsForStream().add(record);

    log.debug("Published DocumentReadyForAnalysisEvent for document {}", documentId);
  }

  private void publishStatusUpdate(String documentId, String tenantId, String status) {
    Map<String, String> payload = new HashMap<>();
    payload.put("documentId", documentId);
    payload.put("tenantId", tenantId);
    payload.put("status", status);
    payload.put("timestamp", Instant.now().toString());

    var record = StreamRecords.string(payload).withStreamKey("documents.status_updates");
    redisTemplate.opsForStream().add(record);
  }
}
