package com.atlasops.documents.application;

import com.atlasops.documents.domain.Document;
import com.atlasops.documents.domain.ports.DocumentRepository;
import com.atlasops.shared.domain.exceptions.ResourceNotFoundException;
import com.atlasops.shared.domain.ports.Clock;
import java.util.Objects;

/**
 * Use case for releasing a legal hold on a document.
 * After release, the document can be archived or deleted normally.
 *
 * <p>Validates: P2.9 — Legal hold preventing archive/delete
 */
public class ReleaseLegalHoldUseCase {

    private final DocumentRepository documentRepository;
    private final Clock clock;

    public ReleaseLegalHoldUseCase(DocumentRepository documentRepository, Clock clock) {
        this.documentRepository = Objects.requireNonNull(documentRepository, "DocumentRepository must not be null");
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    /**
     * Releases the legal hold on the specified document.
     *
     * @param documentId the document identifier
     * @param tenantId   the tenant identifier
     * @param actorId    the user releasing the hold (for audit purposes)
     * @return the updated document with legal hold removed
     * @throws ResourceNotFoundException if the document does not exist
     */
    public Document execute(String documentId, String tenantId, String actorId) {
        Objects.requireNonNull(documentId, "documentId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(actorId, "actorId must not be null");

        Document document = documentRepository
                .findById(documentId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Document with id '" + documentId + "' not found for tenant '" + tenantId + "'"));

        document.releaseLegalHold(clock.now());
        return documentRepository.save(document);
    }
}
