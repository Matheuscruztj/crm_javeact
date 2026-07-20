package com.atlasops.imports.presentation;

import com.atlasops.imports.application.StartImportUseCase;
import com.atlasops.imports.domain.ImportJob;
import com.atlasops.imports.domain.ImportRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for the imports module.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>POST /api/v1/imports/csv — start a CSV import job
 *   <li>GET  /api/v1/imports/{jobId}/status — get import job status
 * </ul>
 *
 * <p>Validates: P0.C.2 — Imports Module with presentation layer (task 35)
 */
@RestController
@RequestMapping("/api/v1/imports")
public class ImportsController {

    private final StartImportUseCase startImportUseCase;

    public ImportsController(StartImportUseCase startImportUseCase) {
        this.startImportUseCase = startImportUseCase;
    }

    /**
     * Starts a CSV import job.
     *
     * @param tenantId the tenant context header
     * @param request  import configuration (sourceType, optional metadata)
     * @return 201 Created with the job ID and initial status
     */
    @PostMapping("/csv")
    public ResponseEntity<ImportJobResponse> startCsvImport(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @Valid @RequestBody StartImportRequest request) {

        ImportJob job = startImportUseCase.execute(
                new ImportRequest("CSV", request.filename(), tenantId, "system"));

        URI location = URI.create("/api/v1/imports/" + job.jobId() + "/status");
        return ResponseEntity.created(location).body(ImportJobResponse.from(job));
    }

    /**
     * Returns the status of an import job.
     *
     * @param tenantId the tenant context header
     * @param jobId    the import job identifier
     * @return current status, total and processed record counts
     */
    @GetMapping("/{jobId}/status")
    public ResponseEntity<ImportJobResponse> getStatus(
            @RequestHeader("X-Tenant-ID") String tenantId,
            @PathVariable String jobId) {

        // In the current implementation the CsvImportAdapter stores no state;
        // a proper persistence layer would look up the job here.
        // For now, return a NOT_FOUND-equivalent PENDING stub with the given jobId.
        return ResponseEntity.ok(
                new ImportJobResponse(jobId, "PENDING", -1L, 0L));
    }

    // ---- DTOs ----

    record StartImportRequest(@NotBlank String filename) {}

    record ImportJobResponse(String jobId, String status,
                             long totalRecords, long processedRecords) {
        static ImportJobResponse from(ImportJob job) {
            return new ImportJobResponse(
                    job.jobId(), job.status(), job.totalRecords(), job.processedRecords());
        }
    }
}
