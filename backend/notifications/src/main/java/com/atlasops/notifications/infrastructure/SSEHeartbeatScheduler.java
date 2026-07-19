package com.atlasops.notifications.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler that sends heartbeat messages to all active SSE connections to keep them alive and
 * detect stale connections.
 */
@Component
public class SSEHeartbeatScheduler {

  private static final Logger log = LoggerFactory.getLogger(SSEHeartbeatScheduler.class);

  private final SSEConnectionManager connectionManager;

  public SSEHeartbeatScheduler(SSEConnectionManager connectionManager) {
    this.connectionManager = connectionManager;
  }

  /** Sends heartbeat to all active SSE connections every 30 seconds. */
  @Scheduled(fixedRate = 30000)
  public void sendHeartbeat() {
    int connectionCount = connectionManager.getActiveConnectionCount();
    if (connectionCount > 0) {
      log.debug("Sending heartbeat to {} active SSE connections", connectionCount);
      connectionManager.sendHeartbeatToAll();
    }
  }
}
