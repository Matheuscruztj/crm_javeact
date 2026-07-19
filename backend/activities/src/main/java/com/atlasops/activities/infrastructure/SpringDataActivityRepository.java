package com.atlasops.activities.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for ActivityJpaEntity persistence. All queries enforce tenant
 * isolation.
 */
@Repository
public interface SpringDataActivityRepository extends JpaRepository<ActivityJpaEntity, String> {

  boolean existsByEventId(String eventId);

  Optional<ActivityJpaEntity> findByIdAndTenantId(String id, String tenantId);

  Page<ActivityJpaEntity> findByEntityTypeAndEntityIdAndTenantIdOrderByTimestampDesc(
      String entityType, String entityId, String tenantId, Pageable pageable);

  Page<ActivityJpaEntity> findByTenantIdOrderByTimestampDesc(String tenantId, Pageable pageable);

  @Query(
      "SELECT a FROM ActivityJpaEntity a WHERE a.tenantId = :tenantId "
          + "AND a.entityId IN :entityIds ORDER BY a.timestamp DESC")
  Page<ActivityJpaEntity> findByTenantIdAndEntityIdInOrderByTimestampDesc(
      @Param("tenantId") String tenantId,
      @Param("entityIds") List<String> entityIds,
      Pageable pageable);
}
