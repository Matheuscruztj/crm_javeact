package com.atlasops.boot.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that enforces tenant maintenance mode. When the Redis key
 * {@code maintenance:tenant:{tenantId}} exists with value "true", all mutating
 * HTTP methods (POST, PUT, PATCH, DELETE) return 503 with RFC 7807 body.
 *
 * <p>GET requests always pass through. Actuator paths are bypassed.
 * Users with SUPER_ADMIN role bypass maintenance mode.
 *
 * <p>Validates: P2.11 — Tenant read-only maintenance mode
 */
public class MaintenanceModeFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceModeFilter.class);
    private static final Set<String> MUTATING_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final Set<String> BYPASS_ROLES = Set.of("SUPER_ADMIN");
    private static final String MAINTENANCE_KEY_PREFIX = "maintenance:tenant:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public MaintenanceModeFilter(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String method = request.getMethod().toUpperCase();

        // Only check mutating operations
        if (!MUTATING_METHODS.contains(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract tenant ID from header (same header pattern used throughout)
        String tenantId = request.getHeader("X-Tenant-ID");
        if (tenantId == null || tenantId.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check for SUPER_ADMIN bypass (simple header check — actual auth validation done elsewhere)
        String rolesHeader = request.getHeader("X-User-Roles");
        if (rolesHeader != null) {
            for (String role : BYPASS_ROLES) {
                if (rolesHeader.contains(role)) {
                    filterChain.doFilter(request, response);
                    return;
                }
            }
        }

        // Check Redis maintenance flag
        if (isMaintenanceActive(tenantId)) {
            log.info("Blocking mutating request {} {} — tenant '{}' is in maintenance mode",
                    method, request.getRequestURI(), tenantId);
            sendMaintenanceResponse(response, tenantId, request.getRequestURI());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isMaintenanceActive(String tenantId) {
        try {
            String value = redisTemplate.opsForValue().get(MAINTENANCE_KEY_PREFIX + tenantId);
            return "true".equalsIgnoreCase(value);
        } catch (Exception e) {
            log.debug("Could not check maintenance flag for tenant '{}': {}", tenantId, e.getMessage());
            return false;
        }
    }

    private void sendMaintenanceResponse(
            HttpServletResponse response, String tenantId, String path) throws IOException {
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        response.setContentType("application/problem+json");
        response.setHeader("Retry-After", "300");

        Map<String, Object> body = Map.of(
                "type", "https://atlasops/errors/maintenance-mode",
                "title", "Service Temporarily Unavailable",
                "status", 503,
                "code", "MAINTENANCE_MODE",
                "detail", "Tenant is in maintenance mode. Only read operations are allowed.",
                "traceId", tenantId + "-maintenance",
                "timestamp", Instant.now().toString());

        objectMapper.writeValue(response.getWriter(), body);
    }
}
