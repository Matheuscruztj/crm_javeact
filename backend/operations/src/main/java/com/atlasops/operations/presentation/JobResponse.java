package com.atlasops.operations.presentation;

import com.atlasops.operations.domain.Job;
import java.time.Instant;

/**
 * Response DTO for a {@link Job}.
 */
public record JobResponse(
    String id,
    String type,
    String status,
    String tenantId,
    Instant createdAt,
    Instant startedAt,
    Instant completedAt,
    Integer progressPercent,
    String errorMessage,
    String referenceId) {

  public static JobResponse from(Job job) {
    return new JobResponse(
        job.getId(),
        job.getType(),
        job.getStatus().name(),
        job.getTenantId(),
        job.getCreatedAt(),
        job.getStartedAt(),
        job.getCompletedAt(),
        job.getProgressPercent(),
        job.getErrorMessage(),
        job.getReferenceId());
  }
}
