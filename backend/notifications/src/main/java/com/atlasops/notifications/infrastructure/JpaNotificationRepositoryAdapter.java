package com.atlasops.notifications.infrastructure;

import com.atlasops.notifications.domain.Notification;
import com.atlasops.notifications.domain.ports.NotificationRepository;
import com.atlasops.shared.domain.ports.Clock;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA adapter implementing the NotificationRepository port. Provides persistence operations for
 * Notification entities with user and tenant isolation.
 */
@Component
public class JpaNotificationRepositoryAdapter implements NotificationRepository {

  private final SpringDataNotificationRepository springDataRepository;
  private final Clock clock;

  public JpaNotificationRepositoryAdapter(
      SpringDataNotificationRepository springDataRepository, Clock clock) {
    this.springDataRepository = springDataRepository;
    this.clock = clock;
  }

  @Override
  public Notification save(Notification notification) {
    NotificationJpaEntity entity = toEntity(notification);
    NotificationJpaEntity saved = springDataRepository.save(entity);
    return toDomain(saved);
  }

  @Override
  public Page<Notification> findByUserAndTenantId(
      String recipientUserId, String tenantId, Pageable pageable) {
    return springDataRepository
        .findByRecipientUserIdAndTenantIdOrderByCreatedAtDesc(recipientUserId, tenantId, pageable)
        .map(this::toDomain);
  }

  @Override
  public long countUnreadByUser(String recipientUserId, String tenantId) {
    return springDataRepository.countByRecipientUserIdAndTenantIdAndReadFalse(
        recipientUserId, tenantId);
  }

  @Override
  @Transactional
  public List<String> markAsRead(List<String> ids, String recipientUserId, String tenantId) {
    springDataRepository.markAsReadByIdsAndRecipientUserIdAndTenantId(
        ids, recipientUserId, tenantId);

    return springDataRepository
        .findByIdInAndRecipientUserIdAndTenantId(ids, recipientUserId, tenantId)
        .stream()
        .filter(NotificationJpaEntity::isRead)
        .map(NotificationJpaEntity::getId)
        .toList();
  }

  private NotificationJpaEntity toEntity(Notification notification) {
    return new NotificationJpaEntity(
        notification.getId(),
        notification.getTenantId(),
        notification.getRecipientUserId(),
        notification.getTitle(),
        notification.getMessage(),
        notification.isRead(),
        notification.getLink(),
        notification.getCreatedAt(),
        clock.now());
  }

  private Notification toDomain(NotificationJpaEntity entity) {
    return Notification.reconstitute(
        entity.getId(),
        entity.getRecipientUserId(),
        entity.getTenantId(),
        entity.getTitle(),
        entity.getMessage(),
        entity.isRead(),
        entity.getLink(),
        entity.getCreatedAt());
  }
}
