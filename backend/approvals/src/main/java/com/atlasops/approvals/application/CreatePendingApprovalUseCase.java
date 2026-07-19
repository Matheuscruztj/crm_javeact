package com.atlasops.approvals.application;

import com.atlasops.approvals.domain.Approval;
import com.atlasops.approvals.domain.ports.ApprovalRepository;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;

/**
 * Use case for creating a pending approval when a document reaches ANALYZED status. Triggered by
 * the approval creation consumer upon receiving DocumentAnalyzedEvent.
 */
public class CreatePendingApprovalUseCase {

  private final ApprovalRepository approvalRepository;
  private final IdGenerator idGenerator;
  private final Clock clock;

  public CreatePendingApprovalUseCase(
      ApprovalRepository approvalRepository, IdGenerator idGenerator, Clock clock) {
    this.approvalRepository = approvalRepository;
    this.idGenerator = idGenerator;
    this.clock = clock;
  }

  /**
   * Creates a pending approval for an analyzed document.
   *
   * @param command the create pending approval command
   * @return the created Approval in PENDING status
   */
  public Approval execute(CreatePendingApprovalCommand command) {
    String id = idGenerator.generate();

    Approval approval =
        Approval.createPending(id, command.tenantId(), command.documentId(), clock.now());

    return approvalRepository.save(approval);
  }
}
