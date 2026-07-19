package com.atlasops.approvals.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlasops.approvals.domain.Approval;
import com.atlasops.approvals.domain.ApprovalStatus;
import com.atlasops.approvals.domain.ports.ApprovalRepository;
import com.atlasops.shared.domain.exceptions.ForbiddenActionException;
import com.atlasops.shared.domain.ports.Clock;
import com.atlasops.shared.domain.ports.EventPublisher;
import java.time.Instant;
import java.util.Optional;
import net.jqwik.api.*;

/**
 * Property-based test for Approval Role Enforcement.
 *
 * <p><b>Validates: Requirements 13.4, 13.5</b>
 *
 * <p>Property 20: Approval Role Enforcement
 *
 * <p>Requirement 13.4: THE Approval_Module SHALL enforce that only users with ANALYST or ADMIN role
 * can approve or reject documents.
 *
 * <p>Requirement 13.5: IF a non-authorized user attempts an approval action, THEN THE
 * Approval_Module SHALL deny the action and return a 403 Forbidden error.
 *
 * <p>This test verifies that: - For ANY user with ANALYST or ADMIN role, approve/reject actions
 * succeed (Req 13.4) - For ANY user without ANALYST or ADMIN role, approve/reject actions throw
 * ForbiddenActionException (Req 13.5)
 */
@Tag("Feature: project-implementation-kickoff")
@Tag("Property 20: Approval Role Enforcement")
class ApprovalRoleEnforcementPropertyTest {

  private static final Instant FIXED_NOW = Instant.parse("2025-01-15T10:00:00Z");
  private static final Instant CREATION_TIME = Instant.parse("2025-01-15T09:00:00Z");

  // ─── Property Tests ──────────────────────────────────────────────────────────

  /**
   * Property: For ANY user with an authorized role (ANALYST or ADMIN), the approve action SHALL
   * succeed and transition the approval to APPROVED status.
   *
   * <p>Validates: Requirement 13.4
   */
  @Property(tries = 100)
  void should_allowApproval_when_userHasAuthorizedRole(
      @ForAll("authorizedRoles") String role,
      @ForAll("validUserIds") String userId,
      @ForAll("validApprovalIds") String approvalId,
      @ForAll("validTenantIds") String tenantId) {

    // Arrange
    ApprovalRepository repository = mock(ApprovalRepository.class);
    EventPublisher eventPublisher = mock(EventPublisher.class);
    Clock clock = mock(Clock.class);

    var useCase = new ApproveDocumentUseCase(repository, eventPublisher, clock);

    Approval pendingApproval =
        Approval.createPending(approvalId, tenantId, "doc-123", CREATION_TIME);
    when(repository.findById(approvalId, tenantId)).thenReturn(Optional.of(pendingApproval));
    when(repository.save(any(Approval.class))).thenAnswer(inv -> inv.getArgument(0));
    when(clock.now()).thenReturn(FIXED_NOW);

    var command = new ApproveDocumentCommand(approvalId, userId, role, tenantId, "corr-001");

    // Act
    Approval result = useCase.execute(command);

    // Assert
    assertThat(result.getStatus())
        .as("Authorized role '%s' must be able to approve", role)
        .isEqualTo(ApprovalStatus.APPROVED);
    assertThat(result.getDecisionBy())
        .as("DecisionBy must be recorded as the approving user")
        .isEqualTo(userId);
  }

  /**
   * Property: For ANY user with an authorized role (ANALYST or ADMIN), the reject action SHALL
   * succeed and transition the approval to REJECTED status.
   *
   * <p>Validates: Requirement 13.4
   */
  @Property(tries = 100)
  void should_allowRejection_when_userHasAuthorizedRole(
      @ForAll("authorizedRoles") String role,
      @ForAll("validUserIds") String userId,
      @ForAll("validApprovalIds") String approvalId,
      @ForAll("validTenantIds") String tenantId,
      @ForAll("validRejectionReasons") String reason) {

    // Arrange
    ApprovalRepository repository = mock(ApprovalRepository.class);
    EventPublisher eventPublisher = mock(EventPublisher.class);
    Clock clock = mock(Clock.class);

    var useCase = new RejectDocumentUseCase(repository, eventPublisher, clock);

    Approval pendingApproval =
        Approval.createPending(approvalId, tenantId, "doc-456", CREATION_TIME);
    when(repository.findById(approvalId, tenantId)).thenReturn(Optional.of(pendingApproval));
    when(repository.save(any(Approval.class))).thenAnswer(inv -> inv.getArgument(0));
    when(clock.now()).thenReturn(FIXED_NOW);

    var command = new RejectDocumentCommand(approvalId, userId, reason, role, tenantId, "corr-002");

    // Act
    Approval result = useCase.execute(command);

    // Assert
    assertThat(result.getStatus())
        .as("Authorized role '%s' must be able to reject", role)
        .isEqualTo(ApprovalStatus.REJECTED);
    assertThat(result.getDecisionBy())
        .as("DecisionBy must be recorded as the rejecting user")
        .isEqualTo(userId);
    assertThat(result.getRejectionReason()).as("Rejection reason must be stored").isEqualTo(reason);
  }

