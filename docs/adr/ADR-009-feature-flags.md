# ADR-009 — Feature Flags for Specialized Data Adapters

**Status:** Accepted | **Date:** 2026-07-20

## Context

AtlasOps AI supports multiple specialized data stores (OpenSearch, Neo4j, TimescaleDB, ClickHouse, EventStoreDB). Not all deployments need all stores. We need a way to activate adapters without code changes.

## Decision

Use a two-tier **feature flag system** via `FeatureFlagPort`:

1. **Property-based (static):** `app.features.opensearch.enabled=false` in `application.yml`; overridable via env var (e.g., `FEATURE_OPENSEARCH=true`)
2. **Redis runtime toggle:** key `feature:{flagName}` with value `"true"` → flag enabled at runtime without restart

`PropertySourceFeatureFlagAdapter` checks Redis first, falls back to Spring `Environment`.

## Consequences

**Positive:** Zero-downtime feature activation; environment-specific config; A/B rollout possible  
**Negative:** Two sources of truth (Redis + properties) may cause confusion; requires Redis for runtime toggles
