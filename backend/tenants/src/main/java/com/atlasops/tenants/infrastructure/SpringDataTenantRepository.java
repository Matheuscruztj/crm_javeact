package com.atlasops.tenants.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository for TenantJpaEntity persistence. */
@Repository
public interface SpringDataTenantRepository extends JpaRepository<TenantJpaEntity, String> {

  /**
   * Checks whether a tenant with the given name exists (case-insensitive).
   *
   * @param name the tenant name to check
   * @return true if a tenant with that name exists
   */
  boolean existsByNameIgnoreCase(String name);
}
