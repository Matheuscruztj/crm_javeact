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
 * Integration test: verifies tenant isolation at the API layer.
 * Validates: P0.A.1 - Testcontainers integration tests setup
 */
class TenantIsolationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void should_returnUnauthorized_when_noAuthHeader() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", "tenant-alpha");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/customers",
                HttpMethod.GET,
                request,
                String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_returnOk_when_actuatorHealthEndpoint() {
        // Act
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/actuator/health", String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void should_returnForbidden_when_tenantHeaderDoesNotMatchJwt() {
        HttpHeaders headers =
                authenticatedHeaders("user-alpha", "tenant-alpha", Role.ADMIN, "tenant-beta");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/customers",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
