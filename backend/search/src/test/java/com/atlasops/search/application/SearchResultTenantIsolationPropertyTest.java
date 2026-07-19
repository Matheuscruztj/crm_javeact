package com.atlasops.search.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.search.domain.SearchQuery;
import com.atlasops.search.domain.SearchResult;
import com.atlasops.search.domain.ports.SearchIndexPort;
import com.atlasops.search.domain.ports.UserCustomerPort;
import java.util.List;
import net.jqwik.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Property-based tests for search result tenant isolation.
 *
 * <p><b>Validates: Requirements 18.4, 18.5</b>
 *
 * <p>Property 27: Search Result Tenant Isolation
 *
 * <p>Requirement 18.4: THE Search_Module SHALL filter all search results by the authenticated
 * user's tenant identifier, ensuring no results from other tenants appear.
 *
 * <p>Requirement 18.5: IF the authenticated user has the CLIENT role, THEN THE Search_Module SHALL
 * further restrict results to entities associated with the user's customer.
 */
@Tag("Feature: project-implementation-kickoff, Property 27: Search Result Tenant Isolation")
class SearchResultTenantIsolationPropertyTest {

  private static final String[] ENTITY_TYPES = {"CUSTOMER", "REQUEST", "DOCUMENT"};

  /**
   * Property: For ANY search query from tenant A, the SearchQuery passed to the SearchIndexPort
   * ALWAYS contains tenant A's identifier, ensuring results are scoped to the requesting tenant.
   *
   * <p>Validates: Requirement 18.4
   */
  @Property(tries = 100)
  void should_alwaysScopeSearchByRequestingTenant_forAnyTenantAndQuery(
      @ForAll("validTenantIds") String tenantId,
      @ForAll("validSearchQueries") String query,
      @ForAll("validUserIds") String userId,
      @ForAll("validRoles") String role) {

    // Arrange
    SearchIndexPort searchIndexPort = mock(SearchIndexPort.class);
    UserCustomerPort userCustomerPort = mock(UserCustomerPort.class);
    UnifiedSearchUseCase useCase = new UnifiedSearchUseCase(searchIndexPort, userCustomerPort);

    when(searchIndexPort.search(any(SearchQuery.class), any(Pageable.class)))
        .thenReturn(Page.empty());
    when(userCustomerPort.findCustomerIdsByUserId(any(), any()))
        .thenReturn(List.of("customer-001"));

    UnifiedSearchCommand command =
        new UnifiedSearchCommand(query, null, tenantId, userId, role, 0, 20);

    // Act
    useCase.execute(command);

    // Assert: SearchQuery always contains the requesting tenant's ID
    ArgumentCaptor<SearchQuery> queryCaptor = ArgumentCaptor.forClass(SearchQuery.class);
    verify(searchIndexPort).search(queryCaptor.capture(), any(Pageable.class));

    assertThat(queryCaptor.getValue().tenantId()).isEqualTo(tenantId);
  }

