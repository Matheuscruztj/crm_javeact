package com.atlasops.operations.application;

import com.atlasops.operations.domain.Job;
import com.atlasops.operations.domain.ports.JobRepository;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import java.util.Objects;

/**
 * Use case for retrieving job details by identifier.
 */
public class GetJobDetailsUseCase {

  private final JobRepository jobRepository;

  public GetJobDetailsUseCase(JobRepository jobRepository) {
    this.jobRepository = Objects.requireNonNull(jobRepository);
  }

  /**
   * Retrieves a job by its identifier within the specified tenant.
   *
   * @param jobId the job identifier
   * @param tenantId the tenant identifier
   * @return the job
   * @throws ResourceNotFoundException if the job is not found
   */
  public Job execute(String jobId, String tenantId) {
    Objects.requireNonNull(jobId, "Job id must not be null");
    Objects.requireNonNull(tenantId, "Tenant id must not be null");

    return jobRepository
        .findById(jobId, tenantId)
        .orElseThrow(() -> new ResourceNotFoundException("Job with id '" + jobId + "' not found"));
  }
}
