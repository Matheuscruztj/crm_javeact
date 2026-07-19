package com.atlasops.documents.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.documents.domain.AllowedContentType;
import com.atlasops.documents.domain.Document;
import com.atlasops.documents.domain.DocumentStatus;
import com.atlasops.documents.domain.ports.DocumentRepository;
import com.atlasops.documents.domain.ports.ObjectStoragePort;
import com.atlasops.shared.domain.DomainEvent;
import com.atlasops.shared.domain.exceptions.BusinessRuleViolationException;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.EventPublisher;
import com.atlasops.shared.domain.types.TenantId;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConfirmUploadUseCaseTest {

  private static final Instant FIXED_NOW = Instant.parse("2025-01-15T10:30:00Z");
  private static final String DOCUMENT_ID = "doc-001";
  private static final String TENANT_ID = "tenant-alpha";
  private static final String STORAGE_PATH = "tenant-alpha/2025/01/doc-001/report.pdf";
  private static final String CORRELATION_ID = "corr-001";
  private static final String VALID_CHECKSUM = "a".repeat(64);

  @Mock private DocumentRepository documentRepository;

  @Mock private ObjectStoragePort objectStoragePort;

  @Mock private EventPublisher eventPublisher;

  @Mock private Clock clock;

  private ConfirmUploadUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase =
        new ConfirmUploadUseCase(documentRepository, objectStoragePort, eventPublisher, clock);
  }

  @Test
  void should_confirmUpload_when_checksumMatches() {
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
    when(objectStoragePort.getObjectChecksum(STORAGE_PATH)).thenReturn(VALID_CHECKSUM);
    when(clock.now()).thenReturn(FIXED_NOW);
    when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

    var command = new ConfirmUploadCommand(DOCUMENT_ID, STORAGE_PATH, TENANT_ID, CORRELATION_ID);
    Document result = useCase.execute(command);

    assertThat(result.getStatus()).isEqualTo(DocumentStatus.UPLOADED);
    assertThat(result.getStoragePath()).isEqualTo(STORAGE_PATH);
    verify(eventPublisher).publish(any(DomainEvent.class));
    verify(objectStoragePort, never()).deleteObject(any());
  }

  @Test
  void should_markUploadFailed_when_checksumMismatches() {
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

    String differentChecksum = "b".repeat(64);
    when(documentRepository.findById(DOCUMENT_ID, TENANT_ID)).thenReturn(Optional.of(document));
    when(objectStoragePort.getObjectChecksum(STORAGE_PATH)).thenReturn(differentChecksum);
    when(clock.now()).thenReturn(FIXED_NOW);
    when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

    var command = new ConfirmUploadCommand(DOCUMENT_ID, STORAGE_PATH, TENANT_ID, CORRELATION_ID);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(ChecksumMismatchException.class)
        .hasMessageContaining("Checksum mismatch");

    assertThat(document.getStatus()).isEqualTo(DocumentStatus.UPLOAD_FAILED);
    verify(objectStoragePort).deleteObject(STORAGE_PATH);
    verify(documentRepository).save(document);
    verify(eventPublisher, never()).publish(any());
  }

  @Test
  void should_throwException_when_documentNotFound() {
    when(documentRepository.findById(DOCUMENT_ID, TENANT_ID)).thenReturn(Optional.empty());

    var command = new ConfirmUploadCommand(DOCUMENT_ID, STORAGE_PATH, TENANT_ID, CORRELATION_ID);

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
    document.confirmUpload(STORAGE_PATH, CORRELATION_ID, FIXED_NOW);
    document.clearDomainEvents();

    when(documentRepository.findById(DOCUMENT_ID, TENANT_ID)).thenReturn(Optional.of(document));

    var command = new ConfirmUploadCommand(DOCUMENT_ID, STORAGE_PATH, TENANT_ID, CORRELATION_ID);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(BusinessRuleViolationException.class)
        .hasMessageContaining("PENDING_UPLOAD");
  }

  @Test
  void should_throwException_when_documentIdIsEmpty() {
    var command = new ConfirmUploadCommand("", STORAGE_PATH, TENANT_ID, CORRELATION_ID);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("DocumentId");
  }

  @Test
  void should_throwException_when_storagePathIsEmpty() {
    var command = new ConfirmUploadCommand(DOCUMENT_ID, "", TENANT_ID, CORRELATION_ID);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("StoragePath");
  }

  @Test
  void should_confirmUpload_when_checksumMatchesCaseInsensitive() {
    String uppercaseChecksum = "A".repeat(64);
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
    when(objectStoragePort.getObjectChecksum(STORAGE_PATH)).thenReturn(uppercaseChecksum);
    when(clock.now()).thenReturn(FIXED_NOW);
    when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

    var command = new ConfirmUploadCommand(DOCUMENT_ID, STORAGE_PATH, TENANT_ID, CORRELATION_ID);
    Document result = useCase.execute(command);

    assertThat(result.getStatus()).isEqualTo(DocumentStatus.UPLOADED);
    verify(eventPublisher).publish(any(DomainEvent.class));
  }
}
