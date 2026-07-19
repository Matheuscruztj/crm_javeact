package com.atlasops.requests.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Set;
import net.jqwik.api.*;

/**
 * Property-based tests for the Request Status State Machine.
 *
 * <p><b>Validates: Requirements 8.3, 8.4</b>
 *
 * <p>Property 11: Request Status State Machine
 *
 * <p>Requirement 8.3: THE Request_Module SHALL enforce the following status transitions:
 * OPEN→IN_PROGRESS, OPEN→CANCELLED, IN_PROGRESS→WAITING_CUSTOMER, IN_PROGRESS→COMPLETED,
 * IN_PROGRESS→CANCELLED, WAITING_CUSTOMER→IN_PROGRESS, WAITING_CUSTOMER→CANCELLED
 *
 * <p>Requirement 8.4: IF a status transition not in the allowed set is attempted, THEN THE
 * Request_Module SHALL return a 422 Unprocessable Entity error with the current status and the list
 * of valid target statuses
 */
@Tag("Feature: project-implementation-kickoff, Property 11: Request Status State Machine")
class RequestStatusStateMachinePropertyTest {

  /** The complete set of allowed transitions as defined in Requirement 8.3. */
  private static final Set<StatusPair> ALLOWED_TRANSITIONS =
      Set.of(
          new StatusPair(RequestStatus.OPEN, RequestStatus.IN_PROGRESS),
          new StatusPair(RequestStatus.OPEN, RequestStatus.CANCELLED),
          new StatusPair(RequestStatus.IN_PROGRESS, RequestStatus.WAITING_CUSTOMER),
          new StatusPair(RequestStatus.IN_PROGRESS, RequestStatus.COMPLETED),
          new StatusPair(RequestStatus.IN_PROGRESS, RequestStatus.CANCELLED),
          new StatusPair(RequestStatus.WAITING_CUSTOMER, RequestStatus.IN_PROGRESS),
          new StatusPair(RequestStatus.WAITING_CUSTOMER, RequestStatus.CANCELLED));

  /**
   * Property: For ANY allowed (current, target) pair in the defined transition set, canTransitionTo
   * MUST return true and validateTransitionTo MUST NOT throw.
   *
   * <p>Validates: Requirements 8.3
   */
  @Property(tries = 100)
  void should_allowTransition_when_pairIsInAllowedSet(
      @ForAll("allowedTransitions") StatusPair pair) {

    // Act
    boolean result = pair.from().canTransitionTo(pair.to());

    // Assert
    assertThat(result)
        .as("Transition from %s to %s should be allowed", pair.from(), pair.to())
        .isTrue();

    // Also verify validateTransitionTo does not throw
    Throwable thrown = catchThrowable(() -> pair.from().validateTransitionTo(pair.to()));
    assertThat(thrown).isNull();
  }

  /**
   * Property: For ANY (current, target) pair NOT in the allowed transition set, canTransitionTo
   * MUST return false and validateTransitionTo MUST throw IllegalStateException.
   *
   * <p>Validates: Requirements 8.4
   */
  @Property(tries = 100)
  void should_rejectTransition_when_pairIsNotInAllowedSet(
      @ForAll("disallowedTransitions") StatusPair pair) {

    // Act
    boolean result = pair.from().canTransitionTo(pair.to());

    // Assert
    assertThat(result)
        .as("Transition from %s to %s should be rejected", pair.from(), pair.to())
        .isFalse();

    // Also verify validateTransitionTo throws with meaningful message
    Throwable thrown = catchThrowable(() -> pair.from().validateTransitionTo(pair.to()));
    assertThat(thrown)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining(pair.from().name())
        .hasMessageContaining(pair.from().getAllowedTransitions().toString());
  }

  /**
   * Property: For ANY status, the set of allowed transitions returned by getAllowedTransitions() is
   * consistent with canTransitionTo() — every status in the set returns true, and every status NOT
   * in the set returns false.
   *
   * <p>Validates: Requirements 8.3, 8.4
   */
  @Property(tries = 100)
  void should_beConsistent_when_comparingAllowedTransitionsWithCanTransitionTo(
      @ForAll("allStatuses") RequestStatus currentStatus) {

    Set<RequestStatus> allowedTargets = currentStatus.getAllowedTransitions();

    for (RequestStatus candidate : RequestStatus.values()) {
      boolean canTransition = currentStatus.canTransitionTo(candidate);

      if (allowedTargets.contains(candidate)) {
        assertThat(canTransition)
            .as(
                "Status %s lists %s as allowed but canTransitionTo returns false",
                currentStatus, candidate)
            .isTrue();
      } else {
        assertThat(canTransition)
            .as(
                "Status %s does not list %s as allowed but canTransitionTo returns true",
                currentStatus, candidate)
            .isFalse();
      }
    }
  }

  /**
   * Property: Terminal states (COMPLETED, CANCELLED) have NO allowed transitions. For ANY target
   * status, transitioning from a terminal state MUST be rejected.
   *
   * <p>Validates: Requirements 8.3
   */
  @Property(tries = 100)
  void should_rejectAllTransitions_when_currentStatusIsTerminal(
      @ForAll("terminalStatuses") RequestStatus terminalStatus,
      @ForAll("allStatuses") RequestStatus target) {

    // Act
    boolean result = terminalStatus.canTransitionTo(target);

    // Assert: terminal states cannot transition to anything
    assertThat(result)
        .as("Terminal status %s should not allow transition to %s", terminalStatus, target)
        .isFalse();
    assertThat(terminalStatus.getAllowedTransitions()).isEmpty();
  }

  // ---- Custom Arbitraries ----

  @Provide
  Arbitrary<StatusPair> allowedTransitions() {
    return Arbitraries.of(ALLOWED_TRANSITIONS.stream().toList());
  }

  @Provide
  Arbitrary<StatusPair> disallowedTransitions() {
    // Generate all possible (from, to) pairs and exclude the allowed ones
    return Combinators.combine(
            Arbitraries.of(RequestStatus.values()), Arbitraries.of(RequestStatus.values()))
        .as(StatusPair::new)
        .filter(pair -> !ALLOWED_TRANSITIONS.contains(pair));
  }

  @Provide
  Arbitrary<RequestStatus> allStatuses() {
    return Arbitraries.of(RequestStatus.values());
  }

  @Provide
  Arbitrary<RequestStatus> terminalStatuses() {
    return Arbitraries.of(RequestStatus.COMPLETED, RequestStatus.CANCELLED);
  }

  // ---- Helper Record ----

  record StatusPair(RequestStatus from, RequestStatus to) {}
}
