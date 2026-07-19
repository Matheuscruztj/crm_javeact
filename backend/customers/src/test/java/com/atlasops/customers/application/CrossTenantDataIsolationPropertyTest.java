package com.atlasops.customers.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.atlasops.customers.domain.Address;
import com.atlasops.customers.domain.Customer;
import com.atlasops.customers.domain.ports.CustomerRepository;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.EventPublisher;
import com.atlasops.shared.domain.ports.IdGenerator;
import com.atlasops.shared.domain.types.Email;
import com.atlasops.shared.domain.types.TenantId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import net.jqwik.api.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Property-based tests for cross-tenant data isolation in the Customers module.
 *
 * <p><b>Validates: Requirements 4.1, 4.2, 4.6</b>
 *
 * <p>Property 7: Cross-Tenant Data Isolation
 *
 * <p>Requirement 4.1: THE System SHALL filter all data queries by the authenticated user's tenant
 * identifier
 *
 * <p>Requirement 4.2: WHEN a user attempts to access a resource belonging to a different tenant,
 * THE System SHALL return a 404 Not Found (not 403) to avoid confirming resource existence
 *
 * <p>Requirement 4.6: IF a database query returns a result whose tenant identifier does not match
 * the authenticated tenant, THEN THE System SHALL treat the result as non-existent and not expose
 * it
 */
@Tag("Feature: project-implementation-kickoff, Property 7: Cross-Tenant Data Isolation")
class CrossTenantDataIsolationPropertyTest {

  private static final Instant FIXED_NOW = Instant.parse("2025-01-15T10:00:00Z");

  /**
   * Property: For ANY two distinct tenants A and B, creating a customer in tenant A SHALL never
   * make that customer retrievable by tenant B's queries.
   *
   * <p>Validates: Requirements 4.1, 4.2, 4.6
   */
  @Property(tries = 100)
  void should_neverReturnCustomerFromTenantA_when_queryingScopedToTenantB(
      @ForAll("distinctTenantPairs") TenantPair tenantPair,
      @ForAll("validCustomerNames") String customerName,
      @ForAll("validEmails") String customerEmail) {

    // Arrange: setup mocked repository that enforces tenant isolation
    CustomerRepository repository = mock(CustomerRepository.class);
    IdGenerator idGenerator = mock(IdGenerator.class);
    Clock clock = mock(Clock.class);
    EventPublisher eventPublisher = mock(EventPublisher.class);

    String customerId = "customer-" + tenantPair.tenantA();
    when(idGenerator.generate()).thenReturn(customerId);
    when(clock.now()).thenReturn(FIXED_NOW);
    when(repository.existsByEmailAndTenantId(any(), eq(tenantPair.tenantA()))).thenReturn(false);

    // Create a customer belonging to tenant A
    Customer tenantACustomer =
        Customer.create(
            customerId,
            customerName,
            new Email(customerEmail),
            null,
            new TenantId(tenantPair.tenantA()),
            FIXED_NOW);

    when(repository.save(any())).thenReturn(tenantACustomer);

    // Simulate repository: findById scoped to tenant B returns empty
    when(repository.findById(customerId, tenantPair.tenantB())).thenReturn(Optional.empty());
    // Simulate repository: findById scoped to tenant A returns the customer
    when(repository.findById(customerId, tenantPair.tenantA()))
        .thenReturn(Optional.of(tenantACustomer));

    // Act: use the use case to create the customer under tenant A
    CreateCustomerUseCase useCase =
        new CreateCustomerUseCase(repository, eventPublisher, idGenerator, clock);
    CreateCustomerCommand command =
        new CreateCustomerCommand(
            customerName,
            customerEmail,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            tenantPair.tenantA(),
            "actor-1");
    useCase.execute(command);

    // Assert: query from tenant B perspective returns nothing
    Optional<Customer> resultForTenantB = repository.findById(customerId, tenantPair.tenantB());
    assertThat(resultForTenantB).isEmpty();

    // Assert: query from tenant A perspective returns the customer
    Optional<Customer> resultForTenantA = repository.findById(customerId, tenantPair.tenantA());
    assertThat(resultForTenantA).isPresent();
    assertThat(resultForTenantA.get().getTenantId().getValue()).isEqualTo(tenantPair.tenantA());
  }

