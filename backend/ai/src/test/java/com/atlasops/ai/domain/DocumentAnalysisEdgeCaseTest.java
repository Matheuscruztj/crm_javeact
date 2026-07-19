package com.atlasops.ai.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for AI module edge cases. Validates: Requirements 6.1, 6.2, 6.6 */
@Tag("Feature: project-adequation-restructure")
class DocumentAnalysisEdgeCaseTest {

  @Test
  void should_throwException_when_extractedTextExactly100001Chars() {
    String text = "a".repeat(100_001);

    assertThatThrownBy(
            () -> new DocumentAnalysisRequest("tenant-1", "doc-1", text, "analysis:v1", "schema"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("100000");
  }

  @Test
  void should_acceptText_when_extractedTextExactly100000Chars() {
    String text = "a".repeat(100_000);

    var request = new DocumentAnalysisRequest("tenant-1", "doc-1", text, "analysis:v1", "schema");

    assertThat(request.extractedText()).hasSize(100_000);
  }

  @Test
  void should_throwException_when_promptVersionMissingColon() {
    assertThatThrownBy(
            () -> new DocumentAnalysisRequest("tenant-1", "doc-1", "text", "analysisv1", "schema"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("promptVersion");
  }

  @Test
  void should_throwException_when_confidenceScoreIsNegativeEpsilon() {
    double negativeEpsilon = -Double.MIN_VALUE;

    assertThatThrownBy(
            () ->
                new DocumentAnalysisResult(
                    "summary",
                    "category",
                    List.of(),
                    List.of(),
                    List.of(),
                    negativeEpsilon,
                    "provider:v1",
                    false))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("confidenceScore");
  }

  @Test
  void should_createImmutableLists_when_mutableListProvided() {
    var mutableFields = new ArrayList<>(List.of(new KeyValuePair("k", "v")));
    var mutableRisks = new ArrayList<>(List.of("risk"));
    var mutableMissing = new ArrayList<>(List.of("info"));

    var result =
        new DocumentAnalysisResult(
            "summary",
            "category",
            mutableFields,
            mutableRisks,
            mutableMissing,
            0.5,
            "provider:v1",
            false);

    // Mutating original list should NOT affect the record's list
    mutableFields.add(new KeyValuePair("new", "val"));
    mutableRisks.add("new risk");
    mutableMissing.add("new missing");

    assertThat(result.extractedFields()).hasSize(1);
    assertThat(result.risks()).hasSize(1);
    assertThat(result.missingInformation()).hasSize(1);

    // Record's lists should be unmodifiable
    assertThatThrownBy(() -> result.extractedFields().add(new KeyValuePair("x", "y")))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> result.risks().add("x"))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThatThrownBy(() -> result.missingInformation().add("x"))
        .isInstanceOf(UnsupportedOperationException.class);
  }
}
