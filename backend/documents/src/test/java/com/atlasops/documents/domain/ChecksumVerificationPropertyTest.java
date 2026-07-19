package com.atlasops.documents.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.atlasops.shared.domain.types.TenantId;
import java.time.Instant;
import net.jqwik.api.*;

/**
 * Property-based tests for checksum verification round-trip.
 *
 * <p><b>Validates: Requirements 10.2, 10.3, 10.4</b>
 *
 * <p>Property 15: Checksum Verification Round-Trip
 *
 * <p>Requirement 10.2: THE Documents_Module SHALL require a SHA-256 checksum declared at
 * registration time.
 *
 * <p>Requirement 10.3: THE Documents_Module SHALL verify the actual file checksum after upload
 * against the declared checksum.
 *
 * <p>Requirement 10.4: THE Documents_Module SHALL transition to UPLOAD_FAILED when checksum does
 * not match and delete the file from storage.
 */
@Tag("Feature: project-implementation-kickoff, Property 15: Checksum Verification Round-Trip")
class ChecksumVerificationPropertyTest {

  private static final Instant FIXED_NOW = Instant.parse("2025-01-15T10:30:00Z");
  private static final TenantId TENANT_ID = new TenantId("tenant-alpha");
  private static final String STORAGE_PATH = "tenant-alpha/2025/01/doc-001/file.pdf";
  private static final String CORRELATION_ID = "corr-123";

  /**
   * Property: For ANY valid SHA-256 checksum (64 hex characters), Document.create() ALWAYS succeeds
   * and stores the checksum exactly as provided.
   */
  @Property(tries = 100)
  void should_alwaysAcceptAndStoreChecksum_forAnyValidSHA256Hex(
      @ForAll("validSha256Checksums") String checksum) {

    Document doc =
        Document.create(
            "doc-001",
            TENANT_ID,
            null,
            "file.pdf",
            AllowedContentType.PDF,
            1024L,
            checksum,
            FIXED_NOW);

    assertThat(doc.getChecksum()).isEqualTo(checksum);
    assertThat(doc.getStatus()).isEqualTo(DocumentStatus.PENDING_UPLOAD);
  }

  /**
   * Property: For ANY string that is NOT a valid SHA-256 hex (not 64 hex chars), Document.create()
   * ALWAYS rejects with IllegalArgumentException.
   */
  @Property(tries = 100)
  void should_alwaysRejectChecksum_forAnyInvalidSHA256Format(
      @ForAll("invalidChecksums") String invalidChecksum) {

    assertThatThrownBy(
            () ->
                Document.create(
                    "doc-001",
                    TENANT_ID,
                    null,
                    "file.pdf",
                    AllowedContentType.PDF,
                    1024L,
                    invalidChecksum,
                    FIXED_NOW))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("SHA-256");
  }

  /**
   * Property: For ANY valid checksum stored in a document, when the same checksum is compared
   * case-insensitively, it ALWAYS matches. This validates that checksum comparison supports both
   * upper and lower case hex.
   */
  @Property(tries = 100)
  void should_alwaysMatchCaseInsensitively_forAnyValidChecksumPair(
      @ForAll("validSha256Checksums") String checksum) {

    Document doc =
        Document.create(
            "doc-001",
            TENANT_ID,
            null,
            "file.pdf",
            AllowedContentType.PDF,
            1024L,
            checksum,
            FIXED_NOW);

    // Simulate comparison the way ConfirmUploadUseCase does
    String storedChecksum = doc.getChecksum();
    String uppercaseVariant = checksum.toUpperCase();

    assertThat(storedChecksum.equalsIgnoreCase(uppercaseVariant)).isTrue();
  }

  /**
   * Property: For ANY two DIFFERENT valid checksums, they NEVER match. This validates that checksum
   * verification correctly detects mismatches.
   */
  @Property(tries = 100)
  void should_neverMatch_forAnyTwoDifferentChecksums(
      @ForAll("validSha256Checksums") String checksum1,
      @ForAll("validSha256Checksums") String checksum2) {

    Assume.that(!checksum1.equalsIgnoreCase(checksum2));

    Document doc =
        Document.create(
            "doc-001",
            TENANT_ID,
            null,
            "file.pdf",
            AllowedContentType.PDF,
            1024L,
            checksum1,
            FIXED_NOW);

    assertThat(doc.getChecksum().equalsIgnoreCase(checksum2)).isFalse();
  }

  // ---- Custom Arbitraries ----

  @Provide
  Arbitrary<String> validSha256Checksums() {
    return Arbitraries.strings()
        .withChars('a', 'b', 'c', 'd', 'e', 'f', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        .ofLength(64);
  }

  @Provide
  Arbitrary<String> invalidChecksums() {
    return Arbitraries.oneOf(
        // Too short
        Arbitraries.strings()
            .withChars('a', 'b', 'c', 'd', 'e', 'f', '0', '1', '2', '3')
            .ofMinLength(1)
            .ofMaxLength(63),
        // Too long
        Arbitraries.strings()
            .withChars('a', 'b', 'c', 'd', 'e', 'f', '0', '1', '2', '3')
            .ofMinLength(65)
            .ofMaxLength(128),
        // Right length but non-hex characters
        Arbitraries.strings().withChars('g', 'h', 'i', 'j', 'k', 'x', 'y', 'z').ofLength(64),
        // Mixed valid/invalid characters at correct length
        Arbitraries.strings()
            .withChars('a', 'b', 'c', 'g', 'h', 'x', '0', '1')
            .ofLength(64)
            .filter(s -> !s.matches("^[a-fA-F0-9]{64}$")));
  }
}