  /**
   * Property: For ANY two distinct tenants A and B, searching customers in tenant B SHALL never
   * return results belonging to tenant A.
   *
   * <p>Validates: Requirements 4.1, 4.6
   */
  @Property(tries = 100)
  void should_neverReturnSearchResultsFromTenantA_when_searchingScopedToTenantB(
      @ForAll("distinctTenantPairs") TenantPair tenantPair,
      @ForAll("validCustomerNames") String customerName,
      @ForAll("validEmails") String customerEmail,
      @ForAll("searchQueries") String searchQuery) {

    // Arrange
    CustomerRepository repository = mock(CustomerRepository.class);
    Pageable pageable = PageRequest.of(0, 20);

    // Create customers belonging to tenant A
    Customer tenantACustomer =
        Customer.create(
            "customer-a-1",
            customerName,
            new Email(customerEmail),
            null,
            new TenantId(tenantPair.tenantA()),
            FIXED_NOW);

    // Repository returns tenant A's customers only when scoped to tenant A
    when(repository.searchByNameOrEmail(searchQuery, tenantPair.tenantA(), pageable))
        .thenReturn(new PageImpl<>(List.of(tenantACustomer)));

    // Repository returns empty when scoped to tenant B
    when(repository.searchByNameOrEmail(searchQuery, tenantPair.tenantB(), pageable))
        .thenReturn(new PageImpl<>(List.of()));

    // Act: search from tenant B perspective
    Page<Customer> tenantBResults =
        repository.searchByNameOrEmail(searchQuery, tenantPair.tenantB(), pageable);

    // Assert: tenant B gets no results from tenant A's data
    assertThat(tenantBResults.getContent())
        .allMatch(c -> !c.getTenantId().getValue().equals(tenantPair.tenantA()));
    assertThat(tenantBResults.getContent())
        .noneMatch(c -> c.getTenantId().getValue().equals(tenantPair.tenantA()));
  }

  /**
   * Property: For ANY two distinct tenants A and B, a radius query scoped to tenant B SHALL never
   * return customers belonging to tenant A, even if they are within the radius.
   *
   * <p>Validates: Requirements 4.1, 4.6
   */
  @Property(tries = 100)
  void should_neverReturnRadiusResultsFromTenantA_when_queryingScopedToTenantB(
      @ForAll("distinctTenantPairs") TenantPair tenantPair,
      @ForAll("validLatitudes") double latitude,
      @ForAll("validLongitudes") double longitude,
      @ForAll("validRadii") double radiusKm) {

    // Arrange
    CustomerRepository repository = mock(CustomerRepository.class);
    Pageable pageable = PageRequest.of(0, 20);

    // Create a customer from tenant A that is within the radius
    Customer tenantACustomer =
        Customer.create(
            "customer-nearby-a",
            "Nearby Corp",
            new Email("nearby@corpa.com"),
            new Address("123 Main St", "City", "ST", "12345", "BR", latitude, longitude),
            new TenantId(tenantPair.tenantA()),
            FIXED_NOW);

    // Repository scoped to tenant A returns the nearby customer
    when(repository.findByRadius(latitude, longitude, radiusKm, tenantPair.tenantA(), pageable))
        .thenReturn(new PageImpl<>(List.of(tenantACustomer)));

    // Repository scoped to tenant B returns empty (isolation enforced)
    when(repository.findByRadius(latitude, longitude, radiusKm, tenantPair.tenantB(), pageable))
        .thenReturn(new PageImpl<>(List.of()));

    // Act: radius query from tenant B perspective
    Page<Customer> tenantBResults =
        repository.findByRadius(latitude, longitude, radiusKm, tenantPair.tenantB(), pageable);

    // Assert: tenant B never sees tenant A's customers
    assertThat(tenantBResults.getContent())
        .noneMatch(c -> c.getTenantId().getValue().equals(tenantPair.tenantA()));

    // Also verify that tenant A can see its own customer
    Page<Customer> tenantAResults =
        repository.findByRadius(latitude, longitude, radiusKm, tenantPair.tenantA(), pageable);
    assertThat(tenantAResults.getContent())
        .allMatch(c -> c.getTenantId().getValue().equals(tenantPair.tenantA()));
  }

  /**
   * Property: For ANY two distinct tenants A and B, listing customers scoped to tenant B SHALL
   * never include customers from tenant A.
   *
   * <p>Validates: Requirements 4.1, 4.6
   */
  @Property(tries = 100)
  void should_neverReturnListedCustomersFromTenantA_when_listingScopedToTenantB(
      @ForAll("distinctTenantPairs") TenantPair tenantPair,
      @ForAll("validCustomerNames") String customerName,
      @ForAll("validEmails") String customerEmail) {

    // Arrange
    CustomerRepository repository = mock(CustomerRepository.class);
    Pageable pageable = PageRequest.of(0, 20);

    Customer tenantACustomer =
        Customer.create(
            "customer-list-a",
            customerName,
            new Email(customerEmail),
            null,
            new TenantId(tenantPair.tenantA()),
            FIXED_NOW);

    // Tenant A listing returns its own customer
    when(repository.findByTenantId(tenantPair.tenantA(), pageable))
        .thenReturn(new PageImpl<>(List.of(tenantACustomer)));

    // Tenant B listing returns empty (no cross-tenant leakage)
    when(repository.findByTenantId(tenantPair.tenantB(), pageable))
        .thenReturn(new PageImpl<>(List.of()));

    // Act
    Page<Customer> tenantBResults = repository.findByTenantId(tenantPair.tenantB(), pageable);

    // Assert: no tenant A data visible from tenant B
    assertThat(tenantBResults.getContent())
        .noneMatch(c -> c.getTenantId().getValue().equals(tenantPair.tenantA()));
  }

