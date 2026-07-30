package com.atlasops.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.atlasops.auth.domain.JwtClaims;
import com.atlasops.auth.domain.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;

class JwtTokenAdapterTest {

  private static final String SECRET = "0123456789abcdef0123456789abcdef";

  @Test
  void should_generateAndValidateToken_whenClaimsAreComplete() {
    JwtTokenAdapter adapter =
        new JwtTokenAdapter(SECRET, "atlasops", "atlasops-web", 3600);

    String token = adapter.generateAccessToken("user-1", "tenant-1", Role.ADMIN);
    JwtClaims claims = adapter.validateToken(token);

    assertThat(claims.userId()).isEqualTo("user-1");
    assertThat(claims.tenantId()).isEqualTo("tenant-1");
    assertThat(claims.role()).isEqualTo(Role.ADMIN);
    assertThat(claims.expiresAt()).isAfter(Instant.now());
  }

  @Test
  void should_rejectMalformedToken_whenParsingFails() {
    JwtTokenAdapter adapter =
        new JwtTokenAdapter(SECRET, "atlasops", "atlasops-web", 3600);

    assertThatThrownBy(() -> adapter.validateToken("not-a-jwt"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Invalid JWT token");
  }

  @Test
  void should_rejectExpiredToken_whenExpirationIsInThePast() {
    SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    String expiredToken =
        Jwts.builder()
            .subject("user-1")
            .issuer("atlasops")
            .audience().add("atlasops-web").and()
            .issuedAt(Date.from(Instant.now().minusSeconds(120)))
            .expiration(Date.from(Instant.now().minusSeconds(60)))
            .claim("tenantId", "tenant-1")
            .claim("role", Role.ADMIN.name())
            .signWith(key, Jwts.SIG.HS256)
            .compact();

    JwtTokenAdapter adapter =
        new JwtTokenAdapter(SECRET, "atlasops", "atlasops-web", 3600);

    assertThatThrownBy(() -> adapter.validateToken(expiredToken))
        .hasMessageContaining("JWT token has expired");
  }
}
