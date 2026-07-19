package com.atlasops.auth.presentation;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for token refresh.
 *
 * @param refreshToken the refresh token to rotate
 */
public record RefreshRequest(
    @NotBlank(message = "refreshToken must not be blank") String refreshToken) {}
