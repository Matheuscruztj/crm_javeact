package com.atlasops.integrations.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SSRFValidator}.
 * Validates: P0.C.1.4 — Security tests: loopback denial, private network block
 * Validates: P0.J.1 — SSRF Protection Utility
 */
class SSRFValidatorTest {

  @Test
  void should_throwSecurityException_when_targetIsLoopback() {
    assertThatThrownBy(() -> SSRFValidator.validate("http://127.0.0.1/secret"))
        .isInstanceOf(SecurityException.class)
        .hasMessageContaining("loopback");
  }

  @Test
  void should_throwSecurityException_when_targetIsLocalhost() {
    assertThatThrownBy(() -> SSRFValidator.validate("http://localhost/admin"))
        .isInstanceOf(SecurityException.class);
  }

  @Test
  void should_throwSecurityException_when_targetIsPrivate10x() {
    assertThatThrownBy(() -> SSRFValidator.validate("http://10.0.0.1/internal"))
        .isInstanceOf(SecurityException.class)
        .hasMessageContaining("private");
  }

  @Test
  void should_throwSecurityException_when_targetIsPrivate192168() {
    assertThatThrownBy(() -> SSRFValidator.validate("http://192.168.1.100/api"))
        .isInstanceOf(SecurityException.class)
        .hasMessageContaining("private");
  }

  @Test
  void should_throwSecurityException_when_schemeIsFile() {
    assertThatThrownBy(() -> SSRFValidator.validate("file:///etc/passwd"))
        .isInstanceOf(SecurityException.class)
        .hasMessageContaining("HTTP/HTTPS");
  }

  @Test
  void should_throwSecurityException_when_schemeIsFtp() {
    assertThatThrownBy(() -> SSRFValidator.validate("ftp://external.com/file"))
        .isInstanceOf(SecurityException.class)
        .hasMessageContaining("HTTP/HTTPS");
  }

  @Test
  void should_throwIllegalArgument_when_urlIsNull() {
    assertThatThrownBy(() -> SSRFValidator.validate(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("blank");
  }

  @Test
  void should_throwIllegalArgument_when_urlIsBlank() {
    assertThatThrownBy(() -> SSRFValidator.validate("  "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("blank");
  }

  @Test
  void should_returnFalse_isSafe_when_privateAddress() {
    assertThat(SSRFValidator.isSafe("http://10.0.0.1/api")).isFalse();
  }

  @Test
  void should_returnFalse_isSafe_when_loopback() {
    assertThat(SSRFValidator.isSafe("http://127.0.0.1/")).isFalse();
  }

  @Test
  void should_returnFalse_isSafe_when_fileScheme() {
    assertThat(SSRFValidator.isSafe("file:///etc/hosts")).isFalse();
  }
}
