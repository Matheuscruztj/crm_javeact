# ADR-005 — Event Backbone: Redis Streams + Transactional Outbox

**Status:** Accepted  
**Date:** 2025-01-20

---

## Context

Domain events must be delivered reliably from the API process to the Worker process without losing events on crashes or network failures.

## Decision

We use the **Transactional Outbox Pattern** with **Redis Streams** as the event backbone.

1. Events are written atomically to `outbox_events` table in the same DB transaction as the business operation
2. An outbox poller reads PENDING events and publishes them to Redis Streams
3. Worker consumes from Redis Streams consumer groups (at-least-once delivery)
4. Consumers are idempotent (check `event_id` before processing)
5. Failed messages after 5 retries go to Dead Letter Queue (DLQ)

## Consequences

**Positive:**

- No event loss even if API crashes after DB write
- Redis Streams provide durable, ordered delivery
- Consumer groups allow parallel processing
- DLQ enables manual inspection of poison messages

**Negative:**

- Requires Redis to be available (mitigated by circuit breaker)
- Outbox table grows; needs cleanup job (implemented: OutboxCleanupJob)
- At-least-once means consumers must handle duplicates (idempotent by event_id)
