package com.atlasops.notifications.domain;

import com.atlasops.shared.domain.Entity;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents an in-app notification sent to a user. Notifications support read/unread status and
 * contain a link to the related entity.
 */
public final class Notification extends Entity<String> {

  private static final int TITLE_MAX_LENGTH = 150;
  private static final int MESSAGE_MAX_LENGTH = 500;

  private final String recipientUserId;
  private final String tenantId;
  private final String title;
  private final String message;
  private boolean read;
  private final String link;
  private final Instant createdAt;

  private Notification(
      String id,
      String recipientUserId,
      String tenantId,
      String title,
      String message,
      boolean read,
      String link,
      Instant createdAt) {
    super(id);
    this.recipientUserId =
        Objects.requireNonNull(recipientUserId, "RecipientUserId must not be null");
    this.tenantId = Objects.requireNonNull(tenantId, "TenantId must not be null");
    this.title = Objects.requireNonNull(title, "Title must not be null");
    this.message = Objects.requireNonNull(message, "Message must not be null");
    this.read = read;
    this.link = link;
    this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt must not be null");

    validateRecipientUserId(recipientUserId);
    validateTenantId(tenantId);
    validateTitle(title);
    validateMessage(message);
  }

  /**
   * Factory method to create a new Notification with default unread status.
   *
   * @param id unique identifier for the notification
   * @param recipientUserId the user who should receive the notification
   * @param tenantId the tenant this notification belongs to
   * @param title the notification title (max 150 characters)
   * @param message the notification message (max 500 characters)
   * @param link optional link to the related entity
   * @param createdAt the creation timestamp
   * @return a new Notification instance with read=false
   */
  public static Notification create(
      String id,
      String recipientUserId,
      String tenantId,
      String title,
      String message,
      String link,
      Instant createdAt) {
    return new Notification(id, recipientUserId, tenantId, title, message, false, link, createdAt);
  }

  /**
   * Reconstitutes a Notification from persisted data.
   *
   * @param id the notification identifier
   * @param recipientUserId the user who should receive the notification
   * @param tenantId the tenant this notification belongs to
   * @param title the notification title
   * @param message the notification message
   * @param read the current read status
   * @param link optional link to the related entity
   * @param createdAt the creation timestamp
   * @return a reconstituted Notification instance
   */
  public static Notification reconstitute(
      String id,
      String recipientUserId,
      String tenantId,
      String title,
      String message,
      boolean read,
      String link,
      Instant createdAt) {
    return new Notification(id, recipientUserId, tenantId, title, message, read, link, createdAt);
  }

  /** Marks this notification as read. */
  public void markAsRead() {
    this.read = true;
  }

  private void validateRecipientUserId(String recipientUserId) {
    if (recipientUserId.isBlank()) {
      throw new IllegalArgumentException("RecipientUserId must not be blank");
    }
  }

  private void validateTenantId(String tenantId) {
    if (tenantId.isBlank()) {
      throw new IllegalArgumentException("TenantId must not be blank");
    }
  }

  private void validateTitle(String title) {
    if (title.isBlank()) {
      throw new IllegalArgumentException("Title must not be blank");
    }
    if (title.length() > TITLE_MAX_LENGTH) {
      throw new IllegalArgumentException(
          "Title must not exceed " + TITLE_MAX_LENGTH + " characters, got: " + title.length());
    }
  }

  private void validateMessage(String message) {
    if (message.isBlank()) {
      throw new IllegalArgumentException("Message must not be blank");
    }
    if (message.length() > MESSAGE_MAX_LENGTH) {
      throw new IllegalArgumentException(
          "Message must not exceed "
              + MESSAGE_MAX_LENGTH
              + " characters, got: "
              + message.length());
    }
  }

  // --- Getters ---

  public String getRecipientUserId() {
    return recipientUserId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getTitle() {
    return title;
  }

  public String getMessage() {
    return message;
  }

  public boolean isRead() {
    return read;
  }

  public String getLink() {
    return link;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
