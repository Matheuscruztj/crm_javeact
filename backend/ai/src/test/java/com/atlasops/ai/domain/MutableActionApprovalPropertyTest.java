package com.atlasops.ai.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import net.jqwik.api.*;

/**
 * Property-based tests for Mutable AI Action Creates Approval.
 *
 * <p><b>Validates: Requirements 4.10</b>
 *
 * <p>Property 8: For any AI analysis result that proposes a mutable action (create, update, or
 * delete), the system SHALL create a PendingApproval record containing: analysisId, actionType,
 * targetResource, payload, and status=PENDING_APPROVAL, before any mutation is executed.
 */
@Tag("Feature: monorepo-sdd-harness, Property 8: Mutable AI Action Creates Approval")
class MutableActionApprovalPropertyTest {

  // ─── Property: createFromAnalysis always sets status to PENDING_APPROVAL ──────

  @Property(tries = 100)
  void createFromAnalysis_shouldAlwaysSetStatusToPendingApproval(
      @ForAll("validActionTypes") ActionType actionType,
      @ForAll("validAnalysisIds") String analysisId,
      @ForAll("validTargetResources") String targetResource,
      @ForAll("validPayloads") String payload,
      @ForAll("validRequestedBy") String requestedBy) {

    PendingApproval approval =
        PendingApproval.createFromAnalysis(
            "approval-" + System.nanoTime(),
            analysisId,
            actionType,
            targetResource,
            payload,
            requestedBy,
            Instant.now());

    assertThat(approval.getStatus())
        .as(
            "Status must always be PENDING_APPROVAL when created from analysis, but got: %s",
            approval.getStatus())
        .isEqualTo(ApprovalStatus.PENDING_APPROVAL);
  }

  // ─── Property: createFromAnalysis always contains all required fields ──────────

  @Property(tries = 100)
  void createFromAnalysis_shouldAlwaysContainAllRequiredFields(
      @ForAll("validActionTypes") ActionType actionType,
      @ForAll("validAnalysisIds") String analysisId,
      @ForAll("validTargetResources") String targetResource,
      @ForAll("validPayloads") String payload,
      @ForAll("validRequestedBy") String requestedBy) {

    PendingApproval approval =
        PendingApproval.createFromAnalysis(
            "approval-" + System.nanoTime(),
            analysisId,
            actionType,
            targetResource,
            payload,
            requestedBy,
            Instant.now());

    assertThat(approval.getAnalysisId())
        .as("analysisId must be present and match the provided value")
        .isNotNull()
        .isNotBlank()
        .isEqualTo(analysisId);

    assertThat(approval.getActionType())
        .as("actionType must be present and match the provided value")
        .isNotNull()
        .isEqualTo(actionType);

    assertThat(approval.getTargetResource())
        .as("targetResource must be present and match the provided value")
        .isNotNull()
        .isNotBlank()
        .isEqualTo(targetResource);

    assertThat(approval.getPayload())
        .as("payload must be present and match the provided value")
        .isNotNull()
        .isNotBlank()
        .isEqualTo(payload);
  }

  // ─── Property: No mutation is executed (decided fields are null) until approval ─

  @Property(tries = 100)
  void createFromAnalysis_shouldHaveNullDecisionFieldsUntilApproval(
      @ForAll("validActionTypes") ActionType actionType,
      @ForAll("validAnalysisIds") String analysisId,
      @ForAll("validTargetResources") String targetResource,
      @ForAll("validPayloads") String payload,
      @ForAll("validRequestedBy") String requestedBy) {

    PendingApproval approval =
        PendingApproval.createFromAnalysis(
            "approval-" + System.nanoTime(),
            analysisId,
            actionType,
            targetResource,
            payload,
            requestedBy,
            Instant.now());

    assertThat(approval.getDecidedBy()).as("decidedBy must be null before human approval").isNull();

    assertThat(approval.getDecidedAt()).as("decidedAt must be null before human approval").isNull();

    assertThat(approval.isPending())
        .as("approval must be in pending state before human decision")
        .isTrue();

    assertThat(approval.isDecided())
        .as("approval must NOT be decided before human decision")
        .isFalse();
  }

  // ─── Property: All ActionType values are mutable ──────────────────────────────

  @Property(tries = 100)
  void allActionTypes_shouldBeMutable(@ForAll("validActionTypes") ActionType actionType) {

    assertThat(actionType.isMutable()).as("ActionType %s must be mutable", actionType).isTrue();
  }

  // ─── Generators ──────────────────────────────────────────────────────────────

  @Provide
  Arbitrary<ActionType> validActionTypes() {
    return Arbitraries.of(ActionType.CREATE, ActionType.UPDATE, ActionType.DELETE);
  }

  @Provide
  Arbitrary<String> validAnalysisIds() {
    return Arbitraries.strings()
        .alpha()
        .numeric()
        .ofMinLength(1)
        .ofMaxLength(50)
        .map(s -> "analysis-" + s);
  }

  @Provide
  Arbitrary<String> validTargetResources() {
    return Arbitraries.of(
        "customer/123",
        "document/456",
        "request/789",
        "task/abc-def",
        "workflow/step-1",
        "tenant/alpha-corp",
        "user/john-doe",
        "pipeline/etl-daily");
  }

  @Provide
  Arbitrary<String> validPayloads() {
    return Arbitraries.of(
        "{\"name\": \"New Customer\", \"email\": \"new@example.com\"}",
        "{\"status\": \"ACTIVE\", \"tier\": \"GOLD\"}",
        "{\"action\": \"archive\", \"reason\": \"expired\"}",
        "{\"title\": \"Updated Document\", \"version\": 2}",
        "{\"assignee\": \"user-42\", \"priority\": \"HIGH\"}",
        "{\"field\": \"address\", \"newValue\": \"123 Main St\"}",
        "{\"deleteReason\": \"duplicate\", \"mergeTarget\": \"entity-99\"}",
        "{\"description\": \"AI-generated summary of analysis results\"}");
  }

  @Provide
  Arbitrary<String> validRequestedBy() {
    return Arbitraries.of(
        "agent-A1",
        "agent-A2",
        "agent-A3",
        "analysis-pipeline",
        "ai-orchestrator",
        "document-analyzer",
        "classification-engine");
  }
}
