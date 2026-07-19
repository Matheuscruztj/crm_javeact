package com.atlasops.approvals.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.approvals.domain.Approval;
import com.atlasops.approvals.domain.ApprovalStatus;
import com.atlasops.approvals.domain.ports.ApprovalRepository;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreatePendingApprovalUseCase")
class CreatePendingApprovalUseCaseTest {

  private static final Instant FIXED_NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final String GENERATED_ID = "approval-001";

  @Mock private ApprovalRepository approvalRepository;
  @Mock private IdGenerator idGenerator;
  @Mock private Clock clock;

  private CreatePendingApprovalUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new CreatePendingApprovalUseCase(approvalRepository, idGenerator, clock);
  }

  @Test
  void should_createPendingApproval_when_documentAnalyzed() {
    // Arrange
    var command = new CreatePendingApprovalCommand("doc-123", "tenant-alpha");

    when(idGenerator.generate()).thenReturn(GENERATED_ID);
    when(clock.now()).thenReturn(FIXED_NOW);
    when(approvalRepository.save(any(Approval.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    Approval result = useCase.execute(command);

    // Assert
    assertThat(result.getId()).isEqualTo(GENERATED_ID);
    assertThat(result.getDocumentId()).isEqualTo("doc-123");
    assertThat(result.getTenantId()).isEqualTo("tenant-alpha");
    assertThat(result.getStatus()).isEqualTo(ApprovalStatus.PENDING);
    assertThat(result.getDecisionBy()).isNull();
    assertThat(result.getRejectionReason()).isNull();
    assertThat(result.getCreatedAt()).isEqualTo(FIXED_NOW);
    assertThat(result.getDecidedAt()).isNull();

    verify(approvalRepository).save(any(Approval.class));
  }

  @Test
  void should_useIdGenerator_when_creatingApproval() {
    // Arrange
    var command = new CreatePendingApprovalCommand("doc-456", "tenant-beta");

    when(idGenerator.generate()).thenReturn("custom-id-999");
    when(clock.now()).thenReturn(FIXED_NOW);
    when(approvalRepository.save(any(Approval.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    Approval result = useCase.execute(command);

    // Assert
    assertThat(result.getId()).isEqualTo("custom-id-999");
    verify(idGenerator).generate();
  }

  @Test
  void should_useClockTimestamp_when_creatingApproval() {
    // Arrange
    Instant customTime = Instant.parse("2025-06-20T15:30:00Z");
    var command = new CreatePendingApprovalCommand("doc-789", "tenant-gamma");

    when(idGenerator.generate()).thenReturn(GENERATED_ID);
    when(clock.now()).thenReturn(customTime);
    when(approvalRepository.save(any(Approval.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    Approval result = useCase.execute(command);

    // Assert
    assertThat(result.getCreatedAt()).isEqualTo(customTime);
    verify(clock).now();
  }
}
