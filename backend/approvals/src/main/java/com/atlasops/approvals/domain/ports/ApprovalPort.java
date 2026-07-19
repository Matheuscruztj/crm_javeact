package com.atlasops.approvals.domain.ports;

import com.atlasops.approvals.domain.ApprovalRequest;
import com.atlasops.approvals.domain.ApprovalResult;

/**
 * Port defining the contract for approval workflow operations. Implementations handle the
 * persistence and event sourcing of approval decisions.
 */
public interface ApprovalPort {

  /**
   * Submits an approval decision for processing.
   *
   * @param request the approval request containing decision details
   * @return the result of the approval submission
   */
  ApprovalResult submitApproval(ApprovalRequest request);
}
