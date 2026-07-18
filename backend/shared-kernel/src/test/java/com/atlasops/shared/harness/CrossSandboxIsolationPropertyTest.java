package com.atlasops.shared.harness;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import net.jqwik.api.*;

/**
 * Property-based tests for cross-sandbox isolation.
 *
 * <p><b>Validates: Requirements 9.4</b>
 *
 * <p>Property 15: For any two sandboxes active simultaneously, an operation from sandbox A
 * attempting to access resources (database, bucket, branch) belonging to sandbox B SHALL be
 * rejected, with the system validating that every read/write uses exclusively the namespace of the
 * requesting sandbox's run_id.
 */
@Tag("Feature: monorepo-sdd-harness, Property 15: Cross-Sandbox Isolation")
class CrossSandboxIsolationPropertyTest {

  private final CrossSandboxIsolation isolation = new CrossSandboxIsolation();

  // ─── Property: Sandbox A accessing Sandbox B's database is rejected ──────────

  @Property(tries = 100)
  void sandboxA_shouldBeRejected_whenAccessingSandboxBDatabase(
      @ForAll("issueIdentifiers") String issueA,
      @ForAll("differentIssueIdentifiers") String issueB,
      @ForAll("agentRoles") AgentRole roleA,
      @ForAll("agentRoles") AgentRole roleB) {

    Assume.that(!issueA.equalsIgnoreCase(issueB));

    String runIdA = SandboxNamingConvention.runId(issueA, roleA);
    String databaseB = SandboxNamingConvention.databaseName(issueB);

    assertThatThrownBy(() -> isolation.validateAccess(runIdA, databaseB))
        .as(
            "Sandbox A (runId='%s') accessing sandbox B's database ('%s') should be rejected",
            runIdA, databaseB)
        .isInstanceOf(SecurityException.class)
        .hasMessageContaining("Cross-sandbox violation");
  }

  // ─── Property: Sandbox A accessing Sandbox B's bucket is rejected ────────────

  @Property(tries = 100)
  void sandboxA_shouldBeRejected_whenAccessingSandboxBBucket(
      @ForAll("issueIdentifiers") String issueA,
      @ForAll("differentIssueIdentifiers") String issueB,
      @ForAll("agentRoles") AgentRole roleA,
      @ForAll("agentRoles") AgentRole roleB) {

    Assume.that(!issueA.equalsIgnoreCase(issueB));

    String runIdA = SandboxNamingConvention.runId(issueA, roleA);
    String bucketB = SandboxNamingConvention.bucketPrefix(issueB);

    assertThatThrownBy(() -> isolation.validateAccess(runIdA, bucketB))
        .as(
            "Sandbox A (runId='%s') accessing sandbox B's bucket ('%s') should be rejected",
            runIdA, bucketB)
        .isInstanceOf(SecurityException.class)
        .hasMessageContaining("Cross-sandbox violation");
  }

  // ─── Property: Sandbox A accessing Sandbox B's branch is rejected ────────────

  @Property(tries = 100)
  void sandboxA_shouldBeRejected_whenAccessingSandboxBBranch(
      @ForAll("issueIdentifiers") String issueA,
      @ForAll("differentIssueIdentifiers") String issueB,
      @ForAll("agentRoles") AgentRole roleA,
      @ForAll("agentRoles") AgentRole roleB) {

    Assume.that(!issueA.equalsIgnoreCase(issueB));

    String runIdA = SandboxNamingConvention.runId(issueA, roleA);
    // Branch is sandbox/{issueB}-agent-{roleB}
    String branchB =
        SandboxNamingConvention.branchName(SandboxNamingConvention.runId(issueB, roleB));

    assertThatThrownBy(() -> isolation.validateAccess(runIdA, branchB))
        .as(
            "Sandbox A (runId='%s') accessing sandbox B's branch ('%s') should be rejected",
            runIdA, branchB)
        .isInstanceOf(SecurityException.class)
        .hasMessageContaining("Cross-sandbox violation");
  }

  // ─── Property: Sandbox accessing its own database is allowed ─────────────────

  @Property(tries = 100)
  void sandbox_shouldBeAllowed_whenAccessingOwnDatabase(
      @ForAll("issueIdentifiers") String issue, @ForAll("agentRoles") AgentRole role) {

    String runId = SandboxNamingConvention.runId(issue, role);
    String ownDatabase = SandboxNamingConvention.databaseName(issue);

    assertThatNoException()
        .as(
            "Sandbox (runId='%s') should be allowed to access its own database ('%s')",
            runId, ownDatabase)
        .isThrownBy(() -> isolation.validateAccess(runId, ownDatabase));
  }

