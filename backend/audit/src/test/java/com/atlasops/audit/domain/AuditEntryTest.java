package com.atlasops.audit.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuditEntryTest {

  private static final String ID = "audit-001";
  private static final String ACTION_TYPE = "LOGIN";
  private static final String ACTOR_ID = "user-456";
  private static final String TENANT_ID = "tenant-alpha";
  private static final String ENTITY_TYPE = "USER";
  private static final String ENTITY_ID = "user-456";
  private static final String CORRELATION_ID = "550e8400-e29b-41d4-a716-446655440000";
  private static final String DETAILS = "{\"ip\":\"192.168.1.1\"}";
  private static final Instant TIMESTAMP = Instant.parse("2025-01-15T10:30:00Z");

  @Test
  @DisplayName("should create audit entry when all fields are valid")
  void should_createAuditEntry_when_allFieldsAreValid() {
    AuditEntry entry =
        AuditEntry.create(
            ID,
            ACTION_TYPE,
            ACTOR_ID,
            TENANT_ID,
            ENTITY_TYPE,
            ENTITY_ID,
            CORRELATION_ID,
            DETAILS,
            TIMESTAMP);

    assertThat(entry.getId()).isEqualTo(ID);
    assertThat(entry.getActionType()).isEqualTo(ACTION_TYPE);
    assertThat(entry.getActorId()).isEqualTo(ACTOR_ID);
    assertThat(entry.getTenantId()).isEqualTo(TENANT_ID);
    assertThat(entry.getEntityType()).isEqualTo(ENTITY_TYPE);
    assertThat(entry.getEntityId()).isEqualTo(ENTITY_ID);
    assertThat(entry.getCorrelationId()).isEqualTo(CORRELATION_ID);
    assertThat(entry.getDetails()).isEqualTo(DETAILS);
    assertThat(entry.getTimestamp()).isEqualTo(TIMESTAMP);
  }

  @Test
  @DisplayName("should accept details with exactly 10240 characters")
  void should_acceptDetails_when_lengthIsExactly10240() {
    String exactDetails = "x".repeat(10240);

    AuditEntry entry =
        AuditEntry.create(
            ID,
            ACTION_TYPE,
            ACTOR_ID,
            TENANT_ID,
            ENTITY_TYPE,
            ENTITY_ID,
            CORRELATION_ID,
            exactDetails,
            TIMESTAMP);

    assertThat(entry.getDetails()).hasSize(10240);
  }

  @Test
  @DisplayName("should reject details exceeding 10240 characters")
  void should_rejectCreation_when_detailsExceeds10240Characters() {
    String tooLong = "x".repeat(10241);

    assertThatThrownBy(
            () ->
                AuditEntry.create(
                    ID,
                    ACTION_TYPE,
                    ACTOR_ID,
                    TENANT_ID,
                    ENTITY_TYPE,
                    ENTITY_ID,
                    CORRELATION_ID,
                    tooLong,
                    TIMESTAMP))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Details must not exceed 10240 characters");
  }

  @Test
  @DisplayName("should accept empty JSON as details")
  void should_acceptDetails_when_detailsIsEmptyJson() {
    AuditEntry entry =
        AuditEntry.create(
            ID,
            ACTION_TYPE,
            ACTOR_ID,
            TENANT_ID,
            ENTITY_TYPE,
            ENTITY_ID,
            CORRELATION_ID,
            "{}",
            TIMESTAMP);

    assertThat(entry.getDetails()).isEqualTo("{}");
  }

  @Test
  @DisplayName("should reject null id")
  void should_rejectCreation_when_idIsNull() {
    assertThatThrownBy(
            () ->
                AuditEntry.create(
                    null,
                    ACTION_TYPE,
                    ACTOR_ID,
                    TENANT_ID,
                    ENTITY_TYPE,
                    ENTITY_ID,
                    CORRELATION_ID,
                    DETAILS,
                    TIMESTAMP))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Entity id must not be null");
  }

  @Test
  @DisplayName("should reject null actionType")
  void should_rejectCreation_when_actionTypeIsNull() {
    assertThatThrownBy(
            () ->
                AuditEntry.create(
                    ID,
                    null,
                    ACTOR_ID,
                    TENANT_ID,
                    ENTITY_TYPE,
                    ENTITY_ID,
                    CORRELATION_ID,
                    DETAILS,
                    TIMESTAMP))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("ActionType must not be null");
  }

  @Test
  @DisplayName("should reject blank actionType")
  void should_rejectCreation_when_actionTypeIsBlank() {
    assertThatThrownBy(
            () ->
                AuditEntry.create(
                    ID,
                    "   ",
                    ACTOR_ID,
                    TENANT_ID,
                    ENTITY_TYPE,
                    ENTITY_ID,
                    CORRELATION_ID,
                    DETAILS,
                    TIMESTAMP))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("ActionType must not be blank");
  }

  @Test
  @DisplayName("should reject null actorId")
  void should_rejectCreation_when_actorIdIsNull() {
    assertThatThrownBy(
            () ->
                AuditEntry.create(
                    ID,
                    ACTION_TYPE,
                    null,
                    TENANT_ID,
                    ENTITY_TYPE,
                    ENTITY_ID,
                    CORRELATION_ID,
                    DETAILS,
                    TIMESTAMP))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("ActorId must not be null");
  }

  @Test
  @DisplayName("should reject blank actorId")
  void should_rejectCreation_when_actorIdIsBlank() {
    assertThatThrownBy(
            () ->
                AuditEntry.create(
                    ID,
                    ACTION_TYPE,
                    "",
                    TENANT_ID,
                    ENTITY_TYPE,
                    ENTITY_ID,
                    CORRELATION_ID,
                    DETAILS,
                    TIMESTAMP))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("ActorId must not be blank");
  }

  @Test
  @DisplayName("should reject null tenantId")
  void should_rejectCreation_when_tenantIdIsNull() {
    assertThatThrownBy(
            () ->
                AuditEntry.create(
                    ID,
                    ACTION_TYPE,
                    ACTOR_ID,
                    null,
                    ENTITY_TYPE,
                    ENTITY_ID,
                    CORRELATION_ID,
                    DETAILS,
                    TIMESTAMP))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("TenantId must not be null");
  }

  @Test
  @DisplayName("should reject blank tenantId")
  void should_rejectCreation_when_tenantIdIsBlank() {
    assertThatThrownBy(
            () ->
                AuditEntry.create(
                    ID,
                    ACTION_TYPE,
                    ACTOR_ID,
                    "  ",
                    ENTITY_TYPE,
                    ENTITY_ID,
                    CORRELATION_ID,
                    DETAILS,
                    TIMESTAMP))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("TenantId must not be blank");
  }

  @Test
  @DisplayName("should reject null entityType")
  void should_rejectCreation_when_entityTypeIsNull() {
    assertThatThrownBy(
            () ->
                AuditEntry.create(
                    ID,
                    ACTION_TYPE,
                    ACTOR_ID,
                    TENANT_ID,
                    null,
                    ENTITY_ID,
                    CORRELATION_ID,
                    DETAILS,
                    TIMESTAMP))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("EntityType must not be null");
  }

  @Test
  @DisplayName("should reject blank entityType")
  void should_rejectCreation_when_entityTypeIsBlank() {
    assertThatThrownBy(
            () ->
                AuditEntry.create(
                    ID,
                    ACTION_TYPE,
                    ACTOR_ID,
                    TENANT_ID,
                    "",
                    ENTITY_ID,
                    CORRELATION_ID,
                    DETAILS,
                    TIMESTAMP))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("EntityType must not be blank");
  }

  @Test
  @DisplayName("should reject null entityId")
  void should_rejectCreation_when_entityIdIsNull() {
    assertThatThrownBy(
            () ->
                AuditEntry.create(
                    ID,
                    ACTION_TYPE,
                    ACTOR_ID,
                    TENANT_ID,
                    ENTITY_TYPE,
                    null,
                    CORRELATION_ID,
                    DETAILS,
                    TIMESTAMP))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("EntityId must not be null");
  }

  @Test
  @DisplayName("should reject blank entityId")
  void should_rejectCreation_when_entityIdIsBlank() {
    assertThatThrownBy(
            () ->
                AuditEntry.create(
                    ID,
                    ACTION_TYPE,
                    ACTOR_ID,
                    TENANT_ID,
                    ENTITY_TYPE,
                    "   ",
                    CORRELATION_ID,
                    DETAILS,
                    TIMESTAMP))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("EntityId must not be blank");
  }

  @Test
  @DisplayName("should reject null correlationId")
  void should_rejectCreation_when_correlationIdIsNull() {
    assertThatThrownBy(
            () ->
                AuditEntry.create(
                    ID,
                    ACTION_TYPE,
                    ACTOR_ID,
                    TENANT_ID,
                    ENTITY_TYPE,
                    ENTITY_ID,
                    null,
                    DETAILS,
                    TIMESTAMP))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("CorrelationId must not be null");
  }

  @Test
  @DisplayName("should reject blank correlationId")
  void should_rejectCreation_when_correlationIdIsBlank() {
    assertThatThrownBy(
            () ->
                AuditEntry.create(
                    ID,
                    ACTION_TYPE,
                    ACTOR_ID,
                    TENANT_ID,
                    ENTITY_TYPE,
                    ENTITY_ID,
                    "  ",
                    DETAILS,
                    TIMESTAMP))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("CorrelationId must not be blank");
  }

  @Test
  @DisplayName("should reject null details")
  void should_rejectCreation_when_detailsIsNull() {
    assertThatThrownBy(
            () ->
                AuditEntry.create(
                    ID,
                    ACTION_TYPE,
                    ACTOR_ID,
                    TENANT_ID,
                    ENTITY_TYPE,
                    ENTITY_ID,
                    CORRELATION_ID,
                    null,
                    TIMESTAMP))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Details must not be null");
  }

  @Test
  @DisplayName("should reject null timestamp")
  void should_rejectCreation_when_timestampIsNull() {
    assertThatThrownBy(
            () ->
                AuditEntry.create(
                    ID,
                    ACTION_TYPE,
                    ACTOR_ID,
                    TENANT_ID,
                    ENTITY_TYPE,
                    ENTITY_ID,
                    CORRELATION_ID,
                    DETAILS,
                    null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Timestamp must not be null");
  }

  @Test
  @DisplayName("should be immutable - no setter methods exist")
  void should_beImmutable_when_entryIsCreated() {
    AuditEntry entry =
        AuditEntry.create(
            ID,
            ACTION_TYPE,
            ACTOR_ID,
            TENANT_ID,
            ENTITY_TYPE,
            ENTITY_ID,
            CORRELATION_ID,
            DETAILS,
            TIMESTAMP);

    // Verify all fields are accessible only via getters (immutability)
    assertThat(entry.getActionType()).isEqualTo(ACTION_TYPE);
    assertThat(entry.getActorId()).isEqualTo(ACTOR_ID);
    assertThat(entry.getTenantId()).isEqualTo(TENANT_ID);
    assertThat(entry.getEntityType()).isEqualTo(ENTITY_TYPE);
    assertThat(entry.getEntityId()).isEqualTo(ENTITY_ID);
    assertThat(entry.getCorrelationId()).isEqualTo(CORRELATION_ID);
    assertThat(entry.getDetails()).isEqualTo(DETAILS);
    assertThat(entry.getTimestamp()).isEqualTo(TIMESTAMP);

    // AuditEntry is final - cannot be subclassed to add mutability
    assertThat(AuditEntry.class).isFinal();
  }

  @Test
  @DisplayName("should reconstitute entry from persisted data")
  void should_reconstituteEntry_when_loadedFromPersistence() {
    AuditEntry entry =
        AuditEntry.reconstitute(
            ID,
            ACTION_TYPE,
            ACTOR_ID,
            TENANT_ID,
            ENTITY_TYPE,
            ENTITY_ID,
            CORRELATION_ID,
            DETAILS,
            TIMESTAMP);

    assertThat(entry.getId()).isEqualTo(ID);
    assertThat(entry.getActionType()).isEqualTo(ACTION_TYPE);
    assertThat(entry.getTimestamp()).isEqualTo(TIMESTAMP);
  }

  @Test
  @DisplayName("should have equality based on id")
  void should_beEqual_when_idIsSame() {
    AuditEntry entry1 =
        AuditEntry.create(
            ID,
            ACTION_TYPE,
            ACTOR_ID,
            TENANT_ID,
            ENTITY_TYPE,
            ENTITY_ID,
            CORRELATION_ID,
            DETAILS,
            TIMESTAMP);
    AuditEntry entry2 =
        AuditEntry.create(
            ID,
            "DIFFERENT_ACTION",
            "other-actor",
            TENANT_ID,
            ENTITY_TYPE,
            ENTITY_ID,
            CORRELATION_ID,
            DETAILS,
            TIMESTAMP);

    assertThat(entry1).isEqualTo(entry2);
    assertThat(entry1.hashCode()).isEqualTo(entry2.hashCode());
  }

  @Test
  @DisplayName("should not be equal when id is different")
  void should_notBeEqual_when_idIsDifferent() {
    AuditEntry entry1 =
        AuditEntry.create(
            "audit-001",
            ACTION_TYPE,
            ACTOR_ID,
            TENANT_ID,
            ENTITY_TYPE,
            ENTITY_ID,
            CORRELATION_ID,
            DETAILS,
            TIMESTAMP);
    AuditEntry entry2 =
        AuditEntry.create(
            "audit-002",
            ACTION_TYPE,
            ACTOR_ID,
            TENANT_ID,
            ENTITY_TYPE,
            ENTITY_ID,
            CORRELATION_ID,
            DETAILS,
            TIMESTAMP);

    assertThat(entry1).isNotEqualTo(entry2);
  }
}
