/**
 * Health indicators for AtlasOps application readiness checks.
 *
 * <p>Contains custom {@link org.springframework.boot.actuate.health.HealthIndicator}
 * implementations for verifying connectivity to external dependencies (MinIO/S3, PostgreSQL,
 * Redis).
 */
package com.atlasops.boot.health;
