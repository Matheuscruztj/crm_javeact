package com.atlasops.search.application;

import com.atlasops.search.domain.SearchResult;

/**
 * Application read model for unified search results.
 *
 * <p>This keeps the presentation layer decoupled from the domain aggregate while preserving the
 * same contract shape for API responses.
 */
public record SearchResultView(
    String entityType, String entityId, String title, String snippet, double relevanceScore) {

  public static SearchResultView from(SearchResult result) {
    return new SearchResultView(
        result.entityType(),
        result.entityId(),
        result.title(),
        result.snippet(),
        result.relevanceScore());
  }
}
