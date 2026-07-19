package com.atlasops.notifications.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import com.atlasops.notifications.domain.ports.SSEConnectionPort;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PushSSEEventUseCaseTest {

  private static final String USER_ID = "user-001";

  @Mock private SSEConnectionPort sseConnectionPort;

  private PushSSEEventUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new PushSSEEventUseCase(sseConnectionPort);
  }

  @Test
  void should_pushEventToUser_when_commandIsValid() {
    var eventPayload = Map.of("type", "notification", "title", "New approval");
    var command = new PushSSEEventCommand(USER_ID, eventPayload);

    useCase.execute(command);

    verify(sseConnectionPort).pushEvent(USER_ID, eventPayload);
  }

  @Test
  void should_delegateToSSEPort_when_eventIsStringPayload() {
    var command = new PushSSEEventCommand(USER_ID, "simple-event");

    useCase.execute(command);

    verify(sseConnectionPort).pushEvent(USER_ID, "simple-event");
  }

  @Test
  void should_throwException_when_userIdIsNull() {
    var command = new PushSSEEventCommand(null, "event");

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("UserId");
  }

  @Test
  void should_throwException_when_userIdIsBlank() {
    var command = new PushSSEEventCommand("  ", "event");

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("UserId");
  }

  @Test
  void should_throwException_when_eventIsNull() {
    var command = new PushSSEEventCommand(USER_ID, null);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Event");
  }
}
