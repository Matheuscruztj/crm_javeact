package com.atlasops.boot.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter that records per-request metrics using Micrometer.
 *
 * <p>For each HTTP request, this filter records:
 *
 * <ul>
 *   <li>{@code atlasops_http_requests_total} — incremented with tags: method, endpoint, status
 *   <li>{@code atlasops_http_request_duration_seconds} — latency with tags: method, endpoint
 *   <li>{@code atlasops_http_errors_total} — incremented for 4xx/5xx responses with tags:
 *       error_class, endpoint, status
 * </ul>
 *
 * <p>The filter runs after the {@link com.atlasops.boot.filter.CorrelationIdFilter} (which has
 * HIGHEST_PRECEDENCE) to ensure correlation context is available.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RequestMetricsFilter extends OncePerRequestFilter {

  private final MeterRegistry meterRegistry;

  public RequestMetricsFilter(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    long startTime = System.nanoTime();

    try {
      filterChain.doFilter(request, response);
    } finally {
      long durationNanos = System.nanoTime() - startTime;
      recordMetrics(request, response, durationNanos);
    }
  }

  /**
   * Determines whether this filter should be skipped for the given request. Actuator endpoints are
   * excluded to avoid recursive metric recording.
   */
  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/actuator");
  }

  private void recordMetrics(
      HttpServletRequest request, HttpServletResponse response, long durationNanos) {
    String method = request.getMethod();
    String endpoint = normalizeEndpoint(request.getRequestURI());
    int statusCode = response.getStatus();
    String status = String.valueOf(statusCode);

    // Record request count
    Counter.builder(MetricsConfiguration.REQUESTS_TOTAL)
        .description("Total HTTP requests")
        .tag("method", method)
        .tag("endpoint", endpoint)
        .tag("status", status)
        .register(meterRegistry)
        .increment();

    // Record request duration
    Timer.builder(MetricsConfiguration.REQUEST_DURATION)
        .description("HTTP request duration in seconds")
        .publishPercentiles(0.5, 0.95, 0.99)
        .publishPercentileHistogram(true)
        .tag("method", method)
        .tag("endpoint", endpoint)
        .register(meterRegistry)
        .record(durationNanos, TimeUnit.NANOSECONDS);

    // Record errors (4xx and 5xx)
    if (statusCode >= 400) {
      String errorClass = statusCode >= 500 ? "5xx" : "4xx";
      Counter.builder(MetricsConfiguration.ERRORS_TOTAL)
          .description("Total HTTP errors")
          .tag("error_class", errorClass)
          .tag("endpoint", endpoint)
          .tag("status", status)
          .register(meterRegistry)
          .increment();
    }
  }

  /**
   * Normalizes the request URI to avoid high-cardinality metric labels.
   *
   * <p>Replaces path segments that look like UUIDs or numeric IDs with placeholders.
   *
   * @param uri the raw request URI
   * @return the normalized endpoint path
   */
  String normalizeEndpoint(String uri) {
    if (uri == null || uri.isBlank()) {
      return "/";
    }
    // Replace UUID-like segments
    String normalized =
        uri.replaceAll(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}", "{id}");
    // Replace purely numeric segments (e.g., /users/123)
    normalized = normalized.replaceAll("/\\d+", "/{id}");
    return normalized;
  }
}
