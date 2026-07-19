package com.atlasops.documents.application;

import com.atlasops.documents.domain.AllowedContentType;
import com.atlasops.documents.domain.Document;
import com.atlasops.documents.domain.ports.DocumentRepository;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import com.atlasops.shared.domain.types.TenantId;

/**
 * Use case for registering document metadata before upload. Validates content type, file size, and
 * checksum format, then creates a document with PENDING_UPLOAD status.
 */
public class RegisterDocumentMetadataUseCase {

  private final DocumentRepository documentRepository;
  private final IdGenerator idGenerator;
  private final Clock clock;

  public RegisterDocumentMetadataUseCase(
      DocumentRepository documentRepository, IdGenerator idGenerator, Clock clock) {
    this.documentRepository = documentRepository;
    this.idGenerator = idGenerator;
    this.clock = clock;
  }

  /**
   * Registers document metadata and creates a document record with PENDING_UPLOAD status.
   *
   * @param command the registration command
   * @return the created Document
   * @throws IllegalArgumentException if content type is not supported, file size exceeds 2GB, or
   *     checksum format is invalid
   */
  public Document execute(RegisterDocumentMetadataCommand command) {
    validateCommand(command);

    AllowedContentType contentType =
        AllowedContentType.fromMimeType(command.contentType())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Unsupported content type: "
                            + command.contentType()
                            + ". Supported types: "
                            + AllowedContentType.supportedMimeTypes()));

    TenantId tenantId = new TenantId(command.tenantId());
    String id = idGenerator.generate();

    Document document =
        Document.create(
            id,
            tenantId,
            command.requestId(),
            command.filename(),
            contentType,
            command.fileSize(),
            command.checksum(),
            clock.now());

    return documentRepository.save(document);
  }

  private void validateCommand(RegisterDocumentMetadataCommand command) {
    if (command.filename() == null || command.filename().isBlank()) {
      throw new IllegalArgumentException("Filename must not be null or empty");
    }
    if (command.contentType() == null || command.contentType().isBlank()) {
      throw new IllegalArgumentException("Content type must not be null or empty");
    }
    if (command.checksum() == null || command.checksum().isBlank()) {
      throw new IllegalArgumentException("Checksum must not be null or empty");
    }
    if (command.tenantId() == null || command.tenantId().isBlank()) {
      throw new IllegalArgumentException("TenantId must not be null or empty");
    }
  }
}
