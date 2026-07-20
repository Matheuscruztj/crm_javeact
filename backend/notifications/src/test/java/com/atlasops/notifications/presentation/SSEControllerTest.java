package com.atlasops.notifications.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.atlasops.notifications.infrastructure.SSEConnectionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Unit tests for SSEController.
 * Validates: Requirements 17.1, 17.2, 17.5, 17.6, 17.7
 */
@ExtendWith(MockitoExtension.class)
class SSEControllerTest {

    private static final String USER = "user-001";

    @Mock private SSEConnectionManager connectionManager;

    private SSEController controller;

    @BeforeEach
    void setUp() {
        controller = new SSEController(connectionManager);
    }

    @Test
    void should_returnSseEmitter_when_connectionEstablished() {
        SseEmitter emitter = controller.stream("valid-token", USER, null);

        assertThat(emitter).isNotNull();
        verify(connectionManager).register(USER, emitter);
    }

    @Test
    void should_replayMissedEvents_when_lastEventIdProvided() {
        SseEmitter emitter = controller.stream("valid-token", USER, "event-42");

        assertThat(emitter).isNotNull();
        verify(connectionManager).register(USER, emitter);
        verify(connectionManager).replayMissedEvents(USER, "event-42", emitter);
    }

    @Test
    void should_notReplay_when_lastEventIdIsBlank() {
        SseEmitter emitter = controller.stream("valid-token", USER, "   ");

        assertThat(emitter).isNotNull();
        verify(connectionManager).register(USER, emitter);
        // replayMissedEvents should NOT be called for blank lastEventId
        org.mockito.Mockito.verify(connectionManager, org.mockito.Mockito.never())
                .replayMissedEvents(org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any());
    }
}
