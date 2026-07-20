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
class CancelApprovalUseCaseTest {

  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final Instant CANCEL_TIME = Instant.parse("2025-01-15T11:00:00Z");
  private static final String TENANT = "tenant-alpha";
  private static final String APPROVAL_ID = "approval-001";

  @Mock private ApprovalRepository approvalRepository;
  @Mock private EventPublisher eventPublisher;
  @Mock private Clock clock;

  private CancelApprovalUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new CancelApprovalUseCase(approvalRepository, eventPublisher, clock);
  }

  @Test
  void should_cancelApproval_when_adminCancels() {
    Approval pending = Approval.createPending(APPROVAL_ID, TENANT, "doc-001", NOW);
    when(approvalRepository.findById(APPROVAL_ID, TENANT)).thenReturn(Optional.of(pending));
    when(approvalRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(clock.now()).thenReturn(CANCEL_TIME);

    var command = new CancelApprovalCommand(APPROVAL_ID, "admin-001", "ADMIN", TENANT, "corr-001");
    Approval result = useCase.execute(command);

    assertThat(result.getStatus()).isEqualTo(ApprovalStatus.CANCELLED);
    assertThat(result.getDecisionBy()).isEqualTo("admin-001");
    verify(eventPublisher).publish(any());
  }

  @Test
  void should_throwForbidden_when_roleIsNotAdmin() {
    var command = new CancelApprovalCommand(APPROVAL_ID, "analyst-001", "ANALYST", TENANT, "corr-001");

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(ForbiddenActionException.class)
        .hasMessageContaining("ADMIN");
    verify(approvalRepository, never()).findById(any(), any());
  }

  @Test
  void should_throwNotFound_when_approvalMissing() {
    when(approvalRepository.findById(APPROVAL_ID, TENANT)).thenReturn(Optional.empty());

    var command = new CancelApprovalCommand(APPROVAL_ID, "admin-001", "ADMIN", TENANT, "corr-001");

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void should_throwForbidden_when_roleIsClient() {
    var command = new CancelApprovalCommand(APPROVAL_ID, "client-001", "CLIENT", TENANT, "corr-001");

    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(ForbiddenActionException.class);
    verify(approvalRepository, never()).findById(any(), any());
  }
}
