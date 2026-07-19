package com.atlasops.audit.domain.ports;

import com.atlasops.audit.domain.AuditEntry;
import com.atlasops.audit.domain.AuditQueryFilters;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Port defining persistence operations for AuditEntry entities. This is an append-only ledger: no
 * UPDATE or DELETE operations are exposed. All query operations require tenant context for data
 * isolation.
 */
public interface AuditRepository {

  /**
   * Appends a new audit entry to the ledger. This is the only write operation — the ledger is
   * append-only by design.
   *
   * @param entry the audit entry to persist
   * @return the persisted audit entry
   */
  AuditEntry append(AuditEntry entry);

  /**
   * Queries audit entries with the given filters, returning paginated results ordered by timestamp
   * descending.
   *
   * @param filters the query filters (tenantId is required)
   * @param pageable pagination parameters (default size 50, max 200)
   * @return a page of audit entries matching the filters
   */
  Page<AuditEntry> query(AuditQueryFilters filters, Pageable pageable);
}
