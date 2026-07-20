package com.atlasops.integrations.infrastructure;

import java.net.InetAddress;
import java.net.URI;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SSRF (Server-Side Request Forgery) protection utility.
 *
 * <p>Validates outbound HTTP target URLs against known attack vectors:
 * <ul>
 *   <li>Loopback addresses (127.x.x.x, ::1)
 *   <li>Private network ranges (10.x.x.x, 172.16–31.x.x, 192.168.x.x)
 *   <li>Link-local addresses (169.254.x.x)
 *   <li>Cloud metadata endpoints (169.254.169.254, 100.100.100.200 for Alibaba)
 *   <li>Non-HTTP/HTTPS schemes (file://, ftp://, gopher://, etc.)
 * </ul>
 *
 * <p>Validates: P0.J.1 — SSRF Protection Utility
 */
public final class SSRFValidator {

  private static final Logger log = LoggerFactory.getLogger(SSRFValidator.class);

  /** Cloud metadata endpoint IPs to block explicitly. */
  private static final Set<String> BLOCKED_CLOUD_METADATA_IPS = Set.of(
      "169.254.169.254",  // AWS/GCP/Azure instance metadata
      "100.100.100.200"   // Alibaba Cloud metadata
  );

  /** Allowed URI schemes. */
  private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

  private SSRFValidator() {}

  /**
   * Validates a target URL is safe for outbound HTTP requests.
   *
   * @param targetUrl the URL to validate
   * @throws IllegalArgumentException if the URL is null, blank, or malformed
   * @throws SecurityException        if the URL targets a blocked address
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
    if (scheme == null || !ALLOWED_SCHEMES.contains(scheme.toLowerCase())) {
      throw new SecurityException(
          "SSRF blocked: only HTTP/HTTPS allowed, got scheme: " + scheme);
    }

    String host = uri.getHost();
    if (host == null || host.isBlank()) {
      throw new IllegalArgumentException("URL must contain a valid host: " + targetUrl);
    }

    InetAddress address;
    try {
      address = InetAddress.getByName(host);
    } catch (Exception e) {
      throw new IllegalArgumentException("Cannot resolve host: " + host, e);
    }

    validateResolvedAddress(address);
  }

  /**
   * Checks a resolved InetAddress against all blocked categories.
   */
  static void validateResolvedAddress(InetAddress address) {
    String ip = address.getHostAddress();

    if (address.isLoopbackAddress()) {
      throw new SecurityException("SSRF blocked: loopback address not allowed — " + ip);
    }

    if (address.isSiteLocalAddress()) {
      throw new SecurityException("SSRF blocked: private network address not allowed — " + ip);
    }

    if (address.isLinkLocalAddress()) {
      throw new SecurityException("SSRF blocked: link-local address not allowed — " + ip);
    }

    if (address.isMulticastAddress()) {
      throw new SecurityException("SSRF blocked: multicast address not allowed — " + ip);
    }

    if (address.isAnyLocalAddress()) {
      throw new SecurityException("SSRF blocked: any-local address not allowed — " + ip);
    }

    if (BLOCKED_CLOUD_METADATA_IPS.contains(ip)) {
      throw new SecurityException("SSRF blocked: cloud metadata endpoint not allowed — " + ip);
    }

    log.debug("SSRF check passed for IP: {}", ip);
  }

  /**
   * Returns true if the URL is safe; false if any SSRF check fails.
   * Convenience method for non-throwing usage.
   */
  public static boolean isSafe(String targetUrl) {
    try {
      validate(targetUrl);
      return true;
    } catch (SecurityException | IllegalArgumentException e) {
      log.debug("SSRF unsafe URL: {} — {}", targetUrl, e.getMessage());
      return false;
    }
  }
}
