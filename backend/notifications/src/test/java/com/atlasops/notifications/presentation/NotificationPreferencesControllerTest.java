package com.atlasops.notifications.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.atlasops.notifications.domain.NotificationPreferences;
import com.atlasops.notifications.domain.ports.NotificationPreferencesRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for NotificationPreferencesController.
 * Validates: P2.13 — Per-user notification channel preferences
 */
@ExtendWith(MockitoExtension.class)
class NotificationPreferencesControllerTest {

    private static final String TENANT = "tenant-alpha";
    private static final String USER = "user-001";

    @Mock private NotificationPreferencesRepository preferencesRepository;

    private NotificationPreferencesController controller;

    @BeforeEach
    void setUp() {
        controller = new NotificationPreferencesController(preferencesRepository);
    }

    @Test
    void should_returnPreferences_when_userHasPreferences() {
        NotificationPreferences prefs = new NotificationPreferences(
                USER, TENANT, true, List.of("APPROVAL_DECIDED", "REQUEST_UPDATED"));
        when(preferencesRepository.findOrDefault(USER, TENANT)).thenReturn(prefs);

        ResponseEntity<NotificationPreferencesController.PreferencesResponse> response =
                controller.getPreferences(USER, TENANT);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().userId()).isEqualTo(USER);
        assertThat(response.getBody().emailEnabled()).isTrue();
        assertThat(response.getBody().typesEnabled()).containsExactly("APPROVAL_DECIDED", "REQUEST_UPDATED");
    }

    @Test
    void should_updatePreferences_when_newSettingsProvided() {
        NotificationPreferences updatedPrefs = new NotificationPreferences(
                USER, TENANT, false, List.of("REQUEST_UPDATED"));
        when(preferencesRepository.save(any())).thenReturn(updatedPrefs);

        var request = new NotificationPreferencesController.UpdatePreferencesRequest(
                false, List.of("REQUEST_UPDATED"));
        ResponseEntity<NotificationPreferencesController.PreferencesResponse> response =
                controller.updatePreferences(USER, TENANT, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().emailEnabled()).isFalse();
    }

    @Test
    void should_returnDefaultPreferences_when_noneSet() {
        // findOrDefault returns defaults if no prefs found
        NotificationPreferences defaultPrefs = new NotificationPreferences(
                USER, TENANT, true, List.of());
        when(preferencesRepository.findOrDefault(USER, TENANT)).thenReturn(defaultPrefs);

        ResponseEntity<NotificationPreferencesController.PreferencesResponse> response =
                controller.getPreferences(USER, TENANT);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().typesEnabled()).isEmpty();
    }
}
