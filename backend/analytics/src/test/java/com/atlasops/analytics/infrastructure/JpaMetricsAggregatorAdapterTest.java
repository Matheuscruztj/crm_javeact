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
    when(entityManager.createNativeQuery("SELECT COUNT(*) FROM customers WHERE tenant_id = :tenantId"))
        .thenReturn(query);
    when(query.setParameter("tenantId", TENANT_ID)).thenReturn(query);
    when(query.getSingleResult()).thenReturn(7L);

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
    Query customers = mock(Query.class);
    Query requests = mock(Query.class);
    Query activeRequests = mock(Query.class);
    Query documents = mock(Query.class);
    Query approvals = mock(Query.class);

    when(entityManager.createNativeQuery("SELECT COUNT(*) FROM customers WHERE tenant_id = :tenantId"))
        .thenReturn(customers);
    when(customers.setParameter("tenantId", TENANT_ID)).thenReturn(customers);
    when(customers.getSingleResult()).thenReturn(3L);

    when(entityManager.createNativeQuery("SELECT COUNT(*) FROM service_requests WHERE tenant_id = :tenantId"))
        .thenReturn(requests);
    when(requests.setParameter("tenantId", TENANT_ID)).thenReturn(requests);
    when(requests.getSingleResult()).thenReturn(5L);

    when(entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM service_requests WHERE tenant_id = :tenantId AND status NOT IN ('CLOSED', 'CANCELLED', 'REJECTED')"))
        .thenReturn(activeRequests);
    when(activeRequests.setParameter("tenantId", TENANT_ID)).thenReturn(activeRequests);
    when(activeRequests.getSingleResult()).thenReturn(2L);

    when(entityManager.createNativeQuery("SELECT COUNT(*) FROM documents WHERE tenant_id = :tenantId"))
        .thenReturn(documents);
    when(documents.setParameter("tenantId", TENANT_ID)).thenReturn(documents);
    when(documents.getSingleResult()).thenReturn(11L);

    when(entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM approvals WHERE tenant_id = :tenantId AND status = 'PENDING'"))
        .thenReturn(approvals);
    when(approvals.setParameter("tenantId", TENANT_ID)).thenReturn(approvals);
    when(approvals.getSingleResult()).thenReturn(4L);

    var summary = adapter.computeDashboard(TENANT_ID);

    assertThat(summary.get(MetricName.CUSTOMER_COUNT)).isEqualTo(3.0);
    assertThat(summary.get(MetricName.REQUEST_COUNT)).isEqualTo(5.0);
    assertThat(summary.get(MetricName.ACTIVE_REQUEST_COUNT)).isEqualTo(2.0);
    assertThat(summary.get(MetricName.DOCUMENT_COUNT)).isEqualTo(11.0);
    assertThat(summary.get(MetricName.PENDING_APPROVAL_COUNT)).isEqualTo(4.0);
  }
}
