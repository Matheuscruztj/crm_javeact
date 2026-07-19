package com.atlasops.requests.application;

import com.atlasops.requests.domain.RequestStatusHistory;
import com.atlasops.requests.domain.ports.RequestStatusHistoryRepository;
import com.atlasops.requests.domain.ports.ServiceRequestRepository;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.Objects;

/**
 * Use case for retrieving the status transition history of a service request.
 */
public class GetRequestStatusHistoryUseCase {

  private final ServiceRequestRepository serviceRequestRepository;
  private final RequestStatusHistoryRepository historyRepository;

  public GetRequestStatusHistoryUseCase(
      ServiceRequestRepository serviceRequestRepository,
      RequestStatusHistoryRepository historyRepository) {
    this.serviceRequestRepository = Objects.requireNonNull(serviceRequestRepository);
    this.historyRepository = Objects.requireNonNull(historyRepository);
  }

  /**
   * Returns the status transition history for a request, oldest transition first.
   *
   * @param requestId the request identifier
   * @param tenantId the tenant identifier
   * @return list of history records ordered by {@code occurredAt} ascending
   * @throws ResourceNotFoundException if the request is not found
   */
  public List<RequestStatusHistory> execute(String requestId, String tenantId) {
    Objects.requireNonNull(requestId, "RequestId must not be null");
    Objects.requireNonNull(tenantId, "TenantId must not be null");

    // Validate request existence (also validates tenant isolation)
    serviceRequestRepository
        .findByIdAndTenantId(requestId, tenantId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Request with id '" + requestId + "' not found"));

    return historyRepository.findByRequestId(requestId, tenantId);
  }
}
