package com.atlasops.documents.domain;

import com.atlasops.shared.domain.AggregateRoot;
import com.atlasops.shared.domain.events.DocumentAnalyzedEvent;
import com.atlasops.shared.domain.events.DocumentReadyForAnalysisEvent;
import com.atlasops.shared.domain.events.DocumentUploadedEvent;
import com.atlasops.shared.domain.types.TenantId;
import java.time.Instant;
import java.util.Objects;

/**
 * Document aggregate root representing a document within a tenant. Manages the document lifecycle
 * from registration through upload, text extraction, and analysis.
 */
public final class Document extends AggregateRoot<String> {

  /** Maximum allowed file size: 2 GB in bytes. */
  public static final long MAX_FILE_SIZE_BYTES = 2L * 1024 * 1024 * 1024;

  private static final int FILENAME_MIN_LENGTH = 1;
  private static final int FILENAME_MAX_LENGTH = 255;

  private final TenantId tenantId;
  private final String requestId;
  private final String filename;
  private final AllowedContentType contentType;
  private final long fileSize;
  private final String checksum;
  private String storagePath;
  private DocumentStatus status;
  private String analysisResult;
  private final Instant createdAt;
  private Instant updatedAt;

  private Document(
      String id,
      TenantId tenantId,
      String requestId,
      String filename,
      AllowedContentType contentType,
      long fileSize,
      String checksum,
      String storagePath,
      DocumentStatus status,
      String analysisResult,
      Instant createdAt,
      Instant updatedAt) {
    super(id);
    this.tenantId = Objects.requireNonNull(tenantId, "TenantId must not be null");
    this.requestId = requestId;
    this.filename = Objects.requireNonNull(filename, "Filename must not be null");
    this.contentType = Objects.requireNonNull(contentType, "ContentType must not be null");
    this.fileSize = fileSize;
    this.checksum = Objects.requireNonNull(checksum, "Checksum must not be null");
    this.storagePath = storagePath;
    this.status = Objects.requireNonNull(status, "Status must not be null");
    this.analysisResult = analysisResult;
    this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt must not be null");
    this.updatedAt = Objects.requireNonNull(updatedAt, "UpdatedAt must not be null");
  }

  /**
   * Factory method to create a new Document in PENDING_UPLOAD status.
   *
   * @param id unique identifier (UUID string)
   * @param tenantId tenant this document belongs to
   * @param requestId optional associated request identifier
   * @param filename document filename (1-255 characters)
   * @param contentType the content type (must be in AllowedContentType)
   * @param fileSize declared file size in bytes (must be ≤ 2GB)
   * @param checksum SHA-256 checksum declared at registration
   * @param now current timestamp
   * @return a new Document instance with PENDING_UPLOAD status
   */
  public static Document create(
      String id,
      TenantId tenantId,
      String requestId,
      String filename,
      AllowedContentType contentType,
      long fileSize,
      String checksum,
      Instant now) {
    validateFilename(filename);
    validateFileSize(fileSize);
    validateChecksum(checksum);

    return new Document(
        id,
        tenantId,
        requestId,
        filename,
        contentType,
        fileSize,
        checksum,
        null,
        DocumentStatus.PENDING_UPLOAD,
        null,
        now,
        now);
  }

  /** Reconstitutes a Document from persisted data, preserving all fields as-is. */
  public static Document reconstitute(
      String id,
      TenantId tenantId,
      String requestId,
      String filename,
      AllowedContentType contentType,
      long fileSize,
      String checksum,
      String storagePath,
      DocumentStatus status,
      String analysisResult,
      Instant createdAt,
      Instant updatedAt) {
    return new Document(
        id,
        tenantId,
        requestId,
        filename,
        contentType,
        fileSize,
        checksum,
        storagePath,
        status,
        analysisResult,
        createdAt,
        updatedAt);
  }

  /**
   * Confirms the upload by transitioning from PENDING_UPLOAD to UPLOADED. Sets the storage path and
   * publishes a DocumentUploadedEvent.
   *
   * @param storagePath the path in object storage where the file was uploaded
   * @param correlationId the correlation ID for tracing
   * @param now current timestamp
   * @throws IllegalStateException if the document is not in PENDING_UPLOAD status
   */
  public void confirmUpload(String storagePath, String correlationId, Instant now) {
    Objects.requireNonNull(storagePath, "StoragePath must not be null");
    Objects.requireNonNull(now, "Timestamp must not be null");
    assertTransition(DocumentStatus.UPLOADED);
    this.storagePath = storagePath;
    this.status = DocumentStatus.UPLOADED;
    this.updatedAt = now;
    registerEvent(
        new DocumentUploadedEvent(
            getId(),
            tenantId.getValue(),
            contentType.getMimeType(),
            filename,
            storagePath,
            correlationId));
  }

  /**
   * Marks the upload as failed, transitioning from PENDING_UPLOAD to UPLOAD_FAILED.
   *
   * @param now current timestamp
   * @throws IllegalStateException if the document is not in PENDING_UPLOAD status
   */
  public void markUploadFailed(Instant now) {
    Objects.requireNonNull(now, "Timestamp must not be null");
    assertTransition(DocumentStatus.UPLOAD_FAILED);
    this.status = DocumentStatus.UPLOAD_FAILED;
    this.updatedAt = now;
  }

