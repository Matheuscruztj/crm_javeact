package com.atlasops.auth.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.atlasops.auth.domain.Role;
import com.atlasops.auth.domain.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import net.jqwik.api.*;
import net.jqwik.api.lifecycle.AfterProperty;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Property-based tests for tenant context validation in TenantContextFilter.
 *
 * <p><b>Validates: Requirements 2.5, 2.6, 2.8</b>
 *
 * <p>Property 6: Tenant Context Validation
 *
 * <p>Requirement 2.5: Validate tenant context from JWT matches X-Tenant-ID header for every
 * authenticated request
 *
 * <p>Requirement 2.6: Mismatch → 403 Forbidden
 *
 * <p>Requirement 2.8: Missing X-Tenant-ID → 400 Bad Request
 */
@Tag("Feature: project-implementation-kickoff, Property 6: Tenant Context Validation")
class TenantContextFilterPropertyTest {

  private static final String USER_ID = "user-001";

  private TenantContextFilter filter;
  private ObjectMapper objectMapper;

  @BeforeProperty
  void setUp() {
    objectMapper = new ObjectMapper();
    filter = new TenantContextFilter(objectMapper);
    SecurityContextHolder.clearContext();
    TenantContext.clear();
  }

  @AfterProperty
  void tearDown() {
    SecurityContextHolder.clearContext();
    TenantContext.clear();
  }

  /**
   * Property: For ANY two DIFFERENT tenant IDs (one in JWT, one in header), the result is ALWAYS
   * 403 Forbidden.
   *
   * <p>Validates: Requirements 2.5, 2.6
   */
  @Property(tries = 100)
  void should_alwaysReturn403_when_headerTenantDiffersFromJwtTenant(
      @ForAll("validTenantIds") String jwtTenantId,
      @ForAll("validTenantIds") String headerTenantId,
      @ForAll("roles") Role role)
      throws ServletException, IOException {

    Assume.that(!jwtTenantId.equals(headerTenantId));

    // Arrange
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = mock(FilterChain.class);

    setAuthenticated(jwtTenantId, role);
    request.addHeader("X-Tenant-ID", headerTenantId);

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert
    assertThat(response.getStatus()).isEqualTo(403);
    assertThat(response.getContentType()).isEqualTo("application/json");

    String body = response.getContentAsString();
    assertThat(body).contains("\"code\":\"FORBIDDEN_ACTION\"");
    assertThat(body).contains("\"detail\":\"Tenant context mismatch\"");

    verify(filterChain, never()).doFilter(request, response);

    // Verify TenantContext was NOT set (cleared in finally)
    assertThat(TenantContext.getTenantId()).isNull();
  }

  /**
   * Property: For ANY tenant ID where the X-Tenant-ID header is null or blank, the result is ALWAYS
   * 400 Bad Request.
   *
   * <p>Validates: Requirement 2.8
   */
  @Property(tries = 100)
  void should_alwaysReturn400_when_tenantHeaderIsMissingOrBlank(
      @ForAll("validTenantIds") String jwtTenantId,
      @ForAll("blankOrAbsentHeaders") String headerValue,
      @ForAll("roles") Role role)
      throws ServletException, IOException {

    // Arrange
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = mock(FilterChain.class);

    setAuthenticated(jwtTenantId, role);

    // Only add header if headerValue is not the sentinel for "absent"
    if (headerValue != null) {
      request.addHeader("X-Tenant-ID", headerValue);
    }

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert
    assertThat(response.getStatus()).isEqualTo(400);
    assertThat(response.getContentType()).isEqualTo("application/json");

    String body = response.getContentAsString();
    assertThat(body).contains("\"code\":\"BAD_REQUEST\"");
    assertThat(body).contains("\"detail\":\"Missing required header: X-Tenant-ID\"");

    verify(filterChain, never()).doFilter(request, response);

    // Verify TenantContext was NOT set
    assertThat(TenantContext.getTenantId()).isNull();
  }

  /**
   * Property: For ANY matching tenant ID (JWT = header), the request ALWAYS passes through to the
   * filter chain.
   *
   * <p>Validates: Requirement 2.5
   */
  @Property(tries = 100)
  void should_alwaysPassThrough_when_headerMatchesJwtTenant(
      @ForAll("validTenantIds") String tenantId, @ForAll("roles") Role role)
      throws ServletException, IOException {

    // Arrange
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain filterChain = mock(FilterChain.class);

    setAuthenticated(tenantId, role);
    request.addHeader("X-Tenant-ID", tenantId);

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert
    assertThat(response.getStatus()).isEqualTo(200);
    verify(filterChain).doFilter(request, response);

    // Verify TenantContext was cleared after request (finally block)
    assertThat(TenantContext.getTenantId()).isNull();
  }

  // ---- Custom Arbitraries ----

  @Provide
  Arbitrary<String> validTenantIds() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .withChars('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-')
        .ofMinLength(3)
        .ofMaxLength(50)
        .filter(s -> s.matches("^[a-z][a-z0-9-]*[a-z0-9]$"));
  }

  @Provide
  @SuppressWarnings("NP_NONNULL_PARAM_VIOLATION")
  Arbitrary<String> blankOrAbsentHeaders() {
    return Arbitraries.of(null, "", "   ", "\t", "  \t  \n ");
  }

  @Provide
  Arbitrary<Role> roles() {
    return Arbitraries.of(Role.ADMIN, Role.ANALYST, Role.CLIENT);
  }

  // ---- Helper methods ----

  private void setAuthenticated(String tenantId, Role role) {
    var principal = new AuthenticatedPrincipal(USER_ID, tenantId, role);
    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
