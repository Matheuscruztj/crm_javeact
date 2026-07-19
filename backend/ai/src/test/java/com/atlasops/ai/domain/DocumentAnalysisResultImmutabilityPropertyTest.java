package com.atlasops.ai.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import net.jqwik.api.*;

/** Property 4: DocumentAnalysisResult list immutability. Validates: Requirements 6.2 */
@Tag(
    "Feature: project-adequation-restructure, Property 4: DocumentAnalysisResult list immutability")
class DocumentAnalysisResultImmutabilityPropertyTest {

  @Property(tries = 100)
  void should_throwException_when_extractedFieldsMutated(
      @ForAll("validScores") double confidenceScore) {

    var mutableFields = new ArrayList<>(List.of(new KeyValuePair("key", "value")));
    var result =
        new DocumentAnalysisResult(
            "summary",
            "category",
            mutableFields,
            List.of("risk"),
            List.of("missing"),
            confidenceScore,
            "provider:v1",
            false);

    assertThatThrownBy(() -> result.extractedFields().add(new KeyValuePair("new", "field")))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Property(tries = 100)
  void should_throwException_when_risksMutated(@ForAll("validScores") double confidenceScore) {

    var mutableRisks = new ArrayList<>(List.of("existing risk"));
    var result =
        new DocumentAnalysisResult(
            "summary",
            "category",
            List.of(),
            mutableRisks,
            List.of("missing"),
            confidenceScore,
            "provider:v1",
            false);

    assertThatThrownBy(() -> result.risks().add("new risk"))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Property(tries = 100)
  void should_throwException_when_missingInformationMutated(
      @ForAll("validScores") double confidenceScore) {

    var mutableMissing = new ArrayList<>(List.of("existing missing"));
    var result =
        new DocumentAnalysisResult(
            "summary",
            "category",
            List.of(),
            List.of("risk"),
            mutableMissing,
            confidenceScore,
            "provider:v1",
            false);

    assertThatThrownBy(() -> result.missingInformation().add("new info"))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Provide
  Arbitrary<Double> validScores() {
    return Arbitraries.doubles().between(0.0, 1.0);
  }
}
