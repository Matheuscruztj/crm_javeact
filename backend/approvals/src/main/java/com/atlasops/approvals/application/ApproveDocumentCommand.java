package com.atlasops.approvals.application;

import java.util.Objects;

/**
 * Command to approve a document. Transitions the approval from PENDING to APPROVED.
 *
 * @param approvalId the approval identifier
 * @param analystId the analyst making the approval decision
 * @param role the role of the user (must be ANALYST or ADMIN)
 * @param tenantId the tenant identifier
 * @param correlationId correlation ID for tracing
 */
public record ApproveDocumentCommand(
    String approvalId, String analystId, String role, String tenantId, String correlationId) {

  public ApproveDocumentCommand {
    Objects.requireNonNull(approvalId, "approvalId must not be null");
    Objects.requireNonNull(analystId, "analystId must not be null");
    Objects.requireNonNull(role, "role must not be null");
    Objects.requireNonNull(tenantId, "tenantId must not be null");
  }
}
