package com.atlasops.documents.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.documents.domain.AllowedContentType;
import com.atlasops.documents.domain.Document;
import com.atlasops.documents.domain.DocumentStatus;
import com.atlasops.documents.domain.ports.DocumentRepository;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterDocumentMetadataUseCaseTest {

  private static final Instant FIXED_NOW = Instant.parse("2025-01-15T10:30:00Z");
  private static final String GENERATED_ID = "doc-001";
  private static final String TENANT_ID = "tenant-alpha";
  private static final String VALID_CHECKSUM = "a".repeat(64);

  @Mock private DocumentRepository documentRepository;

  @Mock private IdGenerator idGenerator;

  @Mock private Clock clock;

  private RegisterDocumentMetadataUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new RegisterDocumentMetadataUseCase(documentRepository, idGenerator, clock);
  }

  @Test
  void should_createDocument_when_allFieldsValid() {
    when(idGenerator.generate()).thenReturn(GENERATED_ID);
    when(clock.now()).thenReturn(FIXED_NOW);
    when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

    var command =
        new RegisterDocumentMetadataCommand(
            "report.pdf", "application/pdf", 1024L, VALID_CHECKSUM, "request-001", TENANT_ID);

    Document result = useCase.execute(command);

    assertThat(result.getId()).isEqualTo(GENERATED_ID);
    assertThat(result.getTenantId().getValue()).isEqualTo(TENANT_ID);
    assertThat(result.getFilename()).isEqualTo("report.pdf");
    assertThat(result.getContentType()).isEqualTo(AllowedContentType.PDF);
    assertThat(result.getFileSize()).isEqualTo(1024L);
    assertThat(result.getChecksum()).isEqualTo(VALID_CHECKSUM);
    assertThat(result.getStatus()).isEqualTo(DocumentStatus.PENDING_UPLOAD);
    assertThat(result.getRequestId()).isEqualTo("request-001");
    verify(documentRepository).save(any(Document.class));
  }

  @Test
  void should_createDocument_when_requestIdIsNull() {
    when(idGenerator.generate()).thenReturn(GENERATED_ID);
    when(clock.now()).thenReturn(FIXED_NOW);
    when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

    var command =
        new RegisterDocumentMetadataCommand(
            "photo.png", "image/png", 2048L, VALID_CHECKSUM, null, TENANT_ID);

    Document result = useCase.execute(command);

    assertThat(result.getRequestId()).isNull();
    assertThat(result.getContentType()).isEqualTo(AllowedContentType.PNG);
  }

  @Test
  void should_rejectDocument_when_contentTypeNotSupported() {
    var command =
        new RegisterDocumentMetadataCommand(
            "file.zip", "application/zip", 1024L, VALID_CHECKSUM, null, TENANT_ID);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unsupported content type")
        .hasMessageContaining("application/zip");
  }

  @Test
  void should_rejectDocument_when_fileSizeExceeds2GB() {
    when(idGenerator.generate()).thenReturn(GENERATED_ID);
    when(clock.now()).thenReturn(FIXED_NOW);

    long oversizedFile = Document.MAX_FILE_SIZE_BYTES + 1;
    var command =
        new RegisterDocumentMetadataCommand(
            "large.pdf", "application/pdf", oversizedFile, VALID_CHECKSUM, null, TENANT_ID);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("2 GB");
  }

  @Test
  void should_rejectDocument_when_checksumIsInvalid() {
    when(idGenerator.generate()).thenReturn(GENERATED_ID);
    when(clock.now()).thenReturn(FIXED_NOW);

    var command =
        new RegisterDocumentMetadataCommand(
            "report.pdf", "application/pdf", 1024L, "invalid-checksum", null, TENANT_ID);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("SHA-256");
  }

  @Test
  void should_rejectDocument_when_filenameIsEmpty() {
    var command =
        new RegisterDocumentMetadataCommand(
            "", "application/pdf", 1024L, VALID_CHECKSUM, null, TENANT_ID);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Filename");
  }

  @Test
  void should_rejectDocument_when_tenantIdIsNull() {
    var command =
        new RegisterDocumentMetadataCommand(
            "report.pdf", "application/pdf", 1024L, VALID_CHECKSUM, null, null);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("TenantId");
  }

  @Test
  void should_rejectDocument_when_checksumIsEmpty() {
    var command =
        new RegisterDocumentMetadataCommand(
            "report.pdf", "application/pdf", 1024L, "", null, TENANT_ID);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Checksum");
  }
}
