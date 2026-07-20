package com.atlasops.shared.domain.events;

import com.atlasops.shared.domain.DomainEvent;
import java.util.Objects;

/**
 * Published when a customer deletion has been requested via the cross-store deletion orchestrator.
 * Consumers (search index, activities, documents) must handle cleanup.
 *
 * <p>Validates: P2.8 — Cross-store deletion orchestrator
 */
public final class CustomerDeletionRequestedEvent extends DomainEvent {

    private final String customerId;
    private final String requestedBy;

    public CustomerDeletionRequestedEvent(
            String customerId, String tenantId, String requestedBy, String correlationId) {
        super(tenantId, correlationId);
        this.customerId = Objects.requireNonNull(customerId, "customerId must not be null");
        this.requestedBy = Objects.requireNonNull(requestedBy, "requestedBy must not be null");
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getRequestedBy() {
        return requestedBy;
    }
}
