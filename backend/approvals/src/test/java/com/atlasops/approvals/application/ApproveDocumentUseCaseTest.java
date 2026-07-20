package com.atlasops.approvals.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.approvals.domain.Approval;
import com.atlasops.approvals.domain.ApprovalStatus;
import com.atlasops.approvals.domain.ports.ApprovalRepository;
import com.atlasops.shared.domain.exceptions.ForbiddenActionException;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.EventPublisher;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApproveDocumentUseCaseTest {

  private static final Instant CREATED_AT = Instant.parse("2025-01-15T09:00:00Z");
  private static final Instant DECIDED_AT = Instant.parse("2025-01-15T10:00:00Z");
  private static final String APPROVAL_ID = "approval-001";
  private static final String TENANT_ID = "tenant-alpha";
  private static final String ANALYST_ID = "analyst-001";
  private static final String CORRELATION_ID = "corr-001";

  @Mock private ApprovalRepository approvalRepository;
  @Mock private EventPublisher eventPublisher;
  @Mock private Clock clock;

  private ApproveDocumentUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new ApproveDocumentUseCase(approvalRepository, eventPublisher, clock);
  }

  @Test
  void should_approveDocument_when_analystApprovesValidApproval() {
    Approval pending = Approval.createPending(APPROVAL_ID, TENANT_ID, "doc-001", CREATED_AT);
    when(approvalRepository.findById(APPROVAL_ID, TENANT_ID)).thenReturn(Optional.of(pending));
    when(approvalRepository.save(any(Approval.class))).thenAnswer(i -> i.getArgument(0));
    when(clock.now()).thenReturn(DECIDED_AT);

    var command = new ApproveDocumentCommand(APPROVAL_ID, ANALYST_ID, "ANALYST", TENANT_ID, CORRELATION_ID);
    Approval result = useCase.execute(command);

    assertThat(result.getStatus()).isEqualTo(ApprovalStatus.APPROVED);
    assertThat(result.getDecisionBy()).isEqualTo(ANALYST_ID);
    assertThat(result.getDecidedAt()).isEqualTo(DECIDED_AT);
  }

  @Test
  void should_publishDomainEvent_when_approvalSucceeds() {
    Approval pending = Approval.createPending(APPROVAL_ID, TENANT_ID, "doc-001", CREATED_AT);
    when(approvalRepository.findById(APPROVAL_ID, TENANT_ID)).thenReturn(Optional.of(pending));
    when(approvalRepository.save(any(Approval.class))).thenAnswer(i -> i.getArgument(0));
    when(clock.now()).thenReturn(DECIDED_AT);

    var command = new ApproveDocumentCommand(APPROVAL_ID, ANALYST_ID, "ANALYST", TENANT_ID, CORRELATION_ID);
    useCase.execute(command);

    verify(eventPublisher).publish(any());
  }

  @Test
  void should_throwForbiddenAction_when_clientTriesToApprove() {
    var command = new ApproveDocumentCommand(APPROVAL_ID, "client-001", "CLIENT", TENANT_ID, CORRELATION_ID);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(ForbiddenActionException.class)
        .hasMessageContaining("CLIENT");
    verify(approvalRepository, never()).findById(any(), any());
  }

  @Test
  void should_throwResourceNotFound_when_approvalDoesNotExist() {
    when(approvalRepository.findById(APPROVAL_ID, TENANT_ID)).thenReturn(Optional.empty());

    var command = new ApproveDocumentCommand(APPROVAL_ID, ANALYST_ID, "ANALYST", TENANT_ID, CORRELATION_ID);

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void should_allowAdmin_when_adminApprovesDocument() {
    Approval pending = Approval.createPending(APPROVAL_ID, TENANT_ID, "doc-001", CREATED_AT);
    when(approvalRepository.findById(APPROVAL_ID, TENANT_ID)).thenReturn(Optional.of(pending));
    when(approvalRepository.save(any(Approval.class))).thenAnswer(i -> i.getArgument(0));
    when(clock.now()).thenReturn(DECIDED_AT);

    var command = new ApproveDocumentCommand(APPROVAL_ID, "admin-001", "ADMIN", TENANT_ID, CORRELATION_ID);
    Approval result = useCase.execute(command);

    assertThat(result.getStatus()).isEqualTo(ApprovalStatus.APPROVED);
  }
}
