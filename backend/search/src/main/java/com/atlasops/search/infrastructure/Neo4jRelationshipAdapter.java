package com.atlasops.search.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Adapter for Neo4j relationship graph queries.
 * Only instantiated when the {@code app.features.neo4j.enabled} feature flag is true.
 *
 * <p>When the feature flag is enabled but the Neo4j dependency is unavailable, this adapter
 * degrades gracefully and acts as a no-op so the application can continue to start.
 *
 * <p>Validates: P2.2 — Neo4j relationship adapter stub with feature flag
 */
@Component
@ConditionalOnProperty(name = "app.features.neo4j.enabled", havingValue = "true")
public class Neo4jRelationshipAdapter {

    private static final Logger log = LoggerFactory.getLogger(Neo4jRelationshipAdapter.class);

    public Neo4jRelationshipAdapter() {
        log.info("Neo4j adapter enabled without Neo4j dependency; using no-op fallback");
    }

    public void queryRelationships() {
        log.debug("Neo4j relationship query requested but dependency is not configured; no-op fallback");
    }
}
