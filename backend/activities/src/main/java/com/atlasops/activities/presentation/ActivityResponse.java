package com.atlasops.activities.presentation;

import com.atlasops.activities.domain.Activity;
import java.time.Instant;

/**
 * REST response representation for an activity.
 *
 * @param id the activity identifier
 * @param entityType the type of entity this activity relates to
 * @param entityId the identifier of the related entity
 * @param actionType the type of action performed
 * @param actorId the identifier of the user who performed the action
 * @param summary the human-readable summary of the activity
 * @param timestamp when the activity occurred
 */
public record ActivityResponse(
    String id,
    String entityType,
    String entityId,
    String actionType,
    String actorId,
    String summary,
    Instant timestamp) {

  /**
   * Creates an ActivityResponse from a domain Activity.
   *
   * @param activity the domain activity
   * @return the response DTO
   */
  public static ActivityResponse from(Activity activity) {
    return new ActivityResponse(
        activity.getId(),
        activity.getEntityType(),
        activity.getEntityId(),
        activity.getActionType(),
        activity.getActorId(),
        activity.getSummary(),
        activity.getTimestamp());
  }
}
