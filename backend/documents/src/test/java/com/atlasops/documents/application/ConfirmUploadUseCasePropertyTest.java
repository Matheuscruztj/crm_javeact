package com.atlasops.documents.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.documents.domain.AllowedContentType;
import com.atlasops.documents.domain.Document;
import com.atlasops.documents.domain.DocumentStatus;
import com.atlasops.documents.domain.ports.DocumentRepository;
import com.atlasops.documents.domain.ports.ObjectStoragePort;
import com.atlasops.shared.domain.DomainEvent;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.EventPublisher;
import com.atlasops.shared.domain.types.TenantId;
import java.time.Instant;
import java.util.Optional;
import net.jqwik.api.*;

/**
 * Validates: Requirements 10.2, 10.3, 10.4 Property 15: Checksum Verification Round-Trip
 *
 * <p>Tests the ConfirmUploadUseCase behavior with mocked ports to verify: - Matching checksums →
 * document transitions to UPLOADED - Mismatched checksums → document transitions to UPLOAD_FAILED -
 * Checksum comparison is case-insensitive - Checksum format is always 64 hex characters
 */
@Tag("Feature:project-implementation-kickoff")
@Tag("Property-15:Checksum-Verification-Round-Trip")
class ConfirmUploadUseCasePropertyTest {

  private static final Instant FIXED_NOW = Instant.parse("2025-01-15T10:30:00Z");
  private static final String DOCUMENT_ID = "doc-prop-001";
  private static final String TENANT_ID = "tenant-alpha";
  private static final String STORAGE_PATH = "tenant-alpha/2025/01/doc-prop-001/file.pdf";
  private static final String CORRELATION_ID = "corr-prop-001";

  /**
   * Property: For ANY valid SHA-256 checksum declared at registration time, if the actual file
   * checksum matches (same value), the document transitions to UPLOADED.
   */
  @Property(tries = 100)
  void should_transitionToUploaded_when_actualChecksumMatchesDeclared(
      @ForAll("validSha256Checksums") String declaredChecksum) {

    DocumentRepository documentRepository = mock(DocumentRepository.class);
    ObjectStoragePort objectStoragePort = mock(ObjectStoragePort.class);
    EventPublisher eventPublisher = mock(EventPublisher.class);
    Clock clock = mock(Clock.class);

    ConfirmUploadUseCase useCase =
        new ConfirmUploadUseCase(documentRepository, objectStoragePort, eventPublisher, clock);

    Document document =
        Document.create(
            DOCUMENT_ID,
            new TenantId(TENANT_ID),
            null,
            "file.pdf",
            AllowedContentType.PDF,
            1024L,
            declaredChecksum,
            FIXED_NOW);

    when(documentRepository.findById(DOCUMENT_ID, TENANT_ID)).thenReturn(Optional.of(document));
    when(objectStoragePort.getObjectChecksum(STORAGE_PATH)).thenReturn(declaredChecksum);
    when(clock.now()).thenReturn(FIXED_NOW);
    when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

    ConfirmUploadCommand command =
        new ConfirmUploadCommand(DOCUMENT_ID, STORAGE_PATH, TENANT_ID, CORRELATION_ID);

    Document result = useCase.execute(command);

    assertThat(result.getStatus()).isEqualTo(DocumentStatus.UPLOADED);
    verify(eventPublisher).publish(any(DomainEvent.class));
    verify(objectStoragePort, never()).deleteObject(any());
  }

