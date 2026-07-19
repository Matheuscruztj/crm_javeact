package com.atlasops.worker.consumers;

import com.atlasops.approvals.application.CreatePendingApprovalCommand;
import com.atlasops.approvals.application.CreatePendingApprovalUseCase;
import com.atlasops.approvals.domain.Approval;
import com.atlasops.worker.infrastructure.redis.MessageHandler;
import com.atlasops.worker.infrastructure.redis.StreamMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Consumer for creating pending approvals from the documents.analyzed stream. Invokes
 * CreatePendingApprovalUseCase to create a PENDING approval for analyzed documents.
 *
 * <p>Validates: Requirements 13.1
 */
@Component
public class ApprovalCreationConsumer implements MessageHandler {

  private static final Logger log = LoggerFactory.getLogger(ApprovalCreationConsumer.class);
  private static final String STREAM_KEY = "documents.analyzed";

  private final CreatePendingApprovalUseCase createPendingApprovalUseCase;

  public ApprovalCreationConsumer(CreatePendingApprovalUseCase createPendingApprovalUseCase) {
    this.createPendingApprovalUseCase = createPendingApprovalUseCase;
  }

  public String getStreamKey() {
    return STREAM_KEY;
  }

  @Override
  public void handle(StreamMessage message) throws Exception {
    String documentId = message.getRequired("documentId");
    String tenantId = message.getRequired("tenantId");
    String status = message.get("status");

    // Idempotency check - only process ANALYZED documents
    if (status != null && !status.equals("ANALYZED")) {
      log.info("Skipping document {} - not in ANALYZED status (status: {})", documentId, status);
      return;
    }

    log.info("Creating pending approval for document {} (tenant: {})", documentId, tenantId);

    CreatePendingApprovalCommand command = new CreatePendingApprovalCommand(documentId, tenantId);
    Approval approval = createPendingApprovalUseCase.execute(command);

    log.info(
        "Pending approval created with ID {} for document {} (tenant: {})",
        approval.getId(),
        documentId,
        tenantId);
  }
}
