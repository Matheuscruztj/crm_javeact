package com.atlasops.auth.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import com.atlasops.auth.domain.ports.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for RevokeAllSessionsUseCase. */
@ExtendWith(MockitoExtension.class)
class RevokeAllSessionsUseCaseTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;

    private RevokeAllSessionsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RevokeAllSessionsUseCase(refreshTokenRepository);
    }

    @Test
    void should_revokeAllTokens_when_validUserId() {
        useCase.execute("user-001");

        verify(refreshTokenRepository).revokeAllByUserId("user-001");
    }

    @Test
    void should_reject_when_userIdIsNull() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(NullPointerException.class);
    }
}
