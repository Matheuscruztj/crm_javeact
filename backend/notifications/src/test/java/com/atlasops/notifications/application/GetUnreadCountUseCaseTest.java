package com.atlasops.notifications.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.atlasops.notifications.domain.ports.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetUnreadCountUseCaseTest {

  @Mock private NotificationRepository notificationRepository;
  private GetUnreadCountUseCase useCase;

  @BeforeEach
  void setUp() { useCase = new GetUnreadCountUseCase(notificationRepository); }

  @Test
  void should_returnCount_when_userHasUnreadNotifications() {
    when(notificationRepository.countUnreadByUser("user-001", "tenant-alpha")).thenReturn(5L);

    long result = useCase.execute(new GetUnreadCountQuery("user-001", "tenant-alpha"));

    assertThat(result).isEqualTo(5L);
  }

  @Test
  void should_returnZero_when_noUnreadNotifications() {
    when(notificationRepository.countUnreadByUser("user-001", "tenant-alpha")).thenReturn(0L);

    long result = useCase.execute(new GetUnreadCountQuery("user-001", "tenant-alpha"));

    assertThat(result).isZero();
  }

  @Test
  void should_throwIllegalArgument_when_userIdIsBlank() {
    assertThatThrownBy(() -> useCase.execute(new GetUnreadCountQuery("  ", "tenant-alpha")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("UserId");
  }

  @Test
  void should_throwIllegalArgument_when_tenantIdIsBlank() {
    assertThatThrownBy(() -> useCase.execute(new GetUnreadCountQuery("user-001", "")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("TenantId");
  }
}
