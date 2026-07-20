package com.atlasops.activities.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.atlasops.activities.application.GetEntityActivitiesUseCase;
import com.atlasops.activities.application.GetTenantActivityFeedUseCase;
import com.atlasops.activities.domain.Activity;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for ActivityController.
 * Validates: Requirements 14.2, 14.3, 14.4, 14.5
 */
@ExtendWith(MockitoExtension.class)
class ActivityControllerTest {

    private static final String TENANT = "tenant-alpha";
    private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");

    @Mock private GetTenantActivityFeedUseCase getTenantActivityFeedUseCase;
    @Mock private GetEntityActivitiesUseCase getEntityActivitiesUseCase;

    private ActivityController controller;

    @BeforeEach
    void setUp() {
        controller = new ActivityController(getTenantActivityFeedUseCase, getEntityActivitiesUseCase);
    }

    private Activity anActivity(String id) {
        return Activity.create(id, "CUSTOMER", "cust-001", "CREATED",
                "user-001", TENANT, "Customer created", "evt-" + id, NOW);
    }

    @Test
    void should_returnFeed_when_noEntityFilter() {
        when(getTenantActivityFeedUseCase.execute(any()))
                .thenReturn(new PageImpl<>(List.of(anActivity("act-001")), PageRequest.of(0, 20), 1));

        ResponseEntity<PageResponse<ActivityResponse>> response =
                controller.list(TENANT, "user-001", "ADMIN", null, null, 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().content()).hasSize(1);
        assertThat(response.getBody().content().get(0).entityType()).isEqualTo("CUSTOMER");
    }

    @Test
    void should_returnEntityActivities_when_entityFilterProvided() {
        when(getEntityActivitiesUseCase.execute(anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(anActivity("act-002")), PageRequest.of(0, 20), 1));

        ResponseEntity<PageResponse<ActivityResponse>> response =
                controller.list(TENANT, "user-001", "ADMIN", "CUSTOMER", "cust-001", 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().content()).hasSize(1);
    }

    @Test
    void should_returnEmptyPage_when_noActivitiesExist() {
        when(getTenantActivityFeedUseCase.execute(any()))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        ResponseEntity<PageResponse<ActivityResponse>> response =
                controller.list(TENANT, "user-001", "ADMIN", null, null, 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().content()).isEmpty();
    }
}
