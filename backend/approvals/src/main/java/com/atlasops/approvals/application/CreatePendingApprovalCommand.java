package com.atlasops.approvals.application;

import java.util.Objects;

/**
 * Command to create a pending approval when a document reaches ANALYZED status.
 *
 * @param documentId the document identifier that was analyzed
 * @param tenantId the tenant this approval belongs to
 */
public record CreatePendingApprovalCommand(String documentId, String tenantId) {

  public CreatePendingApprovalCommand {
    Objects.requireNonNull(documentId, "documentId must not be null");
    Objects.requireNonNull(tenantId, "tenantId must not be null");
  }
}
