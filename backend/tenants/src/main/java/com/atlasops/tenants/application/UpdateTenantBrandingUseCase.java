package com.atlasops.tenants.application;

import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.tenants.domain.Tenant;
import com.atlasops.tenants.domain.ports.TenantRepository;
import java.util.Objects;

/**
 * Use case for updating tenant branding (logo URL and primary color).
 *
 * <p>Validates: P2.12 — Tenant branding endpoint
 */
public class UpdateTenantBrandingUseCase {

    private static final String HEX_COLOR_REGEX = "^#[0-9A-Fa-f]{6}$";

    private final TenantRepository tenantRepository;
    private final Clock clock;

    public UpdateTenantBrandingUseCase(TenantRepository tenantRepository, Clock clock) {
        this.tenantRepository = Objects.requireNonNull(tenantRepository, "TenantRepository must not be null");
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    /**
     * Updates the branding for the given tenant.
     *
     * @param command the branding update command
     * @return the updated tenant
     * @throws ResourceNotFoundException    if the tenant does not exist
     * @throws IllegalArgumentException if primaryColor is not a valid hex color
     */
    public Tenant execute(UpdateTenantBrandingCommand command) {
        Objects.requireNonNull(command, "Command must not be null");
        Objects.requireNonNull(command.tenantId(), "tenantId must not be null");

        if (command.primaryColor() != null && !command.primaryColor().matches(HEX_COLOR_REGEX)) {
            throw new IllegalArgumentException(
                    "primaryColor must be a valid hex color in #RRGGBB format, got: " + command.primaryColor());
        }

        Tenant tenant = tenantRepository.findById(command.tenantId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tenant with id '" + command.tenantId() + "' not found"));

        tenant.updateBranding(command.logoUrl(), command.primaryColor(), clock.now());
        return tenantRepository.save(tenant);
    }
}
