package com.atlasops.requests.presentation;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for assigning an analyst to a service request.
 *
 * @param analystId the analyst's user identifier
 */
public record AssignAnalystRequest(
    @NotBlank(message = "Analyst id must not be blank") String analystId) {}
