package com.atlasops.imports.application;

import com.atlasops.imports.domain.ImportJob;
import com.atlasops.imports.domain.ImportRequest;
import com.atlasops.imports.domain.ports.ImportPort;
import java.util.Objects;

/**
 * Use case for starting a data import operation.
 *
 * <p>Validates: P0.C.2 — Imports module with use case layer
 */
public class StartImportUseCase {

    private final ImportPort importPort;

    public StartImportUseCase(ImportPort importPort) {
        this.importPort = Objects.requireNonNull(importPort, "ImportPort must not be null");
    }

    /**
     * Starts a new import based on the given request.
     *
     * @param request the import request
     * @return the created import job
     */
    public ImportJob execute(ImportRequest request) {
        Objects.requireNonNull(request, "ImportRequest must not be null");
        if (request.tenantId() == null || request.tenantId().isBlank()) {
            throw new IllegalArgumentException("TenantId must not be blank");
        }
        if (request.sourceType() == null || request.sourceType().isBlank()) {
            throw new IllegalArgumentException("SourceType must not be blank");
        }
        return importPort.startImport(request);
    }
}
