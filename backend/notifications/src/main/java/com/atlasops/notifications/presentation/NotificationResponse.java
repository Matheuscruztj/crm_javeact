package com.atlasops.notifications.presentation;

import com.atlasops.notifications.domain.Notification;
import java.time.Instant;

/**
 * REST response representation for a notification.
 *
 * @param id the notification identifier
 * @param title the notification title
 * @param message the notification message
 * @param read whether the notification has been read
 * @param link optional link to the related entity
 * @param createdAt the creation timestamp
 */
public record NotificationResponse(
    String id, String title, String message, boolean read, String link, Instant createdAt) {

  /**
   * Creates a NotificationResponse from a domain Notification.
   *
   * @param notification the domain notification
   * @return the response DTO
   */
  public static NotificationResponse from(Notification notification) {
    return new NotificationResponse(
        notification.getId(),
        notification.getTitle(),
        notification.getMessage(),
        notification.isRead(),
        notification.getLink(),
        notification.getCreatedAt());
  }
}
