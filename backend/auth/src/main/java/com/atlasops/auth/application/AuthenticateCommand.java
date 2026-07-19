package com.atlasops.auth.application;

import java.util.Objects;

/**
 * Command object for user authentication.
 *
 * @param email the user's email address
 * @param password the user's raw password
 * @param tenantId the tenant context for the authentication request
 */
public record AuthenticateCommand(String email, String password, String tenantId) {

  public AuthenticateCommand {
    Objects.requireNonNull(email, "email must not be null");
    Objects.requireNonNull(password, "password must not be null");
    Objects.requireNonNull(tenantId, "tenantId must not be null");
  }
}
