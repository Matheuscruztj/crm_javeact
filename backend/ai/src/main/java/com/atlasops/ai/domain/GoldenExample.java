package com.atlasops.ai.domain;

import com.atlasops.shared.domain.Entity;
import java.time.Instant;
import java.util.Objects;

/**
 * A golden example for RAG evaluation — a curated query/expected-answer pair
 * used to measure quality of AI responses across prompt versions.
 *
 * <p>Validates: P0.G.2 — Golden Dataset + AI Evaluation Framework
 */
public final class GoldenExample extends Entity<String> {

  private final String tenantId;
  private final String query;
  private final String expectedAnswer;
  private final String category;
  private final String createdBy;
  private final Instant createdAt;

  private GoldenExample(
      String id,
      String tenantId,
      String query,
      String expectedAnswer,
      String category,
      String createdBy,
      Instant createdAt) {
    super(id);
    this.tenantId = Objects.requireNonNull(tenantId, "TenantId must not be null");
    this.query = Objects.requireNonNull(query, "Query must not be null");
    this.expectedAnswer = Objects.requireNonNull(expectedAnswer, "ExpectedAnswer must not be null");
    this.category = category;
    this.createdBy = Objects.requireNonNull(createdBy, "CreatedBy must not be null");
    this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt must not be null");

    if (query.isBlank()) throw new IllegalArgumentException("Query must not be blank");
    if (expectedAnswer.isBlank()) throw new IllegalArgumentException("ExpectedAnswer must not be blank");
  }

  public static GoldenExample create(
      String id, String tenantId, String query, String expectedAnswer,
      String category, String createdBy, Instant createdAt) {
    return new GoldenExample(id, tenantId, query, expectedAnswer, category, createdBy, createdAt);
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getQuery() {
    return query;
  }

  public String getExpectedAnswer() {
    return expectedAnswer;
  }

  public String getCategory() {
    return category;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
