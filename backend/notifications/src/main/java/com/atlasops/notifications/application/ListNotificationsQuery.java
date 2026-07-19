package com.atlasops.notifications.application;

/**
 * Query to retrieve paginated notifications for a user.
 *
 * @param userId the user identifier
 * @param tenantId the tenant identifier
 * @param page the page number (zero-based)
 * @param size the page size (default 20, max 100)
 */
public record ListNotificationsQuery(String userId, String tenantId, int page, int size) {}
