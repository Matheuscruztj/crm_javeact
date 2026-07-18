package com.atlasops.ai.domain;

import java.util.List;
import java.util.Objects;

/**
 * Domain record representing the result of an AI analysis. Contains the response text, confidence
 * score, chunks used, and fallback indicator.
 */
public record AnalysisResult(
    String result,
    double confidenceScore,
    List<String> chunksUsed,
    boolean fallback,
    long durationMs) {

  public AnalysisResult {
    Objects.requireNonNull(result, "result must not be null");
    Objects.requireNonNull(chunksUsed, "chunksUsed must not be null");

    if (confidenceScore < 0.0 || confidenceScore > 1.0) {
      throw new IllegalArgumentException(
          "confidenceScore must be between 0.0 and 1.0, got: " + confidenceScore);
    }
    if (durationMs < 0) {
      throw new IllegalArgumentException("durationMs must not be negative, got: " + durationMs);
    }

    chunksUsed = List.copyOf(chunksUsed);
  }
}
