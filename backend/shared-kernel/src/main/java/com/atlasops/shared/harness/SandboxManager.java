package com.atlasops.shared.harness;

/**
 * Interface for managing agent sandboxes.
 *
 * <p>Implementations handle provisioning of isolated resources (branch, DB, compose, bucket),
 * cleanup after task completion, and access validation for cross-sandbox isolation.
 */
public interface SandboxManager {

  /**
   * Provisions a new sandbox with isolated resources for the given task context.
   *
   * <p>Creates:
   *
   * <ul>
   *   <li>Git branch: sandbox/{run_id}
   *   <li>Database: atlasops_{issue}
   *   <li>Docker Compose project: atlasops_{issue}
   *   <li>Bucket prefix: {issue}/
   * </ul>
   *
   * @param taskContext the task context with issue and role information
   * @return the provisioned sandbox resources
   */
  SandboxResources provision(TaskContext taskContext);

  /**
   * Cleans up all resources associated with the given run ID.
   *
   * <p>Should be triggered within 5 minutes of task completion or when the sandbox TTL (24h)
   * expires.
   *
   * @param runId the run identifier of the sandbox to clean
   */
  void cleanup(String runId);

  /**
   * Validates that the given run ID has access to the specified resource.
   *
   * <p>Ensures cross-sandbox isolation by verifying that operations use exclusively the namespace
   * of the requesting sandbox's run_id.
   *
   * @param runId the run identifier requesting access
   * @param resource the resource being accessed
   * @throws SecurityException if access is denied (cross-sandbox violation)
   */
  void validateAccess(String runId, String resource);
}
