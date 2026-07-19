package com.atlasops.tenants.domain.ports;

import com.atlasops.tenants.domain.Tenant;
import java.util.Optional;

/** Port defining persistence operations for the Tenant aggregate. */
public interface TenantRepository {

  /**
   * Finds a tenant by its identifier.
   *
   * @param id the tenant identifier
   * @return the tenant if found, empty otherwise
   */
  Optional<Tenant> findById(String id);

  /**
   * Checks whether a tenant with the given name already exists (case-insensitive comparison).
   *
   * @param name the tenant name to check
   * @return true if a tenant with that name exists, false otherwise
   */
  boolean existsByNameIgnoreCase(String name);

  /**
   * Persists a tenant (insert or update).
   *
   * @param tenant the tenant to persist
   * @return the persisted tenant
   */
  Tenant save(Tenant tenant);
}
