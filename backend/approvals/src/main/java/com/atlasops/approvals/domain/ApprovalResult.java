package com.atlasops.approvals.domain;

/**
 * Represents the result of an approval submission.
 *
 * @param approvalId the unique identifier assigned to the approval
 * @param status the resulting status after processing the approval
 * @param timestamp the ISO-8601 timestamp of when the approval was processed
 */
public record ApprovalResult(String approvalId, String status, String timestamp) {}
