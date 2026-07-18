package com.atlasops.boot.observability;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/** Unit tests for {@link RequestMetricsFilter}. */
class RequestMetricsFilterTest {

  private MeterRegistry meterRegistry;
  private RequestMetricsFilter filter;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  void setUp() {
    meterRegistry = new SimpleMeterRegistry();
    filter = new RequestMetricsFilter(meterRegistry);
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
  }

  @Test
  void should_recordRequestCount_when_requestProcessed() throws ServletException, IOException {
    request.setMethod("GET");
    request.setRequestURI("/api/v1/customers");
    response.setStatus(200);

    FilterChain chain = (req, res) -> {};
    filter.doFilterInternal(request, response, chain);

    Counter counter =
        meterRegistry
            .find(MetricsConfiguration.REQUESTS_TOTAL)
            .tag("method", "GET")
            .tag("endpoint", "/api/v1/customers")
            .tag("status", "200")
            .counter();

    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void should_recordDuration_when_requestProcessed() throws ServletException, IOException {
    request.setMethod("POST");
    request.setRequestURI("/api/v1/documents");
    response.setStatus(201);

    FilterChain chain =
        (req, res) -> {
          // Simulate some processing time
          try {
            Thread.sleep(5);
          } catch (InterruptedException ignored) {
          }
        };
    filter.doFilterInternal(request, response, chain);

    Timer timer =
        meterRegistry
            .find(MetricsConfiguration.REQUEST_DURATION)
            .tag("method", "POST")
            .tag("endpoint", "/api/v1/documents")
            .timer();

    assertThat(timer).isNotNull();
    assertThat(timer.count()).isEqualTo(1);
    assertThat(timer.totalTime(java.util.concurrent.TimeUnit.NANOSECONDS)).isGreaterThan(0);
  }

  @Test
  void should_recordError4xx_when_clientErrorReturned() throws ServletException, IOException {
    request.setMethod("GET");
    request.setRequestURI("/api/v1/users");
    response.setStatus(404);

    FilterChain chain = (req, res) -> {};
    filter.doFilterInternal(request, response, chain);

    Counter errorCounter =
        meterRegistry
            .find(MetricsConfiguration.ERRORS_TOTAL)
            .tag("error_class", "4xx")
            .tag("endpoint", "/api/v1/users")
            .tag("status", "404")
            .counter();

    assertThat(errorCounter).isNotNull();
    assertThat(errorCounter.count()).isEqualTo(1.0);
  }

  @Test
  void should_recordError5xx_when_serverErrorReturned() throws ServletException, IOException {
    request.setMethod("POST");
    request.setRequestURI("/api/v1/pipeline");
    response.setStatus(500);

    FilterChain chain = (req, res) -> {};
    filter.doFilterInternal(request, response, chain);

    Counter errorCounter =
        meterRegistry
            .find(MetricsConfiguration.ERRORS_TOTAL)
            .tag("error_class", "5xx")
            .tag("endpoint", "/api/v1/pipeline")
            .tag("status", "500")
            .counter();

    assertThat(errorCounter).isNotNull();
    assertThat(errorCounter.count()).isEqualTo(1.0);
  }

  @Test
  void should_notRecordError_when_successfulResponse() throws ServletException, IOException {
    request.setMethod("GET");
    request.setRequestURI("/api/v1/customers");
    response.setStatus(200);

    FilterChain chain = (req, res) -> {};
    filter.doFilterInternal(request, response, chain);

    Counter errorCounter = meterRegistry.find(MetricsConfiguration.ERRORS_TOTAL).counter();
    assertThat(errorCounter).isNull();
  }

  @Test
  void should_skipActuatorEndpoints() {
    request.setRequestURI("/actuator/prometheus");
    assertThat(filter.shouldNotFilter(request)).isTrue();
  }

  @Test
  void should_notSkipApiEndpoints() {
    request.setRequestURI("/api/v1/customers");
    assertThat(filter.shouldNotFilter(request)).isFalse();
  }

  @Test
  void should_normalizeUuidSegments_when_pathContainsUuid() {
    String uri = "/api/v1/customers/550e8400-e29b-41d4-a716-446655440000/documents";
    String normalized = filter.normalizeEndpoint(uri);
    assertThat(normalized).isEqualTo("/api/v1/customers/{id}/documents");
  }

  @Test
  void should_normalizeNumericSegments_when_pathContainsNumbers() {
    String uri = "/api/v1/users/12345";
    String normalized = filter.normalizeEndpoint(uri);
    assertThat(normalized).isEqualTo("/api/v1/users/{id}");
  }

  @Test
  void should_returnSlash_when_uriIsNull() {
    String normalized = filter.normalizeEndpoint(null);
    assertThat(normalized).isEqualTo("/");
  }

  @Test
  void should_returnSlash_when_uriIsBlank() {
    String normalized = filter.normalizeEndpoint("   ");
    assertThat(normalized).isEqualTo("/");
  }

  @Test
  void should_incrementCounterMultipleTimes_when_multipleRequests()
      throws ServletException, IOException {
    request.setMethod("GET");
    request.setRequestURI("/api/v1/tasks");
    response.setStatus(200);

    FilterChain chain = (req, res) -> {};
    filter.doFilterInternal(request, response, chain);
    filter.doFilterInternal(request, response, chain);
    filter.doFilterInternal(request, response, chain);

    Counter counter =
        meterRegistry
            .find(MetricsConfiguration.REQUESTS_TOTAL)
            .tag("method", "GET")
            .tag("endpoint", "/api/v1/tasks")
            .tag("status", "200")
            .counter();

    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(3.0);
  }

  @Test
  void should_recordMetrics_when_filterChainThrowsException() throws ServletException, IOException {
    request.setMethod("GET");
    request.setRequestURI("/api/v1/error");
    response.setStatus(500);

    FilterChain chain =
        (req, res) -> {
          throw new RuntimeException("simulated failure");
        };

    try {
      filter.doFilterInternal(request, response, chain);
    } catch (RuntimeException ignored) {
      // Expected
    }

    // Metrics should still be recorded even on exception
    Counter counter =
        meterRegistry
            .find(MetricsConfiguration.REQUESTS_TOTAL)
            .tag("method", "GET")
            .tag("endpoint", "/api/v1/error")
            .counter();

    assertThat(counter).isNotNull();
    assertThat(counter.count()).isEqualTo(1.0);
  }

  @Test
  void should_record422AsClientError() throws ServletException, IOException {
    request.setMethod("POST");
    request.setRequestURI("/api/v1/workflows");
    response.setStatus(422);

    FilterChain chain = (req, res) -> {};
    filter.doFilterInternal(request, response, chain);

    Counter errorCounter =
        meterRegistry
            .find(MetricsConfiguration.ERRORS_TOTAL)
            .tag("error_class", "4xx")
            .tag("status", "422")
            .counter();

    assertThat(errorCounter).isNotNull();
    assertThat(errorCounter.count()).isEqualTo(1.0);
  }

  @Test
  void should_record503AsServerError() throws ServletException, IOException {
    request.setMethod("GET");
    request.setRequestURI("/api/v1/health");
    response.setStatus(503);

    FilterChain chain = (req, res) -> {};
    filter.doFilterInternal(request, response, chain);

    Counter errorCounter =
        meterRegistry
            .find(MetricsConfiguration.ERRORS_TOTAL)
            .tag("error_class", "5xx")
            .tag("status", "503")
            .counter();

    assertThat(errorCounter).isNotNull();
    assertThat(errorCounter.count()).isEqualTo(1.0);
  }
}
