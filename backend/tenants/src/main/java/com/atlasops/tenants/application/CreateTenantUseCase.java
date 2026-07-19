package com.atlasops.tenants.application;

import com.atlasops.shared.domain.exceptions.DuplicateResourceException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import com.atlasops.tenants.domain.Tenant;
import com.atlasops.tenants.domain.TenantName;
import com.atlasops.tenants.domain.ports.TenantRepository;

/**
 * Use case for creating a new tenant. Validates name format and enforces case-insensitive
 * uniqueness of the tenant name across the platform.
 */
public class CreateTenantUseCase {

  private final TenantRepository tenantRepository;
  private final IdGenerator idGenerator;
  private final Clock clock;

  public CreateTenantUseCase(
      TenantRepository tenantRepository, IdGenerator idGenerator, Clock clock) {
    this.tenantRepository = tenantRepository;
    this.idGenerator = idGenerator;
    this.clock = clock;
  }

  /**
   * Creates a new tenant with the given name.
   *
   * @param name the tenant name (must be 3-100 chars, alphanumeric, hyphens, spaces)
   * @return the persisted Tenant
   * @throws IllegalArgumentException if name format is invalid
   * @throws DuplicateResourceException if a tenant with that name already exists
   */
  public Tenant execute(String name) {
    TenantName tenantName = new TenantName(name);

    if (tenantRepository.existsByNameIgnoreCase(tenantName.getValue())) {
      throw new DuplicateResourceException(
          "Tenant with name '" + tenantName.getValue() + "' already exists");
    }

    String id = idGenerator.generate();
    Tenant tenant = Tenant.create(id, tenantName, clock.now());

    return tenantRepository.save(tenant);
  }
}
