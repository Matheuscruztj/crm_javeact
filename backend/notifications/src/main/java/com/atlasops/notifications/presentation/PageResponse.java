package com.atlasops.notifications.presentation;

import java.util.List;

/**
 * Generic paginated response envelope following the API convention.
 *
 * @param content the list of items in this page
 * @param page the pagination metadata
 * @param <T> the type of items in the page
 */
public record PageResponse<T>(List<T> content, PageMetadata page) {

  /**
   * Pagination metadata.
   *
   * @param number the current page number (zero-based)
   * @param size the page size
   * @param totalElements total number of matching elements
   * @param totalPages total number of pages
   */
  public record PageMetadata(int number, int size, long totalElements, int totalPages) {}
}
