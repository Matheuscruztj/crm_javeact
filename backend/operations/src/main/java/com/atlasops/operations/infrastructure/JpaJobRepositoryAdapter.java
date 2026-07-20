package com.atlasops.operations.infrastructure;

import com.atlasops.operations.domain.Job;
import com.atlasops.operations.domain.JobStatus;
import com.atlasops.operations.domain.ports.JobRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

/**
 * JPA-based adapter implementing {@link JobRepository}.
 * Validates: P0.F.3, P0.I.1 — Operations module
 */
@Component
public class JpaJobRepositoryAdapter implements JobRepository {

    private final SpringDataJobRepository springDataRepository;

    public JpaJobRepositoryAdapter(SpringDataJobRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Job save(Job job) {
        JobJpaEntity entity = toEntity(job);
        springDataRepository.save(entity);
        return job;
    }

    @Override
    public Optional<Job> findById(String id, String tenantId) {
        return springDataRepository.findByIdAndTenantId(id, tenantId).map(this::toDomain);
    }

    @Override
    public List<Job> findByTenantId(String tenantId, List<JobStatus> statuses, int page, int size) {
        List<String> statusStrings = statuses.isEmpty()
                ? null
                : statuses.stream().map(JobStatus::name).toList();
        PageRequest pageable = PageRequest.of(page, size);
        return springDataRepository
                .findByTenantIdAndStatusIn(tenantId, statusStrings, pageable)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public long countByTenantId(String tenantId, List<JobStatus> statuses) {
        List<String> statusStrings = statuses.isEmpty()
                ? null
                : statuses.stream().map(JobStatus::name).toList();
        return springDataRepository.countByTenantIdAndStatusIn(tenantId, statusStrings);
    }

    private Job toDomain(JobJpaEntity e) {
        return Job.reconstitute(
                e.getId(), e.getType(), JobStatus.valueOf(e.getStatus()),
                e.getTenantId(), e.getCreatedAt(), e.getStartedAt(),
                e.getCompletedAt(), e.getProgressPercent(),
                e.getErrorMessage(), e.getReferenceId());
    }

    private JobJpaEntity toEntity(Job j) {
        return new JobJpaEntity(
                j.getId(), j.getType(), j.getStatus().name(),
                j.getTenantId(), j.getCreatedAt(), j.getStartedAt(),
                j.getCompletedAt(), j.getProgressPercent(),
                j.getErrorMessage(), j.getReferenceId());
    }
}
