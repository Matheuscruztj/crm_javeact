package com.atlasops.shared.harness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProhibitedActionEnforcerTest {

  private ProhibitedActionEnforcer enforcer;

  @BeforeEach
  void setUp() {
    enforcer = new ProhibitedActionEnforcer(() -> Instant.parse("2026-07-27T10:15:30Z"));
  }

  @Test
  void should_blockSecretAccess_when_targetReferencesProductionSecrets() {
    var action = new ActionDescriptor(ActionType.SECRET_ACCESS, ".env.production", "run-001");

    var result = enforcer.check(AgentRole.A2, action);

    assertThat(result).isEqualTo(EnforcementResult.BLOCKED);
    assertThat(enforcer.getAuditLog()).hasSize(1);
    assertThat(enforcer.getAuditLog().get(0).reason())
        .contains("Access to production secrets is prohibited");
  }

  @Test
  void should_blockForcePush_when_commandUsesProtectedFlag() {
    var action =
        new ActionDescriptor(ActionType.COMMAND, "git push --force origin main", "run-002");

    var result = enforcer.check(AgentRole.A2, action);

    assertThat(result).isEqualTo(EnforcementResult.BLOCKED);
    assertThat(enforcer.getAuditLog()).hasSize(1);
    assertThat(enforcer.getAuditLog().get(0).attemptedAction())
        .isEqualTo("COMMAND: git push --force origin main");
  }

  @Test
  void should_allowSafeAction_when_targetIsNotRestricted() {
    var action = new ActionDescriptor(ActionType.COMMAND, "git status", "run-003");

    var result = enforcer.check(AgentRole.A2, action);

    assertThat(result).isEqualTo(EnforcementResult.ALLOWED);
    assertThat(enforcer.getAuditLog()).isEmpty();
  }

  @Test
  void should_rejectNullRole_orAction_when_inputsAreMissing() {
    var action = new ActionDescriptor(ActionType.COMMAND, "git status", "run-004");

    assertThatThrownBy(() -> enforcer.check(null, action)).isInstanceOf(NullPointerException.class);
    assertThatThrownBy(() -> enforcer.check(AgentRole.A2, null)).isInstanceOf(NullPointerException.class);
  }
}
