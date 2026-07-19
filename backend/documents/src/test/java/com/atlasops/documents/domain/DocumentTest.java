package com.atlasops.documents.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.atlasops.shared.domain.DomainEvent;
import com.atlasops.shared.domain.events.DocumentAnalyzedEvent;
import com.atlasops.shared.domain.events.DocumentReadyForAnalysisEvent;
import com.atlasops.shared.domain.events.DocumentUploadedEvent;
import com.atlasops.shared.domain.types.TenantId;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the Document aggregate root. Tests creation, validation, and status transitions.
 */
class DocumentTest {

  private static final String VALID_ID = "doc-001";
  private static final TenantId VALID_TENANT_ID = new TenantId("tenant-alpha");
  private static final String VALID_REQUEST_ID = "req-001";
  private static final String VALID_FILENAME = "contract.pdf";
  private static final AllowedContentType VALID_CONTENT_TYPE = AllowedContentType.PDF;
  private static final long VALID_FILE_SIZE = 1024 * 1024; // 1 MB
  private static final String VALID_CHECKSUM = "a".repeat(64);
  private static final Instant FIXED_NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final String CORRELATION_ID = "corr-123";
  private static final String STORAGE_PATH = "tenant-alpha/2025/01/doc-001/contract.pdf";

  // --- Creation Tests ---

  @Test
  void should_createDocument_when_allFieldsValid() {
    Document doc =
        Document.create(
            VALID_ID,
            VALID_TENANT_ID,
            VALID_REQUEST_ID,
            VALID_FILENAME,
            VALID_CONTENT_TYPE,
            VALID_FILE_SIZE,
            VALID_CHECKSUM,
            FIXED_NOW);

    assertThat(doc.getId()).isEqualTo(VALID_ID);
    assertThat(doc.getTenantId()).isEqualTo(VALID_TENANT_ID);
    assertThat(doc.getRequestId()).isEqualTo(VALID_REQUEST_ID);
    assertThat(doc.getFilename()).isEqualTo(VALID_FILENAME);
    assertThat(doc.getContentType()).isEqualTo(VALID_CONTENT_TYPE);
    assertThat(doc.getFileSize()).isEqualTo(VALID_FILE_SIZE);
    assertThat(doc.getChecksum()).isEqualTo(VALID_CHECKSUM);
    assertThat(doc.getStatus()).isEqualTo(DocumentStatus.PENDING_UPLOAD);
    assertThat(doc.getStoragePath()).isNull();
    assertThat(doc.getAnalysisResult()).isNull();
    assertThat(doc.getCreatedAt()).isEqualTo(FIXED_NOW);
    assertThat(doc.getUpdatedAt()).isEqualTo(FIXED_NOW);
  }

  @Test
  void should_createDocument_when_requestIdIsNull() {
    Document doc =
        Document.create(
            VALID_ID,
            VALID_TENANT_ID,
            null,
            VALID_FILENAME,
            VALID_CONTENT_TYPE,
            VALID_FILE_SIZE,
            VALID_CHECKSUM,
            FIXED_NOW);

    assertThat(doc.getRequestId()).isNull();
    assertThat(doc.getStatus()).isEqualTo(DocumentStatus.PENDING_UPLOAD);
  }

  // --- Validation Tests ---

  @Test
  void should_rejectCreation_when_filenameIsNull() {
    assertThatThrownBy(
            () ->
                Document.create(
                    VALID_ID,
                    VALID_TENANT_ID,
                    VALID_REQUEST_ID,
                    null,
                    VALID_CONTENT_TYPE,
                    VALID_FILE_SIZE,
                    VALID_CHECKSUM,
                    FIXED_NOW))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Filename");
  }

  @Test
  void should_rejectCreation_when_filenameIsBlank() {
    assertThatThrownBy(
            () ->
                Document.create(
                    VALID_ID,
                    VALID_TENANT_ID,
                    VALID_REQUEST_ID,
                    "   ",
                    VALID_CONTENT_TYPE,
                    VALID_FILE_SIZE,
                    VALID_CHECKSUM,
                    FIXED_NOW))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Filename");
  }

