package com.atlasops.approvals.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import net.jqwik.api.*;

/**
 * Property-based test for Approval State Machine Immutability.
 *
 * <p><b>Validates: Requirements 13.7</b>
 *
 * <p>Property 19: Approval State Machine Immutability
 *
 * <p>Requirement 13.7: IF an approval or rejection is attempted on an approval record that is not
 * in PENDING status, THEN THE Approval_Module SHALL reject the operation and return a 422 error
 * indicating that the decision is immutable.
 *
 * <p>This test verifies that for ANY approval in a terminal state (APPROVED, REJECTED, CANCELLED),
 * ANY operation (approve, reject, cancel) throws IllegalStateException. Only PENDING approvals can
 * transition to another state. Once a decision is made, it cannot be changed regardless of
 * attempts.
 */
@Tag("Feature: project-implementation-kickoff")
@Tag("Property 19: Approval State Machine Immutability")
class ApprovalStateMachineImmutabilityPropertyTest {

  private static final Instant CREATION_TIME = Instant.parse("2025-01-15T10:00:00Z");
  private static final Instant DECISION_TIME = Instant.parse("2025-01-15T11:00:00Z");
  private static final Instant RETRY_TIME = Instant.parse("2025-01-15T12:00:00Z");
  private static final String CORRELATION_ID = "corr-pbt-001";

  // ─── Property Tests ──────────────────────────────────────────────────────────

  /**
   * Property: For ANY approval in a terminal state and ANY operation attempted, the operation SHALL
   * throw IllegalStateException, enforcing immutability.
   *
   * <p>Validates: Requirement 13.7
   */
  @Property(tries = 100)
  void should_alwaysThrowIllegalStateException_forAnyOperationOnTerminalApproval(
      @ForAll("terminalApprovals") Approval terminalApproval,
      @ForAll("validAnalystIds") String analystId,
      @ForAll("validRejectionReasons") String rejectionReason,
      @ForAll("approvalOperations") ApprovalOperation operation) {

    // Capture original state before attempted mutation
    ApprovalStatus originalStatus = terminalApproval.getStatus();
    String originalDecisionBy = terminalApproval.getDecisionBy();
    Instant originalDecidedAt = terminalApproval.getDecidedAt();

    // Act & Assert: any operation on a terminal approval must throw
    assertThatThrownBy(
            () -> operation.execute(terminalApproval, analystId, rejectionReason, RETRY_TIME))
        .as(
            "Operation '%s' on %s approval must throw IllegalStateException",
            operation.name(), originalStatus)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("immutable");

    // Assert: the state was NOT mutated (immutability preserved)
    assertThat(terminalApproval.getStatus())
        .as("Status must remain unchanged after failed operation")
        .isEqualTo(originalStatus);
    assertThat(terminalApproval.getDecisionBy())
        .as("DecisionBy must remain unchanged after failed operation")
        .isEqualTo(originalDecisionBy);
    assertThat(terminalApproval.getDecidedAt())
        .as("DecidedAt must remain unchanged after failed operation")
        .isEqualTo(originalDecidedAt);
  }

  /**
   * Property: Only PENDING approvals can transition to another state. For ANY PENDING approval and
   * ANY valid target transition, the operation succeeds.
   *
   * <p>Validates: Requirement 13.7 (positive complement — only PENDING can transition)
   */
  @Property(tries = 100)
  void should_alwaysAllowTransition_forAnyOperationOnPendingApproval(
      @ForAll("pendingApprovals") Approval pendingApproval,
      @ForAll("validAnalystIds") String analystId,
      @ForAll("validRejectionReasons") String rejectionReason,
      @ForAll("approvalOperations") ApprovalOperation operation) {

    // Act: operation on PENDING approval must NOT throw
    operation.execute(pendingApproval, analystId, rejectionReason, DECISION_TIME);

    // Assert: status transitioned away from PENDING
    assertThat(pendingApproval.getStatus())
        .as("PENDING approval must transition to a terminal state after operation")
        .isNotEqualTo(ApprovalStatus.PENDING);

    // Assert: the new status is a terminal state
    assertThat(pendingApproval.getStatus().isTerminal())
        .as("After operation, approval must be in a terminal state")
        .isTrue();
  }

