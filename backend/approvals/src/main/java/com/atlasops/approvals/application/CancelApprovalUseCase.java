package com.atlasops.approvals.application;

import com.atlasops.approvals.domain.Approval;
import com.atlasops.approvals.domain.ports.ApprovalRepository;
import com.atlasops.shared.domain.DomainEvent;
import com.atlasops.shared.domain.exceptions.ForbiddenActionException;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.EventPublisher;

/**
 * Use case for cancelling an approval. Transitions from PENDING to CANCELLED. Only ADMIN users can
 * cancel approvals.
 */
public class CancelApprovalUseCase {

  private static final String ADMIN_ROLE = "ADMIN";

  private final ApprovalRepository approvalRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public CancelApprovalUseCase(
      ApprovalRepository approvalRepository, EventPublisher eventPublisher, Clock clock) {
    this.approvalRepository = approvalRepository;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  /**
   * Cancels an approval by transitioning from PENDING to CANCELLED. ADMIN only operation.
   *
   * @param command the cancel approval command
   * @return the updated Approval with CANCELLED status
   * @throws ForbiddenActionException if the user does not have ADMIN role
   * @throws ResourceNotFoundException if the approval is not found
   */
  public Approval execute(CancelApprovalCommand command) {
    enforceAdminRole(command.role());

    Approval approval =
        approvalRepository
            .findById(command.approvalId(), command.tenantId())
            .orElseThrow(
                () -> new ResourceNotFoundException("Approval not found: " + command.approvalId()));

    approval.cancel(command.adminId(), command.correlationId(), clock.now());

    Approval saved = approvalRepository.save(approval);

    for (DomainEvent event : saved.getDomainEvents()) {
      eventPublisher.publish(event);
    }
    saved.clearDomainEvents();

    return saved;
  }

  private void enforceAdminRole(String role) {
    if (!ADMIN_ROLE.equals(role)) {
      throw new ForbiddenActionException("Only ADMIN can cancel approvals. Current role: " + role);
    }
  }
}
