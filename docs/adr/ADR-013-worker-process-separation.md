# ADR-013 — Separate Worker Process for Async Heavy Processing

**Status:** Accepted | **Date:** 2025-01-20

## Context

Document text extraction (Tika), AI analysis (Ollama, up to 2min), preview generation (PDFBox), and bulk import processing are CPU/memory intensive. Running them in the same JVM as the API risks:

- Thread pool exhaustion degrading API latency for all users
- Out-of-memory errors affecting the API
- Long-running background tasks blocking HTTP request threads

## Decision

Run a **separate Spring Boot Worker process** (`backend/worker`) consuming Redis Streams:

| Responsibility              | Justification                         |
| --------------------------- | ------------------------------------- |
| Text extraction (Tika)      | CPU-intensive, not latency-sensitive  |
| AI analysis (Ollama)        | Up to 120s per document               |
| Preview generation (PDFBox) | Memory-intensive                      |
| Bulk import (CSV)           | Variable duration, row-level progress |
| Email notifications         | Network I/O, not user-facing          |
| Webhook dispatch            | Exponential retry with backoff        |

The Worker shares the same PostgreSQL, Redis, and MinIO but runs in a separate JVM.

## Consequences

**Positive:** API latency unaffected by heavy processing; Worker can be scaled independently; crash isolation  
**Negative:** Two processes to deploy and monitor; event delivery adds latency (Redis Streams, typically <100ms)  
**Mitigation:** Testcontainers tests cover the full pipeline; Worker has its own health check and circuit breakers
