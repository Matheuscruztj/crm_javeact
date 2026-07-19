package com.atlasops.notifications.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.notifications.domain.Notification;
import com.atlasops.notifications.domain.ports.NotificationRepository;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateNotificationUseCaseTest {

  private static final Instant FIXED_NOW = Instant.parse("2025-03-15T10:30:00Z");
  private static final String NOTIFICATION_ID = "notif-001";
  private static final String USER_ID = "user-001";
  private static final String TENANT_ID = "tenant-alpha";

  @Mock private NotificationRepository notificationRepository;
  @Mock private IdGenerator idGenerator;
  @Mock private Clock clock;

  private CreateNotificationUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new CreateNotificationUseCase(notificationRepository, idGenerator, clock);
  }

  @Test
  void should_createNotification_when_allFieldsValid() {
    when(idGenerator.generate()).thenReturn(NOTIFICATION_ID);
    when(clock.now()).thenReturn(FIXED_NOW);
    when(notificationRepository.save(any(Notification.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var command =
        new CreateNotificationCommand(
            USER_ID, TENANT_ID, "Approval Decision", "Your document was approved", "/docs/123");

    Notification result = useCase.execute(command);

    assertThat(result.getId()).isEqualTo(NOTIFICATION_ID);
    assertThat(result.getRecipientUserId()).isEqualTo(USER_ID);
    assertThat(result.getTenantId()).isEqualTo(TENANT_ID);
    assertThat(result.getTitle()).isEqualTo("Approval Decision");
    assertThat(result.getMessage()).isEqualTo("Your document was approved");
    assertThat(result.getLink()).isEqualTo("/docs/123");
    assertThat(result.isRead()).isFalse();
    assertThat(result.getCreatedAt()).isEqualTo(FIXED_NOW);
  }

  @Test
  void should_persistNotification_when_commandIsValid() {
    when(idGenerator.generate()).thenReturn(NOTIFICATION_ID);
    when(clock.now()).thenReturn(FIXED_NOW);
    when(notificationRepository.save(any(Notification.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var command =
        new CreateNotificationCommand(
            USER_ID, TENANT_ID, "Status Update", "Request moved to IN_PROGRESS", null);

    useCase.execute(command);

    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(captor.capture());

    Notification saved = captor.getValue();
    assertThat(saved.getId()).isEqualTo(NOTIFICATION_ID);
    assertThat(saved.getRecipientUserId()).isEqualTo(USER_ID);
    assertThat(saved.getTenantId()).isEqualTo(TENANT_ID);
  }

  @Test
  void should_createNotificationWithNullLink_when_linkNotProvided() {
    when(idGenerator.generate()).thenReturn(NOTIFICATION_ID);
    when(clock.now()).thenReturn(FIXED_NOW);
    when(notificationRepository.save(any(Notification.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var command =
        new CreateNotificationCommand(USER_ID, TENANT_ID, "Info", "General info message", null);

    Notification result = useCase.execute(command);

    assertThat(result.getLink()).isNull();
  }

  @Test
  void should_throwException_when_recipientUserIdIsNull() {
    var command = new CreateNotificationCommand(null, TENANT_ID, "Title", "Message", null);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("RecipientUserId");
  }

  @Test
  void should_throwException_when_recipientUserIdIsBlank() {
    var command = new CreateNotificationCommand("  ", TENANT_ID, "Title", "Message", null);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("RecipientUserId");
  }

  @Test
  void should_throwException_when_tenantIdIsNull() {
    var command = new CreateNotificationCommand(USER_ID, null, "Title", "Message", null);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("TenantId");
  }

  @Test
  void should_throwException_when_titleIsNull() {
    var command = new CreateNotificationCommand(USER_ID, TENANT_ID, null, "Message", null);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Title");
  }

  @Test
  void should_throwException_when_messageIsNull() {
    var command = new CreateNotificationCommand(USER_ID, TENANT_ID, "Title", null, null);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Message");
  }
}
