package com.atlasops.customers.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlasops.customers.domain.Address;
import com.atlasops.customers.domain.Customer;
import com.atlasops.customers.domain.CustomerStatus;
import com.atlasops.customers.domain.ports.CustomerRepository;
import com.atlasops.shared.domain.types.Email;
import com.atlasops.shared.domain.types.TenantId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.jqwik.api.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Property-based tests for radius query correctness in FindCustomersByRadiusUseCase.
 *
 * <p><b>Validates: Requirements 7.2</b>
 *
 * <p>Property 13: Radius Query Correctness
 *
 * <p>Requirement 7.2: WHEN a radius query is submitted with center coordinates and distance in
 * kilometers, THE Customer_Module SHALL return all customers within the specified radius ordered by
 * distance ascending
 *
 * <p>The Haversine formula is used as a reference oracle to verify that the use case (backed by
 * ST_DWithin geospatial queries) correctly includes customers inside the radius and excludes
 * customers outside it.
 */
@Tag("Feature: project-implementation-kickoff, Property 13: Radius Query Correctness")
class FindCustomersByRadiusPropertyTest {

  private static final String TENANT_ID = "tenant-test-001";
  private static final double EARTH_RADIUS_KM = 6371.0;

  /**
   * Property: For ANY set of customer locations and ANY valid center/radius query, the use case
   * SHALL return ONLY customers whose Haversine distance from the center is less than or equal to
   * the specified radius.
   *
   * <p>Validates: Requirements 7.2
   */
  @Property(tries = 100)
  void should_returnOnlyCustomersWithinRadius_when_radiusQueryExecuted(
      @ForAll("centerLatitude") double centerLat,
      @ForAll("centerLongitude") double centerLon,
      @ForAll("validRadius") double radiusKm,
      @ForAll("customerLocations") List<double[]> customerLocations) {

    // Arrange: classify customers as inside/outside using Haversine oracle
    List<Customer> allCustomers = createCustomersWithLocations(customerLocations);

    List<Customer> expectedInside =
        allCustomers.stream()
            .filter(c -> c.getAddress() != null && c.getAddress().hasCoordinates())
            .filter(
                c ->
                    haversineDistanceKm(
                            centerLat,
                            centerLon,
                            c.getAddress().getLatitude(),
                            c.getAddress().getLongitude())
                        <= radiusKm)
            .collect(Collectors.toList());

    // Create a repository stub that uses Haversine as its "ST_DWithin" implementation
    CustomerRepository repository = new HaversineBasedCustomerRepository(allCustomers);

    FindCustomersByRadiusUseCase useCase = new FindCustomersByRadiusUseCase(repository);

    // Act
    Page<Customer> result =
        useCase.execute(centerLat, centerLon, radiusKm, TENANT_ID, PageRequest.of(0, 100));

    // Assert: all returned customers should be within radius
    assertThat(result.getContent())
        .allSatisfy(
            customer -> {
              double distance =
                  haversineDistanceKm(
                      centerLat,
                      centerLon,
                      customer.getAddress().getLatitude(),
                      customer.getAddress().getLongitude());
              assertThat(distance)
                  .as(
                      "Customer at (%f, %f) should be within %f km, but is %f km away",
                      customer.getAddress().getLatitude(),
                      customer.getAddress().getLongitude(),
                      radiusKm,
                      distance)
                  .isLessThanOrEqualTo(radiusKm);
            });

    // Assert: count matches expected
    assertThat(result.getContent()).hasSize(expectedInside.size());
  }

  /**
   * Property: For ANY center/radius query, customers OUTSIDE the radius SHALL NOT be included in
   * the result.
   *
   * <p>Validates: Requirements 7.2
   */
  @Property(tries = 100)
  void should_excludeCustomersOutsideRadius_when_radiusQueryExecuted(
      @ForAll("centerLatitude") double centerLat,
      @ForAll("centerLongitude") double centerLon,
      @ForAll("validRadius") double radiusKm,
      @ForAll("customerLocations") List<double[]> customerLocations) {

    // Arrange
    List<Customer> allCustomers = createCustomersWithLocations(customerLocations);

    List<String> expectedOutsideIds =
        allCustomers.stream()
            .filter(c -> c.getAddress() != null && c.getAddress().hasCoordinates())
            .filter(
                c ->
                    haversineDistanceKm(
                            centerLat,
                            centerLon,
                            c.getAddress().getLatitude(),
                            c.getAddress().getLongitude())
                        > radiusKm)
            .map(Customer::getId)
            .collect(Collectors.toList());

    CustomerRepository repository = new HaversineBasedCustomerRepository(allCustomers);
    FindCustomersByRadiusUseCase useCase = new FindCustomersByRadiusUseCase(repository);

    // Act
    Page<Customer> result =
        useCase.execute(centerLat, centerLon, radiusKm, TENANT_ID, PageRequest.of(0, 100));

    // Assert: none of the outside customers should appear in results
    List<String> returnedIds =
        result.getContent().stream().map(Customer::getId).collect(Collectors.toList());

    if (!expectedOutsideIds.isEmpty()) {
      assertThat(returnedIds).doesNotContainAnyElementsOf(expectedOutsideIds);
    }

    // Also verify result count: returned customers = total with coords - outside customers
    long customersWithCoords =
        allCustomers.stream()
            .filter(c -> c.getAddress() != null && c.getAddress().hasCoordinates())
            .count();
    assertThat(returnedIds).hasSize((int) (customersWithCoords - expectedOutsideIds.size()));
  }

