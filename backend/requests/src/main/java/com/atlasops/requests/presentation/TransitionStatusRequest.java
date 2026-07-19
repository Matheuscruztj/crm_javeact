package com.atlasops.requests.presentation;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for transitioning a service request status.
 *
 * @param targetStatus the target status to transition to
 */
public record TransitionStatusRequest(
    @NotBlank(message = "Target status must not be blank") String targetStatus) {}
