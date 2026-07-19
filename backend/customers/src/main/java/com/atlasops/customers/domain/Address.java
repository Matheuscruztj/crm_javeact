package com.atlasops.customers.domain;

import com.atlasops.shared.domain.ValueObject;
import java.util.Objects;

/**
 * Value object representing a customer's physical address with optional geospatial coordinates.
 *
 * <p>All text fields (street, city, state, postalCode, country) are optional. Latitude and
 * longitude must both be present or both absent. Latitude must be in range [-90, 90] and longitude
 * must be in range [-180, 180].
 */
public final class Address extends ValueObject {

  private final String street;
  private final String city;
  private final String state;
  private final String postalCode;
  private final String country;
  private final Double latitude;
  private final Double longitude;

  public Address(
      String street,
      String city,
      String state,
      String postalCode,
      String country,
      Double latitude,
      Double longitude) {
    validateCoordinates(latitude, longitude);
    this.street = street;
    this.city = city;
    this.state = state;
    this.postalCode = postalCode;
    this.country = country;
    this.latitude = latitude;
    this.longitude = longitude;
  }

  private void validateCoordinates(Double latitude, Double longitude) {
    if ((latitude == null) != (longitude == null)) {
      throw new IllegalArgumentException(
          "Latitude and longitude must both be present or both absent");
    }
    if (latitude != null) {
      if (latitude < -90.0 || latitude > 90.0) {
        throw new IllegalArgumentException("Latitude must be between -90 and 90, got: " + latitude);
      }
      if (longitude < -180.0 || longitude > 180.0) {
        throw new IllegalArgumentException(
            "Longitude must be between -180 and 180, got: " + longitude);
      }
    }
  }

  public String getStreet() {
    return street;
  }

  public String getCity() {
    return city;
  }

  public String getState() {
    return state;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public String getCountry() {
    return country;
  }

  public Double getLatitude() {
    return latitude;
  }

  public Double getLongitude() {
    return longitude;
  }

  public boolean hasCoordinates() {
    return latitude != null && longitude != null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Address address = (Address) o;
    return Objects.equals(street, address.street)
        && Objects.equals(city, address.city)
        && Objects.equals(state, address.state)
        && Objects.equals(postalCode, address.postalCode)
        && Objects.equals(country, address.country)
        && Objects.equals(latitude, address.latitude)
        && Objects.equals(longitude, address.longitude);
  }

  @Override
  public int hashCode() {
    return Objects.hash(street, city, state, postalCode, country, latitude, longitude);
  }

  @Override
  public String toString() {
    return "Address{"
        + "street='"
        + street
        + '\''
        + ", city='"
        + city
        + '\''
        + ", state='"
        + state
        + '\''
        + ", postalCode='"
        + postalCode
        + '\''
        + ", country='"
        + country
        + '\''
        + ", latitude="
        + latitude
        + ", longitude="
        + longitude
        + '}';
  }
}
