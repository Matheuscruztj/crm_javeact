package com.atlasops.notifications.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.atlasops.notifications.application.GetUnreadCountUseCase;
import com.atlasops.notifications.application.ListNotificationsUseCase;
import com.atlasops.notifications.application.MarkNotificationsReadUseCase;
import com.atlasops.notifications.domain.Notification;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for NotificationController.
 * Validates: Requirements 15.4, 15.5, 15.7, 15.8
 */
@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    private static final String TENANT = "tenant-alpha";
    private static final String USER = "user-001";
    private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");

    @Mock private ListNotificationsUseCase listNotificationsUseCase;
    @Mock private MarkNotificationsReadUseCase markNotificationsReadUseCase;
    @Mock private GetUnreadCountUseCase getUnreadCountUseCase;

    private NotificationController controller;

    @BeforeEach
    void setUp() {
        controller = new NotificationController(
                listNotificationsUseCase, markNotificationsReadUseCase, getUnreadCountUseCase);
    }

    private Notification aNotification(String id) {
        return Notification.create(id, USER, TENANT, "Test notification " + id,
                "Message", "/link", NOW);
    }

    @Test
    void should_listNotifications_when_userHasNotifications() {
        when(listNotificationsUseCase.execute(any()))
                .thenReturn(new PageImpl<>(List.of(aNotification("n-001")), PageRequest.of(0, 20), 1));

        ResponseEntity<PageResponse<NotificationResponse>> response =
                controller.list(TENANT, USER, 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().content()).hasSize(1);
        assertThat(response.getBody().content().get(0).id()).isEqualTo("n-001");
    }

    @Test
    void should_returnEmptyPage_when_noNotifications() {
        when(listNotificationsUseCase.execute(any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        ResponseEntity<PageResponse<NotificationResponse>> response =
                controller.list(TENANT, USER, 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().content()).isEmpty();
    }

    @Test
    void should_returnUnreadCount_when_requested() {
        when(getUnreadCountUseCase.execute(any())).thenReturn(5L);

        ResponseEntity<NotificationController.UnreadCountResponse> response =
                controller.getUnreadCount(TENANT, USER);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().count()).isEqualTo(5L);
    }

    @Test
    void should_markNotificationsRead_when_idsProvided() {
        when(markNotificationsReadUseCase.execute(any()))
                .thenReturn(List.of("n-001", "n-002"));

        var request = new MarkNotificationsReadRequest(List.of("n-001", "n-002"));
        ResponseEntity<NotificationController.MarkReadResponse> response =
                controller.markAsRead(TENANT, USER, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().markedIds()).containsExactly("n-001", "n-002");
    }
}
