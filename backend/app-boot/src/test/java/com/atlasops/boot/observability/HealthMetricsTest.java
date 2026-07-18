package com.atlasops.boot.observability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthContributorRegistry;
import org.springframework.boot.actuate.health.HealthIndicator;

/** Unit tests for {@link HealthMetrics}. */
class HealthMetricsTest {

  private HealthContributorRegistry registry;
  private MeterRegistry meterRegistry;
  private HealthIndicator dbIndicator;
  private HealthIndicator redisIndicator;
  private HealthIndicator minioIndicator;
  private HealthMetrics healthMetrics;

  @BeforeEach
  void setUp() {
    registry = mock(HealthContributorRegistry.class);
    meterRegistry = new SimpleMeterRegistry();

    dbIndicator = mock(HealthIndicator.class);
    redisIndicator = mock(HealthIndicator.class);
    minioIndicator = mock(HealthIndicator.class);

    when(registry.getContributor("db")).thenReturn(dbIndicator);
    when(registry.getContributor("redis")).thenReturn(redisIndicator);
    when(registry.getContributor("minio")).thenReturn(minioIndicator);

    healthMetrics = new HealthMetrics(registry, meterRegistry);
  }

  @Test
  void should_registerGauges_when_constructed() {
    assertThat(meterRegistry.find("atlasops_health_status").tag("dependency", "postgres").gauge())
        .isNotNull();
    assertThat(meterRegistry.find("atlasops_health_status").tag("dependency", "redis").gauge())
        .isNotNull();
    assertThat(meterRegistry.find("atlasops_health_status").tag("dependency", "minio").gauge())
        .isNotNull();
  }

  @Test
  void should_reportAllUp_when_allDependenciesAreHealthy() {
    when(dbIndicator.health()).thenReturn(Health.up().build());
    when(redisIndicator.health()).thenReturn(Health.up().build());
    when(minioIndicator.health()).thenReturn(Health.up().build());

    healthMetrics.updateHealthMetrics();

    assertThat(healthMetrics.getStatus("postgres")).isEqualTo(1);
    assertThat(healthMetrics.getStatus("redis")).isEqualTo(1);
    assertThat(healthMetrics.getStatus("minio")).isEqualTo(1);
  }

  @Test
  void should_reportDown_when_postgresIsUnhealthy() {
    when(dbIndicator.health()).thenReturn(Health.down().build());
    when(redisIndicator.health()).thenReturn(Health.up().build());
    when(minioIndicator.health()).thenReturn(Health.up().build());

    healthMetrics.updateHealthMetrics();

    assertThat(healthMetrics.getStatus("postgres")).isEqualTo(0);
    assertThat(healthMetrics.getStatus("redis")).isEqualTo(1);
    assertThat(healthMetrics.getStatus("minio")).isEqualTo(1);
  }

  @Test
  void should_reportDown_when_redisIsUnhealthy() {
    when(dbIndicator.health()).thenReturn(Health.up().build());
    when(redisIndicator.health()).thenReturn(Health.down().build());
    when(minioIndicator.health()).thenReturn(Health.up().build());

    healthMetrics.updateHealthMetrics();

    assertThat(healthMetrics.getStatus("postgres")).isEqualTo(1);
    assertThat(healthMetrics.getStatus("redis")).isEqualTo(0);
    assertThat(healthMetrics.getStatus("minio")).isEqualTo(1);
  }

  @Test
  void should_reportDown_when_minioIsUnhealthy() {
    when(dbIndicator.health()).thenReturn(Health.up().build());
    when(redisIndicator.health()).thenReturn(Health.up().build());
    when(minioIndicator.health()).thenReturn(Health.down().build());

    healthMetrics.updateHealthMetrics();

    assertThat(healthMetrics.getStatus("postgres")).isEqualTo(1);
    assertThat(healthMetrics.getStatus("redis")).isEqualTo(1);
    assertThat(healthMetrics.getStatus("minio")).isEqualTo(0);
  }

  @Test
  void should_reportDown_when_contributorNotFound() {
    // Return null for all contributors
    when(registry.getContributor("db")).thenReturn(null);
    when(registry.getContributor("redis")).thenReturn(null);
    when(registry.getContributor("minio")).thenReturn(null);

    // Re-create to pick up the null contributors
    HealthMetrics metrics = new HealthMetrics(registry, new SimpleMeterRegistry());
    metrics.updateHealthMetrics();

    assertThat(metrics.getStatus("postgres")).isEqualTo(0);
    assertThat(metrics.getStatus("redis")).isEqualTo(0);
    assertThat(metrics.getStatus("minio")).isEqualTo(0);
  }

  @Test
  void should_returnMinusOne_when_unknownDependencyQueried() {
    assertThat(healthMetrics.getStatus("unknown")).isEqualTo(-1);
  }

  @Test
  void should_updateGaugeValues_when_statusChanges() {
    // First: all up
    when(dbIndicator.health()).thenReturn(Health.up().build());
    when(redisIndicator.health()).thenReturn(Health.up().build());
    when(minioIndicator.health()).thenReturn(Health.up().build());
    healthMetrics.updateHealthMetrics();

    assertThat(healthMetrics.getStatus("postgres")).isEqualTo(1);

    // Then: postgres goes down
    when(dbIndicator.health()).thenReturn(Health.down().build());
    healthMetrics.updateHealthMetrics();

    assertThat(healthMetrics.getStatus("postgres")).isEqualTo(0);
    assertThat(healthMetrics.getStatus("redis")).isEqualTo(1);
  }

  @Test
  void should_reflectGaugeInMeterRegistry() {
    when(dbIndicator.health()).thenReturn(Health.up().build());
    when(redisIndicator.health()).thenReturn(Health.up().build());
    when(minioIndicator.health()).thenReturn(Health.up().build());

    healthMetrics.updateHealthMetrics();

    var gauge = meterRegistry.find("atlasops_health_status").tag("dependency", "postgres").gauge();
    assertThat(gauge).isNotNull();
    assertThat(gauge.value()).isEqualTo(1.0);
  }
}
