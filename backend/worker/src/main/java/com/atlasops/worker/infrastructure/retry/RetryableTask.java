package com.atlasops.worker.infrastructure.retry;

/** Functional interface for tasks that can be retried. */
@FunctionalInterface
public interface RetryableTask {

  /**
   * Executes the task.
   *
   * @throws Exception if the task fails
   */
  void execute() throws Exception;
}
