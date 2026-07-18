package com.atlasops.shared.sdd;

import java.util.Set;

/**
 * Represents the lifecycle status of a task in the SDD workflow.
 *
 * <p>Valid transitions:
 *
 * <ul>
 *   <li>TODO → IN_PROGRESS
 *   <li>TODO → DONE
 *   <li>IN_PROGRESS → DONE
 *   <li>IN_PROGRESS → BLOCKED
 * </ul>
 */
public enum TaskStatus {
  TODO(Set.of()),
  IN_PROGRESS(Set.of()),
  DONE(Set.of()),
  BLOCKED(Set.of());

  private Set<TaskStatus> validTargets;

  TaskStatus(Set<TaskStatus> validTargets) {
    this.validTargets = validTargets;
  }

  /**
   * Initializes valid transitions. Called statically because enum constants cannot reference
   * forward-declared constants in their constructors.
   */
  static {
    TODO.validTargets = Set.of(IN_PROGRESS, DONE);
    IN_PROGRESS.validTargets = Set.of(DONE, BLOCKED);
    DONE.validTargets = Set.of();
    BLOCKED.validTargets = Set.of();
  }

  /** Returns the set of statuses this status can transition to. */
  public Set<TaskStatus> validTargets() {
    return validTargets;
  }

  /**
   * Checks whether transitioning from this status to the given target is valid.
   *
   * @param target the desired target status
   * @return true if the transition is valid
   */
  public boolean canTransitionTo(TaskStatus target) {
    return validTargets.contains(target);
  }
}
