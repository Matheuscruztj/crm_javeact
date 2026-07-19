package com.atlasops.approvals.domain;

import com.atlasops.shared.domain.AggregateRoot;
import com.atlasops.shared.domain.events.ApprovalDecisionEvent;
import java.time.Instant;
import java.util.Objects;

/**
 * Approval aggregate root representing a document approval workflow item. Once a decision is made
 * (APPROVED, REJECTED, CANCELLED), the approval is immutable.
 */
public final class Approval extends AggregateRoot<String> {

  private static final int REJECTION_REASON_MIN_LENGTH = 10;
  private static final int REJECTION_REASON_MAX_LENGTH = 1000;

  private final String tenantId;
  private final String documentId;
  private ApprovalStatus status;
  private String decisionBy;
  private String rejectionReason;
  private final Instant createdAt;
  private Instant decidedAt;

  private Approval(
      String id,
      String tenantId,
      String documentId,
      ApprovalStatus status,
      String decisionBy,
      String rejectionReason,
      Instant createdAt,
      Instant decidedAt) {
    super(id);
    this.tenantId = Objects.requireNonNull(tenantId, "TenantId must not be null");
    this.documentId = Objects.requireNonNull(documentId, "DocumentId must not be null");
    this.status = Objects.requireNonNull(status, "Status must not be null");
    this.decisionBy = decisionBy;
    this.rejectionReason = rejectionReason;
    this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt must not be null");
    this.decidedAt = decidedAt;
  }

  /**
   * Factory method to create a new Approval in PENDING status.
   *
   * @param id unique identifier
   * @param tenantId tenant this approval belongs to
   * @param documentId the document being approved
   * @param now current timestamp
   * @return a new Approval instance with PENDING status
   */
  public static Approval createPending(String id, String tenantId, String documentId, Instant now) {
    Objects.requireNonNull(id, "Id must not be null");
    Objects.requireNonNull(tenantId, "TenantId must not be null");
    Objects.requireNonNull(documentId, "DocumentId must not be null");
    Objects.requireNonNull(now, "Timestamp must not be null");

    return new Approval(id, tenantId, documentId, ApprovalStatus.PENDING, null, null, now, null);
  }

  /** Reconstitutes an Approval from persisted data. */
  public static Approval reconstitute(
      String id,
      String tenantId,
      String documentId,
      ApprovalStatus status,
      String decisionBy,
      String rejectionReason,
      Instant createdAt,
      Instant decidedAt) {
    return new Approval(
        id, tenantId, documentId, status, decisionBy, rejectionReason, createdAt, decidedAt);
  }

  /**
   * Approves the document. Transitions from PENDING to APPROVED.
   *
   * @param analystId the analyst making the decision
   * @param correlationId correlation ID for tracing
   * @param now current timestamp
   * @throws IllegalStateException if the approval is not in PENDING status
   */
  public void approve(String analystId, String correlationId, Instant now) {
    Objects.requireNonNull(analystId, "AnalystId must not be null");
    Objects.requireNonNull(now, "Timestamp must not be null");
    assertTransition(ApprovalStatus.APPROVED);

    this.status = ApprovalStatus.APPROVED;
    this.decisionBy = analystId;
    this.decidedAt = now;

    registerEvent(
        new ApprovalDecisionEvent(documentId, "APPROVED", analystId, now, tenantId, correlationId));
  }

  /**
   * Rejects the document. Transitions from PENDING to REJECTED.
   *
   * @param analystId the analyst making the decision
   * @param reason the rejection reason (10-1000 characters)
   * @param correlationId correlation ID for tracing
   * @param now current timestamp
   * @throws IllegalStateException if the approval is not in PENDING status
   * @throws IllegalArgumentException if the reason is not between 10 and 1000 characters
   */
  public void reject(String analystId, String reason, String correlationId, Instant now) {
    Objects.requireNonNull(analystId, "AnalystId must not be null");
    Objects.requireNonNull(reason, "Rejection reason must not be null");
    Objects.requireNonNull(now, "Timestamp must not be null");
    validateRejectionReason(reason);
    assertTransition(ApprovalStatus.REJECTED);

    this.status = ApprovalStatus.REJECTED;
    this.decisionBy = analystId;
    this.rejectionReason = reason;
    this.decidedAt = now;

    registerEvent(
        new ApprovalDecisionEvent(documentId, "REJECTED", analystId, now, tenantId, correlationId));
  }

  /**
   * Cancels the approval. Transitions from PENDING to CANCELLED. ADMIN only.
   *
   * @param adminId the admin cancelling the approval
   * @param correlationId correlation ID for tracing
   * @param now current timestamp
   * @throws IllegalStateException if the approval is not in PENDING status
   */
  public void cancel(String adminId, String correlationId, Instant now) {
    Objects.requireNonNull(adminId, "AdminId must not be null");
    Objects.requireNonNull(now, "Timestamp must not be null");
    assertTransition(ApprovalStatus.CANCELLED);

    this.status = ApprovalStatus.CANCELLED;
    this.decisionBy = adminId;
    this.decidedAt = now;

    registerEvent(
        new ApprovalDecisionEvent(documentId, "CANCELLED", adminId, now, tenantId, correlationId));
  }

  private void assertTransition(ApprovalStatus target) {
    if (!this.status.canTransitionTo(target)) {
      throw new IllegalStateException(
          "Cannot transition from "
              + this.status
              + " to "
              + target
              + ". Approval decisions are immutable once made.");
    }
  }

  private void validateRejectionReason(String reason) {
    if (reason.length() < REJECTION_REASON_MIN_LENGTH
        || reason.length() > REJECTION_REASON_MAX_LENGTH) {
      throw new IllegalArgumentException(
          "Rejection reason must be between "
              + REJECTION_REASON_MIN_LENGTH
              + " and "
              + REJECTION_REASON_MAX_LENGTH
              + " characters, got: "
              + reason.length());
    }
  }

  // --- Getters ---

  public String getTenantId() {
    return tenantId;
  }

  public String getDocumentId() {
    return documentId;
  }

  public ApprovalStatus getStatus() {
    return status;
  }

  public String getDecisionBy() {
    return decisionBy;
  }

  public String getRejectionReason() {
    return rejectionReason;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getDecidedAt() {
    return decidedAt;
  }
}
