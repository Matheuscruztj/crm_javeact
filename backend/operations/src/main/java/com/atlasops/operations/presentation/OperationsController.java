package com.atlasops.operations.presentation;

import com.atlasops.operations.application.GetJobDetailsUseCase;
import com.atlasops.operations.application.ListJobsUseCase;
import com.atlasops.operations.application.ListJobsUseCase.JobPageResult;
import com.atlasops.operations.application.ListJobsUseCase.ListJobsQuery;
import com.atlasops.operations.domain.Job;
import com.atlasops.operations.domain.JobStatus;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for operations monitoring — job management and health status.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET /api/v1/operations/jobs — list jobs with pagination
 *   <li>GET /api/v1/operations/jobs/{id} — get job details
 * </ul>
 *
 * <p>Validates: Requirements P0.I.1
 */
@RestController
@RequestMapping("/api/v1/operations")
public class OperationsController {

  private final GetJobDetailsUseCase getJobDetailsUseCase;
  private final ListJobsUseCase listJobsUseCase;

  public OperationsController(
      GetJobDetailsUseCase getJobDetailsUseCase,
      ListJobsUseCase listJobsUseCase) {
    this.getJobDetailsUseCase = getJobDetailsUseCase;
    this.listJobsUseCase = listJobsUseCase;
  }

  /**
   * Lists jobs for the tenant with optional status filter and pagination.
   */
  @GetMapping("/jobs")
  public ResponseEntity<PageResponse<JobResponse>> listJobs(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @RequestParam(required = false) List<String> status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

    List<JobStatus> statusFilters = status != null
        ? status.stream().map(JobStatus::valueOf).toList()
        : List.of();

    ListJobsQuery query = new ListJobsQuery(tenantId, statusFilters, page, size);
    JobPageResult result = listJobsUseCase.execute(query);

    List<JobResponse> content = result.content().stream().map(JobResponse::from).toList();

    var pageMetadata = new PageResponse.PageMetadata(
        result.pageNumber(), result.pageSize(), result.totalElements(), result.totalPages());

    return ResponseEntity.ok(new PageResponse<>(content, pageMetadata));
  }

  /**
   * Retrieves job details by identifier.
   */
  @GetMapping("/jobs/{id}")
  public ResponseEntity<JobResponse> getJob(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @PathVariable String id) {
    Job job = getJobDetailsUseCase.execute(id, tenantId);
    return ResponseEntity.ok(JobResponse.from(job));
  }
}
