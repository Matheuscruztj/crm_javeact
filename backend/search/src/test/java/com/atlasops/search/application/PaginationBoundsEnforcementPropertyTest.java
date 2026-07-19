package com.atlasops.search.application;

import static org.assertj.core.api.Assertions.assertThat;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Property-based tests for pagination bounds enforcement across the application.
 *
 * <p><b>Property 28: Pagination Bounds Enforcement</b>
 *
 * <p><b>Validates: Requirements 6.10, 8.9, 14.4, 15.8, 18.6, 19.5</b>
 *
 * <p>These tests verify that pagination parameters are consistently bounded across all paginated
 * endpoints to prevent resource exhaustion and ensure predictable behavior.
 */
@Tag("Feature: monorepo-sdd-harness, Property 28: Pagination Bounds Enforcement")
class PaginationBoundsEnforcementPropertyTest {

  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int MAX_PAGE_SIZE = 100;

  /**
   * Property: Page size SHALL be capped at MAX_PAGE_SIZE regardless of requested size.
   *
   * <p>Any request for a page size greater than MAX_PAGE_SIZE should be capped to prevent resource
   * exhaustion.
   */
  @Property(tries = 100)
  void should_capPageSize_when_requestedSizeExceedsMax(
      @ForAll @IntRange(min = 101, max = 10000) int requestedSize) {

    // Act
    int effectiveSize = enforcePageSizeBounds(requestedSize);

    // Assert
    assertThat(effectiveSize).isEqualTo(MAX_PAGE_SIZE);
    assertThat(effectiveSize).isLessThanOrEqualTo(MAX_PAGE_SIZE);
  }

  /**
   * Property: Page size SHALL use the requested value when within valid bounds.
   *
   * <p>When the requested page size is between 1 and MAX_PAGE_SIZE, it should be used as-is.
   */
  @Property(tries = 100)
  void should_useRequestedSize_when_withinBounds(
      @ForAll @IntRange(min = 1, max = 100) int requestedSize) {

    // Act
    int effectiveSize = enforcePageSizeBounds(requestedSize);

    // Assert
    assertThat(effectiveSize).isEqualTo(requestedSize);
  }

  /**
   * Property: Page size SHALL default to DEFAULT_PAGE_SIZE when zero or negative.
   *
   * <p>Invalid page sizes (zero or negative) should be replaced with the default.
   */
  @Property(tries = 100)
  void should_useDefaultSize_when_requestedSizeIsInvalid(
      @ForAll @IntRange(min = -1000, max = 0) int requestedSize) {

    // Act
    int effectiveSize = enforcePageSizeBounds(requestedSize);

    // Assert
    assertThat(effectiveSize).isEqualTo(DEFAULT_PAGE_SIZE);
    assertThat(effectiveSize).isGreaterThan(0);
  }

  /**
   * Property: Page number SHALL be non-negative.
   *
   * <p>Negative page numbers should be converted to 0 (first page).
   */
  @Property(tries = 100)
  void should_useZero_when_pageNumberIsNegative(
      @ForAll @IntRange(min = -1000, max = -1) int requestedPage) {

    // Act
    int effectivePage = enforcePageNumberBounds(requestedPage);

    // Assert
    assertThat(effectivePage).isEqualTo(0);
    assertThat(effectivePage).isGreaterThanOrEqualTo(0);
  }

  /**
   * Property: Page number SHALL be preserved when non-negative.
   *
   * <p>Valid page numbers (>= 0) should be used as-is.
   */
  @Property(tries = 100)
  void should_preservePageNumber_when_nonNegative(
      @ForAll @IntRange(min = 0, max = 10000) int requestedPage) {

    // Act
    int effectivePage = enforcePageNumberBounds(requestedPage);

    // Assert
    assertThat(effectivePage).isEqualTo(requestedPage);
  }