  /**
   * Property: For ANY two distinct tenants, a search from tenant A NEVER returns results that
   * belong to tenant B. The SearchIndexPort is always called with only tenant A's context.
   *
   * <p>Validates: Requirement 18.4
   */
  @Property(tries = 100)
  void should_neverReturnCrossTenantResults_forAnyDistinctTenantPair(
      @ForAll("validTenantIds") String tenantA,
      @ForAll("validTenantIds") String tenantB,
      @ForAll("validSearchQueries") String query,
      @ForAll("validUserIds") String userId) {

    Assume.that(!tenantA.equals(tenantB));

    // Arrange: simulate that the search index returns results for tenantA only
    SearchIndexPort searchIndexPort = mock(SearchIndexPort.class);
    UserCustomerPort userCustomerPort = mock(UserCustomerPort.class);
    UnifiedSearchUseCase useCase = new UnifiedSearchUseCase(searchIndexPort, userCustomerPort);

    SearchResult resultForTenantA =
        new SearchResult("CUSTOMER", "cust-a1", "Tenant A Customer", "match in A", 0.85);

    // The port scopes by tenantId in the SearchQuery, so searching as tenantA gets results
    when(searchIndexPort.search(
            org.mockito.ArgumentMatchers.argThat(sq -> sq != null && sq.tenantId().equals(tenantA)),
            any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(resultForTenantA)));

    // Searching as tenantB gets empty results (the port enforces tenant isolation)
    when(searchIndexPort.search(
            org.mockito.ArgumentMatchers.argThat(sq -> sq != null && sq.tenantId().equals(tenantB)),
            any(Pageable.class)))
        .thenReturn(Page.empty());

    // Act: search as tenantB
    UnifiedSearchCommand commandB =
        new UnifiedSearchCommand(query, null, tenantB, userId, "ADMIN", 0, 20);
    Page<SearchResult> resultFromTenantB = useCase.execute(commandB);

    // Assert: tenantB never sees tenantA's results
    assertThat(resultFromTenantB.getContent()).isEmpty();

    // Act: search as tenantA
    UnifiedSearchCommand commandA =
        new UnifiedSearchCommand(query, null, tenantA, userId, "ADMIN", 0, 20);
    Page<SearchResult> resultFromTenantA = useCase.execute(commandA);

    // Assert: tenantA sees its own results
    assertThat(resultFromTenantA.getContent()).hasSize(1);
    assertThat(resultFromTenantA.getContent().get(0).entityId()).isEqualTo("cust-a1");
  }

