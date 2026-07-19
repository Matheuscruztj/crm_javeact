package com.atlasops.documents.application;

import com.atlasops.documents.domain.Document;
import com.atlasops.documents.domain.DocumentStatus;
import com.atlasops.documents.domain.ports.DocumentRepository;
import com.atlasops.documents.domain.ports.ObjectStoragePort;
import com.atlasops.shared.domain.exceptions.BusinessRuleViolationException;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.EventPublisher;

/**
 * Use case for confirming a document upload. Validates the checksum (declared vs actual from
 * MinIO), transitions to UPLOADED and publishes DocumentUploadedEvent on match; on mismatch
 * transitions to UPLOAD_FAILED and deletes from MinIO.
 */
public class ConfirmUploadUseCase {

  private final DocumentRepository documentRepository;
  private final ObjectStoragePort objectStoragePort;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public ConfirmUploadUseCase(
      DocumentRepository documentRepository,
      ObjectStoragePort objectStoragePort,
      EventPublisher eventPublisher,
      Clock clock) {
    this.documentRepository = documentRepository;
    this.objectStoragePort = objectStoragePort;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  /**
   * Confirms the upload of a document by validating the checksum.
   *
   * @param command the confirm upload command
   * @return the updated Document
   * @throws ResourceNotFoundException if the document is not found
   * @throws BusinessRuleViolationException if the document is not in PENDING_UPLOAD status
   * @throws ChecksumMismatchException if the actual checksum does not match the declared checksum
   */
  public Document execute(ConfirmUploadCommand command) {
    validateCommand(command);

    Document document =
        documentRepository
            .findById(command.documentId(), command.tenantId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Document not found: " + command.documentId()));

    if (document.getStatus() != DocumentStatus.PENDING_UPLOAD) {
      throw new BusinessRuleViolationException(
          "Document must be in PENDING_UPLOAD status to confirm upload. Current status: "
              + document.getStatus());
    }

    String actualChecksum = objectStoragePort.getObjectChecksum(command.storagePath());

    if (!document.getChecksum().equalsIgnoreCase(actualChecksum)) {
      document.markUploadFailed(clock.now());
      objectStoragePort.deleteObject(command.storagePath());
      documentRepository.save(document);
      throw new ChecksumMismatchException(
          "Checksum mismatch: declared=" + document.getChecksum() + ", actual=" + actualChecksum);
    }

    document.confirmUpload(command.storagePath(), command.correlationId(), clock.now());
    Document saved = documentRepository.save(document);

    saved.getDomainEvents().forEach(eventPublisher::publish);
    saved.clearDomainEvents();

    return saved;
  }

  private void validateCommand(ConfirmUploadCommand command) {
    if (command.documentId() == null || command.documentId().isBlank()) {
      throw new IllegalArgumentException("DocumentId must not be null or empty");
    }
    if (command.storagePath() == null || command.storagePath().isBlank()) {
      throw new IllegalArgumentException("StoragePath must not be null or empty");
    }
    if (command.tenantId() == null || command.tenantId().isBlank()) {
      throw new IllegalArgumentException("TenantId must not be null or empty");
    }
  }
}
