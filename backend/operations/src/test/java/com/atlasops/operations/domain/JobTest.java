package com.atlasops.operations.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Job} domain entity.
 *
 * <p>Validates: P0.A.3 — Complement unit tests for operations module
 */
class JobTest {

    private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");

    @Test
    void should_createJob_when_allFieldsValid() {
        Job job = Job.create("job-001", "DOCUMENT_PROCESSING", "tenant-alpha", "doc-001", NOW);

        assertThat(job.getId()).isEqualTo("job-001");
        assertThat(job.getType()).isEqualTo("DOCUMENT_PROCESSING");
        assertThat(job.getStatus()).isEqualTo(JobStatus.QUEUED);
        assertThat(job.getTenantId()).isEqualTo("tenant-alpha");
        assertThat(job.getProgressPercent()).isEqualTo(0);
        assertThat(job.getErrorMessage()).isNull();
    }

    @Test
    void should_rejectCreation_when_typeIsBlank() {
        assertThatThrownBy(() -> Job.create("job-001", "", "tenant-alpha", "ref-001", NOW))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("type must not be blank");
    }

    @Test
    void should_transitionToRunning_when_startCalledOnQueuedJob() {
        Job job = Job.create("job-001", "IMPORT", "tenant-alpha", "import-001", NOW);

        job.start(NOW);

        assertThat(job.getStatus()).isEqualTo(JobStatus.RUNNING);
        assertThat(job.getStartedAt()).isEqualTo(NOW);
    }

    @Test
    void should_rejectStart_when_jobIsNotQueued() {
        Job job = Job.create("job-001", "IMPORT", "tenant-alpha", "import-001", NOW);
        job.start(NOW);

        assertThatThrownBy(() -> job.start(NOW))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("QUEUED");
    }

    @Test
    void should_completeJob_when_completedCalled() {
        Job job = Job.create("job-001", "IMPORT", "tenant-alpha", "import-001", NOW);
        job.start(NOW);

        job.complete(NOW);

        assertThat(job.getStatus()).isEqualTo(JobStatus.COMPLETED);
        assertThat(job.getProgressPercent()).isEqualTo(100);
        assertThat(job.getCompletedAt()).isEqualTo(NOW);
    }

    @Test
    void should_failJob_when_failCalledWithErrorMessage() {
        Job job = Job.create("job-001", "IMPORT", "tenant-alpha", "import-001", NOW);
        job.start(NOW);

        job.fail("Connection timeout", NOW);

        assertThat(job.getStatus()).isEqualTo(JobStatus.FAILED);
        assertThat(job.getErrorMessage()).isEqualTo("Connection timeout");
    }

    @Test
    void should_retryJob_when_jobIsInFailedState() {
        Job job = Job.create("job-001", "IMPORT", "tenant-alpha", "import-001", NOW);
        job.start(NOW);
        job.fail("Error", NOW);

        job.retry();

        assertThat(job.getStatus()).isEqualTo(JobStatus.QUEUED);
        assertThat(job.getErrorMessage()).isNull();
        assertThat(job.getProgressPercent()).isEqualTo(0);
    }

    @Test
    void should_rejectRetry_when_jobIsNotFailed() {
        Job job = Job.create("job-001", "IMPORT", "tenant-alpha", "import-001", NOW);

        assertThatThrownBy(() -> job.retry())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("FAILED");
    }

    @Test
    void should_cancelJob_when_jobIsQueued() {
        Job job = Job.create("job-001", "IMPORT", "tenant-alpha", "import-001", NOW);

        job.cancel(NOW);

        assertThat(job.getStatus()).isEqualTo(JobStatus.CANCELLED);
    }

    @Test
    void should_cancelJob_when_jobIsRunning() {
        Job job = Job.create("job-001", "IMPORT", "tenant-alpha", "import-001", NOW);
        job.start(NOW);

        job.cancel(NOW);

        assertThat(job.getStatus()).isEqualTo(JobStatus.CANCELLED);
    }

    @Test
    void should_rejectCancel_when_jobIsCompleted() {
        Job job = Job.create("job-001", "IMPORT", "tenant-alpha", "import-001", NOW);
        job.start(NOW);
        job.complete(NOW);

        assertThatThrownBy(() -> job.cancel(NOW))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_updateProgress_when_validPercent() {
        Job job = Job.create("job-001", "IMPORT", "tenant-alpha", "import-001", NOW);
        job.start(NOW);

        job.updateProgress(50);

        assertThat(job.getProgressPercent()).isEqualTo(50);
    }

    @Test
    void should_rejectProgress_when_exceedsHundred() {
        Job job = Job.create("job-001", "IMPORT", "tenant-alpha", "import-001", NOW);

        assertThatThrownBy(() -> job.updateProgress(101))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
