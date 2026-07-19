package com.atlasops.boot.filter;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.Map;
import net.jqwik.api.*;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Property-based tests for MDC cleanup after request completion.
 *
 * <p><b>Validates: Requirements 27.6</b>
 *
 * <p>Property 26: For ANY set of MDC keys placed before filter execution, after the filter
 * completes (regardless of exception), ALL MDC keys are cleared. MDC.getCopyOfContextMap() returns
 * null or empty after cleanup.
 */
@Tag("Feature: project-implementation-kickoff, Property 26: MDC Cleanup After Request")
class MdcCleanupFilterPropertyTest {

  private final MdcCleanupFilter filter = new MdcCleanupFilter();

  /**
   * Property: For any set of MDC key-value pairs placed before filter execution, after the filter
   * completes normally, ALL MDC keys are cleared.
   */
  @Property(tries = 100)
  void should_alwaysClearMdc_forAnySetOfMdcKeys(
      @ForAll("arbitraryMdcEntries") Map<String, String> mdcEntries)
      throws ServletException, IOException {

    MDC.clear();
    mdcEntries.forEach(MDC::put);

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain chain =
        (req, res) -> {
          /* no-op */
        };

    filter.doFilterInternal(request, response, chain);

    Map<String, String> mdcAfter = MDC.getCopyOfContextMap();
    assertThat(mdcAfter == null || mdcAfter.isEmpty())
        .as("MDC must be empty after filter completes, but found keys: %s", mdcAfter)
        .isTrue();
  }

  /**
   * Property: For any set of MDC key-value pairs, when the filter chain throws an exception, ALL
   * MDC keys are still cleared after the filter propagates the exception.
   */
  @Property(tries = 100)
  void should_alwaysClearMdc_when_filterChainThrows(
      @ForAll("arbitraryMdcEntries") Map<String, String> mdcEntries)
      throws ServletException, IOException {

    MDC.clear();
    mdcEntries.forEach(MDC::put);

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpServletResponse response = new MockHttpServletResponse();
    FilterChain throwingChain =
        (req, res) -> {
          throw new ServletException("Simulated failure during request processing");
        };

    try {
      filter.doFilterInternal(request, response, throwingChain);
    } catch (ServletException e) {
      // Expected — the filter should propagate the exception
    }

    Map<String, String> mdcAfter = MDC.getCopyOfContextMap();
    assertThat(mdcAfter == null || mdcAfter.isEmpty())
        .as(
            "MDC must be empty after filter completes even on exception, but found keys: %s",
            mdcAfter)
        .isTrue();
  }

  // ─── Generators ──────────────────────────────────────────────────────────────────────────────

  @Provide
  Arbitrary<Map<String, String>> arbitraryMdcEntries() {
    Arbitrary<String> keys =
        Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('A', 'Z')
            .withChars('_', '-', '.')
            .ofMinLength(1)
            .ofMaxLength(30)
            .filter(s -> !s.isBlank());

    Arbitrary<String> values =
        Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('0', '9')
            .withChars(' ', '-', '_')
            .ofMinLength(1)
            .ofMaxLength(50)
            .filter(s -> !s.isBlank());

    return Arbitraries.maps(keys, values).ofMinSize(1).ofMaxSize(10);
  }
}
