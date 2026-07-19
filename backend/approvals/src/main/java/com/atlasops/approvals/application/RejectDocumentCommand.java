package com.atlasops.approvals.application;

import java.util.Objects;

/**
 * Command to reject a document. Transitions the approval from PENDING to REJECTED.
 *
 * @param approvalId the approval identifier
 * @param analystId the analyst making the rejection decision
 * @param reason the rejection reason (must be between 10 and 1000 characters)
 * @param role the role of the user (must be ANALYST or ADMIN)
 * @param tenantId the tenant identifier
 * @param correlationId correlation ID for tracing
 */
public record RejectDocumentCommand(
    String approvalId,
    String analystId,
    String reason,
    String role,
    String tenantId,
    String correlationId) {

  public RejectDocumentCommand {
    Objects.requireNonNull(approvalId, "approvalId must not be null");
    Objects.requireNonNull(analystId, "analystId must not be null");
    Objects.requireNonNull(reason, "reason must not be null");
    Objects.requireNonNull(role, "role must not be null");
    Objects.requireNonNull(tenantId, "tenantId must not be null");
  }
}
