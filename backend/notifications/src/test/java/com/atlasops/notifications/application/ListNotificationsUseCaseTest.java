package com.atlasops.notifications.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.notifications.domain.Notification;
import com.atlasops.notifications.domain.ports.NotificationRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ListNotificationsUseCaseTest {

  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final String USER_ID = "user-001";
  private static final String TENANT = "tenant-alpha";

  @Mock private NotificationRepository notificationRepository;
  private ListNotificationsUseCase useCase;

  @BeforeEach
  void setUp() { useCase = new ListNotificationsUseCase(notificationRepository); }

  @Test
  void should_returnPage_when_userHasNotifications() {
    Notification n = Notification.create("notif-001", USER_ID, TENANT, "Title", "Msg", null, NOW);
    Page<Notification> page = new PageImpl<>(List.of(n));
    when(notificationRepository.findByUserAndTenantId(eq(USER_ID), eq(TENANT), any(Pageable.class)))
        .thenReturn(page);

    var query = new ListNotificationsQuery(USER_ID, TENANT, 0, 20);
    Page<Notification> result = useCase.execute(query);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getTitle()).isEqualTo("Title");
  }

  @Test
  void should_capSizeAt100_when_sizeExceedsMax() {
    when(notificationRepository.findByUserAndTenantId(anyString(), anyString(), any()))
        .thenReturn(Page.empty());

    useCase.execute(new ListNotificationsQuery(USER_ID, TENANT, 0, 500));

    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(notificationRepository).findByUserAndTenantId(eq(USER_ID), eq(TENANT), captor.capture());
    assertThat(captor.getValue().getPageSize()).isEqualTo(100);
  }

  @Test
  void should_useDefaultSize_when_sizeIsZero() {
    when(notificationRepository.findByUserAndTenantId(anyString(), anyString(), any()))
        .thenReturn(Page.empty());

    useCase.execute(new ListNotificationsQuery(USER_ID, TENANT, 0, 0));

    ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
    verify(notificationRepository).findByUserAndTenantId(eq(USER_ID), eq(TENANT), captor.capture());
    assertThat(captor.getValue().getPageSize()).isEqualTo(20);
  }

  @Test
  void should_resetPageToZero_when_negative() {
    when(notificationRepository.findByUserAndTenantId(anyString(), anyString(), any()))
        .thenReturn(Page.empty());

    useCase.execute(new ListNotificationsQuery(USER_ID, TENANT, -1, 20));

    verify(notificationRepository).findByUserAndTenantId(USER_ID, TENANT, PageRequest.of(0, 20));
  }

  @Test
  void should_throwIllegalArgument_when_userIdIsBlank() {
    assertThatThrownBy(() -> useCase.execute(new ListNotificationsQuery("", TENANT, 0, 20)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("UserId");
  }

  @Test
  void should_throwIllegalArgument_when_tenantIdIsBlank() {
    assertThatThrownBy(() -> useCase.execute(new ListNotificationsQuery(USER_ID, "", 0, 20)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("TenantId");
  }

  private static String anyString() { return any(String.class); }
}
