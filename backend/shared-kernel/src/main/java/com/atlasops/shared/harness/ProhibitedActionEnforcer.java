package com.atlasops.shared.harness;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Enforces prohibited action policies for agents operating within the harness.
 *
 * <p>Prohibited actions include:
 *
 * <ul>
 *   <li>Access to production secrets
 *   <li>Merge to protected branches (main, master, develop)
 *   <li>Destructive commands outside sandbox: DROP, DELETE without WHERE, rm -rf, truncate, force
 *       push (git push --force / git push -f)
 * </ul>
 *
 * <p>Blocked attempts are recorded in an audit log with timestamp, agent code, attempted action,
 * and blocking reason.
 */
public class ProhibitedActionEnforcer {

  /** Protected branch names that agents cannot merge into. */
  private static final List<String> PROTECTED_BRANCHES = List.of("main", "master", "develop");

  /** Patterns that detect destructive commands. */
  private static final List<DestructivePattern> DESTRUCTIVE_PATTERNS =
      List.of(
          new DestructivePattern(
              Pattern.compile("(?i)\\bDROP\\b"), "Destructive SQL command: DROP"),
          new DestructivePattern(
              Pattern.compile("(?i)\\bDELETE\\b(?!.*\\bWHERE\\b)"),
              "Destructive SQL command: DELETE without WHERE clause"),
          new DestructivePattern(
              Pattern.compile("(?i)\\brm\\s+-rf\\b"), "Destructive shell command: rm -rf"),
          new DestructivePattern(
              Pattern.compile("(?i)\\bTRUNCATE\\b"), "Destructive SQL command: TRUNCATE"),
          new DestructivePattern(
              Pattern.compile("(?i)git\\s+push\\s+--force\\b"),
              "Destructive git command: force push (--force)"),
          new DestructivePattern(
              Pattern.compile("(?i)git\\s+push\\s+-f\\b"),
              "Destructive git command: force push (-f)"));

  /** Pattern for production secret access. */
  private static final Pattern PRODUCTION_SECRET_PATTERN =
      Pattern.compile(
          "(?i)(production|prod).*secret|secret.*(production|prod)|"
              + "\\.env\\.production|\\.env\\.prod|"
              + "prod.*credential|credential.*prod");

  private final List<AuditEntry> auditLog;
  private final java.util.function.Supplier<Instant> clock;

  /**
   * Creates an enforcer with a custom clock for testability.
   *
   * @param clock supplier of the current timestamp
   */
  public ProhibitedActionEnforcer(java.util.function.Supplier<Instant> clock) {
    Objects.requireNonNull(clock, "clock must not be null");
    this.clock = clock;
    this.auditLog = new ArrayList<>();
  }

  /** Creates an enforcer using the system clock. */
  public ProhibitedActionEnforcer() {
    this(Instant::now);
  }

  /**
   * Checks whether an action is allowed or blocked for the given agent. If blocked, an audit entry
   * is recorded.
   *
   * @param role the agent role attempting the action
   * @param action the action descriptor
   * @return {@link EnforcementResult#ALLOWED} or {@link EnforcementResult#BLOCKED}
   */
  public EnforcementResult check(AgentRole role, ActionDescriptor action) {
    Objects.requireNonNull(role, "role must not be null");
    Objects.requireNonNull(action, "action must not be null");

    String reason = evaluateAction(action);

    if (reason != null) {
      recordAudit(role, action, reason);
      return EnforcementResult.BLOCKED;
    }

    return EnforcementResult.ALLOWED;
  }

  /**
   * Returns an unmodifiable view of the audit log.
   *
   * @return list of audit entries for blocked attempts
   */
  public List<AuditEntry> getAuditLog() {
    return Collections.unmodifiableList(auditLog);
  }

  private String evaluateAction(ActionDescriptor action) {
    return switch (action.type()) {
      case SECRET_ACCESS -> evaluateSecretAccess(action.target());
      case MERGE -> evaluateMerge(action.target());
      case COMMAND -> evaluateCommand(action.target());
      case DB_OPERATION -> evaluateDbOperation(action.target());
    };
  }

  private String evaluateSecretAccess(String target) {
    if (PRODUCTION_SECRET_PATTERN.matcher(target).find()) {
      return "Access to production secrets is prohibited";
    }
    return null;
  }

  private String evaluateMerge(String target) {
    for (String protectedBranch : PROTECTED_BRANCHES) {
      if (target.equalsIgnoreCase(protectedBranch) || target.endsWith("/" + protectedBranch)) {
        return "Merge to protected branch '" + protectedBranch + "' is prohibited";
      }
    }
    return null;
  }

  private String evaluateCommand(String target) {
    for (DestructivePattern pattern : DESTRUCTIVE_PATTERNS) {
      if (pattern.pattern().matcher(target).find()) {
        return pattern.reason();
      }
    }
    return null;
  }

  private String evaluateDbOperation(String target) {
    for (DestructivePattern pattern : DESTRUCTIVE_PATTERNS) {
      if (pattern.pattern().matcher(target).find()) {
        return pattern.reason();
      }
    }
    return null;
  }

  private void recordAudit(AgentRole role, ActionDescriptor action, String reason) {
    auditLog.add(
        new AuditEntry(
            clock.get(),
            role.code(),
            action.type() + ": " + action.target(),
            reason,
            action.runId()));
  }

  private record DestructivePattern(Pattern pattern, String reason) {}
}
