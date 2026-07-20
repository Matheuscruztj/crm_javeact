package com.atlasops.approvals.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Stub adapter for EventStoreDB event sourcing of approval workflows.
 * Only instantiated when the {@code app.features.eventstoredb.enabled} feature flag is true.
 *
 * <p>Not yet implemented — placeholder for full event sourcing support with EventStoreDB.
 * Currently approval state is tracked via the PostgreSQL approval_ledger table.
 *
 * <p>Validates: P2.6 — EventStoreDB approval adapter stub with feature flag
 */
@Component
@ConditionalOnProperty(name = "app.features.eventstoredb.enabled", havingValue = "true")
public class EventStoreApprovalAdapter {

    private static final Logger log = LoggerFactory.getLogger(EventStoreApprovalAdapter.class);

    public EventStoreApprovalAdapter() {
        log.info("EventStoreDB adapter not yet implemented — feature flag enabled but implementation pending");
    }

    /**
     * Stub method — not yet implemented.
     *
     * @throws UnsupportedOperationException always
     */
    public void appendEvent(String streamId, Object event) {
        throw new UnsupportedOperationException(
                "EventStoreDB adapter not yet implemented. Feature flag enabled but dependency not configured.");
    }
}
