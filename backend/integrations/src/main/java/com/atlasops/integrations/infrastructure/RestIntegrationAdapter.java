package com.atlasops.integrations.infrastructure;

import com.atlasops.integrations.domain.DispatchResult;
import com.atlasops.integrations.domain.WebhookPayload;
import com.atlasops.integrations.domain.ports.IntegrationPort;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * REST adapter for dispatching webhooks with SSRF protection.
 *
 * <p>Blocks requests to loopback addresses, private network ranges and cloud metadata endpoints
 * to prevent Server-Side Request Forgery (SSRF) attacks.
 *
 * <p>Validates: P0.C.1.1 — RESTIntegrationAdapter with SSRF validation
 */
@Component
public class RestIntegrationAdapter implements IntegrationPort {

    private static final Logger log = LoggerFactory.getLogger(RestIntegrationAdapter.class);

    private final HttpClient httpClient;

    public RestIntegrationAdapter() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public DispatchResult dispatch(WebhookPayload payload) {
        String dispatchId = UUID.randomUUID().toString();

        validateSsrf(payload.targetUrl());

        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(payload.targetUrl()))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("X-Event-Type", payload.eventType())
                    .header("X-Dispatch-ID", dispatchId)
                    .POST(BodyPublishers.ofString(payload.payload() != null ? payload.payload() : "{}"));

            HttpResponse<String> response = httpClient.send(
                    requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

            String status = response.statusCode() >= 200 && response.statusCode() < 300
                    ? "DELIVERED" : "FAILED";

            log.info("Webhook dispatch {} to {}: {} (HTTP {})",
                    dispatchId, payload.targetUrl(), status, response.statusCode());

            return new DispatchResult(dispatchId, status, response.statusCode());

        } catch (Exception e) {
            log.error("Webhook dispatch {} to {} failed: {}", dispatchId, payload.targetUrl(), e.getMessage());
            return new DispatchResult(dispatchId, "FAILED", -1);
        }
    }

    /**
     * Validates a URL against SSRF attack vectors.
     * Blocks: loopback (127.x), private ranges (10.x, 172.16-31.x, 192.168.x),
     * link-local (169.254.x), and metadata endpoints.
     */
    private void validateSsrf(String targetUrl) {
        if (targetUrl == null || targetUrl.isBlank()) {
            throw new IllegalArgumentException("Target URL must not be blank");
        }

        try {
            URI uri = URI.create(targetUrl);
            String scheme = uri.getScheme();
            if (!"https".equalsIgnoreCase(scheme) && !"http".equalsIgnoreCase(scheme)) {
                throw new SecurityException("Only HTTP/HTTPS schemes are allowed, got: " + scheme);
            }

            String host = uri.getHost();
            InetAddress address = InetAddress.getByName(host);

            if (address.isLoopbackAddress()) {
                throw new SecurityException("SSRF blocked: loopback address not allowed");
            }
            if (address.isSiteLocalAddress()) {
                throw new SecurityException("SSRF blocked: private network address not allowed");
            }
            if (address.isLinkLocalAddress()) {
                throw new SecurityException("SSRF blocked: link-local address not allowed");
            }

            // Block cloud metadata endpoints
            String rawAddress = address.getHostAddress();
            if (rawAddress.startsWith("169.254.") || rawAddress.equals("100.100.100.200")) {
                throw new SecurityException("SSRF blocked: cloud metadata endpoint not allowed");
            }

        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid target URL: " + e.getMessage());
        }
    }
}
