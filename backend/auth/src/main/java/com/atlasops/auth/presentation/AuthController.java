package com.atlasops.auth.presentation;

import com.atlasops.auth.application.AuthenticateCommand;
import com.atlasops.auth.application.AuthenticateUserUseCase;
import com.atlasops.auth.application.LogoutUseCase;
import com.atlasops.auth.application.RefreshTokenUseCase;
import com.atlasops.auth.application.RevokeAllSessionsUseCase;
import com.atlasops.auth.domain.AuthenticationResult;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for authentication operations.
 *
 * <p>Endpoints:
 *
 * <ul>
 *   <li>POST /api/v1/auth/login — authenticates user, returns JWT + refresh token
 *   <li>POST /api/v1/auth/refresh — rotates refresh token, returns new JWT + refresh token
 *   <li>POST /api/v1/auth/logout — invalidates refresh token
 * </ul>
 *
 * <p>Validates: Requirements 1.1, 1.2, 1.4, 1.6, 1.9, 1.10
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthenticateUserUseCase authenticateUserUseCase;
  private final RefreshTokenUseCase refreshTokenUseCase;
  private final LogoutUseCase logoutUseCase;
  private final RevokeAllSessionsUseCase revokeAllSessionsUseCase;

  public AuthController(
      AuthenticateUserUseCase authenticateUserUseCase,
      RefreshTokenUseCase refreshTokenUseCase,
      LogoutUseCase logoutUseCase,
      RevokeAllSessionsUseCase revokeAllSessionsUseCase) {
    this.authenticateUserUseCase = authenticateUserUseCase;
    this.refreshTokenUseCase = refreshTokenUseCase;
    this.logoutUseCase = logoutUseCase;
    this.revokeAllSessionsUseCase = revokeAllSessionsUseCase;
  }

  /**
   * Authenticates a user with email and password credentials.
   *
   * @param request the login request containing email and password
   * @param tenantId the tenant context from the X-Tenant-ID header
   * @return 200 OK with the access token, refresh token, and expiration metadata
   */
  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(
      @Valid @RequestBody LoginRequest request, @RequestHeader("X-Tenant-ID") String tenantId) {

    AuthenticateCommand command =
        new AuthenticateCommand(request.email(), request.password(), tenantId);

    AuthenticationResult result = authenticateUserUseCase.execute(command);
    return ResponseEntity.ok(TokenResponse.from(result));
  }

  /**
   * Rotates a refresh token, issuing a new access token and refresh token.
   *
   * @param request the refresh request containing the current refresh token
   * @return 200 OK with the new access token, refresh token, and expiration metadata
   */
  @PostMapping("/refresh")
  public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
    AuthenticationResult result = refreshTokenUseCase.execute(request.refreshToken());
    return ResponseEntity.ok(TokenResponse.from(result));
  }

  /**
   * Logs out a user by invalidating their refresh token.
   *
   * @param request the logout request containing the refresh token to invalidate
   * @return 204 No Content on successful logout
   */
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
    logoutUseCase.execute(request.refreshToken());
    return ResponseEntity.noContent().build();
  }

  /**
   * Revokes all active sessions for the authenticated user.
   * All refresh tokens are invalidated, requiring re-authentication on all devices.
   *
   * @param userId the authenticated user's ID (extracted from JWT by security filter)
   * @return 204 No Content on successful revocation
   */
  @PostMapping("/revoke-all-sessions")
  public ResponseEntity<Void> revokeAllSessions(@RequestHeader("X-User-ID") String userId) {
    revokeAllSessionsUseCase.execute(userId);
    return ResponseEntity.noContent().build();
  }
}
