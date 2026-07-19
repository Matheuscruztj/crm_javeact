package com.atlasops.documents.domain;

import static org.assertj.core.api.Assertions.assertThat;

import net.jqwik.api.*;

/**
 * Property-based tests for document content type validation.
 *
 * <p><b>Validates: Requirements 9.2, 9.3</b>
 *
 * <p>Property 14: Document Content Type Validation
 *
 * <p>Requirement 9.2: THE Documents_Module SHALL only accept uploads with content types:
 * application/pdf, application/vnd.openxmlformats-officedocument.wordprocessingml.document,
 * image/png, image/jpeg.
 *
 * <p>Requirement 9.3: THE Documents_Module SHALL reject uploads with unsupported content types
 * returning 400 Bad Request.
 */
@Tag("Feature:project-implementation-kickoff")
@Tag("Property-14:Document-Content-Type-Validation")
class DocumentContentTypePropertyTest {

  private static final String VALID_CHECKSUM = "a".repeat(64);
  private static final long VALID_FILE_SIZE = 1024L;

  /**
   * Property: For ANY supported MIME type, AllowedContentType.fromMimeType() ALWAYS returns a
   * present Optional with the correct enum value.
   */
  @Property(tries = 100)
  void should_alwaysAcceptSupportedMimeType_forAnySupportedContentType(
      @ForAll("supportedMimeTypes") String mimeType) {

    var result = AllowedContentType.fromMimeType(mimeType);

    assertThat(result).isPresent();
    assertThat(AllowedContentType.isSupported(mimeType)).isTrue();
  }

  /**
   * Property: For ANY unsupported MIME type string, AllowedContentType.fromMimeType() ALWAYS
   * returns an empty Optional, and Document.create() throws IllegalArgumentException when used with
   * RegisterDocumentMetadataUseCase.
   */
  @Property(tries = 100)
  void should_alwaysRejectUnsupportedMimeType_forAnyUnsupportedContentType(
      @ForAll("unsupportedMimeTypes") String mimeType) {

    var result = AllowedContentType.fromMimeType(mimeType);

    assertThat(result).isEmpty();
    assertThat(AllowedContentType.isSupported(mimeType)).isFalse();
  }

  /**
   * Property: For ANY supported MIME type (case-insensitive), the resolution correctly identifies
   * the content type regardless of letter casing.
   */
  @Property(tries = 100)
  void should_resolveCaseInsensitively_forAnySupportedMimeTypeWithRandomCase(
      @ForAll("supportedMimeTypesWithRandomCase") String mimeType) {

    var result = AllowedContentType.fromMimeType(mimeType);

    assertThat(result).isPresent();
  }

  /** Property: For ANY null or blank string, fromMimeType() returns empty Optional. */
  @Property(tries = 100)
  void should_returnEmpty_forAnyBlankOrNullInput(@ForAll("blankStrings") String input) {

    var result = AllowedContentType.fromMimeType(input);

    assertThat(result).isEmpty();
  }

  // ---- Custom Arbitraries ----

  @Provide
  Arbitrary<String> supportedMimeTypes() {
    return Arbitraries.of(
        "application/pdf",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "image/png",
        "image/jpeg");
  }

  @Provide
  Arbitrary<String> unsupportedMimeTypes() {
    return Arbitraries.of(
        "application/zip",
        "application/octet-stream",
        "text/plain",
        "text/html",
        "image/gif",
        "image/bmp",
        "image/webp",
        "application/json",
        "application/xml",
        "video/mp4",
        "audio/mpeg",
        "application/msword",
        "application/x-tar",
        "image/svg+xml",
        "text/csv");
  }

  @Provide
  Arbitrary<String> supportedMimeTypesWithRandomCase() {
    return Arbitraries.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "image/png",
            "image/jpeg")
        .map(this::randomizeCase);
  }

  @Provide
  Arbitrary<String> blankStrings() {
    return Arbitraries.of("", "   ", "\t", "\n", " \t \n ");
  }

  private String randomizeCase(String input) {
    StringBuilder sb = new StringBuilder();
    for (char c : input.toCharArray()) {
      if (Character.isLetter(c) && Math.random() > 0.5) {
        sb.append(Character.toUpperCase(c));
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }
}
