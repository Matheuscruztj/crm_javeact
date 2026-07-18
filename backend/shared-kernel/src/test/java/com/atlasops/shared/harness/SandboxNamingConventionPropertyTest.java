package com.atlasops.shared.harness;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import net.jqwik.api.*;

/**
 * Property-based tests for sandbox naming convention.
 *
 * <p><b>Validates: Requirements 9.2</b>
 *
 * <p>Property 14: For any combination of issue identifier and agent role (A1-A11), the sandbox
 * resources SHALL be named following the convention: {@code run_id={issue}-agent-{role}}, {@code
 * database=atlasops_{issue}}, {@code compose_project=atlasops_{issue}}, {@code
 * bucket_prefix={issue}/}.
 */
@Tag("Feature: monorepo-sdd-harness, Property 14: Sandbox Naming Convention")
class SandboxNamingConventionPropertyTest {

  // ─── Property: run_id follows pattern {issue}-agent-{role} ───────────────────

  @Property(tries = 100)
  void runId_shouldFollowPattern_issueAgentRole(
      @ForAll("issueIdentifiers") String issue, @ForAll("agentRoles") AgentRole role) {

    String runId = SandboxNamingConvention.runId(issue, role);

    assertThat(runId)
        .as(
            "run_id for issue='%s' and role='%s' should be '{issue}-agent-{role}'",
            issue, role.code())
        .isEqualTo(issue + "-agent-" + role.code());
  }

  // ─── Property: database follows pattern atlasops_{issue} ─────────────────────

  @Property(tries = 100)
  void databaseName_shouldFollowPattern_atlasopsIssue(@ForAll("issueIdentifiers") String issue) {

    String dbName = SandboxNamingConvention.databaseName(issue);

    String normalizedIssue = issue.replace("-", "_");
    assertThat(dbName)
        .as("database for issue='%s' should be 'atlasops_{issue}' (hyphens normalized)", issue)
        .isEqualTo("atlasops_" + normalizedIssue);
    assertThat(dbName)
        .as("database name should always start with 'atlasops_'")
        .startsWith("atlasops_");
  }

  // ─── Property: compose_project follows pattern atlasops_{issue} ──────────────

  @Property(tries = 100)
  void composeProject_shouldFollowPattern_atlasopsIssue(@ForAll("issueIdentifiers") String issue) {

    String composeProject = SandboxNamingConvention.composeProject(issue);

    String normalizedIssue = issue.replace("-", "_");
    assertThat(composeProject)
        .as(
            "compose_project for issue='%s' should be 'atlasops_{issue}' (hyphens normalized)",
            issue)
        .isEqualTo("atlasops_" + normalizedIssue);
    assertThat(composeProject)
        .as("compose_project should always start with 'atlasops_'")
        .startsWith("atlasops_");
  }

  // ─── Property: database and compose_project are always equal ─────────────────

  @Property(tries = 100)
  void databaseAndComposeProject_shouldAlwaysBeEqual(@ForAll("issueIdentifiers") String issue) {

    String dbName = SandboxNamingConvention.databaseName(issue);
    String composeProject = SandboxNamingConvention.composeProject(issue);

    assertThat(dbName)
        .as("database and compose_project for issue='%s' should have the same value", issue)
        .isEqualTo(composeProject);
  }

  // ─── Property: bucket_prefix follows pattern {issue}/ ────────────────────────

  @Property(tries = 100)
  void bucketPrefix_shouldFollowPattern_issueSlash(@ForAll("issueIdentifiers") String issue) {

    String bucketPrefix = SandboxNamingConvention.bucketPrefix(issue);

    assertThat(bucketPrefix)
        .as("bucket_prefix for issue='%s' should be '{issue}/'", issue)
        .isEqualTo(issue + "/");
    assertThat(bucketPrefix).as("bucket_prefix should always end with '/'").endsWith("/");
  }

  // ─── Property: createResources produces consistent naming across all fields ──

  @Property(tries = 100)
  void createResources_shouldProduceConsistentNaming(
      @ForAll("issueIdentifiers") String issue,
      @ForAll("agentRoles") AgentRole role,
      @ForAll("creationInstants") Instant createdAt) {

    SandboxResources resources = SandboxNamingConvention.createResources(issue, role, createdAt);

    // Verify run_id convention
    assertThat(resources.runId())
        .as("run_id should follow {issue}-agent-{role}")
        .isEqualTo(issue + "-agent-" + role.code());

    // Verify branch derives from run_id
    assertThat(resources.branchName())
        .as("branchName should be sandbox/{run_id}")
        .isEqualTo("sandbox/" + resources.runId());

    // Verify database convention
    String normalizedIssue = issue.replace("-", "_");
    assertThat(resources.databaseName())
        .as("databaseName should be atlasops_{issue}")
        .isEqualTo("atlasops_" + normalizedIssue);

    // Verify compose_project convention
    assertThat(resources.composeProject())
        .as("composeProject should be atlasops_{issue}")
        .isEqualTo("atlasops_" + normalizedIssue);

    // Verify bucket_prefix convention
    assertThat(resources.bucketPrefix())
        .as("bucketPrefix should be {issue}/")
        .isEqualTo(issue + "/");
  }

  // ─── Property: All roles (A1-A11) produce valid naming ───────────────────────

  @Property(tries = 100)
  void allRoles_shouldProduceRunIdContainingRoleCode(
      @ForAll("issueIdentifiers") String issue, @ForAll("agentRoles") AgentRole role) {

    String runId = SandboxNamingConvention.runId(issue, role);

    // run_id must contain the role code (A1 through A11)
    assertThat(runId)
        .as("run_id should contain the role code '%s'", role.code())
        .contains("-agent-" + role.code());

    // run_id must start with the issue
    assertThat(runId)
        .as("run_id should start with the issue identifier '%s'", issue)
        .startsWith(issue + "-agent-");
  }

  // ─── Generators ──────────────────────────────────────────────────────────────

  @Provide
  Arbitrary<String> issueIdentifiers() {
    // Generate realistic Jira-style issue identifiers: PROJECT-NUMBER
    Arbitrary<String> projectKey =
        Arbitraries.strings().withCharRange('A', 'Z').ofMinLength(2).ofMaxLength(6);

    Arbitrary<Integer> issueNumber = Arbitraries.integers().between(1, 9999);

    return Combinators.combine(projectKey, issueNumber).as((key, number) -> key + "-" + number);
  }

  @Provide
  Arbitrary<AgentRole> agentRoles() {
    return Arbitraries.of(AgentRole.values());
  }

  @Provide
  Arbitrary<Instant> creationInstants() {
    // Generate instants within a reasonable range (year 2024)
    long start = Instant.parse("2024-01-01T00:00:00Z").getEpochSecond();
    long end = Instant.parse("2025-01-01T00:00:00Z").getEpochSecond();
    return Arbitraries.longs().between(start, end).map(Instant::ofEpochSecond);
  }
}
