package com.atlasops.customers.application;

import java.util.Objects;

/**
 * Command for updating an existing customer.
 *
 * @param customerId the customer identifier
 * @param name the new customer name (1-150 characters)
 * @param email the new customer email address
 * @param street optional street address
 * @param city optional city
 * @param state optional state
 * @param postalCode optional postal code
 * @param country optional country
 * @param latitude optional latitude (must pair with longitude)
 * @param longitude optional longitude (must pair with latitude)
 * @param tenantId the tenant this customer belongs to
 */
public record UpdateCustomerCommand(
    String customerId,
    String name,
    String email,
    String street,
    String city,
    String state,
    String postalCode,
    String country,
    Double latitude,
    Double longitude,
    String tenantId) {

  public UpdateCustomerCommand {
    Objects.requireNonNull(customerId, "Customer id must not be null");
    Objects.requireNonNull(name, "Name must not be null");
    Objects.requireNonNull(email, "Email must not be null");
    Objects.requireNonNull(tenantId, "Tenant id must not be null");
  }
}
