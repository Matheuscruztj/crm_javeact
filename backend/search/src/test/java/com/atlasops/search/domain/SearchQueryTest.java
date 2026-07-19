package com.atlasops.search.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SearchQuery value object")
class SearchQueryTest {

  private static final String VALID_QUERY = "search term";
  private static final String VALID_TENANT_ID = "tenant-001";
  private static final String VALID_ENTITY_TYPE = "CUSTOMER";

  @Nested
  @DisplayName("Construction")
  class Construction {

    @Test
    @DisplayName("should create valid SearchQuery when all fields provided")
    void should_createSearchQuery_when_allFieldsProvided() {
      var searchQuery = new SearchQuery(VALID_QUERY, VALID_ENTITY_TYPE, VALID_TENANT_ID);

      assertThat(searchQuery.query()).isEqualTo(VALID_QUERY);
      assertThat(searchQuery.entityTypeFilter()).isEqualTo(VALID_ENTITY_TYPE);
      assertThat(searchQuery.tenantId()).isEqualTo(VALID_TENANT_ID);
    }

    @Test
    @DisplayName("should create valid SearchQuery when entityTypeFilter is null")
    void should_createSearchQuery_when_entityTypeFilterIsNull() {
      var searchQuery = new SearchQuery(VALID_QUERY, null, VALID_TENANT_ID);

      assertThat(searchQuery.query()).isEqualTo(VALID_QUERY);
      assertThat(searchQuery.entityTypeFilter()).isNull();
      assertThat(searchQuery.tenantId()).isEqualTo(VALID_TENANT_ID);
    }

    @Test
    @DisplayName("should create SearchQuery via factory method without filter")
    void should_createSearchQuery_when_usingWithoutFilterFactory() {
      var searchQuery = SearchQuery.withoutFilter(VALID_QUERY, VALID_TENANT_ID);

      assertThat(searchQuery.query()).isEqualTo(VALID_QUERY);
      assertThat(searchQuery.entityTypeFilter()).isNull();
      assertThat(searchQuery.tenantId()).isEqualTo(VALID_TENANT_ID);
    }

    @Test
    @DisplayName("should create SearchQuery when query is exactly 2 characters")
    void should_createSearchQuery_when_queryIsMinLength() {
      var searchQuery = new SearchQuery("ab", null, VALID_TENANT_ID);

      assertThat(searchQuery.query()).isEqualTo("ab");
    }

    @Test
    @DisplayName("should create SearchQuery when query is exactly 200 characters")
    void should_createSearchQuery_when_queryIsMaxLength() {
      var longQuery = "a".repeat(200);
      var searchQuery = new SearchQuery(longQuery, null, VALID_TENANT_ID);

      assertThat(searchQuery.query()).isEqualTo(longQuery);
    }
  }

  @Nested
  @DisplayName("Query validation")
  class QueryValidation {

    @Test
    @DisplayName("should reject null query")
    void should_rejectQuery_when_queryIsNull() {
      assertThatThrownBy(() -> new SearchQuery(null, null, VALID_TENANT_ID))
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining("Query must not be null");
    }

    @Test
    @DisplayName("should reject blank query")
    void should_rejectQuery_when_queryIsBlank() {
      assertThatThrownBy(() -> new SearchQuery("   ", null, VALID_TENANT_ID))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Query must not be blank");
    }

    @Test
    @DisplayName("should reject query shorter than 2 characters")
    void should_rejectQuery_when_queryIsTooShort() {
      assertThatThrownBy(() -> new SearchQuery("a", null, VALID_TENANT_ID))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("at least 2 characters");
    }

    @Test
    @DisplayName("should reject query longer than 200 characters")
    void should_rejectQuery_when_queryIsTooLong() {
      var longQuery = "a".repeat(201);
      assertThatThrownBy(() -> new SearchQuery(longQuery, null, VALID_TENANT_ID))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("at most 200 characters");
    }
  }

  @Nested
  @DisplayName("TenantId validation")
  class TenantIdValidation {

    @Test
    @DisplayName("should reject null tenantId")
    void should_rejectQuery_when_tenantIdIsNull() {
      assertThatThrownBy(() -> new SearchQuery(VALID_QUERY, null, null))
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining("TenantId must not be null");
    }

    @Test
    @DisplayName("should reject blank tenantId")
    void should_rejectQuery_when_tenantIdIsBlank() {
      assertThatThrownBy(() -> new SearchQuery(VALID_QUERY, null, "  "))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("TenantId must not be blank");
    }
  }

  @Nested
  @DisplayName("Equality")
  class Equality {

    @Test
    @DisplayName("should be equal when same values")
    void should_beEqual_when_sameValues() {
      var query1 = new SearchQuery(VALID_QUERY, VALID_ENTITY_TYPE, VALID_TENANT_ID);
      var query2 = new SearchQuery(VALID_QUERY, VALID_ENTITY_TYPE, VALID_TENANT_ID);

      assertThat(query1).isEqualTo(query2);
      assertThat(query1.hashCode()).isEqualTo(query2.hashCode());
    }

    @Test
    @DisplayName("should not be equal when different query text")
    void should_notBeEqual_when_differentQuery() {
      var query1 = new SearchQuery(VALID_QUERY, VALID_ENTITY_TYPE, VALID_TENANT_ID);
      var query2 = new SearchQuery("different", VALID_ENTITY_TYPE, VALID_TENANT_ID);

      assertThat(query1).isNotEqualTo(query2);
    }
  }
}
