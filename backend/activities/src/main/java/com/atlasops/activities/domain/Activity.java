package com.atlasops.activities.domain;

import com.atlasops.shared.domain.Entity;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents a recorded activity in the system. Activities track actions performed on entities and
 * support deduplication via a unique eventId.
 */
public final class Activity extends Entity<String> {

  private static final int SUMMARY_MAX_LENGTH = 500;

  private final String entityType;
  private final String entityId;
  private final String actionType;
  private final String actorId;
  private final String tenantId;
  private final String summary;
  private final String eventId;
  private final Instant timestamp;

  private Activity(
      String id,
      String entityType,
      String entityId,
      String actionType,
      String actorId,
      String tenantId,
      String summary,
      String eventId,
      Instant timestamp) {
    super(id);
    this.entityType = Objects.requireNonNull(entityType, "EntityType must not be null");
    this.entityId = Objects.requireNonNull(entityId, "EntityId must not be null");
    this.actionType = Objects.requireNonNull(actionType, "ActionType must not be null");
    this.actorId = Objects.requireNonNull(actorId, "ActorId must not be null");
    this.tenantId = Objects.requireNonNull(tenantId, "TenantId must not be null");
    this.summary = Objects.requireNonNull(summary, "Summary must not be null");
    this.eventId = Objects.requireNonNull(eventId, "EventId must not be null");
    this.timestamp = Objects.requireNonNull(timestamp, "Timestamp must not be null");

    validateEntityType(entityType);
    validateEntityId(entityId);
    validateActionType(actionType);
    validateActorId(actorId);
    validateTenantId(tenantId);
    validateSummary(summary);
    validateEventId(eventId);
  }

  /**
   * Factory method to create a new Activity.
   *
   * @param id unique identifier for the activity record
   * @param entityType the type of entity this activity relates to (e.g., CUSTOMER, REQUEST)
   * @param entityId the identifier of the entity
   * @param actionType the type of action performed (e.g., CREATED, UPDATED)
   * @param actorId the identifier of the user who performed the action
   * @param tenantId the tenant this activity belongs to
   * @param summary a human-readable summary (max 500 chars)
   * @param eventId unique event identifier for deduplication
   * @param timestamp when the activity occurred
   * @return a new Activity instance
   */
  public static Activity create(
      String id,
      String entityType,
      String entityId,
      String actionType,
      String actorId,
      String tenantId,
      String summary,
      String eventId,
      Instant timestamp) {
    return new Activity(
        id, entityType, entityId, actionType, actorId, tenantId, summary, eventId, timestamp);
  }

  /** Reconstitutes an Activity from persisted data. */
  public static Activity reconstitute(
      String id,
      String entityType,
      String entityId,
      String actionType,
      String actorId,
      String tenantId,
      String summary,
      String eventId,
      Instant timestamp) {
    return new Activity(
        id, entityType, entityId, actionType, actorId, tenantId, summary, eventId, timestamp);
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

  private void validateSummary(String summary) {
    if (summary.isBlank()) {
      throw new IllegalArgumentException("Summary must not be blank");
    }
    if (summary.length() > SUMMARY_MAX_LENGTH) {
      throw new IllegalArgumentException(
          "Summary must not exceed "
              + SUMMARY_MAX_LENGTH
              + " characters, got: "
              + summary.length());
    }
  }

  private void validateEventId(String eventId) {
    if (eventId.isBlank()) {
      throw new IllegalArgumentException("EventId must not be blank");
    }
  }

  // --- Getters ---

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
