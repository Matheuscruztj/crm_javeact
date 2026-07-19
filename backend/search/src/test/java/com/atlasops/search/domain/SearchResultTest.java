package com.atlasops.search.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("SearchResult value object")
class SearchResultTest {

  private static final String VALID_ENTITY_TYPE = "CUSTOMER";
  private static final String VALID_ENTITY_ID = "cust-001";
  private static final String VALID_TITLE = "Acme Corporation";
  private static final String VALID_SNIPPET = "Leading provider of...";
  private static final double VALID_SCORE = 0.85;

  @Nested
  @DisplayName("Construction")
  class Construction {

    @Test
    @DisplayName("should create valid SearchResult when all fields provided")
    void should_createSearchResult_when_allFieldsProvided() {
      var result =
          new SearchResult(
              VALID_ENTITY_TYPE, VALID_ENTITY_ID, VALID_TITLE, VALID_SNIPPET, VALID_SCORE);

      assertThat(result.entityType()).isEqualTo(VALID_ENTITY_TYPE);
      assertThat(result.entityId()).isEqualTo(VALID_ENTITY_ID);
      assertThat(result.title()).isEqualTo(VALID_TITLE);
      assertThat(result.snippet()).isEqualTo(VALID_SNIPPET);
      assertThat(result.relevanceScore()).isEqualTo(VALID_SCORE);
    }

    @Test
    @DisplayName("should create SearchResult when snippet is empty")
    void should_createSearchResult_when_snippetIsEmpty() {
      var result =
          new SearchResult(VALID_ENTITY_TYPE, VALID_ENTITY_ID, VALID_TITLE, "", VALID_SCORE);

      assertThat(result.snippet()).isEmpty();
    }

    @Test
    @DisplayName("should create SearchResult when snippet is exactly 200 characters")
    void should_createSearchResult_when_snippetIsMaxLength() {
      var maxSnippet = "x".repeat(200);
      var result =
          new SearchResult(
              VALID_ENTITY_TYPE, VALID_ENTITY_ID, VALID_TITLE, maxSnippet, VALID_SCORE);

      assertThat(result.snippet()).hasSize(200);
    }

