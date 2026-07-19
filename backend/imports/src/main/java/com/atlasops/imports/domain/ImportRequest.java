package com.atlasops.imports.domain;

/**
 * Represents a request to start a data import operation.
 *
 * @param sourceType the type of import source (e.g., CSV, XLSX, API)
 * @param sourceLocation the location or path of the import source
 * @param tenantId the tenant context for the import
 * @param initiatedBy the identifier of the user who initiated the import
 */
public record ImportRequest(
    String sourceType, String sourceLocation, String tenantId, String initiatedBy) {}
