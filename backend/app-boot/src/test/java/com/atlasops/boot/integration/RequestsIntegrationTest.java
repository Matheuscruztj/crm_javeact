package com.atlasops.boot.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlasops.auth.domain.Role;
import com.atlasops.boot.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Integration tests for requests lifecycle endpoints.
 *
 * <p>Validates: P0.A.1.7 — Requests integration tests: lifecycle, status transitions
 */
class RequestsIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void should_requireAuth_when_creatingRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", "tenant-alpha");
        headers.set("Content-Type", "application/json");

        String body = """
                {"title":"Test Request","description":"A test service request","customerId":"cust-001"}
                """;

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/requests",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_requireAuth_when_listingRequests() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", "tenant-alpha");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/requests",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_requireAuth_when_updatingRequestStatus() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", "tenant-alpha");
        headers.set("Content-Type", "application/json");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/requests/req-id/status",
                HttpMethod.PATCH,
                new HttpEntity<>("{\"status\":\"IN_REVIEW\"}", headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_requireAuth_when_getRequestCrossTenant() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", "tenant-beta");
        headers.set("Authorization", "Bearer fake-token-for-alpha");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/requests",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isIn(
                HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
    }

    @Test
    void should_rejectCrossTenantAccess_when_creatingRequestWithValidJwt() {
        HttpHeaders headers =
                authenticatedHeaders("user-alpha", "tenant-alpha", Role.ADMIN, "tenant-beta");
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        String body = """
                {"title":"Cross Tenant Request","description":"Should be blocked","customerId":"cust-001"}
                """;

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/requests",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void should_rejectCrossTenantAccess_when_listingRequestsWithValidJwt() {
        HttpHeaders headers =
                authenticatedHeaders("user-alpha", "tenant-alpha", Role.ADMIN, "tenant-beta");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/requests",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
