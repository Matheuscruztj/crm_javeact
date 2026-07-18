package com.atlasops.boot.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link EnvironmentValidator}.
 *
 * <p>Validates fail-fast behavior when required environment variables are missing or blank at
 * application startup.
 */
class EnvironmentValidatorTest {

  /** Creates a map with all required environment variables set to valid values. */
  private Map<String, String> allVariablesPresent() {
    Map<String, String> vars = new HashMap<>();
    vars.put("APP_ENV", "local");
    vars.put("APP_PORT", "8080");
    vars.put("DATABASE_URL", "jdbc:postgresql://localhost:5432/atlasops");
    vars.put("REDIS_URL", "redis://localhost:6379");
    vars.put("OBJECT_STORAGE_ENDPOINT", "http://localhost:9000");
    vars.put("OBJECT_STORAGE_BUCKET", "atlasops-local");
    vars.put("JWT_ISSUER", "atlasops-local");
    vars.put("JWT_AUDIENCE", "atlasops-api");
    vars.put("LOG_LEVEL", "INFO");
    return vars;
  }

  @Test
  @DisplayName("should_passValidation_when_allVariablesPresent")
  void should_passValidation_when_allVariablesPresent() {
    Map<String, String> vars = allVariablesPresent();

    List<String> missing = EnvironmentValidator.findMissingVariables(vars);

    assertThat(missing).isEmpty();
    assertThatNoException().isThrownBy(() -> EnvironmentValidator.validate(vars));
  }

  @Test
  @DisplayName("should_reportMissing_when_oneVariableAbsent")
  void should_reportMissing_when_oneVariableAbsent() {
    Map<String, String> vars = allVariablesPresent();
    vars.remove("DATABASE_URL");

    List<String> missing = EnvironmentValidator.findMissingVariables(vars);

    assertThat(missing).containsExactly("DATABASE_URL");
  }

  @Test
  @DisplayName("should_throwException_when_oneVariableAbsent")
  void should_throwException_when_oneVariableAbsent() {
    Map<String, String> vars = allVariablesPresent();
    vars.remove("DATABASE_URL");

    assertThatThrownBy(() -> EnvironmentValidator.validate(vars))
        .isInstanceOf(EnvironmentValidationException.class)
        .hasMessageContaining("DATABASE_URL");
  }

  @Test
  @DisplayName("should_reportAllMissing_when_multipleVariablesAbsent")
  void should_reportAllMissing_when_multipleVariablesAbsent() {
    Map<String, String> vars = allVariablesPresent();
    vars.remove("APP_ENV");
    vars.remove("REDIS_URL");
    vars.remove("JWT_ISSUER");

    List<String> missing = EnvironmentValidator.findMissingVariables(vars);

    assertThat(missing).containsExactlyInAnyOrder("APP_ENV", "REDIS_URL", "JWT_ISSUER");
  }

  @Test
  @DisplayName("should_throwWithAllMissing_when_multipleVariablesAbsent")
  void should_throwWithAllMissing_when_multipleVariablesAbsent() {
    Map<String, String> vars = allVariablesPresent();
    vars.remove("APP_ENV");
    vars.remove("REDIS_URL");
    vars.remove("JWT_ISSUER");

    assertThatThrownBy(() -> EnvironmentValidator.validate(vars))
        .isInstanceOf(EnvironmentValidationException.class)
        .hasMessageContaining("APP_ENV")
        .hasMessageContaining("REDIS_URL")
        .hasMessageContaining("JWT_ISSUER");
  }

  @Test
  @DisplayName("should_treatEmptyAsAbsent_when_variableIsEmptyString")
  void should_treatEmptyAsAbsent_when_variableIsEmptyString() {
    Map<String, String> vars = allVariablesPresent();
    vars.put("LOG_LEVEL", "");

    List<String> missing = EnvironmentValidator.findMissingVariables(vars);

    assertThat(missing).containsExactly("LOG_LEVEL");
  }

  @Test
  @DisplayName("should_treatBlankAsAbsent_when_variableIsWhitespaceOnly")
  void should_treatBlankAsAbsent_when_variableIsWhitespaceOnly() {
    Map<String, String> vars = allVariablesPresent();
    vars.put("JWT_AUDIENCE", "   ");

    List<String> missing = EnvironmentValidator.findMissingVariables(vars);

    assertThat(missing).containsExactly("JWT_AUDIENCE");
  }

  @Test
  @DisplayName("should_reportAllMissing_when_noVariablesProvided")
  void should_reportAllMissing_when_noVariablesProvided() {
    Map<String, String> vars = new HashMap<>();

    List<String> missing = EnvironmentValidator.findMissingVariables(vars);

    assertThat(missing).hasSize(9);
    assertThat(missing)
        .containsExactlyInAnyOrder(
            "APP_ENV",
            "APP_PORT",
            "DATABASE_URL",
            "REDIS_URL",
            "OBJECT_STORAGE_ENDPOINT",
            "OBJECT_STORAGE_BUCKET",
            "JWT_ISSUER",
            "JWT_AUDIENCE",
            "LOG_LEVEL");
  }

  @Test
  @DisplayName("should_formatMessage_when_exceptionThrown")
  void should_formatMessage_when_exceptionThrown() {
    Map<String, String> vars = allVariablesPresent();
    vars.remove("APP_PORT");

    assertThatThrownBy(() -> EnvironmentValidator.validate(vars))
        .isInstanceOf(EnvironmentValidationException.class)
        .hasMessageStartingWith("Missing required environment variables:");
  }
}
