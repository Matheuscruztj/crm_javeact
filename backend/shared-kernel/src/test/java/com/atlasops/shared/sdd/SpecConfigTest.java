package com.atlasops.shared.sdd;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class SpecConfigTest {

  @Test
  void should_beValid_when_allFieldsCorrect_featureType() {
    var config = new SpecConfig("550e8400-e29b-41d4-a716-446655440000", "sdd", "feature");
    var result = config.validate();
    assertThat(result.isValid()).isTrue();
    assertThat(result.errors()).isEmpty();
  }

  @Test
  void should_beValid_when_allFieldsCorrect_fixType() {
    var config = new SpecConfig("some-uuid-string", "sdd", "fix");
    var result = config.validate();
    assertThat(result.isValid()).isTrue();
    assertThat(result.errors()).isEmpty();
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void should_beInvalid_when_specIdMissing(String specId) {
    var config = new SpecConfig(specId, "sdd", "feature");
    var result = config.validate();
    assertThat(result.isValid()).isFalse();
    assertThat(result.errors()).anyMatch(e -> e.contains("specId"));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void should_beInvalid_when_workflowTypeMissing(String workflowType) {
    var config = new SpecConfig("uuid-123", workflowType, "feature");
    var result = config.validate();
    assertThat(result.isValid()).isFalse();
    assertThat(result.errors()).anyMatch(e -> e.contains("workflowType"));
  }

  @Test
  void should_beInvalid_when_workflowTypeNotSdd() {
    var config = new SpecConfig("uuid-123", "agile", "feature");
    var result = config.validate();
    assertThat(result.isValid()).isFalse();
    assertThat(result.errors()).anyMatch(e -> e.contains("workflowType must be 'sdd'"));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void should_beInvalid_when_specTypeMissing(String specType) {
    var config = new SpecConfig("uuid-123", "sdd", specType);
    var result = config.validate();
    assertThat(result.isValid()).isFalse();
    assertThat(result.errors()).anyMatch(e -> e.contains("specType"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"bug", "enhancement", "task", "FEATURE", "FIX"})
  void should_beInvalid_when_specTypeInvalid(String specType) {
    var config = new SpecConfig("uuid-123", "sdd", specType);
    var result = config.validate();
    assertThat(result.isValid()).isFalse();
    assertThat(result.errors()).anyMatch(e -> e.contains("specType must be 'feature' or 'fix'"));
  }

  @Test
  void should_reportAllErrors_when_multipleFieldsInvalid() {
    var config = new SpecConfig(null, "invalid", "invalid");
    var result = config.validate();
    assertThat(result.isValid()).isFalse();
    assertThat(result.errors()).hasSize(3);
  }

  @Test
  void should_reportTwoErrors_when_twoFieldsInvalid() {
    var config = new SpecConfig("uuid-123", null, "invalid");
    var result = config.validate();
    assertThat(result.isValid()).isFalse();
    assertThat(result.errors()).hasSize(2);
  }
}
