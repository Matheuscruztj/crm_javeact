package com.atlasops.search.domain.ports;

import com.atlasops.search.domain.SearchQuery;
import com.atlasops.search.domain.SearchResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Port defining the contract for executing search queries against the search index. Implementations
 * may use PostgreSQL full-text search, OpenSearch, or other engines.
 */
public interface SearchIndexPort {

  /**
   * Executes a search query and returns a paginated list of matching results.
   *
   * @param query the search query containing text, optional entity type filter, and tenant context
   * @param pageable pagination parameters (page number, size, sort)
   * @return a page of search results matching the query
   */
  Page<SearchResult> search(SearchQuery query, Pageable pageable);
}
