package com.atlasops.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.atlasops.auth.domain.AccountLockout;
import com.atlasops.auth.domain.ports.AccountLockoutPort;
import com.atlasops.auth.domain.ports.AuthUserPort;
import com.atlasops.auth.domain.ports.AuthUserPort.AuthUserData;
import com.atlasops.auth.domain.ports.JwtTokenPort;
import com.atlasops.auth.domain.ports.PasswordHashPort;
import com.atlasops.auth.domain.ports.RefreshTokenRepository;
import com.atlasops.shared.domain.exceptions.UnauthorizedException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import java.time.Instant;
import java.util.Optional;
import net.jqwik.api.*;

/**
 * Property-based tests for invalid credentials generic error behavior.
 *
 * <p><b>Validates: Requirements 1.2</b>
 *
 * <p>Property 2: Invalid Credentials Produce Generic Error — For ANY combination of invalid
 * credentials (wrong email, wrong password, both wrong), the error message is ALWAYS the same
 * generic message and NEVER reveals which field is incorrect.
 */
@Tag(
    "Feature: project-implementation-kickoff, Property 2: Invalid Credentials Produce Generic Error")
class InvalidCredentialsGenericErrorPropertyTest {

  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final String TENANT_ID = "tenant-alpha";
  private static final String REGISTERED_EMAIL = "registered@atlasops.test";
  private static final String REGISTERED_PASSWORD_HASH = "$2a$10$hashedPassword";
  private static final String REGISTERED_USER_ID = "user-001";
  private static final String EXPECTED_ERROR_MESSAGE = "Invalid email or password";

  /**
   * Property: For any arbitrary email that does NOT match the registered user, the use case SHALL
   * throw UnauthorizedException with the exact generic message "Invalid email or password".
   *
   * <p>This ensures the error message never leaks information about whether the email exists.
   */
  @Property(tries = 100)
  void should_returnGenericError_when_emailDoesNotExist(
      @ForAll("arbitraryEmails") String wrongEmail,
      @ForAll("arbitraryPasswords") String anyPassword) {

    AuthenticateUserUseCase useCase =
        buildUseCase(wrongEmail, anyPassword, FailureReason.EMAIL_NOT_FOUND);

    UnauthorizedException exception =
        catchThrowableOfType(
            () -> useCase.execute(new AuthenticateCommand(wrongEmail, anyPassword, TENANT_ID)),
            UnauthorizedException.class);

    assertThat(exception).isNotNull();
    assertThat(exception.getMessage()).isEqualTo(EXPECTED_ERROR_MESSAGE);
    assertThat(exception.getMessage()).doesNotContainIgnoringCase("not found");
    assertThat(exception.getMessage()).doesNotContainIgnoringCase("does not exist");
  }

  /**
   * Property: For any arbitrary password that does NOT match the registered password, the use case
   * SHALL throw UnauthorizedException with the exact generic message "Invalid email or password".
   *
   * <p>This ensures the error message never leaks information about which field (password) was
   * incorrect.
   */
  @Property(tries = 100)
  void should_returnGenericError_when_passwordIsWrong(
      @ForAll("arbitraryPasswords") String wrongPassword) {

    AuthenticateUserUseCase useCase =
        buildUseCase(REGISTERED_EMAIL, wrongPassword, FailureReason.WRONG_PASSWORD);

    UnauthorizedException exception =
        catchThrowableOfType(
            () ->
                useCase.execute(
                    new AuthenticateCommand(REGISTERED_EMAIL, wrongPassword, TENANT_ID)),
            UnauthorizedException.class);

    assertThat(exception).isNotNull();
    assertThat(exception.getMessage()).isEqualTo(EXPECTED_ERROR_MESSAGE);
    assertThat(exception.getMessage()).doesNotContainIgnoringCase("incorrect");
    assertThat(exception.getMessage()).doesNotContainIgnoringCase("wrong");
  }

