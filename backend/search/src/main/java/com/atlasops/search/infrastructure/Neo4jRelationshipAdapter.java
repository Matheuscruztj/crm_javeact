package com.atlasops.search.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Stub adapter for Neo4j relationship graph queries.
 * Only instantiated when the {@code app.features.neo4j.enabled} feature flag is true.
 *
 * <p>Not yet implemented — placeholder for graph-based customer/entity relationship queries.
 *
 * <p>Validates: P2.2 — Neo4j relationship adapter stub with feature flag
 */
@Component
@ConditionalOnProperty(name = "app.features.neo4j.enabled", havingValue = "true")
public class Neo4jRelationshipAdapter {

    private static final Logger log = LoggerFactory.getLogger(Neo4jRelationshipAdapter.class);

    public Neo4jRelationshipAdapter() {
        log.info("Neo4j adapter not yet implemented — feature flag enabled but implementation pending");
    }

    /**
     * Stub method — not yet implemented.
     *
     * @throws UnsupportedOperationException always
     */
    public void queryRelationships() {
        throw new UnsupportedOperationException(
                "Neo4j adapter not yet implemented. Feature flag enabled but dependency not configured.");
    }
}
