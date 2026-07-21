package com.atlasops.boot.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Resilience4j circuit breaker configuration and metrics registration.
 *
 * <p>Circuit breakers are declared in application.yml and this class registers their
 * Micrometer metrics for Prometheus/Grafana visibility.
 *
 * <p>Validates: P0.J.2 — Circuit Breaker (Resilience4j)
 *
 * <p>Circuit breakers configured:
 * <ul>
 *   <li>{@code ollama} — 3 failures → OPEN, 30s half-open, 50% threshold
 *   <li>{@code minio} — 5 failures → OPEN, 20s half-open, 60% threshold
 *   <li>{@code webhook-dispatch} — 10 failures → OPEN, 15s half-open, 70% threshold
 * </ul>
 */
@Configuration
public class ResilienceConfig {

  private static final Logger log = LoggerFactory.getLogger(ResilienceConfig.class);

  /**
   * Registers circuit breaker state metrics with Micrometer for Prometheus scraping.
   * Metrics: resilience4j_circuitbreaker_state, resilience4j_circuitbreaker_calls_total, etc.
   */
  @Bean
  public MeterBinder circuitBreakerMetricsBinder(CircuitBreakerRegistry registry) {
    return meterRegistry -> {
      for (CircuitBreaker cb : registry.getAllCircuitBreakers()) {
        log.info("Registering circuit breaker metrics for: {}", cb.getName());
        io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics
            .ofCircuitBreakerRegistry(registry)
            .bindTo(meterRegistry);
        // Only need to bind once for all circuit breakers
        break;
      }
    };
  }

  /**
   * Eager initialization of named circuit breakers to pre-register metrics on startup.
   */
  @Bean
  public CircuitBreaker ollamaCircuitBreaker(CircuitBreakerRegistry registry) {
    CircuitBreaker cb = registry.circuitBreaker("ollama");
    cb.getEventPublisher().onStateTransition(event ->
        log.info("Ollama circuit breaker state: {} → {}",
            event.getStateTransition().getFromState(),
            event.getStateTransition().getToState()));
    return cb;
  }

  @Bean
  public CircuitBreaker minioCircuitBreaker(CircuitBreakerRegistry registry) {
    CircuitBreaker cb = registry.circuitBreaker("minio");
    cb.getEventPublisher().onStateTransition(event ->
        log.info("MinIO circuit breaker state: {} → {}",
            event.getStateTransition().getFromState(),
            event.getStateTransition().getToState()));
    return cb;
  }

  @Bean
  public CircuitBreaker webhookCircuitBreaker(CircuitBreakerRegistry registry) {
    return registry.circuitBreaker("webhook-dispatch");
  }
}
