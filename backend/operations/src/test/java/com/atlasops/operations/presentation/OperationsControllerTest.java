package com.atlasops.operations.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.operations.application.CancelJobUseCase;
import com.atlasops.operations.application.GetJobDetailsUseCase;
import com.atlasops.operations.application.GetProjectionsUseCase;
import com.atlasops.operations.application.ListJobsUseCase;
import com.atlasops.operations.application.RetryJobUseCase;
import com.atlasops.operations.domain.Job;
import com.atlasops.operations.domain.JobStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
 * Unit tests for OperationsController.
 * Validates: P0.F.3 — Operations Job UI (retry/cancel)
 */
@ExtendWith(MockitoExtension.class)
class OperationsControllerTest {

    private static final String TENANT = "tenant-alpha";

    @Mock private ListJobsUseCase listJobsUseCase;
    @Mock private GetJobDetailsUseCase getJobDetailsUseCase;
    @Mock private RetryJobUseCase retryJobUseCase;
    @Mock private CancelJobUseCase cancelJobUseCase;
    @Mock private GetProjectionsUseCase getProjectionsUseCase;

    private OperationsController controller;

    @BeforeEach
    void setUp() {
        controller = new OperationsController(
                listJobsUseCase, getJobDetailsUseCase, retryJobUseCase,
                cancelJobUseCase, getProjectionsUseCase);
    }

    private Job aJob(String id, JobStatus status) {
        Job j = Job.create(id, "DOCUMENT_PROCESSING", TENANT, null,
                Instant.parse("2025-01-15T10:00:00Z"));
        if (status == JobStatus.RUNNING) j.start(Instant.parse("2025-01-15T10:00:01Z"));
        return j;
    }

    @Test
    void should_listJobs_when_tenantHasJobs() {
        when(listJobsUseCase.execute(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(
                        List.of(aJob("job-001", JobStatus.RUNNING)), PageRequest.of(0, 20), 1));

        ResponseEntity<?> response = controller.listJobs(TENANT, null, 0, 20);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void should_retryJob_when_jobIsFailed() {
        Job retriedJob = aJob("job-001", JobStatus.QUEUED);
        when(retryJobUseCase.execute("job-001", TENANT)).thenReturn(retriedJob);

        ResponseEntity<?> response = controller.retryJob(TENANT, "job-001");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(retryJobUseCase).execute("job-001", TENANT);
    }

    @Test
    void should_cancelJob_when_jobIsRunning() {
        Job cancelledJob = aJob("job-001", JobStatus.CANCELLED);
        when(cancelJobUseCase.execute("job-001", TENANT)).thenReturn(cancelledJob);

        ResponseEntity<?> response = controller.cancelJob(TENANT, "job-001");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(cancelJobUseCase).execute("job-001", TENANT);
    }

    @Test
    void should_returnNotFound_when_jobDoesNotExist() {
        when(getJobDetailsUseCase.execute("missing-job", TENANT)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getJob(TENANT, "missing-job");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