  @Test
  void should_rejectCreation_when_filenameTooLong() {
    String longFilename = "a".repeat(256);
    assertThatThrownBy(
            () ->
                Document.create(
                    VALID_ID,
                    VALID_TENANT_ID,
                    VALID_REQUEST_ID,
                    longFilename,
                    VALID_CONTENT_TYPE,
                    VALID_FILE_SIZE,
                    VALID_CHECKSUM,
                    FIXED_NOW))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("255");
  }

  @Test
  void should_rejectCreation_when_fileSizeExceeds2GB() {
    long oversizedFile = Document.MAX_FILE_SIZE_BYTES + 1;
    assertThatThrownBy(
            () ->
                Document.create(
                    VALID_ID,
                    VALID_TENANT_ID,
                    VALID_REQUEST_ID,
                    VALID_FILENAME,
                    VALID_CONTENT_TYPE,
                    oversizedFile,
                    VALID_CHECKSUM,
                    FIXED_NOW))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("2 GB");
  }

  @Test
  void should_rejectCreation_when_fileSizeIsZero() {
    assertThatThrownBy(
            () ->
                Document.create(
                    VALID_ID,
                    VALID_TENANT_ID,
                    VALID_REQUEST_ID,
                    VALID_FILENAME,
                    VALID_CONTENT_TYPE,
                    0,
                    VALID_CHECKSUM,
                    FIXED_NOW))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("greater than zero");
  }

  @Test
  void should_rejectCreation_when_fileSizeIsNegative() {
    assertThatThrownBy(
            () ->
                Document.create(
                    VALID_ID,
                    VALID_TENANT_ID,
                    VALID_REQUEST_ID,
                    VALID_FILENAME,
                    VALID_CONTENT_TYPE,
                    -100,
                    VALID_CHECKSUM,
                    FIXED_NOW))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("greater than zero");
  }

  @Test
  void should_rejectCreation_when_checksumIsInvalid() {
    assertThatThrownBy(
            () ->
                Document.create(
                    VALID_ID,
                    VALID_TENANT_ID,
                    VALID_REQUEST_ID,
                    VALID_FILENAME,
                    VALID_CONTENT_TYPE,
                    VALID_FILE_SIZE,
                    "not-a-sha256",
                    FIXED_NOW))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("SHA-256");
  }

  @Test
  void should_rejectCreation_when_checksumIsNull() {
    assertThatThrownBy(
            () ->
                Document.create(
                    VALID_ID,
                    VALID_TENANT_ID,
                    VALID_REQUEST_ID,
                    VALID_FILENAME,
                    VALID_CONTENT_TYPE,
                    VALID_FILE_SIZE,
                    null,
                    FIXED_NOW))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Checksum");
  }

  @Test
  void should_rejectCreation_when_tenantIdIsNull() {
    assertThatThrownBy(
            () ->
                Document.create(
                    VALID_ID,
                    null,
                    VALID_REQUEST_ID,
                    VALID_FILENAME,
                    VALID_CONTENT_TYPE,
                    VALID_FILE_SIZE,
                    VALID_CHECKSUM,
                    FIXED_NOW))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("TenantId");
  }

  @Test
  void should_rejectCreation_when_contentTypeIsNull() {
    assertThatThrownBy(
            () ->
                Document.create(
                    VALID_ID,
                    VALID_TENANT_ID,
                    VALID_REQUEST_ID,
                    VALID_FILENAME,
                    null,
                    VALID_FILE_SIZE,
                    VALID_CHECKSUM,
                    FIXED_NOW))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("ContentType");
  }

  @Test
  void should_acceptCreation_when_fileSizeIsExactly2GB() {
    Document doc =
        Document.create(
            VALID_ID,
            VALID_TENANT_ID,
            VALID_REQUEST_ID,
            VALID_FILENAME,
            VALID_CONTENT_TYPE,
            Document.MAX_FILE_SIZE_BYTES,
            VALID_CHECKSUM,
            FIXED_NOW);

    assertThat(doc.getFileSize()).isEqualTo(Document.MAX_FILE_SIZE_BYTES);
  }

  // --- Status Transition: PENDING_UPLOAD → UPLOADED ---

