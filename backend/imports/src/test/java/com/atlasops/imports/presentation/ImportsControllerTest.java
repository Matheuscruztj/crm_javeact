package com.atlasops.imports.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.atlasops.imports.application.StartImportUseCase;
import com.atlasops.imports.domain.ImportJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for ImportsController.
 * Validates: P0.C.2 — Imports Module presentation layer
 */
@ExtendWith(MockitoExtension.class)
class ImportsControllerTest {

    private static final String TENANT = "tenant-alpha";

    @Mock private StartImportUseCase startImportUseCase;

    private ImportsController controller;

    @BeforeEach
    void setUp() {
        controller = new ImportsController(startImportUseCase);
    }

    @Test
    void should_startImport_when_validCsvRequest() {
        ImportJob job = new ImportJob("job-001", "PENDING", -1L, 0L);
        when(startImportUseCase.execute(any())).thenReturn(job);

        var request = new ImportsController.StartImportRequest("customers.csv");
        ResponseEntity<ImportsController.ImportJobResponse> response =
                controller.startCsvImport(TENANT, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().jobId()).isEqualTo("job-001");
        assertThat(response.getBody().status()).isEqualTo("PENDING");
    }

    @Test
    void should_returnJobStatus_when_jobIdProvided() {
        ResponseEntity<ImportsController.ImportJobResponse> response =
                controller.getStatus(TENANT, "job-001");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().jobId()).isEqualTo("job-001");
    }

    @Test
    void should_includeLocationHeader_when_jobCreated() {
        ImportJob job = new ImportJob("job-002", "PENDING", 100L, 0L);
        when(startImportUseCase.execute(any())).thenReturn(job);

        var request = new ImportsController.StartImportRequest("data.csv");
        ResponseEntity<ImportsController.ImportJobResponse> response =
                controller.startCsvImport(TENANT, request);

        assertThat(response.getHeaders().getLocation()).isNotNull();
        assertThat(response.getHeaders().getLocation().toString())
                .contains("job-002");
    }
}
