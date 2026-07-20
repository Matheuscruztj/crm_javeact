package com.atlasops.auth.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

import com.atlasops.auth.domain.ports.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for LogoutUseCase. */
@ExtendWith(MockitoExtension.class)
class LogoutUseCaseTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;

    private LogoutUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new LogoutUseCase(refreshTokenRepository);
    }

    @Test
    void should_revokeToken_when_validRefreshToken() {
        // Act
        useCase.execute("some-raw-refresh-token");

        // Assert — token hash is computed and passed to repository
        verify(refreshTokenRepository).revokeByTokenHash(anyString());
    }

    @Test
    void should_computeSha256Hash_before_revoking() {
        String raw = "my-refresh-token";
        String expectedHash = LogoutUseCase.hashToken(raw);

        useCase.execute(raw);

        verify(refreshTokenRepository).revokeByTokenHash(expectedHash);
    }

    @Test
    void should_reject_when_tokenIsNull() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(NullPointerException.class);
    }
}
