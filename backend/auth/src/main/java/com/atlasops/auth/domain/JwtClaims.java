package com.atlasops.auth.domain;

import java.time.Instant;

/**
 * Value object representing validated JWT token claims.
 *
 * @param userId the authenticated user's identifier
 * @param tenantId the tenant identifier from the token
 * @param role the user's role
 * @param expiresAt the token expiration instant
 */
public record JwtClaims(String userId, String tenantId, Role role, Instant expiresAt) {}
