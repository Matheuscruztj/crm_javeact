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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Integration tests for the authentication endpoints.
 *
 * <p>Validates: P0.A.1.2 — Auth integration tests: login, refresh, revoke
 */
@Tag("integration")
class AuthIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void should_returnUnauthorized_when_loginWithInvalidCredentials() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = """
                {"email":"noone@nowhere.com","password":"wrong"}
                """;
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/auth/login",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class);

        assertThat(response.getStatusCode()).isIn(
                HttpStatus.UNAUTHORIZED, HttpStatus.BAD_REQUEST, HttpStatus.NOT_FOUND);
    }

    @Test
    void should_returnMethodNotAllowed_when_loginWithGetMethod() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/auth/login", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Test
    void should_returnUnauthorized_when_refreshWithoutToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/auth/refresh",
                HttpMethod.POST,
                new HttpEntity<>("{}", headers),
                String.class);

        assertThat(response.getStatusCode()).isIn(
                HttpStatus.UNAUTHORIZED, HttpStatus.BAD_REQUEST);
    }

    @Test
    void should_returnUnauthorized_when_revokeWithoutToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", "tenant-alpha");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/auth/revoke",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
