package com.atlasops.worker.consumers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.approvals.application.CreatePendingApprovalCommand;
import com.atlasops.approvals.application.CreatePendingApprovalUseCase;
import com.atlasops.approvals.domain.Approval;
import com.atlasops.worker.infrastructure.redis.StreamMessage;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for ApprovalCreationConsumer. Validates: Requirements 13.1 */
@ExtendWith(MockitoExtension.class)
class ApprovalCreationConsumerTest {

  @Mock private CreatePendingApprovalUseCase createPendingApprovalUseCase;

  private ApprovalCreationConsumer consumer;

  @BeforeEach
  void setUp() {
    consumer = new ApprovalCreationConsumer(createPendingApprovalUseCase);
  }

  @Test
  void should_returnCorrectStreamKey() {
    assertThat(consumer.getStreamKey()).isEqualTo("documents.analyzed");
  }

  @Test
  void should_createPendingApproval_when_documentIsAnalyzed() throws Exception {
    // Arrange
    Map<String, String> payload = new HashMap<>();
    payload.put("documentId", "doc-123");
    payload.put("tenantId", "tenant-alpha");
    payload.put("status", "ANALYZED");

    StreamMessage message = new StreamMessage("documents.analyzed", "msg-1", payload);

    Approval mockApproval =
        Approval.createPending("approval-001", "tenant-alpha", "doc-123", Instant.now());

    when(createPendingApprovalUseCase.execute(any(CreatePendingApprovalCommand.class)))
        .thenReturn(mockApproval);

    // Act
    consumer.handle(message);

    // Assert
    ArgumentCaptor<CreatePendingApprovalCommand> captor =
        ArgumentCaptor.forClass(CreatePendingApprovalCommand.class);
    verify(createPendingApprovalUseCase).execute(captor.capture());

    CreatePendingApprovalCommand capturedCommand = captor.getValue();
    assertThat(capturedCommand.documentId()).isEqualTo("doc-123");
    assertThat(capturedCommand.tenantId()).isEqualTo("tenant-alpha");
  }

  @Test
  void should_skipDocument_when_statusIsNotAnalyzed() throws Exception {
    // Arrange
    Map<String, String> payload = new HashMap<>();
    payload.put("documentId", "doc-123");
    payload.put("tenantId", "tenant-alpha");
    payload.put("status", "UPLOADED");

    StreamMessage message = new StreamMessage("documents.analyzed", "msg-1", payload);

    // Act
    consumer.handle(message);

    // Assert
    verify(createPendingApprovalUseCase, never()).execute(any());
  }

  @Test
  void should_processDocument_when_statusIsNull() throws Exception {
    // Arrange - status being null means we should process (no prior status info)
    Map<String, String> payload = new HashMap<>();
    payload.put("documentId", "doc-123");
    payload.put("tenantId", "tenant-alpha");
    // No status field

    StreamMessage message = new StreamMessage("documents.analyzed", "msg-1", payload);

    Approval mockApproval =
        Approval.createPending("approval-001", "tenant-alpha", "doc-123", Instant.now());

    when(createPendingApprovalUseCase.execute(any(CreatePendingApprovalCommand.class)))
        .thenReturn(mockApproval);

    // Act
    consumer.handle(message);

    // Assert - should process since status is null (not explicitly non-ANALYZED)
    verify(createPendingApprovalUseCase).execute(any(CreatePendingApprovalCommand.class));
  }
}
