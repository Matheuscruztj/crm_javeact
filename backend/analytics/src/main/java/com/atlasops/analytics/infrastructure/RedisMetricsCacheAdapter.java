package com.atlasops.analytics.infrastructure;

import com.atlasops.analytics.domain.DashboardSummary;
import com.atlasops.analytics.domain.Metric;
import com.atlasops.analytics.domain.MetricName;
import com.atlasops.analytics.domain.ports.MetricsAggregator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis caching decorator for {@link MetricsAggregator}.
 *
 * <p>Wraps the JPA aggregator with a 5-minute Redis cache to reduce DB load.
 * Cache key pattern: {@code analytics:dashboard:{tenantId}}
 *
 * <p>Validates: P0.C.3.2 — Implement cache de métricas em Redis (TTL 5min)
 */
public class RedisMetricsCacheAdapter implements MetricsAggregator {

  private static final Logger log = LoggerFactory.getLogger(RedisMetricsCacheAdapter.class);
  private static final Duration CACHE_TTL = Duration.ofMinutes(5);
  private static final String DASHBOARD_KEY_PREFIX = "analytics:dashboard:";
  private static final String METRIC_KEY_PREFIX = "analytics:metric:";

  private final MetricsAggregator delegate;
  private final StringRedisTemplate redisTemplate;
  private final ObjectMapper objectMapper;

  public RedisMetricsCacheAdapter(
      MetricsAggregator delegate,
      StringRedisTemplate redisTemplate,
      ObjectMapper objectMapper) {
    this.delegate = delegate;
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
  }

  @Override
  public DashboardSummary computeDashboard(String tenantId) {
    String key = DASHBOARD_KEY_PREFIX + tenantId;

    Optional<DashboardSummary> cached = getFromCache(key, DashboardSummary.class);
    if (cached.isPresent()) {
      log.debug("Cache hit for dashboard: tenant={}", tenantId);
      return cached.get();
    }

    DashboardSummary fresh = delegate.computeDashboard(tenantId);
    storeInCache(key, fresh);
    return fresh;
  }

  @Override
  public Metric computeMetric(String tenantId, MetricName name) {
    String key = METRIC_KEY_PREFIX + tenantId + ":" + name.name();

    Optional<Metric> cached = getFromCache(key, Metric.class);
    if (cached.isPresent()) {
      log.debug("Cache hit for metric: tenant={}, name={}", tenantId, name);
      return cached.get();
    }

    Metric fresh = delegate.computeMetric(tenantId, name);
    storeInCache(key, fresh);
    return fresh;
  }

  private <T> Optional<T> getFromCache(String key, Class<T> type) {
    try {
      String json = redisTemplate.opsForValue().get(key);
      if (json == null) return Optional.empty();
      return Optional.of(objectMapper.readValue(json, type));
    } catch (Exception e) {
      log.warn("Cache read failed for key {}: {}", key, e.getMessage());
      return Optional.empty();
    }
  }

  private void storeInCache(String key, Object value) {
    try {
      String json = objectMapper.writeValueAsString(value);
      redisTemplate.opsForValue().set(key, json, CACHE_TTL);
    } catch (Exception e) {
      log.warn("Cache write failed for key {}: {}", key, e.getMessage());
    }
  }
}
