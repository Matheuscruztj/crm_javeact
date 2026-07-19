# ADR-002: Transactional Outbox for Domain Events

> **Status:** Accepted  
> **Date:** 2025-07-19  
> **Authors:** AtlasOps Team

---

## Context

Domain events in AtlasOps (e.g., `CustomerCreatedEvent`, `DocumentUploadedEvent`) need to be published to Redis Streams for downstream consumers (worker process, notifications, SSE). The initial implementation published events directly to Redis within the request flow, creating a dual-write problem:

1. If the database transaction commits but Redis publish fails → event is lost
2. If Redis publish succeeds but the database transaction rolls back → phantom event

This violates the at-least-once delivery guarantee required for reliable event processing.

## Decision

Implement the **Transactional Outbox Pattern**:

1. Domain events are serialized and stored in an `outbox_events` database table within the same transaction as the business operation
2. A scheduled dispatcher polls the outbox table every 500ms and publishes pending events to Redis Streams
3. Events are marked as `PUBLISHED` after successful delivery or `FAILED` after 5 retries
4. The `TransactionalEventPublisher` replaces the direct `RedisEventPublisher` as the `@Primary` implementation

Key design choices:

- `FOR UPDATE SKIP LOCKED` for concurrent dispatcher instances
- Deduplication via unique `event_id` index
- Retry with exponential backoff (up to 5 attempts)
- Batch processing (50 events per poll cycle)

## Consequences

### Positive

- Guarantees at-least-once delivery (no lost events)
- Transactional consistency: events are only published if the business operation commits
- Auditable: all events persist in the database with status tracking
- Supports multiple dispatcher instances without conflicts (SKIP LOCKED)

### Negative

- Adds 500ms latency (polling interval) to event delivery
- Requires additional database writes per domain event
- Table growth requires periodic cleanup of PUBLISHED events
- Consumers must handle duplicate events (at-least-once, not exactly-once)

### Neutral

- The old `RedisEventPublisher` remains as a fallback component
- Existing event consumers don't need changes (same Redis Streams)

## Alternatives Considered

### Alternative 1: Change Data Capture (CDC) with Debezium

More infrastructure complexity. Rejected for P0 as overkill — Debezium requires Kafka + connector setup.

### Alternative 2: Two-Phase Commit (2PC)

Distributed transactions across PostgreSQL and Redis. Rejected due to performance impact and Redis not supporting XA.

### Alternative 3: Event Sourcing

Store events as the source of truth and derive state. Rejected as too large a paradigm shift for P0 — planned for P3.

## References

- [Microservices Patterns - Chris Richardson (Transactional Outbox)](https://microservices.io/patterns/data/transactional-outbox.html)
- Migration: `V20250120_0008__create_outbox_events_table.sql`
