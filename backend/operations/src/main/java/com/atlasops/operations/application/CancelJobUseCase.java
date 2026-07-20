package com.atlasops.operations.application;

import com.atlasops.operations.domain.Job;
import com.atlasops.operations.domain.ports.JobRepository;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.ports.Clock;
import java.util.Objects;

/**
 * Use case for cancelling a queued or running job.
 *
 * <p>Validates: P0.F.3 — Operations Job UI (retry/cancel)
 */
public class CancelJobUseCase {

    private final JobRepository jobRepository;
    private final Clock clock;

    public CancelJobUseCase(JobRepository jobRepository, Clock clock) {
        this.jobRepository = Objects.requireNonNull(jobRepository);
        this.clock = Objects.requireNonNull(clock);
    }

    /**
     * Cancels a QUEUED or RUNNING job.
     *
     * @param jobId    the job to cancel
     * @param tenantId the tenant context
     * @return the cancelled job
     * @throws ResourceNotFoundException if the job is not found
     * @throws IllegalStateException     if the job cannot be cancelled
     */
    public Job execute(String jobId, String tenantId) {
        Objects.requireNonNull(jobId, "JobId must not be null");
        Objects.requireNonNull(tenantId, "TenantId must not be null");

        Job job = jobRepository.findById(jobId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Job '" + jobId + "' not found"));

        job.cancel(clock.now());

        return jobRepository.save(job);
    }
}
