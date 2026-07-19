package com.atlasops.documents.infrastructure;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for DocumentJpaEntity persistence. All queries enforce tenant
 * isolation via tenantId parameter.
 */
@Repository
public interface SpringDataDocumentRepository extends JpaRepository<DocumentJpaEntity, String> {

  Optional<DocumentJpaEntity> findByIdAndTenantId(String id, String tenantId);

  Page<DocumentJpaEntity> findByTenantIdAndStatus(
      String tenantId, String status, Pageable pageable);

  Page<DocumentJpaEntity> findByTenantId(String tenantId, Pageable pageable);

  Page<DocumentJpaEntity> findByRequestIdAndTenantId(
      String requestId, String tenantId, Pageable pageable);
}
