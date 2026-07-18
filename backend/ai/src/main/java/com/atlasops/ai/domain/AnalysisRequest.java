package com.atlasops.ai.domain;

import java.util.Objects;

/**
 * Domain record representing a request for AI analysis. Contains the input text to analyze, the
 * model to use, and the prompt version.
 */
public record AnalysisRequest(
    String tenantId, String inputText, String model, String promptVersion) {

  public AnalysisRequest {
    Objects.requireNonNull(tenantId, "tenantId must not be null");
    Objects.requireNonNull(inputText, "inputText must not be null");
    Objects.requireNonNull(model, "model must not be null");
    Objects.requireNonNull(promptVersion, "promptVersion must not be null");

    if (tenantId.isBlank()) {
      throw new IllegalArgumentException("tenantId must not be blank");
    }
    if (inputText.isBlank()) {
      throw new IllegalArgumentException("inputText must not be blank");
    }
    if (model.isBlank()) {
      throw new IllegalArgumentException("model must not be blank");
    }
    if (promptVersion.isBlank()) {
      throw new IllegalArgumentException("promptVersion must not be blank");
    }
  }
}
