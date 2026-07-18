package com.atlasops.shared.harness;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import net.jqwik.api.*;

/**
 * Property-based tests for prohibited action enforcement.
 *
 * <p><b>Validates: Requirements 8.6, 8.8</b>
 *
 * <p>Property 13: For any agent (A1-A11) attempting a prohibited action (access production secrets,
 * merge to protected branch, execute destructive command outside sandbox), the system SHALL block
 * the action, register an audit entry with timestamp, agent code, attempted action, and blocking
 * reason, and notify the task owner.
 */
@Tag("Feature: monorepo-sdd-harness, Property 13: Prohibited Action Enforcement")
class ProhibitedActionEnforcementPropertyTest {

  private static final Instant FIXED_TIMESTAMP = Instant.parse("2024-06-15T10:30:00Z");

  // ─── Property: Production secret access is always blocked ─────────────────────

  @Property(tries = 100)
  void productionSecretAccess_shouldAlwaysBeBlocked(
      @ForAll("agentRoles") AgentRole role,
      @ForAll("productionSecretTargets") String secretTarget,
      @ForAll("runIds") String runId) {

    ProhibitedActionEnforcer enforcer = new ProhibitedActionEnforcer(() -> FIXED_TIMESTAMP);
    ActionDescriptor action = new ActionDescriptor(ActionType.SECRET_ACCESS, secretTarget, runId);

    EnforcementResult result = enforcer.check(role, action);

    assertThat(result)
        .as(
            "Agent %s accessing production secret '%s' should be blocked",
            role.code(), secretTarget)
        .isEqualTo(EnforcementResult.BLOCKED);
  }

  // ─── Property: Merge to protected branch is always blocked ────────────────────

  @Property(tries = 100)
  void mergeToProtectedBranch_shouldAlwaysBeBlocked(
      @ForAll("agentRoles") AgentRole role,
      @ForAll("protectedBranchTargets") String branchTarget,
      @ForAll("runIds") String runId) {

    ProhibitedActionEnforcer enforcer = new ProhibitedActionEnforcer(() -> FIXED_TIMESTAMP);
    ActionDescriptor action = new ActionDescriptor(ActionType.MERGE, branchTarget, runId);

    EnforcementResult result = enforcer.check(role, action);

    assertThat(result)
        .as(
            "Agent %s merging to protected branch '%s' should be blocked",
            role.code(), branchTarget)
        .isEqualTo(EnforcementResult.BLOCKED);
  }

  // ─── Property: Destructive commands outside sandbox are always blocked ────────

  @Property(tries = 100)
  void destructiveCommand_shouldAlwaysBeBlocked(
      @ForAll("agentRoles") AgentRole role,
      @ForAll("destructiveCommands") String command,
      @ForAll("runIds") String runId) {

    ProhibitedActionEnforcer enforcer = new ProhibitedActionEnforcer(() -> FIXED_TIMESTAMP);
    ActionDescriptor action = new ActionDescriptor(ActionType.COMMAND, command, runId);

    EnforcementResult result = enforcer.check(role, action);

    assertThat(result)
        .as("Agent %s executing destructive command '%s' should be blocked", role.code(), command)
        .isEqualTo(EnforcementResult.BLOCKED);
  }

  // ─── Property: Blocked action always registers audit entry with required fields ─

  @Property(tries = 100)
  void blockedAction_shouldRegisterAuditEntryWithAllFields(
      @ForAll("agentRoles") AgentRole role, @ForAll("prohibitedActions") ActionDescriptor action) {

    ProhibitedActionEnforcer enforcer = new ProhibitedActionEnforcer(() -> FIXED_TIMESTAMP);

    EnforcementResult result = enforcer.check(role, action);

    assertThat(result).isEqualTo(EnforcementResult.BLOCKED);

    List<AuditEntry> auditLog = enforcer.getAuditLog();
    assertThat(auditLog)
        .as("Audit log should have exactly one entry after one blocked action")
        .hasSize(1);

    AuditEntry entry = auditLog.get(0);

    // Audit entry must contain timestamp
    assertThat(entry.timestamp())
        .as("Audit entry must contain a non-null timestamp")
        .isNotNull()
        .isEqualTo(FIXED_TIMESTAMP);

    // Audit entry must contain agent code
    assertThat(entry.agentCode())
        .as("Audit entry must contain the agent code")
        .isNotNull()
        .isNotBlank()
        .isEqualTo(role.code());

    // Audit entry must contain the attempted action description
    assertThat(entry.attemptedAction())
        .as("Audit entry must contain a description of the attempted action")
        .isNotNull()
        .isNotBlank();

    // Audit entry must contain blocking reason
    assertThat(entry.reason())
        .as("Audit entry must contain a blocking reason")
        .isNotNull()
        .isNotBlank();
  }

