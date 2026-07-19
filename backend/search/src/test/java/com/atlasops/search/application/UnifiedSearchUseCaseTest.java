package com.atlasops.search.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.atlasops.search.domain.SearchQuery;
import com.atlasops.search.domain.SearchResult;
import com.atlasops.search.domain.ports.SearchIndexPort;
import com.atlasops.search.domain.ports.UserCustomerPort;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class UnifiedSearchUseCaseTest {

  private static final String TENANT_ID = "tenant-alpha";
  private static final String USER_ID = "user-001";
  private static final String ADMIN_ROLE = "ADMIN";
  private static final String CLIENT_ROLE = "CLIENT";
  private static final String ANALYST_ROLE = "ANALYST";

  @Mock private SearchIndexPort searchIndexPort;

  @Mock private UserCustomerPort userCustomerPort;

  private UnifiedSearchUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new UnifiedSearchUseCase(searchIndexPort, userCustomerPort);
  }

  @Nested
  class QueryValidation {

    @Test
    void should_throwException_when_commandIsNull() {
      assertThatThrownBy(() -> useCase.execute(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining("Command must not be null");
    }

    @Test
    void should_throwException_when_queryIsTooShort() {
      UnifiedSearchCommand command =
          new UnifiedSearchCommand("a", null, TENANT_ID, USER_ID, ADMIN_ROLE, 0, 20);

      assertThatThrownBy(() -> useCase.execute(command))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("at least 2 characters");
    }

    @Test
    void should_throwException_when_queryIsTooLong() {
      String longQuery = "a".repeat(201);
      UnifiedSearchCommand command =
          new UnifiedSearchCommand(longQuery, null, TENANT_ID, USER_ID, ADMIN_ROLE, 0, 20);

      assertThatThrownBy(() -> useCase.execute(command))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("at most 200 characters");
    }

    @Test
    void should_throwException_when_queryIsBlank() {
      UnifiedSearchCommand command =
          new UnifiedSearchCommand("   ", null, TENANT_ID, USER_ID, ADMIN_ROLE, 0, 20);

      assertThatThrownBy(() -> useCase.execute(command))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("must not be blank");
    }
  }

  @Nested
  class Pagination {

    @Test
    void should_useDefaultPageSize_when_sizeIsZero() {
      UnifiedSearchCommand command =
          new UnifiedSearchCommand("test query", null, TENANT_ID, USER_ID, ADMIN_ROLE, 0, 0);
      when(searchIndexPort.search(any(SearchQuery.class), any(Pageable.class)))
          .thenReturn(Page.empty());

      useCase.execute(command);

      ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
      verify(searchIndexPort).search(any(SearchQuery.class), pageableCaptor.capture());
      assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(20);
    }

    @Test
    void should_capPageSizeAtMax_when_sizeExceedsMaximum() {
      UnifiedSearchCommand command =
          new UnifiedSearchCommand("test query", null, TENANT_ID, USER_ID, ADMIN_ROLE, 0, 100);
      when(searchIndexPort.search(any(SearchQuery.class), any(Pageable.class)))
          .thenReturn(Page.empty());

      useCase.execute(command);

      ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
      verify(searchIndexPort).search(any(SearchQuery.class), pageableCaptor.capture());
      assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(50);
    }

    @Test
    void should_usePageZero_when_pageIsNegative() {
      UnifiedSearchCommand command =
          new UnifiedSearchCommand("test query", null, TENANT_ID, USER_ID, ADMIN_ROLE, -1, 20);
      when(searchIndexPort.search(any(SearchQuery.class), any(Pageable.class)))
          .thenReturn(Page.empty());

      useCase.execute(command);

      ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
      verify(searchIndexPort).search(any(SearchQuery.class), pageableCaptor.capture());
      assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(0);
    }

    @Test
    void should_useRequestedPageSize_when_sizeIsWithinBounds() {
      UnifiedSearchCommand command =
          new UnifiedSearchCommand("test query", null, TENANT_ID, USER_ID, ADMIN_ROLE, 2, 30);
      when(searchIndexPort.search(any(SearchQuery.class), any(Pageable.class)))
          .thenReturn(Page.empty());

      useCase.execute(command);

      ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
      verify(searchIndexPort).search(any(SearchQuery.class), pageableCaptor.capture());
      assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(2);
      assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(30);
    }
  }

  @Nested
  class TenantIsolation {

    @Test
    void should_passTenanIdToSearchQuery_when_searching() {
      UnifiedSearchCommand command =
          new UnifiedSearchCommand("test query", null, TENANT_ID, USER_ID, ADMIN_ROLE, 0, 20);
      when(searchIndexPort.search(any(SearchQuery.class), any(Pageable.class)))
          .thenReturn(Page.empty());

      useCase.execute(command);

      ArgumentCaptor<SearchQuery> queryCaptor = ArgumentCaptor.forClass(SearchQuery.class);
      verify(searchIndexPort).search(queryCaptor.capture(), any(Pageable.class));
      assertThat(queryCaptor.getValue().tenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    void should_passEntityTypeFilter_when_filterProvided() {
      UnifiedSearchCommand command =
          new UnifiedSearchCommand("test query", "CUSTOMER", TENANT_ID, USER_ID, ADMIN_ROLE, 0, 20);
      when(searchIndexPort.search(any(SearchQuery.class), any(Pageable.class)))
          .thenReturn(Page.empty());

      useCase.execute(command);

      ArgumentCaptor<SearchQuery> queryCaptor = ArgumentCaptor.forClass(SearchQuery.class);
      verify(searchIndexPort).search(queryCaptor.capture(), any(Pageable.class));
      assertThat(queryCaptor.getValue().entityTypeFilter()).isEqualTo("CUSTOMER");
    }
  }

  @Nested
  class AdminAndAnalystSearch {

    @Test
    void should_returnAllResults_when_roleIsAdmin() {
      SearchResult result1 =
          new SearchResult("CUSTOMER", "cust-1", "Alpha Corp", "match in name", 0.9);
      SearchResult result2 =
          new SearchResult("REQUEST", "req-1", "Support Request", "match in title", 0.7);

      Page<SearchResult> expectedResults =
          new PageImpl<>(List.of(result1, result2), PageRequest.of(0, 20), 2);

      UnifiedSearchCommand command =
          new UnifiedSearchCommand("alpha", null, TENANT_ID, USER_ID, ADMIN_ROLE, 0, 20);
      when(searchIndexPort.search(any(SearchQuery.class), any(Pageable.class)))
          .thenReturn(expectedResults);

      Page<SearchResult> results = useCase.execute(command);

      assertThat(results.getContent()).hasSize(2);
      assertThat(results.getContent()).containsExactly(result1, result2);
    }

    @Test
    void should_returnAllResults_when_roleIsAnalyst() {
      SearchResult result =
          new SearchResult("DOCUMENT", "doc-1", "Report.pdf", "analysis complete", 0.8);

      Page<SearchResult> expectedResults =
          new PageImpl<>(List.of(result), PageRequest.of(0, 20), 1);

      UnifiedSearchCommand command =
          new UnifiedSearchCommand("report", null, TENANT_ID, USER_ID, ANALYST_ROLE, 0, 20);
      when(searchIndexPort.search(any(SearchQuery.class), any(Pageable.class)))
          .thenReturn(expectedResults);

      Page<SearchResult> results = useCase.execute(command);

      assertThat(results.getContent()).hasSize(1);
      assertThat(results.getContent()).containsExactly(result);
    }

    @Test
    void should_notCallUserCustomerPort_when_roleIsAdmin() {
      UnifiedSearchCommand command =
          new UnifiedSearchCommand("test", null, TENANT_ID, USER_ID, ADMIN_ROLE, 0, 20);
      when(searchIndexPort.search(any(SearchQuery.class), any(Pageable.class)))
          .thenReturn(Page.empty());

      useCase.execute(command);

      verifyNoInteractions(userCustomerPort);
    }

    @Test
    void should_notCallUserCustomerPort_when_roleIsAnalyst() {
      UnifiedSearchCommand command =
          new UnifiedSearchCommand("test", null, TENANT_ID, USER_ID, ANALYST_ROLE, 0, 20);
      when(searchIndexPort.search(any(SearchQuery.class), any(Pageable.class)))
          .thenReturn(Page.empty());

      useCase.execute(command);

      verifyNoInteractions(userCustomerPort);
    }
  }

  @Nested
  class ClientRoleFiltering {

    @Test
    void should_filterResultsToOwnCustomer_when_roleIsClient() {
      String clientCustomerId = "cust-owned";
      SearchResult ownedCustomer =
          new SearchResult("CUSTOMER", clientCustomerId, "My Corp", "owned customer", 0.9);
      SearchResult otherCustomer =
          new SearchResult("CUSTOMER", "cust-other", "Other Corp", "not mine", 0.6);
      SearchResult ownedRequest =
          new SearchResult("REQUEST", "req-1", "My Request", "request content", 0.8);

      Page<SearchResult> allResults =
          new PageImpl<>(
              List.of(ownedCustomer, otherCustomer, ownedRequest), PageRequest.of(0, 20), 3);

      UnifiedSearchCommand command =
          new UnifiedSearchCommand("query", null, TENANT_ID, USER_ID, CLIENT_ROLE, 0, 20);
      when(searchIndexPort.search(any(SearchQuery.class), any(Pageable.class)))
          .thenReturn(allResults);
      when(userCustomerPort.findCustomerIdsByUserId(USER_ID, TENANT_ID))
          .thenReturn(List.of(clientCustomerId));

      Page<SearchResult> results = useCase.execute(command);

      // Only the owned customer and the request (non-CUSTOMER entities pass through)
      assertThat(results.getContent()).hasSize(2);
      assertThat(results.getContent()).contains(ownedCustomer, ownedRequest);
      assertThat(results.getContent()).doesNotContain(otherCustomer);
    }

    @Test
    void should_returnEmptyPage_when_clientHasNoAssociatedCustomers() {
      SearchResult result = new SearchResult("CUSTOMER", "cust-1", "Some Corp", "content", 0.8);
      Page<SearchResult> allResults = new PageImpl<>(List.of(result), PageRequest.of(0, 20), 1);

      UnifiedSearchCommand command =
          new UnifiedSearchCommand("query", null, TENANT_ID, USER_ID, CLIENT_ROLE, 0, 20);
      when(searchIndexPort.search(any(SearchQuery.class), any(Pageable.class)))
          .thenReturn(allResults);
      when(userCustomerPort.findCustomerIdsByUserId(USER_ID, TENANT_ID)).thenReturn(List.of());

      Page<SearchResult> results = useCase.execute(command);

      assertThat(results.getContent()).isEmpty();
    }

    @Test
    void should_callUserCustomerPort_when_roleIsClient() {
      UnifiedSearchCommand command =
          new UnifiedSearchCommand("test", null, TENANT_ID, USER_ID, CLIENT_ROLE, 0, 20);
      when(searchIndexPort.search(any(SearchQuery.class), any(Pageable.class)))
          .thenReturn(Page.empty());
      when(userCustomerPort.findCustomerIdsByUserId(USER_ID, TENANT_ID)).thenReturn(List.of());

      useCase.execute(command);

      verify(userCustomerPort).findCustomerIdsByUserId(USER_ID, TENANT_ID);
    }

    @Test
    void should_handleCaseInsensitiveClientRole_when_roleIsLowercase() {
      UnifiedSearchCommand command =
          new UnifiedSearchCommand("test", null, TENANT_ID, USER_ID, "client", 0, 20);
      when(searchIndexPort.search(any(SearchQuery.class), any(Pageable.class)))
          .thenReturn(Page.empty());
      when(userCustomerPort.findCustomerIdsByUserId(USER_ID, TENANT_ID)).thenReturn(List.of());

      useCase.execute(command);

      verify(userCustomerPort).findCustomerIdsByUserId(USER_ID, TENANT_ID);
    }
  }

  @Nested
  class EmptyResults {

    @Test
    void should_returnEmptyPage_when_noResultsFound() {
      UnifiedSearchCommand command =
          new UnifiedSearchCommand("nonexistent", null, TENANT_ID, USER_ID, ADMIN_ROLE, 0, 20);
      when(searchIndexPort.search(any(SearchQuery.class), any(Pageable.class)))
          .thenReturn(Page.empty());

      Page<SearchResult> results = useCase.execute(command);

      assertThat(results.getContent()).isEmpty();
      assertThat(results.getTotalElements()).isEqualTo(0);
    }
  }
}
