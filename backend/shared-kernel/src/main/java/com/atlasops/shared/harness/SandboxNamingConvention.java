package com.atlasops.shared.harness;

import java.time.Instant;
import java.util.Objects;

/**
 * Utility that generates sandbox resource names following the naming convention:
 *
 * <ul>
 *   <li>run_id: {issue}-agent-{role}
 *   <li>branch: sandbox/{run_id}
 *   <li>database: atlasops_{issue}
 *   <li>compose_project: atlasops_{issue}
 *   <li>bucket_prefix: {issue}/
 * </ul>
 *
 * <p>The issue identifier is normalized by replacing hyphens with underscores for database and
 * compose project names (to avoid invalid characters).
 */
public final class SandboxNamingConvention {

  private SandboxNamingConvention() {
    // Utility class — no instantiation
  }

  /**
   * Generates the run ID for a given issue and agent role.
   *
   * @param issue the issue identifier (e.g., "ATLAS-42")
   * @param role the agent role
   * @return the run ID in format "{issue}-agent-{role}"
   */
  public static String runId(String issue, AgentRole role) {
    Objects.requireNonNull(issue, "issue must not be null");
    Objects.requireNonNull(role, "role must not be null");
    return issue + "-agent-" + role.code();
  }

  /**
   * Generates the branch name for a sandbox.
   *
   * @param runId the run identifier
   * @return the branch name in format "sandbox/{runId}"
   */
  public static String branchName(String runId) {
    Objects.requireNonNull(runId, "runId must not be null");
    return "sandbox/" + runId;
  }

  /**
   * Generates the database name for a given issue. Hyphens in the issue are replaced with
   * underscores.
   *
   * @param issue the issue identifier
   * @return the database name in format "atlasops_{issue}"
   */
  public static String databaseName(String issue) {
    Objects.requireNonNull(issue, "issue must not be null");
    return "atlasops_" + normalizeForIdentifier(issue);
  }

  /**
   * Generates the Docker Compose project name for a given issue. Hyphens in the issue are replaced
   * with underscores.
   *
   * @param issue the issue identifier
   * @return the compose project name in format "atlasops_{issue}"
   */
  public static String composeProject(String issue) {
    Objects.requireNonNull(issue, "issue must not be null");
    return "atlasops_" + normalizeForIdentifier(issue);
  }

  /**
   * Generates the bucket prefix for a given issue.
   *
   * @param issue the issue identifier
   * @return the bucket prefix in format "{issue}/"
   */
  public static String bucketPrefix(String issue) {
    Objects.requireNonNull(issue, "issue must not be null");
    return issue + "/";
  }

  /**
   * Creates a complete {@link SandboxResources} using the naming convention.
   *
   * @param issue the issue identifier
   * @param role the agent role
   * @param createdAt the creation time
   * @return fully named SandboxResources with TTL applied
   */
  public static SandboxResources createResources(String issue, AgentRole role, Instant createdAt) {
    Objects.requireNonNull(issue, "issue must not be null");
    Objects.requireNonNull(role, "role must not be null");
    Objects.requireNonNull(createdAt, "createdAt must not be null");

    String runId = runId(issue, role);
    return SandboxResources.withTtl(
        runId,
        branchName(runId),
        databaseName(issue),
        composeProject(issue),
        bucketPrefix(issue),
        createdAt);
  }

  /**
   * Normalizes an issue identifier for use in database/compose names. Replaces hyphens with
   * underscores.
   */
  private static String normalizeForIdentifier(String issue) {
    return issue.replace("-", "_");
  }
}
