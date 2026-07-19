package com.atlasops.boot.infrastructure.outbox;

import com.atlasops.shared.domain.OutboxEvent;
import com.atlasops.shared.domain.ports.OutboxEventRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA/JDBC implementation of the OutboxEventRepository.
 * Uses JDBC for efficiency and participates in the existing transaction.
 */
@Repository
public class JpaOutboxEventRepository implements OutboxEventRepository {

  private final JdbcTemplate jdbcTemplate;

  public JpaOutboxEventRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void save(OutboxEvent event) {
    jdbcTemplate.update(
        """
        INSERT INTO outbox_events (id, event_type, event_id, tenant_id, correlation_id, payload, stream_name, status, created_at, retry_count)
        VALUES (?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?)
        ON CONFLICT (event_id) DO NOTHING
        """,
        event.getId(),
        event.getEventType(),
        event.getEventId(),
        event.getTenantId(),
        event.getCorrelationId(),
        event.getPayload(),
        event.getStreamName(),
        event.getStatus(),
        event.getCreatedAt(),
        event.getRetryCount());
  }

  @Override
  @Transactional(readOnly = true)
  public List<OutboxEvent> findPendingEvents(int limit) {
    return jdbcTemplate.query(
        """
        SELECT id, event_type, event_id, tenant_id, correlation_id, payload, stream_name, created_at
        FROM outbox_events
        WHERE status = 'PENDING'
        ORDER BY created_at ASC
        LIMIT ?
        FOR UPDATE SKIP LOCKED
        """,
        (rs, rowNum) ->
            new OutboxEvent(
                rs.getString("id"),
                rs.getString("event_type"),
                rs.getString("event_id"),
                rs.getString("tenant_id"),
                rs.getString("correlation_id"),
                rs.getString("payload"),
                rs.getString("stream_name"),
                rs.getTimestamp("created_at").toInstant()),
        limit);
  }

  @Override
  @Transactional
  public void markPublished(String eventId) {
    jdbcTemplate.update(
        """
        UPDATE outbox_events
        SET status = 'PUBLISHED', published_at = ?
        WHERE event_id = ? AND status = 'PENDING'
        """,
        Instant.now(),
        eventId);
  }

  @Override
  @Transactional
  public void markFailed(String eventId, String error) {
    jdbcTemplate.update(
        """
        UPDATE outbox_events
        SET retry_count = retry_count + 1,
            last_error = ?,
            status = CASE WHEN retry_count >= 4 THEN 'FAILED' ELSE 'PENDING' END
        WHERE event_id = ?
        """,
        error,
        eventId);
  }
}
