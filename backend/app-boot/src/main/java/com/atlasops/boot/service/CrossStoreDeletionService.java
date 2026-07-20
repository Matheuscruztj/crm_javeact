package com.atlasops.boot.service;

import com.atlasops.documents.domain.ports.DocumentRepository;
import com.atlasops.shared.domain.events.CustomerDeletionRequestedEvent;
import com.atlasops.shared.domain.events.DocumentDeletionRequestedEvent;
import com.atlasops.shared.domain.ports.DistributedLockPort;
import com.atlasops.shared.domain.ports.DistributedLockPort.LockHandle;
import com.atlasops.shared.domain.ports.EventPublisher;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Orchestrator for cross-store deletion of documents and customers.
 * Acquires a distributed lock, validates preconditions (e.g., legal hold),
 * and publishes deletion events for downstream cleanup by each store adapter.
 *
 * <p>Validates: P2.8 — Cross-store deletion orchestrator
 */
@Service
public class CrossStoreDeletionService {

    private static final Logger log = LoggerFactory.getLogger(CrossStoreDeletionService.class);
    private static final Duration LOCK_TTL = Duration.ofSeconds(30);
    private static final String SYSTEM_CORRELATION_ID = "cross-store-deletion";

    private final EventPublisher eventPublisher;
    private final DistributedLockPort lockPort;
    private final DocumentRepository documentRepository;

    public CrossStoreDeletionService(
            EventPublisher eventPublisher,
            DistributedLockPort lockPort,
            DocumentRepository documentRepository) {
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "EventPublisher must not be null");
        this.lockPort = Objects.requireNonNull(lockPort, "DistributedLockPort must not be null");
        this.documentRepository = Objects.requireNonNull(documentRepository, "DocumentRepository must not be null");
    }

    /**
     * Orchestrates deletion of a document across all stores.
     * <ol>
     *   <li>Acquires a distributed lock on the document
     *   <li>Checks for legal hold — refuses deletion if active
     *   <li>Publishes {@link DocumentDeletionRequestedEvent}
     *   <li>Releases the lock
     * </ol>
     *
     * @param documentId the document to delete
     * @param tenantId   the tenant context
     * @throws IllegalStateException    if a legal hold is active on the document
     * @throws IllegalArgumentException if the lock cannot be acquired
     */
    public void deleteDocument(String documentId, String tenantId) {
        Objects.requireNonNull(documentId, "documentId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");

        String lockKey = "deletion:document:" + documentId;
        log.info("Initiating cross-store deletion for document '{}' (tenant: '{}')", documentId, tenantId);

        // Step 1: Acquire distributed lock
        Optional<LockHandle> lock = lockPort.tryAcquire(lockKey, LOCK_TTL);
        if (lock.isEmpty()) {
            throw new IllegalArgumentException(
                    "Could not acquire deletion lock for document '" + documentId + "'. Deletion already in progress.");
        }
        log.debug("Acquired lock for document deletion: {}", documentId);

        try {
            // Step 2: Check legal hold
            documentRepository.findById(documentId, tenantId).ifPresent(doc -> {
                if (doc.isLegalHold()) {
                    throw new IllegalStateException(
                            "Legal hold active on document '" + documentId + "'. Deletion is not permitted.");
                }
            });

            // Step 3: Publish deletion event
            var event = new DocumentDeletionRequestedEvent(
                    documentId, tenantId, "system", SYSTEM_CORRELATION_ID);
            eventPublisher.publish(event);
            log.info("Published DocumentDeletionRequestedEvent for document '{}' (tenant: '{}')",
                    documentId, tenantId);

        } finally {
            // Step 4: Release lock
            lockPort.release(lock.get());
            log.debug("Released lock for document deletion: {}", documentId);
        }
    }

    /**
     * Orchestrates deletion of a customer across all stores.
     * <ol>
     *   <li>Acquires a distributed lock on the customer
     *   <li>Publishes {@link CustomerDeletionRequestedEvent}
     *   <li>Releases the lock
     * </ol>
     *
     * @param customerId the customer to delete
     * @param tenantId   the tenant context
     * @throws IllegalArgumentException if the lock cannot be acquired
     */
    public void deleteCustomer(String customerId, String tenantId) {
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");

        String lockKey = "deletion:customer:" + customerId;
        log.info("Initiating cross-store deletion for customer '{}' (tenant: '{}')", customerId, tenantId);

        // Step 1: Acquire distributed lock
        Optional<LockHandle> lock = lockPort.tryAcquire(lockKey, LOCK_TTL);
        if (lock.isEmpty()) {
            throw new IllegalArgumentException(
                    "Could not acquire deletion lock for customer '" + customerId + "'. Deletion already in progress.");
        }
        log.debug("Acquired lock for customer deletion: {}", customerId);

        try {
            // Step 2: Publish deletion event
            var event = new CustomerDeletionRequestedEvent(
                    customerId, tenantId, "system", SYSTEM_CORRELATION_ID);
            eventPublisher.publish(event);
            log.info("Published CustomerDeletionRequestedEvent for customer '{}' (tenant: '{}')",
                    customerId, tenantId);

        } finally {
            // Step 3: Release lock
            lockPort.release(lock.get());
            log.debug("Released lock for customer deletion: {}", customerId);
        }
    }
}
