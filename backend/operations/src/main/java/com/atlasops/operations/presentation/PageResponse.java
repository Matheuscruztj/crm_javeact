package com.atlasops.operations.presentation;

import java.util.List;

/**
 * Generic paginated response wrapper.
 *
 * @param <T> the type of content items
 */
public record PageResponse<T>(List<T> content, PageMetadata page) {

  public record PageMetadata(int number, int size, long totalElements, int totalPages) {}
}
