package com.atlasops.integrations.infrastructure;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

/**
 * Utility class for validating URLs against Server-Side Request Forgery (SSRF) attack vectors.
 *
 * <p>Blocks requests to:
 * <ul>
 *   <li>Loopback addresses (127.x, ::1)
 *   <li>Private network ranges (10.x, 172.16-31.x, 192.168.x)
 *   <li>Link-local addresses (169.254.x)
 *   <li>Cloud metadata endpoints (169.254.169.254, 100.100.100.200)
 *   <li>Non-HTTP(S) schemes
 * </ul>
 *
 * <p>Validates: P0.J.1 — SSRF Protection Utility
 */
public final class SSRFValidator {

    private SSRFValidator() {}

    /**
     * Validates that the given URL is safe to make an outbound HTTP request to.
     *
     * @param targetUrl the URL to validate
     * @throws IllegalArgumentException if the URL is null, blank, or has an invalid format
     * @throws SecurityException        if the URL targets a blocked address (SSRF risk)
     */
    public static void validate(String targetUrl) {
        if (targetUrl == null || targetUrl.isBlank()) {
            throw new IllegalArgumentException("Target URL must not be blank");
        }

        URI uri;
        try {
            uri = URI.create(targetUrl);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Malformed URL: " + targetUrl, e);
        }

        String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            throw new SecurityException("SSRF blocked: only HTTP/HTTPS schemes are allowed, got: " + scheme);
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("URL has no valid host: " + targetUrl);
        }

        InetAddress address;
        try {
            address = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Cannot resolve host: " + host, e);
        }

        if (address.isLoopbackAddress()) {
            throw new SecurityException("SSRF blocked: loopback address not allowed: " + host);
        }

        if (address.isSiteLocalAddress()) {
            throw new SecurityException("SSRF blocked: private network address not allowed: " + host);
        }

        if (address.isLinkLocalAddress()) {
            throw new SecurityException("SSRF blocked: link-local address not allowed: " + host);
        }

        String rawIp = address.getHostAddress();
        // Block cloud metadata endpoints
        if (rawIp.startsWith("169.254.") || rawIp.equals("100.100.100.200")) {
            throw new SecurityException("SSRF blocked: cloud metadata endpoint not allowed: " + rawIp);
        }

        // Block multicast
        if (address.isMulticastAddress()) {
            throw new SecurityException("SSRF blocked: multicast address not allowed: " + host);
        }
    }

    /**
     * Returns true if the URL is safe for outbound requests; false otherwise.
     * Silently swallows exceptions — use {@link #validate(String)} for explicit error handling.
     */
    public static boolean isSafe(String targetUrl) {
        try {
            validate(targetUrl);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
