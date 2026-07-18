package com.atlasops.shared.harness;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import net.jqwik.api.*;

/**
 * Property-based tests for sandbox destructive command blocking.
 *
 * <p><b>Validates: Requirements 9.5</b>
 *
 * <p>Property 16: For any destructive command (DROP, DELETE sem WHERE, rm -rf, truncate, force
 * push) targeting resources outside the sandbox's namespace, the system SHALL block execution,
 * prevent side effects, and log the attempt with timestamp, run_id, blocked command, and
 * responsible agent.
 */
@Tag("Feature: monorepo-sdd-harness, Property 16: Sandbox Destructive Command Blocking")
class SandboxDestructiveCommandBlockingPropertyTest {

  private static final Instant FIXED_TIMESTAMP = Instant.parse("2024-06-15T10:30:00Z");

  // ─── Property: All destructive commands are blocked ──────────────────────────

  @Property(tries = 100)
  void destructiveCommands_shouldAlwaysBeBlocked(
      @ForAll("destructiveCommands") String command,
      @ForAll("agentRoles") AgentRole role,
      @ForAll("runIds") String runId) {

    var enforcer = new ProhibitedActionEnforcer(() -> FIXED_TIMESTAMP);
    var action = new ActionDescriptor(ActionType.COMMAND, command, runId);

    EnforcementResult result = enforcer.check(role, action);

    assertThat(result)
        .as("Destructive command '%s' from agent %s should be BLOCKED", command, role.code())
        .isEqualTo(EnforcementResult.BLOCKED);
  }

  // ─── Property: Destructive DB operations are blocked ─────────────────────────

  @Property(tries = 100)
  void destructiveDbOperations_shouldAlwaysBeBlocked(
      @ForAll("destructiveDbStatements") String dbStatement,
      @ForAll("agentRoles") AgentRole role,
      @ForAll("runIds") String runId) {

    var enforcer = new ProhibitedActionEnforcer(() -> FIXED_TIMESTAMP);
    var action = new ActionDescriptor(ActionType.DB_OPERATION, dbStatement, runId);

    EnforcementResult result = enforcer.check(role, action);

    assertThat(result)
        .as(
            "Destructive DB operation '%s' from agent %s should be BLOCKED",
            dbStatement, role.code())
        .isEqualTo(EnforcementResult.BLOCKED);
  }

  // ─── Property: Blocked attempts are logged with all required fields ──────────

  @Property(tries = 100)
  void blockedAttempt_shouldBeLogged_withTimestampRunIdCommandAndAgent(
      @ForAll("destructiveCommands") String command,
      @ForAll("agentRoles") AgentRole role,
      @ForAll("runIds") String runId) {

    var enforcer = new ProhibitedActionEnforcer(() -> FIXED_TIMESTAMP);
    var action = new ActionDescriptor(ActionType.COMMAND, command, runId);

    enforcer.check(role, action);

    List<AuditEntry> auditLog = enforcer.getAuditLog();
    assertThat(auditLog)
        .as("Audit log should contain exactly one entry for the blocked command")
        .hasSize(1);

    AuditEntry entry = auditLog.get(0);

    // Timestamp must be present and match the clock
    assertThat(entry.timestamp())
        .as("Audit entry must contain the timestamp of the attempt")
        .isNotNull()
        .isEqualTo(FIXED_TIMESTAMP);

    // run_id must be recorded
    assertThat(entry.runId())
        .as("Audit entry must contain the run_id")
        .isNotNull()
        .isEqualTo(runId);

    // Blocked command must be recorded in the attemptedAction field
    assertThat(entry.attemptedAction())
        .as("Audit entry must contain the blocked command")
        .isNotNull()
        .contains(command);

    // Responsible agent must be recorded
    assertThat(entry.agentCode())
        .as("Audit entry must contain the responsible agent code")
        .isNotNull()
        .isEqualTo(role.code());
  }

  // ─── Property: Blocking prevents side effects (no state change) ──────────────

  @Property(tries = 100)
  void blockedCommand_shouldPreventSideEffects(
      @ForAll("destructiveCommands") String command,
      @ForAll("agentRoles") AgentRole role,
      @ForAll("runIds") String runId) {

    var enforcer = new ProhibitedActionEnforcer(() -> FIXED_TIMESTAMP);
    var action = new ActionDescriptor(ActionType.COMMAND, command, runId);

    // The result must be BLOCKED — this means execution is prevented
    EnforcementResult result = enforcer.check(role, action);

    assertThat(result)
        .as("Destructive command must be BLOCKED to prevent side effects")
        .isEqualTo(EnforcementResult.BLOCKED);

    // The only state change is the audit log entry — no other side effects
    assertThat(enforcer.getAuditLog())
        .as("Only side effect of blocking should be the audit log entry")
        .hasSize(1);
  }

  // ─── Property: Audit log reason describes the destructive nature ─────────────