  // ─── Property: Audit timestamp reflects the moment of the attempt ─────────────

  @Property(tries = 100)
  void auditEntry_shouldRecordTimestampFromClock(
      @ForAll("agentRoles") AgentRole role,
      @ForAll("prohibitedActions") ActionDescriptor action,
      @ForAll("timestamps") Instant timestamp) {

    ProhibitedActionEnforcer enforcer = new ProhibitedActionEnforcer(() -> timestamp);

    enforcer.check(role, action);

    List<AuditEntry> auditLog = enforcer.getAuditLog();
    assertThat(auditLog).hasSize(1);
    assertThat(auditLog.get(0).timestamp())
        .as("Audit timestamp should reflect the clock's instant at the time of the attempt")
        .isEqualTo(timestamp);
  }

  // ─── Property: Each blocked action adds exactly one audit entry ───────────────

  @Property(tries = 100)
  void multipleBlockedActions_shouldEachAddOneAuditEntry(
      @ForAll("agentRoles") AgentRole role1,
      @ForAll("agentRoles") AgentRole role2,
      @ForAll("prohibitedActions") ActionDescriptor action1,
      @ForAll("prohibitedActions") ActionDescriptor action2) {

    ProhibitedActionEnforcer enforcer = new ProhibitedActionEnforcer(() -> FIXED_TIMESTAMP);

    enforcer.check(role1, action1);
    enforcer.check(role2, action2);

    List<AuditEntry> auditLog = enforcer.getAuditLog();
    assertThat(auditLog).as("Each blocked action should add exactly one audit entry").hasSize(2);

    assertThat(auditLog.get(0).agentCode()).isEqualTo(role1.code());
    assertThat(auditLog.get(1).agentCode()).isEqualTo(role2.code());
  }

  // ─── Property: Allowed actions produce no audit entries ───────────────────────

  @Property(tries = 100)
  void allowedAction_shouldNotCreateAuditEntry(
      @ForAll("agentRoles") AgentRole role,
      @ForAll("safeActionDescriptors") ActionDescriptor action) {

    ProhibitedActionEnforcer enforcer = new ProhibitedActionEnforcer(() -> FIXED_TIMESTAMP);

    EnforcementResult result = enforcer.check(role, action);

    assertThat(result)
        .as("Safe action '%s' should be allowed", action.target())
        .isEqualTo(EnforcementResult.ALLOWED);
    assertThat(enforcer.getAuditLog())
        .as("Allowed actions should not produce audit entries")
        .isEmpty();
  }

  // ─── Property: Destructive DB operations are blocked ─────────────────────────

  @Property(tries = 100)
  void destructiveDbOperation_shouldAlwaysBeBlocked(
      @ForAll("agentRoles") AgentRole role,
      @ForAll("destructiveDbOperations") String dbOp,
      @ForAll("runIds") String runId) {

    ProhibitedActionEnforcer enforcer = new ProhibitedActionEnforcer(() -> FIXED_TIMESTAMP);
    ActionDescriptor action = new ActionDescriptor(ActionType.DB_OPERATION, dbOp, runId);

    EnforcementResult result = enforcer.check(role, action);

    assertThat(result)
        .as("Agent %s executing destructive DB operation '%s' should be blocked", role.code(), dbOp)
        .isEqualTo(EnforcementResult.BLOCKED);
  }

  // ─── Generators ──────────────────────────────────────────────────────────────

  @Provide
  Arbitrary<AgentRole> agentRoles() {
    return Arbitraries.of(AgentRole.values());
  }

  @Provide
  Arbitrary<String> productionSecretTargets() {
    return Arbitraries.of(
        "production-secret-key",
        "prod-secret-config",
        "secret-for-production",
        ".env.production",
        ".env.prod",
        "prod-credential-store",
        "credential-prod-vault",
        "PRODUCTION_SECRET_KEY",
        "access production secrets",
        "prod.secret.yml");
  }

  @Provide
  Arbitrary<String> protectedBranchTargets() {
    return Arbitraries.of(
        "main",
        "master",
        "develop",
        "MAIN",
        "MASTER",
        "DEVELOP",
        "origin/main",
        "origin/master",
        "origin/develop");
  }

  @Provide
  Arbitrary<String> destructiveCommands() {
    return Arbitraries.of(
        "rm -rf /data",
        "rm -rf .",
        "DROP TABLE users",
        "DROP DATABASE atlasops",
        "DELETE FROM customers",
        "TRUNCATE TABLE logs",
        "git push --force origin main",
        "git push -f origin feature",
        "sudo rm -rf /var",
        "TRUNCATE sessions");
  }

