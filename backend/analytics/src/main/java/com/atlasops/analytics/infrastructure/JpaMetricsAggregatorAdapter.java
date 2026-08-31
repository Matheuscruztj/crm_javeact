package com.atlasops.analytics.infrastructure;

import com.atlasops.analytics.domain.DashboardSummary;
import com.atlasops.analytics.domain.Metric;
import com.atlasops.analytics.domain.MetricName;
import com.atlasops.analytics.domain.ports.MetricsAggregator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA-based implementation of {@link MetricsAggregator}.
 *
 * <p>Computes dashboard metrics via native JPQL/native queries against the main tables.
 * Results are not cached at this layer — caching can be added at the use case layer.
 *
 * <p>Validates: P0.C.3.1 — Aggregation queries for analytics dashboard
 */
@Component
@Transactional(readOnly = true)
public class JpaMetricsAggregatorAdapter implements MetricsAggregator {

    @PersistenceContext
    private EntityManager em;

    @Override
    public DashboardSummary computeDashboard(String tenantId) {
        Instant now = Instant.now();
        DashboardMetricsProjection projection = loadDashboardMetricsProjection(tenantId);
        Map<MetricName, Double> metrics = new EnumMap<>(MetricName.class);
        metrics.put(MetricName.CUSTOMER_COUNT, projection.customerCount());
        metrics.put(MetricName.REQUEST_COUNT, projection.requestCount());
        metrics.put(MetricName.ACTIVE_REQUEST_COUNT, projection.activeRequestCount());
        metrics.put(MetricName.DOCUMENT_COUNT, projection.documentCount());
        metrics.put(MetricName.PENDING_APPROVAL_COUNT, projection.pendingApprovalCount());
        return new DashboardSummary(tenantId, metrics, now);
    }

    @Override
    public Metric computeMetric(String tenantId, MetricName name) {
        Instant now = Instant.now();
        DashboardMetricsProjection projection = loadDashboardMetricsProjection(tenantId);
        double value =
            switch (name) {
              case CUSTOMER_COUNT -> projection.customerCount();
              case REQUEST_COUNT -> projection.requestCount();
              case ACTIVE_REQUEST_COUNT -> projection.activeRequestCount();
              case DOCUMENT_COUNT -> projection.documentCount();
              case PENDING_APPROVAL_COUNT -> projection.pendingApprovalCount();
              default -> 0.0;
            };
        return Metric.of(tenantId, name, value, now);
    }

    private DashboardMetricsProjection loadDashboardMetricsProjection(String tenantId) {
        try {
            Object[] result = (Object[]) em.createNativeQuery("""
                    SELECT
                        (SELECT COUNT(*) FROM customers WHERE tenant_id = :tenantId) AS customer_count,
                        (SELECT COUNT(*) FROM service_requests WHERE tenant_id = :tenantId) AS request_count,
                        (SELECT COUNT(*) FROM service_requests WHERE tenant_id = :tenantId AND status NOT IN ('CLOSED', 'CANCELLED', 'REJECTED')) AS active_request_count,
                        (SELECT COUNT(*) FROM documents WHERE tenant_id = :tenantId) AS document_count,
                        (SELECT COUNT(*) FROM approvals WHERE tenant_id = :tenantId AND status = 'PENDING') AS pending_approval_count
                    """)
                    .setParameter("tenantId", tenantId)
                    .getSingleResult();
            return new DashboardMetricsProjection(
                    toDouble(result[0]),
                    toDouble(result[1]),
                    toDouble(result[2]),
                    toDouble(result[3]),
                    toDouble(result[4]));
        } catch (Exception e) {
            return new DashboardMetricsProjection(0.0, 0.0, 0.0, 0.0, 0.0);
        }
    }

    private double toDouble(Object value) {
        return value instanceof Number number ? number.doubleValue() : 0.0;
    }
}
