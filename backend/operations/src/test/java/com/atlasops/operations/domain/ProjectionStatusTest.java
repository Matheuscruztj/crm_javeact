package com.atlasops.operations.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ProjectionStatus domain entity.
 * Validates: P2.7 — Projection health registry lifecycle
 */
class ProjectionStatusTest {

  private static final Instant NOW = Instant.parse("2025-01-15T10:00:00Z");

  @Test
  void should_createProjection_with_pendingStatus() {
    ProjectionStatus ps = ProjectionStatus.create("search-index");
    assertThat(ps.getName()).isEqualTo("search-index");
    assertThat(ps.getStatus()).isEqualTo(ProjectionStatus.Status.PENDING);
    assertThat(ps.getLag()).isZero();
    assertThat(ps.getLastBuiltAt()).isNull();
    assertThat(ps.getErrorMessage()).isNull();
  }

  @Test
  void should_markReady_when_buildCompletes() {
    ProjectionStatus ps = ProjectionStatus.create("vector-index");
    ps.markReady(NOW);

    assertThat(ps.getStatus()).isEqualTo(ProjectionStatus.Status.READY);
    assertThat(ps.getLastBuiltAt()).isEqualTo(NOW);
    assertThat(ps.getLag()).isZero();
    assertThat(ps.getErrorMessage()).isNull();
  }

  @Test
  void should_markFailed_when_buildErrorOccurs() {
    ProjectionStatus ps = ProjectionStatus.create("analytics");
    ps.markFailed("Connection timeout", NOW);

    assertThat(ps.getStatus()).isEqualTo(ProjectionStatus.Status.FAILED);
    assertThat(ps.getErrorMessage()).isEqualTo("Connection timeout");
    assertThat(ps.getLastBuiltAt()).isEqualTo(NOW);
  }

  @Test
  void should_becomeStale_when_lagIncreases() {
    ProjectionStatus ps = ProjectionStatus.create("graph");
    ps.markReady(NOW);

    ps.updateLag(100L);

    assertThat(ps.getStatus()).isEqualTo(ProjectionStatus.Status.STALE);
    assertThat(ps.getLag()).isEqualTo(100L);
  }

  @Test
  void should_notChangeStatus_when_lagIsZeroAfterReady() {
    ProjectionStatus ps = ProjectionStatus.create("timeline");
    ps.markReady(NOW);

    ps.updateLag(0L);

    assertThat(ps.getStatus()).isEqualTo(ProjectionStatus.Status.READY);
    assertThat(ps.getLag()).isZero();
  }

  @Test
  void should_throwIllegalArgument_when_nameIsBlank() {
    assertThatThrownBy(() -> ProjectionStatus.create("  "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("name");
  }

  @Test
  void should_throwNullPointer_when_nameIsNull() {
    assertThatThrownBy(() -> ProjectionStatus.create(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void should_reconstitute_fromPersistedState() {
    ProjectionStatus ps = ProjectionStatus.reconstitute(
        "search-index", ProjectionStatus.Status.READY, NOW, 0L, null);

    assertThat(ps.getName()).isEqualTo("search-index");
    assertThat(ps.getStatus()).isEqualTo(ProjectionStatus.Status.READY);
    assertThat(ps.getLastBuiltAt()).isEqualTo(NOW);
  }
}
