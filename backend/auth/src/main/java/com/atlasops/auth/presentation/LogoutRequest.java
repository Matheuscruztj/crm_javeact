package com.atlasops.auth.presentation;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for user logout.
 *
 * @param refreshToken the refresh token to invalidate
 */
public record LogoutRequest(
    @NotBlank(message = "refreshToken must not be blank") String refreshToken) {}
