package com.atlasops.ai.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("AI domain value objects")
class ValueObjectsTest {

  @Nested
  @DisplayName("AnalysisId")
  class AnalysisIdTest {

    @Test
    @DisplayName("should create valid AnalysisId")
    void should_createValidId_when_valueProvided() {
      var id = new AnalysisId("analysis-001");
      assertEquals("analysis-001", id.getValue());
    }

    @Test
    @DisplayName("should reject null value")
    void should_rejectId_when_valueIsNull() {
      assertThrows(IllegalArgumentException.class, () -> new AnalysisId(null));
    }

    @Test
    @DisplayName("should reject blank value")
    void should_rejectId_when_valueIsBlank() {
      assertThrows(IllegalArgumentException.class, () -> new AnalysisId("  "));
    }

    @Test
    @DisplayName("should be equal when same value")
    void should_beEqual_when_sameValue() {
      var id1 = new AnalysisId("abc-123");
      var id2 = new AnalysisId("abc-123");
      assertEquals(id1, id2);
      assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    @DisplayName("should not be equal when different value")
    void should_notBeEqual_when_differentValue() {
      var id1 = new AnalysisId("abc-123");
      var id2 = new AnalysisId("xyz-789");
      assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("should have meaningful toString")
    void should_haveMeaningfulToString() {
      var id = new AnalysisId("analysis-001");
      assertTrue(id.toString().contains("analysis-001"));
    }
  }

  @Nested
  @DisplayName("DocumentId")
  class DocumentIdTest {

    @Test
    @DisplayName("should create valid DocumentId")
    void should_createValidId_when_valueProvided() {
      var id = new DocumentId("doc-456");
      assertEquals("doc-456", id.getValue());
    }

    @Test
    @DisplayName("should reject null value")
    void should_rejectId_when_valueIsNull() {
      assertThrows(IllegalArgumentException.class, () -> new DocumentId(null));
    }

    @Test
    @DisplayName("should reject blank value")
    void should_rejectId_when_valueIsBlank() {
      assertThrows(IllegalArgumentException.class, () -> new DocumentId(""));
    }

    @Test
    @DisplayName("should be equal when same value")
    void should_beEqual_when_sameValue() {
      var id1 = new DocumentId("doc-123");
      var id2 = new DocumentId("doc-123");
      assertEquals(id1, id2);
      assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    @DisplayName("should not be equal when different value")
    void should_notBeEqual_when_differentValue() {
      var id1 = new DocumentId("doc-123");
      var id2 = new DocumentId("doc-789");
      assertNotEquals(id1, id2);
    }

    @Test
    @DisplayName("should have meaningful toString")
    void should_haveMeaningfulToString() {
      var id = new DocumentId("doc-456");
      assertTrue(id.toString().contains("doc-456"));
    }
  }
}
