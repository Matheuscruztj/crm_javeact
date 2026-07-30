package com.atlasops.notifications.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataNotificationPreferencesRepository
    extends JpaRepository<NotificationPreferencesJpaEntity, NotificationPreferencesJpaEntity.NotificationPreferencesId> {}
