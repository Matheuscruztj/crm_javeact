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
 * Integration tests for approvals workflow.
 *
 * <p>Validates: P0.A.1.8 — Approvals integration tests: approval workflow
 * Validates: P0.A.2.4 — Isolamento de approvals entre tenants
 */
class ApprovalsIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void should_requireAuth_when_creatingApproval() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", "tenant-alpha");
        headers.set("Content-Type", "application/json");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/approvals",
                HttpMethod.POST,
                new HttpEntity<>("{\"documentId\":\"doc-001\"}", headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_requireAuth_when_approvingDocument() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", "tenant-alpha");
        headers.set("Content-Type", "application/json");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/approvals/approval-id/approve",
                HttpMethod.POST,
                new HttpEntity<>("{}", headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_requireAuth_when_rejectingDocument() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", "tenant-alpha");
        headers.set("Content-Type", "application/json");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/approvals/approval-id/reject",
                HttpMethod.POST,
                new HttpEntity<>("{\"reason\":\"Not compliant with policy\"}", headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void should_rejectCrossTenantAccess_when_usingMismatchedTenantHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", "tenant-beta");
        headers.set("Authorization", "Bearer token-from-tenant-alpha");

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/approvals",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertThat(response.getStatusCode()).isIn(
                HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
    }

    @Test
    void should_rejectCrossTenantAccess_when_approvalCreationUsesValidJwt() {
        HttpHeaders headers =
                authenticatedHeaders("user-alpha", "tenant-alpha", Role.ADMIN, "tenant-beta");
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/approvals",
                HttpMethod.POST,
                new HttpEntity<>("{\"documentId\":\"doc-001\"}", headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void should_rejectCrossTenantAccess_when_approvingApprovalWithValidJwt() {
        HttpHeaders headers =
                authenticatedHeaders("user-alpha", "tenant-alpha", Role.ADMIN, "tenant-beta");
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/approvals/approval-id/approve",
                HttpMethod.POST,
                new HttpEntity<>("{}", headers),
                String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
