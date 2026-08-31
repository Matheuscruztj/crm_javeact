package com.atlasops.analytics.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.atlasops.analytics.domain.MetricName;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JpaMetricsAggregatorAdapterTest {

  private static final String TENANT_ID = "tenant-alpha";

  private JpaMetricsAggregatorAdapter adapter;
  private EntityManager entityManager;

  @BeforeEach
  void setUp() throws Exception {
    adapter = new JpaMetricsAggregatorAdapter();
    entityManager = mock(EntityManager.class);
    Field field = JpaMetricsAggregatorAdapter.class.getDeclaredField("em");
    field.setAccessible(true);
    field.set(adapter, entityManager);
  }

  @Test
  void should_computeMetric_when_metricIsKnown() {
    Query query = mock(Query.class);
    when(entityManager.createNativeQuery(
            """
                    SELECT
                        (SELECT COUNT(*) FROM customers WHERE tenant_id = :tenantId) AS customer_count,
                        (SELECT COUNT(*) FROM service_requests WHERE tenant_id = :tenantId) AS request_count,
                        (SELECT COUNT(*) FROM service_requests WHERE tenant_id = :tenantId AND status NOT IN ('CLOSED', 'CANCELLED', 'REJECTED')) AS active_request_count,
                        (SELECT COUNT(*) FROM documents WHERE tenant_id = :tenantId) AS document_count,
                        (SELECT COUNT(*) FROM approvals WHERE tenant_id = :tenantId AND status = 'PENDING') AS pending_approval_count
                    """))
        .thenReturn(query);
    when(query.setParameter("tenantId", TENANT_ID)).thenReturn(query);
    when(query.getSingleResult()).thenReturn(new Object[] {7L, 5L, 2L, 11L, 4L});

    var result = adapter.computeMetric(TENANT_ID, MetricName.CUSTOMER_COUNT);

    assertThat(result.value()).isEqualTo(7.0);
    assertThat(result.name()).isEqualTo(MetricName.CUSTOMER_COUNT);
  }

  @Test
  void should_returnZero_when_metricIsUnsupported() {
    var result = adapter.computeMetric(TENANT_ID, MetricName.AI_AVG_CONFIDENCE);

    assertThat(result.value()).isZero();
    assertThat(result.name()).isEqualTo(MetricName.AI_AVG_CONFIDENCE);
  }

  @Test
  void should_returnDashboard_when_queriesSucceed() {
    Query query = mock(Query.class);
    when(entityManager.createNativeQuery(
            """
                    SELECT
                        (SELECT COUNT(*) FROM customers WHERE tenant_id = :tenantId) AS customer_count,
                        (SELECT COUNT(*) FROM service_requests WHERE tenant_id = :tenantId) AS request_count,
                        (SELECT COUNT(*) FROM service_requests WHERE tenant_id = :tenantId AND status NOT IN ('CLOSED', 'CANCELLED', 'REJECTED')) AS active_request_count,
                        (SELECT COUNT(*) FROM documents WHERE tenant_id = :tenantId) AS document_count,
                        (SELECT COUNT(*) FROM approvals WHERE tenant_id = :tenantId AND status = 'PENDING') AS pending_approval_count
                    """))
        .thenReturn(query);
    when(query.setParameter("tenantId", TENANT_ID)).thenReturn(query);
    when(query.getSingleResult()).thenReturn(new Object[] {3L, 5L, 2L, 11L, 4L});

    var summary = adapter.computeDashboard(TENANT_ID);

    assertThat(summary.get(MetricName.CUSTOMER_COUNT)).isEqualTo(3.0);
    assertThat(summary.get(MetricName.REQUEST_COUNT)).isEqualTo(5.0);
    assertThat(summary.get(MetricName.ACTIVE_REQUEST_COUNT)).isEqualTo(2.0);
    assertThat(summary.get(MetricName.DOCUMENT_COUNT)).isEqualTo(11.0);
    assertThat(summary.get(MetricName.PENDING_APPROVAL_COUNT)).isEqualTo(4.0);
  }

  @Test
  void should_returnZeroDashboard_when_projectionFails() {
    Query query = mock(Query.class);
    when(entityManager.createNativeQuery(
            """
                    SELECT
                        (SELECT COUNT(*) FROM customers WHERE tenant_id = :tenantId) AS customer_count,
                        (SELECT COUNT(*) FROM service_requests WHERE tenant_id = :tenantId) AS request_count,
                        (SELECT COUNT(*) FROM service_requests WHERE tenant_id = :tenantId AND status NOT IN ('CLOSED', 'CANCELLED', 'REJECTED')) AS active_request_count,
                        (SELECT COUNT(*) FROM documents WHERE tenant_id = :tenantId) AS document_count,
                        (SELECT COUNT(*) FROM approvals WHERE tenant_id = :tenantId AND status = 'PENDING') AS pending_approval_count
                    """))
        .thenReturn(query);
    when(query.setParameter("tenantId", TENANT_ID)).thenThrow(new RuntimeException("boom"));

    var summary = adapter.computeDashboard(TENANT_ID);

    assertThat(summary.get(MetricName.CUSTOMER_COUNT)).isZero();
    assertThat(summary.get(MetricName.REQUEST_COUNT)).isZero();
    assertThat(summary.get(MetricName.ACTIVE_REQUEST_COUNT)).isZero();
    assertThat(summary.get(MetricName.DOCUMENT_COUNT)).isZero();
    assertThat(summary.get(MetricName.PENDING_APPROVAL_COUNT)).isZero();
  }
}
