package com.atlasops.boot.filter;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import net.jqwik.api.*;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Property-based tests for Correlation ID propagation through the HTTP filter.
 *
 * <p><b>Validates: Requirements 3.11, 11.4, 11.5</b>
 *
 * <p>Property 3: For any HTTP request received by the Backend API, if the {@code X-Correlation-ID}
 * header is present the system SHALL reuse that value, otherwise it SHALL generate a valid UUID v4;
 * in both cases the correlation ID SHALL appear in the MDC context, all log entries for that
 * request, and the {@code X-Correlation-ID} response header.
 */
@Tag("Feature: monorepo-sdd-harness, Property 3: Correlation ID Propagation")
class CorrelationIdFilterPropertyTest {

  private final CorrelationIdFilter filter = new CorrelationIdFilter();

  // ─── Property: When X-Correlation-ID header is present, the SAME value appears in response ───

  @Property(tries = 100)
  void providedCorrelationId_shouldBeReusedInResponse(
      @ForAll("validCorrelationIds") String providedId) throws ServletException, IOException {

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, providedId);

    AtomicReference<String> mdcValue = new AtomicReference<>();
    FilterChain chain =
        (req, res) -> mdcValue.set(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID_KEY));

    filter.doFilterInternal(request, response, chain);

    String expectedId = providedId.trim();

    assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER))
        .as("Response header should contain the same correlation ID provided in the request")
        .isEqualTo(expectedId);

    assertThat(mdcValue.get())
        .as("MDC should contain the same correlation ID provided in the request")
        .isEqualTo(expectedId);
  }

  // ─── Property: When X-Correlation-ID header is absent, a valid UUID v4 is generated ──────────

  @Property(tries = 100)
  void absentCorrelationId_shouldGenerateValidUuidV4InResponse(
      @ForAll("httpMethods") String httpMethod, @ForAll("requestUris") String requestUri)
      throws ServletException, IOException {

    MockHttpServletRequest request = new MockHttpServletRequest(httpMethod, requestUri);
    MockHttpServletResponse response = new MockHttpServletResponse();
    // No X-Correlation-ID header set

    AtomicReference<String> mdcValue = new AtomicReference<>();
    FilterChain chain =
        (req, res) -> mdcValue.set(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID_KEY));

    filter.doFilterInternal(request, response, chain);

    String responseHeaderValue = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);

    // Must be a valid UUID v4
    assertThat(responseHeaderValue)
        .as("Generated correlation ID must be non-null and non-blank")
        .isNotNull()
        .isNotBlank();

    UUID parsed = UUID.fromString(responseHeaderValue);
    assertThat(parsed.version())
        .as("Generated correlation ID must be a UUID version 4")
        .isEqualTo(4);

    // MDC must match response header
    assertThat(mdcValue.get())
        .as("MDC correlationId must match the response header value")
        .isEqualTo(responseHeaderValue);
  }

  // ─── Property: Correlation ID is ALWAYS present in response regardless of input ──────────────

  @Property(tries = 100)
  void correlationId_shouldAlwaysBeInResponse(@ForAll("optionalCorrelationIds") String headerValue)
      throws ServletException, IOException {

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();

    if (headerValue != null && !headerValue.isEmpty()) {
      request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, headerValue);
    }

    FilterChain chain =
        (req, res) -> {
          /* no-op */
        };

    filter.doFilterInternal(request, response, chain);

    String responseId = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);

    assertThat(responseId)
        .as("Correlation ID must always be present in the response header")
        .isNotNull()
        .isNotBlank();
  }

  // ─── Property: Generated/propagated ID is always a valid UUID v4 format ──────────────────────

  @Property(tries = 100)
  void generatedCorrelationId_shouldAlwaysBeValidUuidV4(@ForAll("httpMethods") String httpMethod)
      throws ServletException, IOException {

    MockHttpServletRequest request = new MockHttpServletRequest(httpMethod, "/api/test");
    MockHttpServletResponse response = new MockHttpServletResponse();
    // No correlation ID header → forces generation

    FilterChain chain =
        (req, res) -> {
          /* no-op */
        };

    filter.doFilterInternal(request, response, chain);

    String responseId = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);

    // Must parse as UUID
    assertThat(responseId).isNotNull();
    UUID parsed = UUID.fromString(responseId);

    // Must be version 4
    assertThat(parsed.version()).as("Every generated correlation ID must be UUID v4").isEqualTo(4);

    // Must be variant 2 (RFC 4122)
    assertThat(parsed.variant())
        .as("Every generated correlation ID must be RFC 4122 variant")
        .isEqualTo(2);
  }

  // ─── Generators ──────────────────────────────────────────────────────────────────────────────

  @Provide
  Arbitrary<String> validCorrelationIds() {
    // Generate valid UUID v4 strings that simulate realistic correlation IDs
    return Arbitraries.oneOf(
        // UUID v4 format (most common case)
        Arbitraries.create(() -> UUID.randomUUID().toString()),
        // Custom correlation ID strings (non-UUID but valid headers)
        Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('0', '9')
            .withChars('-', '_')
            .ofMinLength(1)
            .ofMaxLength(64)
            .filter(s -> !s.isBlank()));
  }

  @Provide
  Arbitrary<String> httpMethods() {
    return Arbitraries.of("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS");
  }

  @Provide
  Arbitrary<String> requestUris() {
    return Arbitraries.of(
        "/api/v1/users",
        "/api/v1/customers/123",
        "/api/v1/documents",
        "/actuator/health",
        "/api/v1/tenants/abc-def/requests",
        "/api/v1/ai/analyze");
  }

  @Provide
  Arbitrary<String> optionalCorrelationIds() {
    // Mix of scenarios: present valid IDs, empty, null-like, blank
    return Arbitraries.oneOf(
        // Valid UUID correlation IDs
        Arbitraries.create(() -> UUID.randomUUID().toString()),
        // Custom non-blank string IDs
        Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('0', '9')
            .withChars('-')
            .ofMinLength(1)
            .ofMaxLength(36)
            .filter(s -> !s.isBlank()),
        // Empty/blank (triggers generation)
        Arbitraries.of("", " ", "  "));
  }
}
