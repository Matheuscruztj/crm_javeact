package com.atlasops.search.infrastructure;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class Neo4jRelationshipAdapterResilienceTest {

  @Test
  @DisplayName("should_ignoreQuery_when_neo4jAdapterIsInvoked")
  void should_ignoreQuery_when_neo4jAdapterIsInvoked() {
    Neo4jRelationshipAdapter adapter = new Neo4jRelationshipAdapter();

    assertThatCode(adapter::queryRelationships).doesNotThrowAnyException();
  }
}
