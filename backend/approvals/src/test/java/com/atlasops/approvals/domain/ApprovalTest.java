package com.atlasops.approvals.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.atlasops.shared.domain.DomainEvent;
import com.atlasops.shared.domain.events.ApprovalDecisionEvent;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the Approval aggregate root. Tests creation, validation, status transitions, and
 * immutability.
 */
class ApprovalTest {

  private static final String VALID_ID = "approval-001";
  private static final String VALID_TENANT_ID = "tenant-alpha";
  private static final String VALID_DOCUMENT_ID = "doc-001";
  private static final String ANALYST_ID = "analyst-001";
  private static final String ADMIN_ID = "admin-001";
  private static final String CORRELATION_ID = "corr-123";
  private static final Instant FIXED_NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final Instant DECISION_TIME = Instant.parse("2025-01-15T11:00:00Z");
  private static final String VALID_REJECTION_REASON =
      "The document contains incorrect data and needs revision";

  // --- Creation Tests ---

  @Test
  void should_createPendingApproval_when_allFieldsValid() {
    Approval approval =
        Approval.createPending(VALID_ID, VALID_TENANT_ID, VALID_DOCUMENT_ID, FIXED_NOW);

    assertThat(approval.getId()).isEqualTo(VALID_ID);
    assertThat(approval.getTenantId()).isEqualTo(VALID_TENANT_ID);
    assertThat(approval.getDocumentId()).isEqualTo(VALID_DOCUMENT_ID);
    assertThat(approval.getStatus()).isEqualTo(ApprovalStatus.PENDING);
    assertThat(approval.getDecisionBy()).isNull();
    assertThat(approval.getRejectionReason()).isNull();
    assertThat(approval.getCreatedAt()).isEqualTo(FIXED_NOW);
    assertThat(approval.getDecidedAt()).isNull();
  }

  @Test
  void should_rejectCreation_when_idIsNull() {
    assertThatThrownBy(
            () -> Approval.createPending(null, VALID_TENANT_ID, VALID_DOCUMENT_ID, FIXED_NOW))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Id");
  }

  @Test
  void should_rejectCreation_when_tenantIdIsNull() {
    assertThatThrownBy(() -> Approval.createPending(VALID_ID, null, VALID_DOCUMENT_ID, FIXED_NOW))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("TenantId");
  }

  @Test
  void should_rejectCreation_when_documentIdIsNull() {
    assertThatThrownBy(() -> Approval.createPending(VALID_ID, VALID_TENANT_ID, null, FIXED_NOW))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("DocumentId");
  }

