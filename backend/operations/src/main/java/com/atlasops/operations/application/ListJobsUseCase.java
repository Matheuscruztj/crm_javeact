package com.atlasops.operations.application;

import com.atlasops.operations.domain.Job;
import com.atlasops.operations.domain.JobStatus;
import com.atlasops.operations.domain.ports.JobRepository;
import java.util.List;
import java.util.Objects;

/**
 * Use case for listing jobs within a tenant with optional status filters.
 */
public class ListJobsUseCase {

  private static final int MAX_PAGE_SIZE = 100;
  private static final int DEFAULT_PAGE_SIZE = 20;

  private final JobRepository jobRepository;

  public ListJobsUseCase(JobRepository jobRepository) {
    this.jobRepository = Objects.requireNonNull(jobRepository);
  }

  /**
   * Lists jobs for the specified tenant.
   *
   * @param query the list query parameters
   * @return page result with jobs
   */
  public JobPageResult execute(ListJobsQuery query) {
    Objects.requireNonNull(query, "Query must not be null");

    int effectiveSize = Math.min(Math.max(query.size(), 1), MAX_PAGE_SIZE);
    int effectivePage = Math.max(query.page(), 0);

    List<Job> jobs = jobRepository.findByTenantId(
        query.tenantId(), query.statuses(), effectivePage, effectiveSize);
    long total = jobRepository.countByTenantId(query.tenantId(), query.statuses());

    return new JobPageResult(jobs, effectivePage, effectiveSize, total);
  }

  /**
   * Query parameters for listing jobs.
   *
   * @param tenantId the tenant to filter by
   * @param statuses optional list of statuses to filter by (null = all statuses)
   * @param page page number (zero-based)
   * @param size page size (max 100, default 20)
   */
  public record ListJobsQuery(
      String tenantId,
      List<JobStatus> statuses,
      int page,
      int size) {

    public ListJobsQuery {
      Objects.requireNonNull(tenantId, "TenantId must not be null");
    }
  }

  /**
   * Page result for a job listing query.
   *
   * @param content the list of jobs for this page
   * @param pageNumber the current page number (zero-based)
   * @param pageSize the page size
   * @param totalElements total number of matching jobs
   */
  public record JobPageResult(
      List<Job> content,
      int pageNumber,
      int pageSize,
      long totalElements) {

    public int totalPages() {
      return pageSize == 0 ? 0 : (int) Math.ceil((double) totalElements / pageSize);
    }
  }
}
