package com.atlasops.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.auth.domain.AccountLockout;
import com.atlasops.auth.domain.Role;
import com.atlasops.auth.domain.ports.AccountLockoutPort;
import com.atlasops.auth.domain.ports.AuthUserPort;
import com.atlasops.auth.domain.ports.AuthUserPort.AuthUserData;
import com.atlasops.auth.domain.ports.JwtTokenPort;
import com.atlasops.auth.domain.ports.PasswordHashPort;
import com.atlasops.auth.domain.ports.RefreshTokenRepository;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import java.time.Instant;
import java.util.Optional;
import net.jqwik.api.*;
import org.mockito.ArgumentCaptor;

/**
 * Property-based test for JWT Claims Completeness.
 *
 * <p><b>Validates: Requirements 1.3</b>
 *
 * <p>Property 1: For ANY valid user data (arbitrary userId, tenantId, and role from
 * ADMIN/ANALYST/CLIENT), when authentication succeeds, the JWT token generation is invoked with ALL
 * three claims (userId, tenantId, role) — none are null or missing.
 */
@Tag("Feature: auth, Property 1: JWT Claims Completeness")
class AuthenticateUserUseCasePropertyTest {

  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final String RAW_PASSWORD = "securePassword123";
  private static final String PASSWORD_HASH = "$2a$10$hashedPassword";

  // ─── Property 1: JWT Claims Completeness ────────────────────────────────────

  @Property(tries = 100)
  void should_invokeJwtGenerationWithAllClaims_when_authenticationSucceeds(
      @ForAll("validUserIds") String userId,
      @ForAll("validTenantIds") String tenantId,
      @ForAll("validRoles") Role role) {

    // Arrange — create fresh mocks for each property invocation
    AuthUserPort authUserPort = mock(AuthUserPort.class);
    AccountLockoutPort accountLockoutPort = mock(AccountLockoutPort.class);
    PasswordHashPort passwordHashPort = mock(PasswordHashPort.class);
    JwtTokenPort jwtTokenPort = mock(JwtTokenPort.class);
    RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
    IdGenerator idGenerator = mock(IdGenerator.class);
    Clock clock = mock(Clock.class);

    AuthenticateUserUseCase useCase =
        new AuthenticateUserUseCase(
            authUserPort,
            accountLockoutPort,
            passwordHashPort,
            jwtTokenPort,
            refreshTokenRepository,
            idGenerator,
            clock);

    String email = "user-" + userId + "@test.com";

    AuthUserData userData =
        new AuthUserData(userId, email, PASSWORD_HASH, role.name(), tenantId, true);

    when(clock.now()).thenReturn(NOW);
    when(accountLockoutPort.getAttempts(email)).thenReturn(new AccountLockout(email, 0, null));
    when(authUserPort.findByEmailAndTenantId(email, tenantId)).thenReturn(Optional.of(userData));
    when(passwordHashPort.verify(RAW_PASSWORD, PASSWORD_HASH)).thenReturn(true);
    when(jwtTokenPort.generateAccessToken(anyString(), anyString(), any(Role.class)))
        .thenReturn("generated-jwt-token");
    when(idGenerator.generate()).thenReturn("refresh-token-id");

    var command = new AuthenticateCommand(email, RAW_PASSWORD, tenantId);

    // Act
    useCase.execute(command);

    // Assert — JWT generation is invoked with ALL three claims, none are null
    ArgumentCaptor<String> userIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> tenantIdCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);

    verify(jwtTokenPort)
        .generateAccessToken(
            userIdCaptor.capture(), tenantIdCaptor.capture(), roleCaptor.capture());

    assertThat(userIdCaptor.getValue()).as("JWT userId claim must not be null").isNotNull();
    assertThat(userIdCaptor.getValue())
        .as("JWT userId claim must match the authenticated user")
        .isEqualTo(userId);

    assertThat(tenantIdCaptor.getValue()).as("JWT tenantId claim must not be null").isNotNull();
    assertThat(tenantIdCaptor.getValue())
        .as("JWT tenantId claim must match the user's tenant")
        .isEqualTo(tenantId);

    assertThat(roleCaptor.getValue()).as("JWT role claim must not be null").isNotNull();
    assertThat(roleCaptor.getValue())
        .as("JWT role claim must match the user's role")
        .isEqualTo(role);
  }

  // ─── Generators ──────────────────────────────────────────────────────────────

  @Provide
  Arbitrary<String> validUserIds() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .ofMinLength(5)
        .ofMaxLength(36)
        .map(s -> "user-" + s);
  }

  @Provide
  Arbitrary<String> validTenantIds() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .ofMinLength(5)
        .ofMaxLength(36)
        .map(s -> "tenant-" + s);
  }

  @Provide
  Arbitrary<Role> validRoles() {
    return Arbitraries.of(Role.ADMIN, Role.ANALYST, Role.CLIENT);
  }
}