  @Test
  void should_transitionToUploaded_when_confirmUploadCalled() {
    Document doc = createValidDocument();

    doc.confirmUpload(STORAGE_PATH, CORRELATION_ID, FIXED_NOW);

    assertThat(doc.getStatus()).isEqualTo(DocumentStatus.UPLOADED);
    assertThat(doc.getStoragePath()).isEqualTo(STORAGE_PATH);
    assertThat(doc.getUpdatedAt()).isEqualTo(FIXED_NOW);
  }

  @Test
  void should_publishDocumentUploadedEvent_when_confirmUploadCalled() {
    Document doc = createValidDocument();

    doc.confirmUpload(STORAGE_PATH, CORRELATION_ID, FIXED_NOW);

    List<DomainEvent> events = doc.getDomainEvents();
    assertThat(events).hasSize(1);
    assertThat(events.get(0)).isInstanceOf(DocumentUploadedEvent.class);
    DocumentUploadedEvent event = (DocumentUploadedEvent) events.get(0);
    assertThat(event.getDocumentId()).isEqualTo(VALID_ID);
    assertThat(event.getTenantId()).isEqualTo(VALID_TENANT_ID.getValue());
    assertThat(event.getContentType()).isEqualTo(VALID_CONTENT_TYPE.getMimeType());
    assertThat(event.getFilename()).isEqualTo(VALID_FILENAME);
    assertThat(event.getStoragePath()).isEqualTo(STORAGE_PATH);
    assertThat(event.getCorrelationId()).isEqualTo(CORRELATION_ID);
  }

  // --- Status Transition: PENDING_UPLOAD → UPLOAD_FAILED ---

  @Test
  void should_transitionToUploadFailed_when_markUploadFailedCalled() {
    Document doc = createValidDocument();

    doc.markUploadFailed(FIXED_NOW);

    assertThat(doc.getStatus()).isEqualTo(DocumentStatus.UPLOAD_FAILED);
    assertThat(doc.getUpdatedAt()).isEqualTo(FIXED_NOW);
  }

  // --- Status Transition: UPLOADED → TEXT_EXTRACTED ---

  @Test
  void should_transitionToTextExtracted_when_markTextExtractedCalled() {
    Document doc = createUploadedDocument();

    doc.markTextExtracted(CORRELATION_ID, FIXED_NOW);

    assertThat(doc.getStatus()).isEqualTo(DocumentStatus.TEXT_EXTRACTED);
    assertThat(doc.getUpdatedAt()).isEqualTo(FIXED_NOW);
  }

  @Test
  void should_publishDocumentReadyForAnalysisEvent_when_markTextExtractedCalled() {
    Document doc = createUploadedDocument();
    doc.clearDomainEvents();

    doc.markTextExtracted(CORRELATION_ID, FIXED_NOW);

    List<DomainEvent> events = doc.getDomainEvents();
    assertThat(events).hasSize(1);
    assertThat(events.get(0)).isInstanceOf(DocumentReadyForAnalysisEvent.class);
    DocumentReadyForAnalysisEvent event = (DocumentReadyForAnalysisEvent) events.get(0);
    assertThat(event.getDocumentId()).isEqualTo(VALID_ID);
    assertThat(event.getTenantId()).isEqualTo(VALID_TENANT_ID.getValue());
  }

  // --- Status Transition: TEXT_EXTRACTED → ANALYZED ---

  @Test
  void should_transitionToAnalyzed_when_markAnalyzedCalled() {
    Document doc = createTextExtractedDocument();
    String analysisJson = "{\"summary\":\"test\"}";

    doc.markAnalyzed(analysisJson, false, CORRELATION_ID, FIXED_NOW);

    assertThat(doc.getStatus()).isEqualTo(DocumentStatus.ANALYZED);
    assertThat(doc.getAnalysisResult()).isEqualTo(analysisJson);
    assertThat(doc.getUpdatedAt()).isEqualTo(FIXED_NOW);
  }

  @Test
  void should_publishDocumentAnalyzedEvent_when_markAnalyzedCalled() {
    Document doc = createTextExtractedDocument();
    doc.clearDomainEvents();

    doc.markAnalyzed("{}", true, CORRELATION_ID, FIXED_NOW);

    List<DomainEvent> events = doc.getDomainEvents();
    assertThat(events).hasSize(1);
    assertThat(events.get(0)).isInstanceOf(DocumentAnalyzedEvent.class);
    DocumentAnalyzedEvent event = (DocumentAnalyzedEvent) events.get(0);
    assertThat(event.getDocumentId()).isEqualTo(VALID_ID);
    assertThat(event.isFallbackUsed()).isTrue();
  }