  /**
   * Property: Terminal states are truly terminal — no transitions are valid from them.
   *
   * <p>Validates: Requirement 13.7
   */
  @Property(tries = 100)
  void should_alwaysReportNoValidTransitions_forAnyTerminalState(
      @ForAll("terminalStatuses") ApprovalStatus terminalStatus) {

    // Assert: terminal states cannot transition to any other state
    for (ApprovalStatus target : ApprovalStatus.values()) {
      assertThat(terminalStatus.canTransitionTo(target))
          .as("Terminal state %s must not allow transition to %s", terminalStatus, target)
          .isFalse();
    }

    // Assert: isTerminal() returns true
    assertThat(terminalStatus.isTerminal())
        .as("Status %s must be reported as terminal", terminalStatus)
        .isTrue();
  }

  // ─── Generators ──────────────────────────────────────────────────────────────

  @Provide
  Arbitrary<ApprovalStatus> terminalStatuses() {
    return Arbitraries.of(
        ApprovalStatus.APPROVED, ApprovalStatus.REJECTED, ApprovalStatus.CANCELLED);
  }

  @Provide
  Arbitrary<Approval> terminalApprovals() {
    return Combinators.combine(
            validApprovalIds(),
            validTenantIds(),
            validDocumentIds(),
            terminalStatuses(),
            validAnalystIds())
        .as(
            (id, tenantId, documentId, status, decisionBy) -> {
              String rejectionReason =
                  status == ApprovalStatus.REJECTED
                      ? "This document was rejected due to compliance issues"
                      : null;
              return Approval.reconstitute(
                  id,
                  tenantId,
                  documentId,
                  status,
                  decisionBy,
                  rejectionReason,
                  CREATION_TIME,
                  DECISION_TIME);
            });
  }

  @Provide
  Arbitrary<Approval> pendingApprovals() {
    return Combinators.combine(validApprovalIds(), validTenantIds(), validDocumentIds())
        .as(
            (id, tenantId, documentId) ->
                Approval.createPending(id, tenantId, documentId, CREATION_TIME));
  }

  @Provide
  Arbitrary<String> validAnalystIds() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .ofMinLength(5)
        .ofMaxLength(20)
        .map(s -> "analyst-" + s);
  }

  @Provide
  Arbitrary<String> validRejectionReasons() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .withChars(' ', '.', ',')
        .ofMinLength(10)
        .ofMaxLength(200)
        .filter(s -> !s.isBlank() && s.length() >= 10);
  }

  @Provide
  Arbitrary<ApprovalOperation> approvalOperations() {
    return Arbitraries.of(ApprovalOperation.values());
  }

  private Arbitrary<String> validApprovalIds() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .numeric()
        .ofMinLength(5)
        .ofMaxLength(15)
        .map(s -> "approval-" + s);
  }

  private Arbitrary<String> validTenantIds() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .numeric()
        .ofMinLength(3)
        .ofMaxLength(10)
        .map(s -> "tenant-" + s);
  }

  private Arbitrary<String> validDocumentIds() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .numeric()
        .ofMinLength(5)
        .ofMaxLength(15)
        .map(s -> "doc-" + s);
  }

  // ─── Helper Types ────────────────────────────────────────────────────────────

  /** Represents the three possible operations that can be attempted on an Approval. */
  enum ApprovalOperation {
    APPROVE {
      @Override
      void execute(Approval approval, String actorId, String reason, Instant now) {
        approval.approve(actorId, "corr-pbt-retry", now);
      }
    },
    REJECT {
      @Override
      void execute(Approval approval, String actorId, String reason, Instant now) {
        approval.reject(actorId, reason, "corr-pbt-retry", now);
      }
    },
    CANCEL {
      @Override
      void execute(Approval approval, String actorId, String reason, Instant now) {
        approval.cancel(actorId, "corr-pbt-retry", now);
      }
    };

    abstract void execute(Approval approval, String actorId, String reason, Instant now);
  }
}
