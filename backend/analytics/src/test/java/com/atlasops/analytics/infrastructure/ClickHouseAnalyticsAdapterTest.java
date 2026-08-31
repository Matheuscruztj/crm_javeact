package com.atlasops.analytics.infrastructure;

import org.junit.jupiter.api.Test;

class ClickHouseAnalyticsAdapterTest {

  @Test
  void should_executeQuery_asNoOp() {
    new ClickHouseAnalyticsAdapter().executeQuery("SELECT 1");
  }
}
