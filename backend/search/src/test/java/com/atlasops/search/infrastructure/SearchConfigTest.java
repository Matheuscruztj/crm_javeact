package com.atlasops.search.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.atlasops.search.domain.ports.SearchIndexPort;
import com.atlasops.shared.domain.ports.FeatureFlagPort;
import org.junit.jupiter.api.Test;

class SearchConfigTest {

  @Test
  void should_createPrimarySearchIndexPortBean() {
    SearchConfig config = new SearchConfig();
    PostgresFullTextSearchAdapter postgresAdapter = mock(PostgresFullTextSearchAdapter.class);
    FeatureFlagPort featureFlagPort = mock(FeatureFlagPort.class);
    when(featureFlagPort.isEnabled("opensearch")).thenReturn(false);

    SearchIndexPort searchIndexPort = config.searchIndexPort(postgresAdapter, featureFlagPort);

    assertThat(searchIndexPort).isInstanceOf(OpenSearchAdapter.class);
  }
}
