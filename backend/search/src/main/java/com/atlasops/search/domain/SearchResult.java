package com.atlasops.search.domain;

import java.util.Objects;

/**
 * Value object representing a single search result item. Validates snippet length and relevance
 * score constraints at construction time.
 *
 * @param entityType the type of the matched entity (e.g., CUSTOMER, DOCUMENT, REQUEST)
 * @param entityId the identifier of the matched entity
 * @param title the display title of the matched entity
 * @param snippet a text snippet highlighting the match (max 200 characters)
 * @param relevanceScore the relevance score of the match (0.0 to 1.0 inclusive)
 */
public record SearchResult(
    String entityType, String entityId, String title, String snippet, double relevanceScore) {

  private static final int SNIPPET_MAX_LENGTH = 200;
  private static final double MIN_SCORE = 0.0;
  private static final double MAX_SCORE = 1.0;

  public SearchResult {
    Objects.requireNonNull(entityType, "EntityType must not be null");
    Objects.requireNonNull(entityId, "EntityId must not be null");
    Objects.requireNonNull(title, "Title must not be null");
    Objects.requireNonNull(snippet, "Snippet must not be null");

    if (entityType.isBlank()) {
      throw new IllegalArgumentException("EntityType must not be blank");
    }
    if (entityId.isBlank()) {
      throw new IllegalArgumentException("EntityId must not be blank");
    }
    if (title.isBlank()) {
      throw new IllegalArgumentException("Title must not be blank");
    }
    if (snippet.length() > SNIPPET_MAX_LENGTH) {
      throw new IllegalArgumentException(
          "Snippet must be at most "
              + SNIPPET_MAX_LENGTH
              + " characters, got: "
              + snippet.length());
    }
    if (relevanceScore < MIN_SCORE || relevanceScore > MAX_SCORE) {
      throw new IllegalArgumentException(
          "RelevanceScore must be between "
              + MIN_SCORE
              + " and "
              + MAX_SCORE
              + ", got: "
              + relevanceScore);
    }
  }
}
