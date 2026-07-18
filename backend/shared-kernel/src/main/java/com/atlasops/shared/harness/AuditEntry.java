package com.atlasops.shared.harness;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents an audit log entry for a blocked action attempt.
 *
 * @param timestamp when the attempt was made (UTC)
 * @param agentCode the agent role code (e.g., "A2")
 * @param attemptedAction description of the action that was attempted
 * @param reason the reason the action was blocked
 * @param runId the sandbox run_id where the attempt originated
 */
public record AuditEntry(
    Instant timestamp, String agentCode, String attemptedAction, String reason, String runId) {

  public AuditEntry {
    Objects.requireNonNull(timestamp, "timestamp must not be null");
    Objects.requireNonNull(agentCode, "agentCode must not be null");
    Objects.requireNonNull(attemptedAction, "attemptedAction must not be null");
    Objects.requireNonNull(reason, "reason must not be null");
    Objects.requireNonNull(runId, "runId must not be null");
  }
}
