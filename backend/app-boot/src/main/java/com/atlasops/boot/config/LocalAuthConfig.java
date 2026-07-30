package com.atlasops.boot.config;

import com.atlasops.auth.domain.ports.PasswordHashPort;
import com.atlasops.auth.domain.ports.RateLimiterPort;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Local auth helpers for Docker startup. */
@Configuration
@Profile("local")
public class LocalAuthConfig {

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  PasswordHashPort passwordHashPort(PasswordEncoder passwordEncoder) {
    return new PasswordHashPort() {
      @Override
      public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
      }

      @Override
      public boolean verify(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
      }
    };
  }

  @Bean
  RateLimiterPort rateLimiterPort() {
    return new RateLimiterPort() {
      @Override
      public boolean isAllowed(String key, int maxRequests, Duration window) {
        return true;
      }

      @Override
      public long remaining(String key, int maxRequests) {
        return maxRequests;
      }
    };
  }
}
