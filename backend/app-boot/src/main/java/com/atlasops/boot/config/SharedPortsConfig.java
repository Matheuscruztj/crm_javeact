package com.atlasops.boot.config;

import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import java.time.Instant;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration providing production implementations of shared-kernel ports (Clock, IdGenerator).
 */
@Configuration
public class SharedPortsConfig {

  @Bean
  public Clock clock() {
    return Instant::now;
  }

  @Bean
  public IdGenerator idGenerator() {
    return () -> UUID.randomUUID().toString();
  }
}
