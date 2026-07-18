package com.atlasops.ai.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AIAnalysisRecord domain entity")
class AIAnalysisRecordTest {

  private static final String VALID_ID = "analysis-001";
  private static final String VALID_TENANT_ID = "tenant-1";
  private static final String VALID_MODEL = "llama3.1:8b";
  private static final String VALID_PROMPT_VERSION = "document-analysis:v3";
  private static final String VALID_INPUT_HASH =
      "a1b2c3d4e5f6789012345678901234567890123456789012345678901234abcd";
  private static final long VALID_DURATION_MS = 1500L;
  private static final double VALID_CONFIDENCE_SCORE = 0.87;
  private static final boolean VALID_FALLBACK = false;
  private static final String VALID_RESULT = "The document describes a service contract.";
  private static final List<String> VALID_CHUNKS_USED = List.of("chunk-1", "chunk-2", "chunk-3");
  private static final Instant VALID_CREATED_AT = Instant.parse("2024-01-15T10:30:00Z");

  private AIAnalysisRecord createValidRecord() {
    return new AIAnalysisRecord(
        VALID_ID,
        VALID_TENANT_ID,
        VALID_MODEL,
        VALID_PROMPT_VERSION,
        VALID_INPUT_HASH,
        VALID_DURATION_MS,
        VALID_CONFIDENCE_SCORE,
        VALID_FALLBACK,
        VALID_RESULT,
        VALID_CHUNKS_USED,
        VALID_CREATED_AT);
  }

  @Test
  @DisplayName("should create valid AIAnalysisRecord with all fields")
  void should_createValidRecord_when_allFieldsProvided() {
    var record = createValidRecord();

    assertEquals(VALID_ID, record.getId());
    assertEquals(VALID_TENANT_ID, record.getTenantId());
    assertEquals(VALID_MODEL, record.getModel());
    assertEquals(VALID_PROMPT_VERSION, record.getPromptVersion());
    assertEquals(VALID_INPUT_HASH, record.getInputHash());
    assertEquals(VALID_DURATION_MS, record.getDurationMs());
    assertEquals(VALID_CONFIDENCE_SCORE, record.getConfidenceScore());
    assertEquals(VALID_FALLBACK, record.isFallback());
    assertEquals(VALID_RESULT, record.getResult());
    assertEquals(VALID_CHUNKS_USED, record.getChunksUsed());
    assertEquals(VALID_CREATED_AT, record.getCreatedAt());
  }

