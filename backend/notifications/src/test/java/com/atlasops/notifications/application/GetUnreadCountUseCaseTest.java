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

  private static final String USER_ID = "user-001";
  private static final String TENANT_ID = "tenant-alpha";

  @Mock private NotificationRepository notificationRepository;

  private GetUnreadCountUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new GetUnreadCountUseCase(notificationRepository);
  }

  @Test
  void should_returnUnreadCount_when_userHasUnreadNotifications() {
    when(notificationRepository.countUnreadByUser(USER_ID, TENANT_ID)).thenReturn(5L);

    var query = new GetUnreadCountQuery(USER_ID, TENANT_ID);
    long result = useCase.execute(query);

    assertThat(result).isEqualTo(5L);
  }

  @Test
  void should_returnZero_when_userHasNoUnreadNotifications() {
    when(notificationRepository.countUnreadByUser(USER_ID, TENANT_ID)).thenReturn(0L);

    var query = new GetUnreadCountQuery(USER_ID, TENANT_ID);
    long result = useCase.execute(query);

    assertThat(result).isZero();
  }

  @Test
  void should_scopeByUserAndTenant_when_queryExecuted() {
    when(notificationRepository.countUnreadByUser("user-002", "tenant-beta")).thenReturn(12L);

    var query = new GetUnreadCountQuery("user-002", "tenant-beta");
    long result = useCase.execute(query);

    assertThat(result).isEqualTo(12L);
  }

  @Test
  void should_throwException_when_userIdIsNull() {
    var query = new GetUnreadCountQuery(null, TENANT_ID);

    assertThatThrownBy(() -> useCase.execute(query))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("UserId");
  }

  @Test
  void should_throwException_when_userIdIsBlank() {
    var query = new GetUnreadCountQuery("  ", TENANT_ID);

    assertThatThrownBy(() -> useCase.execute(query))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("UserId");
  }

  @Test
  void should_throwException_when_tenantIdIsNull() {
    var query = new GetUnreadCountQuery(USER_ID, null);

    assertThatThrownBy(() -> useCase.execute(query))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("TenantId");
  }

  @Test
  void should_throwException_when_tenantIdIsBlank() {
    var query = new GetUnreadCountQuery(USER_ID, "");

    assertThatThrownBy(() -> useCase.execute(query))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("TenantId");
  }
}
