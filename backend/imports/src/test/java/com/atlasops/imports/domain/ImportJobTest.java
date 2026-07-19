package com.atlasops.imports.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ImportJob} domain record.
 *
 * <p>Validates: P0.A.3 — Complement unit tests for imports module
 */
class ImportJobTest {

    @Test
    void should_createImportJob_when_allFieldsProvided() {
        ImportJob job = new ImportJob("job-001", "RUNNING", 1000L, 250L);

        assertThat(job.jobId()).isEqualTo("job-001");
        assertThat(job.status()).isEqualTo("RUNNING");
        assertThat(job.totalRecords()).isEqualTo(1000L);
        assertThat(job.processedRecords()).isEqualTo(250L);
    }

    @Test
    void should_allowUnknownTotalRecords_when_totalIsNegativeOne() {
        ImportJob job = new ImportJob("job-001", "PENDING", -1L, 0L);

        assertThat(job.totalRecords()).isEqualTo(-1L);
    }
}
