package com.atlasops.notifications.application;

import com.atlasops.notifications.domain.ports.SSEConnectionPort;

/** Use case for pushing real-time events to active SSE connections. */
public class PushSSEEventUseCase {

  private final SSEConnectionPort sseConnectionPort;

  public PushSSEEventUseCase(SSEConnectionPort sseConnectionPort) {
    this.sseConnectionPort = sseConnectionPort;
  }

  /**
   * Pushes an event to the specified user's active SSE connection(s).
   *
   * @param command the push SSE event command
   */
  public void execute(PushSSEEventCommand command) {
    validateCommand(command);
    sseConnectionPort.pushEvent(command.userId(), command.event());
  }

  private void validateCommand(PushSSEEventCommand command) {
    if (command.userId() == null || command.userId().isBlank()) {
      throw new IllegalArgumentException("UserId must not be null or empty");
    }
    if (command.event() == null) {
      throw new IllegalArgumentException("Event must not be null");
    }
  }
}
