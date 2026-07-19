package com.atlasops.requests.application;

import com.atlasops.requests.domain.RequestStatus;
import com.atlasops.requests.domain.ServiceRequest;
import com.atlasops.requests.domain.ports.ServiceRequestRepository;
import com.atlasops.shared.domain.events.RequestStatusChangedEvent;
import com.atlasops.shared.domain.exceptions.BusinessRuleViolationException;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.ports.EventPublisher;
import java.util.Objects;

/**
 * Use case for transitioning a service request to a new status. Validates the state machine rules
 * and publishes a RequestStatusChangedEvent.
 */
public class TransitionRequestStatusUseCase {

  private final ServiceRequestRepository serviceRequestRepository;
  private final EventPublisher eventPublisher;

  public TransitionRequestStatusUseCase(
      ServiceRequestRepository serviceRequestRepository, EventPublisher eventPublisher) {
    this.serviceRequestRepository = serviceRequestRepository;
    this.eventPublisher = eventPublisher;
  }

  /**
   * Transitions a request to the specified target status.
   *
   * @param command the transition command
   * @return the updated service request
   * @throws ResourceNotFoundException if the request is not found
   * @throws BusinessRuleViolationException if the transition is not allowed
   */
  public ServiceRequest execute(TransitionCommand command) {
    Objects.requireNonNull(command, "Command must not be null");

    ServiceRequest request =
        serviceRequestRepository
            .findByIdAndTenantId(command.requestId(), command.tenantId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Request with id '" + command.requestId() + "' not found"));

    String previousStatus = request.getStatus().name();

    try {
      request.transitionTo(command.targetStatus());
    } catch (IllegalStateException ex) {
      throw new BusinessRuleViolationException(ex.getMessage());
    }

    ServiceRequest saved = serviceRequestRepository.save(request);

    eventPublisher.publish(
        new RequestStatusChangedEvent(
            saved.getId(),
            previousStatus,
            saved.getStatus().name(),
            command.actorId() != null ? command.actorId() : "system",
            command.tenantId(),
            null));

    return saved;
  }

  /**
   * Command for transitioning a request status.
   *
   * @param requestId the request identifier
   * @param targetStatus the target status
   * @param tenantId the tenant identifier
   * @param actorId the user performing the transition (nullable, defaults to "system")
   */
  public record TransitionCommand(
      String requestId, RequestStatus targetStatus, String tenantId, String actorId) {

    public TransitionCommand(String requestId, RequestStatus targetStatus, String tenantId) {
      this(requestId, targetStatus, tenantId, null);
    }

    public TransitionCommand {
      Objects.requireNonNull(requestId, "Request id must not be null");
      Objects.requireNonNull(targetStatus, "Target status must not be null");
      Objects.requireNonNull(tenantId, "Tenant id must not be null");
    }
  }
}