  /**
   * Property: For ANY combination of invalid credentials (wrong email OR wrong password), the error
   * message SHALL ALWAYS be identical — never revealing which field caused the failure.
   */
  @Property(tries = 100)
  void should_produceIdenticalErrorMessage_when_anyCredentialCombinationIsInvalid(
      @ForAll("arbitraryEmails") String wrongEmail,
      @ForAll("arbitraryPasswords") String wrongPassword) {

    // Scenario 1: wrong email
    AuthenticateUserUseCase useCaseWrongEmail =
        buildUseCase(wrongEmail, "anyPass", FailureReason.EMAIL_NOT_FOUND);
    UnauthorizedException wrongEmailException =
        catchThrowableOfType(
            () ->
                useCaseWrongEmail.execute(
                    new AuthenticateCommand(wrongEmail, "anyPass", TENANT_ID)),
            UnauthorizedException.class);

    // Scenario 2: wrong password
    AuthenticateUserUseCase useCaseWrongPassword =
        buildUseCase(REGISTERED_EMAIL, wrongPassword, FailureReason.WRONG_PASSWORD);
    UnauthorizedException wrongPasswordException =
        catchThrowableOfType(
            () ->
                useCaseWrongPassword.execute(
                    new AuthenticateCommand(REGISTERED_EMAIL, wrongPassword, TENANT_ID)),
            UnauthorizedException.class);

    // Both scenarios MUST produce the exact same error message
    assertThat(wrongEmailException).isNotNull();
    assertThat(wrongPasswordException).isNotNull();
    assertThat(wrongEmailException.getMessage()).isEqualTo(wrongPasswordException.getMessage());
    assertThat(wrongEmailException.getMessage()).isEqualTo(EXPECTED_ERROR_MESSAGE);
  }

  // --- Custom Arbitraries ---

  @Provide
  Arbitrary<String> arbitraryEmails() {
    Arbitrary<String> localPart =
        Arbitraries.strings()
            .withChars("abcdefghijklmnopqrstuvwxyz0123456789._-".toCharArray())
            .ofMinLength(1)
            .ofMaxLength(30);

    Arbitrary<String> domain =
        Arbitraries.strings()
            .withChars("abcdefghijklmnopqrstuvwxyz0123456789".toCharArray())
            .ofMinLength(2)
            .ofMaxLength(15);

    Arbitrary<String> tld = Arbitraries.of("com", "org", "net", "io", "test", "dev");

    return Combinators.combine(localPart, domain, tld)
        .as((local, dom, ext) -> local + "@" + dom + "." + ext)
        .filter(email -> !email.equalsIgnoreCase(REGISTERED_EMAIL));
  }

  @Provide
  Arbitrary<String> arbitraryPasswords() {
    return Arbitraries.strings().withCharRange('!', '~').ofMinLength(1).ofMaxLength(50);
  }

  // --- Test Infrastructure ---

  private enum FailureReason {
    EMAIL_NOT_FOUND,
    WRONG_PASSWORD
  }

  private AuthenticateUserUseCase buildUseCase(
      String email, String password, FailureReason reason) {

    AuthUserPort authUserPort = mock(AuthUserPort.class);
    AccountLockoutPort accountLockoutPort = mock(AccountLockoutPort.class);
    PasswordHashPort passwordHashPort = mock(PasswordHashPort.class);
    JwtTokenPort jwtTokenPort = mock(JwtTokenPort.class);
    RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
    IdGenerator idGenerator = mock(IdGenerator.class);
    Clock clock = mock(Clock.class);

    when(clock.now()).thenReturn(NOW);
    lenient()
        .when(accountLockoutPort.getAttempts(anyString()))
        .thenReturn(new AccountLockout(email, 0, null));

    switch (reason) {
      case EMAIL_NOT_FOUND ->
          when(authUserPort.findByEmailAndTenantId(email, TENANT_ID)).thenReturn(Optional.empty());
      case WRONG_PASSWORD -> {
        when(authUserPort.findByEmailAndTenantId(email, TENANT_ID))
            .thenReturn(
                Optional.of(
                    new AuthUserData(
                        REGISTERED_USER_ID,
                        email,
                        REGISTERED_PASSWORD_HASH,
                        "ANALYST",
                        TENANT_ID,
                        true)));
        when(passwordHashPort.verify(password, REGISTERED_PASSWORD_HASH)).thenReturn(false);
      }
    }

    return new AuthenticateUserUseCase(
        authUserPort,
        accountLockoutPort,
        passwordHashPort,
        jwtTokenPort,
        refreshTokenRepository,
        idGenerator,
        clock);
  }
}