  @Provide
  Arbitrary<String> destructiveDbOperations() {
    return Arbitraries.of(
        "DROP TABLE users",
        "DROP DATABASE atlasops",
        "DELETE FROM customers",
        "TRUNCATE TABLE sessions",
        "DROP INDEX idx_users",
        "TRUNCATE events",
        "DELETE FROM orders");
  }

  @Provide
  Arbitrary<ActionDescriptor> prohibitedActions() {
    Arbitrary<ActionDescriptor> secretAccess =
        Arbitraries.of(
                "production-secret-key",
                ".env.production",
                "prod-credential-store",
                "credential-prod-vault",
                ".env.prod",
                "secret-for-production")
            .map(
                target ->
                    new ActionDescriptor(ActionType.SECRET_ACCESS, target, "ATLAS-1-agent-A2"));

    Arbitrary<ActionDescriptor> mergeProtected =
        Arbitraries.of(
                "main", "master", "develop", "origin/main", "origin/master", "origin/develop")
            .map(target -> new ActionDescriptor(ActionType.MERGE, target, "ATLAS-1-agent-A2"));

    Arbitrary<ActionDescriptor> destructiveCmd =
        Arbitraries.of(
                "rm -rf /data",
                "DROP TABLE users",
                "DELETE FROM customers",
                "TRUNCATE TABLE logs",
                "git push --force origin main",
                "git push -f origin feature")
            .map(target -> new ActionDescriptor(ActionType.COMMAND, target, "ATLAS-1-agent-A2"));

    return Arbitraries.oneOf(List.of(secretAccess, mergeProtected, destructiveCmd));
  }

  @Provide
  Arbitrary<ActionDescriptor> safeActionDescriptors() {
    // Generate action descriptors for actions that should NOT be blocked
    Arbitrary<String> safeCommandTargets =
        Arbitraries.of(
            "ls -la",
            "cat README.md",
            "npm install",
            "gradle build",
            "git push origin feature/new-feature",
            "docker compose up -d");

    Arbitrary<String> safeDbTargets =
        Arbitraries.of(
            "SELECT * FROM users WHERE id = 1",
            "INSERT INTO logs VALUES (1, 'test')",
            "UPDATE users SET name = 'test' WHERE id = 1",
            "DELETE FROM temp_data WHERE created_at < '2024-01-01'");

    Arbitrary<String> safeMergeTargets =
        Arbitraries.of("feature/new-feature", "fix/bugfix-123", "sandbox/ATLAS-42-agent-A2");

    Arbitrary<String> safeSecretTargets =
        Arbitraries.of("local-dev-secret", "staging-api-key", "test-config-values");

    Arbitrary<ActionDescriptor> safeCommands =
        safeCommandTargets.map(
            t -> new ActionDescriptor(ActionType.COMMAND, t, "ATLAS-1-agent-A2"));
    Arbitrary<ActionDescriptor> safeDb =
        safeDbTargets.map(
            t -> new ActionDescriptor(ActionType.DB_OPERATION, t, "ATLAS-1-agent-A2"));
    Arbitrary<ActionDescriptor> safeMerge =
        safeMergeTargets.map(t -> new ActionDescriptor(ActionType.MERGE, t, "ATLAS-1-agent-A2"));
    Arbitrary<ActionDescriptor> safeSecret =
        safeSecretTargets.map(
            t -> new ActionDescriptor(ActionType.SECRET_ACCESS, t, "ATLAS-1-agent-A2"));

    return Arbitraries.oneOf(safeCommands, safeDb, safeMerge, safeSecret);
  }

  @Provide
  Arbitrary<String> runIds() {
    Arbitrary<String> projectKey =
        Arbitraries.strings().withCharRange('A', 'Z').ofMinLength(2).ofMaxLength(6);

    Arbitrary<Integer> issueNumber = Arbitraries.integers().between(1, 9999);
    Arbitrary<AgentRole> role = Arbitraries.of(AgentRole.values());

    return Combinators.combine(projectKey, issueNumber, role)
        .as((key, number, r) -> key + "-" + number + "-agent-" + r.code());
  }

  @Provide
  Arbitrary<Instant> timestamps() {
    long start = Instant.parse("2024-01-01T00:00:00Z").getEpochSecond();
    long end = Instant.parse("2025-01-01T00:00:00Z").getEpochSecond();
    return Arbitraries.longs().between(start, end).map(Instant::ofEpochSecond);
  }
}
