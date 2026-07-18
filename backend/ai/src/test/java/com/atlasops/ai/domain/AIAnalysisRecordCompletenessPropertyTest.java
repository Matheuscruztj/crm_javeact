package com.atlasops.ai.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.regex.Pattern;
import net.jqwik.api.*;
import net.jqwik.api.constraints.DoubleRange;
import net.jqwik.api.constraints.LongRange;

/**
 * Property-based tests for AI Analysis Record Completeness.
 *
 * <p><b>Validates: Requirements 4.8</b>
 *
 * <p>Property 6: For any AI analysis performed (successful or fallback), the system SHALL persist a
 * record containing all mandatory fields: model, promptVersion, inputHash (SHA-256), durationMs,
 * confidenceScore (0.0-1.0), fallback (boolean), and result (text).
 */
@Tag("Feature: monorepo-sdd-harness, Property 6: AI Analysis Record Completeness")
class AIAnalysisRecordCompletenessPropertyTest {

  private static final Pattern SHA256_PATTERN = Pattern.compile("^[0-9a-f]{64}$");

  // ─── Property: Any valid AI analysis record contains all mandatory fields ─────

  @Property(tries = 100)
  void anyValidRecord_shouldContainAllMandatoryFields(
      @ForAll("validModels") String model,
      @ForAll("validPromptVersions") String promptVersion,
      @ForAll("validSha256Hashes") String inputHash,
      @ForAll @LongRange(min = 0, max = 100_000) long durationMs,
      @ForAll @DoubleRange(min = 0.0, max = 1.0) double confidenceScore,
      @ForAll boolean fallback,
      @ForAll("validResults") String result) {

    var record =
        new AIAnalysisRecord(
            "id-" + System.nanoTime(),
            "tenant-1",
            model,
            promptVersion,
            inputHash,
            durationMs,
            confidenceScore,
            fallback,
            result,
            List.of("chunk-1"),
            Instant.now());

    assertThat(record.getModel()).isNotNull().isNotBlank();
    assertThat(record.getPromptVersion()).isNotNull().isNotBlank();
    assertThat(record.getInputHash()).isNotNull().isNotBlank();
    assertThat(record.getDurationMs()).isGreaterThanOrEqualTo(0L);
    assertThat(record.getConfidenceScore()).isBetween(0.0, 1.0);
    assertThat(record.getResult()).isNotNull();
  }

  // ─── Property: inputHash is always a valid SHA-256 hex string (64 hex chars) ──

  @Property(tries = 100)
  void inputHash_shouldAlwaysBeValidSha256HexString(@ForAll("validSha256Hashes") String inputHash) {

    var record =
        new AIAnalysisRecord(
            "id-" + System.nanoTime(),
            "tenant-1",
            "llama3.1:8b",
            "analysis:v1",
            inputHash,
            500L,
            0.85,
            false,
            "Analysis result",
            List.of(),
            Instant.now());

    assertThat(record.getInputHash())
        .as("inputHash '%s' should be a valid SHA-256 (64 hex characters)", record.getInputHash())
        .matches(SHA256_PATTERN);
    assertThat(record.getInputHash()).hasSize(64);
  }

  // ─── Property: confidenceScore is always between 0.0 and 1.0 ──────────────────

  @Property(tries = 100)
  void confidenceScore_shouldAlwaysBeBetweenZeroAndOne(
      @ForAll @DoubleRange(min = 0.0, max = 1.0) double confidenceScore) {

    var record =
        new AIAnalysisRecord(
            "id-" + System.nanoTime(),
            "tenant-1",
            "llama3.1:8b",
            "analysis:v1",
            "a1b2c3d4e5f6789012345678901234567890123456789012345678901234abcd",
            1000L,
            confidenceScore,
            false,
            "Result text",
            List.of(),
            Instant.now());

    assertThat(record.getConfidenceScore())
        .as("confidenceScore should be between 0.0 and 1.0, got: %f", record.getConfidenceScore())
        .isBetween(0.0, 1.0);
  }

  // ─── Property: durationMs is always non-negative ──────────────────────────────