    @Test
    @DisplayName("should create SearchResult when relevanceScore is 0.0")
    void should_createSearchResult_when_scoreIsZero() {
      var result =
          new SearchResult(VALID_ENTITY_TYPE, VALID_ENTITY_ID, VALID_TITLE, VALID_SNIPPET, 0.0);

      assertThat(result.relevanceScore()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("should create SearchResult when relevanceScore is 1.0")
    void should_createSearchResult_when_scoreIsOne() {
      var result =
          new SearchResult(VALID_ENTITY_TYPE, VALID_ENTITY_ID, VALID_TITLE, VALID_SNIPPET, 1.0);

      assertThat(result.relevanceScore()).isEqualTo(1.0);
    }
  }

  @Nested
  @DisplayName("Null validation")
  class NullValidation {

    @Test
    @DisplayName("should reject null entityType")
    void should_rejectResult_when_entityTypeIsNull() {
      assertThatThrownBy(
              () ->
                  new SearchResult(null, VALID_ENTITY_ID, VALID_TITLE, VALID_SNIPPET, VALID_SCORE))
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining("EntityType must not be null");
    }

    @Test
    @DisplayName("should reject null entityId")
    void should_rejectResult_when_entityIdIsNull() {
      assertThatThrownBy(
              () ->
                  new SearchResult(
                      VALID_ENTITY_TYPE, null, VALID_TITLE, VALID_SNIPPET, VALID_SCORE))
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining("EntityId must not be null");
    }

    @Test
    @DisplayName("should reject null title")
    void should_rejectResult_when_titleIsNull() {
      assertThatThrownBy(
              () ->
                  new SearchResult(
                      VALID_ENTITY_TYPE, VALID_ENTITY_ID, null, VALID_SNIPPET, VALID_SCORE))
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining("Title must not be null");
    }

    @Test
    @DisplayName("should reject null snippet")
    void should_rejectResult_when_snippetIsNull() {
      assertThatThrownBy(
              () ->
                  new SearchResult(
                      VALID_ENTITY_TYPE, VALID_ENTITY_ID, VALID_TITLE, null, VALID_SCORE))
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining("Snippet must not be null");
    }
  }

  @Nested
  @DisplayName("Blank validation")
  class BlankValidation {

    @Test
    @DisplayName("should reject blank entityType")
    void should_rejectResult_when_entityTypeIsBlank() {
      assertThatThrownBy(
              () ->
                  new SearchResult("  ", VALID_ENTITY_ID, VALID_TITLE, VALID_SNIPPET, VALID_SCORE))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("EntityType must not be blank");
    }

    @Test
    @DisplayName("should reject blank entityId")
    void should_rejectResult_when_entityIdIsBlank() {
      assertThatThrownBy(
              () ->
                  new SearchResult(
                      VALID_ENTITY_TYPE, "  ", VALID_TITLE, VALID_SNIPPET, VALID_SCORE))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("EntityId must not be blank");
    }

    @Test
    @DisplayName("should reject blank title")
    void should_rejectResult_when_titleIsBlank() {
      assertThatThrownBy(
              () ->
                  new SearchResult(
                      VALID_ENTITY_TYPE, VALID_ENTITY_ID, "  ", VALID_SNIPPET, VALID_SCORE))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Title must not be blank");
    }
  }

  @Nested
  @DisplayName("Snippet validation")
  class SnippetValidation {

    @Test
    @DisplayName("should reject snippet longer than 200 characters")
    void should_rejectResult_when_snippetIsTooLong() {
      var longSnippet = "x".repeat(201);
      assertThatThrownBy(
              () ->
                  new SearchResult(
                      VALID_ENTITY_TYPE, VALID_ENTITY_ID, VALID_TITLE, longSnippet, VALID_SCORE))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("at most 200 characters");
    }
  }

  @Nested
  @DisplayName("RelevanceScore validation")
  class RelevanceScoreValidation {

    @Test
    @DisplayName("should reject negative relevanceScore")
    void should_rejectResult_when_scoreIsNegative() {
      assertThatThrownBy(
              () ->
                  new SearchResult(
                      VALID_ENTITY_TYPE, VALID_ENTITY_ID, VALID_TITLE, VALID_SNIPPET, -0.01))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("between 0.0 and 1.0");
    }

    @Test
    @DisplayName("should reject relevanceScore greater than 1.0")
    void should_rejectResult_when_scoreIsAboveOne() {
      assertThatThrownBy(
              () ->
                  new SearchResult(
                      VALID_ENTITY_TYPE, VALID_ENTITY_ID, VALID_TITLE, VALID_SNIPPET, 1.01))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("between 0.0 and 1.0");
    }
  }

  @Nested
  @DisplayName("Equality")
  class Equality {

    @Test
    @DisplayName("should be equal when same values")
    void should_beEqual_when_sameValues() {
      var result1 =
          new SearchResult(
              VALID_ENTITY_TYPE, VALID_ENTITY_ID, VALID_TITLE, VALID_SNIPPET, VALID_SCORE);
      var result2 =
          new SearchResult(
              VALID_ENTITY_TYPE, VALID_ENTITY_ID, VALID_TITLE, VALID_SNIPPET, VALID_SCORE);

      assertThat(result1).isEqualTo(result2);
      assertThat(result1.hashCode()).isEqualTo(result2.hashCode());
    }

    @Test
    @DisplayName("should not be equal when different entityId")
    void should_notBeEqual_when_differentEntityId() {
      var result1 =
          new SearchResult(
              VALID_ENTITY_TYPE, VALID_ENTITY_ID, VALID_TITLE, VALID_SNIPPET, VALID_SCORE);
      var result2 =
          new SearchResult(VALID_ENTITY_TYPE, "cust-999", VALID_TITLE, VALID_SNIPPET, VALID_SCORE);

      assertThat(result1).isNotEqualTo(result2);
    }
  }
}
