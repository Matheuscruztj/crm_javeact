package com.atlasops.customers.application;

/**
 * Command for creating a new customer within a tenant.
 *
 * @param name the customer name (1-150 characters)
 * @param email the customer email address
 * @param street optional street address
 * @param city optional city
 * @param state optional state
 * @param postalCode optional postal code
 * @param country optional country
 * @param latitude optional latitude (must pair with longitude)
 * @param longitude optional longitude (must pair with latitude)
 * @param tenantId the tenant this customer belongs to
 * @param actorId the user performing the action
 */
public record CreateCustomerCommand(
    String name,
    String email,
    String street,
    String city,
    String state,
    String postalCode,
    String country,
    Double latitude,
    Double longitude,
    String tenantId,
    String actorId) {}
