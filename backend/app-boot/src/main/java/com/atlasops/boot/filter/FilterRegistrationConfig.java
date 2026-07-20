package com.atlasops.boot.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Configuration for registering servlet filters with explicit ordering.
 *
 * <p>Filter execution order (lowest order runs first):
 *
 * <ol>
 *   <li>{@link MdcCleanupFilter} — Wraps everything in try/finally to clear MDC
 *   <li>{@link CorrelationIdFilter} — Extracts/generates correlation ID and sets MDC
 *   <li>{@link IdempotencyFilter} — Deduplicates POST requests via Idempotency-Key header
 * </ol>
 */
@Configuration
public class FilterRegistrationConfig {

  @Bean
  public FilterRegistrationBean<MdcCleanupFilter> mdcCleanupFilterRegistration() {
    FilterRegistrationBean<MdcCleanupFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new MdcCleanupFilter());
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
    registration.setName("mdcCleanupFilter");
    registration.addUrlPatterns("/*");
    return registration;
  }

  @Bean
  public FilterRegistrationBean<CorrelationIdFilter> correlationIdFilterRegistration() {
    FilterRegistrationBean<CorrelationIdFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new CorrelationIdFilter());
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
    registration.setName("correlationIdFilter");
    registration.addUrlPatterns("/*");
    return registration;
  }

  /**
   * Idempotency filter for deduplicating POST requests using Idempotency-Key header (P0.E.1).
   */
  @Bean
  public FilterRegistrationBean<IdempotencyFilter> idempotencyFilterRegistration(
      StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
    FilterRegistrationBean<IdempotencyFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new IdempotencyFilter(redisTemplate, objectMapper));
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
    registration.setName("idempotencyFilter");
    registration.addUrlPatterns("/api/*");
    return registration;
  }

  /**
   * ETag filter for GET responses and conditional request support (P0.Q.2).
   */
  @Bean
  public FilterRegistrationBean<ETagFilter> eTagFilterRegistration() {
    FilterRegistrationBean<ETagFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new ETagFilter());
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
    registration.setName("eTagFilter");
    registration.addUrlPatterns("/api/*");
    return registration;
  }

  /**
   * Maintenance mode filter — returns 503 for mutating operations when tenant is in maintenance.
   * Validates: P2.11 — Tenant read-only maintenance mode
   */
  @Bean
  public FilterRegistrationBean<MaintenanceModeFilter> maintenanceModeFilterRegistration(
      StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
    FilterRegistrationBean<MaintenanceModeFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new MaintenanceModeFilter(redisTemplate, objectMapper));
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 30);
    registration.setName("maintenanceModeFilter");
    registration.addUrlPatterns("/api/*");
    return registration;
  }
}
