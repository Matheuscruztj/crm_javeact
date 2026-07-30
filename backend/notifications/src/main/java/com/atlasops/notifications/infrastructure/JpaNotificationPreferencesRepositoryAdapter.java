package com.atlasops.notifications.infrastructure;

import com.atlasops.notifications.domain.NotificationPreferences;
import com.atlasops.notifications.domain.ports.NotificationPreferencesRepository;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class JpaNotificationPreferencesRepositoryAdapter
    implements NotificationPreferencesRepository {

  private final SpringDataNotificationPreferencesRepository repository;

  public JpaNotificationPreferencesRepositoryAdapter(
      SpringDataNotificationPreferencesRepository repository) {
    this.repository = repository;
  }

  @Override
  public NotificationPreferences findOrDefault(String userId, String tenantId) {
    return repository
        .findById(new NotificationPreferencesJpaEntity.NotificationPreferencesId(userId, tenantId))
        .map(this::toDomain)
        .orElseGet(() -> NotificationPreferences.defaults(userId, tenantId));
  }

  @Override
  public NotificationPreferences save(NotificationPreferences preferences) {
    var entity = repository.save(toEntity(preferences));
    return toDomain(entity);
  }

  private NotificationPreferencesJpaEntity toEntity(NotificationPreferences preferences) {
    return new NotificationPreferencesJpaEntity(
        preferences.userId(),
        preferences.tenantId(),
        preferences.emailEnabled(),
        preferences.typesEnabled(),
        Instant.now());
  }

  private NotificationPreferences toDomain(NotificationPreferencesJpaEntity entity) {
    return new NotificationPreferences(
        entity.getUserId(),
        entity.getTenantId(),
        entity.isEmailEnabled(),
        entity.getTypesEnabled());
  }
}
