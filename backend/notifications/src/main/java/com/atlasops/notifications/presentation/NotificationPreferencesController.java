package com.atlasops.notifications.presentation;

import com.atlasops.notifications.domain.NotificationPreferences;
import com.atlasops.notifications.domain.ports.NotificationPreferencesRepository;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing per-user notification preferences.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET /api/v1/notifications/preferences — get current user's preferences
 *   <li>PUT /api/v1/notifications/preferences — update current user's preferences
 * </ul>
 *
 * <p>Validates: P2.13 — Per-user notification channel preferences
 */
@RestController
@RequestMapping("/api/v1/notifications/preferences")
public class NotificationPreferencesController {

    private final NotificationPreferencesRepository preferencesRepository;

    public NotificationPreferencesController(NotificationPreferencesRepository preferencesRepository) {
        this.preferencesRepository = preferencesRepository;
    }

    /**
     * Returns the current user's notification preferences.
     *
     * @param userId   the user ID (from X-User-ID header)
     * @param tenantId the tenant ID (from X-Tenant-ID header)
     * @return 200 OK with the user's preferences
     */
    @GetMapping
    public ResponseEntity<PreferencesResponse> getPreferences(
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader("X-Tenant-ID") String tenantId) {
        NotificationPreferences prefs = preferencesRepository.findOrDefault(userId, tenantId);
        return ResponseEntity.ok(PreferencesResponse.from(prefs));
    }

    /**
     * Updates the current user's notification preferences.
     *
     * @param userId   the user ID (from X-User-ID header)
     * @param tenantId the tenant ID (from X-Tenant-ID header)
     * @param request  the preferences to update
     * @return 200 OK with the updated preferences
     */
    @PutMapping
    public ResponseEntity<PreferencesResponse> updatePreferences(
            @RequestHeader("X-User-ID") String userId,
            @RequestHeader("X-Tenant-ID") String tenantId,
            @RequestBody UpdatePreferencesRequest request) {
        var prefs = new NotificationPreferences(
                userId, tenantId, request.emailEnabled(), request.typesEnabled());
        NotificationPreferences saved = preferencesRepository.save(prefs);
        return ResponseEntity.ok(PreferencesResponse.from(saved));
    }

    /**
     * Response DTO for notification preferences.
     */
    public record PreferencesResponse(
            String userId, String tenantId, boolean emailEnabled, List<String> typesEnabled) {

        public static PreferencesResponse from(NotificationPreferences prefs) {
            return new PreferencesResponse(
                    prefs.userId(), prefs.tenantId(), prefs.emailEnabled(), prefs.typesEnabled());
        }
    }

    /**
     * Request body for updating notification preferences.
     */
    public record UpdatePreferencesRequest(boolean emailEnabled, List<String> typesEnabled) {}
}
