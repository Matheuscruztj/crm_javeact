package com.atlasops.notifications.domain.ports;

/**
 * Port defining the contract for pushing real-time events to connected users via Server-Sent Events
 * (SSE). Implementations manage active SSE connections and deliver events to specific users.
 */
public interface SSEConnectionPort {

  /**
   * Pushes an event to the specified user's active SSE connection(s).
   *
   * @param userId the user identifier to push the event to
   * @param event the event payload to deliver
   */
  void pushEvent(String userId, Object event);
}
