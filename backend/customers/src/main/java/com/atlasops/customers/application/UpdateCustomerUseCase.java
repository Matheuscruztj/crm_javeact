package com.atlasops.customers.application;

import com.atlasops.customers.domain.Address;
import com.atlasops.customers.domain.Customer;
import com.atlasops.customers.domain.ports.CustomerRepository;
import com.atlasops.shared.domain.exceptions.DuplicateResourceException;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.types.Email;
import java.util.Objects;

/**
 * Use case for updating an existing customer's fields. Validates input and enforces email
 * uniqueness per tenant.
 */
public class UpdateCustomerUseCase {

  private final CustomerRepository customerRepository;
  private final Clock clock;

  public UpdateCustomerUseCase(CustomerRepository customerRepository, Clock clock) {
    this.customerRepository = customerRepository;
    this.clock = clock;
  }

  /**
   * Updates a customer with the given command parameters.
   *
   * @param command the update customer command
   * @return the updated Customer
   * @throws ResourceNotFoundException if the customer is not found
   * @throws DuplicateResourceException if the new email already exists for another customer in the
   *     tenant
   */
  public Customer execute(UpdateCustomerCommand command) {
    Objects.requireNonNull(command, "Command must not be null");

    Customer customer =
        customerRepository
            .findById(command.customerId(), command.tenantId())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Customer with id '" + command.customerId() + "' not found"));

    Email newEmail = new Email(command.email());

    if (!customer.getEmail().equals(newEmail)
        && customerRepository.existsByEmailAndTenantId(newEmail.getValue(), command.tenantId())) {
      throw new DuplicateResourceException(
          "Customer with email '" + newEmail.getValue() + "' already exists in this tenant");
    }

    Address address = buildAddress(command);
    customer.update(command.name(), newEmail, address, clock.now());

    return customerRepository.save(customer);
  }

  private Address buildAddress(UpdateCustomerCommand command) {
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
}
