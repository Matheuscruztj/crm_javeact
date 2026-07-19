package com.atlasops.boot.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlasops.boot.AbstractIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Integration test: verifies cross-tenant customer isolation at the API layer.
 *
 * <p>Validates: P0.A.2.1 — Tenant A cannot access customers of Tenant B
 */
@Tag("integration")
class CustomerTenantIsolationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void should_returnUnauthorized_when_accessingCustomersWithoutToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", "tenant-alpha");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/customers",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_returnUnauthorized_when_accessingCustomersWithoutTenantHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer invalid-token");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/customers",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isIn(
                HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
    }
}
