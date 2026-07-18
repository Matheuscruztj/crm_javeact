package com.atlasops.ai.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for PendingApproval domain entity. Validates: Requirements 4.10 */
class PendingApprovalTest {

  private static final String ID = "approval-001";
  private static final String ANALYSIS_ID = "analysis-123";
  private static final String TARGET_RESOURCE = "customers/cust-456";
  private static final String PAYLOAD =
      "{\"name\":\"New Customer\",\"email\":\"test@example.com\"}";
  private static final String REQUESTED_BY = "analysis-123";
  private static final Instant CREATED_AT = Instant.parse("2024-01-15T10:00:00Z");

  @Nested
  @DisplayName("Construction and validation")
  class ConstructionTests {

    @Test
    @DisplayName("should create PendingApproval with all valid fields")
    void should_createPendingApproval_when_allFieldsValid() {
      var approval =
          new PendingApproval(
              ID,
              ANALYSIS_ID,
              ActionType.CREATE,
              TARGET_RESOURCE,
              PAYLOAD,
              ApprovalStatus.PENDING_APPROVAL,
              REQUESTED_BY,
              null,
              null,
              CREATED_AT);

      assertThat(approval.getId()).isEqualTo(ID);
      assertThat(approval.getAnalysisId()).isEqualTo(ANALYSIS_ID);
      assertThat(approval.getActionType()).isEqualTo(ActionType.CREATE);
      assertThat(approval.getTargetResource()).isEqualTo(TARGET_RESOURCE);
      assertThat(approval.getPayload()).isEqualTo(PAYLOAD);
      assertThat(approval.getStatus()).isEqualTo(ApprovalStatus.PENDING_APPROVAL);
      assertThat(approval.getRequestedBy()).isEqualTo(REQUESTED_BY);
      assertThat(approval.getDecidedBy()).isNull();
      assertThat(approval.getDecidedAt()).isNull();
      assertThat(approval.getCreatedAt()).isEqualTo(CREATED_AT);
    }

    @Test
    @DisplayName("should reject null analysisId")
    void should_rejectCreation_when_analysisIdIsNull() {
      assertThatThrownBy(
              () ->
                  new PendingApproval(
                      ID,
                      null,
                      ActionType.CREATE,
                      TARGET_RESOURCE,
                      PAYLOAD,
                      ApprovalStatus.PENDING_APPROVAL,
                      REQUESTED_BY,
                      null,
                      null,
                      CREATED_AT))
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining("analysisId must not be null");
    }

