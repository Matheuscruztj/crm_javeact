package com.atlasops.customers.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating an existing customer.
 *
 * @param name the customer name (1-150 characters, required)
 * @param email the customer email (required)
 * @param street optional street address
 * @param city optional city
 * @param state optional state
 * @param postalCode optional postal code
 * @param country optional country
 * @param latitude optional latitude (must pair with longitude)
 * @param longitude optional longitude (must pair with latitude)
 */
public record UpdateCustomerRequest(
    @NotBlank(message = "Customer name must not be blank")
        @Size(max = 150, message = "Customer name must not exceed 150 characters")
        String name,
    @NotBlank(message = "Email must not be blank") String email,
    String street,
    String city,
    String state,
    String postalCode,
    String country,
    Double latitude,
    Double longitude) {}
