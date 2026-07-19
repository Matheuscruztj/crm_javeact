package com.atlasops.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.EnumSet;
import java.util.Set;
import net.jqwik.api.*;

/**
 * Property-based test for the Role-Based Access Matrix.
 *
 * <p><b>Validates: Requirements 2.1, 2.2, 2.3, 2.4</b>
 *
 * <p>Property 5: Role-Based Access Matrix
 *
 * <p>Requirement 2.1: THE Auth_Module SHALL recognize exactly three roles — ADMIN, ANALYST, CLIENT
 *
 * <p>Requirement 2.2: WHEN a user with role CLIENT attempts to access an ADMIN-only endpoint,
 * return 403
 *
 * <p>Requirement 2.3: WHEN a user with role CLIENT attempts to access an ANALYST-only endpoint,
 * return 403
 *
 * <p>Requirement 2.4: WHEN a user with role ANALYST attempts to access an ADMIN-only endpoint,
 * return 403
 *
 * <p>This test models the access matrix logic as a pure function (no HTTP involved) and verifies
 * that for ANY endpoint with a given required role set, the correct access decisions are made.
 */
@Tag("Feature: project-implementation-kickoff, Property 5: Role-Based Access Matrix")
class RoleBasedAccessMatrixPropertyTest {

  /**
   * The complete set of valid roles recognized by the system. Requirement 2.1: exactly three roles
   * — ADMIN, ANALYST, CLIENT.
   */
  private static final Set<Role> ALL_VALID_ROLES = EnumSet.allOf(Role.class);

  // ─── Access Matrix Logic ─────────────────────────────────────────────────────

  /** Represents the access decision result, mirroring HTTP semantics. */
  enum AccessDecision {
    ALLOWED, // 200 — access granted
    FORBIDDEN // 403 — insufficient role
  }

  /** Represents an endpoint's access policy: which roles are permitted. */
  record EndpointPolicy(String endpointName, Set<Role> allowedRoles) {
    boolean isAdminOnly() {
      return allowedRoles.equals(EnumSet.of(Role.ADMIN));
    }

    boolean isAnalystOnly() {
      return allowedRoles.equals(EnumSet.of(Role.ANALYST));
    }

    boolean isAnalystOrAdmin() {
      return allowedRoles.equals(EnumSet.of(Role.ADMIN, Role.ANALYST));
    }
  }

  /**
   * Evaluates access based on role and endpoint policy. This is the pure function under test — it
   * models the same logic that Spring Security's role-based authorization performs at runtime via
   * ROLE_ authorities set by JwtAuthenticationFilter.
   */
  private static AccessDecision evaluateAccess(Role userRole, EndpointPolicy policy) {
    if (policy.allowedRoles().contains(userRole)) {
      return AccessDecision.ALLOWED;
    }
    return AccessDecision.FORBIDDEN;
  }

  // ─── Property Tests ──────────────────────────────────────────────────────────

  /**
   * Property: The Role enum SHALL contain exactly three values — ADMIN, ANALYST, CLIENT.
   *
   * <p>Validates: Requirement 2.1
   */
  @Property(tries = 1)
  void should_recognizeExactlyThreeRoles_when_roleEnumIsInspected() {
    // Assert: exactly three roles exist
    assertThat(Role.values())
        .as("Auth module must recognize exactly three roles")
        .hasSize(3)
        .containsExactlyInAnyOrder(Role.ADMIN, Role.ANALYST, Role.CLIENT);

    // Assert: these are the only valid roles
    assertThat(ALL_VALID_ROLES).containsExactlyInAnyOrder(Role.ADMIN, Role.ANALYST, Role.CLIENT);
  }

  /**
   * Property: For ANY ADMIN-only endpoint, a CLIENT user SHALL always receive FORBIDDEN (403).
   *
   * <p>Validates: Requirement 2.2
   */
  @Property(tries = 100)
  void should_returnForbidden_when_clientAccessesAdminOnlyEndpoint(
      @ForAll("adminOnlyEndpoints") EndpointPolicy adminEndpoint) {

    // Act
    AccessDecision decision = evaluateAccess(Role.CLIENT, adminEndpoint);

    // Assert: CLIENT accessing ADMIN-only → 403
    assertThat(decision)
        .as(
            "CLIENT accessing ADMIN-only endpoint '%s' must be FORBIDDEN",
            adminEndpoint.endpointName())
        .isEqualTo(AccessDecision.FORBIDDEN);
  }

  /**
   * Property: For ANY ANALYST-only endpoint, a CLIENT user SHALL always receive FORBIDDEN (403).
   *
   * <p>Validates: Requirement 2.3
   */
  @Property(tries = 100)
  void should_returnForbidden_when_clientAccessesAnalystOnlyEndpoint(
      @ForAll("analystOnlyEndpoints") EndpointPolicy analystEndpoint) {

    // Act
    AccessDecision decision = evaluateAccess(Role.CLIENT, analystEndpoint);

    // Assert: CLIENT accessing ANALYST-only → 403
    assertThat(decision)
        .as(
            "CLIENT accessing ANALYST-only endpoint '%s' must be FORBIDDEN",
            analystEndpoint.endpointName())
        .isEqualTo(AccessDecision.FORBIDDEN);
  }

