package com.atlasops.notifications.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class SSEConnectionManagerTest {

  @Test
  void should_registerAndPushEvents_whenConnectionExists() throws Exception {
    SSEConnectionManager manager = new SSEConnectionManager();
    SseEmitter emitter = mock(SseEmitter.class);

    manager.register("user-1", emitter);
    assertThat(manager.getActiveConnectionCount()).isEqualTo(1);

    manager.pushEvent("user-1", "payload");

    verify(emitter)
        .send(org.mockito.ArgumentMatchers.any(SseEmitter.SseEventBuilder.class));
  }

  @Test
  void shouldRemoveConnection_whenUnregistered() {
    SSEConnectionManager manager = new SSEConnectionManager();
    SseEmitter emitter = mock(SseEmitter.class);

    manager.register("user-1", emitter);
    manager.unregister("user-1");

    assertThat(manager.getActiveConnectionCount()).isZero();
    verify(emitter).complete();
  }

  @Test
  void shouldDropConnection_whenSendFails() throws Exception {
    SSEConnectionManager manager = new SSEConnectionManager();
    SseEmitter emitter = mock(SseEmitter.class);
    org.mockito.Mockito.doThrow(new IOException("boom"))
        .when(emitter)
        .send(org.mockito.ArgumentMatchers.any(SseEmitter.SseEventBuilder.class));

    manager.register("user-1", emitter);
    manager.pushEvent("user-1", "payload");

    assertThat(manager.getActiveConnectionCount()).isZero();
  }
}
