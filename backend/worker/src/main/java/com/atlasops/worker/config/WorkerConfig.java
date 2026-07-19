package com.atlasops.worker.config;

import com.atlasops.worker.infrastructure.redis.StreamConsumerConfig;
import com.atlasops.worker.infrastructure.retry.RetryConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration for worker infrastructure beans. */
@Configuration
@EnableConfigurationProperties({StreamConsumerConfig.class, RetryConfig.class})
public class WorkerConfig {

  /**
   * Provides default StreamConsumerConfig if not configured.
   *
   * @return default configuration
   */
  @Bean
  public StreamConsumerConfig streamConsumerConfig() {
    return StreamConsumerConfig.defaults();
  }

  /**
   * Provides default RetryConfig if not configured.
   *
   * @return default configuration
   */
  @Bean
  public RetryConfig retryConfig() {
    return RetryConfig.defaults();
  }
}
