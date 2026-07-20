package com.atlasops.shared.domain.events;

import com.atlasops.shared.domain.DomainEvent;
import java.util.Objects;

/**
 * Published when a document deletion has been requested via the cross-store deletion orchestrator.
 * Consumers (search index, vector store, object storage) must handle cleanup.
 *
 * <p>Validates: P2.8 — Cross-store deletion orchestrator
 */
public final class DocumentDeletionRequestedEvent extends DomainEvent {

    private final String documentId;
    private final String requestedBy;

    public DocumentDeletionRequestedEvent(
            String documentId, String tenantId, String requestedBy, String correlationId) {
        super(tenantId, correlationId);
        this.documentId = Objects.requireNonNull(documentId, "documentId must not be null");
        this.requestedBy = Objects.requireNonNull(requestedBy, "requestedBy must not be null");
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getRequestedBy() {
        return requestedBy;
    }
}
