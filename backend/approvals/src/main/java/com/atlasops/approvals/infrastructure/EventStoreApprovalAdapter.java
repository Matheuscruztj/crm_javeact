package com.atlasops.approvals.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Adapter for EventStoreDB event sourcing of approval workflows.
 * Only instantiated when the {@code app.features.eventstoredb.enabled} feature flag is true.
 *
 * <p>When EventStoreDB is not provisioned, this adapter degrades gracefully and acts as a no-op
 * while the PostgreSQL approval_ledger table remains the source of truth.
 *
 * <p>Validates: P2.6 — EventStoreDB approval adapter stub with feature flag
 */
@Component
@ConditionalOnProperty(name = "app.features.eventstoredb.enabled", havingValue = "true")
public class EventStoreApprovalAdapter {

    private static final Logger log = LoggerFactory.getLogger(EventStoreApprovalAdapter.class);

    public EventStoreApprovalAdapter() {
        log.info("EventStoreDB adapter enabled without EventStoreDB dependency; using no-op fallback");
    }

    public void appendEvent(String streamId, Object event) {
        log.debug("EventStore event append requested for stream '{}' but dependency is not configured; no-op fallback", streamId);
    }
}
