package com.atlasops.audit.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.Arrays;
import net.jqwik.api.*;

/**
 * Property 23: Audit Entry Immutability Validates: Requirements 19.2, 19.3
 *
 * <p>Verifies that AuditEntry is truly immutable: - The class is final (cannot be subclassed) - No
 * setter methods exist (only getters) - Fields are preserved unchanged after creation - Details
 * within max length are stored unchanged - Details exceeding max length throw
 * IllegalArgumentException - Reconstituted entries preserve all fields exactly
 */
@Tag("Feature: project-implementation-kickoff, Property 23: Audit Entry Immutability")
class AuditEntryImmutabilityPropertyTest {

  private static final int DETAILS_MAX_LENGTH = 10240;

  @Property(tries = 100)
  void should_alwaysBeInaccessibleViaSetters_forAnyAuditEntry(
      @ForAll("validActionTypes") String actionType,
      @ForAll("validActorIds") String actorId,
      @ForAll("validTenantIds") String tenantId,
      @ForAll("validEntityTypes") String entityType,
      @ForAll("validEntityIds") String entityId,
      @ForAll("validCorrelationIds") String correlationId,
      @ForAll("validDetails") String details) {

    // AuditEntry must have no setter methods — only getters
    Method[] methods = AuditEntry.class.getDeclaredMethods();
    long setterCount =
        Arrays.stream(methods)
            .filter(m -> m.getName().startsWith("set"))
            .filter(m -> Modifier.isPublic(m.getModifiers()))
            .count();

    assertThat(setterCount).as("AuditEntry must have no public setter methods").isZero();

    // Verify all fields are accessible via getters
    AuditEntry entry =
        AuditEntry.create(
            "audit-" + actionType.hashCode(),
            actionType,
            actorId,
            tenantId,
            entityType,
            entityId,
            correlationId,
            details,
            Instant.now());

    assertThat(entry.getActionType()).isEqualTo(actionType);
    assertThat(entry.getActorId()).isEqualTo(actorId);
    assertThat(entry.getTenantId()).isEqualTo(tenantId);
    assertThat(entry.getEntityType()).isEqualTo(entityType);
    assertThat(entry.getEntityId()).isEqualTo(entityId);
    assertThat(entry.getCorrelationId()).isEqualTo(correlationId);
    assertThat(entry.getDetails()).isEqualTo(details);
  }

  @Property(tries = 100)
  void should_alwaysBeFinalClass_forAnyValidEntry(
      @ForAll("validActionTypes") String actionType, @ForAll("validActorIds") String actorId) {

    // AuditEntry class must be final — cannot be subclassed to add mutability
    assertThat(Modifier.isFinal(AuditEntry.class.getModifiers()))
        .as("AuditEntry must be a final class to prevent subclassing")
        .isTrue();
  }

  @Property(tries = 100)
  void should_alwaysPreserveDetailsUnchanged_forAnyValidDetailsString(
      @ForAll("detailsWithinLimit") String details) {

    AuditEntry entry =
        AuditEntry.create(
            "audit-id-001",
            "LOGIN",
            "actor-001",
            "tenant-001",
            "USER",
            "entity-001",
            "corr-001",
            details,
            Instant.parse("2025-01-15T10:00:00Z"));

    assertThat(entry.getDetails())
        .as("Details must be stored exactly as provided")
        .isEqualTo(details);
    assertThat(entry.getDetails().length())
        .as("Details length must be preserved")
        .isEqualTo(details.length());
  }

