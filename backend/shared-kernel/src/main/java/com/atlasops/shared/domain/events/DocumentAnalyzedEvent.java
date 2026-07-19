package com.atlasops.shared.domain.events;

import com.atlasops.shared.domain.DomainEvent;
import java.util.Objects;

/**
 * Published when AI analysis (or fallback) completes for a document. Triggers pending approval
 * creation.
 */
public final class DocumentAnalyzedEvent extends DomainEvent {

  private final String documentId;
  private final boolean fallbackUsed;

  public DocumentAnalyzedEvent(
      String documentId, String tenantId, boolean fallbackUsed, String correlationId) {
    super(tenantId, correlationId);
    this.documentId = Objects.requireNonNull(documentId, "documentId must not be null");
    this.fallbackUsed = fallbackUsed;
  }

  public String getDocumentId() {
    return documentId;
  }

  public boolean isFallbackUsed() {
    return fallbackUsed;
  }
}
