package com.atlasops.requests.application;

import com.atlasops.requests.domain.RequestPriority;
import com.atlasops.requests.domain.ServiceRequest;
import com.atlasops.requests.domain.ports.ServiceRequestRepository;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import java.util.Objects;

/**
 * Use case for creating a new service request. Creates the request with OPEN status and defaults
 * priority to MEDIUM if not specified.
 */
public class CreateRequestUseCase {

  private final ServiceRequestRepository serviceRequestRepository;
  private final IdGenerator idGenerator;
  private final Clock clock;

  public CreateRequestUseCase(
      ServiceRequestRepository serviceRequestRepository, IdGenerator idGenerator, Clock clock) {
    this.serviceRequestRepository = serviceRequestRepository;
    this.idGenerator = idGenerator;
    this.clock = clock;
  }

  /**
   * Creates a new service request.
   *
   * @param command the creation command
   * @return the created service request
   * @throws IllegalArgumentException if title or description are invalid
   */
  public ServiceRequest execute(CreateRequestCommand command) {
    Objects.requireNonNull(command, "Command must not be null");

    RequestPriority priority =
        command.priority() != null ? command.priority() : RequestPriority.MEDIUM;

    ServiceRequest request =
        ServiceRequest.create(
            idGenerator.generate(),
            command.title(),
            command.description(),
            priority,
            command.customerId(),
            command.tenantId(),
            clock.now());

    return serviceRequestRepository.save(request);
  }

  /**
   * Command for creating a service request.
   *
   * @param title the request title (1-200 characters)
   * @param description the request description (1-5000 characters)
   * @param priority the priority (nullable, defaults to MEDIUM)
   * @param customerId the customer identifier
   * @param tenantId the tenant identifier
   */
  public record CreateRequestCommand(
      String title,
      String description,
      RequestPriority priority,
      String customerId,
      String tenantId) {

    public CreateRequestCommand {
      Objects.requireNonNull(title, "Title must not be null");
      Objects.requireNonNull(description, "Description must not be null");
      Objects.requireNonNull(customerId, "Customer id must not be null");
      Objects.requireNonNull(tenantId, "Tenant id must not be null");
    }
  }
}
