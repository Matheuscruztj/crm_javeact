package com.atlasops.requests.infrastructure;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for ServiceRequestJpaEntity persistence. All queries enforce tenant
 * isolation.
 */
@Repository
public interface SpringDataServiceRequestRepository
    extends JpaRepository<ServiceRequestJpaEntity, String> {

  Optional<ServiceRequestJpaEntity> findByIdAndTenantId(String id, String tenantId);

  @Query(
      "SELECT r FROM ServiceRequestJpaEntity r WHERE r.tenantId = :tenantId"
          + " AND (:status IS NULL OR r.status = :status)"
          + " AND (:priority IS NULL OR r.priority = :priority)"
          + " AND (:customerId IS NULL OR r.customerId = :customerId)"
          + " ORDER BY r.createdAt DESC")
  Page<ServiceRequestJpaEntity> findAllByTenantIdWithFilters(
      @Param("tenantId") String tenantId,
      @Param("status") String status,
      @Param("priority") String priority,
      @Param("customerId") String customerId,
      Pageable pageable);
}
