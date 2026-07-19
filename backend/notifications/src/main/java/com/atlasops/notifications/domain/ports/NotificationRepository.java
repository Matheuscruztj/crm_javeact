package com.atlasops.notifications.domain.ports;

import com.atlasops.notifications.domain.Notification;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Port defining persistence operations for Notification entities. All query methods require user
 * and tenant context for data isolation.
 */
public interface NotificationRepository {

  /**
   * Persists a new notification.
   *
   * @param notification the notification to persist
   * @return the persisted notification
   */
  Notification save(Notification notification);

  /**
   * Finds all notifications for a given user within a tenant, ordered by creation timestamp
   * descending.
   *
   * @param recipientUserId the user identifier
   * @param tenantId the tenant identifier
   * @param pageable pagination parameters
   * @return a page of notifications for the user
   */
  Page<Notification> findByUserAndTenantId(
      String recipientUserId, String tenantId, Pageable pageable);

  /**
   * Counts unread notifications for a given user within a tenant.
   *
   * @param recipientUserId the user identifier
   * @param tenantId the tenant identifier
   * @return the count of unread notifications
   */
  long countUnreadByUser(String recipientUserId, String tenantId);

  /**
   * Marks the specified notifications as read. Only marks notifications that belong to the
   * specified user and tenant.
   *
   * @param ids the notification identifiers to mark as read
   * @param recipientUserId the user identifier (for ownership validation)
   * @param tenantId the tenant identifier (for isolation)
   * @return the list of notification IDs that were actually marked as read
   */
  List<String> markAsRead(List<String> ids, String recipientUserId, String tenantId);
}
