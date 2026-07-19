package com.atlasops.approvals.infrastructure;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for ApprovalJpaEntity persistence. All queries enforce tenant
 * isolation via tenantId parameter.
 */
@Repository
public interface SpringDataApprovalRepository extends JpaRepository<ApprovalJpaEntity, String> {

  Optional<ApprovalJpaEntity> findByIdAndTenantId(String id, String tenantId);

  Optional<ApprovalJpaEntity> findByDocumentIdAndTenantId(String documentId, String tenantId);

  Page<ApprovalJpaEntity> findByTenantIdAndStatus(
      String tenantId, String status, Pageable pageable);

  Page<ApprovalJpaEntity> findByTenantId(String tenantId, Pageable pageable);
}
