package com.atlasops.notifications.application;

/**
 * Command to push an event to a user's active SSE connection.
 *
 * @param userId the user identifier to push the event to
 * @param event the event payload to deliver
 */
public record PushSSEEventCommand(String userId, Object event) {}
