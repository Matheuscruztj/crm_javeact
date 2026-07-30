package com.atlasops.boot.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.Callable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ResilienceConfigResilienceTest {

  @Test
  @DisplayName("should_registerNamedCircuitBreakers_when_registryIsInitialized")
  void should_registerNamedCircuitBreakers_when_registryIsInitialized() {
    var registry = CircuitBreakerRegistry.ofDefaults();
    var config = new ResilienceConfig();

    assertThat(config.ollamaCircuitBreaker(registry).getName()).isEqualTo("ollama");
    assertThat(config.minioCircuitBreaker(registry).getName()).isEqualTo("minio");
    assertThat(config.webhookCircuitBreaker(registry).getName()).isEqualTo("webhook-dispatch");
  }

  @Test
  @DisplayName("should_bindCircuitBreakerMetrics_when_registryHasBreakers")
  void should_bindCircuitBreakerMetrics_when_registryHasBreakers() {
    var registry = CircuitBreakerRegistry.ofDefaults();
    registry.circuitBreaker("ollama");
    registry.circuitBreaker("minio");
    registry.circuitBreaker("webhook-dispatch");

    var meterRegistry = new SimpleMeterRegistry();
    var binder = new ResilienceConfig().circuitBreakerMetricsBinder(registry);

    binder.bindTo(meterRegistry);

    assertThat(meterRegistry.getMeters()).isNotEmpty();
  }

  @Test
  @DisplayName("should_openAndRecoverCircuitBreaker_when_failuresExceedThreshold")
  void should_openAndRecoverCircuitBreaker_when_failuresExceedThreshold() throws Exception {
    CircuitBreakerConfig config = CircuitBreakerConfig.custom()
        .failureRateThreshold(50)
        .slidingWindowSize(4)
        .minimumNumberOfCalls(4)
        .waitDurationInOpenState(Duration.ofMillis(50))
        .permittedNumberOfCallsInHalfOpenState(1)
        .recordExceptions(IOException.class)
        .build();

    CircuitBreaker breaker = CircuitBreaker.of("ollama-test", config);
    Callable<String> failingCall = CircuitBreaker
        .decorateCallable(breaker, () -> {
          throw new IOException("dependency unavailable");
        });

    for (int i = 0; i < 4; i++) {
      try {
        failingCall.call();
      } catch (IOException ignored) {
        // expected
      }
    }

    assertThat(breaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

    Thread.sleep(75);

    Callable<String> successfulCall = CircuitBreaker.decorateCallable(breaker, () -> "fallback-ok");
    assertThat(successfulCall.call()).isEqualTo("fallback-ok");
    assertThat(breaker.getState()).isIn(
        CircuitBreaker.State.HALF_OPEN,
        CircuitBreaker.State.CLOSED);
  }
}
