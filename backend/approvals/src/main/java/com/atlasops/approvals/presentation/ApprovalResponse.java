package com.atlasops.approvals.presentation;

import com.atlasops.approvals.domain.Approval;
import java.time.Instant;

/**
 * REST response representation for an approval.
 *
 * @param id the approval identifier
 * @param documentId the associated document identifier
 * @param status the current approval status
 * @param decisionBy the user who made the decision (null if pending)
 * @param rejectionReason the rejection reason (null if not rejected)
 * @param createdAt the creation timestamp
 * @param decidedAt the decision timestamp (null if pending)
 */
public record ApprovalResponse(
    String id,
    String documentId,
    String status,
    String decisionBy,
    String rejectionReason,
    Instant createdAt,
    Instant decidedAt) {

  /**
   * Creates an ApprovalResponse from a domain Approval.
   *
   * @param approval the domain approval
   * @return the response DTO
   */
  public static ApprovalResponse from(Approval approval) {
    return new ApprovalResponse(
        approval.getId(),
        approval.getDocumentId(),
        approval.getStatus().name(),
        approval.getDecisionBy(),
        approval.getRejectionReason(),
        approval.getCreatedAt(),
        approval.getDecidedAt());
  }
}
