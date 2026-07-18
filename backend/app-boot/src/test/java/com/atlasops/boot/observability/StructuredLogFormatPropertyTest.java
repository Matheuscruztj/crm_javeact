package com.atlasops.boot.observability;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import net.jqwik.api.*;
import net.logstash.logback.encoder.LogstashEncoder;
import org.slf4j.MDC;

/**
 * Property-based tests for structured log format invariant.
 *
 * <p><b>Validates: Requirements 3.10, 11.1</b>
 *
 * <p>Property 2: For any log event emitted by the Backend API (regardless of level, module, or
 * trigger), the JSON output SHALL contain all mandatory fields: timestamp (ISO 8601 UTC), level,
 * service, environment, tenantId, actorId, correlationId, event, resource, duration, and errorCode.
 */
@Tag("Feature: monorepo-sdd-harness, Property 2: Structured Log Format Invariant")
class StructuredLogFormatPropertyTest {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final List<String> MANDATORY_FIELDS =
      List.of(
          "timestamp",
          "level",
          "service",
          "environment",
          "tenantId",
          "actorId",
          "correlationId",
          "event",
          "resource",
          "duration",
          "errorCode");

  private static final List<Level> ALL_LOG_LEVELS =
      List.of(Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR);

  // ─── Property: Any log event contains all mandatory fields ───────────────────

  @Property(tries = 100)
  void anyLogEvent_shouldContainAllMandatoryFields(
      @ForAll("logLevels") Level level,
      @ForAll("logMessages") String message,
      @ForAll("mdcValues") Map<String, String> mdcValues)
      throws Exception {

    String jsonOutput = encodeLogEvent(level, message, mdcValues);

    JsonNode root = OBJECT_MAPPER.readTree(jsonOutput);

    for (String field : MANDATORY_FIELDS) {
      assertThat(root.has(field))
          .as(
              "JSON log output must contain mandatory field '%s'. Actual JSON: %s",
              field, jsonOutput)
          .isTrue();
    }
  }

  // ─── Property: Timestamp is always ISO 8601 UTC format ───────────────────────

  @Property(tries = 100)
  void timestamp_shouldAlwaysBeIso8601Utc(
      @ForAll("logLevels") Level level,
      @ForAll("logMessages") String message,
      @ForAll("mdcValues") Map<String, String> mdcValues)
      throws Exception {

    String jsonOutput = encodeLogEvent(level, message, mdcValues);

    JsonNode root = OBJECT_MAPPER.readTree(jsonOutput);
    assertThat(root.has("timestamp")).isTrue();

    String timestamp = root.get("timestamp").asText();

    assertThat(timestamp).as("Timestamp should not be null or empty").isNotNull().isNotBlank();

    // Verify ISO 8601 UTC format: yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
    assertThat(timestamp)
        .as("Timestamp '%s' should end with 'Z' (UTC indicator)", timestamp)
        .endsWith("Z");

    // Parse to verify it's valid ISO 8601
    assertThat(isValidIso8601Timestamp(timestamp))
        .as("Timestamp '%s' should be parseable as ISO 8601 UTC", timestamp)
        .isTrue();
  }

  // ─── Property: All mandatory fields are non-null (present in JSON) ───────────

  @Property(tries = 100)
  void allMandatoryFields_shouldBeNonNull(
      @ForAll("logLevels") Level level,
      @ForAll("logMessages") String message,
      @ForAll("mdcValues") Map<String, String> mdcValues)
      throws Exception {

    String jsonOutput = encodeLogEvent(level, message, mdcValues);

    JsonNode root = OBJECT_MAPPER.readTree(jsonOutput);

    for (String field : MANDATORY_FIELDS) {
      JsonNode node = root.get(field);
      assertThat(node)
          .as("Field '%s' must be present (non-null) in JSON log output", field)
          .isNotNull();
      assertThat(node.isNull())
          .as("Field '%s' must not be JSON null. Actual value: %s", field, node)
          .isFalse();
    }
  }

  // ─── Property: Level field always matches emitted log level ──────────────────

  @Property(tries = 100)
  void levelField_shouldMatchEmittedLogLevel(
      @ForAll("logLevels") Level level,
      @ForAll("logMessages") String message,
      @ForAll("mdcValues") Map<String, String> mdcValues)
      throws Exception {

    String jsonOutput = encodeLogEvent(level, message, mdcValues);

    JsonNode root = OBJECT_MAPPER.readTree(jsonOutput);
    String loggedLevel = root.get("level").asText();

    assertThat(loggedLevel)
        .as("Level field should match the emitted log level")
        .isEqualToIgnoringCase(level.toString());
  }

  // ─── Helper: Encode a log event using LogstashEncoder matching production config ─

