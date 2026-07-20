# ADR-004 — Local AI Inference: Ollama + pgvector (No Cloud AI)

**Status:** Accepted  
**Date:** 2025-01-20

---

## Context

AtlasOps AI analyzes documents containing sensitive business data. Using cloud AI (OpenAI, Anthropic) would send document contents to third-party servers, which raises data privacy and compliance concerns.

## Decision

We use **Ollama** for local LLM inference and **pgvector** for embedding storage and similarity search. All AI processing happens within the customer's own infrastructure.

## Consequences

**Positive:**

- No document content leaves the customer's network
- No per-request cloud AI costs
- Deterministic fallback when Ollama unavailable (confidence=0.0, fallback=true)
- Compliance-friendly (GDPR, LGPD, healthcare, financial sectors)

**Negative:**

- Quality may be lower than frontier models (GPT-4, Claude)
- Customer must provision GPU/CPU hardware for Ollama
- Model updates are manual

**Mitigations:**

- Golden dataset evaluation framework measures quality degradation
- Circuit breaker (Resilience4j) isolates Ollama failures
- Prompt versioning allows quality improvements without code changes
