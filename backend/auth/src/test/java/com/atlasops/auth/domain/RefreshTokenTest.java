package com.atlasops.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RefreshToken")
class RefreshTokenTest {

  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final Instant PAST = Instant.parse("2025-01-14T10:00:00Z");
  private static final Instant FUTURE = Instant.parse("2025-01-22T10:00:00Z");

  private RefreshToken createToken(Instant expiresAt, boolean revoked) {
    return new RefreshToken(
        "token-id-1", "hash-abc123", "user-001", "tenant-alpha", expiresAt, revoked, NOW);
  }

  @Test
  void should_beExpired_when_nowIsAfterExpiresAt() {
    RefreshToken token = createToken(PAST, false);

    assertThat(token.isExpired(NOW)).isTrue();
  }

  @Test
  void should_notBeExpired_when_nowIsBeforeExpiresAt() {
    RefreshToken token = createToken(FUTURE, false);

    assertThat(token.isExpired(NOW)).isFalse();
  }

  @Test
  void should_beValid_when_notRevokedAndNotExpired() {
    RefreshToken token = createToken(FUTURE, false);

    assertThat(token.isValid(NOW)).isTrue();
  }

  @Test
  void should_notBeValid_when_revoked() {
    RefreshToken token = createToken(FUTURE, true);

    assertThat(token.isValid(NOW)).isFalse();
  }

  @Test
  void should_notBeValid_when_expired() {
    RefreshToken token = createToken(PAST, false);

    assertThat(token.isValid(NOW)).isFalse();
  }

  @Test
  void should_notBeValid_when_revokedAndExpired() {
    RefreshToken token = createToken(PAST, true);

    assertThat(token.isValid(NOW)).isFalse();
  }

  @Test
  void should_becomeRevoked_when_revokeIsCalled() {
    RefreshToken token = createToken(FUTURE, false);

    token.revoke();

    assertThat(token.isRevoked()).isTrue();
    assertThat(token.isValid(NOW)).isFalse();
  }

  @Test
  void should_throwException_when_tokenHashIsNull() {
    assertThatThrownBy(
            () -> new RefreshToken("id", null, "user-001", "tenant-alpha", FUTURE, false, NOW))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("tokenHash");
  }

  @Test
  void should_throwException_when_userIdIsNull() {
    assertThatThrownBy(
            () -> new RefreshToken("id", "hash", null, "tenant-alpha", FUTURE, false, NOW))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("userId");
  }

  @Test
  void should_throwException_when_tenantIdIsNull() {
    assertThatThrownBy(() -> new RefreshToken("id", "hash", "user-001", null, FUTURE, false, NOW))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("tenantId");
  }

  @Test
  void should_throwException_when_idIsNull() {
    assertThatThrownBy(
            () -> new RefreshToken(null, "hash", "user-001", "tenant-alpha", FUTURE, false, NOW))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void should_exposeAllFields_when_constructed() {
    RefreshToken token = createToken(FUTURE, false);

    assertThat(token.getId()).isEqualTo("token-id-1");
    assertThat(token.getTokenHash()).isEqualTo("hash-abc123");
    assertThat(token.getUserId()).isEqualTo("user-001");
    assertThat(token.getTenantId()).isEqualTo("tenant-alpha");
    assertThat(token.getExpiresAt()).isEqualTo(FUTURE);
    assertThat(token.isRevoked()).isFalse();
    assertThat(token.getCreatedAt()).isEqualTo(NOW);
  }
}
