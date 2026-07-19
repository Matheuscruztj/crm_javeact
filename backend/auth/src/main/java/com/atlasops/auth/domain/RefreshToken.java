package com.atlasops.auth.domain;

import com.atlasops.shared.domain.Entity;
import java.time.Instant;
import java.util.Objects;

/**
 * Entity representing a refresh token stored in Redis. Tracks token hash, ownership, expiration,
 * and revocation status.
 */
public class RefreshToken extends Entity<String> {

  private final String tokenHash;
  private final String userId;
  private final String tenantId;
  private final String role;
  private final String familyId;
  private final Instant expiresAt;
  private boolean revoked;
  private final Instant createdAt;

  public RefreshToken(
      String id,
      String tokenHash,
      String userId,
      String tenantId,
      Instant expiresAt,
      boolean revoked,
      Instant createdAt) {
    this(id, tokenHash, userId, tenantId, null, null, expiresAt, revoked, createdAt);
  }

  public RefreshToken(
      String id,
      String tokenHash,
      String userId,
      String tenantId,
      String role,
      String familyId,
      Instant expiresAt,
      boolean revoked,
      Instant createdAt) {
    super(id);
    this.tokenHash = Objects.requireNonNull(tokenHash, "tokenHash must not be null");
    this.userId = Objects.requireNonNull(userId, "userId must not be null");
    this.tenantId = Objects.requireNonNull(tenantId, "tenantId must not be null");
    this.role = role;
    this.familyId = familyId;
    this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt must not be null");
    this.revoked = revoked;
    this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
  }

  public boolean isExpired(Instant now) {
    return now.isAfter(expiresAt);
  }

  public boolean isValid(Instant now) {
    return !revoked && !isExpired(now);
  }

  public void revoke() {
    this.revoked = true;
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public String getUserId() {
    return userId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public boolean isRevoked() {
    return revoked;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public String getRole() {
    return role;
  }

  public String getFamilyId() {
    return familyId;
  }
}
