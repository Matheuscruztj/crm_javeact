package com.atlasops.notifications.infrastructure;

import com.atlasops.notifications.domain.ports.SSEConnectionPort;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * In-memory SSE connection manager implementing the SSEConnectionPort. Manages active SSE
 * connections and delivers events to specific users.
 */
@Component
public class SSEConnectionManager implements SSEConnectionPort {

  private static final Logger log = LoggerFactory.getLogger(SSEConnectionManager.class);

  private final Map<String, SseEmitter> connections = new ConcurrentHashMap<>();

  /**
   * Registers an SSE emitter for a user.
   *
   * @param userId the user identifier
   * @param emitter the SSE emitter to register
   */
  public void register(String userId, SseEmitter emitter) {
    SseEmitter existing = connections.put(userId, emitter);
    if (existing != null) {
      existing.complete();
      log.debug("Replaced existing SSE connection for user {}", userId);
    }
    log.info("SSE connection registered for user {}", userId);

    emitter.onCompletion(
        () -> {
          connections.remove(userId, emitter);
          log.debug("SSE connection completed for user {}", userId);
        });

    emitter.onTimeout(
        () -> {
          connections.remove(userId, emitter);
          log.debug("SSE connection timed out for user {}", userId);
        });

    emitter.onError(
        ex -> {
          connections.remove(userId, emitter);
          log.warn("SSE connection error for user {}: {}", userId, ex.getMessage());
        });
  }

  /**
   * Removes the SSE emitter for a user.
   *
   * @param userId the user identifier
   */
  public void unregister(String userId) {
    SseEmitter emitter = connections.remove(userId);
    if (emitter != null) {
      emitter.complete();
      log.info("SSE connection unregistered for user {}", userId);
    }
  }

  @Override
  public void pushEvent(String userId, Object event) {
    SseEmitter emitter = connections.get(userId);
    if (emitter == null) {
      log.debug("No active SSE connection for user {}, event dropped", userId);
      return;
    }

    try {
      emitter.send(SseEmitter.event().data(event));
      log.debug("SSE event pushed to user {}", userId);
    } catch (IOException e) {
      connections.remove(userId);
      log.warn("Failed to push SSE event to user {}: {}", userId, e.getMessage());
    }
  }

  /** Sends a heartbeat to all connected users to keep connections alive. */
  public void sendHeartbeatToAll() {
    connections.forEach(
        (userId, emitter) -> {
          try {
            emitter.send(SseEmitter.event().comment("heartbeat"));
          } catch (IOException e) {
            connections.remove(userId);
            log.debug("Heartbeat failed for user {}, connection removed", userId);
          }
        });
  }

  /**
   * Returns the number of active connections.
   *
   * @return the count of active SSE connections
   */
  public int getActiveConnectionCount() {
    return connections.size();
  }
}
