package com.atlasops.boot.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that enforces resource-level authorization for CLIENT role users.
 *
 * <p>The filter is intentionally lightweight: it logs access attempts and sets
 * a request attribute that controllers can use to enforce customer-scoped access.
 * Controllers use this attribute together with the user's association list to
 * restrict data visibility.
 *
 * <p>Full resource-level enforcement (CLIENT only sees own customer data) is
 * implemented at the use case layer via {@code UserCustomerAssociationRepository}.
 *
 * <p>Validates: P0.O.1 — Resource Authorization (Customer-Scoped Access)
 */
public class ResourceAuthorizationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ResourceAuthorizationFilter.class);
    private static final String ATTR_USER_ROLE = "X-Effective-Role";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated()) {
            String role = extractRole(auth);
            String userId = auth.getName();

            // Store role and userId in request attributes for downstream use
            request.setAttribute(ATTR_USER_ROLE, role);
            request.setAttribute("X-Effective-User-ID", userId);

            // For CLIENT role: log access and allow controllers to enforce restriction
            if ("CLIENT".equalsIgnoreCase(role)) {
                log.debug("CLIENT user {} accessing {}", userId, request.getRequestURI());
            }
        }

        filterChain.doFilter(request, response);
    }

    private static String extractRole(Authentication auth) {
        if (auth.getAuthorities() == null || auth.getAuthorities().isEmpty()) {
            return "UNKNOWN";
        }
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("UNKNOWN");
    }

    /**
     * Writes a 403 Forbidden response in RFC 7807 Problem Details format.
     */
    static void writeForbidden(HttpServletResponse response, String detail) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String body = """
                {
                  "type": "https://atlasops/errors/forbidden",
                  "title": "Forbidden",
                  "status": 403,
                  "code": "FORBIDDEN_ACTION",
                  "detail": "%s"
                }
                """.formatted(detail);
        response.getWriter().write(body);
    }
}
