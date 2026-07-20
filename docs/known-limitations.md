# AtlasOps AI — Known Limitations

> **Updated:** 2026-07-20 | **Validates:** P3.8.6 — Known limitations documented

---

## Security

| Limitation                                            | Risk   | Mitigation / Future Plan                                                                           |
| ----------------------------------------------------- | ------ | -------------------------------------------------------------------------------------------------- |
| OAuth2/SSO not implemented                            | Medium | Username+password only; planned for post-v1                                                        |
| Access tokens irrevocable until expiry (1h)           | Low    | Short TTL; rate limiting blocks brute force                                                        |
| PostgreSQL Row-Level Security (RLS) not configured    | Low    | App-layer tenant isolation via TenantAuthorizationFilter; RLS as defense-in-depth planned (P3.1.8) |
| DAST (dynamic application security testing) not run   | Medium | P3.1.4 — planned post-RC                                                                           |
| Prompt injection in document text not fully mitigated | Medium | Document text is data not prompt; full LLM safety guidelines in P3.1.7                             |

---

## AI / RAG Pipeline

| Limitation                                                     | Notes                                                                      |
| -------------------------------------------------------------- | -------------------------------------------------------------------------- |
| Quality depends on Ollama model selection                      | Default model may produce low-confidence results for specialized documents |
| No streaming responses                                         | Full analysis response only; no token streaming                            |
| pgvector cosine similarity tuning not done                     | Default threshold 0.7; may need adjustment per use case                    |
| Golden dataset evaluation uses word-overlap similarity         | Embedding-based evaluation recommended for production                      |
| Large documents (>100KB extracted text) chunked to 1000 tokens | Very long documents may lose context across chunks                         |

---

## Infrastructure

| Limitation                                                      | Notes                                                                               |
| --------------------------------------------------------------- | ----------------------------------------------------------------------------------- |
| OpenSearch disabled by default                                  | Feature flag `FEATURE_OPENSEARCH=true` needed; no Docker Compose service by default |
| Neo4j, TimescaleDB, ClickHouse, EventStoreDB adapters are stubs | Feature-flagged stubs; full implementation in P2 roadmap                            |
| Ollama managed externally                                       | Not in Docker Compose; must be running on host or remote URL                        |
| No backup automation                                            | Manual backup only via `make backup`; no scheduled backup job                       |
| No disaster recovery testing                                    | Restore procedure documented but not automated                                      |

---

## Performance

| Limitation                                                | Notes                                                                                      |
| --------------------------------------------------------- | ------------------------------------------------------------------------------------------ |
| File size limit in portal upload page                     | Default 10MB via existing page; multipart supports up to 500MB via UploadManager component |
| Worker throughput not measured under load                 | Estimated 2-5 documents/min per Ollama instance                                            |
| Redis Streams consumer lag not alerting below 1000 events | Prometheus alert not configured for stream lag                                             |

---

## Frontend

| Limitation                                                        | Notes                                                                     |
| ----------------------------------------------------------------- | ------------------------------------------------------------------------- |
| Storybook stories require `pnpm install` with new devDependencies | `@storybook/nextjs@8.4` needs separate install                            |
| next-intl not wired to layouts yet                                | i18n setup exists but not activated in root layout                        |
| No automated accessibility testing (axe)                          | Manual WCAG review needed; Storybook a11y addon provides partial coverage |

---

## Testing

| Limitation                                        | Notes                                                                                   |
| ------------------------------------------------- | --------------------------------------------------------------------------------------- |
| Integration tests require Docker (Testcontainers) | Cannot run in environments without Docker socket                                        |
| E2E tests require running frontend+backend        | Playwright tests skip if services unreachable                                           |
| Coverage < 75% on some modules                    | customers (~20%), requests (~12%), notifications (~21%) — improving with each iteration |
| Flaky test detection not automated                | Nightly CI runs suite 3x; fully manual if needed                                        |

---

## Compliance / Legal

| Limitation                                | Notes                                                                      |
| ----------------------------------------- | -------------------------------------------------------------------------- |
| GDPR/LGPD compliance not audited          | Legal hold feature implemented; DPA/data residency not formally reviewed   |
| No audit log retention policy enforcement | Audit entries retained indefinitely; cleanup not configured                |
| Accessibility: full WCAG AA not verified  | Automated partial coverage only; manual testing with screen readers needed |
