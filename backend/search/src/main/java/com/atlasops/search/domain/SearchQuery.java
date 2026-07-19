package com.atlasops.search.domain;

import java.util.Objects;

/**
 * Value object representing a unified search query across the system. Validates query constraints
 * at construction time.
 *
 * @param query the search text (2-200 characters)
 * @param entityTypeFilter optional filter by entity type (e.g., CUSTOMER, DOCUMENT, REQUEST); null
 *     means all types
 * @param tenantId the tenant context for the search (required)
 */
public record SearchQuery(String query, String entityTypeFilter, String tenantId) {

  private static final int QUERY_MIN_LENGTH = 2;
  private static final int QUERY_MAX_LENGTH = 200;

  public SearchQuery {
    Objects.requireNonNull(query, "Query must not be null");
    Objects.requireNonNull(tenantId, "TenantId must not be null");

    if (query.isBlank()) {
      throw new IllegalArgumentException("Query must not be blank");
    }
    if (query.length() < QUERY_MIN_LENGTH) {
      throw new IllegalArgumentException(
          "Query must be at least " + QUERY_MIN_LENGTH + " characters, got: " + query.length());
    }
    if (query.length() > QUERY_MAX_LENGTH) {
      throw new IllegalArgumentException(
          "Query must be at most " + QUERY_MAX_LENGTH + " characters, got: " + query.length());
    }
    if (tenantId.isBlank()) {
      throw new IllegalArgumentException("TenantId must not be blank");
    }
  }

  /**
   * Creates a SearchQuery without entity type filter.
   *
   * @param query the search text
   * @param tenantId the tenant context
   * @return a SearchQuery searching across all entity types
   */
  public static SearchQuery withoutFilter(String query, String tenantId) {
    return new SearchQuery(query, null, tenantId);
  }
}