  @Property(tries = 100)
  void durationMs_shouldAlwaysBeNonNegative(
      @ForAll @LongRange(min = 0, max = 500_000) long durationMs) {

    var record =
        new AIAnalysisRecord(
            "id-" + System.nanoTime(),
            "tenant-1",
            "llama3.1:8b",
            "analysis:v1",
            "a1b2c3d4e5f6789012345678901234567890123456789012345678901234abcd",
            durationMs,
            0.9,
            false,
            "Result text",
            List.of(),
            Instant.now());

    assertThat(record.getDurationMs())
        .as("durationMs should be non-negative, got: %d", record.getDurationMs())
        .isGreaterThanOrEqualTo(0L);
  }

  // ─── Property: model and promptVersion are never null or blank ─────────────────

  @Property(tries = 100)
  void modelAndPromptVersion_shouldNeverBeNullOrBlank(
      @ForAll("validModels") String model, @ForAll("validPromptVersions") String promptVersion) {

    var record =
        new AIAnalysisRecord(
            "id-" + System.nanoTime(),
            "tenant-1",
            model,
            promptVersion,
            "a1b2c3d4e5f6789012345678901234567890123456789012345678901234abcd",
            200L,
            0.75,
            false,
            "Some result",
            List.of("chunk-1"),
            Instant.now());

    assertThat(record.getModel())
        .as("model should never be null or blank")
        .isNotNull()
        .isNotBlank();
    assertThat(record.getPromptVersion())
        .as("promptVersion should never be null or blank")
        .isNotNull()
        .isNotBlank();
  }

  // ─── Property: Both successful and fallback records contain all required fields ─

  @Property(tries = 100)
  void bothSuccessfulAndFallbackRecords_shouldContainAllRequiredFields(
      @ForAll("validModels") String model,
      @ForAll("validPromptVersions") String promptVersion,
      @ForAll("validSha256Hashes") String inputHash,
      @ForAll @LongRange(min = 0, max = 100_000) long durationMs,
      @ForAll @DoubleRange(min = 0.0, max = 1.0) double confidenceScore,
      @ForAll boolean fallback,
      @ForAll("validResults") String result) {

    var record =
        new AIAnalysisRecord(
            "id-" + System.nanoTime(),
            "tenant-1",
            model,
            promptVersion,
            inputHash,
            durationMs,
            confidenceScore,
            fallback,
            result,
            fallback ? List.of() : List.of("chunk-1", "chunk-2"),
            Instant.now());

    // All mandatory fields must be present regardless of fallback status
    assertThat(record.getModel()).isNotNull().isNotBlank();
    assertThat(record.getPromptVersion()).isNotNull().isNotBlank();
    assertThat(record.getInputHash()).isNotNull().isNotBlank();
    assertThat(record.getInputHash()).matches(SHA256_PATTERN);
    assertThat(record.getDurationMs()).isGreaterThanOrEqualTo(0L);
    assertThat(record.getConfidenceScore()).isBetween(0.0, 1.0);
    assertThat(record.getResult()).isNotNull();

    // Fallback flag is correctly stored
    assertThat(record.isFallback()).isEqualTo(fallback);
  }

  // ─── Generators ──────────────────────────────────────────────────────────────

  @Provide
  Arbitrary<String> validModels() {
    return Arbitraries.of(
        "llama3.1:8b",
        "llama3.1:70b",
        "mistral:7b",
        "codellama:13b",
        "gemma:2b",
        "phi-3:mini",
        "qwen2:7b",
        "deepseek-coder:6.7b");
  }

  @Provide
  Arbitrary<String> validPromptVersions() {
    Arbitrary<String> names =
        Arbitraries.of(
            "document-analysis", "summarization", "classification", "extraction", "sentiment");
    Arbitrary<Integer> versions = Arbitraries.integers().between(1, 20);

    return Combinators.combine(names, versions).as((name, version) -> name + ":v" + version);
  }

  @Provide
  Arbitrary<String> validSha256Hashes() {
    // Generate valid 64-character hex strings (SHA-256)
    return Arbitraries.strings().withCharRange('0', '9').withCharRange('a', 'f').ofLength(64);
  }

  @Provide
  Arbitrary<String> validResults() {
    return Arbitraries.of(
        "The document describes a service contract.",
        "Classification: INVOICE with confidence 0.92",
        "Summary: Project requirements for Q4 delivery.",
        "Extracted entities: 3 persons, 2 organizations.",
        "Ollama unavailable - fallback response",
        "No relevant information found in context.",
        "Analysis complete with high confidence.");
  }
}
