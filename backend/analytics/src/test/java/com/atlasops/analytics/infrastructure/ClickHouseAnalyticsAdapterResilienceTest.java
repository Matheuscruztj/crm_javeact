package com.atlasops.analytics.infrastructure;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClickHouseAnalyticsAdapterResilienceTest {

  @Test
  @DisplayName("should_ignoreQuery_when_clickhouseAdapterIsInvoked")
  void should_ignoreQuery_when_clickhouseAdapterIsInvoked() {
    ClickHouseAnalyticsAdapter adapter = new ClickHouseAnalyticsAdapter();

    assertThatCode(() -> adapter.executeQuery("SELECT 1")).doesNotThrowAnyException();
  }
}
