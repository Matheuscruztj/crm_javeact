package com.atlasops.ai.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("IngestionResult domain record")
class IngestionResultTest {

  @Test
  @DisplayName("should create successful ingestion result")
  void should_createSuccessResult_when_usingFactoryMethod() {
    var result = IngestionResult.success("doc-123", 5, List.of("c1", "c2", "c3", "c4", "c5"));

    assertEquals("doc-123", result.documentId());
    assertEquals(IngestionResult.IngestionStatus.SUCCESS, result.status());
    assertEquals(5, result.chunksCreated());
    assertEquals(List.of("c1", "c2", "c3", "c4", "c5"), result.chunkIds());
    assertNull(result.failureReason());
  }

  @Test
  @DisplayName("should create failed ingestion result")
  void should_createFailedResult_when_usingFactoryMethod() {
    var result = IngestionResult.failed("doc-456", "No text content found");

    assertEquals("doc-456", result.documentId());
    assertEquals(IngestionResult.IngestionStatus.FAILED, result.status());
    assertEquals(0, result.chunksCreated());
    assertEquals(List.of(), result.chunkIds());
    assertEquals("No text content found", result.failureReason());
  }

  @Test
  @DisplayName("should reject null documentId")
  void should_rejectResult_when_documentIdIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            new IngestionResult(
                null, IngestionResult.IngestionStatus.SUCCESS, 1, List.of("c1"), null));
  }

  @Test
  @DisplayName("should reject blank documentId")
  void should_rejectResult_when_documentIdIsBlank() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new IngestionResult(
                "  ", IngestionResult.IngestionStatus.SUCCESS, 1, List.of("c1"), null));
  }

  @Test
  @DisplayName("should reject null status")
  void should_rejectResult_when_statusIsNull() {
    assertThrows(
        NullPointerException.class,
        () -> new IngestionResult("doc-1", null, 1, List.of("c1"), null));
  }

  @Test
  @DisplayName("should reject negative chunksCreated")
  void should_rejectResult_when_chunksCreatedIsNegative() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new IngestionResult(
                "doc-1", IngestionResult.IngestionStatus.SUCCESS, -1, List.of(), null));
  }

  @Test
  @DisplayName("should reject null chunkIds list")
  void should_rejectResult_when_chunkIdsIsNull() {
    assertThrows(
        NullPointerException.class,
        () -> new IngestionResult("doc-1", IngestionResult.IngestionStatus.SUCCESS, 0, null, null));
  }

  @Test
  @DisplayName("should make chunkIds immutable")
  void should_makeChunkIdsImmutable_when_created() {
    var result = IngestionResult.success("doc-1", 1, List.of("c1"));

    assertThrows(UnsupportedOperationException.class, () -> result.chunkIds().add("c2"));
  }
}
