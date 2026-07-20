package com.atlasops.integrations.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SSRFValidator}.
 * Validates: P0.J.1 — SSRF Protection Utility
 * P0.C.1.4 — Security: loopback denial, private network block
 */
class SSRFValidatorTest {

    @Test
    void should_acceptPublicHttpsUrl_when_urlIsValid() {
        assertThat(SSRFValidator.isSafe("https://example.com/webhook")).isTrue();
    }

    @Test
    void should_acceptPublicHttpUrl_when_urlIsValid() {
        assertThat(SSRFValidator.isSafe("http://example.com/webhook")).isTrue();
    }

    @Test
    void should_blockFtpScheme_when_nonHttpScheme() {
        assertThatThrownBy(() -> SSRFValidator.validate("ftp://example.com"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("SSRF blocked")
                .hasMessageContaining("ftp");
    }

    @Test
    void should_blockFileScheme_when_localFileAccess() {
        assertThatThrownBy(() -> SSRFValidator.validate("file:///etc/passwd"))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void should_blockLoopbackIpv4_when_127_0_0_1() {
        assertThatThrownBy(() -> SSRFValidator.validate("http://127.0.0.1/internal"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("loopback");
    }

    @Test
    void should_blockLoopbackLocalhost_when_localhost() {
        assertThatThrownBy(() -> SSRFValidator.validate("http://localhost:8080/admin"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("loopback");
    }

    @Test
    void should_blockPrivateNetwork10_when_10_x_x_x() {
        assertThatThrownBy(() -> SSRFValidator.validate("http://10.0.0.1/internal"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("private network");
    }

    @Test
    void should_blockPrivateNetwork192_168_when_192_168_x_x() {
        assertThatThrownBy(() -> SSRFValidator.validate("http://192.168.1.1/admin"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("private network");
    }

    @Test
    void should_throwException_when_urlIsNull() {
        assertThatThrownBy(() -> SSRFValidator.validate(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void should_throwException_when_urlIsBlank() {
        assertThatThrownBy(() -> SSRFValidator.validate("  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blank");
    }

    @Test
    void should_returnFalse_when_isSafeCalledWithLoopback() {
        assertThat(SSRFValidator.isSafe("http://127.0.0.1/")).isFalse();
    }

    @Test
    void should_returnFalse_when_isSafeCalledWithNull() {
        assertThat(SSRFValidator.isSafe(null)).isFalse();
    }

    @Test
    void should_throwException_when_urlHasNoHost() {
        assertThatThrownBy(() -> SSRFValidator.validate("https:///path"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