  // --- Status Transition: UPLOADED/TEXT_EXTRACTED → PROCESSING_FAILED ---

  @Test
  void should_transitionToProcessingFailed_when_inUploadedStatus() {
    Document doc = createUploadedDocument();
    doc.clearDomainEvents();

    doc.markProcessingFailed(FIXED_NOW);

    assertThat(doc.getStatus()).isEqualTo(DocumentStatus.PROCESSING_FAILED);
  }

  @Test
  void should_transitionToProcessingFailed_when_inTextExtractedStatus() {
    Document doc = createTextExtractedDocument();
    doc.clearDomainEvents();

    doc.markProcessingFailed(FIXED_NOW);

    assertThat(doc.getStatus()).isEqualTo(DocumentStatus.PROCESSING_FAILED);
  }

  // --- Invalid Transition Tests ---

  @Test
  void should_rejectTransition_when_confirmUploadOnUploadedDocument() {
    Document doc = createUploadedDocument();

    assertThatThrownBy(() -> doc.confirmUpload(STORAGE_PATH, CORRELATION_ID, FIXED_NOW))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot transition from UPLOADED to UPLOADED");
  }

  @Test
  void should_rejectTransition_when_markTextExtractedOnPendingDocument() {
    Document doc = createValidDocument();

    assertThatThrownBy(() -> doc.markTextExtracted(CORRELATION_ID, FIXED_NOW))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot transition from PENDING_UPLOAD to TEXT_EXTRACTED");
  }

  @Test
  void should_rejectTransition_when_markAnalyzedOnUploadedDocument() {
    Document doc = createUploadedDocument();

    assertThatThrownBy(() -> doc.markAnalyzed("{}", false, CORRELATION_ID, FIXED_NOW))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot transition from UPLOADED to ANALYZED");
  }

  @Test
  void should_rejectTransition_when_markProcessingFailedOnPendingDocument() {
    Document doc = createValidDocument();

    assertThatThrownBy(() -> doc.markProcessingFailed(FIXED_NOW))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot transition from PENDING_UPLOAD to PROCESSING_FAILED");
  }

  @Test
  void should_rejectTransition_when_markProcessingFailedOnAnalyzedDocument() {
    Document doc = createAnalyzedDocument();

    assertThatThrownBy(() -> doc.markProcessingFailed(FIXED_NOW))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot transition from ANALYZED to PROCESSING_FAILED");
  }

  @Test
  void should_rejectTransition_when_anyTransitionFromUploadFailed() {
    Document doc = createValidDocument();
    doc.markUploadFailed(FIXED_NOW);

    assertThatThrownBy(() -> doc.confirmUpload(STORAGE_PATH, CORRELATION_ID, FIXED_NOW))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot transition from UPLOAD_FAILED");
  }

  @Test
  void should_rejectTransition_when_anyTransitionFromProcessingFailed() {
    Document doc = createUploadedDocument();
    doc.markProcessingFailed(FIXED_NOW);

    assertThatThrownBy(() -> doc.markTextExtracted(CORRELATION_ID, FIXED_NOW))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot transition from PROCESSING_FAILED");
  }

  // --- Helper Methods ---

  private Document createValidDocument() {
    return Document.create(
        VALID_ID,
        VALID_TENANT_ID,
        VALID_REQUEST_ID,
        VALID_FILENAME,
        VALID_CONTENT_TYPE,
        VALID_FILE_SIZE,
        VALID_CHECKSUM,
        FIXED_NOW);
  }

  private Document createUploadedDocument() {
    Document doc = createValidDocument();
    doc.confirmUpload(STORAGE_PATH, CORRELATION_ID, FIXED_NOW);
    return doc;
  }

  private Document createTextExtractedDocument() {
    Document doc = createUploadedDocument();
    doc.markTextExtracted(CORRELATION_ID, FIXED_NOW);
    return doc;
  }

  private Document createAnalyzedDocument() {
    Document doc = createTextExtractedDocument();
    doc.markAnalyzed("{}", false, CORRELATION_ID, FIXED_NOW);
    return doc;
  }
}
