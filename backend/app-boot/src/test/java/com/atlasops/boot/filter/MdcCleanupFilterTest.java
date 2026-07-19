package com.atlasops.boot.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class MdcCleanupFilterTest {

  private MdcCleanupFilter filter;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  void setUp() {
    filter = new MdcCleanupFilter();
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
    MDC.clear();
  }

  @Test
  void should_clearMdc_when_requestCompletesNormally() throws ServletException, IOException {
    FilterChain chain =
        (req, res) -> {
          MDC.put("correlationId", "test-id");
          MDC.put("someOtherKey", "some-value");
        };

    filter.doFilterInternal(request, response, chain);

    assertThat(MDC.get("correlationId")).isNull();
    assertThat(MDC.get("someOtherKey")).isNull();
    assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
  }

  @Test
  void should_clearMdc_when_exceptionOccurs() {
    FilterChain chain =
        (req, res) -> {
          MDC.put("correlationId", "test-id");
          MDC.put("userId", "user-123");
          throw new RuntimeException("simulated exception");
        };

    assertThatThrownBy(() -> filter.doFilterInternal(request, response, chain))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("simulated exception");

    assertThat(MDC.get("correlationId")).isNull();
    assertThat(MDC.get("userId")).isNull();
    assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
  }

  @Test
  void should_clearMdc_when_servletExceptionOccurs() {
    FilterChain chain =
        (req, res) -> {
          MDC.put("correlationId", "test-id");
          throw new ServletException("servlet error");
        };

    assertThatThrownBy(() -> filter.doFilterInternal(request, response, chain))
        .isInstanceOf(ServletException.class)
        .hasMessage("servlet error");

    assertThat(MDC.get("correlationId")).isNull();
    assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
  }

  @Test
  void should_clearPreExistingMdcKeys_when_requestCompletes() throws ServletException, IOException {
    MDC.put("preExistingKey", "pre-existing-value");

    FilterChain chain =
        (req, res) -> {
          /* no-op */
        };

    filter.doFilterInternal(request, response, chain);

    assertThat(MDC.get("preExistingKey")).isNull();
    assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
  }

  @Test
  void should_proceedWithFilterChain_when_noException() throws ServletException, IOException {
    boolean[] chainExecuted = {false};
    FilterChain chain = (req, res) -> chainExecuted[0] = true;

    filter.doFilterInternal(request, response, chain);

    assertThat(chainExecuted[0]).isTrue();
  }
}
