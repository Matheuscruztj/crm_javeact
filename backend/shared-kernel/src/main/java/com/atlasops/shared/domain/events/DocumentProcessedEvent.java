package com.atlasops.shared.domain.events;

import com.atlasops.shared.domain.DomainEvent;
import java.util.Objects;

/**
 * Published when a document finishes text extraction (before AI analysis).
 * Triggers the AI analysis pipeline in the Worker Process.
 * Validates: P0.P.2.2 — document.processed.v1 domain event
 */
public final class DocumentProcessedEvent extends DomainEvent {

    private final String documentId;
    private final String extractedTextPath;
    private final int pageCount;

    public DocumentProcessedEvent(
            String documentId,
            String tenantId,
            String extractedTextPath,
            int pageCount,
            String correlationId) {
        super(tenantId, correlationId);
        this.documentId = Objects.requireNonNull(documentId, "documentId must not be null");
        this.extractedTextPath = Objects.requireNonNull(extractedTextPath, "extractedTextPath must not be null");
        this.pageCount = pageCount;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getExtractedTextPath() {
        return extractedTextPath;
    }

    public int getPageCount() {
        return pageCount;
    }
}
