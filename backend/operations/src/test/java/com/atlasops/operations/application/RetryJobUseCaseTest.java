package com.atlasops.operations.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.operations.domain.Job;
import com.atlasops.operations.domain.JobStatus;
import com.atlasops.operations.domain.ports.JobRepository;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RetryJobUseCaseTest {

  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final String TENANT_ID = "tenant-alpha";

  @Mock private JobRepository jobRepository;

  private RetryJobUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new RetryJobUseCase(jobRepository);
  }

  @Test
  void should_retryJob_when_jobIsInFailedState() {
    Job job = Job.create("job-001", "DOCUMENT_PROCESSING", TENANT_ID, "doc-001", NOW);
    job.start(NOW);
    job.fail("Connection error", NOW);

    when(jobRepository.findById("job-001", TENANT_ID)).thenReturn(Optional.of(job));
    when(jobRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    Job result = useCase.execute("job-001", TENANT_ID);

    assertThat(result.getStatus()).isEqualTo(JobStatus.QUEUED);
    assertThat(result.getErrorMessage()).isNull();
    assertThat(result.getProgressPercent()).isZero();
    verify(jobRepository).save(any());
  }

  @Test
  void should_throwResourceNotFound_when_jobDoesNotExist() {
    when(jobRepository.findById("job-999", TENANT_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute("job-999", TENANT_ID))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("job-999");
  }

  @Test
  void should_throwIllegalState_when_jobIsNotFailed() {
    Job job = Job.create("job-001", "DOCUMENT_PROCESSING", TENANT_ID, "doc-001", NOW);
    // QUEUED state — cannot retry
    when(jobRepository.findById("job-001", TENANT_ID)).thenReturn(Optional.of(job));

    assertThatThrownBy(() -> useCase.execute("job-001", TENANT_ID))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("FAILED");
  }

  @Test
  void should_throwNullPointer_when_jobIdIsNull() {
    assertThatThrownBy(() -> useCase.execute(null, TENANT_ID))
        .isInstanceOf(NullPointerException.class);
  }
}
