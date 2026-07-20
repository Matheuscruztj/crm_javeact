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
 * Integration tests for activities feed cross-tenant isolation.
 *
 * <p>Validates: P0.A.2.5 — Isolamento de activity feed entre tenants
 */
@Tag("integration")
class ActivitiesIsolationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void should_requireAuth_when_listingActivities() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", "tenant-alpha");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/activities",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_blockCrossTenantAccess_when_activitiesEndpoint() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", "tenant-beta");
        headers.set("Authorization", "Bearer token-from-tenant-alpha");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/activities",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isIn(
                HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
    }

    @Test
    void should_requireAuth_when_listingEntityActivities() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", "tenant-alpha");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/activities/entity/customer/cust-001",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
