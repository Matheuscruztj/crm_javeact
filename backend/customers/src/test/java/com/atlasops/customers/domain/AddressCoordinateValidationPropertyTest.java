package com.atlasops.customers.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;

/**
 * Property-based tests for coordinate validation bounds in the Address value object.
 *
 * <p><b>Validates: Requirements 7.3, 7.4</b>
 *
 * <p>Property 12: Coordinate Validation Bounds
 *
 * <p>Requirement 7.3: THE Customer_Module SHALL validate latitude values between -90 and 90 and
 * longitude values between -180 and 180
 *
 * <p>Requirement 7.4: IF invalid coordinates are provided (outside valid ranges or non-numeric),
 * THEN THE Customer_Module SHALL return a 400 Bad Request with a descriptive validation error
 * indicating the violated constraint
 */
@Tag("Feature: project-implementation-kickoff, Property 12: Coordinate Validation Bounds")
class AddressCoordinateValidationPropertyTest {

  /**
   * Property: For ANY latitude in [-90, 90] and longitude in [-180, 180], the Address SHALL accept
   * and store the coordinates.
   *
   * <p>Validates: Requirements 7.3
   */
  @Property(tries = 100)
  void should_acceptCoordinates_when_withinValidBounds(
      @ForAll @DoubleRange(min = -90.0, max = 90.0) double latitude,
      @ForAll @DoubleRange(min = -180.0, max = 180.0) double longitude) {

    // Act
    Address address = new Address(null, null, null, null, null, latitude, longitude);

    // Assert: coordinates are stored correctly
    assertThat(address.getLatitude()).isEqualTo(latitude);
    assertThat(address.getLongitude()).isEqualTo(longitude);
    assertThat(address.hasCoordinates()).isTrue();
  }

  /**
   * Property: For ANY latitude outside [-90, 90] (above 90), the Address SHALL reject with an
   * appropriate error message.
   *
   * <p>Validates: Requirements 7.3, 7.4
   */
  @Property(tries = 100)
  void should_rejectCoordinates_when_latitudeAboveUpperBound(
      @ForAll("latitudeAbove90") double invalidLatitude,
      @ForAll @DoubleRange(min = -180.0, max = 180.0) double validLongitude) {

    // Act
    Throwable thrown =
        catchThrowable(
            () -> new Address(null, null, null, null, null, invalidLatitude, validLongitude));

    // Assert: rejected with descriptive error
    assertThat(thrown)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Latitude")
        .hasMessageContaining("-90")
        .hasMessageContaining("90");
  }

  /**
   * Property: For ANY latitude outside [-90, 90] (below -90), the Address SHALL reject with an
   * appropriate error message.
   *
   * <p>Validates: Requirements 7.3, 7.4
   */
  @Property(tries = 100)
  void should_rejectCoordinates_when_latitudeBelowLowerBound(
      @ForAll("latitudeBelow90") double invalidLatitude,
      @ForAll @DoubleRange(min = -180.0, max = 180.0) double validLongitude) {

    // Act
    Throwable thrown =
        catchThrowable(
            () -> new Address(null, null, null, null, null, invalidLatitude, validLongitude));

    // Assert: rejected with descriptive error
    assertThat(thrown)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Latitude")
        .hasMessageContaining("-90")
        .hasMessageContaining("90");
  }

  /**
   * Property: For ANY longitude outside [-180, 180] (above 180), the Address SHALL reject with an
   * appropriate error message.
   *
   * <p>Validates: Requirements 7.3, 7.4
   */
  @Property(tries = 100)
  void should_rejectCoordinates_when_longitudeAboveUpperBound(
      @ForAll @DoubleRange(min = -90.0, max = 90.0) double validLatitude,
      @ForAll("longitudeAbove180") double invalidLongitude) {

    // Act
    Throwable thrown =
        catchThrowable(
            () -> new Address(null, null, null, null, null, validLatitude, invalidLongitude));

    // Assert: rejected with descriptive error
    assertThat(thrown)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Longitude")
        .hasMessageContaining("-180")
        .hasMessageContaining("180");
  }

  /**
   * Property: For ANY longitude outside [-180, 180] (below -180), the Address SHALL reject with an
   * appropriate error message.
   *
   * <p>Validates: Requirements 7.3, 7.4
   */
  @Property(tries = 100)
  void should_rejectCoordinates_when_longitudeBelowLowerBound(
      @ForAll @DoubleRange(min = -90.0, max = 90.0) double validLatitude,
      @ForAll("longitudeBelow180") double invalidLongitude) {

    // Act
    Throwable thrown =
        catchThrowable(
            () -> new Address(null, null, null, null, null, validLatitude, invalidLongitude));

    // Assert: rejected with descriptive error
    assertThat(thrown)
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Longitude")
        .hasMessageContaining("-180")
        .hasMessageContaining("180");
  }

  /**
   * Property: For ANY latitude and longitude both outside valid ranges, the Address SHALL reject
   * with a descriptive error.
   *
   * <p>Validates: Requirements 7.3, 7.4
   */
  @Property(tries = 100)
  void should_rejectCoordinates_when_bothLatitudeAndLongitudeInvalid(
      @ForAll("invalidLatitudes") double invalidLatitude,
      @ForAll("invalidLongitudes") double invalidLongitude) {

    // Act
    Throwable thrown =
        catchThrowable(
            () -> new Address(null, null, null, null, null, invalidLatitude, invalidLongitude));

    // Assert: rejected (latitude is validated first in the implementation)
    assertThat(thrown).isInstanceOf(IllegalArgumentException.class);
  }

  // ---- Custom Arbitraries ----

  @Provide
  Arbitrary<Double> latitudeAbove90() {
    return Arbitraries.doubles().between(90.01, 1000.0);
  }

  @Provide
  Arbitrary<Double> latitudeBelow90() {
    return Arbitraries.doubles().between(-1000.0, -90.01);
  }

  @Provide
  Arbitrary<Double> longitudeAbove180() {
    return Arbitraries.doubles().between(180.01, 1000.0);
  }

  @Provide
  Arbitrary<Double> longitudeBelow180() {
    return Arbitraries.doubles().between(-1000.0, -180.01);
  }

  @Provide
  Arbitrary<Double> invalidLatitudes() {
    return Arbitraries.oneOf(latitudeAbove90(), latitudeBelow90());
  }

  @Provide
  Arbitrary<Double> invalidLongitudes() {
    return Arbitraries.oneOf(longitudeAbove180(), longitudeBelow180());
  }
}
