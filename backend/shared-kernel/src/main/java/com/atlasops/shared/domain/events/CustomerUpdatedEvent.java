package com.atlasops.shared.domain.events;

import com.atlasops.shared.domain.DomainEvent;
import java.util.Objects;

/**
 * Published when a customer's data is updated (name, email, address, status).
 * Validates: P0.P.2.1 — customer.updated.v1 domain event
 */
public final class CustomerUpdatedEvent extends DomainEvent {

    private final String customerId;
    private final String actorId;

    public CustomerUpdatedEvent(String customerId, String tenantId, String actorId, String correlationId) {
        super(tenantId, correlationId);
        this.customerId = Objects.requireNonNull(customerId, "customerId must not be null");
        this.actorId = Objects.requireNonNull(actorId, "actorId must not be null");
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getActorId() {
        return actorId;
    }
}
