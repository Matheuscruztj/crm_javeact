package com.atlasops.operations.domain;

import com.atlasops.shared.domain.Entity;
import java.time.Instant;
import java.util.Objects;

/**
 * Entity representing an asynchronous job tracked by the operations module.
 *
 * <p>A job can represent document processing, imports, notification batches, or any
 * long-running background task.
 */
public final class Job extends Entity<String> {

  private static final int MAX_TYPE_LENGTH = 100;
  private static final int MAX_MESSAGE_LENGTH = 2000;

  private final String type;
  private JobStatus status;
  private final String tenantId;
  private final Instant createdAt;
  private Instant startedAt;
  private Instant completedAt;
  private Integer progressPercent;
  private String errorMessage;
  private String referenceId;

  private Job(
      String id,
      String type,
      JobStatus status,
      String tenantId,
      Instant createdAt,
      Instant startedAt,
      Instant completedAt,
      Integer progressPercent,
      String errorMessage,
      String referenceId) {
    super(id);
    this.type = validateType(type);
    this.status = Objects.requireNonNull(status, "Status must not be null");
    this.tenantId = Objects.requireNonNull(tenantId, "TenantId must not be null");
    this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt must not be null");
    this.startedAt = startedAt;
    this.completedAt = completedAt;
    this.progressPercent = progressPercent;
    this.errorMessage = errorMessage;
    this.referenceId = referenceId;
  }

  /** Creates a new job in QUEUED state. */
  public static Job create(String id, String type, String tenantId, String referenceId, Instant now) {
    return new Job(id, type, JobStatus.QUEUED, tenantId, now, null, null, 0, null, referenceId);
  }

  /** Reconstitutes a job from persisted state. */
  public static Job reconstitute(
      String id,
      String type,
      JobStatus status,
      String tenantId,
      Instant createdAt,
      Instant startedAt,
      Instant completedAt,
      Integer progressPercent,
      String errorMessage,
      String referenceId) {
    return new Job(
        id, type, status, tenantId, createdAt, startedAt, completedAt,
        progressPercent, errorMessage, referenceId);
  }

  /** Transitions the job to RUNNING. */
  public void start(Instant now) {
    if (this.status != JobStatus.QUEUED) {
      throw new IllegalStateException("Job must be QUEUED to start, current: " + this.status);
    }
    this.status = JobStatus.RUNNING;
    this.startedAt = now;
  }

  /** Updates the progress percentage (0–100). */
  public void updateProgress(int percent) {
    if (percent < 0 || percent > 100) {
      throw new IllegalArgumentException("Progress must be between 0 and 100, got: " + percent);
    }
    this.progressPercent = percent;
  }

  /** Marks the job as successfully completed. */
  public void complete(Instant now) {
    this.status = JobStatus.COMPLETED;
    this.completedAt = now;
    this.progressPercent = 100;
  }

  /** Marks the job as failed with an error message. */
  public void fail(String errorMessage, Instant now) {
    this.status = JobStatus.FAILED;
    this.completedAt = now;
    this.errorMessage = errorMessage != null
        && errorMessage.length() > MAX_MESSAGE_LENGTH
        ? errorMessage.substring(0, MAX_MESSAGE_LENGTH)
        : errorMessage;
  }

  /** Resets a FAILED job back to QUEUED for retry. */
  public void retry() {
    if (this.status != JobStatus.FAILED) {
      throw new IllegalStateException("Only FAILED jobs can be retried, current: " + this.status);
    }
    this.status = JobStatus.QUEUED;
    this.startedAt = null;
    this.completedAt = null;
    this.progressPercent = 0;
    this.errorMessage = null;
  }

  /** Cancels a QUEUED or RUNNING job. */
  public void cancel(Instant now) {
    if (this.status != JobStatus.QUEUED && this.status != JobStatus.RUNNING) {
      throw new IllegalStateException(
          "Only QUEUED or RUNNING jobs can be cancelled, current: " + this.status);
    }
    this.status = JobStatus.CANCELLED;
    this.completedAt = now;
  }

  private static String validateType(String type) {
    if (type == null || type.isBlank()) {
      throw new IllegalArgumentException("Job type must not be blank");
    }
    if (type.length() > MAX_TYPE_LENGTH) {
      throw new IllegalArgumentException(
          "Job type must not exceed " + MAX_TYPE_LENGTH + " chars, got: " + type.length());
    }
    return type;
  }

  public String getType() { return type; }
  public JobStatus getStatus() { return status; }
  public String getTenantId() { return tenantId; }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getStartedAt() { return startedAt; }
  public Instant getCompletedAt() { return completedAt; }
  public Integer getProgressPercent() { return progressPercent; }
  public String getErrorMessage() { return errorMessage; }
  public String getReferenceId() { return referenceId; }
}
