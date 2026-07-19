package com.atlasops.auth.presentation;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for user login.
 *
 * @param email the user's email address
 * @param password the user's password
 */
public record LoginRequest(
    @NotBlank(message = "email must not be blank") String email,
    @NotBlank(message = "password must not be blank") String password) {}
