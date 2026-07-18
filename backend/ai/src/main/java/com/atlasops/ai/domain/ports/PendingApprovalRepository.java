package com.atlasops.ai.domain.ports;

import com.atlasops.ai.domain.ApprovalStatus;
import com.atlasops.ai.domain.PendingApproval;
import java.util.List;
import java.util.Optional;

/**
 * Port interface for persisting and retrieving PendingApproval records. Enforces the
 * human-in-the-loop invariant: no mutable action is executed without an approved PendingApproval.
 *
 * <p>Implementations may use JPA, in-memory storage, or any other persistence mechanism.
 *
 * <p>Validates: Requirements 4.10
 */
public interface PendingApprovalRepository {

  /**
   * Persists a PendingApproval record.
   *
   * @param approval the approval to save
   * @return the saved approval
   */
  PendingApproval save(PendingApproval approval);

  /**
   * Finds a PendingApproval by its unique identifier.
   *
   * @param id the approval identifier
   * @return the approval if found, empty otherwise
   */
  Optional<PendingApproval> findById(String id);

  /**
   * Finds all PendingApprovals associated with a given analysis.
   *
   * @param analysisId the analysis identifier
   * @return list of approvals for the analysis
   */
  List<PendingApproval> findByAnalysisId(String analysisId);

  /**
   * Finds all PendingApprovals with a given status.
   *
   * @param status the approval status to filter by
   * @return list of approvals matching the status
   */
  List<PendingApproval> findByStatus(ApprovalStatus status);
}
