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
 * <p><b>Validates: Requirements 27.1, 27.2, 27.3, 27.4, 27.5</b>
 *
 * <p>Property 25: For any HTTP request received by the Backend API, if the {@code X-Correlation-ID}
 * header is present with a valid UUID, the system SHALL reuse that value; otherwise it SHALL
 * generate a valid UUID v4. In both cases the correlation ID SHALL appear in the MDC context and
 * the {@code X-Correlation-ID} response header.
 */
@Tag("Feature: project-implementation-kickoff, Property 25: Correlation ID Propagation")
class CorrelationIdFilterPropertyTest {

  private final CorrelationIdFilter filter = new CorrelationIdFilter();

  /**
   * Property: When X-Correlation-ID header is present with a valid UUID, the SAME value appears in
   * response and MDC.
   */
  @Property(tries = 100)
  void should_reuseCorrelationId_when_headerContainsValidUuid(
      @ForAll("validUuidCorrelationIds") String providedId) throws ServletException, IOException {

    MDC.clear();
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

  /** Property: When X-Correlation-ID header is absent, a valid UUID v4 is generated. */
  @Property(tries = 100)
  void should_generateValidUuidV4_when_headerAbsent(
      @ForAll("httpMethods") String httpMethod, @ForAll("requestUris") String requestUri)
      throws ServletException, IOException {

    MDC.clear();
    MockHttpServletRequest request = new MockHttpServletRequest(httpMethod, requestUri);
    MockHttpServletResponse response = new MockHttpServletResponse();

    AtomicReference<String> mdcValue = new AtomicReference<>();
    FilterChain chain =
        (req, res) -> mdcValue.set(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID_KEY));

    filter.doFilterInternal(request, response, chain);

    String responseHeaderValue = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);

    assertThat(responseHeaderValue)
        .as("Generated correlation ID must be non-null and non-blank")
        .isNotNull()
        .isNotBlank();

    UUID parsed = UUID.fromString(responseHeaderValue);
    assertThat(parsed.version())
        .as("Generated correlation ID must be a UUID version 4")
        .isEqualTo(4);

    assertThat(mdcValue.get())
        .as("MDC correlationId must match the response header value")
        .isEqualTo(responseHeaderValue);
  }

  /**
   * Property: When X-Correlation-ID header has an invalid (non-UUID) value, a new UUID v4 is
   * generated instead.
   */
  @Property(tries = 100)
  void should_generateNewUuid_when_headerContainsInvalidFormat(
      @ForAll("invalidCorrelationIds") String invalidId) throws ServletException, IOException {

    MDC.clear();
    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, invalidId);

    AtomicReference<String> mdcValue = new AtomicReference<>();
    FilterChain chain =
        (req, res) -> mdcValue.set(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID_KEY));

    filter.doFilterInternal(request, response, chain);

    String responseHeaderValue = response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER);

    assertThat(responseHeaderValue)
        .as("Response must always have a correlation ID")
        .isNotNull()
        .isNotBlank();

    UUID parsed = UUID.fromString(responseHeaderValue);
    assertThat(parsed.version())
        .as("Generated correlation ID must be a UUID version 4 when input is invalid")
        .isEqualTo(4);

    assertThat(mdcValue.get())
        .as("MDC correlationId must match the response header value")
        .isEqualTo(responseHeaderValue);
  }

  /** Property: Correlation ID is ALWAYS present in response regardless of input. */
  @Property(tries = 100)
  void should_alwaysHaveCorrelationIdInResponse(
      @ForAll("optionalCorrelationIds") String headerValue) throws ServletException, IOException {

    MDC.clear();
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

  // ─── Generators ──────────────────────────────────────────────────────────────────────────────

  @Provide
  Arbitrary<String> validUuidCorrelationIds() {
    return Arbitraries.create(() -> UUID.randomUUID().toString());
  }

  @Provide
  Arbitrary<String> invalidCorrelationIds() {
    return Arbitraries.oneOf(
        Arbitraries.of(
            "not-a-uuid",
            "abc-123-def",
            "12345",
            "hello-world",
            "GGGGGGGG-GGGG-GGGG-GGGG-GGGGGGGGGGGG",
            "too-short",
            "123e4567-e89b-12d3-a456"),
        Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('0', '9')
            .withChars('-', '_')
            .ofMinLength(1)
            .ofMaxLength(64)
            .filter(
                s ->
                    !s.isBlank()
                        && !s.matches(
                            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")));
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
    return Arbitraries.oneOf(
        Arbitraries.create(() -> UUID.randomUUID().toString()),
        Arbitraries.of("not-a-uuid", "invalid-format"),
        Arbitraries.of("", " ", "  "));
  }
}
