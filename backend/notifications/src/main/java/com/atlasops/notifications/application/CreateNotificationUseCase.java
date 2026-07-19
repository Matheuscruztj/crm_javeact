package com.atlasops.notifications.application;

import com.atlasops.notifications.domain.Notification;
import com.atlasops.notifications.domain.ports.NotificationRepository;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;

/**
 * Use case for creating an in-app notification. Triggered by approval decisions and request status
 * changes.
 */
public class CreateNotificationUseCase {

  private final NotificationRepository notificationRepository;
  private final IdGenerator idGenerator;
  private final Clock clock;

  public CreateNotificationUseCase(
      NotificationRepository notificationRepository, IdGenerator idGenerator, Clock clock) {
    this.notificationRepository = notificationRepository;
    this.idGenerator = idGenerator;
    this.clock = clock;
  }

  /**
   * Creates a new in-app notification.
   *
   * @param command the create notification command
   * @return the persisted notification
   */
  public Notification execute(CreateNotificationCommand command) {
    validateCommand(command);

    Notification notification =
        Notification.create(
            idGenerator.generate(),
            command.recipientUserId(),
            command.tenantId(),
            command.title(),
            command.message(),
            command.link(),
            clock.now());

    return notificationRepository.save(notification);
  }

  private void validateCommand(CreateNotificationCommand command) {
    if (command.recipientUserId() == null || command.recipientUserId().isBlank()) {
      throw new IllegalArgumentException("RecipientUserId must not be null or empty");
    }
    if (command.tenantId() == null || command.tenantId().isBlank()) {
      throw new IllegalArgumentException("TenantId must not be null or empty");
    }
    if (command.title() == null || command.title().isBlank()) {
      throw new IllegalArgumentException("Title must not be null or empty");
    }
    if (command.message() == null || command.message().isBlank()) {
      throw new IllegalArgumentException("Message must not be null or empty");
    }
  }
}