  /**
   * Marks text extraction as complete, transitioning from UPLOADED to TEXT_EXTRACTED. Publishes a
   * DocumentReadyForAnalysisEvent.
   *
   * @param correlationId the correlation ID for tracing
   * @param now current timestamp
   * @throws IllegalStateException if the document is not in UPLOADED status
   */
  public void markTextExtracted(String correlationId, Instant now) {
    Objects.requireNonNull(now, "Timestamp must not be null");
    assertTransition(DocumentStatus.TEXT_EXTRACTED);
    this.status = DocumentStatus.TEXT_EXTRACTED;
    this.updatedAt = now;
    registerEvent(new DocumentReadyForAnalysisEvent(getId(), tenantId.getValue(), correlationId));
  }

  /**
   * Marks the document as analyzed, transitioning from TEXT_EXTRACTED to ANALYZED. Stores the
   * analysis result and publishes a DocumentAnalyzedEvent.
   *
   * @param analysisResult JSON string containing the analysis result
   * @param fallbackUsed whether a deterministic fallback was used instead of AI
   * @param correlationId the correlation ID for tracing
   * @param now current timestamp
   * @throws IllegalStateException if the document is not in TEXT_EXTRACTED status
   */
  public void markAnalyzed(
      String analysisResult, boolean fallbackUsed, String correlationId, Instant now) {
    Objects.requireNonNull(now, "Timestamp must not be null");
    assertTransition(DocumentStatus.ANALYZED);
    this.analysisResult = analysisResult;
    this.status = DocumentStatus.ANALYZED;
    this.updatedAt = now;
    registerEvent(
        new DocumentAnalyzedEvent(getId(), tenantId.getValue(), fallbackUsed, correlationId));
  }

  /**
   * Marks the document as having a processing failure. Valid from UPLOADED or TEXT_EXTRACTED
   * statuses.
   *
   * @param now current timestamp
   * @throws IllegalStateException if the current status cannot transition to PROCESSING_FAILED
   */
  public void markProcessingFailed(Instant now) {
    Objects.requireNonNull(now, "Timestamp must not be null");
    assertTransition(DocumentStatus.PROCESSING_FAILED);
    this.status = DocumentStatus.PROCESSING_FAILED;
    this.updatedAt = now;
  }

  /**
   * Marks the document for reprocessing, transitioning from ANALYZED or PROCESSING_FAILED
   * back to UPLOADED so the worker re-processes it.
   *
   * <p>Clears the previous analysis result and re-publishes the {@link DocumentUploadedEvent}
   * so the worker picks it up again.
   *
   * @param correlationId the correlation ID for tracing
   * @param now current timestamp
   * @throws IllegalStateException if the document is not in ANALYZED or PROCESSING_FAILED status
   */
  public void reprocess(String correlationId, Instant now) {
    Objects.requireNonNull(now, "Timestamp must not be null");
    assertTransition(DocumentStatus.UPLOADED);
    this.analysisResult = null;
    this.status = DocumentStatus.UPLOADED;
    this.updatedAt = now;
    registerEvent(
        new DocumentUploadedEvent(
            getId(),
            tenantId.getValue(),
            contentType.getMimeType(),
            filename,
            storagePath,
            correlationId));
  }

  private void assertTransition(DocumentStatus target) {
    if (!this.status.canTransitionTo(target)) {
      throw new IllegalStateException("Cannot transition from " + this.status + " to " + target);
    }
  }

  private static void validateFilename(String filename) {
    if (filename == null || filename.isBlank()) {
      throw new IllegalArgumentException("Filename must not be null or empty");
    }
    if (filename.length() < FILENAME_MIN_LENGTH || filename.length() > FILENAME_MAX_LENGTH) {
      throw new IllegalArgumentException(
          "Filename must be between "
              + FILENAME_MIN_LENGTH
              + " and "
              + FILENAME_MAX_LENGTH
              + " characters, got: "
              + filename.length());
    }
  }

  private static void validateFileSize(long fileSize) {
    if (fileSize <= 0) {
      throw new IllegalArgumentException("File size must be greater than zero");
    }
    if (fileSize > MAX_FILE_SIZE_BYTES) {
      throw new IllegalArgumentException(
          "File size must not exceed " + MAX_FILE_SIZE_BYTES + " bytes (2 GB), got: " + fileSize);
    }
  }

  private static void validateChecksum(String checksum) {
    if (checksum == null || checksum.isBlank()) {
      throw new IllegalArgumentException("Checksum must not be null or empty");
    }
    // SHA-256 produces 64 hex characters
    if (!checksum.matches("^[a-fA-F0-9]{64}$")) {
      throw new IllegalArgumentException(
          "Checksum must be a valid SHA-256 hex string (64 characters)");
    }
  }

  // --- Getters ---

  public TenantId getTenantId() {
    return tenantId;
  }

  public String getRequestId() {
    return requestId;
  }

  public String getFilename() {
    return filename;
  }

  public AllowedContentType getContentType() {
    return contentType;
  }

  public long getFileSize() {
    return fileSize;
  }

  public String getChecksum() {
    return checksum;
  }

  public String getStoragePath() {
    return storagePath;
  }

  public DocumentStatus getStatus() {
    return status;
  }

  public String getAnalysisResult() {
    return analysisResult;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
