package com.atlasops.boot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration for AtlasOps API.
 *
 * <ul>
 *   <li>Stateless session management (no server-side session state)
 *   <li>CSRF disabled for REST API endpoints
 *   <li>Actuator endpoints permitted without authentication
 *   <li>JWT-based authentication (filter placeholder for future implementation)
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(
                        "/actuator/**",
                        "/actuator/health/**",
                        "/actuator/prometheus",
                        "/actuator/info")
                    .permitAll()
                    .anyRequest()
                    .authenticated());

    // TODO: Add JWT authentication filter
    // .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

    return http.build();
  }
}
