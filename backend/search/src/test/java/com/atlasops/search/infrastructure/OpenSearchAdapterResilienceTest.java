package com.atlasops.search.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.atlasops.search.domain.SearchQuery;
import com.atlasops.search.domain.ports.SearchIndexPort;
import com.atlasops.search.domain.ports.SearchIndexUpdatePort;
import com.atlasops.shared.domain.ports.FeatureFlagPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

class OpenSearchAdapterResilienceTest {

  @Test
  @DisplayName("should_failFast_when_opensearchFeatureFlagIsEnabled")
  void should_failFast_when_opensearchFeatureFlagIsEnabled() {
    SearchIndexPort fallback = mock(SearchIndexPort.class);
    SearchIndexUpdatePort fallbackUpdate = mock(SearchIndexUpdatePort.class);
    FeatureFlagPort flags = mock(FeatureFlagPort.class);
    when(flags.isEnabled("opensearch")).thenReturn(true);

    OpenSearchAdapter adapter = new OpenSearchAdapter(fallback, fallbackUpdate, flags);

    assertThatThrownBy(() -> adapter.search(SearchQuery.withoutFilter("acme", "tenant-alpha"), PageRequest.of(0, 10)))
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessageContaining("OpenSearch client not yet configured");
  }
}
