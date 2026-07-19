package com.atlasops.approvals.domain;

import java.util.Map;
import java.util.Set;

/**
 * Enum representing the lifecycle status of an approval.
 *
 * <p>Valid transitions:
 *
 * <ul>
 *   <li>PENDING → APPROVED
 *   <li>PENDING → REJECTED
 *   <li>PENDING → CANCELLED
 * </ul>
 *
 * <p>Terminal states (no transitions allowed): APPROVED, REJECTED, CANCELLED
 */
public enum ApprovalStatus {
  PENDING,
  APPROVED,
  REJECTED,
  CANCELLED;

  private static final Map<ApprovalStatus, Set<ApprovalStatus>> TRANSITIONS =
      Map.of(
          PENDING, Set.of(APPROVED, REJECTED, CANCELLED),
          APPROVED, Set.of(),
          REJECTED, Set.of(),
          CANCELLED, Set.of());

  /**
   * Checks if a transition from this status to the target status is valid.
   *
   * @param target the target status
   * @return true if the transition is allowed
   */
  public boolean canTransitionTo(ApprovalStatus target) {
    return TRANSITIONS.get(this).contains(target);
  }

  /**
   * Returns true if this status is a terminal state (no further transitions allowed).
   *
   * @return true if terminal
   */
  public boolean isTerminal() {
    return TRANSITIONS.get(this).isEmpty();
  }
}
