package com.atlasops.requests.application;

import com.atlasops.requests.domain.ServiceRequest;
import java.util.List;

/**
 * Application layer representation of a paginated service request result.
 *
 * <p>This record encapsulates pagination metadata returned from the application layer, preventing
 * direct dependency on domain ports from the presentation layer.
 *
 * @param content the list of requests in this page
 * @param pageNumber the current page number (zero-based)
 * @param pageSize the page size
 * @param totalElements total number of matching elements
 * @param totalPages total number of pages
 */
public record ServiceRequestPageResult(
    List<ServiceRequest> content,
    int pageNumber,
    int pageSize,
    long totalElements,
    int totalPages) {}
