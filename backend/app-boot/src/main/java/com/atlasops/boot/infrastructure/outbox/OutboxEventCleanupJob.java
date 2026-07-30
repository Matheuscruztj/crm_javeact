package com.atlasops.boot.infrastructure.outbox;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scheduled job that removes old PUBLISHED outbox events to prevent unbounded table growth.
 *
 * <p>Runs every hour by default. Only removes events with status = 'PUBLISHED'; PENDING and FAILED
 * events are preserved for debugging and retry purposes.
 *
 * <p>Validates: P0.H.3 — Outbox Event Cleanup
 */
@Component
public class OutboxEventCleanupJob {

  private static final Logger log = LoggerFactory.getLogger(OutboxEventCleanupJob.class);

  private final JdbcTemplate jdbcTemplate;
  private final Counter cleanedCounter;

  @Value("${app.outbox.retention-days:7}")
  private int retentionDays;

  public OutboxEventCleanupJob(JdbcTemplate jdbcTemplate, MeterRegistry meterRegistry) {
    this.jdbcTemplate = jdbcTemplate;
    this.cleanedCounter =
        Counter.builder("outbox.events.cleaned")
            .description("Number of outbox events removed by the cleanup job")
            .register(meterRegistry);
  }

  /**
   * Deletes PUBLISHED outbox events older than the configured retention period.
   * Runs every hour (3600000 ms).
   */
  @Scheduled(fixedDelayString = "${app.outbox.cleanup-interval-ms:3600000}")
  @Transactional
  public void cleanPublishedEvents() {
    Instant cutoff = Instant.now().minus(Duration.ofDays(retentionDays));

    int deleted =
        jdbcTemplate.update(
            """
            DELETE FROM outbox_events
            WHERE status = 'PUBLISHED'
              AND created_at < ?
            """,
            Timestamp.from(cutoff));

    if (deleted > 0) {
      cleanedCounter.increment(deleted);
      log.info(
          "Outbox cleanup: removed {} PUBLISHED events older than {} days (cutoff={})",
          deleted,
          retentionDays,
          cutoff);
    } else {
      log.debug("Outbox cleanup: no events to remove (retention={} days)", retentionDays);
    }
  }
}
