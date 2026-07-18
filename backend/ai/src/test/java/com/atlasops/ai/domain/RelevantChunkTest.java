package com.atlasops.ai.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RelevantChunk domain record")
class RelevantChunkTest {

  @Test
  @DisplayName("should create valid RelevantChunk with all fields")
  void should_createValidChunk_when_allFieldsProvided() {
    var chunk = new RelevantChunk("chunk-1", "Some content here", 0.92, "doc-123");

    assertEquals("chunk-1", chunk.chunkId());
    assertEquals("Some content here", chunk.content());
    assertEquals(0.92, chunk.score());
    assertEquals("doc-123", chunk.documentId());
  }

  @Test
  @DisplayName("should reject null chunkId")
  void should_rejectChunk_when_chunkIdIsNull() {
    assertThrows(
        NullPointerException.class, () -> new RelevantChunk(null, "content", 0.8, "doc-1"));
  }

  @Test
  @DisplayName("should reject blank chunkId")
  void should_rejectChunk_when_chunkIdIsBlank() {
    assertThrows(
        IllegalArgumentException.class, () -> new RelevantChunk("  ", "content", 0.8, "doc-1"));
  }

  @Test
  @DisplayName("should reject null content")
  void should_rejectChunk_when_contentIsNull() {
    assertThrows(
        NullPointerException.class, () -> new RelevantChunk("chunk-1", null, 0.8, "doc-1"));
  }

  @Test
  @DisplayName("should reject null documentId")
  void should_rejectChunk_when_documentIdIsNull() {
    assertThrows(
        NullPointerException.class, () -> new RelevantChunk("chunk-1", "content", 0.8, null));
  }

  @Test
  @DisplayName("should reject blank documentId")
  void should_rejectChunk_when_documentIdIsBlank() {
    assertThrows(
        IllegalArgumentException.class, () -> new RelevantChunk("chunk-1", "content", 0.8, ""));
  }

  @Test
  @DisplayName("should reject score below 0.0")
  void should_rejectChunk_when_scoreBelowZero() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new RelevantChunk("chunk-1", "content", -0.01, "doc-1"));
  }

  @Test
  @DisplayName("should reject score above 1.0")
  void should_rejectChunk_when_scoreAboveOne() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new RelevantChunk("chunk-1", "content", 1.01, "doc-1"));
  }

  @Test
  @DisplayName("should accept boundary scores 0.0 and 1.0")
  void should_acceptBoundaryScores_when_zeroOrOne() {
    var chunkZero = new RelevantChunk("c-1", "content", 0.0, "doc-1");
    var chunkOne = new RelevantChunk("c-2", "content", 1.0, "doc-1");

    assertEquals(0.0, chunkZero.score());
    assertEquals(1.0, chunkOne.score());
  }
}
