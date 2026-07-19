package com.atlasops.notifications.application;

/**
 * Command to create a new in-app notification.
 *
 * @param recipientUserId the user who should receive the notification
 * @param tenantId the tenant this notification belongs to
 * @param title the notification title (max 150 characters)
 * @param message the notification message (max 500 characters)
 * @param link optional link to the related entity
 */
public record CreateNotificationCommand(
    String recipientUserId, String tenantId, String title, String message, String link) {}
