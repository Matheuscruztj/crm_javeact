package com.atlasops.ai.domain;

import java.util.List;
import java.util.Objects;

/**
 * Result of document analysis performed by the AI module.
 *
 * <p>Contains the analysis output including summary, classification, extracted data, identified
 * risks, and confidence scoring.
 */
public record DocumentAnalysisResult(
    String summary,
    String category,
    List<KeyValuePair> extractedFields,
    List<String> risks,
    List<String> missingInformation,
    double confidenceScore,
    String providerMetadata,
    boolean fallback) {

  public DocumentAnalysisResult {
    Objects.requireNonNull(summary, "summary must not be null");
    if (summary.isBlank()) {
      throw new IllegalArgumentException("summary must not be blank");
    }

    Objects.requireNonNull(category, "category must not be null");
    if (category.isBlank()) {
      throw new IllegalArgumentException("category must not be blank");
    }

    Objects.requireNonNull(extractedFields, "extractedFields must not be null");
    extractedFields = List.copyOf(extractedFields);

    Objects.requireNonNull(risks, "risks must not be null");
    risks = List.copyOf(risks);

    Objects.requireNonNull(missingInformation, "missingInformation must not be null");
    missingInformation = List.copyOf(missingInformation);

    if (confidenceScore < 0.0 || confidenceScore > 1.0) {
      throw new IllegalArgumentException(
          "confidenceScore must be between 0.0 and 1.0, got: " + confidenceScore);
    }

    Objects.requireNonNull(providerMetadata, "providerMetadata must not be null");
    if (providerMetadata.isBlank()) {
      throw new IllegalArgumentException("providerMetadata must not be blank");
    }
  }
}