  /**
   * Property: For ANY CLIENT user, the search results are further filtered to only include entities
   * associated with the client's customer, even when the search port returns broader results.
   *
   * <p>Validates: Requirement 18.5
   */
  @Property(tries = 100)
  void should_restrictResultsToClientCustomer_forAnyClientRoleSearch(
      @ForAll("validTenantIds") String tenantId,
      @ForAll("validSearchQueries") String query,
      @ForAll("validUserIds") String clientUserId,
      @ForAll("validCustomerIds") String ownedCustomerId,
      @ForAll("validCustomerIds") String otherCustomerId) {

    Assume.that(!ownedCustomerId.equals(otherCustomerId));

    // Arrange
    SearchIndexPort searchIndexPort = mock(SearchIndexPort.class);
    UserCustomerPort userCustomerPort = mock(UserCustomerPort.class);
    UnifiedSearchUseCase useCase = new UnifiedSearchUseCase(searchIndexPort, userCustomerPort);

    // The port returns results for both owned and other customers within the same tenant
    SearchResult ownedResult =
        new SearchResult("CUSTOMER", ownedCustomerId, "My Customer", "owned match", 0.9);
    SearchResult otherResult =
        new SearchResult("CUSTOMER", otherCustomerId, "Other Customer", "other match", 0.7);

    List<SearchResult> allResults = List.of(ownedResult, otherResult);
    when(searchIndexPort.search(any(SearchQuery.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(allResults, PageRequest.of(0, 20), 2));

    // The user is associated only with the owned customer
    when(userCustomerPort.findCustomerIdsByUserId(clientUserId, tenantId))
        .thenReturn(List.of(ownedCustomerId));

    // Act
    UnifiedSearchCommand command =
        new UnifiedSearchCommand(query, null, tenantId, clientUserId, "CLIENT", 0, 20);
    Page<SearchResult> results = useCase.execute(command);

    // Assert: only results belonging to the client's customer are returned
    assertThat(results.getContent())
        .allMatch(r -> !r.entityType().equals("CUSTOMER") || ownedCustomerId.equals(r.entityId()));
    assertThat(results.getContent()).doesNotContain(otherResult);
    assertThat(results.getContent()).contains(ownedResult);
  }

  /**
   * Property: For ANY CLIENT user with no associated customers, the search ALWAYS returns an empty
   * result set regardless of what the search index contains.
   *
   * <p>Validates: Requirement 18.5
   */
  @Property(tries = 100)
  void should_returnEmptyResults_forAnyClientWithNoAssociatedCustomers(
      @ForAll("validTenantIds") String tenantId,
      @ForAll("validSearchQueries") String query,
      @ForAll("validUserIds") String clientUserId) {

    // Arrange
    SearchIndexPort searchIndexPort = mock(SearchIndexPort.class);
    UserCustomerPort userCustomerPort = mock(UserCustomerPort.class);
    UnifiedSearchUseCase useCase = new UnifiedSearchUseCase(searchIndexPort, userCustomerPort);

    // The port returns some results
    SearchResult result =
        new SearchResult("CUSTOMER", "cust-001", "Some Customer", "some content", 0.8);
    when(searchIndexPort.search(any(SearchQuery.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(result), PageRequest.of(0, 20), 1));

    // The client has no associated customers
    when(userCustomerPort.findCustomerIdsByUserId(clientUserId, tenantId)).thenReturn(List.of());

    // Act
    UnifiedSearchCommand command =
        new UnifiedSearchCommand(query, null, tenantId, clientUserId, "CLIENT", 0, 20);
    Page<SearchResult> results = useCase.execute(command);

    // Assert: results are always empty for a client with no customer associations
    assertThat(results.getContent()).isEmpty();
  }

  /**
   * Property: For ANY ADMIN or ANALYST user, tenant isolation is enforced through the SearchQuery
   * tenantId, and no additional CLIENT-level filtering is applied (all tenant results are visible).
   *
   * <p>Validates: Requirements 18.4, 18.5
   */
  @Property(tries = 100)
  void should_returnAllTenantResults_forAnyNonClientRoleSearch(
      @ForAll("validTenantIds") String tenantId,
      @ForAll("validSearchQueries") String query,
      @ForAll("validUserIds") String userId,
      @ForAll("nonClientRoles") String role) {

    // Arrange
    SearchIndexPort searchIndexPort = mock(SearchIndexPort.class);
    UserCustomerPort userCustomerPort = mock(UserCustomerPort.class);
    UnifiedSearchUseCase useCase = new UnifiedSearchUseCase(searchIndexPort, userCustomerPort);

    SearchResult result1 =
        new SearchResult("CUSTOMER", "cust-001", "Customer One", "match one", 0.9);
    SearchResult result2 = new SearchResult("REQUEST", "req-001", "Request One", "match two", 0.7);
    SearchResult result3 =
        new SearchResult("DOCUMENT", "doc-001", "Document One", "match three", 0.5);

    List<SearchResult> allResults = List.of(result1, result2, result3);
    when(searchIndexPort.search(any(SearchQuery.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(allResults, PageRequest.of(0, 20), 3));

    // Act
    UnifiedSearchCommand command =
        new UnifiedSearchCommand(query, null, tenantId, userId, role, 0, 20);
    Page<SearchResult> results = useCase.execute(command);

    // Assert: all results from the search port are returned (no CLIENT filtering)
    assertThat(results.getContent()).hasSize(3);
    assertThat(results.getContent()).containsExactlyElementsOf(allResults);
  }

  // ---- Custom Arbitraries ----

  @Provide
  Arbitrary<String> validTenantIds() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .withChars('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-')
        .ofMinLength(5)
        .ofMaxLength(20)
        .filter(s -> s.matches("^[a-z][a-z0-9-]*[a-z0-9]$"));
  }

  @Provide
  Arbitrary<String> validUserIds() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .withChars('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-')
        .ofMinLength(5)
        .ofMaxLength(30)
        .filter(s -> s.matches("^[a-z][a-z0-9-]*[a-z0-9]$"));
  }

  @Provide
  Arbitrary<String> validCustomerIds() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .withChars('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-')
        .ofMinLength(5)
        .ofMaxLength(20)
        .filter(s -> s.matches("^[a-z][a-z0-9-]*[a-z0-9]$"));
  }

  @Provide
  Arbitrary<String> validSearchQueries() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .withChars(' ', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        .ofMinLength(2)
        .ofMaxLength(50)
        .filter(s -> !s.isBlank() && s.length() >= 2);
  }

  @Provide
  Arbitrary<String> validRoles() {
    return Arbitraries.of("ADMIN", "ANALYST", "CLIENT");
  }

  @Provide
  Arbitrary<String> nonClientRoles() {
    return Arbitraries.of("ADMIN", "ANALYST");
  }
}
