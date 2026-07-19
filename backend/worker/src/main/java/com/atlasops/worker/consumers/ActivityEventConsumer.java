package com.atlasops.worker.consumers;

import com.atlasops.activities.application.RecordActivityCommand;
import com.atlasops.activities.application.RecordActivityUseCase;
import com.atlasops.activities.domain.Activity;
import com.atlasops.worker.infrastructure.redis.MessageHandler;
import com.atlasops.worker.infrastructure.redis.StreamMessage;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Consumer for recording activities from the activities.events stream. Consumes domain events
 * (CustomerCreated, RequestStatusChanged, etc.) and records activity entries with deduplication via
 * eventId.
 *
 * <p>Validates: Requirements 14.1, 14.7, 14.8
 */
@Component
public class ActivityEventConsumer implements MessageHandler {

  private static final Logger log = LoggerFactory.getLogger(ActivityEventConsumer.class);
  private static final String STREAM_KEY = "activities.events";

  private final RecordActivityUseCase recordActivityUseCase;

  public ActivityEventConsumer(RecordActivityUseCase recordActivityUseCase) {
    this.recordActivityUseCase = recordActivityUseCase;
  }

  public String getStreamKey() {
    return STREAM_KEY;
  }

  @Override
  public void handle(StreamMessage message) throws Exception {
    String eventId = message.getRequired("eventId");
    String entityType = message.getRequired("entityType");
    String entityId = message.getRequired("entityId");
    String actionType = message.getRequired("actionType");
    String actorId = message.getRequired("actorId");
    String tenantId = message.getRequired("tenantId");
    String summary = message.get("summary");

    // Use default summary if not provided
    if (summary == null || summary.isBlank()) {
      summary = buildDefaultSummary(entityType, entityId, actionType);
    }

    log.debug(
        "Processing activity event {} for {} {} (action: {})",
        eventId,
        entityType,
        entityId,
        actionType);

    RecordActivityCommand command =
        new RecordActivityCommand(
            entityType, entityId, actionType, actorId, tenantId, summary, eventId);

    Optional<Activity> result = recordActivityUseCase.execute(command);

    if (result.isPresent()) {
      log.info(
          "Recorded activity {} for {} {} (action: {}, tenant: {})",
          result.get().getId(),
          entityType,
          entityId,
          actionType,
          tenantId);
    } else {
      log.debug("Activity event {} was deduplicated (already processed)", eventId);
    }
  }

  private String buildDefaultSummary(String entityType, String entityId, String actionType) {
    return String.format("%s %s %s", entityType, entityId, formatActionType(actionType));
  }

  private String formatActionType(String actionType) {
    if (actionType == null) {
      return "updated";
    }
    return switch (actionType.toUpperCase()) {
      case "CREATED" -> "was created";
      case "UPDATED" -> "was updated";
      case "DELETED" -> "was deleted";
      case "APPROVED" -> "was approved";
      case "REJECTED" -> "was rejected";
      case "CANCELLED" -> "was cancelled";
      case "SUBMITTED" -> "was submitted";
      case "ASSIGNED" -> "was assigned";
      case "COMMENTED" -> "received a comment";
      case "UPLOADED" -> "was uploaded";
      case "ANALYZED" -> "was analyzed";
      case "STATUS_CHANGED" -> "status changed";
      default -> actionType.toLowerCase().replace("_", " ");
    };
  }
}
