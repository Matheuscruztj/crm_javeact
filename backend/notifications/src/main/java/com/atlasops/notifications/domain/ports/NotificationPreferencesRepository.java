package com.atlasops.notifications.domain.ports;

import com.atlasops.notifications.domain.NotificationPreferences;

/**
 * Repository port for notification preferences.
 *
 * <p>Validates: P2.13 — Per-user notification channel preferences
 */
public interface NotificationPreferencesRepository {

    /**
     * Finds notification preferences for a user in a tenant.
     * If no preferences are saved, returns sensible defaults.
     *
     * @param userId   the user identifier
     * @param tenantId the tenant context
     * @return the user's preferences, or defaults if none exist
     */
    NotificationPreferences findOrDefault(String userId, String tenantId);

    /**
     * Persists notification preferences for a user.
     *
     * @param preferences the preferences to save (insert or update)
     * @return the persisted preferences
     */
    NotificationPreferences save(NotificationPreferences preferences);
}
