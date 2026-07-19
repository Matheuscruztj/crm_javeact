package com.atlasops.shared.domain.events;

import com.atlasops.shared.domain.DomainEvent;
import java.util.Objects;

/**
 * Published when a document upload is confirmed (checksum validated). Triggers text extraction in
 * the Worker Process.
 */
public final class DocumentUploadedEvent extends DomainEvent {

  private final String documentId;
  private final String contentType;
  private final String filename;
  private final String storagePath;

  public DocumentUploadedEvent(
      String documentId,
      String tenantId,
      String contentType,
      String filename,
      String storagePath,
      String correlationId) {
    super(tenantId, correlationId);
    this.documentId = Objects.requireNonNull(documentId, "documentId must not be null");
    this.contentType = Objects.requireNonNull(contentType, "contentType must not be null");
    this.filename = Objects.requireNonNull(filename, "filename must not be null");
    this.storagePath = Objects.requireNonNull(storagePath, "storagePath must not be null");
  }

  public String getDocumentId() {
    return documentId;
  }

  public String getContentType() {
    return contentType;
  }

  public String getFilename() {
    return filename;
  }

  public String getStoragePath() {
    return storagePath;
  }
}