  // ─── Property: Sandbox accessing its own bucket is allowed ───────────────────

  @Property(tries = 100)
  void sandbox_shouldBeAllowed_whenAccessingOwnBucket(
      @ForAll("issueIdentifiers") String issue, @ForAll("agentRoles") AgentRole role) {

    String runId = SandboxNamingConvention.runId(issue, role);
    String ownBucket = SandboxNamingConvention.bucketPrefix(issue);

    assertThatNoException()
        .as(
            "Sandbox (runId='%s') should be allowed to access its own bucket ('%s')",
            runId, ownBucket)
        .isThrownBy(() -> isolation.validateAccess(runId, ownBucket));
  }

  // ─── Property: Sandbox accessing its own branch is allowed ───────────────────

  @Property(tries = 100)
  void sandbox_shouldBeAllowed_whenAccessingOwnBranch(
      @ForAll("issueIdentifiers") String issue, @ForAll("agentRoles") AgentRole role) {

    String runId = SandboxNamingConvention.runId(issue, role);
    String ownBranch = SandboxNamingConvention.branchName(runId);

    assertThatNoException()
        .as(
            "Sandbox (runId='%s') should be allowed to access its own branch ('%s')",
            runId, ownBranch)
        .isThrownBy(() -> isolation.validateAccess(runId, ownBranch));
  }

  // ─── Property: Isolation holds for all resource types simultaneously ─────────

  @Property(tries = 100)
  void isolation_shouldHoldForAllResourceTypes_whenTwoSandboxesAreActive(
      @ForAll("issueIdentifiers") String issueA,
      @ForAll("differentIssueIdentifiers") String issueB,
      @ForAll("agentRoles") AgentRole roleA,
      @ForAll("agentRoles") AgentRole roleB) {

    Assume.that(!issueA.equalsIgnoreCase(issueB));

    // Create two simultaneous sandboxes
    Instant now = Instant.now();
    SandboxResources sandboxA = SandboxNamingConvention.createResources(issueA, roleA, now);
    SandboxResources sandboxB = SandboxNamingConvention.createResources(issueB, roleB, now);

    // Sandbox A cannot access any of Sandbox B's resources
    assertThatThrownBy(() -> isolation.validateAccess(sandboxA.runId(), sandboxB.databaseName()))
        .isInstanceOf(SecurityException.class);
    assertThatThrownBy(() -> isolation.validateAccess(sandboxA.runId(), sandboxB.bucketPrefix()))
        .isInstanceOf(SecurityException.class);
    assertThatThrownBy(() -> isolation.validateAccess(sandboxA.runId(), sandboxB.branchName()))
        .isInstanceOf(SecurityException.class);

    // Sandbox B cannot access any of Sandbox A's resources
    assertThatThrownBy(() -> isolation.validateAccess(sandboxB.runId(), sandboxA.databaseName()))
        .isInstanceOf(SecurityException.class);
    assertThatThrownBy(() -> isolation.validateAccess(sandboxB.runId(), sandboxA.bucketPrefix()))
        .isInstanceOf(SecurityException.class);
    assertThatThrownBy(() -> isolation.validateAccess(sandboxB.runId(), sandboxA.branchName()))
        .isInstanceOf(SecurityException.class);

    // Each sandbox CAN access its own resources
    assertThatNoException()
        .isThrownBy(() -> isolation.validateAccess(sandboxA.runId(), sandboxA.databaseName()));
    assertThatNoException()
        .isThrownBy(() -> isolation.validateAccess(sandboxB.runId(), sandboxB.databaseName()));
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
  Arbitrary<String> differentIssueIdentifiers() {
    // Generate a different set of issue identifiers to ensure distinct issues
    Arbitrary<String> projectKey =
        Arbitraries.strings().withCharRange('A', 'Z').ofMinLength(3).ofMaxLength(5);

    Arbitrary<Integer> issueNumber = Arbitraries.integers().between(5000, 9999);

    return Combinators.combine(projectKey, issueNumber).as((key, number) -> key + "-" + number);
  }

  @Provide
  Arbitrary<AgentRole> agentRoles() {
    return Arbitraries.of(AgentRole.values());
  }
}
