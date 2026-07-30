package com.atlasops.notifications.application;

import com.atlasops.notifications.domain.NotificationPreferences;
import com.atlasops.notifications.domain.ports.NotificationPreferencesRepository;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

/**
 * Use case for updating a user's notification preferences.
 *
 * <p>Validates: P2.13 — Per-user notification channel preferences
 */
@Service
public class UpdatePreferencesUseCase {

    private final NotificationPreferencesRepository preferencesRepository;

    public UpdatePreferencesUseCase(NotificationPreferencesRepository preferencesRepository) {
        this.preferencesRepository = Objects.requireNonNull(preferencesRepository,
                "preferencesRepository must not be null");
    }

    /**
     * Updates the user's notification preferences.
     *
     * @param userId       the user identifier
     * @param tenantId     the tenant context
     * @param emailEnabled whether email notifications are enabled
     * @param typesEnabled list of enabled notification type codes
     * @return the persisted preferences
     */
    public NotificationPreferences execute(
            String userId, String tenantId, boolean emailEnabled, List<String> typesEnabled) {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");

        var prefs = new NotificationPreferences(userId, tenantId, emailEnabled,
                typesEnabled != null ? typesEnabled : List.of());
        return preferencesRepository.save(prefs);
    }
}