  @Property(tries = 100)
  void should_alwaysThrowIllegalArgument_forAnyDetailsExceedingMaxLength(
      @ForAll("detailsOverLimit") String details) {

    assertThatThrownBy(
            () ->
                AuditEntry.create(
                    "audit-id-001",
                    "LOGIN",
                    "actor-001",
                    "tenant-001",
                    "USER",
                    "entity-001",
                    "corr-001",
                    details,
                    Instant.parse("2025-01-15T10:00:00Z")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Details must not exceed 10240 characters");
  }

  @Property(tries = 100)
  void should_alwaysPreserveAllFields_forAnyReconstitutedEntry(
      @ForAll("validActionTypes") String actionType,
      @ForAll("validActorIds") String actorId,
      @ForAll("validTenantIds") String tenantId,
      @ForAll("validEntityTypes") String entityType,
      @ForAll("validEntityIds") String entityId,
      @ForAll("validCorrelationIds") String correlationId,
      @ForAll("validDetails") String details,
      @ForAll("validTimestamps") Instant timestamp) {

    String id = "audit-reconst-" + actorId.hashCode();

    AuditEntry reconstituted =
        AuditEntry.reconstitute(
            id,
            actionType,
            actorId,
            tenantId,
            entityType,
            entityId,
            correlationId,
            details,
            timestamp);

    assertThat(reconstituted.getId()).isEqualTo(id);
    assertThat(reconstituted.getActionType()).isEqualTo(actionType);
    assertThat(reconstituted.getActorId()).isEqualTo(actorId);
    assertThat(reconstituted.getTenantId()).isEqualTo(tenantId);
    assertThat(reconstituted.getEntityType()).isEqualTo(entityType);
    assertThat(reconstituted.getEntityId()).isEqualTo(entityId);
    assertThat(reconstituted.getCorrelationId()).isEqualTo(correlationId);
    assertThat(reconstituted.getDetails()).isEqualTo(details);
    assertThat(reconstituted.getTimestamp()).isEqualTo(timestamp);
  }

  // --- Custom Providers ---

  @Provide
  Arbitrary<String> validActionTypes() {
    return Arbitraries.of(
        "LOGIN",
        "CREATE_CUSTOMER",
        "UPLOAD_DOCUMENT",
        "APPROVAL_DECISION",
        "ROLE_CHANGE",
        "TENANT_DEACTIVATION");
  }

  @Provide
  Arbitrary<String> validActorIds() {
    return Arbitraries.strings()
        .alpha()
        .numeric()
        .withChars('-')
        .ofMinLength(3)
        .ofMaxLength(50)
        .filter(s -> !s.isBlank());
  }

  @Provide
  Arbitrary<String> validTenantIds() {
    return Arbitraries.strings()
        .alpha()
        .numeric()
        .withChars('-')
        .ofMinLength(3)
        .ofMaxLength(50)
        .filter(s -> !s.isBlank());
  }

  @Provide
  Arbitrary<String> validEntityTypes() {
    return Arbitraries.of("USER", "CUSTOMER", "DOCUMENT", "REQUEST", "APPROVAL", "TENANT");
  }

  @Provide
  Arbitrary<String> validEntityIds() {
    return Arbitraries.strings()
        .alpha()
        .numeric()
        .withChars('-')
        .ofMinLength(3)
        .ofMaxLength(50)
        .filter(s -> !s.isBlank());
  }

  @Provide
  Arbitrary<String> validCorrelationIds() {
    return Arbitraries.strings()
        .alpha()
        .numeric()
        .withChars('-')
        .ofMinLength(5)
        .ofMaxLength(50)
        .filter(s -> !s.isBlank());
  }

  @Provide
  Arbitrary<String> validDetails() {
    return Arbitraries.strings()
        .ascii()
        .ofMinLength(0)
        .ofMaxLength(DETAILS_MAX_LENGTH)
        .filter(s -> s.length() <= DETAILS_MAX_LENGTH);
  }

  @Provide
  Arbitrary<String> detailsWithinLimit() {
    return Arbitraries.strings().ascii().ofMinLength(0).ofMaxLength(DETAILS_MAX_LENGTH);
  }

  @Provide
  Arbitrary<String> detailsOverLimit() {
    return Arbitraries.strings()
        .ascii()
        .ofMinLength(DETAILS_MAX_LENGTH + 1)
        .ofMaxLength(DETAILS_MAX_LENGTH + 5000);
  }

  @Provide
  Arbitrary<Instant> validTimestamps() {
    return Arbitraries.longs()
        .between(0L, 4_102_444_800L) // 2000-01-01 to 2100-01-01
        .map(Instant::ofEpochSecond);
  }
}
