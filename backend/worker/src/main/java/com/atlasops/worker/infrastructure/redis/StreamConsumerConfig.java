package com.atlasops.worker.infrastructure.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Configuration properties for Redis Streams consumer. */
@ConfigurationProperties(prefix = "atlasops.worker.streams")
public record StreamConsumerConfig(
    String groupName,
    String consumerName,
    int batchSize,
    int maxConcurrentHandlers,
    long blockTimeoutMs,
    long claimIdleTimeMs,
    long reconnectInitialDelayMs,
    long reconnectMaxDelayMs) {

  public StreamConsumerConfig {
    if (groupName == null || groupName.isBlank()) {
      groupName = "atlasops-worker";
    }
    if (consumerName == null || consumerName.isBlank()) {
      consumerName = "consumer-" + ProcessHandle.current().pid();
    }
    if (batchSize <= 0) {
      batchSize = 10;
    }
    if (maxConcurrentHandlers <= 0) {
      maxConcurrentHandlers = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
    }
    if (blockTimeoutMs <= 0) {
      blockTimeoutMs = 2000;
    }
    if (claimIdleTimeMs <= 0) {
      claimIdleTimeMs = 60000;
    }
    if (reconnectInitialDelayMs <= 0) {
      reconnectInitialDelayMs = 1000;
    }
    if (reconnectMaxDelayMs <= 0) {
      reconnectMaxDelayMs = 30000;
    }
  }

  /**
   * Creates default configuration.
   *
   * @return default StreamConsumerConfig
   */
  public static StreamConsumerConfig defaults() {
    return new StreamConsumerConfig(null, null, 0, 0, 0, 0, 0, 0);
  }
}
