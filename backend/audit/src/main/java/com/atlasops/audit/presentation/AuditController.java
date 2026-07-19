package com.atlasops.audit.presentation;

import com.atlasops.audit.application.QueryAuditEntriesUseCase;
import com.atlasops.audit.domain.AuditEntry;
import com.atlasops.audit.domain.AuditQueryFilters;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for audit log query operations.
 *
 * <p>Endpoint:
 *
 * <ul>
 *   <li>GET /api/v1/audit?actorId=...&actionType=...&from=...&to=... — query audit entries
 * </ul>
 *
 * <p>Validates: Requirements 19.1, 19.4, 19.5
 */
@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

  private static final int DEFAULT_PAGE_SIZE = 50;
  private static final int MAX_PAGE_SIZE = 200;

  private final QueryAuditEntriesUseCase queryAuditEntriesUseCase;

  public AuditController(QueryAuditEntriesUseCase queryAuditEntriesUseCase) {
    this.queryAuditEntriesUseCase = queryAuditEntriesUseCase;
  }

  /**
   * Queries audit entries with optional filters and pagination.
   *
   * @param tenantId the tenant identifier from header
   * @param actorId optional filter by actor
   * @param actionType optional filter by action type
   * @param entityType optional filter by entity type
   * @param entityId optional filter by entity identifier
   * @param from optional lower bound for timestamp (inclusive, ISO-8601)
   * @param to optional upper bound for timestamp (inclusive, ISO-8601)
   * @param page page number (zero-based, defaults to 0)
   * @param size page size (defaults to 50, max 200)
   * @return 200 OK with paginated response ordered by timestamp descending
   */
  @GetMapping
  public ResponseEntity<PageResponse<AuditEntryResponse>> query(
      @RequestHeader("X-Tenant-ID") String tenantId,
      @RequestParam(required = false) String actorId,
      @RequestParam(required = false) String actionType,
      @RequestParam(required = false) String entityType,
      @RequestParam(required = false) String entityId,
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to,
      @RequestParam(defaultValue = "0") Integer page,
      @RequestParam(defaultValue = "50") Integer size) {

    int effectivePage = Math.max(0, page);
    int effectiveSize = Math.min(Math.max(1, size), MAX_PAGE_SIZE);

    PageRequest pageable =
        PageRequest.of(effectivePage, effectiveSize, Sort.by(Sort.Direction.DESC, "timestamp"));

    var filters =
        new AuditQueryFilters(tenantId, actorId, entityType, entityId, actionType, from, to);

    Page<AuditEntry> result = queryAuditEntriesUseCase.execute(filters, pageable);

    List<AuditEntryResponse> content =
        result.getContent().stream().map(AuditEntryResponse::from).toList();

    var pageMetadata =
        new PageResponse.PageMetadata(
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages());

    return ResponseEntity.ok(new PageResponse<>(content, pageMetadata));
  }
}
