package com.atlasops.operations.application;

import com.atlasops.operations.domain.Job;
import com.atlasops.operations.domain.ports.JobRepository;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import java.util.Objects;

/**
 * Use case for retrying a failed job.
 *
 * <p>Validates: P0.F.3 — Operations Job UI (retry/cancel)
 */
public class RetryJobUseCase {

    private final JobRepository jobRepository;

    public RetryJobUseCase(JobRepository jobRepository) {
        this.jobRepository = Objects.requireNonNull(jobRepository);
    }

    /**
     * Retries a failed job by resetting it to QUEUED state.
     *
     * @param jobId    the job to retry
     * @param tenantId the tenant context
     * @return the updated job
     * @throws ResourceNotFoundException if the job is not found
     * @throws IllegalStateException     if the job is not in FAILED state
     */
    public Job execute(String jobId, String tenantId) {
        Objects.requireNonNull(jobId, "JobId must not be null");
        Objects.requireNonNull(tenantId, "TenantId must not be null");

        Job job = jobRepository.findById(jobId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Job '" + jobId + "' not found"));

        job.retry();

        return jobRepository.save(job);
    }
}