  @Test
  void should_rejectCreation_when_timestampIsNull() {
    assertThatThrownBy(
            () -> Approval.createPending(VALID_ID, VALID_TENANT_ID, VALID_DOCUMENT_ID, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Timestamp");
  }

  // --- Approve Tests ---

  @Test
  void should_transitionToApproved_when_approveCalled() {
    Approval approval = createPendingApproval();

    approval.approve(ANALYST_ID, CORRELATION_ID, DECISION_TIME);

    assertThat(approval.getStatus()).isEqualTo(ApprovalStatus.APPROVED);
    assertThat(approval.getDecisionBy()).isEqualTo(ANALYST_ID);
    assertThat(approval.getDecidedAt()).isEqualTo(DECISION_TIME);
    assertThat(approval.getRejectionReason()).isNull();
  }

  @Test
  void should_publishApprovalDecisionEvent_when_approveCalled() {
    Approval approval = createPendingApproval();

    approval.approve(ANALYST_ID, CORRELATION_ID, DECISION_TIME);

    List<DomainEvent> events = approval.getDomainEvents();
    assertThat(events).hasSize(1);
    assertThat(events.get(0)).isInstanceOf(ApprovalDecisionEvent.class);

    ApprovalDecisionEvent event = (ApprovalDecisionEvent) events.get(0);
    assertThat(event.getDocumentId()).isEqualTo(VALID_DOCUMENT_ID);
    assertThat(event.getDecision()).isEqualTo("APPROVED");
    assertThat(event.getAnalystId()).isEqualTo(ANALYST_ID);
    assertThat(event.getDecisionTimestamp()).isEqualTo(DECISION_TIME);
    assertThat(event.getTenantId()).isEqualTo(VALID_TENANT_ID);
    assertThat(event.getCorrelationId()).isEqualTo(CORRELATION_ID);
  }

  @Test
  void should_rejectApproval_when_analystIdIsNull() {
    Approval approval = createPendingApproval();

    assertThatThrownBy(() -> approval.approve(null, CORRELATION_ID, DECISION_TIME))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("AnalystId");
  }

  @Test
  void should_rejectApproval_when_timestampIsNull() {
    Approval approval = createPendingApproval();

    assertThatThrownBy(() -> approval.approve(ANALYST_ID, CORRELATION_ID, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Timestamp");
  }

  // --- Reject Tests ---

  @Test
  void should_transitionToRejected_when_rejectCalled() {
    Approval approval = createPendingApproval();

    approval.reject(ANALYST_ID, VALID_REJECTION_REASON, CORRELATION_ID, DECISION_TIME);

    assertThat(approval.getStatus()).isEqualTo(ApprovalStatus.REJECTED);
    assertThat(approval.getDecisionBy()).isEqualTo(ANALYST_ID);
    assertThat(approval.getRejectionReason()).isEqualTo(VALID_REJECTION_REASON);
    assertThat(approval.getDecidedAt()).isEqualTo(DECISION_TIME);
  }

  @Test
  void should_publishApprovalDecisionEvent_when_rejectCalled() {
    Approval approval = createPendingApproval();

    approval.reject(ANALYST_ID, VALID_REJECTION_REASON, CORRELATION_ID, DECISION_TIME);

    List<DomainEvent> events = approval.getDomainEvents();
    assertThat(events).hasSize(1);
    assertThat(events.get(0)).isInstanceOf(ApprovalDecisionEvent.class);

    ApprovalDecisionEvent event = (ApprovalDecisionEvent) events.get(0);
    assertThat(event.getDocumentId()).isEqualTo(VALID_DOCUMENT_ID);
    assertThat(event.getDecision()).isEqualTo("REJECTED");
    assertThat(event.getAnalystId()).isEqualTo(ANALYST_ID);
    assertThat(event.getDecisionTimestamp()).isEqualTo(DECISION_TIME);
    assertThat(event.getTenantId()).isEqualTo(VALID_TENANT_ID);
    assertThat(event.getCorrelationId()).isEqualTo(CORRELATION_ID);
  }

  @Test
  void should_rejectRejection_when_reasonIsNull() {
    Approval approval = createPendingApproval();

    assertThatThrownBy(() -> approval.reject(ANALYST_ID, null, CORRELATION_ID, DECISION_TIME))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Rejection reason");
  }

  @Test
  void should_rejectRejection_when_reasonTooShort() {
    Approval approval = createPendingApproval();
    String shortReason = "short";

    assertThatThrownBy(
            () -> approval.reject(ANALYST_ID, shortReason, CORRELATION_ID, DECISION_TIME))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("10")
        .hasMessageContaining("1000");
  }

  @Test
  void should_rejectRejection_when_reasonTooLong() {
    Approval approval = createPendingApproval();
    String longReason = "a".repeat(1001);

    assertThatThrownBy(() -> approval.reject(ANALYST_ID, longReason, CORRELATION_ID, DECISION_TIME))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("10")
        .hasMessageContaining("1000");
  }

  @Test
  void should_acceptRejection_when_reasonExactlyMinLength() {
    Approval approval = createPendingApproval();
    String minReason = "a".repeat(10);

    approval.reject(ANALYST_ID, minReason, CORRELATION_ID, DECISION_TIME);

    assertThat(approval.getStatus()).isEqualTo(ApprovalStatus.REJECTED);
    assertThat(approval.getRejectionReason()).isEqualTo(minReason);
  }

  @Test
  void should_acceptRejection_when_reasonExactlyMaxLength() {
    Approval approval = createPendingApproval();
    String maxReason = "a".repeat(1000);

    approval.reject(ANALYST_ID, maxReason, CORRELATION_ID, DECISION_TIME);

    assertThat(approval.getStatus()).isEqualTo(ApprovalStatus.REJECTED);
    assertThat(approval.getRejectionReason()).isEqualTo(maxReason);
  }

  @Test
  void should_rejectRejection_when_analystIdIsNull() {
    Approval approval = createPendingApproval();

    assertThatThrownBy(
            () -> approval.reject(null, VALID_REJECTION_REASON, CORRELATION_ID, DECISION_TIME))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("AnalystId");
  }

  @Test
  void should_rejectRejection_when_timestampIsNull() {
    Approval approval = createPendingApproval();

    assertThatThrownBy(
            () -> approval.reject(ANALYST_ID, VALID_REJECTION_REASON, CORRELATION_ID, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Timestamp");
  }

  // --- Cancel Tests ---

  @Test
  void should_transitionToCancelled_when_cancelCalled() {
    Approval approval = createPendingApproval();

    approval.cancel(ADMIN_ID, CORRELATION_ID, DECISION_TIME);

    assertThat(approval.getStatus()).isEqualTo(ApprovalStatus.CANCELLED);
    assertThat(approval.getDecisionBy()).isEqualTo(ADMIN_ID);
    assertThat(approval.getDecidedAt()).isEqualTo(DECISION_TIME);
  }

  @Test
  void should_publishApprovalDecisionEvent_when_cancelCalled() {
    Approval approval = createPendingApproval();

    approval.cancel(ADMIN_ID, CORRELATION_ID, DECISION_TIME);

    List<DomainEvent> events = approval.getDomainEvents();
    assertThat(events).hasSize(1);
    assertThat(events.get(0)).isInstanceOf(ApprovalDecisionEvent.class);

    ApprovalDecisionEvent event = (ApprovalDecisionEvent) events.get(0);
    assertThat(event.getDocumentId()).isEqualTo(VALID_DOCUMENT_ID);
    assertThat(event.getDecision()).isEqualTo("CANCELLED");
    assertThat(event.getAnalystId()).isEqualTo(ADMIN_ID);
    assertThat(event.getDecisionTimestamp()).isEqualTo(DECISION_TIME);
    assertThat(event.getTenantId()).isEqualTo(VALID_TENANT_ID);
    assertThat(event.getCorrelationId()).isEqualTo(CORRELATION_ID);
  }

  @Test
  void should_rejectCancellation_when_adminIdIsNull() {
    Approval approval = createPendingApproval();

    assertThatThrownBy(() -> approval.cancel(null, CORRELATION_ID, DECISION_TIME))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("AdminId");
  }

  @Test
  void should_rejectCancellation_when_timestampIsNull() {
    Approval approval = createPendingApproval();

    assertThatThrownBy(() -> approval.cancel(ADMIN_ID, CORRELATION_ID, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Timestamp");
  }

  // --- Immutability Tests (once decided, no further transitions) ---

  @Test
  void should_rejectApproval_when_alreadyApproved() {
    Approval approval = createApprovedApproval();

    assertThatThrownBy(() -> approval.approve(ANALYST_ID, CORRELATION_ID, DECISION_TIME))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot transition from APPROVED")
        .hasMessageContaining("immutable");
  }

  @Test
  void should_rejectRejection_when_alreadyApproved() {
    Approval approval = createApprovedApproval();

    assertThatThrownBy(
            () ->
                approval.reject(ANALYST_ID, VALID_REJECTION_REASON, CORRELATION_ID, DECISION_TIME))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot transition from APPROVED")
        .hasMessageContaining("immutable");
  }

  @Test
  void should_rejectCancellation_when_alreadyApproved() {
    Approval approval = createApprovedApproval();

    assertThatThrownBy(() -> approval.cancel(ADMIN_ID, CORRELATION_ID, DECISION_TIME))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot transition from APPROVED")
        .hasMessageContaining("immutable");
  }

  @Test
  void should_rejectApproval_when_alreadyRejected() {
    Approval approval = createRejectedApproval();

    assertThatThrownBy(() -> approval.approve(ANALYST_ID, CORRELATION_ID, DECISION_TIME))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot transition from REJECTED")
        .hasMessageContaining("immutable");
  }

  @Test
  void should_rejectRejection_when_alreadyRejected() {
    Approval approval = createRejectedApproval();

    assertThatThrownBy(
            () ->
                approval.reject(ANALYST_ID, VALID_REJECTION_REASON, CORRELATION_ID, DECISION_TIME))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot transition from REJECTED")
        .hasMessageContaining("immutable");
  }

  @Test
  void should_rejectCancellation_when_alreadyRejected() {
    Approval approval = createRejectedApproval();

    assertThatThrownBy(() -> approval.cancel(ADMIN_ID, CORRELATION_ID, DECISION_TIME))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot transition from REJECTED")
        .hasMessageContaining("immutable");
  }

  @Test
  void should_rejectApproval_when_alreadyCancelled() {
    Approval approval = createCancelledApproval();

    assertThatThrownBy(() -> approval.approve(ANALYST_ID, CORRELATION_ID, DECISION_TIME))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot transition from CANCELLED")
        .hasMessageContaining("immutable");
  }

  @Test
  void should_rejectRejection_when_alreadyCancelled() {
    Approval approval = createCancelledApproval();

    assertThatThrownBy(
            () ->
                approval.reject(ANALYST_ID, VALID_REJECTION_REASON, CORRELATION_ID, DECISION_TIME))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot transition from CANCELLED")
        .hasMessageContaining("immutable");
  }

  @Test
  void should_rejectCancellation_when_alreadyCancelled() {
    Approval approval = createCancelledApproval();

    assertThatThrownBy(() -> approval.cancel(ADMIN_ID, CORRELATION_ID, DECISION_TIME))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Cannot transition from CANCELLED")
        .hasMessageContaining("immutable");
  }

  // --- Reconstitute Tests ---

  @Test
  void should_reconstituteApproval_when_allFieldsProvided() {
    Approval approval =
        Approval.reconstitute(
            VALID_ID,
            VALID_TENANT_ID,
            VALID_DOCUMENT_ID,
            ApprovalStatus.APPROVED,
            ANALYST_ID,
            null,
            FIXED_NOW,
            DECISION_TIME);

    assertThat(approval.getId()).isEqualTo(VALID_ID);
    assertThat(approval.getTenantId()).isEqualTo(VALID_TENANT_ID);
    assertThat(approval.getDocumentId()).isEqualTo(VALID_DOCUMENT_ID);
    assertThat(approval.getStatus()).isEqualTo(ApprovalStatus.APPROVED);
    assertThat(approval.getDecisionBy()).isEqualTo(ANALYST_ID);
    assertThat(approval.getRejectionReason()).isNull();
    assertThat(approval.getCreatedAt()).isEqualTo(FIXED_NOW);
    assertThat(approval.getDecidedAt()).isEqualTo(DECISION_TIME);
  }

  @Test
  void should_reconstituteRejectedApproval_when_rejectionReasonProvided() {
    Approval approval =
        Approval.reconstitute(
            VALID_ID,
            VALID_TENANT_ID,
            VALID_DOCUMENT_ID,
            ApprovalStatus.REJECTED,
            ANALYST_ID,
            VALID_REJECTION_REASON,
            FIXED_NOW,
            DECISION_TIME);

    assertThat(approval.getStatus()).isEqualTo(ApprovalStatus.REJECTED);
    assertThat(approval.getRejectionReason()).isEqualTo(VALID_REJECTION_REASON);
  }

  // --- Helper Methods ---

  private Approval createPendingApproval() {
    return Approval.createPending(VALID_ID, VALID_TENANT_ID, VALID_DOCUMENT_ID, FIXED_NOW);
  }

  private Approval createApprovedApproval() {
    Approval approval = createPendingApproval();
    approval.approve(ANALYST_ID, CORRELATION_ID, DECISION_TIME);
    return approval;
  }

  private Approval createRejectedApproval() {
    Approval approval = createPendingApproval();
    approval.reject(ANALYST_ID, VALID_REJECTION_REASON, CORRELATION_ID, DECISION_TIME);
    return approval;
  }

  private Approval createCancelledApproval() {
    Approval approval = createPendingApproval();
    approval.cancel(ADMIN_ID, CORRELATION_ID, DECISION_TIME);
    return approval;
  }
}
