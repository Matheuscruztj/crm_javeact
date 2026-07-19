package com.atlasops.requests.domain;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Represents the lifecycle status of a service request with a defined state machine.
 *
 * <p>Allowed transitions:
 *
 * <ul>
 *   <li>OPEN → IN_PROGRESS
 *   <li>OPEN → CANCELLED
 *   <li>IN_PROGRESS → WAITING_CUSTOMER
 *   <li>IN_PROGRESS → COMPLETED
 *   <li>IN_PROGRESS → CANCELLED
 *   <li>WAITING_CUSTOMER → IN_PROGRESS
 *   <li>WAITING_CUSTOMER → CANCELLED
 * </ul>
 */
public enum RequestStatus {
  OPEN,
  IN_PROGRESS,
  WAITING_CUSTOMER,
  COMPLETED,
  CANCELLED;

  /**
   * Returns the set of statuses this status can transition to.
   *
   * @return an unmodifiable set of allowed target statuses
   */
  public Set<RequestStatus> getAllowedTransitions() {
    return switch (this) {
      case OPEN -> Collections.unmodifiableSet(EnumSet.of(IN_PROGRESS, CANCELLED));
      case IN_PROGRESS ->
          Collections.unmodifiableSet(EnumSet.of(WAITING_CUSTOMER, COMPLETED, CANCELLED));
      case WAITING_CUSTOMER -> Collections.unmodifiableSet(EnumSet.of(IN_PROGRESS, CANCELLED));
      case COMPLETED, CANCELLED -> Collections.emptySet();
    };
  }

  /**
   * Checks whether a transition from this status to the target status is allowed.
   *
   * @param target the target status
   * @return true if the transition is allowed, false otherwise
   */
  public boolean canTransitionTo(RequestStatus target) {
    if (target == null) {
      return false;
    }
    return getAllowedTransitions().contains(target);
  }

  /**
   * Validates that a transition to the target status is allowed, throwing an exception if not.
   *
   * @param target the target status
   * @throws IllegalStateException if the transition is not allowed
   */
  public void validateTransitionTo(RequestStatus target) {
    if (!canTransitionTo(target)) {
      throw new IllegalStateException(
          "Cannot transition from "
              + this
              + " to "
              + target
              + ". Allowed transitions from "
              + this
              + ": "
              + getAllowedTransitions());
    }
  }
}
