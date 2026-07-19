package com.atlasops.customers.domain.ports;

import com.atlasops.customers.domain.Customer;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Port defining persistence operations for Customer aggregates. All query methods require tenant
 * context for data isolation.
 */
public interface CustomerRepository {

  /**
   * Persists a customer (create or update).
   *
   * @param customer the customer to persist
   * @return the persisted customer
   */
  Customer save(Customer customer);

  /**
   * Finds a customer by their unique identifier within a tenant.
   *
   * @param id the customer identifier
   * @param tenantId the tenant identifier
   * @return the customer if found
   */
  Optional<Customer> findById(String id, String tenantId);

  /**
   * Checks whether a customer with the given email already exists within a tenant.
   *
   * @param email the customer email address (case-insensitive)
   * @param tenantId the tenant identifier
   * @return true if a customer with that email exists in the tenant
   */
  boolean existsByEmailAndTenantId(String email, String tenantId);

  /**
   * Finds all customers belonging to a tenant with pagination.
   *
   * @param tenantId the tenant identifier
   * @param pageable pagination parameters
   * @return a page of customers
   */
  Page<Customer> findByTenantId(String tenantId, Pageable pageable);

  /**
   * Searches customers by name or email (case-insensitive partial match).
   *
   * @param query the search query (minimum 2 characters)
   * @param tenantId the tenant identifier
   * @param pageable pagination parameters
   * @return a page of matching customers
   */
  Page<Customer> searchByNameOrEmail(String query, String tenantId, Pageable pageable);

  /**
   * Finds customers within a specified radius from a center point, ordered by distance ascending.
   *
   * @param latitude center point latitude
   * @param longitude center point longitude
   * @param distanceKm radius in kilometers
   * @param tenantId the tenant identifier
   * @param pageable pagination parameters
   * @return a page of customers within the radius, ordered by distance
   */
  Page<Customer> findByRadius(
      double latitude, double longitude, double distanceKm, String tenantId, Pageable pageable);
}
