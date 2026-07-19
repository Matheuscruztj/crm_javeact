package com.atlasops.shared.domain.events;

import com.atlasops.shared.domain.DomainEvent;
import java.time.Instant;
import java.util.Objects;

/**
 * Published when an analyst approves or rejects a document. Triggers notification creation for the
 * CLIENT user.
 */
public final class ApprovalDecisionEvent extends DomainEvent {

  private final String documentId;
  private final String decision;
  private final String analystId;
  private final Instant decisionTimestamp;

  public ApprovalDecisionEvent(
      String documentId,
      String decision,
      String analystId,
      Instant decisionTimestamp,
      String tenantId,
      String correlationId) {
    super(tenantId, correlationId);
    this.documentId = Objects.requireNonNull(documentId, "documentId must not be null");
    this.decision = Objects.requireNonNull(decision, "decision must not be null");
    this.analystId = Objects.requireNonNull(analystId, "analystId must not be null");
    this.decisionTimestamp =
        Objects.requireNonNull(decisionTimestamp, "decisionTimestamp must not be null");
  }

  public String getDocumentId() {
    return documentId;
  }

  public String getDecision() {
    return decision;
  }

  public String getAnalystId() {
    return analystId;
  }

  public Instant getDecisionTimestamp() {
    return decisionTimestamp;
  }
}
