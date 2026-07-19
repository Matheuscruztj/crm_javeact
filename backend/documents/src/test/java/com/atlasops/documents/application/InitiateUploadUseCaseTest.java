package com.atlasops.documents.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.documents.domain.AllowedContentType;
import com.atlasops.documents.domain.Document;
import com.atlasops.documents.domain.ports.DocumentRepository;
import com.atlasops.documents.domain.ports.ObjectStoragePort;
import com.atlasops.shared.domain.exceptions.BusinessRuleViolationException;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.types.TenantId;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InitiateUploadUseCaseTest {

  private static final Instant FIXED_NOW = Instant.parse("2025-03-15T10:30:00Z");
  private static final String DOCUMENT_ID = "doc-001";
  private static final String TENANT_ID = "tenant-alpha";
  private static final String VALID_CHECKSUM = "a".repeat(64);
  private static final String PRESIGNED_URL = "https://minio.local/bucket/presigned-url";

  @Mock private DocumentRepository documentRepository;

  @Mock private ObjectStoragePort objectStoragePort;

  @Mock private Clock clock;

  private InitiateUploadUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new InitiateUploadUseCase(documentRepository, objectStoragePort, clock);
  }

  @Test
  void should_generatePresignedUrl_when_documentInPendingUploadStatus() {
    Document document =
        Document.create(
            DOCUMENT_ID,
            new TenantId(TENANT_ID),
            null,
            "report.pdf",
            AllowedContentType.PDF,
            1024L,
            VALID_CHECKSUM,
            FIXED_NOW);

    when(documentRepository.findById(DOCUMENT_ID, TENANT_ID)).thenReturn(Optional.of(document));
    when(clock.now()).thenReturn(FIXED_NOW);
    when(objectStoragePort.generatePresignedUploadUrl(anyString(), anyString(), anyInt()))
        .thenReturn(PRESIGNED_URL);

    var command = new InitiateUploadCommand(DOCUMENT_ID, TENANT_ID);
    InitiateUploadResult result = useCase.execute(command);

    assertThat(result.uploadUrl()).isEqualTo(PRESIGNED_URL);
    assertThat(result.storagePath()).isEqualTo("tenant-alpha/2025/03/doc-001/report.pdf");

    verify(objectStoragePort)
        .generatePresignedUploadUrl(
            eq("tenant-alpha/2025/03/doc-001/report.pdf"), eq("application/pdf"), eq(60));
  }

  @Test
  void should_throwException_when_documentNotFound() {
    when(documentRepository.findById(DOCUMENT_ID, TENANT_ID)).thenReturn(Optional.empty());

    var command = new InitiateUploadCommand(DOCUMENT_ID, TENANT_ID);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(DOCUMENT_ID);
  }

  @Test
  void should_throwException_when_documentNotInPendingUploadStatus() {
    Document document =
        Document.create(
            DOCUMENT_ID,
            new TenantId(TENANT_ID),
            null,
            "report.pdf",
            AllowedContentType.PDF,
            1024L,
            VALID_CHECKSUM,
            FIXED_NOW);
    // Transition to UPLOADED to simulate wrong status
    document.confirmUpload("some/path", "corr-id", FIXED_NOW);

    when(documentRepository.findById(DOCUMENT_ID, TENANT_ID)).thenReturn(Optional.of(document));

    var command = new InitiateUploadCommand(DOCUMENT_ID, TENANT_ID);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(BusinessRuleViolationException.class)
        .hasMessageContaining("PENDING_UPLOAD");
  }

  @Test
  void should_throwException_when_documentIdIsEmpty() {
    var command = new InitiateUploadCommand("", TENANT_ID);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("DocumentId");
  }

  @Test
  void should_throwException_when_tenantIdIsEmpty() {
    var command = new InitiateUploadCommand(DOCUMENT_ID, "");

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("TenantId");
  }

  @Test
  void should_buildCorrectStoragePath_when_dateIsJanuary() {
    Document document =
        Document.create(
            "doc-xyz",
            new TenantId("tenant-beta"),
            null,
            "invoice.docx",
            AllowedContentType.DOCX,
            2048L,
            VALID_CHECKSUM,
            Instant.parse("2025-01-05T08:00:00Z"));

    when(documentRepository.findById("doc-xyz", "tenant-beta")).thenReturn(Optional.of(document));
    when(clock.now()).thenReturn(Instant.parse("2025-01-10T12:00:00Z"));
    when(objectStoragePort.generatePresignedUploadUrl(anyString(), anyString(), anyInt()))
        .thenReturn(PRESIGNED_URL);

    var command = new InitiateUploadCommand("doc-xyz", "tenant-beta");
    InitiateUploadResult result = useCase.execute(command);

    assertThat(result.storagePath()).isEqualTo("tenant-beta/2025/01/doc-xyz/invoice.docx");
  }
}
