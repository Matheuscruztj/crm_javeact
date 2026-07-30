package com.atlasops.boot.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlasops.auth.domain.ports.PasswordHashPort;
import com.atlasops.auth.domain.ports.RateLimiterPort;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class LocalAuthConfigTest {

  private final LocalAuthConfig config = new LocalAuthConfig();

  @Test
  void should_hashAndVerifyPassword_when_localProfileBeansAreUsed() {
    PasswordEncoder passwordEncoder = config.passwordEncoder();
    PasswordHashPort passwordHashPort = config.passwordHashPort(passwordEncoder);

    String hashed = passwordHashPort.hash("secret-password");

    assertThat(hashed).isNotBlank();
    assertThat(passwordHashPort.verify("secret-password", hashed)).isTrue();
    assertThat(passwordHashPort.verify("wrong-password", hashed)).isFalse();
  }

  @Test
  void should_allowAllRequests_when_localRateLimiterIsUsed() {
    RateLimiterPort rateLimiterPort = config.rateLimiterPort();

    assertThat(rateLimiterPort.isAllowed("auth:login", 3, Duration.ofMinutes(1))).isTrue();
    assertThat(rateLimiterPort.remaining("auth:login", 3)).isEqualTo(3);
  }
}
