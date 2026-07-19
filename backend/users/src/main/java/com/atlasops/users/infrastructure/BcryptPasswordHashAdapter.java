package com.atlasops.users.infrastructure;

import com.atlasops.users.domain.ports.PasswordHashPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Bcrypt-based implementation of the PasswordHashPort. Uses a cost factor of 10 (minimum required
 * by requirements).
 */
@Component
public class BcryptPasswordHashAdapter implements PasswordHashPort {

  private static final int BCRYPT_COST = 10;

  private final BCryptPasswordEncoder encoder;

  public BcryptPasswordHashAdapter() {
    this.encoder = new BCryptPasswordEncoder(BCRYPT_COST);
  }

  @Override
  public String hash(String rawPassword) {
    if (rawPassword == null || rawPassword.isEmpty()) {
      throw new IllegalArgumentException("Raw password must not be null or empty");
    }
    return encoder.encode(rawPassword);
  }

  @Override
  public boolean verify(String rawPassword, String hashedPassword) {
    if (rawPassword == null || hashedPassword == null) {
      return false;
    }
    return encoder.matches(rawPassword, hashedPassword);
  }
}
