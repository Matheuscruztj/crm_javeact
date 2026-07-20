package com.atlasops.boot.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * Servlet filter implementing Idempotency-Key header support for POST endpoints.
 *
 * <p>When a request includes an {@code Idempotency-Key} header:
 * <ol>
 *   <li>If the key was seen before within 24h, the cached response is returned.
 *   <li>Otherwise, the request is processed and the response is cached.
 * </ol>
 *
 * <p>Only applies to POST requests on idempotency-eligible paths.
 * Validates: P0.E.1 — Idempotency-Key Header
 */
public class IdempotencyFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(IdempotencyFilter.class);
  public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

  private static final Set<String> ELIGIBLE_PATHS = Set.of(
      "/api/v1/requests",
      "/api/v1/approvals",
      "/api/v1/documents"
  );

  private static final long TTL_SECONDS = 24 * 60 * 60; // 24 hours

  private final StringRedisTemplate redisTemplate;

  public IdempotencyFilter(StringRedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  /** Secondary constructor for compatibility with FilterRegistrationConfig. */
  public IdempotencyFilter(StringRedisTemplate redisTemplate, com.fasterxml.jackson.databind.ObjectMapper ignoredMapper) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);

    if (!HttpMethod.POST.matches(request.getMethod())
        || idempotencyKey == null
        || idempotencyKey.isBlank()
        || !isEligiblePath(request.getRequestURI())) {
      filterChain.doFilter(request, response);
      return;
    }

    String tenantId = request.getHeader("X-Tenant-ID");
    String redisKey = buildRedisKey(idempotencyKey, tenantId);

    // Check for cached response
    String cached = redisTemplate.opsForValue().get(redisKey);
    if (cached != null) {
      log.debug("Idempotency key hit: {}", idempotencyKey);
      response.setStatus(200);
      response.setContentType("application/json");
      response.setHeader("X-Idempotency-Replayed", "true");
      response.getWriter().write(cached);
      return;
    }

    // Process request and cache response
    ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
    filterChain.doFilter(request, responseWrapper);

    int status = responseWrapper.getStatus();
    // Cache only successful responses
    if (status >= 200 && status < 300) {
      String responseBody = new String(responseWrapper.getContentAsByteArray());
      try {
        redisTemplate.opsForValue().set(redisKey, responseBody,
            java.time.Duration.ofSeconds(TTL_SECONDS));
        log.debug("Idempotency key stored: {} (TTL {}s)", idempotencyKey, TTL_SECONDS);
      } catch (Exception e) {
        log.warn("Failed to store idempotency key {}: {}", idempotencyKey, e.getMessage());
      }
    }

    responseWrapper.copyBodyToResponse();
  }

  private boolean isEligiblePath(String uri) {
    return ELIGIBLE_PATHS.stream().anyMatch(uri::startsWith);
  }

  private String buildRedisKey(String idempotencyKey, String tenantId) {
    String tenant = tenantId != null ? tenantId : "global";
    return "idempotency:" + tenant + ":" + idempotencyKey;
  }
}
