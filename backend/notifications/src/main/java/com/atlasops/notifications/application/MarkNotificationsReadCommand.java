package com.atlasops.notifications.application;

import java.util.List;

/**
 * Command to mark one or more notifications as read.
 *
 * @param notificationIds the IDs of notifications to mark as read (max 100)
 * @param userId the user requesting the operation (for ownership validation)
 * @param tenantId the tenant context
 */
public record MarkNotificationsReadCommand(
    List<String> notificationIds, String userId, String tenantId) {}
