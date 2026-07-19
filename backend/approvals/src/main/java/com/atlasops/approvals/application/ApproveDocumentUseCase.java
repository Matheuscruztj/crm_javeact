package com.atlasops.approvals.application;

import com.atlasops.approvals.domain.Approval;
import com.atlasops.approvals.domain.ports.ApprovalRepository;
import com.atlasops.shared.domain.DomainEvent;
import com.atlasops.shared.domain.exceptions.ForbiddenActionException;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.EventPublisher;
import java.util.Set;

/**
 * Use case for approving a document. Transitions the approval from PENDING to APPROVED. Only users
 * with ANALYST or ADMIN role can approve documents.
 */
public class ApproveDocumentUseCase {

  private static final Set<String> ALLOWED_ROLES = Set.of("ANALYST", "ADMIN");

  private final ApprovalRepository approvalRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public ApproveDocumentUseCase(
      ApprovalRepository approvalRepository, EventPublisher eventPublisher, Clock clock) {
    this.approvalRepository = approvalRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  /**
   * Approves a document by transitioning the approval from PENDING to APPROVED.
   *
   * @param command the approve document command
   * @return the updated Approval with APPROVED status
   * @throws ForbiddenActionException if the user does not have ANALYST or ADMIN role
   * @throws ResourceNotFoundException if the approval is not found
   */
  public Approval execute(ApproveDocumentCommand command) {
    enforceRole(command.role());

    Approval approval =
        approvalRepository
            .findById(command.approvalId(), command.tenantId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Approval not found: " + command.approvalId()));

    approval.approve(command.analystId(), command.correlationId(), clock.now());

    Approval saved = approvalRepository.save(approval);

    for (DomainEvent event : saved.getDomainEvents()) {
      eventPublisher.publish(event);
    }
    saved.clearDomainEvents();

    return saved;
  }

  private void enforceRole(String role) {
    if (!ALLOWED_ROLES.contains(role)) {
      throw new ForbiddenActionException(
          "Only ANALYST or ADMIN can approve documents. Current role: " + role);
    }
  }
}
