package com.atlasops.tenants.application;

/**
 * Command for updating tenant branding settings.
 *
 * @param tenantId     the tenant to update
 * @param logoUrl      URL to the tenant's logo (may be null to clear)
 * @param primaryColor hex color in #RRGGBB format (may be null to clear)
 */
public record UpdateTenantBrandingCommand(String tenantId, String logoUrl, String primaryColor) {}
