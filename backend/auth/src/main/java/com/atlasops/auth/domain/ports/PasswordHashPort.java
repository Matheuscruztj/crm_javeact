package com.atlasops.auth.domain.ports;

/** Port for password hashing and verification (bcrypt). */
public interface PasswordHashPort {

  String hash(String rawPassword);

  boolean verify(String rawPassword, String hashedPassword);
}
