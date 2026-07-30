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
 * Integration tests for user management endpoints.
 *
 * <p>Validates: P0.A.1.4 — Users integration tests: user CRUD, role assignment
 */
class UsersIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void should_returnUnauthorized_when_listingUsersWithoutToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", "tenant-alpha");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/users",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_returnUnauthorized_when_creatingUserWithoutToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", "tenant-alpha");
        headers.set("Content-Type", "application/json");

        String body = """
                {"email":"user@test.com","role":"CLIENT","name":"Test User"}
                """;

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/users",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_returnUnauthorized_when_assigningRoleWithoutToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", "tenant-alpha");
        headers.set("Content-Type", "application/json");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/users/user-id/role",
                HttpMethod.PUT,
                new HttpEntity<>("{\"role\":\"ANALYST\"}", headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_rejectCrossTenantAccess_when_userCreateUsesValidJwt() {
        HttpHeaders headers =
                authenticatedHeaders("user-alpha", "tenant-alpha", Role.ADMIN, "tenant-beta");
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        String body = """
                {"email":"user@tenant-beta.test","role":"CLIENT","name":"Tenant Beta User"}
                """;

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/users",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void should_rejectCrossTenantAccess_when_listingUsersWithValidJwt() {
        HttpHeaders headers =
                authenticatedHeaders("user-alpha", "tenant-alpha", Role.ADMIN, "tenant-beta");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/users",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
