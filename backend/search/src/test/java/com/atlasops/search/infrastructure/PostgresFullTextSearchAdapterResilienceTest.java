package com.atlasops.search.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.atlasops.search.domain.SearchQuery;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;

class PostgresFullTextSearchAdapterResilienceTest {

  @Test
  @DisplayName("should_propagateFailure_when_postgresSearchQueryFails")
  void should_propagateFailure_when_postgresSearchQueryFails() {
    SpringDataSearchIndexRepository repository = mock(SpringDataSearchIndexRepository.class);
    JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    PostgresFullTextSearchAdapter adapter =
        new PostgresFullTextSearchAdapter(repository, jdbcTemplate);

    SearchQuery query = SearchQuery.withoutFilter("acme", "tenant-alpha");
    when(repository.searchByQuery("acme", "tenant-alpha", PageRequest.of(0, 10)))
        .thenThrow(new RuntimeException("postgres unavailable"));

    assertThatThrownBy(() -> adapter.search(query, PageRequest.of(0, 10)))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("postgres unavailable");
  }

  @Test
  @DisplayName("should_propagateFailure_when_postgresIndexWriteFails")
  void should_propagateFailure_when_postgresIndexWriteFails() {
    SpringDataSearchIndexRepository repository = mock(SpringDataSearchIndexRepository.class);
    JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    PostgresFullTextSearchAdapter adapter =
        new PostgresFullTextSearchAdapter(repository, jdbcTemplate);

    when(repository.findByEntityTypeAndEntityIdAndTenantId("CUSTOMER", "cust-1", "tenant-alpha"))
        .thenReturn(Optional.empty());
    when(jdbcTemplate.update(
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any()))
        .thenThrow(new RuntimeException("postgres write unavailable"));

    assertThatThrownBy(
            () -> adapter.indexEntity("CUSTOMER", "cust-1", "Acme Corp", "tenant-alpha"))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("postgres write unavailable");
  }
}
