package com.atlasops.boot.infrastructure.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.sql.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

class OutboxEventCleanupJobTest {

  private JdbcTemplate jdbcTemplate;
  private SimpleMeterRegistry meterRegistry;
  private OutboxEventCleanupJob job;

  @BeforeEach
  void setUp() {
    jdbcTemplate = org.mockito.Mockito.mock(JdbcTemplate.class);
    meterRegistry = new SimpleMeterRegistry();
    job = new OutboxEventCleanupJob(jdbcTemplate, meterRegistry);
    ReflectionTestUtils.setField(job, "retentionDays", 3);
  }

  @Test
  void should_incrementCounter_whenOldPublishedEventsAreDeleted() {
    when(jdbcTemplate.update(org.mockito.ArgumentMatchers.anyString(), any(Timestamp.class)))
        .thenReturn(2);

    job.cleanPublishedEvents();

    verify(jdbcTemplate).update(org.mockito.ArgumentMatchers.anyString(), any(Timestamp.class));
    assertThat(meterRegistry.find("outbox.events.cleaned").counter().count()).isEqualTo(2.0);
  }

  @Test
  void should_leaveCounterUnchanged_whenNothingIsDeleted() {
    when(jdbcTemplate.update(org.mockito.ArgumentMatchers.anyString(), any(Timestamp.class)))
        .thenReturn(0);

    job.cleanPublishedEvents();

    assertThat(meterRegistry.find("outbox.events.cleaned").counter().count()).isEqualTo(0.0);
  }
}
