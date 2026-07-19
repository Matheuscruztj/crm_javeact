package com.atlasops.operations.domain.ports;

import com.atlasops.operations.domain.Job;
import com.atlasops.operations.domain.JobStatus;
import java.util.List;
import java.util.Optional;

/**
 * Port for persisting and querying {@link Job} aggregates.
 */
public interface JobRepository {

  Job save(Job job);

  Optional<Job> findById(String id, String tenantId);

  List<Job> findByTenantId(String tenantId, List<JobStatus> statuses, int page, int size);

  long countByTenantId(String tenantId, List<JobStatus> statuses);
}
