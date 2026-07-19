package com.atlasops.notifications.application;

/**
 * Query to retrieve the count of unread notifications for a user.
 *
 * @param userId the user identifier
 * @param tenantId the tenant identifier
 */
public record GetUnreadCountQuery(String userId, String tenantId) {}
