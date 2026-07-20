package com.atlasops.operations.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for job entities.
 */
public interface SpringDataJobRepository extends JpaRepository<JobJpaEntity, String> {

    Optional<JobJpaEntity> findByIdAndTenantId(String id, String tenantId);

    @Query("SELECT j FROM JobJpaEntity j WHERE j.tenantId = :tenantId "
            + "AND (:statuses IS NULL OR j.status IN :statuses) "
            + "ORDER BY j.createdAt DESC")
    List<JobJpaEntity> findByTenantIdAndStatusIn(
            @Param("tenantId") String tenantId,
            @Param("statuses") List<String> statuses,
            Pageable pageable);

    @Query("SELECT COUNT(j) FROM JobJpaEntity j WHERE j.tenantId = :tenantId "
            + "AND (:statuses IS NULL OR j.status IN :statuses)")
    long countByTenantIdAndStatusIn(
            @Param("tenantId") String tenantId,
            @Param("statuses") List<String> statuses);
}
