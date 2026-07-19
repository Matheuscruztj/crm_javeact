package com.atlasops.requests.application;

import com.atlasops.requests.domain.RequestPriority;
import com.atlasops.requests.domain.RequestStatus;
import com.atlasops.requests.domain.ports.ServiceRequestRepository;
import com.atlasops.requests.domain.ports.ServiceRequestRepository.ServiceRequestPage;
import java.util.Objects;

/**
 * Use case for listing service requests with pagination and optional filters. Enforces tenant
 * isolation and supports CLIENT-restricted queries by customerId.
 */
public class ListRequestsUseCase {

  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int MAX_PAGE_SIZE = 100;

  private final ServiceRequestRepository serviceRequestRepository;

  public ListRequestsUseCase(ServiceRequestRepository serviceRequestRepository) {
    this.serviceRequestRepository = serviceRequestRepository;
  }

  /**
   * Lists service requests with pagination and optional filters.
   *
   * @param query the query parameters
   * @return a page of service requests
   */
  public ServiceRequestPageResult execute(ListRequestsQuery query) {
    Objects.requireNonNull(query, "Query must not be null");

    int size = query.size() != null ? query.size() : DEFAULT_PAGE_SIZE;
    if (size < 1) {
      size = DEFAULT_PAGE_SIZE;
    }
    if (size > MAX_PAGE_SIZE) {
      size = MAX_PAGE_SIZE;
    }

    int page = query.page() != null ? query.page() : 0;
    if (page < 0) {
      page = 0;
    }

    ServiceRequestPage domainPage =
        serviceRequestRepository.findAllByTenantId(
            query.tenantId(), query.status(), query.priority(), query.customerId(), page, size);

    return new ServiceRequestPageResult(
        domainPage.content(),
        domainPage.pageNumber(),
        domainPage.pageSize(),
        domainPage.totalElements(),
        domainPage.totalPages());
  }

  /**
   * Query parameters for listing requests.
   *
   * @param tenantId the tenant identifier (required)
   * @param status optional status filter
   * @param priority optional priority filter
   * @param customerId optional customer filter (used for CLIENT role restriction)
   * @param page page number (zero-based, defaults to 0)
   * @param size page size (defaults to 20, max 100)
   */
  public record ListRequestsQuery(
      String tenantId,
      RequestStatus status,
      RequestPriority priority,
      String customerId,
      Integer page,
      Integer size) {

    public ListRequestsQuery {
      Objects.requireNonNull(tenantId, "Tenant id must not be null");
    }
  }
}