  private String encodeLogEvent(Level level, String message, Map<String, String> mdcValues)
      throws Exception {
    LoggerContext loggerContext = new LoggerContext();
    loggerContext.start();

    // Configure LogstashEncoder matching the production logback-spring.xml
    LogstashEncoder encoder = new LogstashEncoder();
    encoder.setContext(loggerContext);
    encoder.setTimeZone("UTC");
    encoder.setTimestampPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    // Configure field names to match production config
    encoder.getFieldNames().setTimestamp("timestamp");
    encoder.getFieldNames().setLevel("level");
    encoder.getFieldNames().setLogger("event");
    encoder.getFieldNames().setMessage("message");
    encoder.getFieldNames().setThread("[ignore]");
    encoder.getFieldNames().setStackTrace("stackTrace");
    encoder.getFieldNames().setVersion("[ignore]");
    encoder.getFieldNames().setLevelValue("[ignore]");

    // Static custom fields (service and environment)
    encoder.setCustomFields("{\"service\":\"atlasops-api\",\"environment\":\"ci\"}");

    // Include MDC keys matching production config
    encoder.addIncludeMdcKeyName("tenantId");
    encoder.addIncludeMdcKeyName("actorId");
    encoder.addIncludeMdcKeyName("correlationId");
    encoder.addIncludeMdcKeyName("traceId");
    encoder.addIncludeMdcKeyName("event");
    encoder.addIncludeMdcKeyName("resource");
    encoder.addIncludeMdcKeyName("duration");
    encoder.addIncludeMdcKeyName("errorCode");

    encoder.start();

    // Set MDC values (simulating the filter and application context)
    for (Map.Entry<String, String> entry : mdcValues.entrySet()) {
      MDC.put(entry.getKey(), entry.getValue());
    }

    try {
      // Create a LoggingEvent with MDC context properly bound
      LoggingEvent event = new LoggingEvent();
      event.setLoggerContext(loggerContext);
      event.setLoggerName("com.atlasops.test");
      event.setLevel(level);
      event.setMessage(message);
      event.setTimeStamp(System.currentTimeMillis());
      event.setThreadName(Thread.currentThread().getName());
      // Bind the current MDC map to the event
      event.setMDCPropertyMap(MDC.getCopyOfContextMap());

      // Encode the event directly
      byte[] encodedBytes = encoder.encode(event);

      assertThat(encodedBytes).as("Encoder must produce non-null output for log event").isNotNull();

      String result = new String(encodedBytes, StandardCharsets.UTF_8).trim();
      assertThat(result).as("Encoder should produce non-empty JSON output").isNotEmpty();
      return result;
    } finally {
      MDC.clear();
      encoder.stop();
      loggerContext.stop();
    }
  }

  private boolean isValidIso8601Timestamp(String timestamp) {
    try {
      // Try parsing as ISO instant (e.g. 2024-01-15T10:30:00.123Z)
      Instant.parse(timestamp);
      return true;
    } catch (DateTimeParseException e) {
      // Try parsing with DateTimeFormatter ISO pattern
      try {
        DateTimeFormatter.ISO_DATE_TIME.parse(timestamp);
        return true;
      } catch (DateTimeParseException e2) {
        return false;
      }
    }
  }

  // ─── Generators ──────────────────────────────────────────────────────────────

  @Provide
  Arbitrary<Level> logLevels() {
    return Arbitraries.of(ALL_LOG_LEVELS);
  }

  @Provide
  Arbitrary<String> logMessages() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .withCharRange('A', 'Z')
        .withCharRange('0', '9')
        .withChars(' ', '.', '-', '_', ':', '/')
        .ofMinLength(1)
        .ofMaxLength(200);
  }

  @Provide
  Arbitrary<Map<String, String>> mdcValues() {
    // Generate MDC values for all mandatory MDC-based fields
    Arbitrary<String> nonBlankValue =
        Arbitraries.strings()
            .withCharRange('a', 'z')
            .withCharRange('0', '9')
            .withChars('-', '_')
            .ofMinLength(1)
            .ofMaxLength(50);

    return Combinators.combine(
            nonBlankValue, // tenantId
            nonBlankValue, // actorId
            nonBlankValue, // correlationId
            nonBlankValue, // resource
            Arbitraries.longs().between(0, 10000).map(String::valueOf), // duration
            Arbitraries.of("ERR_001", "ERR_TIMEOUT", "VALIDATION_FAILED", "NONE") // errorCode
            )
        .as(
            (tenantId, actorId, correlationId, resource, duration, errorCode) ->
                Map.of(
                    "tenantId", tenantId,
                    "actorId", actorId,
                    "correlationId", correlationId,
                    "resource", resource,
                    "duration", duration,
                    "errorCode", errorCode));
  }
}
