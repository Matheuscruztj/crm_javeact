package com.atlasops.ai.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA repository for AI analysis records. */
public interface SpringDataAIAnalysisRecordRepository
        extends JpaRepository<AIAnalysisRecordJpaEntity, String> {

    List<AIAnalysisRecordJpaEntity> findByTenantId(String tenantId);
}
