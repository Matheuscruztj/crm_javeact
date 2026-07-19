package com.atlasops.ai.domain;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Request for document analysis via the AI module.
 *
 * <p>Contains all necessary input for the DocumentAnalysisPort to process a document.
 */
public record DocumentAnalysisRequest(
    String tenantId,
    String documentId,
    String extractedText,
    String promptVersion,
    String outputSchema) {

  private static final int MAX_EXTRACTED_TEXT_LENGTH = 100_000;
  private static final Pattern PROMPT_VERSION_PATTERN =
      Pattern.compile("^[a-zA-Z][a-zA-Z0-9_-]*:v\\d+$");

  public DocumentAnalysisRequest {
    Objects.requireNonNull(tenantId, "tenantId must not be null");
    if (tenantId.isBlank()) {
      throw new IllegalArgumentException("tenantId must not be blank");
    }

    Objects.requireNonNull(documentId, "documentId must not be null");
    if (documentId.isBlank()) {
      throw new IllegalArgumentException("documentId must not be blank");
    }

    Objects.requireNonNull(extractedText, "extractedText must not be null");
    if (extractedText.isBlank()) {
      throw new IllegalArgumentException("extractedText must not be blank");
    }
    if (extractedText.length() > MAX_EXTRACTED_TEXT_LENGTH) {
      throw new IllegalArgumentException(
          "extractedText must not exceed " + MAX_EXTRACTED_TEXT_LENGTH + " characters");
    }

    Objects.requireNonNull(promptVersion, "promptVersion must not be null");
    if (!PROMPT_VERSION_PATTERN.matcher(promptVersion).matches()) {
      throw new IllegalArgumentException(
          "promptVersion must match format 'name:vN' (e.g., 'analysis:v1')");
    }

    Objects.requireNonNull(outputSchema, "outputSchema must not be null");
    if (outputSchema.isBlank()) {
      throw new IllegalArgumentException("outputSchema must not be blank");
    }
  }
}
