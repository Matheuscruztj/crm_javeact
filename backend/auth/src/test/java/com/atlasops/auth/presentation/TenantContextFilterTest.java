package com.atlasops.auth.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.atlasops.auth.domain.Role;
import com.atlasops.auth.domain.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("TenantContextFilter")
class TenantContextFilterTest {

  private static final String USER_ID = "user-001";
  private static final String TENANT_ID = "tenant-alpha";

  @Mock private FilterChain filterChain;

  private TenantContextFilter filter;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    filter = new TenantContextFilter(objectMapper);
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    SecurityContextHolder.clearContext();
    TenantContext.clear();
  }

  @Test
  void should_setTenantContext_when_headerMatchesJwtTenant() throws ServletException, IOException {
    // Arrange
    setAuthenticated(USER_ID, TENANT_ID, Role.ANALYST);
    request.addHeader("X-Tenant-ID", TENANT_ID);

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(200);
    // TenantContext is cleared in finally block, so we verify the chain was called
  }

  @Test
  void should_return400_when_tenantHeaderIsMissing() throws ServletException, IOException {
    // Arrange
    setAuthenticated(USER_ID, TENANT_ID, Role.ANALYST);
    // No X-Tenant-ID header

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert
    assertThat(response.getStatus()).isEqualTo(400);
    assertThat(response.getContentType()).isEqualTo("application/json");

    String body = response.getContentAsString();
    assertThat(body).contains("\"code\":\"BAD_REQUEST\"");
    assertThat(body).contains("\"detail\":\"Missing required header: X-Tenant-ID\"");

    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  void should_return400_when_tenantHeaderIsBlank() throws ServletException, IOException {
    // Arrange
    setAuthenticated(USER_ID, TENANT_ID, Role.ANALYST);
    request.addHeader("X-Tenant-ID", "   ");

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert
    assertThat(response.getStatus()).isEqualTo(400);

    String body = response.getContentAsString();
    assertThat(body).contains("\"code\":\"BAD_REQUEST\"");

    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  void should_return403_when_tenantHeaderDoesNotMatchJwtClaim()
      throws ServletException, IOException {
    // Arrange
    setAuthenticated(USER_ID, TENANT_ID, Role.ANALYST);
    request.addHeader("X-Tenant-ID", "tenant-beta");

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert
    assertThat(response.getStatus()).isEqualTo(403);
    assertThat(response.getContentType()).isEqualTo("application/json");

    String body = response.getContentAsString();
    assertThat(body).contains("\"code\":\"FORBIDDEN_ACTION\"");
    assertThat(body).contains("\"detail\":\"Tenant context mismatch\"");

    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  void should_proceedWithoutValidation_when_requestIsUnauthenticated()
      throws ServletException, IOException {
    // Arrange — no authentication set
    // No X-Tenant-ID header either

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  void should_clearTenantContext_when_requestCompletes() throws ServletException, IOException {
    // Arrange
    setAuthenticated(USER_ID, TENANT_ID, Role.ANALYST);
    request.addHeader("X-Tenant-ID", TENANT_ID);

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert — TenantContext is cleared after request
    assertThat(TenantContext.getTenantId()).isNull();
  }

  @Test
  void should_clearTenantContext_when_requestFails() throws ServletException, IOException {
    // Arrange
    setAuthenticated(USER_ID, TENANT_ID, Role.ANALYST);
    request.addHeader("X-Tenant-ID", "tenant-beta"); // mismatch — will return 403

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert — TenantContext is cleared even on error
    assertThat(TenantContext.getTenantId()).isNull();
  }

  @Test
  void should_trimTenantHeader_when_headerHasWhitespace() throws ServletException, IOException {
    // Arrange
    setAuthenticated(USER_ID, TENANT_ID, Role.ANALYST);
    request.addHeader("X-Tenant-ID", "  " + TENANT_ID + "  ");

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert
    verify(filterChain).doFilter(request, response);
    assertThat(response.getStatus()).isEqualTo(200);
  }

  @Test
  void should_return403_when_adminTriesWrongTenant() throws ServletException, IOException {
    // Arrange — even ADMIN role can't mismatch tenant
    setAuthenticated(USER_ID, TENANT_ID, Role.ADMIN);
    request.addHeader("X-Tenant-ID", "tenant-beta");

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert
    assertThat(response.getStatus()).isEqualTo(403);
    verify(filterChain, never()).doFilter(request, response);
  }

  // --- Helper methods ---

  private void setAuthenticated(String userId, String tenantId, Role role) {
    var principal = new AuthenticatedPrincipal(userId, tenantId, role);
    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
