package com.atlasops.notifications.domain;

import java.util.List;
import java.util.Objects;

/**
 * Value object representing a user's notification channel preferences.
 * Controls which notification types and channels are active for a user.
 *
 * <p>Validates: P2.13 — Per-user notification channel preferences
 *
 * @param userId       the user this preference belongs to
 * @param tenantId     the tenant context
 * @param emailEnabled whether email notifications are enabled (default: true)
 * @param typesEnabled list of notification type codes the user wants to receive
 *                     (empty = all types)
 */
public record NotificationPreferences(
        String userId,
        String tenantId,
        boolean emailEnabled,
        List<String> typesEnabled) {

    public NotificationPreferences {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        typesEnabled = typesEnabled != null ? List.copyOf(typesEnabled) : List.of();
    }

    /**
     * Creates default preferences (all channels enabled, all types active).
     *
     * @param userId   the user
     * @param tenantId the tenant
     * @return default preferences
     */
    public static NotificationPreferences defaults(String userId, String tenantId) {
        return new NotificationPreferences(userId, tenantId, true, List.of());
    }

    /**
     * Returns true if a notification of the given type should be sent to this user.
     * If {@code typesEnabled} is empty, all types are considered active.
     *
     * @param notificationType the type to check
     * @return true if this type is enabled
     */
    public boolean isTypeEnabled(String notificationType) {
        return typesEnabled.isEmpty() || typesEnabled.contains(notificationType);
    }
}
