package com.atlasops.notifications.application;

import com.atlasops.notifications.domain.ports.NotificationRepository;
import com.atlasops.shared.domain.exceptions.BusinessRuleViolationException;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Use case for marking notifications as read. Supports single or bulk operations (max 100 IDs per
 * call). Validates that notifications exist and belong to the requesting user.
 */
@Service
public class MarkNotificationsReadUseCase {

  private static final int MAX_IDS_PER_CALL = 100;

  private final NotificationRepository notificationRepository;

  public MarkNotificationsReadUseCase(NotificationRepository notificationRepository) {
    this.notificationRepository = notificationRepository;
  }

  /**
   * Marks the specified notifications as read.
   *
   * @param command the mark-read command with notification IDs and user context
   * @return the list of notification IDs that were successfully marked as read
   * @throws IllegalArgumentException if command is invalid or exceeds max IDs
   * @throws ResourceNotFoundException if any notification is not found or belongs to another user
   */
  public List<String> execute(MarkNotificationsReadCommand command) {
    validateCommand(command);

    List<String> markedIds =
        notificationRepository.markAsRead(
            command.notificationIds(), command.userId(), command.tenantId());

    if (markedIds.size() != command.notificationIds().size()) {
      List<String> notFoundIds =
          command.notificationIds().stream().filter(id -> !markedIds.contains(id)).toList();
      throw new ResourceNotFoundException(
          "Notifications not found or do not belong to user: " + notFoundIds);
    }

    return markedIds;
  }

  private void validateCommand(MarkNotificationsReadCommand command) {
    if (command.notificationIds() == null || command.notificationIds().isEmpty()) {
      throw new IllegalArgumentException("NotificationIds must not be null or empty");
    }
    if (command.notificationIds().size() > MAX_IDS_PER_CALL) {
      throw new BusinessRuleViolationException(
          "Cannot mark more than "
              + MAX_IDS_PER_CALL
              + " notifications as read per call. Got: "
              + command.notificationIds().size());
    }
    if (command.userId() == null || command.userId().isBlank()) {
      throw new IllegalArgumentException("UserId must not be null or empty");
    }
    if (command.tenantId() == null || command.tenantId().isBlank()) {
      throw new IllegalArgumentException("TenantId must not be null or empty");
    }
  }
}
