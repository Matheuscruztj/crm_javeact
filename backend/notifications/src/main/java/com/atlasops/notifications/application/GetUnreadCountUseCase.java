package com.atlasops.notifications.application;

import com.atlasops.notifications.domain.ports.NotificationRepository;

/** Use case for retrieving the count of unread notifications for a user within a tenant. */
public class GetUnreadCountUseCase {

  private final NotificationRepository notificationRepository;

  public GetUnreadCountUseCase(NotificationRepository notificationRepository) {
    this.notificationRepository = notificationRepository;
  }

  /**
   * Returns the count of unread notifications for the specified user and tenant.
   *
   * @param query the query containing user and tenant identifiers
   * @return the number of unread notifications
   */
  public long execute(GetUnreadCountQuery query) {
    validateQuery(query);
    return notificationRepository.countUnreadByUser(query.userId(), query.tenantId());
  }

  private void validateQuery(GetUnreadCountQuery query) {
    if (query.userId() == null || query.userId().isBlank()) {
      throw new IllegalArgumentException("UserId must not be null or empty");
    }
    if (query.tenantId() == null || query.tenantId().isBlank()) {
      throw new IllegalArgumentException("TenantId must not be null or empty");
    }
  }
}
