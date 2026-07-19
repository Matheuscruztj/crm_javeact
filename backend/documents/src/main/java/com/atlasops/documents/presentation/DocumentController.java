package com.atlasops.documents.presentation;

import com.atlasops.documents.application.ConfirmUploadCommand;
import com.atlasops.documents.application.ConfirmUploadUseCase;
import com.atlasops.documents.application.InitiateUploadCommand;
import com.atlasops.documents.application.InitiateUploadResult;
import com.atlasops.documents.application.InitiateUploadUseCase;
import com.atlasops.documents.application.RegisterDocumentMetadataCommand;
import com.atlasops.documents.application.RegisterDocumentMetadataUseCase;
import com.atlasops.documents.domain.Document;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for document management operations.
 *
 * <p>Endpoints:
 *
 * <ul>
 *   <li>POST /api/v1/documents — register document metadata
 *   <li>POST /api/v1/documents/{id}/upload-url — get presigned upload URL
 *   <li>POST /api/v1/documents/{id}/confirm-upload — confirm upload after file transfer
 * </ul>
 *
 * <p>Validates: Requirements 9.1, 10.1, 10.2
 */
@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

  private final RegisterDocumentMetadataUseCase registerDocumentMetadataUseCase;
  private final InitiateUploadUseCase initiateUploadUseCase;
  private final ConfirmUploadUseCase confirmUploadUseCase;

  public DocumentController(
      RegisterDocumentMetadataUseCase registerDocumentMetadataUseCase,
      InitiateUploadUseCase initiateUploadUseCase,
      ConfirmUploadUseCase confirmUploadUseCase) {
    this.registerDocumentMetadataUseCase = registerDocumentMetadataUseCase;
    this.initiateUploadUseCase = initiateUploadUseCase;
    this.confirmUploadUseCase = confirmUploadUseCase;
  }

  /**
   * Registers document metadata, creating a document record with PENDING_UPLOAD status.
   *
   * @param tenantId the tenant identifier from header
   * @param request the document registration request
   * @return 201 Created with the document representation and Location header
   */
  @PostMapping
  public ResponseEntity<DocumentResponse> registerMetadata(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @Valid @RequestBody RegisterDocumentRequest request) {

    var command =
        new RegisterDocumentMetadataCommand(
            request.filename(),
            request.contentType(),
            request.fileSize(),
            request.checksum(),
            request.requestId(),
            tenantId);

    Document created = registerDocumentMetadataUseCase.execute(command);
    DocumentResponse response = DocumentResponse.from(created);
    URI location = URI.create("/api/v1/documents/" + created.getId());
    return ResponseEntity.created(location).body(response);
  }

  /**
   * Generates a presigned upload URL for the specified document.
   *
   * @param tenantId the tenant identifier from header
   * @param id the document identifier
   * @return 200 OK with the upload URL and storage path
   */
  @PostMapping("/{id}/upload-url")
  public ResponseEntity<UploadUrlResponse> getUploadUrl(
      @RequestHeader("X-Tenant-ID") String tenantId, @PathVariable String id) {

    var command = new InitiateUploadCommand(id, tenantId);
    InitiateUploadResult result = initiateUploadUseCase.execute(command);

    var response = new UploadUrlResponse(result.uploadUrl(), result.storagePath());
    return ResponseEntity.ok(response);
  }

  /**
   * Confirms that the document upload is complete by verifying the checksum.
   *
   * @param tenantId the tenant identifier from header
   * @param id the document identifier
   * @param request the confirm upload request containing the storage path
   * @return 200 OK with the updated document representation
   */
  @PostMapping("/{id}/confirm-upload")
  public ResponseEntity<DocumentResponse> confirmUpload(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId,
      @PathVariable String id,
      @Valid @RequestBody ConfirmUploadRequest request) {

    var command = new ConfirmUploadCommand(id, request.storagePath(), tenantId, correlationId);
    Document updated = confirmUploadUseCase.execute(command);

    return ResponseEntity.ok(DocumentResponse.from(updated));
  }
}
