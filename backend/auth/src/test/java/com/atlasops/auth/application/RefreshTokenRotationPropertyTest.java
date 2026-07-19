package com.atlasops.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.auth.domain.AuthenticationResult;
import com.atlasops.auth.domain.RefreshToken;
import com.atlasops.auth.domain.Role;
import com.atlasops.auth.domain.ports.JwtTokenPort;
import com.atlasops.auth.domain.ports.RefreshTokenRepository;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.Tag;
import org.mockito.ArgumentCaptor;

/**
 * Validates: Requirements 1.4, 1.6 Property 3: Refresh Token Rotation Invalidates Previous
 *
 * <p>For ANY valid refresh token, after rotation: - The old token is invalidated (revoked) - A new
 * token pair (access + refresh) is returned - The new refresh token is different from the old one
 */
@Tag("Feature: auth, Property 3: Refresh Token Rotation Invalidates Previous")
class RefreshTokenRotationPropertyTest {

  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final String ACCESS_TOKEN = "jwt-access-token-generated";

  @Property(tries = 100)
  void should_invalidateOldToken_and_returnNewDifferentToken_when_rotatingAnyValidRefreshToken(
      @ForAll("validRefreshTokenInputs") RefreshTokenInput input) {

    // Arrange
    RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
    JwtTokenPort jwtTokenPort = mock(JwtTokenPort.class);
    IdGenerator idGenerator = mock(IdGenerator.class);
    Clock clock = mock(Clock.class);

    RefreshTokenUseCase useCase =
        new RefreshTokenUseCase(refreshTokenRepository, jwtTokenPort, idGenerator, clock);

    String oldTokenHash = RefreshTokenUseCase.hashToken(input.rawToken());

    RefreshToken existingToken =
        new RefreshToken(
            input.tokenId(),
            oldTokenHash,
            input.userId(),
            input.tenantId(),
            NOW.plus(Duration.ofDays(7)),
            false,
            NOW.minus(Duration.ofHours(1)));

    when(refreshTokenRepository.findByTokenHash(oldTokenHash))
        .thenReturn(Optional.of(existingToken));
    when(clock.now()).thenReturn(NOW);
    when(idGenerator.generate()).thenReturn("new-token-id");
    when(jwtTokenPort.generateAccessToken(input.userId(), input.tenantId(), Role.ADMIN))
        .thenReturn(ACCESS_TOKEN);

    // Act
    AuthenticationResult result = useCase.execute(input.rawToken());

    // Assert — Old token is revoked
    verify(refreshTokenRepository).revokeByTokenHash(oldTokenHash);

    // Assert — New token pair returned
    assertThat(result).isNotNull();
    assertThat(result.accessToken()).isEqualTo(ACCESS_TOKEN);
    assertThat(result.refreshToken()).isNotBlank();
    assertThat(result.tokenType()).isEqualTo("Bearer");
    assertThat(result.expiresIn()).isGreaterThan(0);

    // Assert — New refresh token is different from old one
    assertThat(result.refreshToken()).isNotEqualTo(input.rawToken());

    // Assert — New refresh token is saved
    ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
    verify(refreshTokenRepository).save(captor.capture());
    RefreshToken savedToken = captor.getValue();
    assertThat(savedToken.getUserId()).isEqualTo(input.userId());
    assertThat(savedToken.getTenantId()).isEqualTo(input.tenantId());
    assertThat(savedToken.isRevoked()).isFalse();
    assertThat(savedToken.getTokenHash()).isNotEqualTo(oldTokenHash);
  }

  @Property(tries = 100)
  void should_revokeRefreshToken_when_logoutWithAnyValidToken(
      @ForAll("validRawTokens") String rawToken) {

    // Arrange
    RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
    LogoutUseCase logoutUseCase = new LogoutUseCase(refreshTokenRepository);

    String expectedHash = LogoutUseCase.hashToken(rawToken);

    // Act
    logoutUseCase.execute(rawToken);

    // Assert — Token is revoked in repository by its hash
    verify(refreshTokenRepository).revokeByTokenHash(expectedHash);
  }

  @Property(tries = 100)
  void should_produceDifferentHashForDifferentTokens_forAnyTwoDistinctTokens(
      @ForAll("validRawTokens") String token1, @ForAll("validRawTokens") String token2) {

    // If tokens are different, their hashes should also be different (collision resistant)
    if (!token1.equals(token2)) {
      String hash1 = RefreshTokenUseCase.hashToken(token1);
      String hash2 = RefreshTokenUseCase.hashToken(token2);
      assertThat(hash1).isNotEqualTo(hash2);
    }
  }

  @Provide
  Arbitrary<RefreshTokenInput> validRefreshTokenInputs() {
    Arbitrary<String> rawTokens = validRawTokens();
    Arbitrary<String> userIds =
        Arbitraries.strings()
            .withCharRange('a', 'z')
            .ofMinLength(5)
            .ofMaxLength(20)
            .map(s -> "user-" + s);
    Arbitrary<String> tenantIds =
        Arbitraries.strings()
            .withCharRange('a', 'z')
            .ofMinLength(5)
            .ofMaxLength(20)
            .map(s -> "tenant-" + s);
    Arbitrary<String> tokenIds =
        Arbitraries.strings()
            .withCharRange('a', 'z')
            .ofMinLength(5)
            .ofMaxLength(10)
            .map(s -> "rt-" + s);

    return Combinators.combine(rawTokens, userIds, tenantIds, tokenIds).as(RefreshTokenInput::new);
  }

  @Provide
  Arbitrary<String> validRawTokens() {
    return Arbitraries.strings()
        .withCharRange('0', '9')
        .withCharRange('a', 'f')
        .withChars('-')
        .ofMinLength(8)
        .ofMaxLength(64)
        .filter(s -> !s.isBlank() && !s.startsWith("-") && !s.endsWith("-"));
  }

  record RefreshTokenInput(String rawToken, String userId, String tenantId, String tokenId) {}
}
