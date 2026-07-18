package com.atlasops.boot.observability;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures custom Micrometer metrics for the AtlasOps API.
 *
 * <p>Registers:
 *
 * <ul>
 *   <li>{@code atlasops_http_requests_total} — request count by endpoint, method, and status
 *   <li>{@code atlasops_http_request_duration_seconds} — latency histogram with p50/p95/p99 buckets
 *   <li>{@code atlasops_http_errors_total} — error count by HTTP code class (4xx, 5xx) and endpoint
 * </ul>
 *
 * <p>These metrics supplement Spring Boot Actuator's default {@code http.server.requests} with
 * AtlasOps-specific naming and error grouping.
 *
 * @see RequestMetricsFilter
 * @see HealthMetrics
 */
@Configuration
public class MetricsConfiguration {

  /** Metric name for total HTTP request count. */
  public static final String REQUESTS_TOTAL = "atlasops_http_requests_total";

  /** Metric name for HTTP request duration histogram. */
  public static final String REQUEST_DURATION = "atlasops_http_request_duration_seconds";

  /** Metric name for HTTP error count. */
  public static final String ERRORS_TOTAL = "atlasops_http_errors_total";

  /**
   * Registers a MeterBinder that ensures the Timer for request duration is pre-configured with
   * percentile histograms (p50, p95, p99).
   */
  @Bean
  public MeterBinder requestDurationMeterBinder() {
    return registry ->
        Timer.builder(REQUEST_DURATION)
            .description("HTTP request duration in seconds")
            .publishPercentiles(0.5, 0.95, 0.99)
            .publishPercentileHistogram(true)
            .register(registry);
  }
}
