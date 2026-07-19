package com.atlasops.documents.application;

import com.atlasops.documents.domain.Document;
import com.atlasops.documents.domain.DocumentStatus;
import com.atlasops.documents.domain.ports.DocumentRepository;
import com.atlasops.shared.domain.exceptions.BusinessRuleViolationException;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.EventPublisher;
import java.util.Objects;
import java.util.Set;

/**
 * Use case for re-triggering AI processing on an already-uploaded document.
 *
 * <p>Only documents in {@code ANALYZED} or {@code PROCESSING_FAILED} status can be reprocessed.
 * The document is reset to {@code UPLOADED} and the worker is notified via a domain event.
 *
 * <p>Validates: Requirements P0.N.5
 */
public class ReprocessDocumentUseCase {

  private static final Set<DocumentStatus> ELIGIBLE_STATUSES =
      Set.of(DocumentStatus.ANALYZED, DocumentStatus.PROCESSING_FAILED);

  private final DocumentRepository documentRepository;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public ReprocessDocumentUseCase(
      DocumentRepository documentRepository, EventPublisher eventPublisher, Clock clock) {
    this.documentRepository = Objects.requireNonNull(documentRepository);
    this.eventPublisher = Objects.requireNonNull(eventPublisher);
    this.clock = Objects.requireNonNull(clock);
  }

  /**
   * Reprocesses a document by re-triggering the AI analysis pipeline.
   *
   * @param documentId the document identifier
   * @param tenantId the tenant identifier
   * @param correlationId optional correlation ID for tracing
   * @return the updated document with UPLOADED status
   * @throws ResourceNotFoundException if the document is not found
   * @throws BusinessRuleViolationException if the document is not eligible for reprocessing
   */
  public Document execute(String documentId, String tenantId, String correlationId) {
    Objects.requireNonNull(documentId, "DocumentId must not be null");
    Objects.requireNonNull(tenantId, "TenantId must not be null");

    Document document =
        documentRepository
            .findByIdAndTenantId(documentId, tenantId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Document with id '" + documentId + "' not found"));

    if (!ELIGIBLE_STATUSES.contains(document.getStatus())) {
      throw new BusinessRuleViolationException(
          "Document cannot be reprocessed in status '"
              + document.getStatus()
              + "'. Eligible statuses: ANALYZED, PROCESSING_FAILED");
    }

    document.reprocess(correlationId, clock.now());

    Document saved = documentRepository.save(document);

    // Publish domain events accumulated during reprocess()
    document.getDomainEvents().forEach(eventPublisher::publish);
    document.clearDomainEvents();

    return saved;
  }
}
