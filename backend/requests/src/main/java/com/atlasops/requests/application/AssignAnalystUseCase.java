package com.atlasops.requests.application;

import com.atlasops.requests.domain.ServiceRequest;
import com.atlasops.requests.domain.ports.ServiceRequestRepository;
import com.atlasops.shared.domain.exceptions.BusinessRuleViolationException;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.ports.Clock;
import java.util.Objects;

/**
 * Use case for assigning an analyst to a service request. The request must be in OPEN status; upon
 * assignment it transitions to IN_PROGRESS.
 */
public class AssignAnalystUseCase {

  private final ServiceRequestRepository serviceRequestRepository;
  private final Clock clock;

  public AssignAnalystUseCase(ServiceRequestRepository serviceRequestRepository, Clock clock) {
    this.serviceRequestRepository = serviceRequestRepository;
    this.clock = clock;
  }

  /**
   * Assigns an analyst to a request, transitioning it from OPEN to IN_PROGRESS.
   *
   * @param command the assignment command
   * @return the updated service request
   * @throws ResourceNotFoundException if the request is not found
   * @throws BusinessRuleViolationException if the request is not in OPEN status
   */
  public ServiceRequest execute(AssignAnalystCommand command) {
    Objects.requireNonNull(command, "Command must not be null");

    ServiceRequest request =
        serviceRequestRepository
            .findByIdAndTenantId(command.requestId(), command.tenantId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Request with id '" + command.requestId() + "' not found"));

    try {
      request.assignAnalyst(command.analystId(), clock.now());
    } catch (IllegalStateException ex) {
      throw new BusinessRuleViolationException(ex.getMessage());
    }

    return serviceRequestRepository.save(request);
  }

  /**
   * Command for assigning an analyst to a request.
   *
   * @param requestId the request identifier
   * @param analystId the analyst's user identifier
   * @param tenantId the tenant identifier
   */
  public record AssignAnalystCommand(String requestId, String analystId, String tenantId) {

    public AssignAnalystCommand {
      Objects.requireNonNull(requestId, "Request id must not be null");
      Objects.requireNonNull(analystId, "Analyst id must not be null");
      Objects.requireNonNull(tenantId, "Tenant id must not be null");
    }
  }
}
