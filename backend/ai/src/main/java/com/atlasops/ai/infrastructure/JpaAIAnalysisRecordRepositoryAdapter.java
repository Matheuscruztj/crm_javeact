package com.atlasops.ai.infrastructure;

import com.atlasops.ai.domain.AIAnalysisRecord;
import com.atlasops.ai.domain.ports.AIAnalysisRecordRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * JPA adapter implementing AIAnalysisRecordRepository.
 * Persists AI analysis records in the ai_analysis_records table.
 *
 * <p>Validates: Requirement 4.8
 */
@Component
public class JpaAIAnalysisRecordRepositoryAdapter implements AIAnalysisRecordRepository {

    private final SpringDataAIAnalysisRecordRepository springRepo;

    public JpaAIAnalysisRecordRepositoryAdapter(SpringDataAIAnalysisRecordRepository springRepo) {
        this.springRepo = springRepo;
    }

    @Override
    public AIAnalysisRecord save(AIAnalysisRecord record) {
        AIAnalysisRecordJpaEntity entity = toEntity(record);
        springRepo.save(entity);
        return record;
    }

    @Override
    public Optional<AIAnalysisRecord> findById(String id) {
        return springRepo.findById(id).map(this::toDomain);
    }

    @Override
    public List<AIAnalysisRecord> findByTenantId(String tenantId) {
        return springRepo.findByTenantId(tenantId).stream()
                .map(this::toDomain)
                .toList();
    }

    private AIAnalysisRecordJpaEntity toEntity(AIAnalysisRecord r) {
        return new AIAnalysisRecordJpaEntity(
                r.getId(), r.getTenantId(), r.getModel(), r.getPromptVersion(),
                r.getInputHash(), r.getDurationMs(), r.getConfidenceScore(),
                r.isFallback(), r.getResult(),
                r.getChunksUsed().toArray(String[]::new), r.getCreatedAt());
    }

    private AIAnalysisRecord toDomain(AIAnalysisRecordJpaEntity e) {
        return new AIAnalysisRecord(
                e.getId(), e.getTenantId(), e.getModel(), e.getPromptVersion(),
                e.getInputHash(), e.getDurationMs(), e.getConfidenceScore(),
                e.isFallback(), e.getResult(),
                e.getChunksUsed() != null ? Arrays.asList(e.getChunksUsed()) : List.of(),
                e.getCreatedAt());
    }
}
