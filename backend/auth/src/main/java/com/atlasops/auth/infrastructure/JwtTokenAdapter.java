package com.atlasops.auth.infrastructure;

import com.atlasops.auth.domain.JwtClaims;
import com.atlasops.auth.domain.Role;
import com.atlasops.auth.domain.TokenExpiredException;
import com.atlasops.auth.domain.ports.JwtTokenPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Local JWT adapter backed by JJWT.
 *
 * <p>Generates HMAC-SHA tokens with the issuer, audience, and expiration configured from the
 * environment. Tokens include user, tenant, and role claims required by the security filters.
 */
@Component
public class JwtTokenAdapter implements JwtTokenPort {

  private final SecretKey secretKey;
  private final String issuer;
  private final String audience;
  private final long expirationSeconds;

  public JwtTokenAdapter(
      @Value("${spring.security.jwt.secret}") String secret,
      @Value("${spring.security.jwt.issuer}") String issuer,
      @Value("${spring.security.jwt.audience}") String audience,
      @Value("${spring.security.jwt.expiration:3600}") long expirationSeconds) {
    byte[] keyBytes =
        secret.length() >= 32
            ? secret.getBytes(StandardCharsets.UTF_8)
            : Decoders.BASE64.decode(secret);
    this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    this.issuer = issuer;
    this.audience = audience;
    this.expirationSeconds = expirationSeconds;
  }

  @Override
  public String generateAccessToken(String userId, String tenantId, Role role) {
    Instant now = Instant.now();
    Instant expiresAt = now.plusSeconds(expirationSeconds);

    return Jwts.builder()
        .subject(userId)
        .issuer(issuer)
        .audience().add(audience).and()
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiresAt))
        .claim("tenantId", tenantId)
        .claim("role", role.name())
        .signWith(secretKey, Jwts.SIG.HS256)
        .compact();
  }

  @Override
  public JwtClaims validateToken(String token) {
    try {
      Jws<Claims> jws =
          Jwts.parser()
              .verifyWith(secretKey)
              .requireIssuer(issuer)
              .requireAudience(audience)
              .build()
              .parseSignedClaims(token);

      Claims claims = jws.getPayload();
      String userId = claims.getSubject();
      String tenantId = claims.get("tenantId", String.class);
      String roleName = claims.get("role", String.class);
      Date expiration = claims.getExpiration();

      if (userId == null || tenantId == null || roleName == null || expiration == null) {
        throw new IllegalArgumentException("JWT is missing required claims");
      }

      return new JwtClaims(
          userId, tenantId, Role.valueOf(roleName), expiration.toInstant());
    } catch (ExpiredJwtException ex) {
      throw new TokenExpiredException("JWT token has expired", ex);
    } catch (JwtException | IllegalArgumentException ex) {
      throw new IllegalArgumentException("Invalid JWT token", ex);
    }
  }
}
