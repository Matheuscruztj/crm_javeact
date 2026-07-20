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
import com.atlasops.shared.domain.ports.Clock;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CancelJobUseCaseTest {

  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final Instant CANCEL_TIME = Instant.parse("2025-01-15T11:00:00Z");
  private static final String TENANT_ID = "tenant-alpha";

  @Mock private JobRepository jobRepository;
  @Mock private Clock clock;

  private CancelJobUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new CancelJobUseCase(jobRepository, clock);
  }

  @Test
  void should_cancelJob_when_jobIsQueued() {
    Job job = Job.create("job-001", "DOCUMENT_PROCESSING", TENANT_ID, "doc-001", NOW);
    when(jobRepository.findById("job-001", TENANT_ID)).thenReturn(Optional.of(job));
    when(jobRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(clock.now()).thenReturn(CANCEL_TIME);

    Job result = useCase.execute("job-001", TENANT_ID);

    assertThat(result.getStatus()).isEqualTo(JobStatus.CANCELLED);
    assertThat(result.getCompletedAt()).isEqualTo(CANCEL_TIME);
    verify(jobRepository).save(any());
  }

  @Test
  void should_cancelJob_when_jobIsRunning() {
    Job job = Job.create("job-001", "DOCUMENT_PROCESSING", TENANT_ID, "doc-001", NOW);
    job.start(NOW);
    when(jobRepository.findById("job-001", TENANT_ID)).thenReturn(Optional.of(job));
    when(jobRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(clock.now()).thenReturn(CANCEL_TIME);

    Job result = useCase.execute("job-001", TENANT_ID);

    assertThat(result.getStatus()).isEqualTo(JobStatus.CANCELLED);
  }

  @Test
  void should_throwResourceNotFound_when_jobDoesNotExist() {
    when(jobRepository.findById("job-999", TENANT_ID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.execute("job-999", TENANT_ID))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void should_throwIllegalState_when_jobIsAlreadyCompleted() {
    Job job = Job.create("job-001", "DOCUMENT_PROCESSING", TENANT_ID, "doc-001", NOW);
    job.start(NOW);
    job.complete(NOW);
    when(jobRepository.findById("job-001", TENANT_ID)).thenReturn(Optional.of(job));

    assertThatThrownBy(() -> useCase.execute("job-001", TENANT_ID))
        .isInstanceOf(IllegalStateException.class);
  }
}
