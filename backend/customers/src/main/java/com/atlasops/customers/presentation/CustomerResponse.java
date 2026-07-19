package com.atlasops.customers.presentation;

import com.atlasops.customers.domain.Customer;
import java.time.Instant;

/**
 * Response DTO representing a customer.
 *
 * @param id the customer identifier
 * @param name the customer name
 * @param email the customer email
 * @param street the street address (nullable)
 * @param city the city (nullable)
 * @param state the state (nullable)
 * @param postalCode the postal code (nullable)
 * @param country the country (nullable)
 * @param latitude the latitude coordinate (nullable)
 * @param longitude the longitude coordinate (nullable)
 * @param status the customer status (ACTIVE/INACTIVE)
 * @param tenantId the tenant identifier
 * @param createdAt the creation timestamp
 * @param updatedAt the last update timestamp
 */
public record CustomerResponse(
    String id,
    String name,
    String email,
    String street,
    String city,
    String state,
    String postalCode,
    String country,
    Double latitude,
    Double longitude,
    String status,
    String tenantId,
    Instant createdAt,
    Instant updatedAt) {

  /**
   * Creates a CustomerResponse from a Customer domain object.
   *
   * @param customer the domain customer
   * @return the response DTO
   */
  public static CustomerResponse from(Customer customer) {
    return new CustomerResponse(
        customer.getId(),
        customer.getName(),
        customer.getEmail().getValue(),
        customer.getAddress() != null ? customer.getAddress().getStreet() : null,
        customer.getAddress() != null ? customer.getAddress().getCity() : null,
        customer.getAddress() != null ? customer.getAddress().getState() : null,
        customer.getAddress() != null ? customer.getAddress().getPostalCode() : null,
        customer.getAddress() != null ? customer.getAddress().getCountry() : null,
        customer.getAddress() != null ? customer.getAddress().getLatitude() : null,
        customer.getAddress() != null ? customer.getAddress().getLongitude() : null,
        customer.getStatus().name(),
        customer.getTenantId().getValue(),
        customer.getCreatedAt(),
        customer.getUpdatedAt());
  }
}
