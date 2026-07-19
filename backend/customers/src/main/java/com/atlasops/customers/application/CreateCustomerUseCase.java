package com.atlasops.customers.application;

import com.atlasops.customers.domain.Address;
import com.atlasops.customers.domain.Customer;
import com.atlasops.customers.domain.ports.CustomerRepository;
import com.atlasops.shared.domain.events.CustomerCreatedEvent;
import com.atlasops.shared.domain.exceptions.DuplicateResourceException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.EventPublisher;
import com.atlasops.shared.domain.ports.IdGenerator;
import com.atlasops.shared.domain.types.Email;
import com.atlasops.shared.domain.types.TenantId;

/**
 * Use case for creating a new customer within a tenant. Validates input, enforces email uniqueness
 * per tenant, and publishes CustomerCreatedEvent.
 */
public class CreateCustomerUseCase {

  private final CustomerRepository customerRepository;
  private final EventPublisher eventPublisher;
  private final IdGenerator idGenerator;
  private final Clock clock;

  public CreateCustomerUseCase(
      CustomerRepository customerRepository,
      EventPublisher eventPublisher,
      IdGenerator idGenerator,
      Clock clock) {
    this.customerRepository = customerRepository;
    this.eventPublisher = eventPublisher;
    this.idGenerator = idGenerator;
    this.clock = clock;
  }

  /**
   * Creates a new customer with the given command parameters.
   *
   * @param command the create customer command
   * @return the persisted Customer
   * @throws IllegalArgumentException if any input validation fails
   * @throws DuplicateResourceException if email already exists within the tenant
   */
  public Customer execute(CreateCustomerCommand command) {
    validateCommand(command);

    Email email = new Email(command.email());

    if (customerRepository.existsByEmailAndTenantId(email.getValue(), command.tenantId())) {
      throw new DuplicateResourceException(
          "Customer with email '" + email.getValue() + "' already exists in this tenant");
    }

    Address address = buildAddress(command);
    TenantId tenantId = new TenantId(command.tenantId());

    String id = idGenerator.generate();
    Customer customer =
        Customer.create(id, command.name().trim(), email, address, tenantId, clock.now());

    Customer saved = customerRepository.save(customer);

    eventPublisher.publish(
        new CustomerCreatedEvent(
            saved.getId(), saved.getName(), command.tenantId(), command.actorId(), null));

    return saved;
  }

  private Address buildAddress(CreateCustomerCommand command) {
    boolean hasAnyAddressField =
        command.street() != null
            || command.city() != null
            || command.state() != null
            || command.postalCode() != null
            || command.country() != null
            || command.latitude() != null
            || command.longitude() != null;

    if (!hasAnyAddressField) {
      return null;
    }

    return new Address(
        command.street(),
        command.city(),
        command.state(),
        command.postalCode(),
        command.country(),
        command.latitude(),
        command.longitude());
  }

  private void validateCommand(CreateCustomerCommand command) {
    if (command.name() == null || command.name().isBlank()) {
      throw new IllegalArgumentException("Customer name must not be null or empty");
    }
    if (command.email() == null || command.email().isBlank()) {
      throw new IllegalArgumentException("Email must not be null or empty");
    }
    if (command.tenantId() == null || command.tenantId().isBlank()) {
      throw new IllegalArgumentException("TenantId must not be null or empty");
    }
    if (command.actorId() == null || command.actorId().isBlank()) {
      throw new IllegalArgumentException("ActorId must not be null or empty");
    }
  }
}
