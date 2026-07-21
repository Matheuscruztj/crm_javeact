package com.atlasops.auth.presentation;

import com.atlasops.auth.domain.ports.RateLimiterPort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * HTTP filter that enforces rate limiting on sensitive endpoints.
 *
 * <p>Rate limits:
 * <ul>
 *   <li>Login endpoint (/api/v1/auth/login): 10 requests per minute per IP</li>
 *   <li>Refresh endpoint (/api/v1/auth/refresh): 20 requests per minute per IP</li>
 *   <li>General API: 100 requests per minute per IP</li>
 * </ul>
 *
 * <p>Returns HTTP 429 with standard error body when limit is exceeded.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter extends OncePerRequestFilter {

  private static final int LOGIN_MAX_REQUESTS = 10;
  private static final int REFRESH_MAX_REQUESTS = 20;
  private static final int GENERAL_MAX_REQUESTS = 100;
  private static final Duration WINDOW = Duration.ofMinutes(1);

  private final RateLimiterPort rateLimiter;

  public RateLimitFilter(RateLimiterPort rateLimiter) {
    this.rateLimiter = rateLimiter;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String clientIp = resolveClientIp(request);
    String path = request.getRequestURI();

    int maxRequests = resolveMaxRequests(path);
    String rateLimitKey = path + ":" + clientIp;

    if (!rateLimiter.isAllowed(rateLimitKey, maxRequests, WINDOW)) {
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.getWriter().write(buildErrorResponse(clientIp));
      return;
    }

    long remaining = rateLimiter.remaining(rateLimitKey, maxRequests);
    response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequests));
    response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));

    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    // Only rate limit API endpoints
    return !path.startsWith("/api/");
  }

  private int resolveMaxRequests(String path) {
    if (path.contains("/auth/login")) {
      return LOGIN_MAX_REQUESTS;
    }
    if (path.contains("/auth/refresh")) {
      return REFRESH_MAX_REQUESTS;
    }
    return GENERAL_MAX_REQUESTS;
  }

  private String resolveClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isBlank()) {
      return xForwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }

  private String buildErrorResponse(String clientIp) {
    return """
        {
          "type": "https://atlasops/errors/rate-limit-exceeded",
          "title": "Too Many Requests",
          "status": 429,
          "code": "RATE_LIMIT_EXCEEDED",
          "detail": "Rate limit exceeded. Please retry after a short wait.",
          "traceId": "%s"
        }
        """
        .formatted(clientIp);
  }
}
