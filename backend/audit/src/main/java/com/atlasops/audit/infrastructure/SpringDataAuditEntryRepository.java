package com.atlasops.audit.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for AuditEntryJpaEntity. Only exposes save and query methods — no
 * delete operations. Uses JpaSpecificationExecutor for dynamic filtering with tenant isolation.
 */
@Repository
public interface SpringDataAuditEntryRepository
    extends JpaRepository<AuditEntryJpaEntity, String>,
        JpaSpecificationExecutor<AuditEntryJpaEntity> {

  Page<AuditEntryJpaEntity> findByTenantId(String tenantId, Pageable pageable);
}
