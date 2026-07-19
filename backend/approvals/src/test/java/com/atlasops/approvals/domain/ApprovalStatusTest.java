package com.atlasops.approvals.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** Unit tests for ApprovalStatus enum state machine. */
class ApprovalStatusTest {

  // --- PENDING transitions ---

  @Test
  void should_allowTransitionToApproved_when_statusIsPending() {
    assertThat(ApprovalStatus.PENDING.canTransitionTo(ApprovalStatus.APPROVED)).isTrue();
  }

  @Test
  void should_allowTransitionToRejected_when_statusIsPending() {
    assertThat(ApprovalStatus.PENDING.canTransitionTo(ApprovalStatus.REJECTED)).isTrue();
  }

  @Test
  void should_allowTransitionToCancelled_when_statusIsPending() {
    assertThat(ApprovalStatus.PENDING.canTransitionTo(ApprovalStatus.CANCELLED)).isTrue();
  }

  @Test
  void should_notAllowTransitionToPending_when_statusIsPending() {
    assertThat(ApprovalStatus.PENDING.canTransitionTo(ApprovalStatus.PENDING)).isFalse();
  }

  @Test
  void should_notBeTerminal_when_statusIsPending() {
    assertThat(ApprovalStatus.PENDING.isTerminal()).isFalse();
  }

  // --- Terminal states (no transitions allowed) ---

  @ParameterizedTest
  @EnumSource(
      value = ApprovalStatus.class,
      names = {"APPROVED", "REJECTED", "CANCELLED"})
  void should_notAllowAnyTransition_when_statusIsTerminal(ApprovalStatus terminalStatus) {
    for (ApprovalStatus target : ApprovalStatus.values()) {
      assertThat(terminalStatus.canTransitionTo(target)).isFalse();
    }
  }

  @ParameterizedTest
  @EnumSource(
      value = ApprovalStatus.class,
      names = {"APPROVED", "REJECTED", "CANCELLED"})
  void should_beTerminal_when_statusIsDecided(ApprovalStatus terminalStatus) {
    assertThat(terminalStatus.isTerminal()).isTrue();
  }
}
