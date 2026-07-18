package com.atlasops.ai.domain;

import com.atlasops.shared.domain.Entity;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Domain entity representing a persisted AI analysis record. Captures all metadata about a
 * completed analysis operation: model, prompt version, input hash, duration, confidence, fallback
 * status, result, and chunks used.
 *
 * <p>Validates: Requirements 4.5, 4.8
 */
public class AIAnalysisRecord extends Entity<String> {

  private final String tenantId;
  private final String model;
  private final String promptVersion;
  private final String inputHash;
  private final long durationMs;
  private final double confidenceScore;
  private final boolean fallback;
  private final String result;
  private final List<String> chunksUsed;
  private final Instant createdAt;

  public AIAnalysisRecord(
      String id,
      String tenantId,
      String model,
      String promptVersion,
      String inputHash,
      long durationMs,
      double confidenceScore,
      boolean fallback,
      String result,
      List<String> chunksUsed,
      Instant createdAt) {
    super(id);

    Objects.requireNonNull(tenantId, "tenantId must not be null");
    Objects.requireNonNull(model, "model must not be null");
    Objects.requireNonNull(promptVersion, "promptVersion must not be null");
    Objects.requireNonNull(inputHash, "inputHash must not be null");
    Objects.requireNonNull(result, "result must not be null");
    Objects.requireNonNull(chunksUsed, "chunksUsed must not be null");
    Objects.requireNonNull(createdAt, "createdAt must not be null");

    if (tenantId.isBlank()) {
      throw new IllegalArgumentException("tenantId must not be blank");
    }
    if (model.isBlank()) {
      throw new IllegalArgumentException("model must not be blank");
    }
    if (promptVersion.isBlank()) {
      throw new IllegalArgumentException("promptVersion must not be blank");
    }
    if (inputHash.isBlank()) {
      throw new IllegalArgumentException("inputHash must not be blank");
    }
    if (durationMs < 0) {
      throw new IllegalArgumentException("durationMs must not be negative, got: " + durationMs);
    }
    if (confidenceScore < 0.0 || confidenceScore > 1.0) {
      throw new IllegalArgumentException(
          "confidenceScore must be between 0.0 and 1.0, got: " + confidenceScore);
    }

    this.tenantId = tenantId;
    this.model = model;
    this.promptVersion = promptVersion;
    this.inputHash = inputHash;
    this.durationMs = durationMs;
    this.confidenceScore = confidenceScore;
    this.fallback = fallback;
    this.result = result;
    this.chunksUsed = List.copyOf(chunksUsed);
    this.createdAt = createdAt;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getModel() {
    return model;
  }

  public String getPromptVersion() {
    return promptVersion;
  }

  public String getInputHash() {
    return inputHash;
  }

  public long getDurationMs() {
    return durationMs;
  }

  public double getConfidenceScore() {
    return confidenceScore;
  }

  public boolean isFallback() {
    return fallback;
  }

  public String getResult() {
    return result;
  }

  public List<String> getChunksUsed() {
    return chunksUsed;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  @Override
  public String toString() {
    return "AIAnalysisRecord{"
        + "id="
        + getId()
        + ", tenantId='"
        + tenantId
        + '\''
        + ", model='"
        + model
        + '\''
        + ", promptVersion='"
        + promptVersion
        + '\''
        + ", durationMs="
        + durationMs
        + ", confidenceScore="
        + confidenceScore
        + ", fallback="
        + fallback
        + ", createdAt="
        + createdAt
        + '}';
  }
}
