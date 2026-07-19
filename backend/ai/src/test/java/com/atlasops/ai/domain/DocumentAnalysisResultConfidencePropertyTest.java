package com.atlasops.ai.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import net.jqwik.api.*;

/**
 * Property 3: DocumentAnalysisResult confidence score bounds.
 *
 * <p><b>Validates: Requirements 6.2</b>
 */
@Tag(
    "Feature: project-adequation-restructure, Property 3: DocumentAnalysisResult confidence score"
        + " bounds")
class DocumentAnalysisResultConfidencePropertyTest {

  @Property(tries = 100)
  void should_rejectResult_when_confidenceScoreBelowZero(
      @ForAll("negativeScores") double negativeScore) {
    assertThatThrownBy(
            () ->
                new DocumentAnalysisResult(
                    "summary",
                    "category",
                    List.of(),
                    List.of(),
                    List.of(),
                    negativeScore,
                    "provider:v1",
                    false))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Property(tries = 100)
  void should_rejectResult_when_confidenceScoreAboveOne(
      @ForAll("overOneScores") double overOneScore) {
    assertThatThrownBy(
            () ->
                new DocumentAnalysisResult(
                    "summary",
                    "category",
                    List.of(),
                    List.of(),
                    List.of(),
                    overOneScore,
                    "provider:v1",
                    false))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Property(tries = 100)
  void should_acceptResult_when_confidenceScoreWithinBounds(
      @ForAll("validScores") double validScore) {
    var result =
        new DocumentAnalysisResult(
            "summary",
            "category",
            List.of(),
            List.of(),
            List.of(),
            validScore,
            "provider:v1",
            false);

    assertThat(result.confidenceScore()).isEqualTo(validScore);
  }

  @Provide
  Arbitrary<Double> negativeScores() {
    return Arbitraries.doubles().lessOrEqual(-0.01).ofScale(2);
  }

  @Provide
  Arbitrary<Double> overOneScores() {
    return Arbitraries.doubles().greaterOrEqual(1.01).ofScale(2);
  }

  @Provide
  Arbitrary<Double> validScores() {
    return Arbitraries.doubles().between(0.0, 1.0).ofScale(2);
  }
}
