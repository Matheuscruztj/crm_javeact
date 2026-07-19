package com.atlasops.tenants.presentation;

import com.atlasops.tenants.domain.Tenant;
import java.time.Instant;

/**
 * Response DTO representing a tenant.
 *
 * @param id the tenant identifier
 * @param name the tenant name
 * @param status the tenant status (ACTIVE or INACTIVE)
 * @param createdAt the creation timestamp
 * @param updatedAt the last update timestamp
 */
public record TenantResponse(
    String id, String name, String status, Instant createdAt, Instant updatedAt) {

  /**
   * Creates a TenantResponse from a Tenant domain object.
   *
   * @param tenant the domain tenant
   * @return the response DTO
   */
  public static TenantResponse from(Tenant tenant) {
    return new TenantResponse(
        tenant.getId(),
        tenant.getName().getValue(),
        tenant.getStatus().name(),
        tenant.getCreatedAt(),
        tenant.getUpdatedAt());
  }
}
