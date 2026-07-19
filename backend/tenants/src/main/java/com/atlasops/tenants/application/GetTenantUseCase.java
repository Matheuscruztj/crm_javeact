package com.atlasops.tenants.application;

import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.tenants.domain.Tenant;
import com.atlasops.tenants.domain.ports.TenantRepository;

/** Use case for retrieving a tenant by identifier. */
public class GetTenantUseCase {

  private final TenantRepository tenantRepository;

  public GetTenantUseCase(TenantRepository tenantRepository) {
    this.tenantRepository = tenantRepository;
  }

  /**
   * Retrieves the tenant with the given identifier.
   *
   * @param tenantId the tenant identifier
   * @return the Tenant
   * @throws ResourceNotFoundException if no tenant with the given ID exists
   */
  public Tenant execute(String tenantId) {
    return tenantRepository
        .findById(tenantId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Tenant with id '" + tenantId + "' not found"));
  }
}
