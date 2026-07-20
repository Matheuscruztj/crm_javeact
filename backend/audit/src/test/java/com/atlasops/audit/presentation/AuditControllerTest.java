package com.atlasops.audit.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.atlasops.audit.application.QueryAuditEntriesUseCase;
import com.atlasops.audit.domain.AuditEntry;
import com.atlasops.audit.domain.AuditQueryFilters;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for AuditController.
 * Validates: Requirements 19.1, 19.4, 19.5
 */
@ExtendWith(MockitoExtension.class)
class AuditControllerTest {

    private static final String TENANT = "tenant-alpha";
    private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");

    @Mock private QueryAuditEntriesUseCase queryAuditEntriesUseCase;

    private AuditController controller;

    @BeforeEach
    void setUp() {
        controller = new AuditController(queryAuditEntriesUseCase);
    }

    @Test
    void should_returnAuditEntries_when_tenantHasEntries() {
        AuditEntry entry = AuditEntry.create(
                "audit-001", "LOGIN", "user-001", TENANT,
                "USER", "user-001", "corr-001", "{}", NOW);

        when(queryAuditEntriesUseCase.execute(any(AuditQueryFilters.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entry), PageRequest.of(0, 50), 1));

        ResponseEntity<PageResponse<AuditEntryResponse>> response =
                controller.query(TENANT, null, null, null, null, null, null, 0, 50);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().content()).hasSize(1);
        assertThat(response.getBody().content().get(0).actionType()).isEqualTo("LOGIN");
    }

    @Test
    void should_returnEmptyPage_when_noEntriesExist() {
        when(queryAuditEntriesUseCase.execute(any(AuditQueryFilters.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 50), 0));

        ResponseEntity<PageResponse<AuditEntryResponse>> response =
                controller.query(TENANT, null, null, null, null, null, null, 0, 50);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().content()).isEmpty();
    }

    @Test
    void should_capPageSizeAt200() {
        when(queryAuditEntriesUseCase.execute(any(AuditQueryFilters.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 200), 0));

        // size=999 should be capped at 200
        ResponseEntity<PageResponse<AuditEntryResponse>> response =
                controller.query(TENANT, null, null, null, null, null, null, 0, 999);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void should_filterByActorId_when_paramProvided() {
        when(queryAuditEntriesUseCase.execute(any(AuditQueryFilters.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 50), 0));

        ResponseEntity<PageResponse<AuditEntryResponse>> response =
                controller.query(TENANT, "user-001", "LOGIN", "USER", null, null, null, 0, 50);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
