package com.atlasops.customers.application;

import com.atlasops.customers.domain.Customer;
import com.atlasops.customers.domain.ports.CustomerRepository;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/** Use case for listing customers with pagination. Paginated listing: default 20, max 100. */
public class ListCustomersUseCase {

  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int MAX_PAGE_SIZE = 100;

  private final CustomerRepository customerRepository;

  public ListCustomersUseCase(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  /**
   * Lists customers for the given tenant with pagination.
   *
   * @param tenantId the tenant identifier
   * @param page zero-based page number
   * @param size page size (default 20, max 100)
   * @return a page of customers
   */
  public Page<Customer> execute(String tenantId, int page, int size) {
    Objects.requireNonNull(tenantId, "Tenant id must not be null");

    if (page < 0) {
      page = 0;
    }
    if (size < 1) {
      size = DEFAULT_PAGE_SIZE;
    }
    if (size > MAX_PAGE_SIZE) {
      size = MAX_PAGE_SIZE;
    }

    return customerRepository.findByTenantId(tenantId, PageRequest.of(page, size));
  }
}
