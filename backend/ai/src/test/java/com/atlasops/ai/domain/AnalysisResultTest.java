package com.atlasops.ai.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AnalysisResult domain record")
class AnalysisResultTest {

  @Test
  @DisplayName("should create valid AnalysisResult with all fields")
  void should_createValidResult_when_allFieldsProvided() {
    var result =
        new AnalysisResult(
            "This document describes a contract",
            0.85,
            List.of("chunk-1", "chunk-2"),
            false,
            1200L);

    assertEquals("This document describes a contract", result.result());
    assertEquals(0.85, result.confidenceScore());
    assertEquals(List.of("chunk-1", "chunk-2"), result.chunksUsed());
    assertFalse(result.fallback());
    assertEquals(1200L, result.durationMs());
  }

  @Test
  @DisplayName("should create fallback result")
  void should_createFallbackResult_when_fallbackIsTrue() {
    var result = new AnalysisResult("Service unavailable", 0.0, List.of(), true, 50L);

    assertTrue(result.fallback());
    assertEquals(0.0, result.confidenceScore());
  }

  @Test
  @DisplayName("should reject null result text")
  void should_rejectResult_when_resultIsNull() {
    assertThrows(
        NullPointerException.class, () -> new AnalysisResult(null, 0.5, List.of(), false, 100L));
  }

  @Test
  @DisplayName("should reject null chunksUsed")
  void should_rejectResult_when_chunksUsedIsNull() {
    assertThrows(
        NullPointerException.class, () -> new AnalysisResult("text", 0.5, null, false, 100L));
  }

  @Test
  @DisplayName("should reject confidence score below 0.0")
  void should_rejectResult_when_confidenceScoreBelowZero() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new AnalysisResult("text", -0.1, List.of(), false, 100L));
  }

  @Test
  @DisplayName("should reject confidence score above 1.0")
  void should_rejectResult_when_confidenceScoreAboveOne() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new AnalysisResult("text", 1.1, List.of(), false, 100L));
  }

  @Test
  @DisplayName("should reject negative durationMs")
  void should_rejectResult_when_durationIsNegative() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new AnalysisResult("text", 0.5, List.of(), false, -1L));
  }

  @Test
  @DisplayName("should make chunksUsed immutable")
  void should_makeChunksUsedImmutable_when_created() {
    var mutableList = new java.util.ArrayList<>(List.of("chunk-1"));
    var result = new AnalysisResult("text", 0.5, mutableList, false, 100L);

    assertThrows(UnsupportedOperationException.class, () -> result.chunksUsed().add("chunk-2"));
  }

  @Test
  @DisplayName("should accept boundary confidence scores 0.0 and 1.0")
  void should_acceptBoundaryScores_when_zeroOrOne() {
    var resultZero = new AnalysisResult("text", 0.0, List.of(), false, 100L);
    var resultOne = new AnalysisResult("text", 1.0, List.of(), false, 100L);

    assertEquals(0.0, resultZero.confidenceScore());
    assertEquals(1.0, resultOne.confidenceScore());
  }
}