  @Test
  @DisplayName("should reject null id")
  void should_rejectRecord_when_idIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            new AIAnalysisRecord(
                null,
                VALID_TENANT_ID,
                VALID_MODEL,
                VALID_PROMPT_VERSION,
                VALID_INPUT_HASH,
                VALID_DURATION_MS,
                VALID_CONFIDENCE_SCORE,
                VALID_FALLBACK,
                VALID_RESULT,
                VALID_CHUNKS_USED,
                VALID_CREATED_AT));
  }

  @Test
  @DisplayName("should reject null tenantId")
  void should_rejectRecord_when_tenantIdIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            new AIAnalysisRecord(
                VALID_ID,
                null,
                VALID_MODEL,
                VALID_PROMPT_VERSION,
                VALID_INPUT_HASH,
                VALID_DURATION_MS,
                VALID_CONFIDENCE_SCORE,
                VALID_FALLBACK,
                VALID_RESULT,
                VALID_CHUNKS_USED,
                VALID_CREATED_AT));
  }

  @Test
  @DisplayName("should reject blank tenantId")
  void should_rejectRecord_when_tenantIdIsBlank() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new AIAnalysisRecord(
                VALID_ID,
                "  ",
                VALID_MODEL,
                VALID_PROMPT_VERSION,
                VALID_INPUT_HASH,
                VALID_DURATION_MS,
                VALID_CONFIDENCE_SCORE,
                VALID_FALLBACK,
                VALID_RESULT,
                VALID_CHUNKS_USED,
                VALID_CREATED_AT));
  }

  @Test
  @DisplayName("should reject null model")
  void should_rejectRecord_when_modelIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            new AIAnalysisRecord(
                VALID_ID,
                VALID_TENANT_ID,
                null,
                VALID_PROMPT_VERSION,
                VALID_INPUT_HASH,
                VALID_DURATION_MS,
                VALID_CONFIDENCE_SCORE,
                VALID_FALLBACK,
                VALID_RESULT,
                VALID_CHUNKS_USED,
                VALID_CREATED_AT));
  }

  @Test
  @DisplayName("should reject blank model")
  void should_rejectRecord_when_modelIsBlank() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new AIAnalysisRecord(
                VALID_ID,
                VALID_TENANT_ID,
                "",
                VALID_PROMPT_VERSION,
                VALID_INPUT_HASH,
                VALID_DURATION_MS,
                VALID_CONFIDENCE_SCORE,
                VALID_FALLBACK,
                VALID_RESULT,
                VALID_CHUNKS_USED,
                VALID_CREATED_AT));
  }

  @Test
  @DisplayName("should reject confidence score below 0.0")
  void should_rejectRecord_when_confidenceScoreBelowZero() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new AIAnalysisRecord(
                VALID_ID,
                VALID_TENANT_ID,
                VALID_MODEL,
                VALID_PROMPT_VERSION,
                VALID_INPUT_HASH,
                VALID_DURATION_MS,
                -0.001,
                VALID_FALLBACK,
                VALID_RESULT,
                VALID_CHUNKS_USED,
                VALID_CREATED_AT));
  }

  @Test
  @DisplayName("should reject confidence score above 1.0")
  void should_rejectRecord_when_confidenceScoreAboveOne() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new AIAnalysisRecord(
                VALID_ID,
                VALID_TENANT_ID,
                VALID_MODEL,
                VALID_PROMPT_VERSION,
                VALID_INPUT_HASH,
                VALID_DURATION_MS,
                1.001,
                VALID_FALLBACK,
                VALID_RESULT,
                VALID_CHUNKS_USED,
                VALID_CREATED_AT));
  }

  @Test
  @DisplayName("should reject negative durationMs")
  void should_rejectRecord_when_durationMsIsNegative() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new AIAnalysisRecord(
                VALID_ID,
                VALID_TENANT_ID,
                VALID_MODEL,
                VALID_PROMPT_VERSION,
                VALID_INPUT_HASH,
                -1L,
                VALID_CONFIDENCE_SCORE,
                VALID_FALLBACK,
                VALID_RESULT,
                VALID_CHUNKS_USED,
                VALID_CREATED_AT));
  }

  @Test
  @DisplayName("should reject null inputHash")
  void should_rejectRecord_when_inputHashIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            new AIAnalysisRecord(
                VALID_ID,
                VALID_TENANT_ID,
                VALID_MODEL,
                VALID_PROMPT_VERSION,
                null,
                VALID_DURATION_MS,
                VALID_CONFIDENCE_SCORE,
                VALID_FALLBACK,
                VALID_RESULT,
                VALID_CHUNKS_USED,
                VALID_CREATED_AT));
  }

  @Test
  @DisplayName("should reject blank inputHash")
  void should_rejectRecord_when_inputHashIsBlank() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new AIAnalysisRecord(
                VALID_ID,
                VALID_TENANT_ID,
                VALID_MODEL,
                VALID_PROMPT_VERSION,
                "  ",
                VALID_DURATION_MS,
                VALID_CONFIDENCE_SCORE,
                VALID_FALLBACK,
                VALID_RESULT,
                VALID_CHUNKS_USED,
                VALID_CREATED_AT));
  }

  @Test
  @DisplayName("should make chunksUsed immutable")
  void should_makeChunksUsedImmutable_when_created() {
    var record = createValidRecord();

    assertThrows(
        UnsupportedOperationException.class, () -> record.getChunksUsed().add("chunk-new"));
  }

  @Test
  @DisplayName("should accept boundary confidence scores")
  void should_acceptBoundaryConfidenceScores_when_zeroOrOne() {
    var recordZero =
        new AIAnalysisRecord(
            VALID_ID,
            VALID_TENANT_ID,
            VALID_MODEL,
            VALID_PROMPT_VERSION,
            VALID_INPUT_HASH,
            VALID_DURATION_MS,
            0.0,
            VALID_FALLBACK,
            VALID_RESULT,
            VALID_CHUNKS_USED,
            VALID_CREATED_AT);

    var recordOne =
        new AIAnalysisRecord(
            "id-2",
            VALID_TENANT_ID,
            VALID_MODEL,
            VALID_PROMPT_VERSION,
            VALID_INPUT_HASH,
            VALID_DURATION_MS,
            1.0,
            VALID_FALLBACK,
            VALID_RESULT,
            VALID_CHUNKS_USED,
            VALID_CREATED_AT);

    assertEquals(0.0, recordZero.getConfidenceScore());
    assertEquals(1.0, recordOne.getConfidenceScore());
  }

  @Test
  @DisplayName("should accept zero durationMs")
  void should_acceptZeroDuration_when_durationIsZero() {
    var record =
        new AIAnalysisRecord(
            VALID_ID,
            VALID_TENANT_ID,
            VALID_MODEL,
            VALID_PROMPT_VERSION,
            VALID_INPUT_HASH,
            0L,
            VALID_CONFIDENCE_SCORE,
            VALID_FALLBACK,
            VALID_RESULT,
            VALID_CHUNKS_USED,
            VALID_CREATED_AT);

    assertEquals(0L, record.getDurationMs());
  }

  @Test
  @DisplayName("should create fallback record")
  void should_createFallbackRecord_when_fallbackIsTrue() {
    var record =
        new AIAnalysisRecord(
            VALID_ID,
            VALID_TENANT_ID,
            VALID_MODEL,
            VALID_PROMPT_VERSION,
            VALID_INPUT_HASH,
            50L,
            0.0,
            true,
            "Ollama unavailable",
            List.of(),
            VALID_CREATED_AT);

    assertTrue(record.isFallback());
    assertEquals("Ollama unavailable", record.getResult());
    assertTrue(record.getChunksUsed().isEmpty());
  }

  @Test
  @DisplayName("should support entity equality by id")
  void should_beEqual_when_sameId() {
    var record1 = createValidRecord();
    var record2 =
        new AIAnalysisRecord(
            VALID_ID,
            "other-tenant",
            "other-model",
            "v2",
            "otherhash",
            200L,
            0.5,
            true,
            "other result",
            List.of(),
            Instant.now());

    assertEquals(record1, record2);
    assertEquals(record1.hashCode(), record2.hashCode());
  }

  @Test
  @DisplayName("should not be equal when different id")
  void should_notBeEqual_when_differentId() {
    var record1 = createValidRecord();
    var record2 =
        new AIAnalysisRecord(
            "different-id",
            VALID_TENANT_ID,
            VALID_MODEL,
            VALID_PROMPT_VERSION,
            VALID_INPUT_HASH,
            VALID_DURATION_MS,
            VALID_CONFIDENCE_SCORE,
            VALID_FALLBACK,
            VALID_RESULT,
            VALID_CHUNKS_USED,
            VALID_CREATED_AT);

    assertNotEquals(record1, record2);
  }
}
