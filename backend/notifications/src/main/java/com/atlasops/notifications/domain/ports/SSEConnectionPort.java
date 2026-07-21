package com.atlasops.notifications.domain.ports;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Port defining the contract for pushing real-time events to connected users via Server-Sent Events
 * (SSE). Implementations manage active SSE connections and deliver events to specific users.
 */
public interface SSEConnectionPort {

  /**
   * Registers an SSE emitter for a user, replacing any existing connection.
   *
   * @param userId  the user identifier
   * @param emitter the SSE emitter to register
   */
  void register(String userId, SseEmitter emitter);

  /**
   * Pushes an event to the specified user's active SSE connection(s).
   *
   * @param userId the user identifier to push the event to
   * @param event  the event payload to deliver
   */
  void pushEvent(String userId, Object event);

  /**
   * Replays missed events to a reconnecting user since the given lastEventId.
   *
   * @param userId      the reconnecting user
   * @param lastEventId the last event ID received by the client
   * @param emitter     the new emitter to send replayed events to
   */
  void replayMissedEvents(String userId, String lastEventId, SseEmitter emitter);
}
