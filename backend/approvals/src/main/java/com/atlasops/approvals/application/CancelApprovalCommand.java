package com.atlasops.approvals.application;

import java.util.Objects;

/**
 * Command to cancel an approval. Only ADMIN users can cancel. Transitions PENDING to CANCELLED.
 *
 * @param approvalId the approval identifier
 * @param adminId the admin cancelling the approval
 * @param role the role of the user (must be ADMIN)
 * @param tenantId the tenant identifier
 * @param correlationId correlation ID for tracing
 */
public record CancelApprovalCommand(
    String approvalId, String adminId, String role, String tenantId, String correlationId) {

  public CancelApprovalCommand {
    Objects.requireNonNull(approvalId, "approvalId must not be null");
    Objects.requireNonNull(adminId, "adminId must not be null");
    Objects.requireNonNull(role, "role must not be null");
    Objects.requireNonNull(tenantId, "tenantId must not be null");
  }
}
