package com.atlasops.boot.observability;

import io.micrometer.core.instrument.Counter;
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
 *   <li>{@code atlasops_http_request_duration_seconds} — latency histogram with p50/p95/p99
 *   <li>{@code atlasops_http_errors_total} — error count by HTTP code class and endpoint
 *   <li>{@code ai_analysis_duration_seconds} — AI document analysis duration histogram (P0.E.3)
 *   <li>{@code ai_fallback_total} — AI fallback counter when Ollama is unavailable (P0.E.3)
 *   <li>{@code document_processing_duration_seconds} — document processing duration (P0.E.3)
 * </ul>
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

  /** Metric name for AI analysis duration (P0.E.3). */
  public static final String AI_ANALYSIS_DURATION = "ai_analysis_duration_seconds";

  /** Metric name for AI fallback activations (P0.E.3). */
  public static final String AI_FALLBACK_TOTAL = "ai_fallback_total";

  /** Metric name for document processing duration (P0.E.3). */
  public static final String DOCUMENT_PROCESSING_DURATION = "document_processing_duration_seconds";

  /**
   * Registers a MeterBinder that pre-configures all timers with percentile histograms.
   * (P0.E.3 — Processing Metrics)
   */
  @Bean
  public MeterBinder requestDurationMeterBinder() {
    return registry -> {
      // HTTP request duration
      Timer.builder(REQUEST_DURATION)
          .description("HTTP request duration in seconds")
          .publishPercentiles(0.5, 0.95, 0.99)
          .publishPercentileHistogram(true)
          .register(registry);

      // AI analysis duration histogram (P0.E.3.1)
      Timer.builder(AI_ANALYSIS_DURATION)
          .description("Duration of AI document analysis in seconds")
          .publishPercentiles(0.5, 0.95, 0.99)
          .publishPercentileHistogram(true)
          .tag("model", "unknown")
          .register(registry);

      // Document processing duration histogram (P0.E.3.3)
      Timer.builder(DOCUMENT_PROCESSING_DURATION)
          .description("Duration of document text extraction and processing in seconds")
          .publishPercentiles(0.5, 0.95, 0.99)
          .publishPercentileHistogram(true)
          .register(registry);

      // AI fallback counter (P0.E.3.2)
      Counter.builder(AI_FALLBACK_TOTAL)
          .description("Number of times AI analysis fell back to default response")
          .tag("reason", "ollama_unavailable")
          .register(registry);
    };
  }
}
