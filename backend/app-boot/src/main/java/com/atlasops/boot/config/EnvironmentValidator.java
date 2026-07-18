package com.atlasops.boot.config;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Validates that all required environment variables are defined and non-empty at startup.
 *
 * <p>Implements fail-fast behavior: if any required variable is absent or blank, the application
 * halts immediately with a clear error listing ALL missing variables.
 *
 * <p>Registered via {@code META-INF/spring.factories} as an {@link EnvironmentPostProcessor} to run
 * before any beans are created.
 */
public class EnvironmentValidator implements EnvironmentPostProcessor {

  private static final Logger log = LoggerFactory.getLogger(EnvironmentValidator.class);

  /** All environment variables required for AtlasOps to start. */
  static final List<String> REQUIRED_VARIABLES =
      List.of(
          "APP_ENV",
          "APP_PORT",
          "DATABASE_URL",
          "REDIS_URL",
          "OBJECT_STORAGE_ENDPOINT",
          "OBJECT_STORAGE_BUCKET",
          "JWT_ISSUER",
          "JWT_AUDIENCE",
          "LOG_LEVEL");

  @Override
  public void postProcessEnvironment(
      ConfigurableEnvironment environment, org.springframework.boot.SpringApplication application) {
    List<String> missing = findMissingVariables(environment);
    if (!missing.isEmpty()) {
      String message = "Missing required environment variables: " + missing;
      log.error(message);
      throw new EnvironmentValidationException(message);
    }
  }

  /**
   * Finds all required environment variables that are absent or blank.
   *
   * @param environment the Spring environment to check
   * @return list of missing/empty variable names
   */
  List<String> findMissingVariables(ConfigurableEnvironment environment) {
    return REQUIRED_VARIABLES.stream()
        .filter(
            varName -> {
              String value = environment.getProperty(varName);
              return value == null || value.isBlank();
            })
        .collect(Collectors.toList());
  }

  /**
   * Checks a raw map of variable names to values for missing/blank entries. Useful for unit testing
   * without a full Spring Environment.
   *
   * @param variables map of variable name → value (null if absent)
   * @return list of missing/empty variable names
   */
  public static List<String> findMissingVariables(java.util.Map<String, String> variables) {
    return REQUIRED_VARIABLES.stream()
        .filter(
            varName -> {
              String value = variables.get(varName);
              return value == null || value.isBlank();
            })
        .collect(Collectors.toList());
  }

  /**
   * Validates the given variable map and throws if any are missing.
   *
   * @param variables map of variable name → value
   * @throws EnvironmentValidationException if any required variables are missing/blank
   */
  public static void validate(java.util.Map<String, String> variables) {
    List<String> missing = findMissingVariables(variables);
    if (!missing.isEmpty()) {
      throw new EnvironmentValidationException(
          "Missing required environment variables: " + missing);
    }
  }
}
