package com.atlasops.boot.config;

import com.atlasops.auth.application.ValidateTokenUseCase;
import com.atlasops.auth.presentation.JwtAuthenticationFilter;
import com.atlasops.boot.filter.IdempotencyFilter;
import com.atlasops.boot.filter.TenantAuthorizationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security configuration for AtlasOps API.
 *
 * <ul>
 *   <li>Stateless session management (no server-side session state)
 *   <li>CSRF disabled for REST API endpoints
 *   <li>Actuator endpoints permitted without authentication
 *   <li>JWT-based authentication via {@link JwtAuthenticationFilter}
 *   <li>CORS configuration allowing frontend origins
 * </ul>
 *
 * <p>Validates: Requirements 1.9, 2.7 (P0.H.1, P0.K.1)
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Value("${app.cors.allowed-origins:http://localhost:3000}")
  private String allowedOrigins;

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JwtAuthenticationFilter jwtAuthenticationFilter,
      ObjectMapper objectMapper) throws Exception {

    http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(
                        "/actuator/**",
                        "/actuator/health/**",
                        "/actuator/prometheus",
                        "/actuator/info",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/api/v1/auth/login",
                        "/api/v1/auth/refresh")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(tenantAuthorizationFilter(objectMapper), JwtAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public TenantAuthorizationFilter tenantAuthorizationFilter(ObjectMapper objectMapper) {
    return new TenantAuthorizationFilter(objectMapper);
  }

  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter(
      ValidateTokenUseCase validateTokenUseCase, ObjectMapper objectMapper) {
    return new JwtAuthenticationFilter(validateTokenUseCase, objectMapper);
  }

  /**
   * Idempotency filter registration (P0.E.1).
   * Runs before security filters to allow cached response replay.
   */
  @Bean
  public FilterRegistrationBean<IdempotencyFilter> idempotencyFilterRegistration(
      StringRedisTemplate redisTemplate) {
    FilterRegistrationBean<IdempotencyFilter> reg = new FilterRegistrationBean<>();
    reg.setFilter(new IdempotencyFilter(redisTemplate));
    reg.addUrlPatterns("/api/v1/requests/*", "/api/v1/approvals/*", "/api/v1/documents/*");
    reg.setOrder(10);
    reg.setName("idempotencyFilter");
    return reg;
  }

  /**
   * CORS configuration allowing frontend origins (P0.K.1).
   * Allows localhost:3000 in dev; configurable via {@code app.cors.allowed-origins} in production.
   */
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    List<String> origins = List.of(allowedOrigins.split(","));
    config.setAllowedOrigins(origins);
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of(
        "Authorization",
        "Content-Type",
        "X-Tenant-ID",
        "X-User-ID",
        "X-Correlation-ID",
        "Idempotency-Key",
        "Last-Event-ID"));
    config.setExposedHeaders(List.of(
        "X-Correlation-ID",
        "X-API-Deprecated",
        "Location"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
