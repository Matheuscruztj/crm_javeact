package com.atlasops.ai.domain;

import com.atlasops.shared.domain.Entity;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain entity representing a pending approval for a mutable action proposed by AI analysis.
 * Enforces the key invariant: NO mutable action (CREATE, UPDATE, DELETE) is executed without
 * explicit human approval.
 *
 * <p>When an AI analysis result proposes a mutable action, a PendingApproval is created with status
 * PENDING_APPROVAL. Only after a human user explicitly approves or rejects the action does the
 * status transition occur. Validates: Requirements 4.10
 */
public class PendingApproval extends Entity<String> {

  private final String analysisId;
  private final ActionType actionType;
  private final String targetResource;
  private final String payload;
  private ApprovalStatus status;
  private final String requestedBy;
  private String decidedBy;
  private Instant decidedAt;
  private final Instant createdAt;

  public PendingApproval(
      String id,
      String analysisId,
      ActionType actionType,
      String targetResource,
      String payload,
      ApprovalStatus status,
      String requestedBy,
      String decidedBy,
      Instant decidedAt,
      Instant createdAt) {
    super(id);

    Objects.requireNonNull(analysisId, "analysisId must not be null");
    Objects.requireNonNull(actionType, "actionType must not be null");
    Objects.requireNonNull(targetResource, "targetResource must not be null");
    Objects.requireNonNull(payload, "payload must not be null");
    Objects.requireNonNull(status, "status must not be null");
    Objects.requireNonNull(requestedBy, "requestedBy must not be null");
    Objects.requireNonNull(createdAt, "createdAt must not be null");

    if (analysisId.isBlank()) {
      throw new IllegalArgumentException("analysisId must not be blank");
    }
    if (targetResource.isBlank()) {
      throw new IllegalArgumentException("targetResource must not be blank");
    }
    if (payload.isBlank()) {
      throw new IllegalArgumentException("payload must not be blank");
    }
    if (requestedBy.isBlank()) {
      throw new IllegalArgumentException("requestedBy must not be blank");
    }

    this.analysisId = analysisId;
    this.actionType = actionType;
    this.targetResource = targetResource;
    this.payload = payload;
    this.status = status;
    this.requestedBy = requestedBy;
    this.decidedBy = decidedBy;
    this.decidedAt = decidedAt;
    this.createdAt = createdAt;
  }

  /**
   * Factory method to create a new PendingApproval from an analysis that proposes a mutable action.
   * The approval starts with status PENDING_APPROVAL — no mutation is executed until approved.
   *
   * @param id unique identifier for this approval
   * @param analysisId the analysis that originated this approval request
   * @param actionType the type of mutable action proposed (CREATE, UPDATE, DELETE)
   * @param targetResource the resource that would be affected by the action
   * @param payload JSON payload describing the proposed mutation
   * @param requestedBy the agent or analysis that requested this action
   * @param createdAt the timestamp when this approval was created
   * @return a new PendingApproval with status PENDING_APPROVAL
   */
  public static PendingApproval createFromAnalysis(
      String id,
      String analysisId,
      ActionType actionType,
      String targetResource,
      String payload,
      String requestedBy,
      Instant createdAt) {
    return new PendingApproval(
        id,
        analysisId,
        actionType,
        targetResource,
        payload,
        ApprovalStatus.PENDING_APPROVAL,
        requestedBy,
        null,
        null,
        createdAt);
  }

  /**
   * Approves this pending action. Only transitions from PENDING_APPROVAL to APPROVED.
   *
   * @param userId the human user who approved the action
   * @param decidedAt the timestamp of the approval decision
   * @throws IllegalStateException if the approval is not in PENDING_APPROVAL status
   */
  public void approve(String userId, Instant decidedAt) {
    Objects.requireNonNull(userId, "userId must not be null");
    Objects.requireNonNull(decidedAt, "decidedAt must not be null");
    if (userId.isBlank()) {
      throw new IllegalArgumentException("userId must not be blank");
    }
    if (this.status != ApprovalStatus.PENDING_APPROVAL) {
      throw new IllegalStateException(
          "Cannot approve: current status is " + this.status + ", expected PENDING_APPROVAL");
    }
    this.status = ApprovalStatus.APPROVED;
    this.decidedBy = userId;
    this.decidedAt = decidedAt;
  }

  /**
   * Rejects this pending action. Only transitions from PENDING_APPROVAL to REJECTED.
   *
   * @param userId the human user who rejected the action
   * @param decidedAt the timestamp of the rejection decision
   * @throws IllegalStateException if the approval is not in PENDING_APPROVAL status
   */
  public void reject(String userId, Instant decidedAt) {
    Objects.requireNonNull(userId, "userId must not be null");
    Objects.requireNonNull(decidedAt, "decidedAt must not be null");
    if (userId.isBlank()) {
      throw new IllegalArgumentException("userId must not be blank");
    }
    if (this.status != ApprovalStatus.PENDING_APPROVAL) {
      throw new IllegalStateException(
          "Cannot reject: current status is " + this.status + ", expected PENDING_APPROVAL");
    }
    this.status = ApprovalStatus.REJECTED;
    this.decidedBy = userId;
    this.decidedAt = decidedAt;
  }

  /**
   * Checks whether this approval has been decided (approved or rejected).
   *
   * @return true if the status is APPROVED or REJECTED
   */
  public boolean isDecided() {
    return this.status == ApprovalStatus.APPROVED || this.status == ApprovalStatus.REJECTED;
  }

  /**
   * Checks whether this approval is still pending human decision.
   *
   * @return true if the status is PENDING_APPROVAL
   */
  public boolean isPending() {
    return this.status == ApprovalStatus.PENDING_APPROVAL;
  }

  public String getAnalysisId() {
    return analysisId;
  }

  public ActionType getActionType() {
    return actionType;
  }

  public String getTargetResource() {
    return targetResource;
  }

  public String getPayload() {
    return payload;
  }

  public ApprovalStatus getStatus() {
    return status;
  }

  public String getRequestedBy() {
    return requestedBy;
  }

  public String getDecidedBy() {
    return decidedBy;
  }

  public Instant getDecidedAt() {
    return decidedAt;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  @Override
  public String toString() {
    return "PendingApproval{"
        + "id="
        + getId()
        + ", analysisId='"
        + analysisId
        + '\''
        + ", actionType="
        + actionType
        + ", targetResource='"
        + targetResource
        + '\''
        + ", status="
        + status
        + ", requestedBy='"
        + requestedBy
        + '\''
        + ", decidedBy='"
        + decidedBy
        + '\''
        + ", createdAt="
        + createdAt
        + '}';
  }
}
