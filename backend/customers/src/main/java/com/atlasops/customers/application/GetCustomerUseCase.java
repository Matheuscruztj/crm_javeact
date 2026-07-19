package com.atlasops.customers.application;

import com.atlasops.customers.domain.Customer;
import com.atlasops.customers.domain.ports.CustomerRepository;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import java.util.Objects;

/** Use case for retrieving a single customer by ID with tenant isolation. */
public class GetCustomerUseCase {

  private final CustomerRepository customerRepository;

  public GetCustomerUseCase(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  /**
   * Retrieves a customer by id within the specified tenant.
   *
   * @param customerId the customer identifier
   * @param tenantId the tenant identifier
   * @return the customer
   * @throws ResourceNotFoundException if the customer is not found
   */
  public Customer execute(String customerId, String tenantId) {
    Objects.requireNonNull(customerId, "Customer id must not be null");
    Objects.requireNonNull(tenantId, "Tenant id must not be null");

    return customerRepository
        .findById(customerId, tenantId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Customer with id '" + customerId + "' not found"));
  }
}
