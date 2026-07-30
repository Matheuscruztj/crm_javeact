package com.atlasops.notifications.application;

import com.atlasops.notifications.domain.NotificationPreferences;
import com.atlasops.notifications.domain.ports.NotificationPreferencesRepository;
import java.util.Objects;
import org.springframework.stereotype.Service;

/**
 * Use case for retrieving a user's notification preferences.
 * Returns defaults when no preferences have been explicitly saved.
 *
 * <p>Validates: P2.13 — Per-user notification channel preferences
 */
@Service
public class GetPreferencesUseCase {

    private final NotificationPreferencesRepository preferencesRepository;

    public GetPreferencesUseCase(NotificationPreferencesRepository preferencesRepository) {
        this.preferencesRepository = Objects.requireNonNull(preferencesRepository,
                "preferencesRepository must not be null");
    }

    /**
     * Retrieves the current user's notification preferences.
     *
     * @param userId   the user identifier
     * @param tenantId the tenant context
     * @return the user's preferences, or defaults if none exist
     */
    public NotificationPreferences execute(String userId, String tenantId) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        return preferencesRepository.findOrDefault(userId, tenantId);
    }
}
