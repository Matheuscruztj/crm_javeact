package com.atlasops.worker.consumers;

import com.atlasops.notifications.application.CreateNotificationCommand;
import com.atlasops.notifications.application.CreateNotificationUseCase;
import com.atlasops.notifications.application.PushSSEEventCommand;
import com.atlasops.notifications.application.PushSSEEventUseCase;
import com.atlasops.notifications.domain.Notification;
import com.atlasops.worker.infrastructure.redis.MessageHandler;
import com.atlasops.worker.infrastructure.redis.StreamMessage;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Consumer for processing approval decision events from the approvals.decided stream. Creates
 * in-app notifications for CLIENT users, pushes SSE events, and publishes email notifications to
 * the notifications.email stream.
 *
 * <p>Validates: Requirements 15.1, 15.2, 16.1, 17.3, 17.4
 */
@Component
public class NotificationEventConsumer implements MessageHandler {

  private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);
  private static final String STREAM_KEY = "approvals.decided";
  private static final String EMAIL_STREAM = "notifications.email";

  private final CreateNotificationUseCase createNotificationUseCase;
  private final PushSSEEventUseCase pushSSEEventUseCase;
  private final StringRedisTemplate redisTemplate;

  public NotificationEventConsumer(
      CreateNotificationUseCase createNotificationUseCase,
      PushSSEEventUseCase pushSSEEventUseCase,
      StringRedisTemplate redisTemplate) {
    this.createNotificationUseCase = createNotificationUseCase;
    this.pushSSEEventUseCase = pushSSEEventUseCase;
    this.redisTemplate = redisTemplate;
  }

  public String getStreamKey() {
    return STREAM_KEY;
  }

  @Override
  public void handle(StreamMessage message) throws Exception {
    String documentId = message.getRequired("documentId");
    String tenantId = message.getRequired("tenantId");
    String decision = message.getRequired("decision");
    String analystId = message.get("analystId");
    String recipientUserId = message.get("recipientUserId");
    String recipientEmail = message.get("recipientEmail");
    String tenantName = message.get("tenantName");

    log.info(
        "Processing approval decision event for document {} (decision: {}, tenant: {})",
        documentId,
        decision,
        tenantId);

    // Build notification content based on decision
    String title = buildNotificationTitle(decision);
    String notificationMessage = buildNotificationMessage(documentId, decision, analystId);
    String link = "/documents/" + documentId;

    // 1. Create in-app notification
    if (recipientUserId != null && !recipientUserId.isBlank()) {
      try {
        CreateNotificationCommand notificationCommand =
            new CreateNotificationCommand(
                recipientUserId, tenantId, title, notificationMessage, link);

        Notification notification = createNotificationUseCase.execute(notificationCommand);
        log.debug(
            "Created in-app notification {} for user {}", notification.getId(), recipientUserId);

        // 2. Push SSE event to connected user
        pushSSEEvent(recipientUserId, notification);
      } catch (Exception e) {
        log.warn(
            "Failed to create/push in-app notification for user {}: {}",
            recipientUserId,
            e.getMessage());
      }
    } else {
      log.warn("No recipient user ID for document {} approval decision", documentId);
    }

    // 3. Publish email notification to email stream
    if (recipientEmail != null && !recipientEmail.isBlank()) {
      publishEmailNotification(
          recipientEmail,
          title,
          notificationMessage,
          tenantName != null ? tenantName : "AtlasOps",
          documentId,
          tenantId);
    } else {
      log.debug("No recipient email for document {} approval decision", documentId);
    }

    log.info("Processed approval decision event for document {}", documentId);
  }

  private String buildNotificationTitle(String decision) {
    return switch (decision.toUpperCase()) {
      case "APPROVED" -> "Document Approved";
      case "REJECTED" -> "Document Rejected";
      case "CANCELLED" -> "Approval Cancelled";
      default -> "Document Status Updated";
    };
  }

  private String buildNotificationMessage(String documentId, String decision, String analystId) {
    String analyst = analystId != null ? analystId : "an analyst";
    return switch (decision.toUpperCase()) {
      case "APPROVED" ->
          String.format("Your document (%s) has been approved by %s.", documentId, analyst);
      case "REJECTED" ->
          String.format("Your document (%s) has been rejected by %s.", documentId, analyst);
      case "CANCELLED" ->
          String.format("The approval for your document (%s) has been cancelled.", documentId);
      default -> String.format("Your document (%s) status has been updated.", documentId);
    };
  }

  private void pushSSEEvent(String userId, Notification notification) {
    try {
      Map<String, Object> eventPayload = new HashMap<>();
      eventPayload.put("type", "notification");
      eventPayload.put("id", notification.getId());
      eventPayload.put("title", notification.getTitle());
      eventPayload.put("message", notification.getMessage());
      eventPayload.put("link", notification.getLink());
      eventPayload.put("timestamp", notification.getCreatedAt().toString());

      PushSSEEventCommand command = new PushSSEEventCommand(userId, eventPayload);
      pushSSEEventUseCase.execute(command);
      log.debug("Pushed SSE event to user {}", userId);
    } catch (Exception e) {
      log.warn("Failed to push SSE event to user {}: {}", userId, e.getMessage());
    }
  }

  private void publishEmailNotification(
      String to,
      String subject,
      String body,
      String tenantName,
      String documentId,
      String tenantId) {

    Map<String, String> payload = new HashMap<>();
    payload.put("to", to);
    payload.put("subject", subject);
    payload.put("body", body);
    payload.put("tenantName", tenantName);
    payload.put("documentId", documentId);
    payload.put("tenantId", tenantId);
    payload.put("timestamp", Instant.now().toString());

    var record = StreamRecords.string(payload).withStreamKey(EMAIL_STREAM);
    redisTemplate.opsForStream().add(record);

    log.debug("Published email notification to stream for {}", to);
  }
}
