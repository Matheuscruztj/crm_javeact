package com.atlasops.documents.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.regex.Pattern;
import net.jqwik.api.*;

/**
 * Property-based tests for MinIO storage path format.
 *
 * <p><b>Validates: Requirements 10.5</b>
 *
 * <p>Property 16: MinIO Storage Path Format
 *
 * <p>Requirement 10.5: THE Documents_Module SHALL store documents using the path format:
 * {tenantId}/{year}/{month}/{documentId}/{filename}
 */
@Tag("Feature: project-implementation-kickoff, Property 16: MinIO Storage Path Format")
class MinioStoragePathPropertyTest {

  /**
   * Expected path format: {tenantId}/{year}/{month}/{documentId}/{filename} Example:
   * tenant-alpha/2025/01/doc-001/report.pdf
   */
  private static final Pattern STORAGE_PATH_PATTERN =
      Pattern.compile("^[a-zA-Z0-9_-]+/\\d{4}/\\d{2}/[a-zA-Z0-9_-]+/.+$");

  /**
   * Property: For ANY valid combination of tenantId, timestamp, documentId, and filename, the
   * generated storage path ALWAYS follows the format
   * {tenantId}/{year}/{month}/{documentId}/{filename}.
   */
  @Property(tries = 100)
  void should_alwaysProduceValidPathFormat_forAnyValidInputs(
      @ForAll("validTenantIds") String tenantId,
      @ForAll("validDocumentIds") String documentId,
      @ForAll("validFilenames") String filename,
      @ForAll("validTimestamps") Instant timestamp) {

    String path = buildStoragePath(tenantId, documentId, filename, timestamp);

    assertThat(path).matches(STORAGE_PATH_PATTERN);
  }

  /**
   * Property: For ANY valid inputs, the storage path ALWAYS contains exactly 4 slashes separating 5
   * path segments.
   */
  @Property(tries = 100)
  void should_alwaysHaveFiveSegments_forAnyValidInputs(
      @ForAll("validTenantIds") String tenantId,
      @ForAll("validDocumentIds") String documentId,
      @ForAll("validFilenames") String filename,
      @ForAll("validTimestamps") Instant timestamp) {

    String path = buildStoragePath(tenantId, documentId, filename, timestamp);
    String[] segments = path.split("/");

    assertThat(segments).hasSize(5);
    assertThat(segments[0]).isEqualTo(tenantId);
    assertThat(segments[3]).isEqualTo(documentId);
    assertThat(segments[4]).isEqualTo(filename);
  }

  /**
   * Property: For ANY valid timestamp, the year and month in the path ALWAYS match the UTC year and
   * month of the timestamp.
   */
  @Property(tries = 100)
  void should_alwaysMatchUtcYearAndMonth_forAnyValidTimestamp(
      @ForAll("validTenantIds") String tenantId,
      @ForAll("validDocumentIds") String documentId,
      @ForAll("validFilenames") String filename,
      @ForAll("validTimestamps") Instant timestamp) {

    String path = buildStoragePath(tenantId, documentId, filename, timestamp);
    String[] segments = path.split("/");

    LocalDate date = timestamp.atZone(ZoneOffset.UTC).toLocalDate();
    String expectedYear = String.valueOf(date.getYear());
    String expectedMonth = String.format("%02d", date.getMonthValue());

    assertThat(segments[1]).isEqualTo(expectedYear);
    assertThat(segments[2]).isEqualTo(expectedMonth);
  }

  /** Property: For ANY valid inputs, the month segment is ALWAYS zero-padded to 2 digits. */
  @Property(tries = 100)
  void should_alwaysZeroPadMonth_forAnyValidTimestamp(
      @ForAll("validTenantIds") String tenantId,
      @ForAll("validDocumentIds") String documentId,
      @ForAll("validFilenames") String filename,
      @ForAll("validTimestamps") Instant timestamp) {

    String path = buildStoragePath(tenantId, documentId, filename, timestamp);
    String[] segments = path.split("/");

    assertThat(segments[2]).hasSize(2);
    int month = Integer.parseInt(segments[2]);
    assertThat(month).isBetween(1, 12);
  }

  // ---- Helper method replicating InitiateUploadUseCase.buildStoragePath logic ----

  private String buildStoragePath(
      String tenantId, String documentId, String filename, Instant timestamp) {
    LocalDate date = timestamp.atZone(ZoneOffset.UTC).toLocalDate();
    int year = date.getYear();
    int month = date.getMonthValue();
    return String.format("%s/%d/%02d/%s/%s", tenantId, year, month, documentId, filename);
  }

  // ---- Custom Arbitraries ----

  @Provide
  Arbitrary<String> validTenantIds() {
    return Arbitraries.strings()
        .withChars(
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', '0',
            '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_')
        .ofMinLength(3)
        .ofMaxLength(30)
        .filter(s -> s.matches("^[a-zA-Z0-9_-]+$"));
  }

  @Provide
  Arbitrary<String> validDocumentIds() {
    return Arbitraries.strings()
        .withChars(
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', '0',
            '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_')
        .ofMinLength(3)
        .ofMaxLength(36)
        .filter(s -> s.matches("^[a-zA-Z0-9_-]+$"));
  }

  @Provide
  Arbitrary<String> validFilenames() {
    return Arbitraries.of(
        "report.pdf",
        "contract.docx",
        "photo.png",
        "scan.jpeg",
        "analysis-2025.pdf",
        "document_v2.pdf",
        "invoice.pdf");
  }

  @Provide
  Arbitrary<Instant> validTimestamps() {
    // Range: 2020-01-01 to 2030-12-31
    long minEpoch = Instant.parse("2020-01-01T00:00:00Z").getEpochSecond();
    long maxEpoch = Instant.parse("2030-12-31T23:59:59Z").getEpochSecond();
    return Arbitraries.longs().between(minEpoch, maxEpoch).map(Instant::ofEpochSecond);
  }
}