  @Property(tries = 100)
  void auditLogReason_shouldDescribeDestructiveNature(
      @ForAll("destructiveCommands") String command,
      @ForAll("agentRoles") AgentRole role,
      @ForAll("runIds") String runId) {

    var enforcer = new ProhibitedActionEnforcer(() -> FIXED_TIMESTAMP);
    var action = new ActionDescriptor(ActionType.COMMAND, command, runId);

    enforcer.check(role, action);

    AuditEntry entry = enforcer.getAuditLog().get(0);

    // Reason must be non-empty and describe the destructive nature
    assertThat(entry.reason())
        .as("Audit entry reason should describe why the command is destructive")
        .isNotNull()
        .isNotBlank();

    // Reason should mention "Destructive" in some form
    assertThat(entry.reason().toLowerCase())
        .as("Audit entry reason should indicate a destructive action")
        .contains("destructive");
  }

  // ─── Property: Force push variants are all blocked ───────────────────────────

  @Property(tries = 100)
  void forcePushVariants_shouldAllBeBlocked(
      @ForAll("forcePushCommands") String forcePush,
      @ForAll("agentRoles") AgentRole role,
      @ForAll("runIds") String runId) {

    var enforcer = new ProhibitedActionEnforcer(() -> FIXED_TIMESTAMP);
    var action = new ActionDescriptor(ActionType.COMMAND, forcePush, runId);

    EnforcementResult result = enforcer.check(role, action);

    assertThat(result)
        .as("Force push command '%s' should be BLOCKED", forcePush)
        .isEqualTo(EnforcementResult.BLOCKED);
  }

  // ─── Generators ──────────────────────────────────────────────────────────────

  @Provide
  Arbitrary<String> destructiveCommands() {
    // Generate various destructive command patterns
    Arbitrary<String> dropCommands =
        Arbitraries.of(
            "DROP TABLE users",
            "DROP DATABASE production",
            "DROP INDEX idx_name",
            "drop table customers cascade",
            "DROP SCHEMA public CASCADE");

    Arbitrary<String> deleteWithoutWhereCommands =
        Arbitraries.of(
            "DELETE FROM users",
            "DELETE FROM orders",
            "delete from customers",
            "DELETE FROM audit_log",
            "DELETE FROM documents");

    Arbitrary<String> rmRfCommands =
        Arbitraries.of(
            "rm -rf /var/data",
            "rm -rf /home/user/project",
            "rm -rf /tmp/sandbox",
            "rm -rf ./node_modules",
            "rm -rf /etc/config");

    Arbitrary<String> truncateCommands =
        Arbitraries.of(
            "TRUNCATE TABLE users",
            "TRUNCATE customers",
            "truncate table orders",
            "TRUNCATE audit_log CASCADE",
            "TRUNCATE documents");

    Arbitrary<String> forcePushLong =
        Arbitraries.of(
            "git push --force origin main",
            "git push --force origin develop",
            "git push --force origin feature/branch");

    Arbitrary<String> forcePushShort =
        Arbitraries.of(
            "git push -f origin main",
            "git push -f origin develop",
            "git push -f origin feature/branch");

    return Arbitraries.oneOf(
        dropCommands,
        deleteWithoutWhereCommands,
        rmRfCommands,
        truncateCommands,
        forcePushLong,
        forcePushShort);
  }

  @Provide
  Arbitrary<String> destructiveDbStatements() {
    // Generate destructive SQL statements via DB_OPERATION action type
    Arbitrary<String> dropStatements =
        Arbitraries.of(
            "DROP TABLE users",
            "DROP DATABASE atlasops_production",
            "DROP INDEX idx_users_email",
            "drop table tenants cascade");

    Arbitrary<String> deleteWithoutWhere =
        Arbitraries.of(
            "DELETE FROM users",
            "DELETE FROM customers",
            "delete from orders",
            "DELETE FROM documents");

    Arbitrary<String> truncateStatements =
        Arbitraries.of(
            "TRUNCATE TABLE users",
            "TRUNCATE customers",
            "truncate table orders",
            "TRUNCATE documents CASCADE");

    return Arbitraries.oneOf(dropStatements, deleteWithoutWhere, truncateStatements);
  }

  @Provide
  Arbitrary<String> forcePushCommands() {
    // Generate different force push variations
    Arbitrary<String> branches =
        Arbitraries.of("main", "master", "develop", "feature/branch", "hotfix/fix");

    Arbitrary<String> longForm = branches.map(branch -> "git push --force origin " + branch);
    Arbitrary<String> shortForm = branches.map(branch -> "git push -f origin " + branch);

    return Arbitraries.oneOf(longForm, shortForm);
  }

  @Provide
  Arbitrary<AgentRole> agentRoles() {
    return Arbitraries.of(AgentRole.values());
  }

  @Provide
  Arbitrary<String> runIds() {
    // Generate realistic run_id values following convention: {issue}-agent-{role}
    Arbitrary<String> projectKey =
        Arbitraries.strings().withCharRange('A', 'Z').ofMinLength(2).ofMaxLength(6);

    Arbitrary<Integer> issueNumber = Arbitraries.integers().between(1, 9999);
    Arbitrary<AgentRole> role = Arbitraries.of(AgentRole.values());

    return Combinators.combine(projectKey, issueNumber, role)
        .as((key, number, r) -> key + "-" + number + "-agent-" + r.code());
  }
}
