package com.atlasops.approvals.domain.ports;

import com.atlasops.approvals.domain.Approval;
import com.atlasops.approvals.domain.ApprovalStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Port defining persistence operations for Approval aggregates. All query methods require tenant
 * context for data isolation.
 */
public interface ApprovalRepository {

  /**
   * Persists an approval (create or update).
   *
   * @param approval the approval to persist
   * @return the persisted approval
   */
  Approval save(Approval approval);

  /**
   * Finds an approval by its unique identifier within a tenant.
   *
   * @param id the approval identifier
   * @param tenantId the tenant identifier
   * @return the approval if found
   */
  Optional<Approval> findById(String id, String tenantId);

  /**
   * Finds an approval by document identifier within a tenant.
   *
   * @param documentId the document identifier
   * @param tenantId the tenant identifier
   * @return the approval if found
   */
  Optional<Approval> findByDocumentIdAndTenantId(String documentId, String tenantId);

  /**
   * Finds all pending approvals for a tenant with pagination.
   *
   * @param tenantId the tenant identifier
   * @param pageable pagination parameters
   * @return a page of pending approvals
   */
  Page<Approval> findPendingByTenantId(String tenantId, Pageable pageable);

  /**
   * Finds approvals by status for a tenant with pagination.
   *
   * @param tenantId the tenant identifier
   * @param status the approval status to filter by
   * @param pageable pagination parameters
   * @return a page of approvals
   */
  Page<Approval> findByTenantIdAndStatus(String tenantId, ApprovalStatus status, Pageable pageable);
}
