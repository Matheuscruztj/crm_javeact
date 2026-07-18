package com.atlasops.shared.harness;

import java.time.Duration;
import java.time.Instant;

/**
 * Represents the provisioned resources for an agent sandbox.
 *
 * <p>Resources follow the naming convention:
 *
 * <ul>
 *   <li>runId: {issue}-agent-{role}
 *   <li>branchName: sandbox/{runId}
 *   <li>databaseName: atlasops_{issue}
 *   <li>composeProject: atlasops_{issue}
 *   <li>bucketPrefix: {issue}/
 * </ul>
 *
 * @param runId unique run identifier
 * @param branchName Git branch name for the sandbox
 * @param databaseName database name for the sandbox
 * @param composeProject Docker Compose project name
 * @param bucketPrefix object storage bucket prefix
 * @param createdAt when the sandbox was created
 * @param expiresAt when the sandbox expires (createdAt + TTL)
 */
public record SandboxResources(
    String runId,
    String branchName,
    String databaseName,
    String composeProject,
    String bucketPrefix,
    Instant createdAt,
    Instant expiresAt) {

  /** Maximum time-to-live for a sandbox: 24 hours. */
  public static final Duration TTL = Duration.ofHours(24);

  /** Cleanup delay after task completion: 5 minutes. */
  public static final Duration CLEANUP_DELAY = Duration.ofMinutes(5);

  /**
   * Checks whether this sandbox has expired at the given point in time.
   *
   * @param now the current time
   * @return true if the sandbox has expired
   */
  public boolean isExpired(Instant now) {
    return now.isAfter(expiresAt) || now.equals(expiresAt);
  }

  /**
   * Creates a SandboxResources with TTL applied from the given creation time.
   *
   * @param runId unique run identifier
   * @param branchName Git branch name
   * @param databaseName database name
   * @param composeProject Docker Compose project name
   * @param bucketPrefix object storage bucket prefix
   * @param createdAt creation time
   * @return a new SandboxResources with expiresAt = createdAt + TTL
   */
  public static SandboxResources withTtl(
      String runId,
      String branchName,
      String databaseName,
      String composeProject,
      String bucketPrefix,
      Instant createdAt) {
    return new SandboxResources(
        runId,
        branchName,
        databaseName,
        composeProject,
        bucketPrefix,
        createdAt,
        createdAt.plus(TTL));
  }
}
