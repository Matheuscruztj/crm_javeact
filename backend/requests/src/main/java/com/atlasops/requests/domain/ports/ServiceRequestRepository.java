package com.atlasops.requests.domain.ports;

import com.atlasops.requests.domain.RequestPriority;
import com.atlasops.requests.domain.RequestStatus;
import com.atlasops.requests.domain.ServiceRequest;
import java.util.List;
import java.util.Optional;

/** Port defining persistence operations for the ServiceRequest aggregate. */
public interface ServiceRequestRepository {

  /**
   * Finds a service request by its identifier and tenant.
   *
   * @param id the request identifier
   * @param tenantId the tenant identifier
   * @return the request if found, empty otherwise
   */
  Optional<ServiceRequest> findByIdAndTenantId(String id, String tenantId);

  /**
   * Finds all service requests for a tenant with pagination and optional filters.
   *
   * @param tenantId the tenant identifier
   * @param status optional status filter (null means all)
   * @param priority optional priority filter (null means all)
   * @param customerId optional customer filter (null means all)
   * @param page zero-based page number
   * @param size page size (max 100)
   * @return a page of service requests matching the criteria
   */
  ServiceRequestPage findAllByTenantId(
      String tenantId,
      RequestStatus status,
      RequestPriority priority,
      String customerId,
      int page,
      int size);

  /**
   * Persists a service request (insert or update).
   *
   * @param request the service request to persist
   * @return the persisted service request
   */
  ServiceRequest save(ServiceRequest request);

  /**
   * A page of service requests with pagination metadata.
   *
   * @param content the list of requests in this page
   * @param pageNumber the current page number (zero-based)
   * @param pageSize the page size
   * @param totalElements total number of matching elements
   * @param totalPages total number of pages
   */
  record ServiceRequestPage(
      List<ServiceRequest> content,
      int pageNumber,
      int pageSize,
      long totalElements,
      int totalPages) {}
}
