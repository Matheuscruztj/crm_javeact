package com.atlasops.shared.domain.events;

import com.atlasops.shared.domain.DomainEvent;
import java.util.Objects;

/**
 * Published when text extraction completes and the document is ready for AI analysis. Consumed by
 * the AI Analysis consumer in the Worker Process.
 */
public final class DocumentReadyForAnalysisEvent extends DomainEvent {

  private final String documentId;

  public DocumentReadyForAnalysisEvent(String documentId, String tenantId, String correlationId) {
    super(tenantId, correlationId);
    this.documentId = Objects.requireNonNull(documentId, "documentId must not be null");
  }

  public String getDocumentId() {
    return documentId;
  }
}
