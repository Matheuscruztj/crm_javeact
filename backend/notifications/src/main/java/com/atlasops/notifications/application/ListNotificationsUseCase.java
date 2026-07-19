package com.atlasops.notifications.application;

import com.atlasops.notifications.domain.Notification;
import com.atlasops.notifications.domain.ports.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * Use case for listing notifications for a user within a tenant. Results are ordered by creation
 * timestamp descending with configurable pagination.
 */
public class ListNotificationsUseCase {

  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int MAX_PAGE_SIZE = 100;

  private final NotificationRepository notificationRepository;

  public ListNotificationsUseCase(NotificationRepository notificationRepository) {
    this.notificationRepository = notificationRepository;
  }

  /**
   * Lists notifications for a user with pagination.
   *
   * @param query the query containing user/tenant identifiers and pagination params
   * @return a page of notifications ordered by createdAt descending
   */
  public Page<Notification> execute(ListNotificationsQuery query) {
    validateQuery(query);

    int page = Math.max(0, query.page());
    int size = query.size();
    if (size < 1) {
      size = DEFAULT_PAGE_SIZE;
    }
    if (size > MAX_PAGE_SIZE) {
      size = MAX_PAGE_SIZE;
    }

    return notificationRepository.findByUserAndTenantId(
        query.userId(), query.tenantId(), PageRequest.of(page, size));
  }

  private void validateQuery(ListNotificationsQuery query) {
    if (query.userId() == null || query.userId().isBlank()) {
      throw new IllegalArgumentException("UserId must not be null or empty");
    }
    if (query.tenantId() == null || query.tenantId().isBlank()) {
      throw new IllegalArgumentException("TenantId must not be null or empty");
    }
  }
}
