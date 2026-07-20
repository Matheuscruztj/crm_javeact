package com.atlasops.shared.domain.ports;

/**
 * Port for feature flag evaluation. Implementations may read from property sources,
 * Redis, or remote configuration services. Supports both global and per-tenant flags.
 *
 * <p>Usage pattern:
 * <pre>{@code
 * if (featureFlags.isEnabled("opensearch")) {
 *   // use OpenSearch
 * } else {
 *   // fallback to PostgreSQL
 * }
 * }</pre>
 *
 * <p>Validates: P1.20 — Feature Flag Framework
 */
public interface FeatureFlagPort {

    /**
     * Checks whether a feature flag is enabled globally.
     *
     * @param flagName the flag identifier (e.g., "opensearch", "neo4j")
     * @return true if the flag is enabled
     */
    boolean isEnabled(String flagName);

    /**
     * Checks whether a feature flag is enabled for a specific tenant.
     * Falls back to the global flag value if no tenant-specific override exists.
     *
     * @param flagName the flag identifier
     * @param tenantId the tenant to check for a per-tenant override
     * @return true if the flag is enabled for this tenant
     */
    boolean isEnabled(String flagName, String tenantId);
}
