package com.atlasops.ai.domain;

import static org.assertj.core.api.Assertions.assertThat;

import net.jqwik.api.*;

/**
 * Property 2: DocumentAnalysisRequest accepts all valid inputs.
 *
 * <p><b>Validates: Requirements 6.1</b>
 *
 * <p>For any input where tenantId is non-blank, documentId is non-blank, extractedText is non-blank
 * and ≤ 100,000 characters, promptVersion matches {@code ^[a-zA-Z][a-zA-Z0-9_-]*:v\d+$}, and
 * outputSchema is non-blank, constructing a DocumentAnalysisRequest SHALL succeed without exception
 * and all fields SHALL be accessible with the same values provided.
 */
@Tag(
    "Feature: project-adequation-restructure, Property 2: DocumentAnalysisRequest accepts all valid"
        + " inputs")
class DocumentAnalysisRequestValidPropertyTest {

  @Property(tries = 100)
  void should_createRequest_when_allInputsAreValid(
      @ForAll("validNonBlankStrings") String tenantId,
      @ForAll("validNonBlankStrings") String documentId,
      @ForAll("validExtractedTexts") String extractedText,
      @ForAll("validPromptVersions") String promptVersion,
      @ForAll("validNonBlankStrings") String outputSchema) {

    var request =
        new DocumentAnalysisRequest(
            tenantId, documentId, extractedText, promptVersion, outputSchema);

    assertThat(request.tenantId()).isEqualTo(tenantId);
    assertThat(request.documentId()).isEqualTo(documentId);
    assertThat(request.extractedText()).isEqualTo(extractedText);
    assertThat(request.promptVersion()).isEqualTo(promptVersion);
    assertThat(request.outputSchema()).isEqualTo(outputSchema);
  }

  @Provide
  Arbitrary<String> validNonBlankStrings() {
    return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50);
  }

  @Provide
  Arbitrary<String> validExtractedTexts() {
    return Arbitraries.strings().ofMinLength(1).ofMaxLength(100_000).filter(s -> !s.isBlank());
  }

  @Provide
  Arbitrary<String> validPromptVersions() {
    return Combinators.combine(
            Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20),
            Arbitraries.integers().between(1, 100))
        .as((name, version) -> name + ":v" + version);
  }
}