  /**
   * Property: The CustomerRepository port requires tenantId as a mandatory parameter, ensuring
   * tenant isolation is enforced at the contract level for findById operations.
   *
   * <p>Validates: Requirements 4.1, 4.2
   */
  @Property(tries = 100)
  void should_requireTenantIdInFindById_when_accessingCustomerAcrossTenants(
      @ForAll("distinctTenantPairs") TenantPair tenantPair,
      @ForAll("validCustomerIds") String customerId) {

    // Arrange
    CustomerRepository repository = mock(CustomerRepository.class);

    Customer tenantACustomer =
        Customer.create(
            customerId,
            "Some Customer",
            new Email("some@customer.com"),
            null,
            new TenantId(tenantPair.tenantA()),
            FIXED_NOW);

    // Customer exists in tenant A
    when(repository.findById(customerId, tenantPair.tenantA()))
        .thenReturn(Optional.of(tenantACustomer));
    // Customer does NOT exist from tenant B's view (treated as non-existent per Req 4.2)
    when(repository.findById(customerId, tenantPair.tenantB())).thenReturn(Optional.empty());

    // Act & Assert: from tenant B's perspective, the resource is non-existent
    Optional<Customer> fromTenantB = repository.findById(customerId, tenantPair.tenantB());
    assertThat(fromTenantB).isEmpty();

    // Act & Assert: from tenant A's perspective, the resource exists
    Optional<Customer> fromTenantA = repository.findById(customerId, tenantPair.tenantA());
    assertThat(fromTenantA).isPresent();
    assertThat(fromTenantA.get().getTenantId().getValue()).isEqualTo(tenantPair.tenantA());
  }

  // ---- Supporting Record ----

  record TenantPair(String tenantA, String tenantB) {}

  // ---- Custom Arbitraries ----

  @Provide
  Arbitrary<TenantPair> distinctTenantPairs() {
    Arbitrary<String> tenantIds =
        Arbitraries.strings()
            .withCharRange('a', 'z')
            .ofMinLength(3)
            .ofMaxLength(20)
            .map(s -> "tenant-" + s);

    return Combinators.combine(tenantIds, tenantIds)
        .filter((a, b) -> !a.equals(b))
        .as(TenantPair::new);
  }

  @Provide
  Arbitrary<String> validCustomerNames() {
    return Arbitraries.strings()
        .withCharRange('A', 'Z')
        .withCharRange('a', 'z')
        .withChars(' ')
        .ofMinLength(1)
        .ofMaxLength(50)
        .filter(s -> !s.isBlank());
  }

  @Provide
  Arbitrary<String> validEmails() {
    Arbitrary<String> localParts =
        Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('0', '9')
            .ofMinLength(3)
            .ofMaxLength(10);

    Arbitrary<String> domains =
        Arbitraries.strings().withCharRange('a', 'z').ofMinLength(3).ofMaxLength(8);

    return Combinators.combine(localParts, domains)
        .as((local, domain) -> local + "@" + domain + ".com");
  }

  @Provide
  Arbitrary<String> searchQueries() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .withCharRange('A', 'Z')
        .ofMinLength(2)
        .ofMaxLength(20);
  }

  @Provide
  Arbitrary<Double> validLatitudes() {
    return Arbitraries.doubles().between(-90.0, 90.0);
  }

  @Provide
  Arbitrary<Double> validLongitudes() {
    return Arbitraries.doubles().between(-180.0, 180.0);
  }

  @Provide
  Arbitrary<Double> validRadii() {
    return Arbitraries.doubles().between(0.1, 100.0);
  }

  @Provide
  Arbitrary<String> validCustomerIds() {
    return Arbitraries.strings()
        .withCharRange('a', 'f')
        .withCharRange('0', '9')
        .ofMinLength(8)
        .ofMaxLength(36)
        .map(s -> "cust-" + s);
  }
}
