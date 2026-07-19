package com.atlasops.notifications.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.notifications.domain.ports.NotificationRepository;
import com.atlasops.shared.domain.exceptions.BusinessRuleViolationException;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MarkNotificationsReadUseCaseTest {

  private static final String USER_ID = "user-001";
  private static final String TENANT_ID = "tenant-alpha";

  @Mock private NotificationRepository notificationRepository;

  private MarkNotificationsReadUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new MarkNotificationsReadUseCase(notificationRepository);
  }

  @Test
  void should_markSingleNotificationAsRead_when_notificationBelongsToUser() {
    List<String> ids = List.of("notif-001");
    when(notificationRepository.markAsRead(ids, USER_ID, TENANT_ID)).thenReturn(ids);

    var command = new MarkNotificationsReadCommand(ids, USER_ID, TENANT_ID);
    List<String> result = useCase.execute(command);

    assertThat(result).containsExactly("notif-001");
    verify(notificationRepository).markAsRead(ids, USER_ID, TENANT_ID);
  }

  @Test
  void should_markBulkNotificationsAsRead_when_allBelongToUser() {
    List<String> ids = List.of("notif-001", "notif-002", "notif-003");
    when(notificationRepository.markAsRead(ids, USER_ID, TENANT_ID)).thenReturn(ids);

    var command = new MarkNotificationsReadCommand(ids, USER_ID, TENANT_ID);
    List<String> result = useCase.execute(command);

    assertThat(result).hasSize(3);
    assertThat(result).containsExactlyInAnyOrder("notif-001", "notif-002", "notif-003");
  }

  @Test
  void should_throwResourceNotFoundException_when_someNotificationsNotFound() {
    List<String> ids = List.of("notif-001", "notif-002", "notif-999");
    when(notificationRepository.markAsRead(ids, USER_ID, TENANT_ID))
        .thenReturn(List.of("notif-001", "notif-002"));

    var command = new MarkNotificationsReadCommand(ids, USER_ID, TENANT_ID);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("notif-999");
  }

  @Test
  void should_throwResourceNotFoundException_when_notificationBelongsToAnotherUser() {
    List<String> ids = List.of("notif-001");
    when(notificationRepository.markAsRead(ids, USER_ID, TENANT_ID))
        .thenReturn(Collections.emptyList());

    var command = new MarkNotificationsReadCommand(ids, USER_ID, TENANT_ID);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("notif-001");
  }

  @Test
  void should_throwException_when_idsExceedMax100() {
    List<String> ids = IntStream.rangeClosed(1, 101).mapToObj(i -> "notif-" + i).toList();

    var command = new MarkNotificationsReadCommand(ids, USER_ID, TENANT_ID);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(BusinessRuleViolationException.class)
        .hasMessageContaining("100");
  }

  @Test
  void should_acceptExactly100Ids_when_atMaxLimit() {
    List<String> ids = IntStream.rangeClosed(1, 100).mapToObj(i -> "notif-" + i).toList();
    when(notificationRepository.markAsRead(ids, USER_ID, TENANT_ID)).thenReturn(ids);

    var command = new MarkNotificationsReadCommand(ids, USER_ID, TENANT_ID);
    List<String> result = useCase.execute(command);

    assertThat(result).hasSize(100);
  }

  @Test
  void should_throwException_when_idsListIsNull() {
    var command = new MarkNotificationsReadCommand(null, USER_ID, TENANT_ID);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("NotificationIds");
  }

  @Test
  void should_throwException_when_idsListIsEmpty() {
    var command = new MarkNotificationsReadCommand(Collections.emptyList(), USER_ID, TENANT_ID);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("NotificationIds");
  }

  @Test
  void should_throwException_when_userIdIsNull() {
    var command = new MarkNotificationsReadCommand(List.of("notif-001"), null, TENANT_ID);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("UserId");
  }

  @Test
  void should_throwException_when_tenantIdIsBlank() {
    var command = new MarkNotificationsReadCommand(List.of("notif-001"), USER_ID, "  ");

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("TenantId");
  }
}