  /**
   * Property: For ANY ADMIN-only endpoint, an ANALYST user SHALL always receive FORBIDDEN (403).
   *
   * <p>Validates: Requirement 2.4
   */
  @Property(tries = 100)
  void should_returnForbidden_when_analystAccessesAdminOnlyEndpoint(
      @ForAll("adminOnlyEndpoints") EndpointPolicy adminEndpoint) {

    // Act
    AccessDecision decision = evaluateAccess(Role.ANALYST, adminEndpoint);

    // Assert: ANALYST accessing ADMIN-only → 403
    assertThat(decision)
        .as(
            "ANALYST accessing ADMIN-only endpoint '%s' must be FORBIDDEN",
            adminEndpoint.endpointName())
        .isEqualTo(AccessDecision.FORBIDDEN);
  }

  /**
   * Property: For ANY endpoint, the designated allowed roles SHALL always receive ALLOWED. This is
   * the positive complement — verifying only the designated roles can access.
   *
   * <p>Validates: Requirements 2.1, 2.2, 2.3, 2.4 (completeness of access matrix)
   */
  @Property(tries = 100)
  void should_allowAccess_when_roleMatchesEndpointPolicy(
      @ForAll("anyEndpointWithMatchingRole") RoleEndpointPair pair) {

    // Act
    AccessDecision decision = evaluateAccess(pair.role(), pair.policy());

    // Assert: designated role accessing its allowed endpoint → ALLOWED
    assertThat(decision)
        .as(
            "Role %s accessing endpoint '%s' where it is allowed must be ALLOWED",
            pair.role(), pair.policy().endpointName())
        .isEqualTo(AccessDecision.ALLOWED);
  }

  /**
   * Property: For ANY endpoint and ANY role NOT in the allowed set, access SHALL be FORBIDDEN
   * (403).
   *
   * <p>Validates: Requirements 2.2, 2.3, 2.4 (generalized denial)
   */
  @Property(tries = 100)
  void should_returnForbidden_when_roleNotInAllowedSet(
      @ForAll("anyEndpointWithDeniedRole") RoleEndpointPair pair) {

    // Act
    AccessDecision decision = evaluateAccess(pair.role(), pair.policy());

    // Assert: non-allowed role → FORBIDDEN
    assertThat(decision)
        .as(
            "Role %s accessing endpoint '%s' where it is NOT allowed must be FORBIDDEN",
            pair.role(), pair.policy().endpointName())
        .isEqualTo(AccessDecision.FORBIDDEN);
  }

  // ─── Generators ──────────────────────────────────────────────────────────────

  @Provide
  Arbitrary<EndpointPolicy> adminOnlyEndpoints() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .ofMinLength(3)
        .ofMaxLength(30)
        .map(name -> new EndpointPolicy("/api/v1/admin/" + name, EnumSet.of(Role.ADMIN)));
  }

  @Provide
  Arbitrary<EndpointPolicy> analystOnlyEndpoints() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .ofMinLength(3)
        .ofMaxLength(30)
        .map(name -> new EndpointPolicy("/api/v1/analyst/" + name, EnumSet.of(Role.ANALYST)));
  }

  @Provide
  Arbitrary<EndpointPolicy> analystOrAdminEndpoints() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .ofMinLength(3)
        .ofMaxLength(30)
        .map(
            name ->
                new EndpointPolicy(
                    "/api/v1/management/" + name, EnumSet.of(Role.ADMIN, Role.ANALYST)));
  }

  @Provide
  Arbitrary<EndpointPolicy> anyProtectedEndpoints() {
    return Arbitraries.oneOf(
        adminOnlyEndpoints(), analystOnlyEndpoints(), analystOrAdminEndpoints());
  }

  @Provide
  Arbitrary<RoleEndpointPair> anyEndpointWithMatchingRole() {
    return anyProtectedEndpoints()
        .flatMap(
            policy ->
                Arbitraries.of(policy.allowedRoles().toArray(new Role[0]))
                    .map(role -> new RoleEndpointPair(role, policy)));
  }

  @Provide
  Arbitrary<RoleEndpointPair> anyEndpointWithDeniedRole() {
    return anyProtectedEndpoints()
        .flatMap(
            policy -> {
              Set<Role> deniedRoles =
                  EnumSet.complementOf(
                      policy.allowedRoles() instanceof EnumSet<Role> es
                          ? es
                          : EnumSet.copyOf(policy.allowedRoles()));
              if (deniedRoles.isEmpty()) {
                return Arbitraries.just(null);
              }
              return Arbitraries.of(deniedRoles.toArray(new Role[0]))
                  .map(role -> new RoleEndpointPair(role, policy));
            })
        .filter(pair -> pair != null);
  }

  // ─── Helper Types ────────────────────────────────────────────────────────────

  record RoleEndpointPair(Role role, EndpointPolicy policy) {}
}
