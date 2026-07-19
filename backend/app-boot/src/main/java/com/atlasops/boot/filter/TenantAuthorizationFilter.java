package com.atlasops.boot.filter;

import com.atlasops.auth.presentation.AuthenticatedPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Spring Security filter that validates the {@code X-Tenant-ID} header against the JWT claim.
 *
 * <p>Executes after {@link com.atlasops.auth.presentation.JwtAuthenticationFilter}. Ensures a
 * user from Tenant A cannot pass {@code X-Tenant-ID: tenant-B} to access another tenant's data.
 *
 * <p>Bypass rules:
 * <ul>
 *   <li>Unauthenticated requests — handled by security config
 *   <li>Public paths (login, refresh, actuator, swagger) — excluded
 *   <li>Role SUPER_ADMIN — allowed to access any tenant
 * </ul>
 *
 * <p>Validates: Requirement P0.K.2 — tenant escalation prevention
 */
public class TenantAuthorizationFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(TenantAuthorizationFilter.class);
  private static final String TENANT_HEADER = "X-Tenant-ID";
  private static final String ERROR_TYPE_BASE = "https://atlasops/errors/";

  /** Paths that do not require tenant validation. */
  private static final Set<String> EXCLUDED_PREFIXES = Set.of(
      "/actuator",
      "/swagger-ui",
      "/v3/api-docs",
      "/api/v1/auth/login",
      "/api/v1/auth/refresh");

  private final ObjectMapper objectMapper;

  public TenantAuthorizationFilter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    // Skip excluded public paths
    String path = request.getRequestURI();
    if (isExcluded(path)) {
      chain.doFilter(request, response);
      return;
    }

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // No authentication present — let Spring Security handle 401
    if (authentication == null || !authentication.isAuthenticated()
        || !(authentication.getPrincipal() instanceof AuthenticatedPrincipal principal)) {
      chain.doFilter(request, response);
      return;
    }

    // SUPER_ADMIN bypasses tenant check (multi-tenant administration)
    if ("SUPER_ADMIN".equals(principal.role().name())) {
      chain.doFilter(request, response);
      return;
    }

    String requestedTenantId = request.getHeader(TENANT_HEADER);

    // If no tenant header present, reject with 403
    if (requestedTenantId == null || requestedTenantId.isBlank()) {
      log.warn("Missing X-Tenant-ID header for user {} on path {}", principal.userId(), path);
      writeErrorResponse(response, HttpStatus.FORBIDDEN, "FORBIDDEN_ACTION",
          "X-Tenant-ID header is required");
      return;
    }

    // Validate that the tenant in the header matches the JWT claim
    if (!requestedTenantId.equals(principal.tenantId())) {
      log.warn(
          "Tenant mismatch for user {}: JWT tenantId={}, header tenantId={}",
          principal.userId(), principal.tenantId(), requestedTenantId);
      writeErrorResponse(response, HttpStatus.FORBIDDEN, "FORBIDDEN_ACTION",
          "Access to tenant '" + requestedTenantId + "' is not allowed for this account");
      return;
    }

    chain.doFilter(request, response);
  }

  private boolean isExcluded(String path) {
    return EXCLUDED_PREFIXES.stream().anyMatch(path::startsWith);
  }

  private void writeErrorResponse(
      HttpServletResponse response, HttpStatus status, String code, String detail)
      throws IOException {
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    String traceId = MDC.get("correlationId");
    if (traceId == null) traceId = "unknown";

    Map<String, Object> body = Map.of(
        "type", ERROR_TYPE_BASE + "forbidden",
        "title", "Forbidden",
        "status", status.value(),
        "code", code,
        "detail", detail,
        "traceId", traceId);

    objectMapper.writeValue(response.getOutputStream(), body);
  }
}
