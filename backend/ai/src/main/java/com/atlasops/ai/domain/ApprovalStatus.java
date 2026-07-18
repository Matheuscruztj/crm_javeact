package com.atlasops.ai.domain;

/**
 * Enum representing the lifecycle status of a PendingApproval. All mutable actions start as
 * PENDING_APPROVAL and transition to APPROVED or REJECTED only after explicit human decision.
 *
 * <p>Validates: Requirements 4.10
 */
public enum ApprovalStatus {
  PENDING_APPROVAL,
  APPROVED,
  REJECTED
}
