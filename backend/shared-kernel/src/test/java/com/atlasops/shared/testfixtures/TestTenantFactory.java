package com.atlasops.shared.testfixtures;

/**
 * Factory for creating run-scoped tenant IDs in integration tests.
 *
 * <p>Using a unique test run ID prefix ensures that tenants created by one test
 * run do not collide with tenants from a parallel or previously interrupted run.
 * This prevents flaky tests caused by pre-existing data.
 *
 * <p>Usage:
 * <pre>{@code
 * String runId = "a1b2c3d4"; // from TestRunConfig.testRunId()
 * String tenantId = TestTenantFactory.createTenantId(runId, "alpha");
 * // → "test-tenant-a1b2c3d4-alpha"
 * }</pre>
 *
 * <p>Validates: P1.25 — Test Isolation Infrastructure (TEST_RUN_ID)
 */
public final class TestTenantFactory {

    private TestTenantFactory() {
        // static factory — not instantiable
    }

    /**
     * Creates a unique tenant ID scoped to this test run to prevent cross-test tenant collisions.
     *
     * @param runId  the unique test run ID (e.g., "a1b2c3d4" from TestRunConfig)
     * @param suffix a short human-readable label for the tenant (e.g., "alpha", "beta")
     * @return a formatted tenant ID string like {@code "test-tenant-a1b2c3d4-alpha"}
     */
    public static String createTenantId(String runId, String suffix) {
        if (runId == null || runId.isBlank()) {
            throw new IllegalArgumentException("runId must not be blank");
        }
        if (suffix == null || suffix.isBlank()) {
            throw new IllegalArgumentException("suffix must not be blank");
        }
        return "test-tenant-" + runId + "-" + suffix;
    }

    /**
     * Creates a short tenant ID using only the first 4 chars of runId.
     * Useful when tenant IDs have length restrictions.
     *
     * @param runId  the unique test run ID
     * @param suffix a short label
     * @return a shortened tenant ID string
     */
    public static String createShortTenantId(String runId, String suffix) {
        String shortRun = runId != null && runId.length() >= 4
                ? runId.substring(0, 4)
                : runId;
        return createTenantId(shortRun, suffix);
    }
}
