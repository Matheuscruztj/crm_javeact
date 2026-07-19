package com.atlasops.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.auth.domain.AccountLockout;
import com.atlasops.auth.domain.AuthenticationResult;
import com.atlasops.auth.domain.RefreshToken;
import com.atlasops.auth.domain.Role;
import com.atlasops.auth.domain.ports.AccountLockoutPort;
import com.atlasops.auth.domain.ports.AuthUserPort;
import com.atlasops.auth.domain.ports.AuthUserPort.AuthUserData;
import com.atlasops.auth.domain.ports.JwtTokenPort;
import com.atlasops.auth.domain.ports.PasswordHashPort;
import com.atlasops.auth.domain.ports.RefreshTokenRepository;
import com.atlasops.shared.domain.exceptions.TooManyRequestsException;
import com.atlasops.shared.domain.exceptions.UnauthorizedException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticateUserUseCase")
class AuthenticateUserUseCaseTest {

  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final Instant LOCKED_UNTIL_FUTURE = Instant.parse("2025-01-15T10:15:00Z");
  private static final String TENANT_ID = "tenant-alpha";
  private static final String USER_ID = "user-001";
  private static final String USER_EMAIL = "user@atlasops.test";
  private static final String RAW_PASSWORD = "securePassword123";
  private static final String PASSWORD_HASH = "$2a$10$hashedPassword";
  private static final String ACCESS_TOKEN = "jwt-access-token";
  private static final String REFRESH_TOKEN_ID = "rt-001";

  @Mock private AuthUserPort authUserPort;
  @Mock private AccountLockoutPort accountLockoutPort;
  @Mock private PasswordHashPort passwordHashPort;
  @Mock private JwtTokenPort jwtTokenPort;
  @Mock private RefreshTokenRepository refreshTokenRepository;
  @Mock private IdGenerator idGenerator;
  @Mock private Clock clock;

