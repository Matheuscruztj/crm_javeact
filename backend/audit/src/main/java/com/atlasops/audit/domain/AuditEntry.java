package com.atlasops.audit.domain;

import com.atlasops.shared.domain.Entity;
import java.time.Instant;
import java.util.Objects;

/**
 * Immutable entity representing a single audit log entry. Once created, an AuditEntry cannot be
 * modified or deleted (append-only ledger). The details field stores arbitrary JSON with a maximum
 * size of 10 KB (10240 characters).
 */
public final class AuditEntry extends Entity<String> {

  private static final int DETAILS_MAX_LENGTH = 10240;

  private final String actionType;
  private final String actorId;
  private final String tenantId;
  private final String entityType;
  private final String entityId;
  private final String correlationId;
  private final String details;
  private final Instant timestamp;

  private AuditEntry(
      String id,
      String actionType,
      String actorId,
      String tenantId,
      String entityType,
      String entityId,
      String correlationId,
      String details,
      Instant timestamp) {
    super(id);
    this.actionType = Objects.requireNonNull(actionType, "ActionType must not be null");
    this.actorId = Objects.requireNonNull(actorId, "ActorId must not be null");
    this.tenantId = Objects.requireNonNull(tenantId, "TenantId must not be null");
    this.entityType = Objects.requireNonNull(entityType, "EntityType must not be null");
    this.entityId = Objects.requireNonNull(entityId, "EntityId must not be null");
    this.correlationId = Objects.requireNonNull(correlationId, "CorrelationId must not be null");
    this.details = Objects.requireNonNull(details, "Details must not be null");
    this.timestamp = Objects.requireNonNull(timestamp, "Timestamp must not be null");

    validateActionType(actionType);
    validateActorId(actorId);
    validateTenantId(tenantId);
    validateEntityType(entityType);
    validateEntityId(entityId);
    validateCorrelationId(correlationId);
    validateDetails(details);
  }

  /**
   * Factory method to create a new AuditEntry.
   *
   * @param id unique identifier for the audit entry
   * @param actionType the type of action performed (e.g., LOGIN, CREATE_CUSTOMER)
   * @param actorId the identifier of the user who performed the action
   * @param tenantId the tenant this entry belongs to
   * @param entityType the type of entity affected (e.g., CUSTOMER, DOCUMENT)
   * @param entityId the identifier of the affected entity
   * @param correlationId the correlation ID for distributed tracing
   * @param details JSON string with action details (max 10 KB)
   * @param timestamp when the action occurred
   * @return a new immutable AuditEntry instance
   */
  public static AuditEntry create(
      String id,
      String actionType,
      String actorId,
      String tenantId,
      String entityType,
      String entityId,
      String correlationId,
      String details,
      Instant timestamp) {
    return new AuditEntry(
        id, actionType, actorId, tenantId, entityType, entityId, correlationId, details, timestamp);
  }

  /** Reconstitutes an AuditEntry from persisted data. */
  public static AuditEntry reconstitute(
      String id,
      String actionType,
      String actorId,
      String tenantId,
      String entityType,
      String entityId,
      String correlationId,
      String details,
      Instant timestamp) {
    return new AuditEntry(
        id, actionType, actorId, tenantId, entityType, entityId, correlationId, details, timestamp);
  }

  private void validateActionType(String actionType) {
    if (actionType.isBlank()) {
      throw new IllegalArgumentException("ActionType must not be blank");
    }
  }

  private void validateActorId(String actorId) {
    if (actorId.isBlank()) {
      throw new IllegalArgumentException("ActorId must not be blank");
    }
  }

  private void validateTenantId(String tenantId) {
    if (tenantId.isBlank()) {
      throw new IllegalArgumentException("TenantId must not be blank");
    }
  }

  private void validateEntityType(String entityType) {
    if (entityType.isBlank()) {
      throw new IllegalArgumentException("EntityType must not be blank");
    }
  }

  private void validateEntityId(String entityId) {
    if (entityId.isBlank()) {
      throw new IllegalArgumentException("EntityId must not be blank");
    }
  }

  private void validateCorrelationId(String correlationId) {
    if (correlationId.isBlank()) {
      throw new IllegalArgumentException("CorrelationId must not be blank");
    }
  }

  private void validateDetails(String details) {
    if (details.length() > DETAILS_MAX_LENGTH) {
      throw new IllegalArgumentException(
          "Details must not exceed "
              + DETAILS_MAX_LENGTH
              + " characters, got: "
              + details.length());
    }
  }

  // --- Getters (no setters — immutable by design) ---

  public String getActionType() {
    return actionType;
  }

  public String getActorId() {
    return actorId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getEntityType() {
    return entityType;
  }

  public String getEntityId() {
    return entityId;
  }

  public String getCorrelationId() {
    return correlationId;
  }

  public String getDetails() {
    return details;
  }

  public Instant getTimestamp() {
    return timestamp;
  }
}
