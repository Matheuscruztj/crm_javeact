package com.atlasops.analytics.infrastructure;

/**
 * Read model projection for dashboard metrics.
 *
 * <p>This projection keeps the hot analytics summary separate from the domain aggregate and makes
 * the DB-to-dashboard mapping explicit.
 */
record DashboardMetricsProjection(
    double customerCount,
    double requestCount,
    double activeRequestCount,
    double documentCount,
    double pendingApprovalCount) {}
