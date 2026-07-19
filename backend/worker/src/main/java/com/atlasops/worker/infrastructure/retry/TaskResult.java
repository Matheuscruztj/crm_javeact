package com.atlasops.worker.infrastructure.retry;

/** Represents the result of a task execution attempt. */
public sealed interface TaskResult {

  /** Indicates successful task completion. */
  record Success() implements TaskResult {}

  /**
   * Indicates task failure with an error.
   *
   * @param error the exception that caused the failure
   * @param attempt the attempt number (1-based)
   * @param durationMs the execution duration in milliseconds
   */
  record Failure(Throwable error, int attempt, long durationMs) implements TaskResult {}

  /**
   * Indicates the task was moved to DLQ after exhausting all retries.
   *
   * @param originalError the last error before moving to DLQ
   * @param totalAttempts total number of attempts made
   */
  record MovedToDlq(Throwable originalError, int totalAttempts) implements TaskResult {}
}
