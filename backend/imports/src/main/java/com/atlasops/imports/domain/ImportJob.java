package com.atlasops.imports.domain;

/**
 * Represents an import job that has been started.
 *
 * @param jobId the unique identifier assigned to the import job
 * @param status the current status of the import job (e.g., PENDING, RUNNING, COMPLETED, FAILED)
 * @param totalRecords the estimated total number of records to import, or -1 if unknown
 * @param processedRecords the number of records processed so far
 */
public record ImportJob(String jobId, String status, long totalRecords, long processedRecords) {}
