package com.atlasops.users.domain.ports;

/** Port defining password hashing and verification operations. */
public interface PasswordHashPort {

  /**
   * Hashes a raw password using a secure algorithm (bcrypt with cost >= 10).
   *
   * @param rawPassword the plain-text password
   * @return the hashed password
   */
  String hash(String rawPassword);

  /**
   * Verifies a raw password against a hashed password.
   *
   * @param rawPassword the plain-text password to verify
   * @param hashedPassword the stored hashed password
   * @return true if the raw password matches the hashed password
   */
  boolean verify(String rawPassword, String hashedPassword);
}
