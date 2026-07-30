package com.atlasops.analytics.infrastructure;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TimescaleMetricsAdapterResilienceTest {

  @Test
  @DisplayName("should_ignoreMetric_when_timescaleAdapterIsInvoked")
  void should_ignoreMetric_when_timescaleAdapterIsInvoked() {
    TimescaleMetricsAdapter adapter = new TimescaleMetricsAdapter();

    assertThatCode(() -> adapter.recordMetric("document_processing_duration", 42.0))
        .doesNotThrowAnyException();
  }
}
