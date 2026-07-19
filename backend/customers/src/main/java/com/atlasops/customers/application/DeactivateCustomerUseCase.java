package com.atlasops.customers.application;

import com.atlasops.customers.domain.Customer;
import com.atlasops.customers.domain.ports.CustomerRepository;
import com.atlasops.shared.domain.exceptions.BusinessRuleViolationException;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.ports.Clock;
import java.util.Objects;

/**
 * Use case for deactivating a customer. Prevents new requests from being created for this customer.
 */
public class DeactivateCustomerUseCase {

  private final CustomerRepository customerRepository;
  private final Clock clock;

  public DeactivateCustomerUseCase(CustomerRepository customerRepository, Clock clock) {
    this.customerRepository = customerRepository;
    this.clock = clock;
  }

  /**
   * Deactivates a customer within the specified tenant.
   *
   * @param customerId the customer identifier
   * @param tenantId the tenant identifier
   * @return the deactivated customer
   * @throws ResourceNotFoundException if the customer is not found
   * @throws BusinessRuleViolationException if the customer is already inactive
   */
  public Customer execute(String customerId, String tenantId) {
    Objects.requireNonNull(customerId, "Customer id must not be null");
    Objects.requireNonNull(tenantId, "Tenant id must not be null");

    Customer customer =
        customerRepository
            .findById(customerId, tenantId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Customer with id '" + customerId + "' not found"));

    try {
      customer.deactivate(clock.now());
    } catch (IllegalStateException ex) {
      throw new BusinessRuleViolationException(ex.getMessage());
    }

    return customerRepository.save(customer);
  }
}