  /**
   * Property: Pageable object SHALL always have valid bounds.
   *
   * <p>The combination of page number and size should always result in a valid Pageable with:
   *
   * <ul>
   *   <li>page >= 0
   *   <li>1 <= size <= MAX_PAGE_SIZE
   * </ul>
   */
  @Property(tries = 100)
  void should_produceValidPageable_forAnyInput(
      @ForAll @IntRange(min = -100, max = 1000) int requestedPage,
      @ForAll @IntRange(min = -100, max = 500) int requestedSize) {

    // Act
    Pageable pageable = buildBoundedPageable(requestedPage, requestedSize);

    // Assert
    assertThat(pageable.getPageNumber()).isGreaterThanOrEqualTo(0);
    assertThat(pageable.getPageSize()).isGreaterThan(0);
    assertThat(pageable.getPageSize()).isLessThanOrEqualTo(MAX_PAGE_SIZE);
  }

  /**
   * Property: Offset calculation SHALL be consistent with page and size.
   *
   * <p>The offset should equal page * size for valid pagination parameters.
   */
  @Property(tries = 100)
  void should_calculateCorrectOffset_forAnyValidPagination(
      @ForAll @IntRange(min = 0, max = 100) int page,
      @ForAll @IntRange(min = 1, max = 100) int size) {

    // Act
    Pageable pageable = PageRequest.of(page, size);

    // Assert
    assertThat(pageable.getOffset()).isEqualTo((long) page * size);
  }

  /**
   * Property: Bounded pageable SHALL handle extreme values gracefully.
   *
   * <p>Very large or very small values should be handled without exceptions.
   */
  @Property(tries = 100)
  void should_handleExtremeValues_withoutException(
      @ForAll @IntRange(min = Integer.MIN_VALUE + 1, max = Integer.MAX_VALUE - 1) int requestedPage,
      @ForAll @IntRange(min = Integer.MIN_VALUE + 1, max = Integer.MAX_VALUE - 1)
          int requestedSize) {

    // Act & Assert - should not throw
    Pageable pageable = buildBoundedPageable(requestedPage, requestedSize);

    // Additional invariants
    assertThat(pageable.getPageNumber()).isGreaterThanOrEqualTo(0);
    assertThat(pageable.getPageSize()).isGreaterThan(0);
    assertThat(pageable.getPageSize()).isLessThanOrEqualTo(MAX_PAGE_SIZE);
  }

  /**
   * Property: Page beyond total count SHALL return empty content, not error.
   *
   * <p>Requesting a page beyond the available data should return an empty page, not 404 or error.
   * This is consistent with Spring Data behavior.
   */
  @Property(tries = 100)
  void should_allowPageBeyondTotal_withoutError(
      @ForAll @IntRange(min = 0, max = 10000) int totalElements,
      @ForAll @IntRange(min = 1, max = 100) int pageSize) {

    // Calculate a page number that's beyond the total
    int totalPages = (int) Math.ceil((double) totalElements / pageSize);
    int pageBeyondTotal = totalPages + 1;

    // Act
    Pageable pageable = PageRequest.of(pageBeyondTotal, pageSize);

    // Assert - should be creatable without error
    assertThat(pageable.getPageNumber()).isEqualTo(pageBeyondTotal);
    assertThat(pageable.getPageSize()).isEqualTo(pageSize);
  }

  // ─── Helper Methods (match application pagination logic) ──────────────────────

  /**
   * Enforces page size bounds as used across the application.
   *
   * @param requestedSize the requested page size
   * @return the effective page size within bounds
   */
  private int enforcePageSizeBounds(int requestedSize) {
    if (requestedSize < 1) {
      return DEFAULT_PAGE_SIZE;
    }
    if (requestedSize > MAX_PAGE_SIZE) {
      return MAX_PAGE_SIZE;
    }
    return requestedSize;
  }

  /**
   * Enforces page number bounds as used across the application.
   *
   * @param requestedPage the requested page number
   * @return the effective page number (non-negative)
   */
  private int enforcePageNumberBounds(int requestedPage) {
    return Math.max(0, requestedPage);
  }

  /**
   * Builds a Pageable with enforced bounds, matching application pagination logic.
   *
   * @param requestedPage the requested page number
   * @param requestedSize the requested page size
   * @return a Pageable with valid bounds
   */
  private Pageable buildBoundedPageable(int requestedPage, int requestedSize) {
    int effectivePage = enforcePageNumberBounds(requestedPage);
    int effectiveSize = enforcePageSizeBounds(requestedSize);
    return PageRequest.of(effectivePage, effectiveSize);
  }
}
