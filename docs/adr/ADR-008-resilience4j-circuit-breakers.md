# ADR-008 — Resilience4j Circuit Breakers for External Dependencies

**Status:** Accepted | **Date:** 2025-01-20

## Context

AtlasOps AI depends on Ollama (AI), MinIO (storage), and webhook endpoints. Any of these can be temporarily unavailable. Without circuit breakers, failures cascade: thread pools fill with blocked requests, latency spikes, and the entire API degrades.

## Decision

Use **Resilience4j** (Spring Boot 3 native) with COUNT_BASED sliding window circuit breakers:

| Circuit Breaker    | Threshold      | Wait Duration | Half-Open Calls |
| ------------------ | -------------- | ------------- | --------------- |
| `ollama`           | 50% / 10 calls | 30s           | 3               |
| `minio`            | 60% / 5 calls  | 20s           | 2               |
| `webhook-dispatch` | 70% / 10 calls | 15s           | —               |

**Fallback behavior:** Ollama → deterministic AI response (fallback=true, confidence=0.0)

## Consequences

**Positive:** Fast-fail prevents resource exhaustion; metrics exposed to Prometheus; health indicators in `/actuator/health`  
**Negative:** False positives possible during transient network issues (mitigated by half-open recovery)
