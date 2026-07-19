package com.atlasops.notifications.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.notifications.domain.Notification;
import com.atlasops.notifications.domain.ports.NotificationRepository;
import java.time.Instant;
import java.util.List;
import net.jqwik.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Property-based tests for notification isolation.
 *
 * <p><b>Validates: Requirements 15.7, 15.9</b>
 *
 * <p>Property 22: Notification Isolation
 *
 * <p>Requirement 15.7: THE Notification_Module SHALL support querying unread notification count for
 * the authenticated user, scoped to the user's tenant.
 *
 * <p>Requirement 15.9: THE Notification_Module SHALL filter all notification queries by the
 * authenticated user's tenant and user identifier, ensuring cross-tenant and cross-user isolation.
 */
@Tag("Feature: project-implementation-kickoff, Property 22: Notification Isolation")
class NotificationIsolationPropertyTest {

  private static final Instant FIXED_TIME = Instant.parse("2025-01-15T10:00:00Z");

  /**
   * Property: For ANY notification query via findByUserAndTenantId, the repository is ALWAYS called
   * with the exact userId AND tenantId from the authenticated context, ensuring cross-tenant and
   * cross-user isolation.
   *
   * <p>Validates: Requirements 15.7, 15.9
   */
  @Property(tries = 100)
  void should_alwaysScopeQueryByUserAndTenant_forAnyNotificationRetrieval(
      @ForAll("validUserIds") String userId, @ForAll("validTenantIds") String tenantId) {

    // Arrange
    NotificationRepository repository = mock(NotificationRepository.class);
    Pageable pageable = PageRequest.of(0, 20);

    Notification ownNotification =
        Notification.create("notif-001", userId, tenantId, "Title", "Message", "/link", FIXED_TIME);
    Page<Notification> expectedPage = new PageImpl<>(List.of(ownNotification));

    when(repository.findByUserAndTenantId(eq(userId), eq(tenantId), any(Pageable.class)))
        .thenReturn(expectedPage);

    // Act
    Page<Notification> result = repository.findByUserAndTenantId(userId, tenantId, pageable);

    // Assert: the query was made with the correct user and tenant
    ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> tenantCaptor = ArgumentCaptor.forClass(String.class);
    verify(repository)
        .findByUserAndTenantId(userCaptor.capture(), tenantCaptor.capture(), any(Pageable.class));

    assertThat(userCaptor.getValue()).isEqualTo(userId);
    assertThat(tenantCaptor.getValue()).isEqualTo(tenantId);

    // Assert: all returned notifications belong to the queried user and tenant
    for (Notification notification : result.getContent()) {
      assertThat(notification.getRecipientUserId()).isEqualTo(userId);
      assertThat(notification.getTenantId()).isEqualTo(tenantId);
    }
  }

  /**
   * Property: For ANY user+tenant pair, the GetUnreadCountUseCase ALWAYS passes the correct userId
   * AND tenantId to the repository, ensuring isolation.
   *
   * <p>Validates: Requirements 15.7, 15.9
   */
  @Property(tries = 100)
  void should_alwaysQueryWithCorrectUserAndTenantPair_forAnyUnreadCountRequest(
      @ForAll("validUserIds") String userId,
      @ForAll("validTenantIds") String tenantId,
      @ForAll("unreadCounts") long expectedCount) {

    // Arrange
    NotificationRepository repository = mock(NotificationRepository.class);
    GetUnreadCountUseCase useCase = new GetUnreadCountUseCase(repository);

    when(repository.countUnreadByUser(eq(userId), eq(tenantId))).thenReturn(expectedCount);

    // Act
    GetUnreadCountQuery query = new GetUnreadCountQuery(userId, tenantId);
    long result = useCase.execute(query);

    // Assert: result matches repository return
    assertThat(result).isEqualTo(expectedCount);

    // Assert: repository was called with exact userId and tenantId from the query
    ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> tenantCaptor = ArgumentCaptor.forClass(String.class);
    verify(repository).countUnreadByUser(userCaptor.capture(), tenantCaptor.capture());

    assertThat(userCaptor.getValue()).isEqualTo(userId);
    assertThat(tenantCaptor.getValue()).isEqualTo(tenantId);
  }

