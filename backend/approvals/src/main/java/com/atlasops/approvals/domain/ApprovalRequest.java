package com.atlasops.approvals.domain;

/**
 * Represents a request to submit an approval decision.
 *
 * @param requestId the unique identifier of the approval request
 * @param approverId the identifier of the user performing the approval
 * @param decision the approval decision (e.g., APPROVED, REJECTED)
 */
public record ApprovalRequest(String requestId, String approverId, String decision) {}
