package com.atlasops.shared.harness;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class SandboxResourcesTest {

  @Test
  void should_haveTtlOf24Hours() {
    assertThat(SandboxResources.TTL).isEqualTo(Duration.ofHours(24));
  }

  @Test
  void should_haveCleanupDelayOf5Minutes() {
    assertThat(SandboxResources.CLEANUP_DELAY).isEqualTo(Duration.ofMinutes(5));
  }

  @Test
  void should_calculateExpiresAt_when_withTtlUsed() {
    Instant createdAt = Instant.parse("2024-01-15T10:00:00Z");
    Instant expectedExpiry = Instant.parse("2024-01-16T10:00:00Z"); // +24h

    SandboxResources resources =
        SandboxResources.withTtl(
            "ATLAS-42-agent-A2",
            "sandbox/ATLAS-42-agent-A2",
            "atlasops_ATLAS_42",
            "atlasops_ATLAS_42",
            "ATLAS-42/",
            createdAt);

    assertThat(resources.expiresAt()).isEqualTo(expectedExpiry);
    assertThat(resources.createdAt()).isEqualTo(createdAt);
  }

  @Test
  void should_notBeExpired_when_beforeExpiryTime() {
    Instant createdAt = Instant.parse("2024-01-15T10:00:00Z");
    Instant now = Instant.parse("2024-01-15T20:00:00Z"); // 10h later

    SandboxResources resources =
        SandboxResources.withTtl("run-id", "branch", "db", "compose", "bucket/", createdAt);

    assertThat(resources.isExpired(now)).isFalse();
  }

  @Test
  void should_beExpired_when_afterExpiryTime() {
    Instant createdAt = Instant.parse("2024-01-15T10:00:00Z");
    Instant now = Instant.parse("2024-01-16T10:00:01Z"); // 24h + 1s later

    SandboxResources resources =
        SandboxResources.withTtl("run-id", "branch", "db", "compose", "bucket/", createdAt);

    assertThat(resources.isExpired(now)).isTrue();
  }

  @Test
  void should_beExpired_when_exactlyAtExpiryTime() {
    Instant createdAt = Instant.parse("2024-01-15T10:00:00Z");
    Instant now = Instant.parse("2024-01-16T10:00:00Z"); // exactly 24h

    SandboxResources resources =
        SandboxResources.withTtl("run-id", "branch", "db", "compose", "bucket/", createdAt);

    assertThat(resources.isExpired(now)).isTrue();
  }

  @Test
  void should_notBeExpired_when_justBeforeExpiryTime() {
    Instant createdAt = Instant.parse("2024-01-15T10:00:00Z");
    Instant now = Instant.parse("2024-01-16T09:59:59Z"); // 1s before expiry

    SandboxResources resources =
        SandboxResources.withTtl("run-id", "branch", "db", "compose", "bucket/", createdAt);

    assertThat(resources.isExpired(now)).isFalse();
  }

  @Test
  void should_preserveAllFields_when_constructed() {
    Instant createdAt = Instant.now();
    Instant expiresAt = createdAt.plus(SandboxResources.TTL);

    SandboxResources resources =
        new SandboxResources(
            "my-run-id",
            "sandbox/my-run-id",
            "atlasops_my_db",
            "atlasops_my_project",
            "my-issue/",
            createdAt,
            expiresAt);

    assertThat(resources.runId()).isEqualTo("my-run-id");
    assertThat(resources.branchName()).isEqualTo("sandbox/my-run-id");
    assertThat(resources.databaseName()).isEqualTo("atlasops_my_db");
    assertThat(resources.composeProject()).isEqualTo("atlasops_my_project");
    assertThat(resources.bucketPrefix()).isEqualTo("my-issue/");
    assertThat(resources.createdAt()).isEqualTo(createdAt);
    assertThat(resources.expiresAt()).isEqualTo(expiresAt);
  }
}
