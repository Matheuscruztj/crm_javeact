package com.atlasops.shared.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlasops.shared.validation.FeatureNameValidator.ValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FeatureNameValidatorTest {

  @ParameterizedTest
  @ValueSource(
      strings = {
        "user-auth",
        "monorepo-sdd-harness",
        "a",
        "feature1",
        "my-feature-2",
        "abc-def-ghi-jkl",
        "a1b2c3"
      })
  void should_beValid_when_validKebabCaseNames(String name) {
    ValidationResult result = FeatureNameValidator.validate(name);
    assertThat(result.isValid()).isTrue();
    assertThat(result.reason()).isNull();
  }

  @Test
  void should_beValid_when_exactlyMaxLength() {
    // 50 characters
    String name = "abcde-abcde-abcde-abcde-abcde-abcde-abcde-abcde123";
    assertThat(name.length()).isEqualTo(50);
    ValidationResult result = FeatureNameValidator.validate(name);
    assertThat(result.isValid()).isTrue();
  }

  @Test
  void should_beInvalid_when_exceedsMaxLength() {
    String name = "a".repeat(51);
    ValidationResult result = FeatureNameValidator.validate(name);
    assertThat(result.isValid()).isFalse();
    assertThat(result.reason()).contains("at most 50 characters");
  }

  @Test
  void should_beInvalid_when_null() {
    ValidationResult result = FeatureNameValidator.validate(null);
    assertThat(result.isValid()).isFalse();
    assertThat(result.reason()).contains("must not be null or empty");
  }

  @Test
  void should_beInvalid_when_empty() {
    ValidationResult result = FeatureNameValidator.validate("");
    assertThat(result.isValid()).isFalse();
    assertThat(result.reason()).contains("must not be null or empty");
  }

  @Test
  void should_beInvalid_when_blank() {
    ValidationResult result = FeatureNameValidator.validate("   ");
    assertThat(result.isValid()).isFalse();
    assertThat(result.reason()).contains("must not be null or empty");
  }

  @Test
  void should_beInvalid_when_startsWithHyphen() {
    ValidationResult result = FeatureNameValidator.validate("-my-feature");
    assertThat(result.isValid()).isFalse();
    assertThat(result.reason()).contains("must not start with a hyphen");
  }

  @Test
  void should_beInvalid_when_endsWithHyphen() {
    ValidationResult result = FeatureNameValidator.validate("my-feature-");
    assertThat(result.isValid()).isFalse();
    assertThat(result.reason()).contains("must not end with a hyphen");
  }

  @Test
  void should_beInvalid_when_consecutiveHyphens() {
    ValidationResult result = FeatureNameValidator.validate("my--feature");
    assertThat(result.isValid()).isFalse();
    assertThat(result.reason()).contains("must not contain consecutive hyphens");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "MyFeature",
        "my_feature",
        "my feature",
        "MY-FEATURE",
        "Feature",
        "camelCase",
        "with.dots",
        "with@special"
      })
  void should_beInvalid_when_invalidCharacters(String name) {
    ValidationResult result = FeatureNameValidator.validate(name);
    assertThat(result.isValid()).isFalse();
    assertThat(result.reason()).contains("kebab-case");
  }
}