  private AuthenticateUserUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase =
        new AuthenticateUserUseCase(
            authUserPort,
            accountLockoutPort,
            passwordHashPort,
            jwtTokenPort,
            refreshTokenRepository,
            idGenerator,
            clock);
  }

  @Test
  void should_returnAuthenticationResult_when_credentialsAreValid() {
    // Arrange
    var command = new AuthenticateCommand(USER_EMAIL, RAW_PASSWORD, TENANT_ID);

    when(clock.now()).thenReturn(NOW);
    when(accountLockoutPort.getAttempts(USER_EMAIL))
        .thenReturn(new AccountLockout(USER_EMAIL, 0, null));
    when(authUserPort.findByEmailAndTenantId(USER_EMAIL, TENANT_ID))
        .thenReturn(Optional.of(activeUserData()));
    when(passwordHashPort.verify(RAW_PASSWORD, PASSWORD_HASH)).thenReturn(true);
    when(jwtTokenPort.generateAccessToken(USER_ID, TENANT_ID, Role.ANALYST))
        .thenReturn(ACCESS_TOKEN);
    when(idGenerator.generate()).thenReturn(REFRESH_TOKEN_ID);

    // Act
    AuthenticationResult result = useCase.execute(command);

    // Assert
    assertThat(result.accessToken()).isEqualTo(ACCESS_TOKEN);
    assertThat(result.refreshToken()).isNotBlank();
    assertThat(result.expiresIn()).isEqualTo(900L);
    assertThat(result.tokenType()).isEqualTo("Bearer");
  }

  @Test
  void should_resetFailedAttempts_when_authenticationSucceeds() {
    // Arrange
    var command = new AuthenticateCommand(USER_EMAIL, RAW_PASSWORD, TENANT_ID);

    when(clock.now()).thenReturn(NOW);
    when(accountLockoutPort.getAttempts(USER_EMAIL))
        .thenReturn(new AccountLockout(USER_EMAIL, 3, null));
    when(authUserPort.findByEmailAndTenantId(USER_EMAIL, TENANT_ID))
        .thenReturn(Optional.of(activeUserData()));
    when(passwordHashPort.verify(RAW_PASSWORD, PASSWORD_HASH)).thenReturn(true);
    when(jwtTokenPort.generateAccessToken(USER_ID, TENANT_ID, Role.ANALYST))
        .thenReturn(ACCESS_TOKEN);
    when(idGenerator.generate()).thenReturn(REFRESH_TOKEN_ID);

    // Act
    useCase.execute(command);

    // Assert
    verify(accountLockoutPort).resetFailedAttempts(USER_EMAIL);
  }

  @Test
  void should_saveRefreshToken_when_authenticationSucceeds() {
    // Arrange
    var command = new AuthenticateCommand(USER_EMAIL, RAW_PASSWORD, TENANT_ID);

    when(clock.now()).thenReturn(NOW);
    when(accountLockoutPort.getAttempts(USER_EMAIL))
        .thenReturn(new AccountLockout(USER_EMAIL, 0, null));
    when(authUserPort.findByEmailAndTenantId(USER_EMAIL, TENANT_ID))
        .thenReturn(Optional.of(activeUserData()));
    when(passwordHashPort.verify(RAW_PASSWORD, PASSWORD_HASH)).thenReturn(true);
    when(jwtTokenPort.generateAccessToken(USER_ID, TENANT_ID, Role.ANALYST))
        .thenReturn(ACCESS_TOKEN);
    when(idGenerator.generate()).thenReturn(REFRESH_TOKEN_ID);

    // Act
    useCase.execute(command);

    // Assert
    ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
    verify(refreshTokenRepository).save(captor.capture());

    RefreshToken saved = captor.getValue();
    assertThat(saved.getId()).isEqualTo(REFRESH_TOKEN_ID);
    assertThat(saved.getUserId()).isEqualTo(USER_ID);
    assertThat(saved.getTenantId()).isEqualTo(TENANT_ID);
    assertThat(saved.isRevoked()).isFalse();
    assertThat(saved.getCreatedAt()).isEqualTo(NOW);
  }

  @Test
  void should_throwUnauthorized_when_emailNotFound() {
    // Arrange
    var command = new AuthenticateCommand("nonexistent@test.com", RAW_PASSWORD, TENANT_ID);

    when(clock.now()).thenReturn(NOW);
    when(accountLockoutPort.getAttempts("nonexistent@test.com"))
        .thenReturn(new AccountLockout("nonexistent@test.com", 0, null))
        .thenReturn(new AccountLockout("nonexistent@test.com", 1, null));
    when(authUserPort.findByEmailAndTenantId("nonexistent@test.com", TENANT_ID))
        .thenReturn(Optional.empty());

    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessage("Invalid email or password");

    verify(accountLockoutPort).incrementFailedAttempts("nonexistent@test.com");
  }

  @Test
  void should_throwUnauthorized_when_passwordIsWrong() {
    // Arrange
    var command = new AuthenticateCommand(USER_EMAIL, "wrongPassword", TENANT_ID);

    when(clock.now()).thenReturn(NOW);
    when(accountLockoutPort.getAttempts(USER_EMAIL))
        .thenReturn(new AccountLockout(USER_EMAIL, 0, null))
        .thenReturn(new AccountLockout(USER_EMAIL, 1, null));
    when(authUserPort.findByEmailAndTenantId(USER_EMAIL, TENANT_ID))
        .thenReturn(Optional.of(activeUserData()));
    when(passwordHashPort.verify("wrongPassword", PASSWORD_HASH)).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessage("Invalid email or password");

    verify(accountLockoutPort).incrementFailedAttempts(USER_EMAIL);
  }

  @Test
  void should_throwUnauthorized_when_userIsInactive() {
    // Arrange
    var command = new AuthenticateCommand(USER_EMAIL, RAW_PASSWORD, TENANT_ID);

    when(clock.now()).thenReturn(NOW);
    when(accountLockoutPort.getAttempts(USER_EMAIL))
        .thenReturn(new AccountLockout(USER_EMAIL, 0, null))
        .thenReturn(new AccountLockout(USER_EMAIL, 1, null));
    when(authUserPort.findByEmailAndTenantId(USER_EMAIL, TENANT_ID))
        .thenReturn(Optional.of(inactiveUserData()));

    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(UnauthorizedException.class)
        .hasMessage("Invalid email or password");

    verify(accountLockoutPort).incrementFailedAttempts(USER_EMAIL);
    verify(passwordHashPort, never()).verify(anyString(), anyString());
  }

  @Test
  void should_throwTooManyRequests_when_accountIsLocked() {
    // Arrange
    var command = new AuthenticateCommand(USER_EMAIL, RAW_PASSWORD, TENANT_ID);

    when(clock.now()).thenReturn(NOW);
    when(accountLockoutPort.getAttempts(USER_EMAIL))
        .thenReturn(new AccountLockout(USER_EMAIL, 5, LOCKED_UNTIL_FUTURE));

    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(command))
        .isInstanceOf(TooManyRequestsException.class)
        .hasMessageContaining("Account temporarily locked");

    verify(authUserPort, never()).findByEmailAndTenantId(anyString(), anyString());
    verify(passwordHashPort, never()).verify(anyString(), anyString());
  }

  @Test
  void should_lockAccount_when_failedAttemptsReachThreshold() {
    // Arrange
    var command = new AuthenticateCommand(USER_EMAIL, "wrongPassword", TENANT_ID);

    when(clock.now()).thenReturn(NOW);
    when(accountLockoutPort.getAttempts(USER_EMAIL))
        .thenReturn(new AccountLockout(USER_EMAIL, 4, null)) // first call: check if locked
        .thenReturn(new AccountLockout(USER_EMAIL, 5, null)); // second call: after increment
    when(authUserPort.findByEmailAndTenantId(USER_EMAIL, TENANT_ID))
        .thenReturn(Optional.of(activeUserData()));
    when(passwordHashPort.verify("wrongPassword", PASSWORD_HASH)).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(UnauthorizedException.class);

    verify(accountLockoutPort).incrementFailedAttempts(USER_EMAIL);
    verify(accountLockoutPort).lockAccount(USER_EMAIL, NOW.plusSeconds(900));
  }

  @Test
  void should_notLockAccount_when_failedAttemptsBelowThreshold() {
    // Arrange
    var command = new AuthenticateCommand(USER_EMAIL, "wrongPassword", TENANT_ID);

    when(clock.now()).thenReturn(NOW);
    when(accountLockoutPort.getAttempts(USER_EMAIL))
        .thenReturn(new AccountLockout(USER_EMAIL, 2, null)) // first call
        .thenReturn(new AccountLockout(USER_EMAIL, 3, null)); // after increment
    when(authUserPort.findByEmailAndTenantId(USER_EMAIL, TENANT_ID))
        .thenReturn(Optional.of(activeUserData()));
    when(passwordHashPort.verify("wrongPassword", PASSWORD_HASH)).thenReturn(false);

    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(UnauthorizedException.class);

    verify(accountLockoutPort).incrementFailedAttempts(USER_EMAIL);
    verify(accountLockoutPort, never()).lockAccount(anyString(), any(Instant.class));
  }

  @Test
  void should_returnGenericError_when_emailNotFoundSameAsWrongPassword() {
    // Verifies Requirement 1.2: generic message for both wrong email and wrong password

    // Wrong email scenario
    var wrongEmailCommand = new AuthenticateCommand("wrong@test.com", RAW_PASSWORD, TENANT_ID);
    when(clock.now()).thenReturn(NOW);
    when(accountLockoutPort.getAttempts("wrong@test.com"))
        .thenReturn(new AccountLockout("wrong@test.com", 0, null))
        .thenReturn(new AccountLockout("wrong@test.com", 1, null));
    when(authUserPort.findByEmailAndTenantId("wrong@test.com", TENANT_ID))
        .thenReturn(Optional.empty());

    String emailNotFoundMessage = null;
    try {
      useCase.execute(wrongEmailCommand);
    } catch (UnauthorizedException e) {
      emailNotFoundMessage = e.getMessage();
    }

    // Wrong password scenario
    var wrongPasswordCommand = new AuthenticateCommand(USER_EMAIL, "wrongPass", TENANT_ID);
    when(accountLockoutPort.getAttempts(USER_EMAIL))
        .thenReturn(new AccountLockout(USER_EMAIL, 0, null))
        .thenReturn(new AccountLockout(USER_EMAIL, 1, null));
    when(authUserPort.findByEmailAndTenantId(USER_EMAIL, TENANT_ID))
        .thenReturn(Optional.of(activeUserData()));
    when(passwordHashPort.verify("wrongPass", PASSWORD_HASH)).thenReturn(false);

    String wrongPasswordMessage = null;
    try {
      useCase.execute(wrongPasswordCommand);
    } catch (UnauthorizedException e) {
      wrongPasswordMessage = e.getMessage();
    }

    // Both should have the exact same generic message
    assertThat(emailNotFoundMessage).isEqualTo(wrongPasswordMessage);
    assertThat(emailNotFoundMessage).isEqualTo("Invalid email or password");
  }

  @Test
  void should_includeCorrectClaimsInJwt_when_authenticationSucceeds() {
    // Verifies Requirement 1.3: JWT includes userId, tenantId, and role
    var command = new AuthenticateCommand(USER_EMAIL, RAW_PASSWORD, TENANT_ID);

    when(clock.now()).thenReturn(NOW);
    when(accountLockoutPort.getAttempts(USER_EMAIL))
        .thenReturn(new AccountLockout(USER_EMAIL, 0, null));
    when(authUserPort.findByEmailAndTenantId(USER_EMAIL, TENANT_ID))
        .thenReturn(Optional.of(activeUserData()));
    when(passwordHashPort.verify(RAW_PASSWORD, PASSWORD_HASH)).thenReturn(true);
    when(jwtTokenPort.generateAccessToken(USER_ID, TENANT_ID, Role.ANALYST))
        .thenReturn(ACCESS_TOKEN);
    when(idGenerator.generate()).thenReturn(REFRESH_TOKEN_ID);

    // Act
    useCase.execute(command);

    // Assert — verifies that JWT is generated with correct user, tenant, and role
    verify(jwtTokenPort).generateAccessToken(USER_ID, TENANT_ID, Role.ANALYST);
  }

  @Test
  void should_throwNullPointerException_when_commandIsNull() {
    assertThatThrownBy(() -> useCase.execute(null)).isInstanceOf(NullPointerException.class);
  }

  // --- Helper methods ---

  private AuthUserData activeUserData() {
    return new AuthUserData(USER_ID, USER_EMAIL, PASSWORD_HASH, "ANALYST", TENANT_ID, true);
  }

  private AuthUserData inactiveUserData() {
    return new AuthUserData(USER_ID, USER_EMAIL, PASSWORD_HASH, "ANALYST", TENANT_ID, false);
  }
}
