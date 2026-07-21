package com.atlasops.integrations.domain.ports;

/**
 * Port defining the contract for outbound URL safety validation (SSRF protection).
 * Implementations check target URLs against blocked address ranges and schemes.
 *
 * <p>Validates: P0.J.1 — SSRF Protection
 */
public interface UrlValidationPort {

    /**
     * Returns {@code true} if the URL is safe for outbound HTTP requests;
     * {@code false} if any SSRF check fails (private ranges, loopback, etc.).
     *
     * @param url the target URL to validate
     * @return {@code true} if safe; {@code false} otherwise
     */
    boolean isSafe(String url);
}
