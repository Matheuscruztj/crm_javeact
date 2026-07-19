package com.atlasops.activities.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/** JPA entity mapping to the "activities" database table. */
@Entity
@Table(
    name = "activities",
    indexes = {
      @Index(name = "idx_activities_tenant_timestamp", columnList = "tenant_id, timestamp DESC"),
      @Index(name = "idx_activities_entity", columnList = "tenant_id, entity_type, entity_id"),
      @Index(name = "idx_activities_event_id", columnList = "event_id", unique = true)
    })
public class ActivityJpaEntity {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private String id;

  @Column(name = "entity_type", nullable = false, updatable = false)
  private String entityType;

  @Column(name = "entity_id", nullable = false, updatable = false)
  private String entityId;

  @Column(name = "action_type", nullable = false, updatable = false)
  private String actionType;

  @Column(name = "actor_id", nullable = false, updatable = false)
  private String actorId;

  @Column(name = "tenant_id", nullable = false, updatable = false)
  private String tenantId;

  @Column(name = "summary", nullable = false, length = 500, updatable = false)
  private String summary;

  @Column(name = "event_id", nullable = false, unique = true, updatable = false)
  private String eventId;

  @Column(name = "timestamp", nullable = false, updatable = false)
  private Instant timestamp;

  protected ActivityJpaEntity() {
    // JPA requires no-arg constructor
  }

  public ActivityJpaEntity(
      String id,
      String entityType,
      String entityId,
      String actionType,
      String actorId,
      String tenantId,
      String summary,
      String eventId,
      Instant timestamp) {
    this.id = id;
    this.entityType = entityType;
    this.entityId = entityId;
    this.actionType = actionType;
    this.actorId = actorId;
    this.tenantId = tenantId;
    this.summary = summary;
    this.eventId = eventId;
    this.timestamp = timestamp;
  }

  public String getId() {
    return id;
  }

  public String getEntityType() {
    return entityType;
  }

  public String getEntityId() {
    return entityId;
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

  public String getSummary() {
    return summary;
  }

  public String getEventId() {
    return eventId;
  }

  public Instant getTimestamp() {
    return timestamp;
  }
}
