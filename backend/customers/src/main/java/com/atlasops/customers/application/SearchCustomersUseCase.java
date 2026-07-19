package com.atlasops.customers.application;

import com.atlasops.customers.domain.Customer;
import com.atlasops.customers.domain.ports.CustomerRepository;
import java.util.Objects;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Use case for searching customers by name or email with partial match (min 2 chars). Results are
 * paginated: default 20, max 100.
 */
public class SearchCustomersUseCase {

  private static final int MIN_QUERY_LENGTH = 2;
  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int MAX_PAGE_SIZE = 100;

  private final CustomerRepository customerRepository;

  public SearchCustomersUseCase(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  /**
   * Searches customers by partial match on name or email.
   *
   * @param query the search query (minimum 2 characters)
   * @param tenantId the tenant identifier
   * @param page zero-based page number
   * @param size page size (default 20, max 100)
   * @return a page of matching customers
   * @throws IllegalArgumentException if query is less than 2 characters
   */
  public Page<Customer> execute(String query, String tenantId, int page, int size) {
    Objects.requireNonNull(query, "Search query must not be null");
    Objects.requireNonNull(tenantId, "Tenant id must not be null");

    if (query.length() < MIN_QUERY_LENGTH) {
      throw new IllegalArgumentException(
          "Search query must be at least " + MIN_QUERY_LENGTH + " characters");
    }

    if (page < 0) {
      page = 0;
    }
    if (size < 1) {
      size = DEFAULT_PAGE_SIZE;
    }
    if (size > MAX_PAGE_SIZE) {
      size = MAX_PAGE_SIZE;
    }

    return customerRepository.searchByNameOrEmail(query, tenantId, PageRequest.of(page, size));
  }
}
