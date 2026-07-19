package com.atlasops.requests.application;

import com.atlasops.requests.domain.ServiceRequest;
import com.atlasops.requests.domain.ports.ServiceRequestRepository;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import java.util.Objects;

/** Use case for retrieving a single service request by id with tenant isolation. */
public class GetRequestUseCase {

  private final ServiceRequestRepository serviceRequestRepository;

  public GetRequestUseCase(ServiceRequestRepository serviceRequestRepository) {
    this.serviceRequestRepository = serviceRequestRepository;
  }

  /**
   * Retrieves a service request by id within the specified tenant.
   *
   * @param requestId the request identifier
   * @param tenantId the tenant identifier
   * @return the service request
   * @throws ResourceNotFoundException if the request is not found
   */
  public ServiceRequest execute(String requestId, String tenantId) {
    Objects.requireNonNull(requestId, "Request id must not be null");
    Objects.requireNonNull(tenantId, "Tenant id must not be null");

    return serviceRequestRepository
        .findByIdAndTenantId(requestId, tenantId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Request with id '" + requestId + "' not found"));
  }
}
