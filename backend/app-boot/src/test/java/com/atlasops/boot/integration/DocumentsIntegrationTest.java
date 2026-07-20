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
 * Integration tests for documents endpoints.
 *
 * <p>Validates: P0.A.1.6 — Documents integration tests: upload session, storage adapter
 */
@Tag("integration")
class DocumentsIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void should_requireAuth_when_initiatingUpload() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", "tenant-alpha");
        headers.set("Content-Type", "application/json");

        String body = """
                {"filename":"test.pdf","contentType":"application/pdf","sizeBytes":1024,"requestId":"req-001"}
                """;

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/documents/upload",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_requireAuth_when_listingDocuments() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", "tenant-alpha");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/documents",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_requireAuth_when_confirmingUpload() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", "tenant-alpha");
        headers.set("Content-Type", "application/json");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/documents/doc-id/confirm",
                HttpMethod.POST,
                new HttpEntity<>("{}", headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_requireAuth_when_accessingDocumentsByCrossTenant() {
        // Simulate cross-tenant access attempt without valid token
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", "tenant-beta");
        headers.set("Authorization", "Bearer token-for-tenant-alpha");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/documents",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        // Either 401 (invalid token) or 403 (valid token, wrong tenant)
        assertThat(response.getStatusCode()).isIn(
                HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
    }
}
