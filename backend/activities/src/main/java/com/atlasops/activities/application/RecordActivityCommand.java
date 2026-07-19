package com.atlasops.activities.application;

import java.util.Objects;

/**
 * Command for recording a new activity entry from a domain event.
 *
 * @param entityType the type of entity the activity relates to (e.g., CUSTOMER, REQUEST)
 * @param entityId the identifier of the entity
 * @param actionType the type of action performed (e.g., CREATED, UPDATED)
 * @param actorId the identifier of the user who performed the action
 * @param tenantId the tenant this activity belongs to
 * @param summary a human-readable summary (max 500 chars)
 * @param eventId unique event identifier for deduplication
 */
public record RecordActivityCommand(
    String entityType,
    String entityId,
    String actionType,
    String actorId,
    String tenantId,
    String summary,
    String eventId) {

  public RecordActivityCommand {
    Objects.requireNonNull(entityType, "EntityType must not be null");
    Objects.requireNonNull(entityId, "EntityId must not be null");
    Objects.requireNonNull(actionType, "ActionType must not be null");
    Objects.requireNonNull(actorId, "ActorId must not be null");
    Objects.requireNonNull(tenantId, "TenantId must not be null");
    Objects.requireNonNull(summary, "Summary must not be null");
    Objects.requireNonNull(eventId, "EventId must not be null");
  }
}
