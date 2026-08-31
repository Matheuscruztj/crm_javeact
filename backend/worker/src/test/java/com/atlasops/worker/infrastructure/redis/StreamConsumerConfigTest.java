package com.atlasops.worker.infrastructure.redis;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StreamConsumerConfigTest {

  @Test
  void should_applySafeDefaults_whenConfigurationIsMissing() {
    StreamConsumerConfig config = StreamConsumerConfig.defaults();

    assertThat(config.groupName()).isEqualTo("atlasops-worker");
    assertThat(config.consumerName()).startsWith("consumer-");
    assertThat(config.batchSize()).isEqualTo(10);
    assertThat(config.maxConcurrentHandlers()).isGreaterThanOrEqualTo(1);
    assertThat(config.blockTimeoutMs()).isEqualTo(2000);
    assertThat(config.claimIdleTimeMs()).isEqualTo(60000);
    assertThat(config.reconnectInitialDelayMs()).isEqualTo(1000);
    assertThat(config.reconnectMaxDelayMs()).isEqualTo(30000);
  }

  @Test
  void should_honorExplicitConcurrency_whenProvided() {
    StreamConsumerConfig config =
        new StreamConsumerConfig("group", "consumer-a", 8, 3, 1500, 5000, 250, 8000);

    assertThat(config.maxConcurrentHandlers()).isEqualTo(3);
    assertThat(config.batchSize()).isEqualTo(8);
    assertThat(config.blockTimeoutMs()).isEqualTo(1500);
  }
}
