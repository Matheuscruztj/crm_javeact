package com.atlasops.activities.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ActivityTest {

  private static final String ID = "activity-001";
  private static final String ENTITY_TYPE = "CUSTOMER";
  private static final String ENTITY_ID = "customer-123";
  private static final String ACTION_TYPE = "CREATED";
  private static final String ACTOR_ID = "user-456";
  private static final String TENANT_ID = "tenant-alpha";
  private static final String SUMMARY = "Customer 'Empresa Alpha' was created";
  private static final String EVENT_ID = "evt-789";
  private static final Instant TIMESTAMP = Instant.parse("2025-01-15T10:30:00Z");

  @Test
  @DisplayName("should create activity with all fields when all fields are valid")
  void should_createActivity_when_allFieldsAreValid() {
    Activity activity =
        Activity.create(
            ID,
            ENTITY_TYPE,
            ENTITY_ID,
            ACTION_TYPE,
            ACTOR_ID,
            TENANT_ID,
            SUMMARY,
            EVENT_ID,
            TIMESTAMP);

    assertThat(activity.getId()).isEqualTo(ID);
    assertThat(activity.getEntityType()).isEqualTo(ENTITY_TYPE);
    assertThat(activity.getEntityId()).isEqualTo(ENTITY_ID);
    assertThat(activity.getActionType()).isEqualTo(ACTION_TYPE);
    assertThat(activity.getActorId()).isEqualTo(ACTOR_ID);
    assertThat(activity.getTenantId()).isEqualTo(TENANT_ID);
    assertThat(activity.getSummary()).isEqualTo(SUMMARY);
    assertThat(activity.getEventId()).isEqualTo(EVENT_ID);
    assertThat(activity.getTimestamp()).isEqualTo(TIMESTAMP);
  }

  @Test
  @DisplayName("should accept summary with exactly 500 characters")
  void should_acceptSummary_when_lengthIsExactly500() {
    String maxSummary = "a".repeat(500);

    Activity activity =
        Activity.create(
            ID,
            ENTITY_TYPE,
            ENTITY_ID,
            ACTION_TYPE,
            ACTOR_ID,
            TENANT_ID,
            maxSummary,
            EVENT_ID,
            TIMESTAMP);

    assertThat(activity.getSummary()).hasSize(500);
  }

  @Test
  @DisplayName("should reject summary exceeding 500 characters")
  void should_rejectCreation_when_summaryExceeds500Characters() {
    String tooLong = "a".repeat(501);

    assertThatThrownBy(
            () ->
                Activity.create(
                    ID,
                    ENTITY_TYPE,
                    ENTITY_ID,
                    ACTION_TYPE,
                    ACTOR_ID,
                    TENANT_ID,
                    tooLong,
                    EVENT_ID,
                    TIMESTAMP))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Summary must not exceed 500 characters");
  }

  @Test
  @DisplayName("should reject null id")
  void should_rejectCreation_when_idIsNull() {
    assertThatThrownBy(
            () ->
                Activity.create(
                    null,
                    ENTITY_TYPE,
                    ENTITY_ID,
                    ACTION_TYPE,
                    ACTOR_ID,
                    TENANT_ID,
                    SUMMARY,
                    EVENT_ID,
                    TIMESTAMP))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Entity id must not be null");
  }

  @Test
  @DisplayName("should reject null entityType")
  void should_rejectCreation_when_entityTypeIsNull() {
    assertThatThrownBy(
            () ->
                Activity.create(
                    ID,
                    null,
                    ENTITY_ID,
                    ACTION_TYPE,
                    ACTOR_ID,
                    TENANT_ID,
                    SUMMARY,
                    EVENT_ID,
                    TIMESTAMP))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("EntityType must not be null");
  }

  @Test
  @DisplayName("should reject blank entityType")
  void should_rejectCreation_when_entityTypeIsBlank() {
    assertThatThrownBy(
            () ->
                Activity.create(
                    ID,
                    "   ",
                    ENTITY_ID,
                    ACTION_TYPE,
                    ACTOR_ID,
                    TENANT_ID,
                    SUMMARY,
                    EVENT_ID,
                    TIMESTAMP))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("EntityType must not be blank");
  }

  @Test
  @DisplayName("should reject null entityId")
  void should_rejectCreation_when_entityIdIsNull() {
    assertThatThrownBy(
            () ->
                Activity.create(
                    ID,
                    ENTITY_TYPE,
                    null,
                    ACTION_TYPE,
                    ACTOR_ID,
                    TENANT_ID,
                    SUMMARY,
                    EVENT_ID,
                    TIMESTAMP))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("EntityId must not be null");
  }

  @Test
  @DisplayName("should reject blank entityId")
  void should_rejectCreation_when_entityIdIsBlank() {
    assertThatThrownBy(
            () ->
                Activity.create(
                    ID,
                    ENTITY_TYPE,
                    "",
                    ACTION_TYPE,
                    ACTOR_ID,
                    TENANT_ID,
                    SUMMARY,
                    EVENT_ID,
                    TIMESTAMP))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("EntityId must not be blank");
  }

  @Test
  @DisplayName("should reject null actionType")
  void should_rejectCreation_when_actionTypeIsNull() {
    assertThatThrownBy(
            () ->
                Activity.create(
                    ID,
                    ENTITY_TYPE,
                    ENTITY_ID,
                    null,
                    ACTOR_ID,
                    TENANT_ID,
                    SUMMARY,
                    EVENT_ID,
                    TIMESTAMP))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("ActionType must not be null");
  }

  @Test
  @DisplayName("should reject blank actionType")
  void should_rejectCreation_when_actionTypeIsBlank() {
    assertThatThrownBy(
            () ->
                Activity.create(
                    ID,
                    ENTITY_TYPE,
                    ENTITY_ID,
                    "  ",
                    ACTOR_ID,
                    TENANT_ID,
                    SUMMARY,
                    EVENT_ID,
                    TIMESTAMP))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("ActionType must not be blank");
  }

  @Test
  @DisplayName("should reject null actorId")
  void should_rejectCreation_when_actorIdIsNull() {
    assertThatThrownBy(
            () ->
                Activity.create(
                    ID,
                    ENTITY_TYPE,
                    ENTITY_ID,
                    ACTION_TYPE,
                    null,
                    TENANT_ID,
                    SUMMARY,
                    EVENT_ID,
                    TIMESTAMP))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("ActorId must not be null");
  }

  @Test
  @DisplayName("should reject blank actorId")
  void should_rejectCreation_when_actorIdIsBlank() {
    assertThatThrownBy(
            () ->
                Activity.create(
                    ID,
                    ENTITY_TYPE,
                    ENTITY_ID,
                    ACTION_TYPE,
                    "",
                    TENANT_ID,
                    SUMMARY,
                    EVENT_ID,
                    TIMESTAMP))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("ActorId must not be blank");
  }

  @Test
  @DisplayName("should reject null tenantId")
  void should_rejectCreation_when_tenantIdIsNull() {
    assertThatThrownBy(
            () ->
                Activity.create(
                    ID,
                    ENTITY_TYPE,
                    ENTITY_ID,
                    ACTION_TYPE,
                    ACTOR_ID,
                    null,
                    SUMMARY,
                    EVENT_ID,
                    TIMESTAMP))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("TenantId must not be null");
  }

  @Test
  @DisplayName("should reject blank tenantId")
  void should_rejectCreation_when_tenantIdIsBlank() {
    assertThatThrownBy(
            () ->
                Activity.create(
                    ID,
                    ENTITY_TYPE,
                    ENTITY_ID,
                    ACTION_TYPE,
                    ACTOR_ID,
                    "   ",
                    SUMMARY,
                    EVENT_ID,
                    TIMESTAMP))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("TenantId must not be blank");
  }

  @Test
  @DisplayName("should reject null summary")
  void should_rejectCreation_when_summaryIsNull() {
    assertThatThrownBy(
            () ->
                Activity.create(
                    ID,
                    ENTITY_TYPE,
                    ENTITY_ID,
                    ACTION_TYPE,
                    ACTOR_ID,
                    TENANT_ID,
                    null,
                    EVENT_ID,
                    TIMESTAMP))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Summary must not be null");
  }

  @Test
  @DisplayName("should reject blank summary")
  void should_rejectCreation_when_summaryIsBlank() {
    assertThatThrownBy(
            () ->
                Activity.create(
                    ID,
                    ENTITY_TYPE,
                    ENTITY_ID,
                    ACTION_TYPE,
                    ACTOR_ID,
                    TENANT_ID,
                    "   ",
                    EVENT_ID,
                    TIMESTAMP))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Summary must not be blank");
  }

  @Test
  @DisplayName("should reject null eventId")
  void should_rejectCreation_when_eventIdIsNull() {
    assertThatThrownBy(
            () ->
                Activity.create(
                    ID,
                    ENTITY_TYPE,
                    ENTITY_ID,
                    ACTION_TYPE,
                    ACTOR_ID,
                    TENANT_ID,
                    SUMMARY,
                    null,
                    TIMESTAMP))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("EventId must not be null");
  }

  @Test
  @DisplayName("should reject blank eventId")
  void should_rejectCreation_when_eventIdIsBlank() {
    assertThatThrownBy(
            () ->
                Activity.create(
                    ID,
                    ENTITY_TYPE,
                    ENTITY_ID,
                    ACTION_TYPE,
                    ACTOR_ID,
                    TENANT_ID,
                    SUMMARY,
                    "",
                    TIMESTAMP))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("EventId must not be blank");
  }

  @Test
  @DisplayName("should reject null timestamp")
  void should_rejectCreation_when_timestampIsNull() {
    assertThatThrownBy(
            () ->
                Activity.create(
                    ID,
                    ENTITY_TYPE,
                    ENTITY_ID,
                    ACTION_TYPE,
                    ACTOR_ID,
                    TENANT_ID,
                    SUMMARY,
                    EVENT_ID,
                    null))
        .isInstanceOf(NullPointerException.class)
        .hasMessageContaining("Timestamp must not be null");
  }

  @Test
  @DisplayName("should have identity-based equality via Entity base class")
  void should_beEqual_when_sameId() {
    Activity activity1 =
        Activity.create(
            ID,
            ENTITY_TYPE,
            ENTITY_ID,
            ACTION_TYPE,
            ACTOR_ID,
            TENANT_ID,
            SUMMARY,
            EVENT_ID,
            TIMESTAMP);
    Activity activity2 =
        Activity.create(
            ID,
            "REQUEST",
            "req-999",
            "UPDATED",
            "user-other",
            "tenant-beta",
            "Different summary",
            "evt-other",
            Instant.now());

    assertThat(activity1).isEqualTo(activity2);
    assertThat(activity1.hashCode()).isEqualTo(activity2.hashCode());
  }

  @Test
  @DisplayName("should not be equal when different id")
  void should_notBeEqual_when_differentId() {
    Activity activity1 =
        Activity.create(
            "activity-001",
            ENTITY_TYPE,
            ENTITY_ID,
            ACTION_TYPE,
            ACTOR_ID,
            TENANT_ID,
            SUMMARY,
            EVENT_ID,
            TIMESTAMP);
    Activity activity2 =
        Activity.create(
            "activity-002",
            ENTITY_TYPE,
            ENTITY_ID,
            ACTION_TYPE,
            ACTOR_ID,
            TENANT_ID,
            SUMMARY,
            "evt-other",
            TIMESTAMP);

    assertThat(activity1).isNotEqualTo(activity2);
  }

  @Test
  @DisplayName("should reconstitute activity from persisted data")
  void should_reconstituteActivity_when_calledWithPersistedData() {
    Activity activity =
        Activity.reconstitute(
            ID,
            ENTITY_TYPE,
            ENTITY_ID,
            ACTION_TYPE,
            ACTOR_ID,
            TENANT_ID,
            SUMMARY,
            EVENT_ID,
            TIMESTAMP);

    assertThat(activity.getId()).isEqualTo(ID);
    assertThat(activity.getEntityType()).isEqualTo(ENTITY_TYPE);
    assertThat(activity.getEntityId()).isEqualTo(ENTITY_ID);
    assertThat(activity.getActionType()).isEqualTo(ACTION_TYPE);
    assertThat(activity.getActorId()).isEqualTo(ACTOR_ID);
    assertThat(activity.getTenantId()).isEqualTo(TENANT_ID);
    assertThat(activity.getSummary()).isEqualTo(SUMMARY);
    assertThat(activity.getEventId()).isEqualTo(EVENT_ID);
    assertThat(activity.getTimestamp()).isEqualTo(TIMESTAMP);
  }
}
