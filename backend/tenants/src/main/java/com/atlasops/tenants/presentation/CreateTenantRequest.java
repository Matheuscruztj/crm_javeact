package com.atlasops.tenants.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new tenant.
 *
 * @param name the tenant name (3-100 characters, alphanumeric, hyphens, spaces)
 */
public record CreateTenantRequest(
    @NotBlank(message = "Tenant name must not be blank")
        @Size(min = 3, max = 100, message = "Tenant name must be between 3 and 100 characters")
        String name) {}
