package com.atlasops.tenants.application;

import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.tenants.domain.Tenant;
import com.atlasops.tenants.domain.ports.TenantRepository;

/** Use case for deactivating an existing tenant. Sets the tenant status to INACTIVE. */
public class DeactivateTenantUseCase {

  private final TenantRepository tenantRepository;
  private final Clock clock;

  public DeactivateTenantUseCase(TenantRepository tenantRepository, Clock clock) {
    this.tenantRepository = tenantRepository;
    this.clock = clock;
  }

  /**
   * Deactivates the tenant with the given identifier.
   *
   * @param tenantId the tenant identifier
   * @return the deactivated Tenant
   * @throws ResourceNotFoundException if no tenant with the given ID exists
   */
  public Tenant execute(String tenantId) {
    Tenant tenant =
        tenantRepository
            .findById(tenantId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Tenant with id '" + tenantId + "' not found"));

    tenant.deactivate(clock.now());

    return tenantRepository.save(tenant);
  }
}
