package com.atlasops.boot.filter;

import com.atlasops.boot.presentation.ProblemDetailResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Idempotency filter that enforces deduplication for POST requests using
 * the {@code Idempotency-Key} header.
 *
 * <ul>
 *   <li>Stores key in Redis with a 24-hour TTL
 *   <li>Returns 409 Conflict if the same key is submitted twice within the TTL
 * </ul>
 *
 * <p>Validates: P0.E.1 — Idempotency-Key header implementation
 */
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyFilter.class);

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    private static final String REDIS_PREFIX = "idempotency:";
    private static final Duration TTL = Duration.ofHours(24);

    /** Paths where idempotency enforcement applies. */
    private static final Set<String> IDEMPOTENCY_PATHS = Set.of(
            "/api/v1/requests",
            "/api/v1/customers",
            "/api/v1/approvals"
    );

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public IdempotencyFilter(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Only enforce on POST requests to configured paths
        if (!HttpMethod.POST.matches(request.getMethod()) || !isIdempotencyPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);

        // No header provided — allow request through (idempotency is opt-in)
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        String redisKey = REDIS_PREFIX + idempotencyKey;

        // Check if already processed
        Boolean exists = redisTemplate.hasKey(redisKey);
        if (Boolean.TRUE.equals(exists)) {
            log.warn("Duplicate request detected for Idempotency-Key: {}", idempotencyKey);
            writeConflict(response, idempotencyKey);
            return;
        }

        // Store key before processing
        redisTemplate.opsForValue().set(redisKey, "1", TTL);

        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            // On failure, remove the key so the client can retry
            redisTemplate.delete(redisKey);
            throw e;
        }
    }

    private boolean isIdempotencyPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        return IDEMPOTENCY_PATHS.stream().anyMatch(path::startsWith);
    }

    private void writeConflict(HttpServletResponse response, String key) throws IOException {
        response.setStatus(HttpStatus.CONFLICT.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ProblemDetailResponse problem = new ProblemDetailResponse(
                "https://atlasops/errors/duplicate-request",
                "Duplicate Request",
                HttpStatus.CONFLICT.value(),
                "DUPLICATE_REQUEST",
                "A request with Idempotency-Key '" + key + "' was already processed.",
                null);
        response.getWriter().write(objectMapper.writeValueAsString(problem));
    }
}