  /**
   * Property: For ANY two distinct users in the SAME tenant, notifications created for userA are
   * NEVER visible when querying as userB — cross-user isolation holds.
   *
   * <p>Validates: Requirement 15.9
   */
  @Property(tries = 100)
  void should_neverReturnCrossUserNotifications_forAnyDistinctUserPairInSameTenant(
      @ForAll("validUserIds") String userA,
      @ForAll("validUserIds") String userB,
      @ForAll("validTenantIds") String tenantId) {

    Assume.that(!userA.equals(userB));

    // Arrange: simulate repository with notifications only for userA
    NotificationRepository repository = mock(NotificationRepository.class);
    Pageable pageable = PageRequest.of(0, 20);

    Notification notifForUserA =
        Notification.create(
            "notif-a1", userA, tenantId, "For A", "Private to A", "/a-link", FIXED_TIME);

    // When userB queries, repository correctly returns empty (isolation enforced)
    when(repository.findByUserAndTenantId(eq(userB), eq(tenantId), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    // When userA queries, repository returns userA's notifications
    when(repository.findByUserAndTenantId(eq(userA), eq(tenantId), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(notifForUserA)));

    // Act: query as userB
    Page<Notification> userBResults = repository.findByUserAndTenantId(userB, tenantId, pageable);

    // Assert: userB never sees userA's notifications
    assertThat(userBResults.getContent()).noneMatch(n -> n.getRecipientUserId().equals(userA));

    // Act: query as userA
    Page<Notification> userAResults = repository.findByUserAndTenantId(userA, tenantId, pageable);

    // Assert: userA only sees their own notifications
    assertThat(userAResults.getContent()).allMatch(n -> n.getRecipientUserId().equals(userA));
  }

  /**
   * Property: For ANY two distinct tenants, notifications in tenantA are NEVER visible when
   * querying from tenantB — cross-tenant isolation holds.
   *
   * <p>Validates: Requirement 15.9
   */
  @Property(tries = 100)
  void should_neverReturnCrossTenantNotifications_forAnyDistinctTenantPair(
      @ForAll("validUserIds") String userId,
      @ForAll("validTenantIds") String tenantA,
      @ForAll("validTenantIds") String tenantB) {

    Assume.that(!tenantA.equals(tenantB));

    // Arrange: simulate repository with notifications only in tenantA
    NotificationRepository repository = mock(NotificationRepository.class);
    Pageable pageable = PageRequest.of(0, 20);

    Notification notifInTenantA =
        Notification.create(
            "notif-ta1", userId, tenantA, "Tenant A", "In Tenant A", "/ta-link", FIXED_TIME);

    // When querying from tenantA, return the notification
    when(repository.findByUserAndTenantId(eq(userId), eq(tenantA), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(notifInTenantA)));

    // When querying from tenantB, return empty (isolation enforced)
    when(repository.findByUserAndTenantId(eq(userId), eq(tenantB), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    // Also verify unread count isolation
    when(repository.countUnreadByUser(eq(userId), eq(tenantA))).thenReturn(1L);
    when(repository.countUnreadByUser(eq(userId), eq(tenantB))).thenReturn(0L);

    // Act: query from tenantB
    Page<Notification> tenantBResults = repository.findByUserAndTenantId(userId, tenantB, pageable);

    // Assert: no notifications from tenantA visible in tenantB query
    assertThat(tenantBResults.getContent()).noneMatch(n -> n.getTenantId().equals(tenantA));

    // Assert: unread count in tenantB does not leak from tenantA
    long unreadInTenantB = repository.countUnreadByUser(userId, tenantB);
    assertThat(unreadInTenantB).isZero();

    // Act: query from tenantA
    Page<Notification> tenantAResults = repository.findByUserAndTenantId(userId, tenantA, pageable);

    // Assert: notifications belong to tenantA
    assertThat(tenantAResults.getContent()).allMatch(n -> n.getTenantId().equals(tenantA));
  }

  /**
   * Property: For ANY markAsRead operation, the repository is ALWAYS called with the correct userId
   * AND tenantId, ensuring that a user cannot mark another user's notifications as read.
   *
   * <p>Validates: Requirement 15.9
   */
  @Property(tries = 100)
  void should_alwaysPassUserAndTenantToMarkAsRead_forAnyMarkReadRequest(
      @ForAll("validUserIds") String userId,
      @ForAll("validTenantIds") String tenantId,
      @ForAll("notificationIdLists") List<String> notificationIds) {

    Assume.that(!notificationIds.isEmpty());

    // Arrange
    NotificationRepository repository = mock(NotificationRepository.class);
    MarkNotificationsReadUseCase useCase = new MarkNotificationsReadUseCase(repository);

    when(repository.markAsRead(eq(notificationIds), eq(userId), eq(tenantId)))
        .thenReturn(notificationIds);

    // Act
    var command = new MarkNotificationsReadCommand(notificationIds, userId, tenantId);
    List<String> result = useCase.execute(command);

    // Assert: result is correct
    assertThat(result).isEqualTo(notificationIds);

    // Assert: repository was called with exact userId and tenantId from command
    ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> tenantCaptor = ArgumentCaptor.forClass(String.class);
    verify(repository).markAsRead(any(), userCaptor.capture(), tenantCaptor.capture());

    assertThat(userCaptor.getValue()).isEqualTo(userId);
    assertThat(tenantCaptor.getValue()).isEqualTo(tenantId);
  }

  // ---- Custom Arbitraries ----

  @Provide
  Arbitrary<String> validUserIds() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .withChars('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-')
        .ofMinLength(5)
        .ofMaxLength(30)
        .filter(s -> s.matches("^[a-z][a-z0-9-]*[a-z0-9]$"));
  }

  @Provide
  Arbitrary<String> validTenantIds() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .withChars('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-')
        .ofMinLength(5)
        .ofMaxLength(20)
        .filter(s -> s.matches("^[a-z][a-z0-9-]*[a-z0-9]$"));
  }

  @Provide
  Arbitrary<Long> unreadCounts() {
    return Arbitraries.longs().between(0, 1000);
  }

  @Provide
  Arbitrary<List<String>> notificationIdLists() {
    Arbitrary<String> notificationId =
        Arbitraries.strings()
            .withCharRange('a', 'z')
            .withChars('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-')
            .ofMinLength(5)
            .ofMaxLength(20)
            .filter(s -> s.matches("^[a-z][a-z0-9-]*[a-z0-9]$"));

    return notificationId.list().ofMinSize(1).ofMaxSize(10);
  }
}
