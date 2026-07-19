package com.atlasops.auth.presentation;

import com.atlasops.auth.application.ValidateTokenUseCase;
import com.atlasops.auth.domain.JwtClaims;
import com.atlasops.auth.domain.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Spring Security filter that validates JWT tokens on every request.
 *
 * <p>Extracts the Bearer token from the Authorization header, validates it via {@link
 * ValidateTokenUseCase}, and sets the Spring Security context with the authenticated principal.
 *
 * <p>On expired token: returns 401 with code TOKEN_EXPIRED. On invalid/missing token: lets the
 * request proceed unauthenticated (Spring Security will handle 401 if endpoint requires
 * authentication).
 *
 * <p>Validates: Requirements 1.9, 2.7
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";
  private static final String ERROR_TYPE_BASE = "https://atlasops/errors/";

  private final ValidateTokenUseCase validateTokenUseCase;
  private final ObjectMapper objectMapper;

  public JwtAuthenticationFilter(
      ValidateTokenUseCase validateTokenUseCase, ObjectMapper objectMapper) {
    this.validateTokenUseCase = validateTokenUseCase;
    this.objectMapper = objectMapper;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String token = extractToken(request);
    if (token == null) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      JwtClaims claims = validateTokenUseCase.execute(token);
      setSecurityContext(claims);
      filterChain.doFilter(request, response);
    } catch (TokenExpiredException ex) {
      log.warn("Expired JWT token for request: {}", request.getRequestURI());
      writeErrorResponse(
          response, HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "Access token has expired");
    } catch (Exception ex) {
      log.warn("Invalid JWT token for request: {}: {}", request.getRequestURI(), ex.getMessage());
      writeErrorResponse(
          response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Invalid or malformed token");
    }
  }

  private String extractToken(HttpServletRequest request) {
    String header = request.getHeader(AUTHORIZATION_HEADER);
    if (header != null && header.startsWith(BEARER_PREFIX)) {
      String token = header.substring(BEARER_PREFIX.length()).trim();
      return token.isEmpty() ? null : token;
    }
    return null;
  }

  private void setSecurityContext(JwtClaims claims) {
    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + claims.role().name()));

    var authentication =
        new UsernamePasswordAuthenticationToken(
            new AuthenticatedPrincipal(claims.userId(), claims.tenantId(), claims.role()),
            null,
            authorities);

    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  private void writeErrorResponse(
      HttpServletResponse response, HttpStatus status, String code, String detail)
      throws IOException {

    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    String traceId = MDC.get("correlationId");
    if (traceId == null) {
      traceId = "unknown";
    }

    Map<String, Object> body =
        Map.of(
            "type",
            ERROR_TYPE_BASE + "unauthorized",
            "title",
            "Unauthorized",
            "status",
            status.value(),
            "code",
            code,
            "detail",
            detail,
            "traceId",
            traceId);

    objectMapper.writeValue(response.getOutputStream(), body);
  }
}