    @Test
    @DisplayName("should reject blank analysisId")
    void should_rejectCreation_when_analysisIdIsBlank() {
      assertThatThrownBy(
              () ->
                  new PendingApproval(
                      ID,
                      "  ",
                      ActionType.CREATE,
                      TARGET_RESOURCE,
                      PAYLOAD,
                      ApprovalStatus.PENDING_APPROVAL,
                      REQUESTED_BY,
                      null,
                      null,
                      CREATED_AT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("analysisId must not be blank");
    }

    @Test
    @DisplayName("should reject null actionType")
    void should_rejectCreation_when_actionTypeIsNull() {
      assertThatThrownBy(
              () ->
                  new PendingApproval(
                      ID,
                      ANALYSIS_ID,
                      null,
                      TARGET_RESOURCE,
                      PAYLOAD,
                      ApprovalStatus.PENDING_APPROVAL,
                      REQUESTED_BY,
                      null,
                      null,
                      CREATED_AT))
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining("actionType must not be null");
    }

    @Test
    @DisplayName("should reject null targetResource")
    void should_rejectCreation_when_targetResourceIsNull() {
      assertThatThrownBy(
              () ->
                  new PendingApproval(
                      ID,
                      ANALYSIS_ID,
                      ActionType.UPDATE,
                      null,
                      PAYLOAD,
                      ApprovalStatus.PENDING_APPROVAL,
                      REQUESTED_BY,
                      null,
                      null,
                      CREATED_AT))
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining("targetResource must not be null");
    }

    @Test
    @DisplayName("should reject blank targetResource")
    void should_rejectCreation_when_targetResourceIsBlank() {
      assertThatThrownBy(
              () ->
                  new PendingApproval(
                      ID,
                      ANALYSIS_ID,
                      ActionType.UPDATE,
                      "",
                      PAYLOAD,
                      ApprovalStatus.PENDING_APPROVAL,
                      REQUESTED_BY,
                      null,
                      null,
                      CREATED_AT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("targetResource must not be blank");
    }

    @Test
    @DisplayName("should reject null payload")
    void should_rejectCreation_when_payloadIsNull() {
      assertThatThrownBy(
              () ->
                  new PendingApproval(
                      ID,
                      ANALYSIS_ID,
                      ActionType.DELETE,
                      TARGET_RESOURCE,
                      null,
                      ApprovalStatus.PENDING_APPROVAL,
                      REQUESTED_BY,
                      null,
                      null,
                      CREATED_AT))
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining("payload must not be null");
    }

    @Test
    @DisplayName("should reject blank payload")
    void should_rejectCreation_when_payloadIsBlank() {
      assertThatThrownBy(
              () ->
                  new PendingApproval(
                      ID,
                      ANALYSIS_ID,
                      ActionType.DELETE,
                      TARGET_RESOURCE,
                      "   ",
                      ApprovalStatus.PENDING_APPROVAL,
                      REQUESTED_BY,
                      null,
                      null,
                      CREATED_AT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("payload must not be blank");
    }

    @Test
    @DisplayName("should reject null requestedBy")
    void should_rejectCreation_when_requestedByIsNull() {
      assertThatThrownBy(
              () ->
                  new PendingApproval(
                      ID,
                      ANALYSIS_ID,
                      ActionType.CREATE,
                      TARGET_RESOURCE,
                      PAYLOAD,
                      ApprovalStatus.PENDING_APPROVAL,
                      null,
                      null,
                      null,
                      CREATED_AT))
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining("requestedBy must not be null");
    }

    @Test
    @DisplayName("should reject blank requestedBy")
    void should_rejectCreation_when_requestedByIsBlank() {
      assertThatThrownBy(
              () ->
                  new PendingApproval(
                      ID,
                      ANALYSIS_ID,
                      ActionType.CREATE,
                      TARGET_RESOURCE,
                      PAYLOAD,
                      ApprovalStatus.PENDING_APPROVAL,
                      "",
                      null,
                      null,
                      CREATED_AT))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("requestedBy must not be blank");
    }

    @Test
    @DisplayName("should reject null createdAt")
    void should_rejectCreation_when_createdAtIsNull() {
      assertThatThrownBy(
              () ->
                  new PendingApproval(
                      ID,
                      ANALYSIS_ID,
                      ActionType.CREATE,
                      TARGET_RESOURCE,
                      PAYLOAD,
                      ApprovalStatus.PENDING_APPROVAL,
                      REQUESTED_BY,
                      null,
                      null,
                      null))
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining("createdAt must not be null");
    }
  }

  @Nested
  @DisplayName("Factory method createFromAnalysis")
  class FactoryMethodTests {

    @Test
    @DisplayName("should create approval with PENDING_APPROVAL status")
    void should_createWithPendingStatus_when_usingFactoryMethod() {
      var approval =
          PendingApproval.createFromAnalysis(
              ID,
              ANALYSIS_ID,
              ActionType.CREATE,
              TARGET_RESOURCE,
              PAYLOAD,
              REQUESTED_BY,
              CREATED_AT);

      assertThat(approval.getStatus()).isEqualTo(ApprovalStatus.PENDING_APPROVAL);
      assertThat(approval.getDecidedBy()).isNull();
      assertThat(approval.getDecidedAt()).isNull();
      assertThat(approval.isPending()).isTrue();
      assertThat(approval.isDecided()).isFalse();
    }

    @Test
    @DisplayName("should create approval for DELETE action")
    void should_createApproval_when_deleteActionProposed() {
      var approval =
          PendingApproval.createFromAnalysis(
              ID,
              ANALYSIS_ID,
              ActionType.DELETE,
              "documents/doc-789",
              "{\"reason\":\"outdated\"}",
              REQUESTED_BY,
              CREATED_AT);

      assertThat(approval.getActionType()).isEqualTo(ActionType.DELETE);
      assertThat(approval.getTargetResource()).isEqualTo("documents/doc-789");
    }

    @Test
    @DisplayName("should create approval for UPDATE action")
    void should_createApproval_when_updateActionProposed() {
      var approval =
          PendingApproval.createFromAnalysis(
              ID,
              ANALYSIS_ID,
              ActionType.UPDATE,
              "users/user-001",
              "{\"role\":\"ADMIN\"}",
              REQUESTED_BY,
              CREATED_AT);

      assertThat(approval.getActionType()).isEqualTo(ActionType.UPDATE);
    }
  }

  @Nested
  @DisplayName("Approval workflow")
  class ApprovalWorkflowTests {

    @Test
    @DisplayName("should approve pending approval")
    void should_transitionToApproved_when_approved() {
      var approval =
          PendingApproval.createFromAnalysis(
              ID,
              ANALYSIS_ID,
              ActionType.CREATE,
              TARGET_RESOURCE,
              PAYLOAD,
              REQUESTED_BY,
              CREATED_AT);
      var decidedAt = Instant.parse("2024-01-15T11:00:00Z");

      approval.approve("user-admin", decidedAt);

      assertThat(approval.getStatus()).isEqualTo(ApprovalStatus.APPROVED);
      assertThat(approval.getDecidedBy()).isEqualTo("user-admin");
      assertThat(approval.getDecidedAt()).isEqualTo(decidedAt);
      assertThat(approval.isDecided()).isTrue();
      assertThat(approval.isPending()).isFalse();
    }

    @Test
    @DisplayName("should reject pending approval")
    void should_transitionToRejected_when_rejected() {
      var approval =
          PendingApproval.createFromAnalysis(
              ID,
              ANALYSIS_ID,
              ActionType.DELETE,
              TARGET_RESOURCE,
              PAYLOAD,
              REQUESTED_BY,
              CREATED_AT);
      var decidedAt = Instant.parse("2024-01-15T11:30:00Z");

      approval.reject("user-reviewer", decidedAt);

      assertThat(approval.getStatus()).isEqualTo(ApprovalStatus.REJECTED);
      assertThat(approval.getDecidedBy()).isEqualTo("user-reviewer");
      assertThat(approval.getDecidedAt()).isEqualTo(decidedAt);
      assertThat(approval.isDecided()).isTrue();
      assertThat(approval.isPending()).isFalse();
    }

    @Test
    @DisplayName("should reject approving an already approved record")
    void should_throwException_when_approvingAlreadyApproved() {
      var approval =
          PendingApproval.createFromAnalysis(
              ID,
              ANALYSIS_ID,
              ActionType.CREATE,
              TARGET_RESOURCE,
              PAYLOAD,
              REQUESTED_BY,
              CREATED_AT);
      approval.approve("user-1", Instant.now());

      assertThatThrownBy(() -> approval.approve("user-2", Instant.now()))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Cannot approve")
          .hasMessageContaining("APPROVED");
    }

    @Test
    @DisplayName("should reject rejecting an already rejected record")
    void should_throwException_when_rejectingAlreadyRejected() {
      var approval =
          PendingApproval.createFromAnalysis(
              ID,
              ANALYSIS_ID,
              ActionType.UPDATE,
              TARGET_RESOURCE,
              PAYLOAD,
              REQUESTED_BY,
              CREATED_AT);
      approval.reject("user-1", Instant.now());

      assertThatThrownBy(() -> approval.reject("user-2", Instant.now()))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Cannot reject")
          .hasMessageContaining("REJECTED");
    }

    @Test
    @DisplayName("should reject approving with null userId")
    void should_throwException_when_approvingWithNullUserId() {
      var approval =
          PendingApproval.createFromAnalysis(
              ID,
              ANALYSIS_ID,
              ActionType.CREATE,
              TARGET_RESOURCE,
              PAYLOAD,
              REQUESTED_BY,
              CREATED_AT);

      assertThatThrownBy(() -> approval.approve(null, Instant.now()))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should reject approving with blank userId")
    void should_throwException_when_approvingWithBlankUserId() {
      var approval =
          PendingApproval.createFromAnalysis(
              ID,
              ANALYSIS_ID,
              ActionType.CREATE,
              TARGET_RESOURCE,
              PAYLOAD,
              REQUESTED_BY,
              CREATED_AT);

      assertThatThrownBy(() -> approval.approve("  ", Instant.now()))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("userId must not be blank");
    }

    @Test
    @DisplayName("should reject rejecting with null decidedAt")
    void should_throwException_when_rejectingWithNullDecidedAt() {
      var approval =
          PendingApproval.createFromAnalysis(
              ID,
              ANALYSIS_ID,
              ActionType.DELETE,
              TARGET_RESOURCE,
              PAYLOAD,
              REQUESTED_BY,
              CREATED_AT);

      assertThatThrownBy(() -> approval.reject("user-1", null))
          .isInstanceOf(NullPointerException.class);
    }
  }

  @Nested
  @DisplayName("ActionType enum")
  class ActionTypeTests {

    @Test
    @DisplayName("should have all three mutable action types")
    void should_haveThreeMutableActionTypes() {
      assertThat(ActionType.values())
          .containsExactly(ActionType.CREATE, ActionType.UPDATE, ActionType.DELETE);
    }

    @Test
    @DisplayName("all action types should be mutable")
    void should_allBeMutable() {
      for (ActionType type : ActionType.values()) {
        assertThat(type.isMutable()).isTrue();
      }
    }
  }

  @Nested
  @DisplayName("ApprovalStatus enum")
  class ApprovalStatusTests {

    @Test
    @DisplayName("should have all three statuses")
    void should_haveThreeStatuses() {
      assertThat(ApprovalStatus.values())
          .containsExactly(
              ApprovalStatus.PENDING_APPROVAL, ApprovalStatus.APPROVED, ApprovalStatus.REJECTED);
    }
  }
}
