package com.atlasops.audit.presentation;

import com.atlasops.audit.domain.AuditEntry;
import java.time.Instant;

/**
 * REST response representation for an audit entry.
 *
 * @param id the audit entry identifier
 * @param actionType the type of action performed
 * @param actorId the identifier of the user who performed the action
 * @param entityType the type of entity affected
 * @param entityId the identifier of the affected entity
 * @param correlationId the correlation ID for distributed tracing
 * @param details JSON string with action details
 * @param timestamp when the action occurred
 */
public record AuditEntryResponse(
    String id,
    String actionType,
    String actorId,
    String entityType,
    String entityId,
    String correlationId,
    String details,
    Instant timestamp) {

  /**
   * Creates an AuditEntryResponse from a domain AuditEntry.
   *
   * @param auditEntry the domain audit entry
   * @return the response DTO
   */
  public static AuditEntryResponse from(AuditEntry auditEntry) {
    return new AuditEntryResponse(
        auditEntry.getId(),
        auditEntry.getActionType(),
        auditEntry.getActorId(),
        auditEntry.getEntityType(),
        auditEntry.getEntityId(),
        auditEntry.getCorrelationId(),
        auditEntry.getDetails(),
        auditEntry.getTimestamp());
  }
}
