package com.atlasops.boot.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter that extracts or generates a Correlation ID for every HTTP request.
 *
 * <p>Behavior:
 *
 * <ul>
 *   <li>If the incoming request contains an {@code X-Correlation-ID} header with a valid UUID
 *       value, that value is reused.
 *   <li>If the header is absent, blank, or not a valid UUID format, a new UUID v4 is generated.
 *   <li>The correlation ID is placed in the MDC as {@code correlationId} for structured logging.
 *   <li>The correlation ID is added to the response as {@code X-Correlation-ID} header.
 * </ul>
 *
 * <p>Validates: Requirements 27.1, 27.2, 27.3, 27.5
 */
public class CorrelationIdFilter extends OncePerRequestFilter {

  public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
  public static final String MDC_CORRELATION_ID_KEY = "correlationId";

  private static final Pattern UUID_PATTERN =
      Pattern.compile(
          "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String correlationId = extractOrGenerate(request);

    MDC.put(MDC_CORRELATION_ID_KEY, correlationId);
    response.setHeader(CORRELATION_ID_HEADER, correlationId);

    filterChain.doFilter(request, response);
  }

  private String extractOrGenerate(HttpServletRequest request) {
    String headerValue = request.getHeader(CORRELATION_ID_HEADER);
    if (headerValue != null && !headerValue.isBlank()) {
      String trimmed = headerValue.trim();
      if (UUID_PATTERN.matcher(trimmed).matches()) {
        return trimmed;
      }
    }
    return UUID.randomUUID().toString();
  }
}
