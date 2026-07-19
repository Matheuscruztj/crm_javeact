package com.atlasops.worker.consumers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.notifications.application.CreateNotificationCommand;
import com.atlasops.notifications.application.CreateNotificationUseCase;
import com.atlasops.notifications.application.PushSSEEventCommand;
import com.atlasops.notifications.application.PushSSEEventUseCase;
import com.atlasops.notifications.domain.Notification;
import com.atlasops.worker.infrastructure.redis.StreamMessage;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Unit tests for NotificationEventConsumer. Validates: Requirements 15.1, 15.2, 16.1, 17.3, 17.4
 */
@ExtendWith(MockitoExtension.class)
class NotificationEventConsumerTest {

  @Mock private CreateNotificationUseCase createNotificationUseCase;

  @Mock private PushSSEEventUseCase pushSSEEventUseCase;

  @Mock private StringRedisTemplate redisTemplate;

  @Mock private StreamOperations<String, Object, Object> streamOperations;

  private NotificationEventConsumer consumer;

  @BeforeEach
  void setUp() {
    consumer =
        new NotificationEventConsumer(
            createNotificationUseCase, pushSSEEventUseCase, redisTemplate);
  }

  @Test
  void should_returnCorrectStreamKey() {
    assertThat(consumer.getStreamKey()).isEqualTo("approvals.decided");
  }

  @Test
  void should_createNotificationAndPushSSE_when_recipientUserIdIsProvided() throws Exception {
    // Arrange
    when(redisTemplate.opsForStream()).thenReturn(streamOperations);

    Map<String, String> payload = new HashMap<>();
    payload.put("documentId", "doc-123");
    payload.put("tenantId", "tenant-alpha");
    payload.put("decision", "APPROVED");
    payload.put("analystId", "analyst-001");
    payload.put("recipientUserId", "user-001");
    payload.put("recipientEmail", "user@example.com");
    payload.put("tenantName", "Acme Corp");

    StreamMessage message = new StreamMessage("approvals.decided", "msg-1", payload);

    Notification mockNotification =
        Notification.create(
            "notif-001",
            "user-001",
            "tenant-alpha",
            "Document Approved",
            "Your document (doc-123) has been approved by analyst-001.",
            "/documents/doc-123",
            Instant.now());

    when(createNotificationUseCase.execute(any(CreateNotificationCommand.class)))
        .thenReturn(mockNotification);

    // Act
    consumer.handle(message);

    // Assert
    verify(createNotificationUseCase).execute(any(CreateNotificationCommand.class));
    verify(pushSSEEventUseCase).execute(any(PushSSEEventCommand.class));
    verify(streamOperations).add(any()); // Email notification published
  }

  @Test
  void should_publishEmailNotification_when_emailIsProvided() throws Exception {
    // Arrange
    when(redisTemplate.opsForStream()).thenReturn(streamOperations);

    Map<String, String> payload = new HashMap<>();
    payload.put("documentId", "doc-456");
    payload.put("tenantId", "tenant-beta");
    payload.put("decision", "REJECTED");
    payload.put("analystId", "analyst-002");
    payload.put("recipientEmail", "client@example.com");
    payload.put("tenantName", "Beta Inc");

    StreamMessage message = new StreamMessage("approvals.decided", "msg-2", payload);

    // Act
    consumer.handle(message);

    // Assert
    verify(streamOperations).add(any()); // Email notification published
  }

  @Test
  void should_skipInAppNotification_when_recipientUserIdIsEmpty() throws Exception {
    // Arrange
    when(redisTemplate.opsForStream()).thenReturn(streamOperations);

    Map<String, String> payload = new HashMap<>();
    payload.put("documentId", "doc-789");
    payload.put("tenantId", "tenant-gamma");
    payload.put("decision", "CANCELLED");
    payload.put("recipientEmail", "admin@example.com");
    payload.put("tenantName", "Gamma Ltd");
    // No recipientUserId

    StreamMessage message = new StreamMessage("approvals.decided", "msg-3", payload);

    // Act
    consumer.handle(message);

    // Assert
    verify(createNotificationUseCase, never()).execute(any());
    verify(pushSSEEventUseCase, never()).execute(any());
    verify(streamOperations).add(any()); // Email still published
  }

  @Test
  void should_buildCorrectNotificationTitle_forApprovedDecision() throws Exception {
    // Arrange
    Map<String, String> payload = new HashMap<>();
    payload.put("documentId", "doc-100");
    payload.put("tenantId", "tenant-alpha");
    payload.put("decision", "APPROVED");
    payload.put("recipientUserId", "user-100");
    // No recipientEmail - skips email publishing

    StreamMessage message = new StreamMessage("approvals.decided", "msg-4", payload);

    Notification mockNotification =
        Notification.create(
            "notif-100",
            "user-100",
            "tenant-alpha",
            "Document Approved",
            "Your document (doc-100) has been approved.",
            "/documents/doc-100",
            Instant.now());

    when(createNotificationUseCase.execute(any(CreateNotificationCommand.class)))
        .thenReturn(mockNotification);

    // Act
    consumer.handle(message);

    // Assert
    ArgumentCaptor<CreateNotificationCommand> captor =
        ArgumentCaptor.forClass(CreateNotificationCommand.class);
    verify(createNotificationUseCase).execute(captor.capture());

    CreateNotificationCommand capturedCommand = captor.getValue();
    assertThat(capturedCommand.title()).isEqualTo("Document Approved");
  }

  @Test
  void should_buildCorrectNotificationTitle_forRejectedDecision() throws Exception {
    // Arrange
    Map<String, String> payload = new HashMap<>();
    payload.put("documentId", "doc-200");
    payload.put("tenantId", "tenant-beta");
    payload.put("decision", "REJECTED");
    payload.put("recipientUserId", "user-200");
    // No recipientEmail - skips email publishing

    StreamMessage message = new StreamMessage("approvals.decided", "msg-5", payload);

    Notification mockNotification =
        Notification.create(
            "notif-200",
            "user-200",
            "tenant-beta",
            "Document Rejected",
            "Your document (doc-200) has been rejected.",
            "/documents/doc-200",
            Instant.now());

    when(createNotificationUseCase.execute(any(CreateNotificationCommand.class)))
        .thenReturn(mockNotification);

    // Act
    consumer.handle(message);

    // Assert
    ArgumentCaptor<CreateNotificationCommand> captor =
        ArgumentCaptor.forClass(CreateNotificationCommand.class);
    verify(createNotificationUseCase).execute(captor.capture());

    CreateNotificationCommand capturedCommand = captor.getValue();
    assertThat(capturedCommand.title()).isEqualTo("Document Rejected");
  }
}