  /**
   * Property: Results SHALL be ordered by distance ascending from the center point.
   *
   * <p>Validates: Requirements 7.2
   */
  @Property(tries = 100)
  void should_returnResultsOrderedByDistanceAscending_when_radiusQueryExecuted(
      @ForAll("centerLatitude") double centerLat,
      @ForAll("centerLongitude") double centerLon,
      @ForAll("validRadius") double radiusKm,
      @ForAll("customerLocations") List<double[]> customerLocations) {

    // Arrange
    List<Customer> allCustomers = createCustomersWithLocations(customerLocations);
    CustomerRepository repository = new HaversineBasedCustomerRepository(allCustomers);
    FindCustomersByRadiusUseCase useCase = new FindCustomersByRadiusUseCase(repository);

    // Act
    Page<Customer> result =
        useCase.execute(centerLat, centerLon, radiusKm, TENANT_ID, PageRequest.of(0, 100));

    // Assert: distances should be non-decreasing
    List<Double> distances =
        result.getContent().stream()
            .map(
                c ->
                    haversineDistanceKm(
                        centerLat,
                        centerLon,
                        c.getAddress().getLatitude(),
                        c.getAddress().getLongitude()))
            .collect(Collectors.toList());

    for (int i = 1; i < distances.size(); i++) {
      assertThat(distances.get(i))
          .as(
              "Distance at index %d (%f) should be >= distance at index %d (%f)",
              i, distances.get(i), i - 1, distances.get(i - 1))
          .isGreaterThanOrEqualTo(distances.get(i - 1));
    }
  }

  // ---- Haversine reference oracle ----

  /**
   * Computes the Haversine distance between two points on the Earth's surface.
   *
   * @param lat1 latitude of point 1 in degrees
   * @param lon1 longitude of point 1 in degrees
   * @param lat2 latitude of point 2 in degrees
   * @param lon2 longitude of point 2 in degrees
   * @return distance in kilometers
   */
  private static double haversineDistanceKm(double lat1, double lon1, double lat2, double lon2) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLon = Math.toRadians(lon2 - lon1);
    double a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return EARTH_RADIUS_KM * c;
  }

  // ---- Test infrastructure ----

  private List<Customer> createCustomersWithLocations(List<double[]> locations) {
    Instant now = Instant.parse("2025-01-15T10:00:00Z");
    return locations.stream()
        .map(
            coords -> {
              String id = "customer-" + coords[0] + "-" + coords[1];
              Address address =
                  new Address("Street", "City", "State", "12345", "Country", coords[0], coords[1]);
              return Customer.reconstitute(
                  id,
                  "Customer " + id,
                  new Email("customer" + id.hashCode() + "@test.com"),
                  address,
                  CustomerStatus.ACTIVE,
                  new TenantId(TENANT_ID),
                  now,
                  now);
            })
        .collect(Collectors.toList());
  }

  /**
   * A repository implementation that uses Haversine formula to simulate ST_DWithin behavior. This
   * acts as the reference oracle for testing radius query correctness.
   */
  private static class HaversineBasedCustomerRepository implements CustomerRepository {

    private final List<Customer> customers;

    HaversineBasedCustomerRepository(List<Customer> customers) {
      this.customers = customers;
    }

    @Override
    public Page<Customer> findByRadius(
        double latitude, double longitude, double distanceKm, String tenantId, Pageable pageable) {

      List<Customer> withinRadius =
          customers.stream()
              .filter(c -> c.getTenantId().getValue().equals(tenantId))
              .filter(c -> c.getAddress() != null && c.getAddress().hasCoordinates())
              .filter(
                  c ->
                      haversineDistanceKm(
                              latitude,
                              longitude,
                              c.getAddress().getLatitude(),
                              c.getAddress().getLongitude())
                          <= distanceKm)
              .sorted(
                  (a, b) -> {
                    double distA =
                        haversineDistanceKm(
                            latitude,
                            longitude,
                            a.getAddress().getLatitude(),
                            a.getAddress().getLongitude());
                    double distB =
                        haversineDistanceKm(
                            latitude,
                            longitude,
                            b.getAddress().getLatitude(),
                            b.getAddress().getLongitude());
                    return Double.compare(distA, distB);
                  })
              .collect(Collectors.toList());

      return new PageImpl<>(withinRadius, pageable, withinRadius.size());
    }

    @Override
    public Customer save(Customer customer) {
      return customer;
    }

    @Override
    public Optional<Customer> findById(String id, String tenantId) {
      return Optional.empty();
    }

    @Override
    public boolean existsByEmailAndTenantId(String email, String tenantId) {
      return false;
    }

    @Override
    public Page<Customer> findByTenantId(String tenantId, Pageable pageable) {
      return Page.empty();
    }

    @Override
    public Page<Customer> searchByNameOrEmail(String query, String tenantId, Pageable pageable) {
      return Page.empty();
    }
  }

  // ---- Custom Arbitraries ----

  @Provide
  Arbitrary<Double> centerLatitude() {
    return Arbitraries.doubles().between(-90.0, 90.0);
  }

  @Provide
  Arbitrary<Double> centerLongitude() {
    return Arbitraries.doubles().between(-180.0, 180.0);
  }

  @Provide
  Arbitrary<Double> validRadius() {
    return Arbitraries.doubles().between(0.1, 500.0);
  }

  @Provide
  Arbitrary<List<double[]>> customerLocations() {
    Arbitrary<double[]> singleLocation =
        Combinators.combine(
                Arbitraries.doubles().between(-90.0, 90.0),
                Arbitraries.doubles().between(-180.0, 180.0))
            .as((lat, lon) -> new double[] {lat, lon});

    return singleLocation.list().ofMinSize(1).ofMaxSize(10);
  }
}
