package com.atlasops.auth.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.auth.application.ValidateTokenUseCase;
import com.atlasops.auth.domain.JwtClaims;
import com.atlasops.auth.domain.Role;
import com.atlasops.auth.domain.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter")
class JwtAuthenticationFilterTest {

  private static final String VALID_TOKEN = "valid.jwt.token";
  private static final String USER_ID = "user-001";
  private static final String TENANT_ID = "tenant-alpha";
  private static final Instant EXPIRATION = Instant.parse("2025-01-15T11:00:00Z");

  @Mock private ValidateTokenUseCase validateTokenUseCase;

  @Mock private FilterChain filterChain;

  private JwtAuthenticationFilter filter;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    filter = new JwtAuthenticationFilter(validateTokenUseCase, objectMapper);
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    SecurityContextHolder.clearContext();
  }

  @Test
  void should_setSecurityContext_when_validBearerTokenProvided()
      throws ServletException, IOException {
    // Arrange
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    var claims = new JwtClaims(USER_ID, TENANT_ID, Role.ANALYST, EXPIRATION);
    when(validateTokenUseCase.execute(VALID_TOKEN)).thenReturn(claims);

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication).isNotNull();
    assertThat(authentication.isAuthenticated()).isTrue();

    var principal = (AuthenticatedPrincipal) authentication.getPrincipal();
    assertThat(principal.userId()).isEqualTo(USER_ID);
    assertThat(principal.tenantId()).isEqualTo(TENANT_ID);
    assertThat(principal.role()).isEqualTo(Role.ANALYST);

    assertThat(authentication.getAuthorities())
        .extracting(Object::toString)
        .containsExactly("ROLE_ANALYST");

    verify(filterChain).doFilter(request, response);
  }

  @Test
  void should_continueWithoutAuthentication_when_noAuthorizationHeader()
      throws ServletException, IOException {
    // Arrange — no header

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void should_continueWithoutAuthentication_when_authorizationHeaderIsNotBearer()
      throws ServletException, IOException {
    // Arrange
    request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void should_return401WithTokenExpiredCode_when_tokenIsExpired()
      throws ServletException, IOException {
    // Arrange
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    when(validateTokenUseCase.execute(VALID_TOKEN))
        .thenThrow(new TokenExpiredException("Token has expired"));

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert
    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getContentType()).isEqualTo("application/json");

    String body = response.getContentAsString();
    assertThat(body).contains("\"code\":\"TOKEN_EXPIRED\"");
    assertThat(body).contains("\"status\":401");
    assertThat(body).contains("\"detail\":\"Access token has expired\"");

    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  void should_return401WithUnauthorizedCode_when_tokenIsInvalid()
      throws ServletException, IOException {
    // Arrange
    request.addHeader("Authorization", "Bearer invalid-token");
    when(validateTokenUseCase.execute("invalid-token"))
        .thenThrow(new RuntimeException("Invalid token signature"));

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert
    assertThat(response.getStatus()).isEqualTo(401);
    assertThat(response.getContentType()).isEqualTo("application/json");

    String body = response.getContentAsString();
    assertThat(body).contains("\"code\":\"UNAUTHORIZED\"");
    assertThat(body).contains("\"detail\":\"Invalid or malformed token\"");

    verify(filterChain, never()).doFilter(request, response);
  }

  @Test
  void should_continueWithoutAuthentication_when_bearerTokenIsEmpty()
      throws ServletException, IOException {
    // Arrange
    request.addHeader("Authorization", "Bearer ");

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void should_setCorrectRoleAuthority_when_adminTokenProvided()
      throws ServletException, IOException {
    // Arrange
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    var claims = new JwtClaims(USER_ID, TENANT_ID, Role.ADMIN, EXPIRATION);
    when(validateTokenUseCase.execute(VALID_TOKEN)).thenReturn(claims);

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication.getAuthorities())
        .extracting(Object::toString)
        .containsExactly("ROLE_ADMIN");
  }

  @Test
  void should_setCorrectRoleAuthority_when_clientTokenProvided()
      throws ServletException, IOException {
    // Arrange
    request.addHeader("Authorization", "Bearer " + VALID_TOKEN);
    var claims = new JwtClaims(USER_ID, TENANT_ID, Role.CLIENT, EXPIRATION);
    when(validateTokenUseCase.execute(VALID_TOKEN)).thenReturn(claims);

    // Act
    filter.doFilterInternal(request, response, filterChain);

    // Assert
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication.getAuthorities())
        .extracting(Object::toString)
        .containsExactly("ROLE_CLIENT");
  }
}
