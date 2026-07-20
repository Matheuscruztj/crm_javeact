package com.atlasops.ai.infrastructure;

import com.atlasops.ai.domain.GoldenExample;
import com.atlasops.ai.domain.ports.GoldenDatasetRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * JPA adapter implementing GoldenDatasetRepository.
 * Persists golden examples in the golden_dataset table.
 *
 * <p>Validates: P0.G.2 — Golden Dataset + AI Evaluation Framework
 */
@Component
public class JpaGoldenDatasetRepositoryAdapter implements GoldenDatasetRepository {

    private final SpringDataGoldenDatasetRepository springRepo;

    public JpaGoldenDatasetRepositoryAdapter(SpringDataGoldenDatasetRepository springRepo) {
        this.springRepo = springRepo;
    }

    @Override
    public GoldenExample save(GoldenExample example) {
        springRepo.save(toEntity(example));
        return example;
    }

    @Override
    public Optional<GoldenExample> findById(String id, String tenantId) {
        return springRepo.findByIdAndTenantId(id, tenantId).map(this::toDomain);
    }

    @Override
    public List<GoldenExample> findByTenantId(String tenantId) {
        return springRepo.findByTenantId(tenantId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void deleteById(String id, String tenantId) {
        springRepo.findByIdAndTenantId(id, tenantId).ifPresent(springRepo::delete);
    }

    private GoldenExampleJpaEntity toEntity(GoldenExample g) {
        return new GoldenExampleJpaEntity(
                g.getId(), g.getTenantId(), g.getQuery(),
                g.getExpectedAnswer(), g.getCategory(), g.getCreatedBy(), g.getCreatedAt());
    }

    private GoldenExample toDomain(GoldenExampleJpaEntity e) {
        return GoldenExample.create(
                e.getId(), e.getTenantId(), e.getQuery(),
                e.getExpectedAnswer(), e.getCategory(), e.getCreatedBy(), e.getCreatedAt());
    }
}
