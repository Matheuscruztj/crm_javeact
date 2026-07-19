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
    MDC.clear();
  }

  @Test
  void should_reuseCorrelationId_when_headerPresentWithValidUuid()
      throws ServletException, IOException {
    String existingId = UUID.randomUUID().toString();
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

    UUID parsed = UUID.fromString(generatedId);
    assertThat(parsed.version()).isEqualTo(4);

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
  void should_generateUuidV4_when_headerIsNotValidUuidFormat()
      throws ServletException, IOException {
    request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "not-a-valid-uuid");

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
  void should_setMdcDuringFilterChain() throws ServletException, IOException {
    FilterChain chain =
        (req, res) -> {
          assertThat(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID_KEY)).isNotNull();
        };

    filter.doFilterInternal(request, response, chain);
  }

  @Test
  void should_trimCorrelationId_when_headerHasWhitespace() throws ServletException, IOException {
    String uuid = UUID.randomUUID().toString();
    String idWithSpaces = "  " + uuid + "  ";
    request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, idWithSpaces);

    AtomicReference<String> mdcValue = new AtomicReference<>();
    FilterChain chain =
        (req, res) -> mdcValue.set(MDC.get(CorrelationIdFilter.MDC_CORRELATION_ID_KEY));

    filter.doFilterInternal(request, response, chain);

    assertThat(mdcValue.get()).isEqualTo(uuid);
    assertThat(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER)).isEqualTo(uuid);
  }
}
