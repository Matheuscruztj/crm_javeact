package com.atlasops.search.presentation;

import com.atlasops.search.application.SearchResultView;
import com.atlasops.search.domain.SearchResult;

/**
 * REST response representation for a search result.
 *
 * @param entityType the type of the matched entity
 * @param entityId the identifier of the matched entity
 * @param title the display title of the matched entity
 * @param snippet a text snippet highlighting the match
 * @param relevanceScore the relevance score of the match (0.0 to 1.0)
 */
public record SearchResultResponse(
    String entityType, String entityId, String title, String snippet, double relevanceScore) {

  /**
   * Creates a SearchResultResponse from a domain SearchResult.
   *
   * @param searchResult the domain search result
   * @return the response DTO
   */
  public static SearchResultResponse from(SearchResult searchResult) {
    return new SearchResultResponse(
        searchResult.entityType(),
        searchResult.entityId(),
        searchResult.title(),
        searchResult.snippet(),
        searchResult.relevanceScore());
  }

  public static SearchResultResponse from(SearchResultView searchResultView) {
    return new SearchResultResponse(
        searchResultView.entityType(),
        searchResultView.entityId(),
        searchResultView.title(),
        searchResultView.snippet(),
        searchResultView.relevanceScore());
  }
}
