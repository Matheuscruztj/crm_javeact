package com.atlasops.operations.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.atlasops.operations.domain.Job;
import com.atlasops.operations.domain.JobStatus;
import com.atlasops.operations.domain.ports.JobRepository;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link GetJobDetailsUseCase}.
 *
 * <p>Validates: P0.A.3 — Complement unit tests for operations module
 */
@ExtendWith(MockitoExtension.class)
class GetJobDetailsUseCaseTest {

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private GetJobDetailsUseCase useCase;

    private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");

    @Test
    void should_returnJob_when_foundByIdAndTenant() {
        Job job = Job.create("job-001", "DOCUMENT_PROCESSING", "tenant-alpha", "doc-001", NOW);
        when(jobRepository.findById("job-001", "tenant-alpha")).thenReturn(Optional.of(job));

        Job result = useCase.execute("job-001", "tenant-alpha");

        assertThat(result.getId()).isEqualTo("job-001");
        assertThat(result.getStatus()).isEqualTo(JobStatus.QUEUED);
    }

    @Test
    void should_throwNotFound_when_jobDoesNotExist() {
        when(jobRepository.findById("job-999", "tenant-alpha")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("job-999", "tenant-alpha"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("job-999");
    }

    @Test
    void should_throwNullPointer_when_jobIdIsNull() {
        assertThatThrownBy(() -> useCase.execute(null, "tenant-alpha"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void should_throwNullPointer_when_tenantIdIsNull() {
        assertThatThrownBy(() -> useCase.execute("job-001", null))
                .isInstanceOf(NullPointerException.class);
    }
}
