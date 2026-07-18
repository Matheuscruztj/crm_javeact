package com.atlasops.boot.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter that extracts or generates a Correlation ID for every HTTP request.
 *
 * <p>Behavior:
 *
 * <ul>
 *   <li>If the incoming request contains an {@code X-Correlation-ID} header, that value is reused.
 *   <li>Otherwise, a new UUID v4 is generated.
 *   <li>The correlation ID is placed in the MDC as {@code correlationId} for structured logging.
 *   <li>The correlation ID is also set as {@code traceId} in the MDC if no external tracing is
 *       active.
 *   <li>The correlation ID is added to the response as {@code X-Correlation-ID} header.
 *   <li>The MDC is cleaned after the request completes.
 * </ul>
 *
 * <p>Validates: Requirements 3.11, 11.4, 11.5
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

  public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
  public static final String MDC_CORRELATION_ID_KEY = "correlationId";
  public static final String MDC_TRACE_ID_KEY = "traceId";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    try {
      String correlationId = extractOrGenerate(request);

      MDC.put(MDC_CORRELATION_ID_KEY, correlationId);

      // Set traceId from MDC if not already set by a distributed tracing system
      if (MDC.get(MDC_TRACE_ID_KEY) == null) {
        MDC.put(MDC_TRACE_ID_KEY, correlationId);
      }

      response.setHeader(CORRELATION_ID_HEADER, correlationId);

      filterChain.doFilter(request, response);
    } finally {
      MDC.remove(MDC_CORRELATION_ID_KEY);
      MDC.remove(MDC_TRACE_ID_KEY);
    }
  }

  private String extractOrGenerate(HttpServletRequest request) {
    String headerValue = request.getHeader(CORRELATION_ID_HEADER);
    if (headerValue != null && !headerValue.isBlank()) {
      return headerValue.trim();
    }
    return UUID.randomUUID().toString();
  }
}
