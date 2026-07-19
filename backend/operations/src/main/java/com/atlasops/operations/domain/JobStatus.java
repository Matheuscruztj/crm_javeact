package com.atlasops.operations.domain;

/** Lifecycle states of an asynchronous {@link Job}. */
public enum JobStatus {
  /** Job is queued and waiting to be picked up by a worker. */
  QUEUED,
  /** Job is actively being processed. */
  RUNNING,
  /** Job finished successfully. */
  COMPLETED,
  /** Job terminated with an error (eligible for retry). */
  FAILED,
  /** Job was cancelled by an operator before completion. */
  CANCELLED
}
