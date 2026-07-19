package com.atlasops.approvals.presentation;

import com.atlasops.approvals.application.ApproveDocumentCommand;
import com.atlasops.approvals.application.ApproveDocumentUseCase;
import com.atlasops.approvals.application.CancelApprovalCommand;
import com.atlasops.approvals.application.CancelApprovalUseCase;
import com.atlasops.approvals.application.RejectDocumentCommand;
import com.atlasops.approvals.application.RejectDocumentUseCase;
import com.atlasops.approvals.domain.Approval;
import com.atlasops.approvals.domain.ApprovalStatus;
import com.atlasops.approvals.domain.ports.ApprovalRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for approval management operations.
 *
 * <p>Endpoints:
 *
 * <ul>
 *   <li>POST /api/v1/approvals/{id}/approve — approve a document
 *   <li>POST /api/v1/approvals/{id}/reject — reject a document with reason
 *   <li>POST /api/v1/approvals/{id}/cancel — cancel an approval (ADMIN only)
 *   <li>GET /api/v1/approvals — list approvals with optional status filter
 * </ul>
 *
 * <p>Validates: Requirements 13.2, 13.3, 13.8
 */
@RestController
@RequestMapping("/api/v1/approvals")
public class ApprovalController {

  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int MAX_PAGE_SIZE = 100;

  private final ApproveDocumentUseCase approveDocumentUseCase;
  private final RejectDocumentUseCase rejectDocumentUseCase;
  private final CancelApprovalUseCase cancelApprovalUseCase;
  private final ApprovalRepository approvalRepository;

  public ApprovalController(
      ApproveDocumentUseCase approveDocumentUseCase,
      RejectDocumentUseCase rejectDocumentUseCase,
      CancelApprovalUseCase cancelApprovalUseCase,
      ApprovalRepository approvalRepository) {
    this.approveDocumentUseCase = approveDocumentUseCase;
    this.rejectDocumentUseCase = rejectDocumentUseCase;
    this.cancelApprovalUseCase = cancelApprovalUseCase;
    this.approvalRepository = approvalRepository;
  }

  /**
   * Lists approvals with optional status filter and pagination.
   *
   * @param tenantId the tenant identifier from header
   * @param status optional status filter (PENDING, APPROVED, REJECTED, CANCELLED)
   * @param page page number (zero-based, defaults to 0)
   * @param size page size (defaults to 20, max 100)
   * @return 200 OK with paginated response
   */
  @GetMapping
  public ResponseEntity<PageResponse<ApprovalResponse>> list(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @RequestParam(required = false) String status,
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "20") Integer size) {

    int effectivePage = Math.max(0, page);
    int effectiveSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);

    PageRequest pageable = PageRequest.of(effectivePage, effectiveSize);

    Page<Approval> result;
    if (status != null && !status.isBlank()) {
      ApprovalStatus statusFilter = ApprovalStatus.valueOf(status);
      result = approvalRepository.findByTenantIdAndStatus(tenantId, statusFilter, pageable);
    } else {
      result = approvalRepository.findPendingByTenantId(tenantId, pageable);
    }

    List<ApprovalResponse> content =
        result.getContent().stream().map(ApprovalResponse::from).toList();

    var pageMetadata =
        new PageResponse.PageMetadata(
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages());

    return ResponseEntity.ok(new PageResponse<>(content, pageMetadata));
  }

  /**
   * Approves a document.
   *
   * @param tenantId the tenant identifier from header
   * @param userId the authenticated user identifier
   * @param role the user's role (ANALYST or ADMIN required)
   * @param correlationId optional correlation ID for tracing
   * @param id the approval identifier
   * @return 200 OK with the updated approval representation
   */
  @PostMapping("/{id}/approve")
  public ResponseEntity<ApprovalResponse> approve(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @RequestHeader("X-User-ID") String userId,
      @RequestHeader("X-User-Role") String role,
      @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId,
      @PathVariable String id) {

    var command = new ApproveDocumentCommand(id, userId, role, tenantId, correlationId);
    Approval approval = approveDocumentUseCase.execute(command);
    return ResponseEntity.ok(ApprovalResponse.from(approval));
  }

  /**
   * Rejects a document with a required reason.
   *
   * @param tenantId the tenant identifier from header
   * @param userId the authenticated user identifier
   * @param role the user's role (ANALYST or ADMIN required)
   * @param correlationId optional correlation ID for tracing
   * @param id the approval identifier
   * @param request the rejection request containing the reason
   * @return 200 OK with the updated approval representation
   */
  @PostMapping("/{id}/reject")
  public ResponseEntity<ApprovalResponse> reject(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @RequestHeader("X-User-ID") String userId,
      @RequestHeader("X-User-Role") String role,
      @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId,
      @PathVariable String id,
      @Valid @RequestBody RejectApprovalRequest request) {

    var command =
        new RejectDocumentCommand(id, userId, request.reason(), role, tenantId, correlationId);
    Approval approval = rejectDocumentUseCase.execute(command);
    return ResponseEntity.ok(ApprovalResponse.from(approval));
  }

  /**
   * Cancels an approval (ADMIN only).
   *
   * @param tenantId the tenant identifier from header
   * @param userId the authenticated admin user identifier
   * @param role the user's role (ADMIN required)
   * @param correlationId optional correlation ID for tracing
   * @param id the approval identifier
   * @return 200 OK with the updated approval representation
   */
  @PostMapping("/{id}/cancel")
  public ResponseEntity<ApprovalResponse> cancel(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @RequestHeader("X-User-ID") String userId,
      @RequestHeader("X-User-Role") String role,
      @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId,
      @PathVariable String id) {

    var command = new CancelApprovalCommand(id, userId, role, tenantId, correlationId);
    Approval approval = cancelApprovalUseCase.execute(command);
    return ResponseEntity.ok(ApprovalResponse.from(approval));
  }
}
