package com.atlasops.auth.presentation;

import com.atlasops.auth.domain.Role;

/**
 * Represents the authenticated user principal stored in the Spring Security context.
 *
 * @param userId the authenticated user's identifier
 * @param tenantId the tenant identifier from the JWT
 * @param role the user's role
 */
public record AuthenticatedPrincipal(String userId, String tenantId, Role role) {}
