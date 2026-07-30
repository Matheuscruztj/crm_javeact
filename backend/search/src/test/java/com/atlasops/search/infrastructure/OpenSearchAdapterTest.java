package com.atlasops.search.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.search.domain.SearchQuery;
import com.atlasops.search.domain.SearchResult;
import com.atlasops.search.domain.ports.SearchIndexPort;
import com.atlasops.search.domain.ports.SearchIndexUpdatePort;
import com.atlasops.shared.domain.ports.FeatureFlagPort;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;

class OpenSearchAdapterTest {

  @Test
  @DisplayName("should_delegateSearchToFallback_when_opensearchIsDisabled")
  void should_delegateSearchToFallback_when_opensearchIsDisabled() {
    SearchIndexPort fallback = mock(SearchIndexPort.class);
    SearchIndexUpdatePort fallbackUpdate = mock(SearchIndexUpdatePort.class);
    FeatureFlagPort flags = mock(FeatureFlagPort.class);
    when(flags.isEnabled("opensearch")).thenReturn(false);

    Page<SearchResult> fallbackPage =
        new PageImpl<>(
            List.of(new SearchResult("CUSTOMER", "cust-1", "Acme", "Acme Corp", 0.91)));
    SearchQuery query = SearchQuery.withoutFilter("acme", "tenant-alpha");
    PageRequest pageRequest = PageRequest.of(0, 10);
    when(fallback.search(query, pageRequest)).thenReturn(fallbackPage);

    OpenSearchAdapter adapter = new OpenSearchAdapter(fallback, fallbackUpdate, flags);

    Page<SearchResult> result = adapter.search(query, pageRequest);

    assertThat(result).isEqualTo(fallbackPage);
    verify(fallback).search(query, pageRequest);
  }

  @Test
  @DisplayName("should_delegateIndexAndRemoveToFallback_when_opensearchIsDisabled")
  void should_delegateIndexAndRemoveToFallback_when_opensearchIsDisabled() {
    SearchIndexPort fallback = mock(SearchIndexPort.class);
    SearchIndexUpdatePort fallbackUpdate = mock(SearchIndexUpdatePort.class);
    FeatureFlagPort flags = mock(FeatureFlagPort.class);
    when(flags.isEnabled("opensearch")).thenReturn(false);

    OpenSearchAdapter adapter = new OpenSearchAdapter(fallback, fallbackUpdate, flags);

    adapter.indexEntity("CUSTOMER", "cust-1", "Acme Corp", "tenant-alpha");
    adapter.removeEntity("CUSTOMER", "cust-1", "tenant-alpha");

    verify(fallbackUpdate).indexEntity("CUSTOMER", "cust-1", "Acme Corp", "tenant-alpha");
    verify(fallbackUpdate).removeEntity("CUSTOMER", "cust-1", "tenant-alpha");
  }
}
