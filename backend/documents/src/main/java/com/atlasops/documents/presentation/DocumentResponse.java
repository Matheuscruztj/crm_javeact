package com.atlasops.documents.presentation;

import com.atlasops.documents.domain.Document;
import java.time.Instant;

/**
 * Response DTO representing a document.
 *
 * @param id the document identifier
 * @param tenantId the tenant identifier
 * @param requestId the associated request identifier (nullable)
 * @param filename the document filename
 * @param contentType the MIME content type
 * @param fileSize the file size in bytes
 * @param checksum the SHA-256 checksum
 * @param storagePath the storage path (nullable until upload confirmed)
 * @param status the current document status
 * @param createdAt the creation timestamp
 * @param updatedAt the last update timestamp
 */
public record DocumentResponse(
    String id,
    String tenantId,
    String requestId,
    String filename,
    String contentType,
    long fileSize,
    String checksum,
    String storagePath,
    String status,
    Instant createdAt,
    Instant updatedAt) {

  /**
   * Creates a DocumentResponse from a Document domain object.
   *
   * @param document the domain document
   * @return the response DTO
   */
  public static DocumentResponse from(Document document) {
    return new DocumentResponse(
        document.getId(),
        document.getTenantId().getValue(),
        document.getRequestId(),
        document.getFilename(),
        document.getContentType().getMimeType(),
        document.getFileSize(),
        document.getChecksum(),
        document.getStoragePath(),
        document.getStatus().name(),
        document.getCreatedAt(),
        document.getUpdatedAt());
  }
}
