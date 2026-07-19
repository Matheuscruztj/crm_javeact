package com.atlasops.documents.application;

import com.atlasops.documents.domain.Document;
import com.atlasops.documents.domain.DocumentStatus;
import com.atlasops.documents.domain.ports.DocumentRepository;
import com.atlasops.documents.domain.ports.ObjectStoragePort;
import com.atlasops.shared.domain.exceptions.BusinessRuleViolationException;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.ports.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Use case for initiating a document upload by generating a presigned URL. The document must be in
 * PENDING_UPLOAD status. Storage path format: {tenantId}/{year}/{month}/{documentId}/{filename}
 */
public class InitiateUploadUseCase {

  private static final int PRESIGNED_URL_EXPIRY_MINUTES = 60;

  private final DocumentRepository documentRepository;
  private final ObjectStoragePort objectStoragePort;
  private final Clock clock;

  public InitiateUploadUseCase(
      DocumentRepository documentRepository, ObjectStoragePort objectStoragePort, Clock clock) {
    this.documentRepository = documentRepository;
    this.objectStoragePort = objectStoragePort;
    this.clock = clock;
  }

  /**
   * Generates a presigned upload URL for the specified document.
   *
   * @param command the initiate upload command
   * @return the upload result containing the presigned URL and storage path
   * @throws ResourceNotFoundException if the document is not found
   * @throws BusinessRuleViolationException if the document is not in PENDING_UPLOAD status
   */
  public InitiateUploadResult execute(InitiateUploadCommand command) {
    validateCommand(command);

    Document document =
        documentRepository
            .findById(command.documentId(), command.tenantId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Document not found: " + command.documentId()));

    if (document.getStatus() != DocumentStatus.PENDING_UPLOAD) {
      throw new BusinessRuleViolationException(
          "Document must be in PENDING_UPLOAD status to initiate upload. Current status: "
              + document.getStatus());
    }

    String storagePath = buildStoragePath(document, command.tenantId());

    String uploadUrl =
        objectStoragePort.generatePresignedUploadUrl(
            storagePath, document.getContentType().getMimeType(), PRESIGNED_URL_EXPIRY_MINUTES);

    return new InitiateUploadResult(uploadUrl, storagePath);
  }

  private String buildStoragePath(Document document, String tenantId) {
    LocalDate now = clock.now().atZone(ZoneOffset.UTC).toLocalDate();
    int year = now.getYear();
    int month = now.getMonthValue();

    return String.format(
        "%s/%d/%02d/%s/%s", tenantId, year, month, document.getId(), document.getFilename());
  }

  private void validateCommand(InitiateUploadCommand command) {
    if (command.documentId() == null || command.documentId().isBlank()) {
      throw new IllegalArgumentException("DocumentId must not be null or empty");
    }
    if (command.tenantId() == null || command.tenantId().isBlank()) {
      throw new IllegalArgumentException("TenantId must not be null or empty");
    }
  }
}
