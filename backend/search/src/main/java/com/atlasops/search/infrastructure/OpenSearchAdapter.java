package com.atlasops.search.infrastructure;

import com.atlasops.search.domain.SearchQuery;
import com.atlasops.search.domain.SearchResult;
import com.atlasops.search.domain.ports.SearchIndexPort;
import com.atlasops.search.domain.ports.SearchIndexUpdatePort;
import com.atlasops.shared.domain.ports.FeatureFlagPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * OpenSearch adapter stub implementing both {@link SearchIndexPort} and {@link SearchIndexUpdatePort}.
 * When the {@code opensearch} feature flag is enabled, delegates to OpenSearch client (not yet available).
 * Otherwise, delegates to the PostgreSQL full-text search fallback.
 *
 * <p>Validates: P1.3 — OpenSearch Integration stub with feature flag
 */
public class OpenSearchAdapter implements SearchIndexPort, SearchIndexUpdatePort {

    private static final Logger log = LoggerFactory.getLogger(OpenSearchAdapter.class);
    private static final String FEATURE_FLAG = "opensearch";

    private final SearchIndexPort fallback;
    private final SearchIndexUpdatePort fallbackUpdate;
    private final FeatureFlagPort featureFlags;

    public OpenSearchAdapter(
            SearchIndexPort fallback,
            SearchIndexUpdatePort fallbackUpdate,
            FeatureFlagPort featureFlags) {
        this.fallback = fallback;
        this.fallbackUpdate = fallbackUpdate;
        this.featureFlags = featureFlags;
    }

    @Override
    public Page<SearchResult> search(SearchQuery query, Pageable pageable) {
        if (featureFlags.isEnabled(FEATURE_FLAG)) {
            // OpenSearch client not yet available — will be wired in P2.2
            throw new UnsupportedOperationException(
                    "OpenSearch client not yet configured. Enable after wiring dependency.");
        }
        log.debug("Feature flag '{}' disabled — delegating search to fallback adapter", FEATURE_FLAG);
        return fallback.search(query, pageable);
    }

    @Override
    public void indexEntity(String entityType, String entityId, String content, String tenantId) {
        if (featureFlags.isEnabled(FEATURE_FLAG)) {
            throw new UnsupportedOperationException(
                    "OpenSearch client not yet configured. Enable after wiring dependency.");
        }
        log.debug("Feature flag '{}' disabled — delegating indexEntity to fallback adapter", FEATURE_FLAG);
        fallbackUpdate.indexEntity(entityType, entityId, content, tenantId);
    }

    @Override
    public void removeEntity(String entityType, String entityId, String tenantId) {
        if (featureFlags.isEnabled(FEATURE_FLAG)) {
            throw new UnsupportedOperationException(
                    "OpenSearch client not yet configured. Enable after wiring dependency.");
        }
        log.debug("Feature flag '{}' disabled — delegating removeEntity to fallback adapter", FEATURE_FLAG);
        fallbackUpdate.removeEntity(entityType, entityId, tenantId);
    }
}
