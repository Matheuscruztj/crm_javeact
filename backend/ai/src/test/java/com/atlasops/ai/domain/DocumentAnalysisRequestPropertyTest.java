package com.atlasops.ai.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import net.jqwik.api.*;

/**
 * Property 1: DocumentAnalysisRequest rejects all invalid inputs.
 *
 * <p><b>Validates: Requirements 6.1</b>
 */
@Tag(
    "Feature: project-adequation-restructure, Property 1: DocumentAnalysisRequest rejects all"
        + " invalid inputs")
class DocumentAnalysisRequestPropertyTest {

  private static final String VALID_TENANT_ID = "tenant-001";
  private static final String VALID_DOCUMENT_ID = "doc-001";
  private static final String VALID_EXTRACTED_TEXT = "Some document content";
  private static final String VALID_PROMPT_VERSION = "analysis:v1";
  private static final String VALID_OUTPUT_SCHEMA = "standard-v1";

  @Property(tries = 100)
  void should_rejectRequest_when_tenantIdIsBlank(@ForAll("blankStrings") String blankTenantId) {
    assertThatThrownBy(
            () ->
                new DocumentAnalysisRequest(
                    blankTenantId,
                    VALID_DOCUMENT_ID,
                    VALID_EXTRACTED_TEXT,
                    VALID_PROMPT_VERSION,
                    VALID_OUTPUT_SCHEMA))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Property(tries = 100)
  void should_rejectRequest_when_documentIdIsBlank(@ForAll("blankStrings") String blankDocId) {
    assertThatThrownBy(
            () ->
                new DocumentAnalysisRequest(
                    VALID_TENANT_ID,
                    blankDocId,
                    VALID_EXTRACTED_TEXT,
                    VALID_PROMPT_VERSION,
                    VALID_OUTPUT_SCHEMA))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Property(tries = 100)
  void should_rejectRequest_when_extractedTextIsBlank(@ForAll("blankStrings") String blankText) {
    assertThatThrownBy(
            () ->
                new DocumentAnalysisRequest(
                    VALID_TENANT_ID,
                    VALID_DOCUMENT_ID,
                    blankText,
                    VALID_PROMPT_VERSION,
                    VALID_OUTPUT_SCHEMA))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Property(tries = 100)
  void should_rejectRequest_when_extractedTextExceedsMaxLength(
      @ForAll("oversizedTexts") String oversizedText) {
    assertThatThrownBy(
            () ->
                new DocumentAnalysisRequest(
                    VALID_TENANT_ID,
                    VALID_DOCUMENT_ID,
                    oversizedText,
                    VALID_PROMPT_VERSION,
                    VALID_OUTPUT_SCHEMA))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Property(tries = 100)
  void should_rejectRequest_when_promptVersionIsInvalid(
      @ForAll("invalidPromptVersions") String invalidVersion) {
    assertThatThrownBy(
            () ->
                new DocumentAnalysisRequest(
                    VALID_TENANT_ID,
                    VALID_DOCUMENT_ID,
                    VALID_EXTRACTED_TEXT,
                    invalidVersion,
                    VALID_OUTPUT_SCHEMA))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Property(tries = 100)
  void should_rejectRequest_when_outputSchemaIsBlank(@ForAll("blankStrings") String blankSchema) {
    assertThatThrownBy(
            () ->
                new DocumentAnalysisRequest(
                    VALID_TENANT_ID,
                    VALID_DOCUMENT_ID,
                    VALID_EXTRACTED_TEXT,
                    VALID_PROMPT_VERSION,
                    blankSchema))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Provide
  Arbitrary<String> blankStrings() {
    return Arbitraries.of("", " ", "  ", "\t", "\n", "   \t\n  ");
  }

  @Provide
  Arbitrary<String> oversizedTexts() {
    return Arbitraries.integers().between(100_001, 200_000).map(length -> "a".repeat(length));
  }

  @Provide
  Arbitrary<String> invalidPromptVersions() {
    return Arbitraries.of(
        "nocolon",
        "no-version",
        ":v1",
        "1startsWithDigit:v1",
        "name:v",
        "name:1",
        "name:va",
        "",
        " ",
        "name:",
        ":v",
        "name:v0a");
  }
}
