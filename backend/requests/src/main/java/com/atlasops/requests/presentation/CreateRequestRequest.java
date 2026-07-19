package com.atlasops.requests.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new service request.
 *
 * @param title the request title (1-200 characters)
 * @param description the request description (1-5000 characters)
 * @param priority the priority (optional, defaults to MEDIUM)
 * @param customerId the customer identifier
 */
public record CreateRequestRequest(
    @NotBlank(message = "Title must not be blank")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,
    @NotBlank(message = "Description must not be blank")
        @Size(max = 5000, message = "Description must not exceed 5000 characters")
        String description,
    String priority,
    @NotBlank(message = "Customer id must not be blank") String customerId) {}
