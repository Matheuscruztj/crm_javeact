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
        Map<MetricName, Double> metrics = new EnumMap<>(MetricName.class);

        metrics.put(MetricName.CUSTOMER_COUNT, countCustomers(tenantId));
        metrics.put(MetricName.REQUEST_COUNT, countRequests(tenantId));
        metrics.put(MetricName.ACTIVE_REQUEST_COUNT, countActiveRequests(tenantId));
        metrics.put(MetricName.DOCUMENT_COUNT, countDocuments(tenantId));
        metrics.put(MetricName.PENDING_APPROVAL_COUNT, countPendingApprovals(tenantId));

        return new DashboardSummary(tenantId, metrics, now);
    }

    @Override
    public Metric computeMetric(String tenantId, MetricName name) {
        Instant now = Instant.now();
        double value = switch (name) {
            case CUSTOMER_COUNT -> countCustomers(tenantId);
            case REQUEST_COUNT -> countRequests(tenantId);
            case ACTIVE_REQUEST_COUNT -> countActiveRequests(tenantId);
            case DOCUMENT_COUNT -> countDocuments(tenantId);
            case PENDING_APPROVAL_COUNT -> countPendingApprovals(tenantId);
            default -> 0.0;
        };
        return Metric.of(tenantId, name, value, now);
    }

    private double countCustomers(String tenantId) {
        try {
            Object result = em.createNativeQuery(
                    "SELECT COUNT(*) FROM customers WHERE tenant_id = :tenantId")
                    .setParameter("tenantId", tenantId)
                    .getSingleResult();
            return ((Number) result).doubleValue();
        } catch (Exception e) {
            return 0.0;
        }
    }

    private double countRequests(String tenantId) {
        try {
            Object result = em.createNativeQuery(
                    "SELECT COUNT(*) FROM service_requests WHERE tenant_id = :tenantId")
                    .setParameter("tenantId", tenantId)
                    .getSingleResult();
            return ((Number) result).doubleValue();
        } catch (Exception e) {
            return 0.0;
        }
    }

    private double countActiveRequests(String tenantId) {
        try {
            Object result = em.createNativeQuery(
                    "SELECT COUNT(*) FROM service_requests WHERE tenant_id = :tenantId "
                    + "AND status NOT IN ('CLOSED', 'CANCELLED', 'REJECTED')")
                    .setParameter("tenantId", tenantId)
                    .getSingleResult();
            return ((Number) result).doubleValue();
        } catch (Exception e) {
            return 0.0;
        }
    }

    private double countDocuments(String tenantId) {
        try {
            Object result = em.createNativeQuery(
                    "SELECT COUNT(*) FROM documents WHERE tenant_id = :tenantId")
                    .setParameter("tenantId", tenantId)
                    .getSingleResult();
            return ((Number) result).doubleValue();
        } catch (Exception e) {
            return 0.0;
        }
    }

    private double countPendingApprovals(String tenantId) {
        try {
            Object result = em.createNativeQuery(
                    "SELECT COUNT(*) FROM approvals WHERE tenant_id = :tenantId AND status = 'PENDING'")
                    .setParameter("tenantId", tenantId)
                    .getSingleResult();
            return ((Number) result).doubleValue();
        } catch (Exception e) {
            return 0.0;
        }
    }
}
