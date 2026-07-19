package com.atlasops.customers.application;

import com.atlasops.customers.domain.Customer;
import com.atlasops.customers.domain.ports.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Use case for finding customers within a specified radius from a center point. Delegates to the
 * repository which performs a geospatial ST_DWithin query.
 */
public class FindCustomersByRadiusUseCase {

  private final CustomerRepository customerRepository;

  public FindCustomersByRadiusUseCase(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  /**
   * Finds all customers within the specified radius from the given center coordinates.
   *
   * @param latitude center point latitude (must be between -90 and 90)
   * @param longitude center point longitude (must be between -180 and 180)
   * @param radiusKm radius in kilometers (must be positive)
   * @param tenantId tenant identifier for data isolation
   * @param pageable pagination parameters
   * @return a page of customers within the radius, ordered by distance ascending
   * @throws IllegalArgumentException if coordinates are out of bounds or radius is not positive
   */
  public Page<Customer> execute(
      double latitude, double longitude, double radiusKm, String tenantId, Pageable pageable) {
    validateInput(latitude, longitude, radiusKm, tenantId);
    return customerRepository.findByRadius(latitude, longitude, radiusKm, tenantId, pageable);
  }

  private void validateInput(double latitude, double longitude, double radiusKm, String tenantId) {
    if (latitude < -90.0 || latitude > 90.0) {
      throw new IllegalArgumentException("Latitude must be between -90 and 90, got: " + latitude);
    }
    if (longitude < -180.0 || longitude > 180.0) {
      throw new IllegalArgumentException(
          "Longitude must be between -180 and 180, got: " + longitude);
    }
    if (radiusKm <= 0) {
      throw new IllegalArgumentException("Radius must be positive, got: " + radiusKm);
    }
    if (tenantId == null || tenantId.isBlank()) {
      throw new IllegalArgumentException("TenantId must not be null or empty");
    }
  }
}
