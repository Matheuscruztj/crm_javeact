package com.atlasops.customers.presentation;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for associating a CLIENT user with a customer.
 *
 * @param userId the user identifier to associate
 */
public record AssociateClientUserRequest(
    @NotBlank(message = "User id must not be blank") String userId) {}
