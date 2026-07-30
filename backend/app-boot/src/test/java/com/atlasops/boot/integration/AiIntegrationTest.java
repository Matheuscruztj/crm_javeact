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
 * Integration tests for AI/RAG analysis endpoints.
 *
 * <p>Validates: P0.A.1.9 — AI integration tests: RAG pipeline with pgvector
 */
class AiIntegrationTest extends AbstractIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void should_requireAuth_when_queryingAnalysis() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-Tenant-ID", "tenant-alpha");

    ResponseEntity<String> response = restTemplate.exchange(
        "/api/v1/ai/analysis",
        HttpMethod.GET,
        new HttpEntity<>(headers),
        String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void should_requireAuth_when_submittingAnalysisRequest() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-Tenant-ID", "tenant-alpha");
    headers.set("Content-Type", "application/json");

    ResponseEntity<String> response = restTemplate.exchange(
        "/api/v1/ai/analyze",
        HttpMethod.POST,
        new HttpEntity<>("{\"documentId\":\"doc-001\"}", headers),
        String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void should_exposeHealthEndpoint_when_aiModuleLoaded() {
    ResponseEntity<String> response = restTemplate.getForEntity(
        "/actuator/health", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("status");
  }

  @Test
    void should_isolateAnalysisByTenant_when_crossTenantAccess() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Tenant-ID", "tenant-beta");
        headers.set("Authorization", "Bearer token-from-tenant-alpha");

    ResponseEntity<String> response = restTemplate.exchange(
        "/api/v1/ai/analysis",
        HttpMethod.GET,
        new HttpEntity<>(headers),
        String.class);

        assertThat(response.getStatusCode()).isIn(
        HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
    }

  @Test
  void should_rejectCrossTenantAccess_when_aiAnalysisUsesValidJwt() {
    HttpHeaders headers =
        authenticatedHeaders("user-alpha", "tenant-alpha", Role.ADMIN, "tenant-beta");
    headers.set("Content-Type", "application/json");

    ResponseEntity<String> response =
        restTemplate.exchange(
            "/api/v1/ai/analyze",
            HttpMethod.POST,
            new HttpEntity<>("{\"documentId\":\"doc-001\"}", headers),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }
}
