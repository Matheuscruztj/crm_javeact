package com.atlasops.tenants.presentation;

import com.atlasops.tenants.application.UpdateTenantBrandingCommand;
import com.atlasops.tenants.application.UpdateTenantBrandingUseCase;
import com.atlasops.tenants.domain.Tenant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for tenant branding management.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>PUT /api/v1/tenants/{id}/branding — update logo URL and primary color
 * </ul>
 *
 * <p>Validates: P2.12 — Tenant branding endpoint
 */
@RestController
@RequestMapping("/api/v1/tenants")
public class TenantBrandingController {

    private final UpdateTenantBrandingUseCase updateBrandingUseCase;

    public TenantBrandingController(UpdateTenantBrandingUseCase updateBrandingUseCase) {
        this.updateBrandingUseCase = updateBrandingUseCase;
    }

    /**
     * Updates the branding for the given tenant.
     *
     * @param id      the tenant identifier (from path)
     * @param request the branding update request body
     * @return 200 OK with the updated tenant representation
     */
    @PutMapping("/{id}/branding")
    public ResponseEntity<TenantResponse> updateBranding(
            @PathVariable String id,
            @RequestBody BrandingRequest request) {
        var command = new UpdateTenantBrandingCommand(id, request.logoUrl(), request.primaryColor());
        Tenant updated = updateBrandingUseCase.execute(command);
        return ResponseEntity.ok(TenantResponse.from(updated));
    }

    /**
     * Request body for branding update.
     *
     * @param logoUrl      URL to the tenant's logo (optional)
     * @param primaryColor hex color in #RRGGBB format (optional)
     */
    public record BrandingRequest(String logoUrl, String primaryColor) {}
}
