package com.atlasops.ai.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA repository for golden dataset examples. */
public interface SpringDataGoldenDatasetRepository
        extends JpaRepository<GoldenExampleJpaEntity, String> {

    List<GoldenExampleJpaEntity> findByTenantId(String tenantId);

    Optional<GoldenExampleJpaEntity> findByIdAndTenantId(String id, String tenantId);
}
