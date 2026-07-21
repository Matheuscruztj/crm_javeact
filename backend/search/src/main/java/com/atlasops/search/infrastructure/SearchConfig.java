package com.atlasops.search.infrastructure;

import com.atlasops.search.domain.ports.SearchIndexPort;
import com.atlasops.shared.domain.ports.FeatureFlagPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration for search adapters. Wires either OpenSearch (when feature flag enabled)
 * or the default PostgreSQL full-text search adapter.
 *
 * <p>Validates: P1.3 — OpenSearch Integration stub with feature flag
 */
@Configuration
public class SearchConfig {

    /**
     * Creates the active {@link SearchIndexPort} bean. When the {@code opensearch}
     * feature flag is enabled, returns an {@link OpenSearchAdapter} wrapping the
     * PostgreSQL fallback. Otherwise, the {@link PostgresFullTextSearchAdapter}
     * (already a {@code @Component}) is used directly.
     *
     * <p>The {@code @Primary} annotation ensures this bean takes precedence over
     * the {@code @Component}-annotated {@link PostgresFullTextSearchAdapter}.
     */
    @Bean
    @Primary
    public SearchIndexPort searchIndexPort(
            PostgresFullTextSearchAdapter postgresAdapter, FeatureFlagPort featureFlags) {
        return new OpenSearchAdapter(postgresAdapter, postgresAdapter, featureFlags);
    }
}
