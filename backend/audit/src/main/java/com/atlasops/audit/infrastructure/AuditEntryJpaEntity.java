package com.atlasops.audit.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * JPA entity mapping to the "audit_entries" database table. This entity is INSERT-only by design —
 * no update or delete operations are permitted in application code.
 */
@Entity
@Table(name = "audit_entries")
public class AuditEntryJpaEntity {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private String id;

  @Column(name = "action_type", nullable = false, updatable = false)
  private String actionType;

  @Column(name = "actor_id", nullable = false, updatable = false)
  private String actorId;

  @Column(name = "tenant_id", nullable = false, updatable = false)
  private String tenantId;

  @Column(name = "entity_type", nullable = false, updatable = false)
  private String entityType;

  @Column(name = "entity_id", nullable = false, updatable = false)
  private String entityId;

  @Column(name = "correlation_id", nullable = false, updatable = false)
  private String correlationId;

  @Column(name = "details", columnDefinition = "jsonb", updatable = false)
  private String details;

  @Column(name = "timestamp", nullable = false, updatable = false)
  private Instant timestamp;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected AuditEntryJpaEntity() {
    // JPA requires no-arg constructor
  }

  public AuditEntryJpaEntity(
      String id,
      String actionType,
      String actorId,
      String tenantId,
      String entityType,
      String entityId,
      String correlationId,
      String details,
      Instant timestamp,
      Instant createdAt) {
    this.id = id;
    this.actionType = actionType;
    this.actorId = actorId;
    this.tenantId = tenantId;
    this.entityType = entityType;
    this.entityId = entityId;
    this.correlationId = correlationId;
    this.details = details;
    this.timestamp = timestamp;
    this.createdAt = createdAt;
  }

  public String getId() {
    return id;
  }

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

  public Instant getCreatedAt() {
    return createdAt;
  }
}
