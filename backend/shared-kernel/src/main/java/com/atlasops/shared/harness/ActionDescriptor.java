package com.atlasops.shared.harness;

import java.util.Objects;

/**
 * Describes an action being attempted by an agent. Used by {@link ProhibitedActionEnforcer} to
 * evaluate whether the action is allowed.
 *
 * @param type the category of the action
 * @param target the target resource or command string
 * @param runId the sandbox run_id from which the action originates
 */
public record ActionDescriptor(ActionType type, String target, String runId) {

  public ActionDescriptor {
    Objects.requireNonNull(type, "type must not be null");
    Objects.requireNonNull(target, "target must not be null");
    Objects.requireNonNull(runId, "runId must not be null");
  }
}
