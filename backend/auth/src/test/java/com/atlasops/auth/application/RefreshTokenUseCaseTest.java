package com.atlasops.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.auth.domain.AuthenticationResult;
import com.atlasops.auth.domain.RefreshToken;
import com.atlasops.auth.domain.Role;
import com.atlasops.auth.domain.ports.JwtTokenPort;
import com.atlasops.auth.domain.ports.RefreshTokenRepository;
import com.atlasops.shared.domain.exceptions.UnauthorizedException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.IdGenerator;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for RefreshTokenUseCase.
 * Validates: Requirements 1.5, 1.6 — token rotation with replay detection
 */
@ExtendWith(MockitoExtension.class)
class RefreshTokenUseCaseTest {

    private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");
    private static final Instant EXPIRES_FUTURE = NOW.plusSeconds(3600);
    private static final Instant EXPIRES_PAST = NOW.minusSeconds(1);

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtTokenPort jwtTokenPort;
    @Mock private IdGenerator idGenerator;
    @Mock private Clock clock;

    private RefreshTokenUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RefreshTokenUseCase(refreshTokenRepository, jwtTokenPort, idGenerator, clock);
        when(clock.now()).thenReturn(NOW);
        when(idGenerator.generate()).thenReturn("new-id");
    }

    @Test
    void should_rotateToken_when_validRefreshToken() {
        String rawToken = "my-refresh-token";
        String hash = RefreshTokenUseCase.hashToken(rawToken);
        RefreshToken token = new RefreshToken("rt-001", hash, "user-001", "tenant-alpha",
                "ADMIN", "family-001", EXPIRES_FUTURE, false, NOW.minusSeconds(100));

        when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(token));
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(jwtTokenPort.generateAccessToken(anyString(), anyString(), any(Role.class)))
                .thenReturn("new-access-token");

        AuthenticationResult result = useCase.execute(rawToken);

        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isNotBlank();
        verify(refreshTokenRepository).revokeByTokenHash(hash);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void should_rejectAndRevokeAllSessions_when_tokenIsAlreadyRevoked() {
        String rawToken = "revoked-refresh-token";
        String hash = RefreshTokenUseCase.hashToken(rawToken);
        RefreshToken revokedToken = new RefreshToken("rt-002", hash, "user-001", "tenant-alpha",
                "ADMIN", "family-001", EXPIRES_FUTURE, true /* revoked */, NOW.minusSeconds(100));

        when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(revokedToken));

        assertThatThrownBy(() -> useCase.execute(rawToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("invalidated");

        verify(refreshTokenRepository).revokeAllByUserId("user-001");
    }

    @Test
    void should_rejectAndRevokeAllSessions_when_tokenIsExpired() {
        String rawToken = "expired-refresh-token";
        String hash = RefreshTokenUseCase.hashToken(rawToken);
        RefreshToken expiredToken = new RefreshToken("rt-003", hash, "user-001", "tenant-alpha",
                "ADMIN", "family-001", EXPIRES_PAST, false, NOW.minusSeconds(200));

        when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> useCase.execute(rawToken))
                .isInstanceOf(UnauthorizedException.class);

        verify(refreshTokenRepository).revokeAllByUserId("user-001");
    }

    @Test
    void should_throwUnauthorized_when_tokenNotFound() {
        String rawToken = "unknown-token";
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(rawToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Invalid refresh token");

        verify(refreshTokenRepository, never()).save(any());
    }
}
