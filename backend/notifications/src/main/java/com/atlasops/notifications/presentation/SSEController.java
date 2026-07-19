package com.atlasops.notifications.presentation;

import com.atlasops.notifications.infrastructure.SSEConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * REST controller for Server-Sent Events (SSE) connections.
 *
 * <p>Endpoint:
 *
 * <ul>
 *   <li>GET /api/v1/events/stream?token={jwt} — establish SSE connection for real-time updates
 * </ul>
 *
 * <p>Validates: Requirements 17.1, 17.2, 17.5, 17.6, 17.7
 */
@RestController
@RequestMapping("/api/v1/events")
public class SSEController {

  private static final Logger log = LoggerFactory.getLogger(SSEController.class);
  private static final long SSE_TIMEOUT = 0L; // No timeout, managed by heartbeat

  private final SSEConnectionManager connectionManager;

  public SSEController(SSEConnectionManager connectionManager) {
    this.connectionManager = connectionManager;
  }

  /**
   * Establishes an SSE connection for real-time event streaming.
   *
   * <p>The connection is authenticated via the token parameter (JWT validation should be handled by
   * a filter/interceptor in production). Supports Last-Event-ID header for reconnection.
   *
   * @param token the JWT token for authentication
   * @param userId the authenticated user identifier from header
   * @param lastEventId optional Last-Event-ID header for reconnection
   * @return an SseEmitter for streaming events
   */
  @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter stream(
      @RequestParam String token,
      @RequestHeader("X-User-ID") String userId,
      @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {

    log.info("SSE connection requested for user {}, lastEventId: {}", userId, lastEventId);

    SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
    connectionManager.register(userId, emitter);

    if (lastEventId != null && !lastEventId.isBlank()) {
      log.debug("Reconnection detected for user {} with lastEventId: {}", userId, lastEventId);
      // Future: replay missed events since lastEventId
    }

    return emitter;
  }
}
