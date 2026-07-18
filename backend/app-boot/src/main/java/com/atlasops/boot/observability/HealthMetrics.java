package com.atlasops.boot.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.boot.actuate.health.HealthContributorRegistry;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Exposes health indicator statuses as Micrometer gauges.
 *
 * <p>Periodically polls health indicators and publishes a gauge {@code atlasops_health_status} per
 * dependency, where 1 = UP and 0 = DOWN.
 *
 * <p>Monitored dependencies: postgres (db), redis, minio.
 */
@Component
public class HealthMetrics {

  private static final String METRIC_NAME = "atlasops_health_status";

  /** Maps logical dependency names to Spring Boot health contributor names. */
  private static final Map<String, String> DEPENDENCY_TO_CONTRIBUTOR =
      Map.of(
          "postgres", "db",
          "redis", "redis",
          "minio", "minio");

  private final HealthContributorRegistry healthContributorRegistry;
  private final ConcurrentHashMap<String, AtomicInteger> statusMap = new ConcurrentHashMap<>();

  public HealthMetrics(
      HealthContributorRegistry healthContributorRegistry, MeterRegistry meterRegistry) {
    this.healthContributorRegistry = healthContributorRegistry;

    // Register gauges for each dependency
    for (String dependency : DEPENDENCY_TO_CONTRIBUTOR.keySet()) {
      AtomicInteger status = new AtomicInteger(0);
      statusMap.put(dependency, status);
      meterRegistry.gauge(METRIC_NAME, Tags.of("dependency", dependency), status);
    }
  }

  /** Polls health indicators every 30 seconds and updates gauge values. */
  @Scheduled(fixedDelay = 30_000, initialDelay = 5_000)
  public void updateHealthMetrics() {
    for (var entry : DEPENDENCY_TO_CONTRIBUTOR.entrySet()) {
      String dependency = entry.getKey();
      String contributorName = entry.getValue();
      AtomicInteger gauge = statusMap.get(dependency);

      var contributor = healthContributorRegistry.getContributor(contributorName);
      if (contributor instanceof HealthIndicator indicator) {
        var health = indicator.health();
        gauge.set(Status.UP.equals(health.getStatus()) ? 1 : 0);
      } else {
        gauge.set(0);
      }
    }
  }

  /**
   * Returns the current health status value for a dependency. Useful for testing.
   *
   * @param dependency the dependency name (postgres, redis, minio)
   * @return 1 if up, 0 if down, or -1 if unknown dependency
   */
  public int getStatus(String dependency) {
    AtomicInteger value = statusMap.get(dependency);
    return value != null ? value.get() : -1;
  }
}
