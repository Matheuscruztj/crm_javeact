package com.atlasops.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.atlasops.auth.domain.JwtClaims;
import com.atlasops.auth.domain.Role;
import com.atlasops.auth.domain.ports.JwtTokenPort;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for ValidateTokenUseCase. */
@ExtendWith(MockitoExtension.class)
class ValidateTokenUseCaseTest {

    @Mock private JwtTokenPort jwtTokenPort;

    private ValidateTokenUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ValidateTokenUseCase(jwtTokenPort);
    }

    @Test
    void should_returnClaims_when_tokenIsValid() {
        JwtClaims expectedClaims = new JwtClaims(
                "user-001", "tenant-alpha", Role.ADMIN, Instant.now().plusSeconds(3600));
        when(jwtTokenPort.validateToken("valid-token")).thenReturn(expectedClaims);

        JwtClaims result = useCase.execute("valid-token");

        assertThat(result.userId()).isEqualTo("user-001");
        assertThat(result.tenantId()).isEqualTo("tenant-alpha");
    }

    @Test
    void should_reject_when_tokenIsNull() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token must not be null");
    }

    @Test
    void should_reject_when_tokenIsBlank() {
        assertThatThrownBy(() -> useCase.execute("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_propagateException_when_jwtPortThrows() {
        when(jwtTokenPort.validateToken("bad-token"))
                .thenThrow(new IllegalArgumentException("Invalid token signature"));

        assertThatThrownBy(() -> useCase.execute("bad-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid token signature");
    }
}
