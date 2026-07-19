package com.atlasops.audit.application;

import com.atlasops.audit.domain.AuditEntry;
import com.atlasops.audit.domain.AuditQueryFilters;
import com.atlasops.audit.domain.ports.AuditRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Use case for querying audit entries with filters and pagination. Supports filtering by time
 * range, actor, entity, and action type. Pagination defaults to 50 entries per page with a maximum
 * of 200.
 */
public class QueryAuditEntriesUseCase {

  private static final int DEFAULT_PAGE_SIZE = 50;
  private static final int MAX_PAGE_SIZE = 200;

  private final AuditRepository auditRepository;

  public QueryAuditEntriesUseCase(AuditRepository auditRepository) {
    this.auditRepository = auditRepository;
  }

  /**
   * Queries audit entries matching the given filters with bounded pagination.
   *
   * @param filters the query filters (tenantId is required)
   * @param pageable the pagination parameters (size clamped to max 200)
   * @return a page of audit entries matching the filters
   */
  public Page<AuditEntry> execute(AuditQueryFilters filters, Pageable pageable) {
    Pageable bounded = boundPageable(pageable);
    return auditRepository.query(filters, bounded);
  }

  private Pageable boundPageable(Pageable pageable) {
    int pageSize = pageable.getPageSize();
    if (pageSize > MAX_PAGE_SIZE) {
      pageSize = MAX_PAGE_SIZE;
    }
    if (pageSize <= 0) {
      pageSize = DEFAULT_PAGE_SIZE;
    }
    int pageNumber = pageable.getPageNumber();
    if (pageNumber < 0) {
      pageNumber = 0;
    }
    return PageRequest.of(pageNumber, pageSize, pageable.getSort());
  }
}
