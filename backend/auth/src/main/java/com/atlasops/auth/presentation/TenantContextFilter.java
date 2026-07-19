package com.atlasops.auth.presentation;

import com.atlasops.auth.domain.TenantContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Spring Security filter that validates the X-Tenant-ID header against the JWT tenant claim.
 *
 * <p>This filter runs after {@link JwtAuthenticationFilter} and performs the following checks on
 * authenticated requests:
 *
 * <ul>
 *   <li>If the X-Tenant-ID header is absent: returns 400 Bad Request
 *   <li>If X-Tenant-ID does not match the JWT tenant claim: returns 403 Forbidden
 *   <li>On success: stores the validated tenant ID in {@link TenantContext} thread-local
 * </ul>
 *
 * <p>Unauthenticated requests (no SecurityContext) pass through without validation.
 *
 * <p>Validates: Requirements 2.5, 2.6, 2.8
 */
public class TenantContextFilter extends OncePerRequestFilter {

  private static final Logger log = LoggerFactory.getLogger(TenantContextFilter.class);
  private static final String TENANT_HEADER = "X-Tenant-ID";
  private static final String ERROR_TYPE_BASE = "https://atlasops/errors/";

  private final ObjectMapper objectMapper;

  public TenantContextFilter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

      if (authentication == null
          || !authentication.isAuthenticated()
          || !(authentication.getPrincipal() instanceof AuthenticatedPrincipal principal)) {
        // Not authenticated — skip tenant validation
        filterChain.doFilter(request, response);
        return;
      }

      String tenantHeader = request.getHeader(TENANT_HEADER);

      if (tenantHeader == null || tenantHeader.isBlank()) {
        log.warn(
            "Missing X-Tenant-ID header for authenticated request: {}", request.getRequestURI());
        writeErrorResponse(
            response,
            HttpStatus.BAD_REQUEST,
            "BAD_REQUEST",
            "Missing required header: X-Tenant-ID");
        return;
      }

      String trimmedTenantHeader = tenantHeader.trim();
      String jwtTenantId = principal.tenantId();

      if (!trimmedTenantHeader.equals(jwtTenantId)) {
        log.warn(
            "Tenant mismatch: header='{}' vs jwt='{}' for user='{}'",
            trimmedTenantHeader,
            jwtTenantId,
            principal.userId());
        writeErrorResponse(
            response, HttpStatus.FORBIDDEN, "FORBIDDEN_ACTION", "Tenant context mismatch");
        return;
      }

      TenantContext.setTenantId(trimmedTenantHeader);
      filterChain.doFilter(request, response);
    } finally {
      TenantContext.clear();
    }
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

    String errorType =
        status == HttpStatus.BAD_REQUEST
            ? ERROR_TYPE_BASE + "bad-request"
            : ERROR_TYPE_BASE + "forbidden-action";

    Map<String, Object> body =
        Map.of(
            "type", errorType,
            "title", status == HttpStatus.BAD_REQUEST ? "Bad Request" : "Forbidden",
            "status", status.value(),
            "code", code,
            "detail", detail,
            "traceId", traceId);

    objectMapper.writeValue(response.getOutputStream(), body);
  }
}
