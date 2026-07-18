package com.atlasops.boot.filter;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

  private CorrelationIdFilter filter;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  void setUp() {
    filter = new CorrelationIdFilter();
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
  }

  @Test
  void should_reuseCorrelationId_when_headerPresent() throws ServletException, IOException {
    String existingId = "my-existing-correlation-id-123";
    request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, existingId);

    AtomicReference<String> mdcValue = new AtomicReference<>();
    FilterChain chain =
        (req, res) -> mdcValue.set(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID_KEY));

    filter.doFilterInternal(request, response, chain);

    assertThat(mdcValue.get()).isEqualTo(existingId);
    assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).isEqualTo(existingId);
  }

  @Test
  void should_generateUuidV4_when_headerAbsent() throws ServletException, IOException {
    AtomicReference<String> mdcValue = new AtomicReference<>();
    FilterChain chain =
        (req, res) -> mdcValue.set(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID_KEY));

    filter.doFilterInternal(request, response, chain);

    String generatedId = mdcValue.get();
    assertThat(generatedId).isNotNull().isNotBlank();

    // Verify it's a valid UUID
    UUID parsed = UUID.fromString(generatedId);
    assertThat(parsed.version()).isEqualTo(4);

    // Response header matches
    assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER))
        .isEqualTo(generatedId);
  }

  @Test
  void should_generateUuidV4_when_headerIsBlank() throws ServletException, IOException {
    request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "   ");

    AtomicReference<String> mdcValue = new AtomicReference<>();
    FilterChain chain =
        (req, res) -> mdcValue.set(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID_KEY));

    filter.doFilterInternal(request, response, chain);

    String generatedId = mdcValue.get();
    assertThat(generatedId).isNotNull().isNotBlank();
    UUID parsed = UUID.fromString(generatedId);
    assertThat(parsed.version()).isEqualTo(4);
  }

  @Test
  void should_alwaysSetResponseHeader() throws ServletException, IOException {
    FilterChain chain =
        (req, res) -> {
          /* no-op */
        };

    filter.doFilterInternal(request, response, chain);

    assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER))
        .isNotNull()
        .isNotBlank();
  }

  @Test
  void should_clearMdc_afterRequestCompletes() throws ServletException, IOException {
    FilterChain chain =
        (req, res) -> {
          // During the request, MDC should have the correlation ID
          assertThat(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID_KEY)).isNotNull();
        };

    filter.doFilterInternal(request, response, chain);

    // After the filter completes, MDC should be clean
    assertThat(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID_KEY)).isNull();
  }

  @Test
  void should_clearMdc_evenWhenExceptionOccurs() throws ServletException, IOException {
    FilterChain chain =
        (req, res) -> {
          throw new RuntimeException("simulated exception");
        };

    try {
      filter.doFilterInternal(request, response, chain);
    } catch (RuntimeException ignored) {
      // Expected
    }

    // MDC should be clean even after exception
    assertThat(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID_KEY)).isNull();
  }

  @Test
  void should_trimCorrelationId_when_headerHasWhitespace() throws ServletException, IOException {
    String idWithSpaces = "  abc-123-def  ";
    request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, idWithSpaces);

    AtomicReference<String> mdcValue = new AtomicReference<>();
    FilterChain chain =
        (req, res) -> mdcValue.set(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID_KEY));

    filter.doFilterInternal(request, response, chain);

    assertThat(mdcValue.get()).isEqualTo("abc-123-def");
    assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER))
        .isEqualTo("abc-123-def");
  }

  @Test
  void should_setTraceIdInMdc_when_noExternalTracing() throws ServletException, IOException {
    AtomicReference<String> correlationValue = new AtomicReference<>();
    AtomicReference<String> traceValue = new AtomicReference<>();
    FilterChain chain =
        (req, res) -> {
          correlationValue.set(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID_KEY));
          traceValue.set(MDC.get(CorrelationIdFilter.MDC_TRACE_ID_KEY));
        };

    filter.doFilterInternal(request, response, chain);

    assertThat(traceValue.get()).isNotNull();
    assertThat(traceValue.get()).isEqualTo(correlationValue.get());
  }

  @Test
  void should_preserveExistingTraceId_when_alreadySetInMdc() throws ServletException, IOException {
    String existingTraceId = "existing-trace-id-from-tracing-system";
    MDC.put(CorrelationIdFilter.MDC_TRACE_ID_KEY, existingTraceId);

    AtomicReference<String> traceValue = new AtomicReference<>();
    FilterChain chain = (req, res) -> traceValue.set(MDC.get(CorrelationIdFilter.MDC_TRACE_ID_KEY));

    try {
      filter.doFilterInternal(request, response, chain);
      assertThat(traceValue.get()).isEqualTo(existingTraceId);
    } finally {
      MDC.remove(CorrelationIdFilter.MDC_TRACE_ID_KEY);
    }
  }

  @Test
  void should_clearTraceId_afterRequestCompletes() throws ServletException, IOException {
    FilterChain chain =
        (req, res) -> {
          assertThat(MDC.get(CorrelationIdFilter.MDC_TRACE_ID_KEY)).isNotNull();
        };

    filter.doFilterInternal(request, response, chain);

    assertThat(MDC.get(CorrelationIdFilter.MDC_TRACE_ID_KEY)).isNull();
  }
}
