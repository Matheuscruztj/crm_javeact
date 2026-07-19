package com.atlasops.boot.filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Configuration for registering servlet filters with explicit ordering.
 *
 * <p>Filter execution order (lowest order runs first):
 *
 * <ol>
 *   <li>{@link MdcCleanupFilter} — Wraps everything in try/finally to clear MDC
 *   <li>{@link CorrelationIdFilter} — Extracts/generates correlation ID and sets MDC
 * </ol>
 *
 * <p>The MdcCleanupFilter runs first so its finally block executes last, ensuring MDC is always
 * cleaned regardless of what happens in inner filters.
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
}
