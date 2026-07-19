package com.atlasops.boot.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Servlet filter that clears the SLF4J MDC after request completion.
 *
 * <p>This filter runs before {@link CorrelationIdFilter} (higher priority) and wraps the entire
 * filter chain in a try/finally block to ensure the MDC is always cleared, even when exceptions
 * occur. This prevents correlation IDs and other MDC values from leaking between requests on the
 * same thread (thread pool reuse).
 *
 * <p>Validates: Requirements 27.6
 */
public class MdcCleanupFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    try {
      filterChain.doFilter(request, response);
    } finally {
      MDC.clear();
    }
  }
}