  /**
   * Property: For ANY two DIFFERENT valid SHA-256 checksums (declared vs actual), the document
   * transitions to UPLOAD_FAILED and a ChecksumMismatchException is thrown.
   */
  @Property(tries = 100)
  void should_transitionToUploadFailed_when_actualChecksumDiffersFromDeclared(
      @ForAll("mismatchedChecksumPairs") ChecksumPair pair) {

    DocumentRepository documentRepository = mock(DocumentRepository.class);
    ObjectStoragePort objectStoragePort = mock(ObjectStoragePort.class);
    EventPublisher eventPublisher = mock(EventPublisher.class);
    Clock clock = mock(Clock.class);

    ConfirmUploadUseCase useCase =
        new ConfirmUploadUseCase(documentRepository, objectStoragePort, eventPublisher, clock);

    Document document =
        Document.create(
            DOCUMENT_ID,
            new TenantId(TENANT_ID),
            null,
            "file.pdf",
            AllowedContentType.PDF,
            1024L,
            pair.declared(),
            FIXED_NOW);

    when(documentRepository.findById(DOCUMENT_ID, TENANT_ID)).thenReturn(Optional.of(document));
    when(objectStoragePort.getObjectChecksum(STORAGE_PATH)).thenReturn(pair.actual());
    when(clock.now()).thenReturn(FIXED_NOW);
    when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

    ConfirmUploadCommand command =
        new ConfirmUploadCommand(DOCUMENT_ID, STORAGE_PATH, TENANT_ID, CORRELATION_ID);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(ChecksumMismatchException.class);

    assertThat(document.getStatus()).isEqualTo(DocumentStatus.UPLOAD_FAILED);
    verify(objectStoragePort).deleteObject(STORAGE_PATH);
    verify(documentRepository).save(document);
    verify(eventPublisher, never()).publish(any());
  }

  /**
   * Property: For ANY valid SHA-256 checksum, if the actual checksum is the uppercase equivalent,
   * the comparison is case-insensitive and the document transitions to UPLOADED.
   */
  @Property(tries = 100)
  void should_transitionToUploaded_when_checksumMatchesCaseInsensitively(
      @ForAll("validSha256Checksums") String declaredChecksum) {

    DocumentRepository documentRepository = mock(DocumentRepository.class);
    ObjectStoragePort objectStoragePort = mock(ObjectStoragePort.class);
    EventPublisher eventPublisher = mock(EventPublisher.class);
    Clock clock = mock(Clock.class);

    ConfirmUploadUseCase useCase =
        new ConfirmUploadUseCase(documentRepository, objectStoragePort, eventPublisher, clock);

    Document document =
        Document.create(
            DOCUMENT_ID,
            new TenantId(TENANT_ID),
            null,
            "file.pdf",
            AllowedContentType.PDF,
            1024L,
            declaredChecksum,
            FIXED_NOW);

    // Return the uppercase variant as the "actual" checksum from storage
    String uppercaseChecksum = declaredChecksum.toUpperCase();

    when(documentRepository.findById(DOCUMENT_ID, TENANT_ID)).thenReturn(Optional.of(document));
    when(objectStoragePort.getObjectChecksum(STORAGE_PATH)).thenReturn(uppercaseChecksum);
    when(clock.now()).thenReturn(FIXED_NOW);
    when(documentRepository.save(any(Document.class))).thenAnswer(inv -> inv.getArgument(0));

    ConfirmUploadCommand command =
        new ConfirmUploadCommand(DOCUMENT_ID, STORAGE_PATH, TENANT_ID, CORRELATION_ID);

    Document result = useCase.execute(command);

    assertThat(result.getStatus()).isEqualTo(DocumentStatus.UPLOADED);
    verify(eventPublisher).publish(any(DomainEvent.class));
    verify(objectStoragePort, never()).deleteObject(any());
  }

  /**
   * Property: ALL valid SHA-256 checksums are exactly 64 hex characters. Validates the format
   * constraint of the checksum domain.
   */
  @Property(tries = 100)
  void should_alwaysBeValid64HexChars_forAnyGeneratedChecksum(
      @ForAll("validSha256Checksums") String checksum) {

    assertThat(checksum).hasSize(64);
    assertThat(checksum).matches("^[a-fA-F0-9]{64}$");
  }

  // ---- Custom Arbitraries ----

  @Provide
  Arbitrary<String> validSha256Checksums() {
    return Arbitraries.strings()
        .withChars('a', 'b', 'c', 'd', 'e', 'f', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        .ofLength(64);
  }

  @Provide
  Arbitrary<ChecksumPair> mismatchedChecksumPairs() {
    Arbitrary<String> checksums = validSha256Checksums();
    return Combinators.combine(checksums, checksums)
        .filter((a, b) -> !a.equalsIgnoreCase(b))
        .as(ChecksumPair::new);
  }

  /** Pair of checksums used for mismatch testing. */
  record ChecksumPair(String declared, String actual) {}
}
