package com.atlasops.operations.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.operations.domain.Job;
import com.atlasops.operations.domain.JobStatus;
import com.atlasops.operations.domain.ports.JobRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListJobsUseCaseTest {

  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final String TENANT_ID = "tenant-alpha";

  @Mock private JobRepository jobRepository;

  private ListJobsUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new ListJobsUseCase(jobRepository);
  }

  @Test
  void should_listJobs_when_tenantHasJobs() {
    Job job = Job.create("job-001", "DOCUMENT_PROCESSING", TENANT_ID, "doc-001", NOW);
    when(jobRepository.findByTenantId(eq(TENANT_ID), any(), anyInt(), anyInt()))
        .thenReturn(List.of(job));
    when(jobRepository.countByTenantId(eq(TENANT_ID), any())).thenReturn(1L);

    var query = new ListJobsUseCase.ListJobsQuery(TENANT_ID, null, 0, 20);
    var result = useCase.execute(query);

    assertThat(result.content()).hasSize(1);
    assertThat(result.totalElements()).isEqualTo(1L);
    assertThat(result.pageNumber()).isEqualTo(0);
    assertThat(result.pageSize()).isEqualTo(20);
  }

  @Test
  void should_filterByStatus_when_statusListProvided() {
    when(jobRepository.findByTenantId(eq(TENANT_ID), eq(List.of(JobStatus.FAILED)), anyInt(), anyInt()))
        .thenReturn(List.of());
    when(jobRepository.countByTenantId(eq(TENANT_ID), eq(List.of(JobStatus.FAILED)))).thenReturn(0L);

    var query = new ListJobsUseCase.ListJobsQuery(TENANT_ID, List.of(JobStatus.FAILED), 0, 20);
    useCase.execute(query);

    verify(jobRepository).findByTenantId(eq(TENANT_ID), eq(List.of(JobStatus.FAILED)), eq(0), eq(20));
  }

  @Test
  void should_capPageSizeAt100_when_sizeExceedsMax() {
    when(jobRepository.findByTenantId(eq(TENANT_ID), any(), anyInt(), eq(100)))
        .thenReturn(List.of());
    when(jobRepository.countByTenantId(any(), any())).thenReturn(0L);

    var query = new ListJobsUseCase.ListJobsQuery(TENANT_ID, null, 0, 500);
    var result = useCase.execute(query);

    assertThat(result.pageSize()).isEqualTo(100);
  }

  @Test
  void should_resetPageToZero_when_pageIsNegative() {
    when(jobRepository.findByTenantId(eq(TENANT_ID), any(), eq(0), anyInt()))
        .thenReturn(List.of());
    when(jobRepository.countByTenantId(any(), any())).thenReturn(0L);

    var query = new ListJobsUseCase.ListJobsQuery(TENANT_ID, null, -5, 20);
    var result = useCase.execute(query);

    assertThat(result.pageNumber()).isEqualTo(0);
  }
}
