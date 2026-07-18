package com.atlasops.shared.harness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class SandboxNamingConventionTest {

  @Test
  void should_generateRunId_when_issueAndRoleProvided() {
    String runId = SandboxNamingConvention.runId("ATLAS-42", AgentRole.A2);

    assertThat(runId).isEqualTo("ATLAS-42-agent-A2");
  }

  @Test
  void should_generateRunIdWithA11_when_roleIsA11() {
    String runId = SandboxNamingConvention.runId("PROJ-100", AgentRole.A11);

    assertThat(runId).isEqualTo("PROJ-100-agent-A11");
  }

  @ParameterizedTest
  @EnumSource(AgentRole.class)
  void should_generateRunId_forAllRoles(AgentRole role) {
    String runId = SandboxNamingConvention.runId("ISSUE-1", role);

    assertThat(runId).isEqualTo("ISSUE-1-agent-" + role.code());
  }

  @Test
  void should_generateBranchName_when_runIdProvided() {
    String branch = SandboxNamingConvention.branchName("ATLAS-42-agent-A2");

    assertThat(branch).isEqualTo("sandbox/ATLAS-42-agent-A2");
  }

  @Test
  void should_generateDatabaseName_when_issueProvided() {
    String dbName = SandboxNamingConvention.databaseName("ATLAS-42");

    assertThat(dbName).isEqualTo("atlasops_ATLAS_42");
  }

  @Test
  void should_normalizeHyphens_when_issueContainsHyphens() {
    String dbName = SandboxNamingConvention.databaseName("MY-PROJ-123");

    assertThat(dbName).isEqualTo("atlasops_MY_PROJ_123");
  }

  @Test
  void should_generateComposeProject_when_issueProvided() {
    String project = SandboxNamingConvention.composeProject("ATLAS-42");

    assertThat(project).isEqualTo("atlasops_ATLAS_42");
  }

  @Test
  void should_matchDatabaseAndComposeNaming() {
    String issue = "ATLAS-42";
    String dbName = SandboxNamingConvention.databaseName(issue);
    String composeName = SandboxNamingConvention.composeProject(issue);

    assertThat(dbName).isEqualTo(composeName);
  }

  @Test
  void should_generateBucketPrefix_when_issueProvided() {
    String prefix = SandboxNamingConvention.bucketPrefix("ATLAS-42");

    assertThat(prefix).isEqualTo("ATLAS-42/");
  }

  @Test
  void should_endWithSlash_when_bucketPrefixGenerated() {
    String prefix = SandboxNamingConvention.bucketPrefix("ANY-ISSUE");

    assertThat(prefix).endsWith("/");
  }

  @Test
  void should_createCompleteResources_when_allParamsProvided() {
    Instant now = Instant.parse("2024-01-15T10:00:00Z");

    SandboxResources resources =
        SandboxNamingConvention.createResources("ATLAS-42", AgentRole.A2, now);

    assertThat(resources.runId()).isEqualTo("ATLAS-42-agent-A2");
    assertThat(resources.branchName()).isEqualTo("sandbox/ATLAS-42-agent-A2");
    assertThat(resources.databaseName()).isEqualTo("atlasops_ATLAS_42");
    assertThat(resources.composeProject()).isEqualTo("atlasops_ATLAS_42");
    assertThat(resources.bucketPrefix()).isEqualTo("ATLAS-42/");
    assertThat(resources.createdAt()).isEqualTo(now);
    assertThat(resources.expiresAt()).isEqualTo(now.plus(SandboxResources.TTL));
  }

  @Test
  void should_throwNPE_when_issueIsNull() {
    assertThatThrownBy(() -> SandboxNamingConvention.runId(null, AgentRole.A1))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void should_throwNPE_when_roleIsNull() {
    assertThatThrownBy(() -> SandboxNamingConvention.runId("ISSUE-1", null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void should_throwNPE_when_branchRunIdIsNull() {
    assertThatThrownBy(() -> SandboxNamingConvention.branchName(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void should_throwNPE_when_databaseIssueIsNull() {
    assertThatThrownBy(() -> SandboxNamingConvention.databaseName(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void should_throwNPE_when_composeIssueIsNull() {
    assertThatThrownBy(() -> SandboxNamingConvention.composeProject(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void should_throwNPE_when_bucketIssueIsNull() {
    assertThatThrownBy(() -> SandboxNamingConvention.bucketPrefix(null))
        .isInstanceOf(NullPointerException.class);
  }
}
