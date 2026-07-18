package com.atlasops.boot.observability;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link MetricsConfiguration}. */
class MetricsConfigurationTest {

  private MetricsConfiguration configuration;
  private MeterRegistry meterRegistry;

  @BeforeEach
  void setUp() {
    configuration = new MetricsConfiguration();
    meterRegistry = new SimpleMeterRegistry();
  }

  @Test
  void should_registerRequestDurationTimer_when_meterBinderBound() {
    MeterBinder binder = configuration.requestDurationMeterBinder();
    binder.bindTo(meterRegistry);

    Timer timer = meterRegistry.find(MetricsConfiguration.REQUEST_DURATION).timer();
    assertThat(timer).isNotNull();
  }

  @Test
  void should_haveCorrectDescription_when_timerRegistered() {
    MeterBinder binder = configuration.requestDurationMeterBinder();
    binder.bindTo(meterRegistry);

    Timer timer = meterRegistry.find(MetricsConfiguration.REQUEST_DURATION).timer();
    assertThat(timer).isNotNull();
    assertThat(timer.getId().getDescription()).isEqualTo("HTTP request duration in seconds");
  }
}