  /**
   * Property: For ANY user with an unauthorized role (CLIENT or any invalid role string), the
   * approve action SHALL throw ForbiddenActionException.
   *
   * <p>Validates: Requirement 13.5
   */
  @Property(tries = 100)
  void should_denyApproval_when_userHasUnauthorizedRole(
      @ForAll("unauthorizedRoles") String role,
      @ForAll("validUserIds") String userId,
      @ForAll("validApprovalIds") String approvalId,
      @ForAll("validTenantIds") String tenantId) {

    // Arrange
    ApprovalRepository repository = mock(ApprovalRepository.class);
    EventPublisher eventPublisher = mock(EventPublisher.class);
    Clock clock = mock(Clock.class);

    var useCase = new ApproveDocumentUseCase(repository, eventPublisher, clock);

    var command = new ApproveDocumentCommand(approvalId, userId, role, tenantId, "corr-003");

    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(command))
        .as("Unauthorized role '%s' must be denied approval action", role)
        .isInstanceOf(ForbiddenActionException.class);

    // Verify: repository was never queried (fail-fast before DB access)
    verify(repository, never()).findById(anyString(), anyString());
    verify(repository, never()).save(any(Approval.class));
  }

  /**
   * Property: For ANY user with an unauthorized role (CLIENT or any invalid role string), the
   * reject action SHALL throw ForbiddenActionException.
   *
   * <p>Validates: Requirement 13.5
   */
  @Property(tries = 100)
  void should_denyRejection_when_userHasUnauthorizedRole(
      @ForAll("unauthorizedRoles") String role,
      @ForAll("validUserIds") String userId,
      @ForAll("validApprovalIds") String approvalId,
      @ForAll("validTenantIds") String tenantId,
      @ForAll("validRejectionReasons") String reason) {

    // Arrange
    ApprovalRepository repository = mock(ApprovalRepository.class);
    EventPublisher eventPublisher = mock(EventPublisher.class);
    Clock clock = mock(Clock.class);

    var useCase = new RejectDocumentUseCase(repository, eventPublisher, clock);

    var command = new RejectDocumentCommand(approvalId, userId, reason, role, tenantId, "corr-004");

    // Act & Assert
    assertThatThrownBy(() -> useCase.execute(command))
        .as("Unauthorized role '%s' must be denied rejection action", role)
        .isInstanceOf(ForbiddenActionException.class);

    // Verify: repository was never queried (fail-fast before DB access)
    verify(repository, never()).findById(anyString(), anyString());
    verify(repository, never()).save(any(Approval.class));
  }

  // ─── Generators ──────────────────────────────────────────────────────────────

  @Provide
  Arbitrary<String> authorizedRoles() {
    return Arbitraries.of("ANALYST", "ADMIN");
  }

  @Provide
  Arbitrary<String> unauthorizedRoles() {
    return Arbitraries.frequencyOf(
            Tuple.of(5, Arbitraries.of("CLIENT")),
            Tuple.of(
                3,
                Arbitraries.strings()
                    .withCharRange('A', 'Z')
                    .ofMinLength(3)
                    .ofMaxLength(20)
                    .filter(
                        s -> !s.equals("ANALYST") && !s.equals("ADMIN") && !s.equals("CLIENT"))),
            Tuple.of(
                2, Arbitraries.of("analyst", "admin", "client", "VIEWER", "MANAGER", "USER", "")))
        .filter(s -> !s.equals("ANALYST") && !s.equals("ADMIN"));
  }

  @Provide
  Arbitrary<String> validUserIds() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .numeric()
        .ofMinLength(3)
        .ofMaxLength(15)
        .map(s -> "user-" + s);
  }

  @Provide
  Arbitrary<String> validApprovalIds() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .numeric()
        .ofMinLength(5)
        .ofMaxLength(15)
        .map(s -> "approval-" + s);
  }

  @Provide
  Arbitrary<String> validTenantIds() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .numeric()
        .ofMinLength(3)
        .ofMaxLength(10)
        .map(s -> "tenant-" + s);
  }

  @Provide
  Arbitrary<String> validRejectionReasons() {
    return Arbitraries.strings()
        .withCharRange('a', 'z')
        .withChars(' ', '.', ',')
        .ofMinLength(10)
        .ofMaxLength(200)
        .filter(s -> !s.isBlank() && s.length() >= 10);
  }
}
