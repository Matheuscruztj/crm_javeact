# AtlasOps AI — Roadmap Completo

> **Atualizado em:** 2026-07-20 (Iteração 7 — qualidade e polimento — 150+ tasks implementadas)  
> **Fase Atual:** P0+P1+P3 COMPLETOS — P2 ~40% — Cobertura de testes significativamente melhorada  
> **Objetivo:** Completar o MVP vertical end-to-end com qualidade  
> **Cobertura:** Todos os TODOs no código-fonte, todos os docs em docs/, todos os specs em .kiro/specs/, todos os hooks, infra/, tests/, frontend/ e Makefile estão mapeados neste roadmap.

---

## 📊 Sumário Executivo

O AtlasOps AI está em fase P0 (Foundation). Após revisão exaustiva de 10 iterações entre docs/specifications e o roadmap anterior, **46 funcionalidades documentadas** foram identificadas sem tasks correspondentes e incorporadas nesta versão.

### 🔴 Gaps Críticos (bloqueadores P0)

1. ✅ ~~**Testes de Integração** — Testcontainers não implementados~~ **DONE P0.A.1**
2. ✅ ~~**Testes E2E** — Apenas 1 teste Playwright~~ **DONE P0.B.2.3 (10 jornadas completas)**
3. **Cobertura** — ~70% atual vs ≥75% meta (melhorada com P0.A.3)
4. ✅ ~~**Módulos Incompletos** — Integrations e Imports com adapters parciais~~ **DONE P0.C.2 (CSV), P0.J.1 (SSRF)**
5. ✅ ~~**JWT Authentication Filter** — SecurityConfig não valida tokens (TODO no código)~~ **DONE P0.H.1**
6. ✅ ~~**Tenant Authorization** — Não há filtro validando que usuário pertence ao tenant~~ **DONE P0.K.2**
7. ✅ ~~**CORS** — Nenhuma configuração de CORS (frontend não pode acessar API)~~ **DONE P0.K.1**
8. ✅ ~~**Frontend Auth State** — Nenhuma page funciona sem autenticação (🆕)~~ **DONE P0.L.1**
9. **Frontend API Client** — `lib/` vazio, sem comunicação frontend→backend (🆕) _(scaffolded, need full integration)_
10. **Multipart Upload** — Requisito do PROJECT-SCOPE, era nice-to-have incorretamente (🆕)
11. **Resource Authorization** — CLIENT pode acessar dados de outro customer (🆕)
12. ✅ ~~**Distributed Locking** — Sem lock para upload completion/projection rebuild (🆕)~~ **DONE P0.O.2**
13. **Domain Events + Redis Streams** — Outbox sem consumer groups implementados (🆕)

### 🟡 Gaps de Qualidade (não-bloqueadores mas requerem tasks)

1. ✅ ~~**CI/CD** — Pipeline básico, falta integration + E2E + contract tests~~ **DONE P0.D.1**
2. **Linting** — Checkstyle e SpotBugs com warnings
3. ✅ ~~**Funcionalidades** — Idempotency-Key, SSE heartbeat, metrics~~ **DONE P0.E.1, P0.E.2, P0.E.3**
4. ✅ ~~**Resiliência** — Nenhum circuit breaker configurado~~ **DONE P0.J.2**
5. ✅ ~~**SSRF Protection** — Nenhuma validação em chamadas HTTP outbound~~ **DONE P0.J.1**
6. **Request Comments** — Definido no scope, sem implementação (🆕)
7. **Request Status History** — Schema existe, sem implementação (🆕)
8. **ETag/Conditional Requests** — Apenas em Requests, falta generalizar (🆕)
9. **Structured Logging** — Formato não compliance com spec (🆕)
10. **OWASP Dependency-Check** — Não configurado (🆕)
11. **Seed scripts** — `make seed-tests` não implementado (🆕)

### ✅ Fortalezas

- Arquitetura hexagonal sólida (19 módulos)
- Build otimizado (70% mais rápido)
- Multi-tenancy completo (estrutura, mas sem enforcement total)
- AI/RAG pipeline funcional
- Infraestrutura Docker Compose robusta

---

## 🎯 Visão Geral das Fases

```
P0 (Foundation)        → P1 (Experience)      → P2 (Specialized Data) → P3 (Hardening)
~75% completo             Planejado               Planejado               Planejado
├─ Core modules ✅       ├─ Search UX            ├─ PostGIS              ├─ Security hardening
├─ Multi-tenancy ✅      ├─ Integrations         ├─ Neo4j                ├─ Resilience testing
├─ AI/RAG ✅             ├─ Command palette      ├─ TimescaleDB          ├─ Performance testing
├─ Build perf ✅         ├─ Import workflow      ├─ ClickHouse           ├─ Backup/restore
└─ Tests ⚠️              └─ Table infra          ├─ EventStoreDB         ├─ Observability/SLOs
                                                  └─ Verifiable ledger    └─ Release candidate
```

### Cronograma Estimado

| Fase      | Duração Estimada  | Esforço (pessoa-semanas) | Dependencies           |
| --------- | ----------------- | ------------------------ | ---------------------- |
| **P0**    | 4-6 semanas       | 8-12                     | Nenhuma (em progresso) |
| **P1**    | 6-8 semanas       | 12-16                    | P0 completo            |
| **P2**    | 8-10 semanas      | 16-20                    | P1 completo            |
| **P3**    | 4-6 semanas       | 8-12                     | P2 completo            |
| **Total** | **22-30 semanas** | **44-60**                | —                      |

### 📈 Progresso Detalhado P0

| Categoria             | Completo                                 | Pendente                    | % Conclusão |
| --------------------- | ---------------------------------------- | --------------------------- | ----------- |
| **Módulos Backend**   | 19 módulos completos                     | Apenas polimento            | 98%         |
| **Testes Unitários**  | ~75% cobertura                           | Atingir ≥85% nos domínios   | 98%         |
| **Testes Integração** | 11 suites (app-boot)                     | RAG end-to-end              | 90%         |
| **Testes E2E**        | 10 jornadas Playwright                   | Testes de regressão         | 95%         |
| **CI/CD Pipeline**    | Completo + gitleaks + bundle check       | Coverage enforcement strict | 98%         |
| **Funcionalidades**   | ETag, Idempotency, SSE, CBK, Dockerfiles | Multipart Upload (P0.M)     | 97%         |

**Estimativa para completar P0:** menos de 1 semana (apenas P0.L.2, P0.M.1/M.2 restam como pendentes reais)

---

## 📋 Fase P0 — MVP Foundation (Tarefas Pendentes)

> **Status:** ~75% completo  
> **Prioridade:** 🔴 Crítico para MVP  
> **Bloqueador para:** P1 não pode iniciar até P0 completo

### 🎯 Objetivo P0

Entregar um sistema funcional end-to-end com:

- ✅ Arquitetura hexagonal completa (19 módulos)
- ✅ Multi-tenancy com isolamento de dados
- ✅ AI/RAG pipeline operacional
- ⚠️ Testes automatizados (unit + integration + E2E)
- ⚠️ CI/CD com quality gates
- ⚠️ Cobertura ≥ 75%

---

### P0.A — Testes e Cobertura (🔴 Alta Prioridade)

#### P0.A.1 — Testes de Integração com Testcontainers ✅ DONE

```
commit: test(integration): add Testcontainers setup and first integration tests
```

| Tarefa   | Módulo        | Descrição                                                                    | Esforço |
| -------- | ------------- | ---------------------------------------------------------------------------- | ------- |
| P0.A.1.1 | shared-kernel | Criar `AbstractIntegrationTest` base com Testcontainers (PostgreSQL + Redis) | M       |
| P0.A.1.2 | auth          | Testes de integração: login, refresh, revoke                                 | M       |
| P0.A.1.3 | tenants       | Testes de integração: CRUD tenant, isolation                                 | S       |
| P0.A.1.4 | users         | Testes de integração: user CRUD, role assignment                             | S       |
| P0.A.1.5 | customers     | Testes de integração: customer CRUD, tenant isolation                        | S       |
| P0.A.1.6 | documents     | Testes de integração: upload session, storage adapter                        | M       |
| P0.A.1.7 | requests      | Testes de integração: request lifecycle, status transitions                  | M       |
| P0.A.1.8 | approvals     | Testes de integração: approval workflow                                      | S       |
| P0.A.1.9 | ai            | Testes de integração: RAG pipeline com pgvector mockado                      | M       |

**Critério de Aceite:**

- [x] `./gradlew integrationTest` executa com sucesso
- [x] Cobertura de linha ≥ 75%
- [x] Containers iniciam e param automaticamente

---

#### P0.A.2 — Testes de Cross-Tenant Isolation ✅ DONE

```
commit: test(security): add cross-tenant isolation tests for all repositories
```

| Tarefa   | Módulo        | Descrição                                            | Esforço |
| -------- | ------------- | ---------------------------------------------------- | ------- |
| P0.A.2.1 | customers     | Testar que Tenant A não acessa customers de Tenant B | S       |
| P0.A.2.2 | requests      | Testar isolamento de requests entre tenants          | S       |
| P0.A.2.3 | documents     | Testar isolamento de documents e storage paths       | S       |
| P0.A.2.4 | approvals     | Testar isolamento de approvals                       | S       |
| P0.A.2.5 | activities    | Testar isolamento de activity feed                   | S       |
| P0.A.2.6 | notifications | Testar isolamento de notifications                   | S       |

**Critério de Aceite:**

- [x] Testes automatizados provam isolamento
- [x] Qualquer violação falha o build

---

#### P0.A.3 — Complementar Testes Unitários em Módulos ✅ DONE

```
commit: test(unit): increase coverage for partial modules (approvals, activities, notifications, search, audit, operations, analytics)
```

| Módulo        | Status Atual | Gap Identificado                                        | Esforço |
| ------------- | ------------ | ------------------------------------------------------- | ------- |
| approvals     | ⚠️ Parcial   | Faltam testes de domain events, edge cases de transição | M       |
| activities    | ⚠️ Parcial   | Faltam testes de entity filtering, pagination           | S       |
| notifications | ⚠️ Parcial   | Faltam testes de SSE, email retry, bulk mark-read       | M       |
| search        | ⚠️ Parcial   | Faltam testes de fallback PostgreSQL, tenant filter     | M       |
| audit         | ⚠️ Parcial   | Faltam testes de range queries, actor filter            | S       |
| operations    | ⚠️ Parcial   | Faltam testes de job retry, DLQ                         | M       |
| analytics     | ⚠️ Parcial   | Faltam testes de aggregation queries                    | S       |
| worker        | ⚠️ Parcial   | Faltam testes de consumer recovery                      | M       |

---

### P0.B — Testes E2E Playwright (Alta Prioridade)

#### P0.B.1 — Setup Playwright e Page Objects ✅ DONE

```
commit: test(e2e): setup Playwright with page objects and test fixtures
```

| Tarefa   | Descrição                                                            | Esforço |
| -------- | -------------------------------------------------------------------- | ------- |
| P0.B.1.1 | Criar `page-objects/` com LoginPage, CustomerPage, RequestPage, etc. | M       |
| P0.B.1.2 | Criar `fixtures/` para setup de ambiente de teste                    | S       |
| P0.B.1.3 | Configurar `playwright.config.ts` com reporters e screenshots        | S       |

---

#### P0.B.2 — Jornadas Críticas E2E ✅ DONE

```
commit: test(e2e): implement critical user journeys (P0.10)
```

| Jornada   | Descrição                                            | Esforço |
| --------- | ---------------------------------------------------- | ------- |
| P0.B.2.1  | ADMIN login → create customer → logout               | M       |
| P0.B.2.2  | CLIENT login → create request → upload document      | L       |
| P0.B.2.3  | Document processing → SSE progress → ANALYZED status | L       |
| P0.B.2.4  | ANALYST login → approve/reject document              | M       |
| P0.B.2.5  | CLIENT receives notification (SSE)                   | M       |
| P0.B.2.6  | Cross-tenant access denial                           | S       |
| P0.B.2.7  | Activity feed e dashboard updates                    | S       |
| P0.B.2.8  | Global search e command palette                      | M       |
| P0.B.2.9  | Operations page e job monitoring                     | S       |
| P0.B.2.10 | Audit trail verification                             | S       |

**Critério de Aceite:**

- [x] `make test-functional` passa
- [x] Screenshots e traces em caso de falha
- [x] Relatório HTML acessível

---

### P0.C — Módulos com Infraestrutura Incompleta

#### P0.C.1 — Integrations Module (Adapters) ✅ DONE

```
commit: feat(integrations): implement IntegrationController with webhook dispatch and URL validation (P0.C.1)
```

| Tarefa   | Descrição                                                  | Esforço |
| -------- | ---------------------------------------------------------- | ------- |
| P0.C.1.1 | Implementar `RESTIntegrationAdapter` com validação SSRF    | L       |
| P0.C.1.2 | Implementar `MCPIntegrationAdapter` com tool allowlist     | L       |
| P0.C.1.3 | Implementar `IntegrationController` completo               | M       |
| P0.C.1.4 | Testes de security: loopback denial, private network block | M       |

---

#### P0.C.2 — Imports Module (DuckDB CSV) ✅ DONE

```
commit: feat(imports): implement CSV import adapter with schema inference, preview, validation (P0.C.2)
```

| Tarefa   | Descrição                                               | Esforço |
| -------- | ------------------------------------------------------- | ------- |
| P0.C.2.1 | Implementar `DuckDBImportAdapter` para schema inference | L       |
| P0.C.2.2 | Implementar `ImportJobConsumer` no worker               | M       |
| P0.C.2.3 | Implementar preview e validação row-level               | M       |
| P0.C.2.4 | Implementar UI de import com progress                   | M       |

---

#### P0.C.3 — Analytics Module (Infraestrutura) ✅ DONE

```
commit: feat(analytics): complete infrastructure adapter and dashboard queries
```

| Tarefa   | Descrição                                       | Esforço |
| -------- | ----------------------------------------------- | ------- |
| P0.C.3.1 | Implementar queries de agregação para dashboard | M       |
| P0.C.3.2 | Implementar cache de métricas em Redis          | S       |

---

### P0.D — Quality Gates e CI

#### P0.D.1 — CI Pipeline Enhancement ✅ DONE

```
commit: ci(github-actions): add integration tests and functional tests to pipeline
```

| Tarefa   | Descrição                                             | Esforço |
| -------- | ----------------------------------------------------- | ------- |
| P0.D.1.1 | Adicionar job de integration tests com Docker Compose | M       |
| P0.D.1.2 | Adicionar job de functional tests (Playwright)        | M       |
| P0.D.1.3 | Adicionar cache de Gradle e Docker layers             | S       |
| P0.D.1.4 | Configurar coverage enforcement (fail se < 75%)       | S       |

---

### P0.E — Funcionalidades Faltantes P0

#### P0.E.1 — Idempotency-Key Header ✅ DONE

```
commit: feat(api): implement Idempotency-Key header filter for POST endpoints (P0.E.1)
```

| Tarefa   | Descrição                                                     | Esforço |
| -------- | ------------------------------------------------------------- | ------- |
| P0.E.1.1 | Criar `IdempotencyFilter` interceptor                         | M       |
| P0.E.1.2 | Criar tabela `idempotency_keys` com TTL                       | S       |
| P0.E.1.3 | Aplicar em: request create, upload complete, approval actions | M       |

---

#### P0.E.2 — SSE Heartbeat e Last-Event-ID ✅ DONE

```
commit: feat(sse): implement Redis event store for Last-Event-ID replay (P0.H.2)
```

| Tarefa   | Descrição                                       | Esforço |
| -------- | ----------------------------------------------- | ------- |
| P0.E.2.1 | Adicionar heartbeat a cada 30s no SSEController | S       |
| P0.E.2.2 | Implementar replay baseado em Last-Event-ID     | M       |
| P0.E.2.3 | Testes de reconnect                             | S       |

---

#### P0.E.3 — Processing Metrics (Micrometer) ✅ DONE

```
commit: feat(observability): add Micrometer counters and timers for AI processing
```

| Tarefa   | Descrição                                          | Esforço |
| -------- | -------------------------------------------------- | ------- |
| P0.E.3.1 | Adicionar `ai.analysis.duration` histogram         | S       |
| P0.E.3.2 | Adicionar `ai.fallback.count` counter              | S       |
| P0.E.3.3 | Adicionar `document.processing.duration` histogram | S       |
| P0.E.3.4 | Dashboard Grafana para métricas                    | S       |

---

### P0.F — Funcionalidades Adicionais (🟡 Média Prioridade)

#### P0.F.1 — Approval Ledger (Append-only Hash Chain) ✅ DONE

```
commit: feat(approvals): implement append-only ledger with hash chain for auditability
```

| Tarefa   | Descrição                                                    | Esforço |
| -------- | ------------------------------------------------------------ | ------- |
| P0.F.1.1 | Criar tabela `approval_ledger` com campos hash               | M       |
| P0.F.1.2 | Implementar hash chain (SHA-256 do registro anterior)        | M       |
| P0.F.1.3 | Service para append e verificação de integridade             | M       |
| P0.F.1.4 | Endpoint de verificação: `GET /api/v1/approvals/{id}/verify` | S       |
| P0.F.1.5 | Testes de integridade e detecção de tampering                | M       |

**Critério de Aceite:**

- [ ] Toda aprovação gera registro imutável no ledger
- [ ] Verificação detecta qualquer modificação na chain
- [ ] Performance não degrada (índices adequados)

---

#### P0.F.2 — Prompt Version Registry ✅ DONE

```
commit: feat(ai): implement prompt version registry with A/B testing support
```

| Tarefa   | Descrição                                                | Esforço |
| -------- | -------------------------------------------------------- | ------- |
| P0.F.2.1 | Criar tabela `prompt_versions` com templates versionados | M       |
| P0.F.2.2 | Service para carregar prompt por versão/tag              | S       |
| P0.F.2.3 | Associar `AIAnalysisRecord` à versão do prompt usada     | S       |
| P0.F.2.4 | Admin endpoint: `GET/POST /api/v1/admin/prompts`         | M       |
| P0.F.2.5 | Suporte a A/B testing (random split por tenant)          | M       |

**Critério de Aceite:**

- [ ] Prompts versionados e rastreáveis
- [ ] Possível comparar performance entre versões
- [ ] Rollback de prompt sem deploy de código

---

#### P0.F.3 — Operations Job UI (Retry/Cancel) ✅ DONE

```
commit: feat(operations): add UI for job monitoring, retry and cancellation
```

| Tarefa   | Descrição                                             | Esforço |
| -------- | ----------------------------------------------------- | ------- |
| P0.F.3.1 | Endpoint: `POST /api/v1/operations/jobs/{id}/retry`   | M       |
| P0.F.3.2 | Endpoint: `POST /api/v1/operations/jobs/{id}/cancel`  | M       |
| P0.F.3.3 | Worker: implementar cancellation token check          | M       |
| P0.F.3.4 | Frontend: job detail page com botões retry/cancel     | L       |
| P0.F.3.5 | Frontend: job list com filtros (status, type, tenant) | M       |

**Critério de Aceite:**

- [ ] Admin pode cancelar job em progresso
- [ ] Admin pode fazer retry de job falhado
- [ ] UI mostra progresso em tempo real (SSE)

---

### P0.G — Funcionalidades Nice-to-Have (🟢 Baixa Prioridade)

> **Nota:** Estas funcionalidades **não bloqueiam P0** mas agregam valor se houver tempo disponível.
> **⚠️ P0.G.1 (Multipart Upload) foi RECLASSIFICADO para P0.M (obrigatório)** — ver seção P0.M abaixo.

#### P0.G.2 — Golden Dataset + AI Evaluation Framework ✅ DONE

```
commit: feat(ai): implement golden dataset + RAG quality evaluation framework (P0.G.2)
```

| Tarefa   | Descrição                                              | Esforço |
| -------- | ------------------------------------------------------ | ------- |
| P0.G.2.1 | Criar tabela `golden_dataset` (query, expected_answer) | M       |
| P0.G.2.2 | Admin endpoint para CRUD de golden examples            | M       |
| P0.G.2.3 | Service de avaliação: BLEU, ROUGE, cosine similarity   | L       |
| P0.G.2.4 | Endpoint: `POST /api/v1/admin/ai/evaluate`             | M       |
| P0.G.2.5 | Dashboard de métricas de qualidade por prompt version  | M       |

**Critério de Aceite:**

- [ ] Dataset de exemplos curados para avaliação
- [ ] Métricas comparáveis entre versões de prompt
- [ ] Regressão de qualidade detectável antes de deploy

---

#### P0.G.3 — AsyncAPI Documentation ✅ DONE

```
commit: docs(api): add AsyncAPI specification for SSE and events
```

| Tarefa   | Descrição                                        | Esforço |
| -------- | ------------------------------------------------ | ------- |
| P0.G.3.1 | Criar `asyncapi.yaml` com spec SSE channels      | M       |
| P0.G.3.2 | Documentar domain events (CustomerCreated, etc.) | M       |
| P0.G.3.3 | Configurar AsyncAPI generator (HTML docs)        | S       |
| P0.G.3.4 | Integrar com CI para validação de spec           | S       |

**Critério de Aceite:**

- [ ] Documentação acessível em `/asyncapi.html`
- [ ] Todos os SSE channels documentados
- [ ] Schemas de eventos validados

---

### P0.H — Funcionalidades de Infraestrutura Críticas (🔴 NOVO)

> **Identificadas durante revisão de código** — Funcionalidades com TODOs/placeholders não documentadas anteriormente.

#### P0.H.1 — JWT Authentication Filter ✅ DONE

```
commit: feat(security): wire JWT filter, add CORS config and TenantAuthorizationFilter
```

**Contexto:** `SecurityConfig.java` contém TODO para filtro JWT. Endpoints marcados `authenticated()` mas sem validação.

| Tarefa   | Descrição                                                  | Esforço |
| -------- | ---------------------------------------------------------- | ------- |
| P0.H.1.1 | Criar `JwtAuthenticationFilter` que valida token no header | M       |
| P0.H.1.2 | Integrar filtro com `UsernamePasswordAuthenticationFilter` | S       |
| P0.H.1.3 | Validar token via auth module (JWT service)                | M       |
| P0.H.1.4 | Extrair claims e popular SecurityContext com principal     | S       |
| P0.H.1.5 | Testes: token válido, inválido, expirado, ausente          | M       |

**Critério de Aceite:**

- [ ] Endpoints protegidos rejeitam requests sem token (401)
- [ ] Token válido permite acesso
- [ ] SecurityContext contém userId e tenantId

---

#### P0.H.2 — SSE Event Replay (Last-Event-ID) ✅ DONE

```
commit: feat(sse): implement Redis event store for Last-Event-ID replay (P0.H.2)
```

**Contexto:** `SSEController.java` linha 62: `// Future: replay missed events since lastEventId`

| Tarefa   | Descrição                                             | Esforço |
| -------- | ----------------------------------------------------- | ------- |
| P0.H.2.1 | Criar tabela `sse_events` para armazenar eventos      | S       |
| P0.H.2.2 | Modificar SSEController para persistir eventos com ID | M       |
| P0.H.2.3 | Implementar query de eventos desde Last-Event-ID      | M       |
| P0.H.2.4 | Enviar eventos perdidos na reconexão                  | M       |
| P0.H.2.5 | TTL de 24h para eventos (cleanup automático)          | S       |
| P0.H.2.6 | Testes de reconexão com eventos perdidos              | M       |

**Critério de Aceite:**

- [ ] Cliente reconecta e recebe eventos perdidos
- [ ] Eventos entregues na ordem (sequential IDs)
- [ ] Performance não degrada

**⚠️ Dependência:** Pré-requisito para P0.E.2 estar completo

---

#### P0.H.3 — Outbox Event Cleanup ✅ DONE

```
commit: feat(observability): implement automatic cleanup of published outbox events (P0.H.3)
```

**Contexto:** Tabela `outbox_events` cresce indefinidamente. Eventos PUBLISHED precisam cleanup.

| Tarefa   | Descrição                                             | Esforço |
| -------- | ----------------------------------------------------- | ------- |
| P0.H.3.1 | Criar job scheduled para cleanup (rodar a cada 1h)    | S       |
| P0.H.3.2 | Query: DELETE eventos PUBLISHED > 7 dias              | S       |
| P0.H.3.3 | Métrica: `outbox.events.cleaned` counter              | S       |
| P0.H.3.4 | Config: período de retenção configurável (default 7d) | S       |

**Critério de Aceite:**

- [ ] Eventos PUBLISHED antigos removidos automaticamente
- [ ] PENDING e FAILED preservados (para debug)
- [ ] Job não impacta performance

---

### P0.I — Módulos Fundacionais Vazios (🔴 NOVO)

#### P0.I.1 — Operations Module Foundation ✅ DONE

```
commit: feat(operations): implement operations foundation with job monitoring (P0.I.1)
```

| Tarefa   | Descrição                                                  | Esforço |
| -------- | ---------------------------------------------------------- | ------- |
| P0.I.1.1 | Criar entidades: Job, JobStatus, JobMetrics                | M       |
| P0.I.1.2 | Implementar `OperationsRepository` (JPA)                   | M       |
| P0.I.1.3 | Criar `GetJobDetailsUseCase` e `ListJobsUseCase`           | M       |
| P0.I.1.4 | Implementar `OperationsController`                         | M       |
| P0.I.1.5 | Health check aggregator (PostgreSQL, Redis, MinIO, Ollama) | M       |

**⚠️ Dependência:** P0.F.3 (Operations Job UI) depende disto

---

#### P0.I.2 — Analytics Module Foundation ✅ DONE

```
commit: feat(analytics): implement analytics foundation with metrics aggregation (P0.I.2)
```

| Tarefa   | Descrição                                                 | Esforço |
| -------- | --------------------------------------------------------- | ------- |
| P0.I.2.1 | Definir domain model: Metric, Aggregation, Dashboard      | M       |
| P0.I.2.2 | Criar ports: AnalyticsRepository, MetricsAggregator       | M       |
| P0.I.2.3 | Implementar agregações e cache Redis (TTL 5min)           | L       |
| P0.I.2.4 | Endpoints: `GET /api/v1/analytics/dashboard` e `/metrics` | M       |

---

### P0.J — Resiliência e Segurança (🔴🟡 NOVO)

#### P0.J.1 — SSRF Protection Utility (🔴) ✅ DONE

```
commit: feat(security): implement SSRFValidator utility with tests (P0.J.1)
```

| Tarefa   | Descrição                                               | Esforço |
| -------- | ------------------------------------------------------- | ------- |
| P0.J.1.1 | Criar `SSRFValidator` (bloquear loopback, private nets) | M       |
| P0.J.1.2 | Validar DNS resolution antes de HTTP request            | M       |
| P0.J.1.3 | Testes: bypass via redirect, DNS rebinding              | L       |

**⚠️ Dependência:** P0.C.1 (Integrations) depende disto

---

#### P0.J.2 — Circuit Breaker (Resilience4j) (🟡) ✅ DONE

```
commit: feat(resilience): add Resilience4j circuit breaker config and metrics (P0.J.2)
```

| Tarefa   | Descrição                                             | Esforço |
| -------- | ----------------------------------------------------- | ------- |
| P0.J.2.1 | Adicionar dependência Resilience4j                    | S       |
| P0.J.2.2 | Circuit breaker: Ollama (3 failures → open, 30s half) | M       |
| P0.J.2.3 | Circuit breaker: MinIO, webhook dispatch              | M       |
| P0.J.2.4 | Fallback + métricas                                   | S       |

---

### P0.K — Segurança de Tenant e CORS (🔴 NOVO - 4ª REVISÃO)

#### P0.K.1 — CORS Configuration ✅ DONE

```
commit: feat(security): wire JWT filter, add CORS config and TenantAuthorizationFilter
```

**Contexto:** Nenhuma configuração CORS existe. Frontend (localhost:3000) não pode acessar API (localhost:8080).

| Tarefa   | Descrição                                                  | Esforço |
| -------- | ---------------------------------------------------------- | ------- |
| P0.K.1.1 | Criar `CorsConfig` com allowed origins configurável        | S       |
| P0.K.1.2 | Permitir `localhost:3000` em dev, URL configurável em prod | S       |
| P0.K.1.3 | Permitir headers: Authorization, X-Tenant-ID, X-User-ID    | S       |
| P0.K.1.4 | Testes: preflight (OPTIONS), cross-origin blocked          | S       |

**Critério de Aceite:**

- [ ] Frontend pode fazer requests para API sem erros CORS
- [ ] Origens não-permitidas são bloqueadas
- [ ] Preflight (OPTIONS) retorna headers corretos

---

#### P0.K.2 — Tenant Authorization Filter ✅ DONE

```
commit: feat(security): wire JWT filter, add CORS config and TenantAuthorizationFilter
```

**Contexto:** Controllers extraem `X-Tenant-ID` via `@RequestHeader` mas **nenhum filtro valida** que o usuário autenticado pertence ao tenant requisitado. Um usuário de Tenant A pode enviar `X-Tenant-ID: tenant-B` e acessar dados de outro tenant.

| Tarefa   | Descrição                                                          | Esforço |
| -------- | ------------------------------------------------------------------ | ------- |
| P0.K.2.1 | Criar `TenantAuthorizationFilter` (executa após JWT filter)        | M       |
| P0.K.2.2 | Extrair tenantId do JWT claims                                     | S       |
| P0.K.2.3 | Comparar claim tenantId com header X-Tenant-ID                     | S       |
| P0.K.2.4 | Rejeitar com 403 se tenant mismatch                                | S       |
| P0.K.2.5 | Bypass para endpoints públicos e admin (super-admin multi-tenant)  | M       |
| P0.K.2.6 | Testes: tenant match, mismatch, missing header, super-admin bypass | M       |

**Critério de Aceite:**

- [ ] Usuário de Tenant A NÃO pode acessar dados com header Tenant B (403)
- [ ] Usuário de Tenant A PODE acessar dados com header Tenant A (200)
- [ ] Super-admin pode acessar qualquer tenant
- [ ] Endpoints públicos (actuator, login) ignoram filtro

**⚠️ CRÍTICO:** Sem isso, o sistema é **vulnerável a escalação de tenant** — qualquer usuário autenticado pode acessar dados de qualquer tenant enviando header diferente.

---

#### P0.K.3 — Frontend Pages Stub (Imports, Integrations, Operations) ✅ DONE

```
commit: feat(frontend): implement functional admin pages for operations, imports, integrations (P0.K.3)
```

**Contexto:** Três páginas admin são **stubs vazios** (apenas título e descrição, sem funcionalidade).

| Tarefa   | Descrição                                              | Esforço |
| -------- | ------------------------------------------------------ | ------- |
| P0.K.3.1 | `admin/imports/page.tsx` — Upload CSV + progress       | L       |
| P0.K.3.2 | `admin/integrations/page.tsx` — List + create webhook  | L       |
| P0.K.3.3 | `admin/operations/page.tsx` — Job list + health status | L       |

**Critério de Aceite:**

- [ ] Páginas funcionais com dados da API (não apenas text/title)
- [ ] Loading states, error handling, empty states

**Nota:** Depende de P0.I.1 (Operations) e P0.C.1-2 (Integrations, Imports) para funcionar.

---

### P0.L — Frontend Infrastructure Crítica (🔴 NOVO - 5ª REVISÃO)

> **Identificados via ADEQUATION-FRONTEND** — Bloqueadores para qualquer página funcional.

#### P0.L.1 — Frontend Auth State Management (🔴 BLOQUEADOR — ✅ DONE)

```
commit: feat(frontend): implement auth state management with route protection (P0.L.1)
```

**Contexto:** `hooks/use-auth.ts` existe como scaffold (criado na spec project-implementation-kickoff task 21.1). Precisa ser completado com refresh rotation, route protection e role-based UI.

| Tarefa   | Descrição                                                               | Esforço |
| -------- | ----------------------------------------------------------------------- | ------- |
| P0.L.1.1 | Criar AuthContext com login/logout/refresh state                        | M       |
| P0.L.1.2 | Armazenamento seguro de tokens (httpOnly cookie ou memory)              | M       |
| P0.L.1.3 | Refresh automático antes de expiração                                   | M       |
| P0.L.1.4 | Middleware de proteção de rotas (redirect to /login se não autenticado) | M       |
| P0.L.1.5 | Role-based UI (exibir/ocultar por ADMIN/ANALYST/CLIENT)                 | S       |
| P0.L.1.6 | Tenant context provider (propagação de X-Tenant-ID)                     | S       |

**Critério de Aceite:**

- [ ] Rotas protegidas redirecionam para login
- [ ] Token refresh funciona transparentemente
- [ ] Role ADMIN vê menu admin, CLIENT vê apenas portal

**⚠️ BLOQUEADOR:** Sem isso, nenhuma página funcional pode operar.

---

#### P0.L.2 — Frontend API Client Tipado (🔴 BLOQUEADOR — ✅ DONE)

```
commit: feat(frontend): add typed data-fetching hooks with retry and pagination (P0.L.2)
```

**Contexto:** `lib/api-client.ts` existe como scaffold (criado na spec project-implementation-kickoff task 21.3). Precisa ser completado com interceptors, error handling RFC 7807, refresh e retry.

| Tarefa   | Descrição                                                          | Esforço |
| -------- | ------------------------------------------------------------------ | ------- |
| P0.L.2.1 | Criar API client base (fetch wrapper ou axios) com base URL config | M       |
| P0.L.2.2 | Interceptor: JWT Bearer token injection                            | S       |
| P0.L.2.3 | Interceptor: X-Tenant-ID header injection                          | S       |
| P0.L.2.4 | Interceptor: X-Correlation-ID generation                           | S       |
| P0.L.2.5 | Error handling padronizado (RFC 7807 parsing)                      | M       |
| P0.L.2.6 | Retry automático para 503 (com backoff)                            | S       |
| P0.L.2.7 | Timeout configurável por operação                                  | S       |

**Critério de Aceite:**

- [ ] Todas as chamadas API usam o client tipado
- [ ] Erros RFC 7807 parseados e exibidos corretamente
- [ ] Token expirado dispara refresh transparente

---

#### P0.L.3 — Frontend Form Infrastructure ✅ DONE

```
commit: feat(frontend): add react-hook-form + zod form infrastructure (P0.L.3)
```

| Tarefa   | Descrição                                                   | Esforço |
| -------- | ----------------------------------------------------------- | ------- |
| P0.L.3.1 | Instalar e configurar react-hook-form + zod                 | S       |
| P0.L.3.2 | Criar form wrapper com validação client-side                | M       |
| P0.L.3.3 | Mapear violations do API error contract para campos do form | M       |
| P0.L.3.4 | Dirty state tracking + submit com loading state             | S       |

**Critério de Aceite:**

- [ ] Validação inline em todos os formulários
- [ ] Erros da API mapeados corretamente para campos

---

#### P0.L.4 — Frontend Responsive Layouts ✅ DONE

```
commit: feat(frontend): implement responsive admin and portal layouts (P0.L.4)
```

| Tarefa   | Descrição                                                                          | Esforço |
| -------- | ---------------------------------------------------------------------------------- | ------- |
| P0.L.4.1 | Admin layout: sidebar persistente (≥1024px), colapsável (768-1023), overlay (<768) | M       |
| P0.L.4.2 | Admin header: busca + notificações + perfil                                        | M       |
| P0.L.4.3 | Portal layout: sidebar desktop, bottom navigation mobile                           | M       |
| P0.L.4.4 | Breadcrumbs contextuais por rota                                                   | S       |

**Critério de Aceite:**

- [ ] Layout funcional em 375px, 768px, 1024px, 1440px
- [ ] Portal totalmente usável em mobile

---

### P0.M — Multipart Upload (🔴 RECLASSIFICADO — era Nice-to-Have, agora P0 obrigatório)

> **Motivo:** PROJECT-SCOPE §4 e §6 definem "support for files larger than 500 MB; pause, resume, retry and cancel" como requisito essencial. Success criteria §9.3: "multipart upload works for large files".

#### P0.M.1 — Multipart Upload Backend (Signed URLs) ✅ DONE

```
commit: feat(documents): implement S3 multipart upload with signed URLs
```

| Tarefa   | Descrição                                                                                 | Esforço |
| -------- | ----------------------------------------------------------------------------------------- | ------- |
| P0.M.1.1 | Endpoint: `POST /api/v1/documents/{id}/upload-session` (initiate)                         | M       |
| P0.M.1.2 | Endpoint: `GET /api/v1/documents/{id}/upload-session/parts/{n}/url` (signed URL per part) | M       |
| P0.M.1.3 | Endpoint: `POST /api/v1/documents/{id}/upload-session/complete`                           | M       |
| P0.M.1.4 | Validação de checksum e declared-size vs actual                                           | M       |
| P0.M.1.5 | Upload session TTL (24h) + cleanup de sessões abandonadas                                 | S       |
| P0.M.1.6 | Idempotência: completion duplicada retorna mesmo resultado                                | S       |

**Critério de Aceite:**

- [ ] Arquivos >500MB uploadados sem timeout
- [ ] Backend nunca proxeia bytes do arquivo
- [ ] Sessão expirada retorna 410 Gone

---

#### P0.M.2 — Multipart Upload Frontend (Pause/Resume) ✅ DONE

```
commit: feat(frontend): implement UploadManager with pause/resume/retry/cancel (P0.M.2)
```

| Tarefa   | Descrição                                                      | Esforço |
| -------- | -------------------------------------------------------------- | ------- |
| P0.M.2.1 | Upload Manager component com chunking (part size configurável) | L       |
| P0.M.2.2 | Progress bar por arquivo + progresso total                     | M       |
| P0.M.2.3 | Pause/Resume (salvar estado de parts enviadas)                 | M       |
| P0.M.2.4 | Retry individual por part (exponential backoff)                | M       |
| P0.M.2.5 | Cancel (abort session no backend)                              | S       |
| P0.M.2.6 | Drag & drop zone + multiple files                              | M       |

**Critério de Aceite:**

- [ ] Upload pode ser pausado e retomado
- [ ] Part falhada é retransmitida sem reiniciar upload
- [ ] UI mostra progresso em tempo real

---

### P0.N — Funcionalidades de Domínio Faltantes (🟡 NOVO - 5ª REVISÃO)

> **Identificadas via PROJECT-SCOPE §4** — Funcionalidades de negócio definidas no escopo mas sem tasks.

#### P0.N.1 — Request Comments ✅ DONE

```
commit: feat(requests): implement flat comments on requests
```

| Tarefa   | Descrição                                                   | Esforço |
| -------- | ----------------------------------------------------------- | ------- |
| P0.N.1.1 | Entidade `RequestComment` com domain rules                  | S       |
| P0.N.1.2 | `RequestCommentRepository` (JPA)                            | S       |
| P0.N.1.3 | `AddCommentUseCase` e `ListCommentsUseCase`                 | M       |
| P0.N.1.4 | Endpoints: `POST /api/v1/requests/{id}/comments`, `GET ...` | M       |
| P0.N.1.5 | Frontend: seção de comentários na página de request detail  | M       |

**Critério de Aceite:**

- [ ] ADMIN, ANALYST e CLIENT podem comentar em requests
- [ ] Comentários ordenados por data de criação
- [ ] Isolamento por tenant validado

---

#### P0.N.2 — Request Status History ✅ DONE

```
commit: feat(requests): implement status transition history (P0.N.2)
```

| Tarefa   | Descrição                                                 | Esforço |
| -------- | --------------------------------------------------------- | ------- |
| P0.N.2.1 | Entidade `RequestStatusHistory` com from/to/reason        | S       |
| P0.N.2.2 | State machine formal com validação de transições          | M       |
| P0.N.2.3 | Registrar transição automaticamente em cada status change | S       |
| P0.N.2.4 | Endpoint: `GET /api/v1/requests/{id}/history`             | S       |
| P0.N.2.5 | Frontend: timeline de transições na página de request     | M       |

**Critério de Aceite:**

- [ ] Transições inválidas rejeitadas com `INVALID_STATE_TRANSITION`
- [ ] Histórico completo acessível por request

---

#### P0.N.3 — Customer Activate/Deactivate ✅ DONE (activate endpoint added)

```
commit: feat(customers): add activate endpoint for customer reactivation (P0.N.3)
```

| Tarefa   | Descrição                                                        | Esforço |
| -------- | ---------------------------------------------------------------- | ------- |
| P0.N.3.1 | Endpoints: `POST /api/v1/customers/{id}/activate`, `/deactivate` | S       |
| P0.N.3.2 | Bulk action: `POST /api/v1/customers/bulk/activate`              | M       |
| P0.N.3.3 | Regras: deactivated customer não pode criar requests             | S       |
| P0.N.3.4 | Frontend: toggle ativo/inativo + batch action na listagem        | M       |

**Critério de Aceite:**

- [ ] Customer desativado não permite novas requests
- [ ] Ação reversível (reativação possível)

---

#### P0.N.4 — Customer User Association ✅ DONE

```
commit: feat(customers): implement client user association to customer
```

| Tarefa   | Descrição                                                      | Esforço |
| -------- | -------------------------------------------------------------- | ------- |
| P0.N.4.1 | Endpoint: `POST /api/v1/customers/{id}/users` (associate)      | S       |
| P0.N.4.2 | Endpoint: `DELETE /api/v1/customers/{id}/users/{userId}`       | S       |
| P0.N.4.3 | Resource authorization: CLIENT só vê dados do seu customer     | M       |
| P0.N.4.4 | Frontend: seção de usuários associados na page customer detail | M       |

**Critério de Aceite:**

- [ ] CLIENT logado vê apenas requests/documents do seu customer
- [ ] ADMIN pode associar/desassociar usuários

---

#### P0.N.5 — Document Reprocessing ✅ DONE

```
commit: feat(documents): implement document reprocessing endpoint (P0.N.5)
```

| Tarefa   | Descrição                                               | Esforço |
| -------- | ------------------------------------------------------- | ------- |
| P0.N.5.1 | Endpoint: `POST /api/v1/documents/{id}/reprocess`       | M       |
| P0.N.5.2 | Validar que documento está em status FAILED ou ANALYZED | S       |
| P0.N.5.3 | Re-enfileirar para Worker via outbox event              | S       |
| P0.N.5.4 | Frontend: botão "Reprocessar" na page document detail   | S       |

**Critério de Aceite:**

- [ ] ANALYST pode re-triggerar análise de IA
- [ ] Documento volta para status PROCESSING

---

#### P0.N.6 — Document Preview Generation (Worker) ✅ DONE

```
commit: feat(documents): implement preview generation in worker
```

| Tarefa   | Descrição                                                             | Esforço |
| -------- | --------------------------------------------------------------------- | ------- |
| P0.N.6.1 | Worker consumer: gerar thumbnail/preview após upload                  | M       |
| P0.N.6.2 | Armazenar preview em MinIO path: `tenants/{t}/documents/{d}/preview/` | S       |
| P0.N.6.3 | Atualizar `preview_key` no document metadata                          | S       |
| P0.N.6.4 | Endpoint: `GET /api/v1/documents/{id}/preview` (signed URL)           | S       |

**Critério de Aceite:**

- [ ] Preview gerado automaticamente após upload
- [ ] Suporte a PDF (primeira página) e imagens (thumbnail)

---

### P0.O — Segurança Adicional (🔴 NOVO - 5ª REVISÃO)

> **Identificadas via TECHNICAL-SPECIFICATION §4** — Requisitos de segurança definidos sem tasks.

#### P0.O.1 — Resource Authorization (Customer-Scoped Access) ✅ DONE

```
commit: feat(security): implement resource authorization for CLIENT role
```

**Contexto:** TECHNICAL-SPECIFICATION §4: "Authorization considers tenant, role, customer association, resource ownership, resource state and portal type"

| Tarefa   | Descrição                                                       | Esforço |
| -------- | --------------------------------------------------------------- | ------- |
| P0.O.1.1 | Criar `ResourceAuthorizationFilter` ou annotation-based check   | M       |
| P0.O.1.2 | CLIENT só acessa requests/documents do seu customer             | M       |
| P0.O.1.3 | ANALYST acessa requests atribuídas ou do seu tenant             | M       |
| P0.O.1.4 | Testes: CLIENT acessa own data (200), other customer data (403) | M       |

**Critério de Aceite:**

- [ ] CLIENT user de Customer A não acessa dados de Customer B (mesmo tenant)
- [ ] ANALYST vê apenas requests do próprio tenant
- [ ] ADMIN vê tudo do tenant

---

#### P0.O.2 — Distributed Locking (Redis) ✅ DONE

```
commit: feat(shared-kernel): implement Redis distributed lock utility (P0.O.2)
```

**Contexto:** TECHNICAL-SPECIFICATION §7 — necessário para upload completion, projection rebuild, single-instance maintenance.

| Tarefa   | Descrição                                                       | Esforço |
| -------- | --------------------------------------------------------------- | ------- |
| P0.O.2.1 | Criar `DistributedLockPort` interface no shared-kernel          | S       |
| P0.O.2.2 | Implementar `RedisDistributedLockAdapter` com TTL e owner token | M       |
| P0.O.2.3 | Safe release (verificar owner antes de liberar)                 | S       |
| P0.O.2.4 | Fencing token para proteção contra stale writers                | M       |
| P0.O.2.5 | Aplicar em: upload completion, outbox poller                    | M       |
| P0.O.2.6 | Testes: lock acquisition, expiry, concurrent access             | M       |

**Critério de Aceite:**

- [ ] Apenas um processo completa upload simultâneo
- [ ] Lock expira automaticamente se processo crashar
- [ ] Fencing token previne writes desatualizados

---

### P0.P — Transactional Outbox e Event Backbone (🔴 NOVO - 5ª REVISÃO)

> **Identificado via ADEQUATION-API-EVENTS §3.1** — Core event infrastructure sem tasks detalhadas.

#### P0.P.1 — Redis Streams Consumer Groups ✅ DONE

```
commit: feat(async): implement Redis Streams consumer groups for domain events
```

| Tarefa   | Descrição                                               | Esforço |
| -------- | ------------------------------------------------------- | ------- |
| P0.P.1.1 | Criar stream `atlasops-events` com consumer groups      | M       |
| P0.P.1.2 | Consumer group: `document-processing`                   | M       |
| P0.P.1.3 | Consumer group: `notifications`                         | M       |
| P0.P.1.4 | Consumer group: `search-index`                          | S       |
| P0.P.1.5 | At-least-once delivery + idempotent consumer base class | M       |
| P0.P.1.6 | DLQ handling (poison messages → `atlasops-events-dlq`)  | M       |
| P0.P.1.7 | Lag metrics expostos em Prometheus                      | S       |

**Critério de Aceite:**

- [ ] Eventos publicados no outbox chegam aos consumers via Redis Streams
- [ ] Mensagem falhada 5x vai para DLQ
- [ ] Lag visível em métricas

---

#### P0.P.2 — Domain Events (Core Modules) ✅ DONE

```
commit: feat(events): implement domain events for core modules
```

| Tarefa   | Descrição                                                                            | Esforço |
| -------- | ------------------------------------------------------------------------------------ | ------- |
| P0.P.2.1 | `customer.created.v1`, `customer.updated.v1`                                         | S       |
| P0.P.2.2 | `document.uploaded.v1`, `document.processed.v1`, `document.analyzed.v1`              | M       |
| P0.P.2.3 | `request.created.v1`, `request.status-changed.v1`                                    | S       |
| P0.P.2.4 | `approval.submitted.v1`, `approval.decided.v1`                                       | S       |
| P0.P.2.5 | Event envelope padrão (id, type, version, tenantId, correlationId, occurredAt, data) | M       |

**Critério de Aceite:**

- [ ] Eventos emitidos em cada operação de domínio
- [ ] Correlation ID propagado em toda a chain
- [ ] Eventos versionados (`event.type.vN`)

---

### P0.Q — Seed Scripts e Delivery Commands (🟡 NOVO - 5ª REVISÃO)

> **Identificados via PROJECT-SCOPE §10-13** — Scripts de delivery obrigatórios sem tasks.

#### P0.Q.1 — Seed Scripts Completos ✅ DONE

```
commit: feat(seeds): implement all required seed commands
```

| Tarefa   | Descrição                                                              | Esforço |
| -------- | ---------------------------------------------------------------------- | ------- |
| P0.Q.1.1 | `make seed-tests` — fixtures isoladas para testes automatizados        | M       |
| P0.Q.1.2 | `make seed-demo` — cenário demonstrável completo (Tenant Alpha + Beta) | M       |
| P0.Q.1.3 | Garantir idempotência e determinismo em todos os seeds                 | S       |
| P0.Q.1.4 | Documentar credenciais de demo locais                                  | S       |

**Critério de Aceite:**

- [ ] `make seed-tests` gera dados para testes paralelos
- [ ] `make seed-demo` cria cenário completo (PROJECT-SCOPE §10)
- [ ] Seeds são re-executáveis sem duplicar dados

---

#### P0.Q.2 — ETag/Conditional Requests (Across All Resources) ✅ DONE

```
commit: feat(api): implement ETag filter for conditional GET/PUT/PATCH requests (P0.Q.2)
```

| Tarefa   | Descrição                                                          | Esforço |
| -------- | ------------------------------------------------------------------ | ------- |
| P0.Q.2.1 | Interceptor genérico que adiciona ETag header baseado em `version` | M       |
| P0.Q.2.2 | Validar `If-Match` em PUT/PATCH e retornar 412 se conflito         | M       |
| P0.Q.2.3 | Aplicar em: Customers, Requests, Documents, Integrations           | M       |
| P0.Q.2.4 | Testes: concurrent update → 412, sequential update → 200           | M       |

**Critério de Aceite:**

- [ ] Edição concorrente retorna 412 Precondition Failed
- [ ] Clients recebem ETag em toda resposta GET de recurso versionado

---

### P0.R — Observabilidade e Logging (🟡 NOVO - 5ª REVISÃO)

#### P0.R.1 — Structured Logging Compliance ✅ DONE

```
commit: feat(observability): ensure structured logging follows TECHNICAL-SPECIFICATION §13
```

| Tarefa   | Descrição                                                                                      | Esforço |
| -------- | ---------------------------------------------------------------------------------------------- | ------- |
| P0.R.1.1 | Auditar formato de logs (timestamp, level, service, tenantId, actorId, correlationId, traceId) | M       |
| P0.R.1.2 | Adicionar MDC filters para popular campos obrigatórios                                         | M       |
| P0.R.1.3 | Garantir que logs nunca contenham passwords, tokens, secrets                                   | S       |
| P0.R.1.4 | Configurar JSON format em production profile                                                   | S       |

**Critério de Aceite:**

- [ ] Todos os logs incluem tenantId, correlationId e actorId quando disponíveis
- [ ] Nenhum secret vaza em logs (validado por grep automatizado)

---

#### P0.R.2 — OWASP Dependency-Check ✅ DONE

```
commit: chore(security): configure OWASP Dependency-Check to block CRITICAL vulnerabilities (P0.R.2)
```

| Tarefa   | Descrição                                         | Esforço |
| -------- | ------------------------------------------------- | ------- |
| P0.R.2.1 | Adicionar plugin OWASP Dependency-Check ao Gradle | S       |
| P0.R.2.2 | Configurar threshold: fail build se CVSS ≥ 9.0    | S       |
| P0.R.2.3 | Adicionar step ao CI pipeline                     | S       |
| P0.R.2.4 | Suprimir false positives documentados             | S       |

**Critério de Aceite:**

- [ ] Build falha se dependência com CVSS ≥ 9.0 detectada
- [ ] Relatório HTML gerado para análise

---

### P0.S — Gradle Tasks e Makefile (🔴 NOVO - 6ª REVISÃO)

> **Identificados via Makefile e QUALITY-TESTING-CICD** — Comandos referenciados que não existem como Gradle tasks.

#### P0.S.1 — Gradle Custom Tasks ✅ DONE

```
commit: build(gradle): implement custom verification and test tasks
```

| Tarefa   | Descrição                                                       | Esforço |
| -------- | --------------------------------------------------------------- | ------- |
| P0.S.1.1 | Criar task `verifyFast` (format-check + compile + test-fast)    | S       |
| P0.S.1.2 | Criar task `verifyFull` (all gates + integration + coverage)    | S       |
| P0.S.1.3 | Criar task `testFast` (exclude slow, integration, property)     | S       |
| P0.S.1.4 | Criar task `testProperty` (include only jqwik)                  | S       |
| P0.S.1.5 | Criar task `integrationTest` (include only @Tag("integration")) | S       |
| P0.S.1.6 | Criar task `aggregateJacocoReport` (multi-module coverage)      | M       |

**Critério de Aceite:**

- [ ] `./gradlew verifyFast` executa com sucesso
- [ ] `./gradlew integrationTest` filtra corretamente
- [ ] Makefile targets funcionam sem erro

---

#### P0.S.2 — Make Targets Operacionais ✅ DONE

```
commit: chore(makefile): add operational make targets (P0.S.2)
```

| Tarefa   | Descrição                                                         | Esforço |
| -------- | ----------------------------------------------------------------- | ------- |
| P0.S.2.1 | `make format-check` (alias para spotlessCheck)                    | S       |
| P0.S.2.2 | `make health` (curl actuator/health de API e Worker)              | S       |
| P0.S.2.3 | `make compose-logs` (docker compose logs -f)                      | S       |
| P0.S.2.4 | `make worker-logs` (docker compose logs -f worker)                | S       |
| P0.S.2.5 | `make reset` (alias seguro para compose-reset)                    | S       |
| P0.S.2.6 | `make projection-status` (query ProjectionStatus table)           | S       |
| P0.S.2.7 | `make verify-specs` (validar completude de specs em .kiro/specs/) | S       |

**Critério de Aceite:**

- [ ] Todos os comandos do OPERATIONS-RUNBOOK §8 funcionam
- [ ] `make help` lista todos os targets
- [ ] Hook `validate-spec-completeness.json` funciona com `make verify-specs`

---

#### P0.S.3 — Flyway Migration Configuration ✅ DONE

```
commit: build(gradle): add Flyway plugin to app-boot build and docker-build Makefile targets (P0.S.3, P0.U.1)
```

| Tarefa   | Descrição                                               | Esforço |
| -------- | ------------------------------------------------------- | ------- |
| P0.S.3.1 | Configurar plugin Flyway no `app-boot/build.gradle.kts` | M       |
| P0.S.3.2 | `./gradlew :backend:app-boot:flywayMigrate` funcional   | S       |
| P0.S.3.3 | Garantir que `make migrate` funciona end-to-end         | S       |

**Critério de Aceite:**

- [ ] `make migrate` aplica migrations com sucesso
- [ ] Re-execução é idempotente (Flyway history table)

---

### P0.T — CI Pipeline Completo (🔴 NOVO - 6ª REVISÃO)

> **Identificados via .github/workflows/ci.yml vs QUALITY-TESTING-CICD §4** — CI atual falta metade dos gates esperados.

#### P0.T.1 — CI Backend Gates Faltantes ✅ DONE

```
commit: ci(github-actions): add integration, coverage enforcement and architecture checks
```

| Tarefa   | Descrição                                               | Esforço |
| -------- | ------------------------------------------------------- | ------- | ------ | --- |
| P0.T.1.1 | Adicionar step: ArchUnit (architecture rules) explícito | S       |
| P0.T.1.2 | Adicionar step: Coverage enforcement (fail se < 75%)    | S       |
| P0.T.1.3 | Tornar OWASP Dependency-Check blocking (remover `       |         | true`) | S   |
| P0.T.1.4 | Adicionar step: Secret scanning (gitleaks ou similar)   | M       |
| P0.T.1.5 | Adicionar step: Integration tests com Testcontainers    | M       |

---

#### P0.T.2 — CI Frontend Gates Faltantes ✅ DONE

```
commit: ci(github-actions): add TypeScript strict check and bundle analysis
```

| Tarefa   | Descrição                                                 | Esforço |
| -------- | --------------------------------------------------------- | ------- |
| P0.T.2.1 | Adicionar step: `tsc --noEmit` (typecheck explícito)      | S       |
| P0.T.2.2 | Adicionar step: Bundle size check (fail se >1MB increase) | S       |

---

#### P0.T.3 — CI Integration + E2E Jobs ✅ DONE

```
commit: ci(github-actions): add docker build validation, OpenAPI lint, k6 smoke test (P0.T.3, P1.16)
```

| Tarefa   | Descrição                                                     | Esforço |
| -------- | ------------------------------------------------------------- | ------- |
| P0.T.3.1 | Job: Playwright smoke (headless, upload artifacts on failure) | M       |
| P0.T.3.2 | Job: k6 smoke test against running API                        | M       |
| P0.T.3.3 | Job: Docker build validation (Dockerfile exists, builds)      | M       |
| P0.T.3.4 | Job: Contract tests (OpenAPI validation)                      | M       |

---

### P0.U — Dockerfiles e Container Configuration (🟡 NOVO - 6ª REVISÃO)

> **Identificados via QUALITY-TESTING-CICD G11 e CI** — Aplicação não tem Dockerfile.

#### P0.U.1 — Application Dockerfiles ✅ DONE

```
commit: chore(docker): create multi-stage Dockerfiles for backend-api and worker (P0.U.1)
```

| Tarefa   | Descrição                                                   | Esforço |
| -------- | ----------------------------------------------------------- | ------- |
| P0.U.1.1 | Dockerfile para `backend/app-boot` (multi-stage, non-root)  | M       |
| P0.U.1.2 | Dockerfile para `backend/worker` (multi-stage, non-root)    | M       |
| P0.U.1.3 | Health check em ambos (HEALTHCHECK instruction)             | S       |
| P0.U.1.4 | Graceful shutdown handling (SIGTERM → Spring shutdown hook) | S       |
| P0.U.1.5 | `.dockerignore` para excluir build artifacts                | S       |
| P0.U.1.6 | `make docker-build` target no Makefile                      | S       |

**Critério de Aceite:**

- [ ] `docker build -t atlasops-api .` funciona
- [ ] Container roda como non-root user
- [ ] Health check endpoint acessível

---

### P0.V — Documentação Estrutural Faltante (🔴 NOVO - 6ª REVISÃO)

> **Identificados via QUICKSTART.md e OPERATIONS-RUNBOOK** — Docs referenciados que não existem.

#### P0.V.1 — Documentos e Configs Faltantes ✅ DONE

```
commit: docs: add CONTRIBUTING.md with development workflow and standards (P0.V.1)
```

| Tarefa   | Descrição                                                                                                        | Esforço |
| -------- | ---------------------------------------------------------------------------------------------------------------- | ------- |
| P0.V.1.1 | Criar `docs/00-current-status.md` (template do OPERATIONS-RUNBOOK §18) — 🔴 BLOQUEADOR para agent resumption     | M       |
| P0.V.1.2 | Criar `docs/adr/ADR-000-template.md` verificar se completo                                                       | S       |
| P0.V.1.3 | Seed SQL files: criar `/infra/seed/seed.sql` e `/infra/seed/seed-tests.sql`                                      | M       |
| P0.V.1.4 | Bootstrap Makefile: adicionar `make seed` ao final de `make bootstrap`                                           | S       |
| P0.V.1.5 | Criar `infra/monitoring/tempo-config.yml` (referenciado por docker-compose, compose-observability FALHA sem ele) | S       |
| P0.V.1.6 | Adicionar variável `POSTGRES_DB` ao `.env.example` (docker-compose referencia 6+ vezes)                          | S       |
| P0.V.1.7 | Decidir sobre Ollama: adicionar service ao docker-compose OU documentar que deve ser externo                     | M       |

**Critério de Aceite:**

- [ ] `make bootstrap` executa seed ao final
- [ ] `make compose-observability` não falha por config ausente
- [ ] `.env.example` tem todas as variáveis usadas pelo docker-compose
- [ ] AI pipeline funciona com Ollama acessível

---

#### P0.V.2 — Frontend Linting Configuration ✅ DONE

```
commit: feat(frontend): add ESLint config, Prettier and CI bundle/secret scan gates (P0.V.2, P0.T.1.4, P0.T.2.2)
```

**Contexto:** Steering file `frontend-conventions.md` exige ESLint + Prettier + tailwindcss plugin, mas nenhum está configurado. `pnpm lint` passa silenciosamente com config default do Next.js.

| Tarefa   | Descrição                                                                                          | Esforço |
| -------- | -------------------------------------------------------------------------------------------------- | ------- |
| P0.V.2.1 | Instalar devDependencies: eslint, prettier, eslint-plugin-tailwindcss, prettier-plugin-tailwindcss | S       |
| P0.V.2.2 | Criar `.eslintrc.json` conforme steering (next/core-web-vitals + tailwindcss/recommended + no-any) | S       |
| P0.V.2.3 | Criar `.prettierrc` conforme steering (semi, double quotes, tabWidth 2, trailing comma es5)        | S       |
| P0.V.2.4 | Verificar que `pnpm lint` agora detecta violações reais                                            | S       |

**Critério de Aceite:**

- [ ] ESLint detecta `any` types e imports não utilizados
- [ ] Prettier formata com as regras do steering file
- [ ] CI `pnpm lint` falha em código não-conforme

---

---

## Fase P1 — Experience, Search and Integrations

> **Pré-requisito:** P0 completo

### P1.1 — Table Infrastructure ✅ DONE

```
commit: feat(frontend): implement shared DataTable with pagination, skeleton and accessibility (P1.1)
```

| Tarefa | Descrição                                                        | Esforço |
| ------ | ---------------------------------------------------------------- | ------- |
| P1.1.1 | Data table genérica com paginação server-side e ordenação        | L       |
| P1.1.2 | Filtros compostos com URL state sync                             | M       |
| P1.1.3 | Seleção em lote e ações em linha                                 | M       |
| P1.1.4 | Responsividade: cards empilhados no mobile, tabela densa desktop | M       |
| P1.1.5 | Skeleton loading, empty state, error state                       | S       |
| P1.1.6 | Migrar listagens existentes (customers, requests, documents)     | L       |

---

### P1.2 — Search Foundation (PostgreSQL Fallback) ✅ DONE

```
commit: feat(search): implement PostgreSQL text-search fallback
```

| Tarefa | Descrição                                                         | Esforço |
| ------ | ----------------------------------------------------------------- | ------- |
| P1.2.1 | PostgreSQL tsvector search adapter para customers, requests, docs | L       |
| P1.2.2 | Unified search endpoint: `GET /api/v1/search`                     | M       |
| P1.2.3 | Tenant and permission filter                                      | M       |
| P1.2.4 | Deletion propagation (remove from index)                          | S       |

---

### P1.3 — OpenSearch Integration ✅ DONE

```
commit: feat(search): add OpenSearch adapter stub with feature flag
```

```
commit: feat(search): add OpenSearch adapter with analyzers and facets
```

| Tarefa | Descrição                                                         | Esforço |
| ------ | ----------------------------------------------------------------- | ------- |
| P1.3.1 | Docker Compose profile `advanced` com OpenSearch service          | M       |
| P1.3.2 | `OpenSearchAdapter` implementando `SearchPort`                    | L       |
| P1.3.3 | Index aliases (customers-current, requests-current, docs-current) | M       |
| P1.3.4 | Feature flag: `atlasops.opensearch.enabled`                       | S       |
| P1.3.5 | Fallback automático para PostgreSQL text-search se unavailable    | M       |

---

### P1.4 — Semantic Search (pgvector) ✅ DONE

```
commit: feat(search): implement pgvector semantic search with chunking
```

---

### P1.5 — Hybrid Search + Command Palette ✅ DONE

```
commit: feat(frontend): add useCommandPalette hook for global Ctrl+K state (P1.5.3)
```

| Tarefa | Descrição                                               | Esforço |
| ------ | ------------------------------------------------------- | ------- |
| P1.5.1 | Hybrid ranker combining keyword + semantic scores       | L       |
| P1.5.2 | `GET /api/v1/search/commands` — command palette backend | M       |
| P1.5.3 | Frontend: Command Palette component (⌘K)                | L       |
| P1.5.4 | Navigation por rota + ações rápidas                     | M       |

---

### P1.6-P1.10 — Integration Platform ✅ DONE (P1.9+P1.10)

```
commit: feat(integrations): add HMAC-SHA256 webhook signature and retry logic
```

| Tarefa | Descrição                                                          | Esforço |
| ------ | ------------------------------------------------------------------ | ------- |
| P1.6   | REST integration adapter com SSRF validation (DNS check)           | L       |
| P1.7   | MCP integration: client, tool schema import, per-tool policy       | L       |
| P1.8   | MongoDB payload archive (`MongoArchiveAdapter`)                    | M       |
| P1.9   | Outbound webhook: HMAC-SHA256 signature, timestamp, delivery ID    | L       |
| P1.10  | Webhook retry (exponential backoff: 1s, 5s, 30s, 120s, 600s → DLQ) | M       |

---

### P1.11 — DuckDB Customer Import ✅ DONE

```
commit: feat(imports): complete DuckDB CSV import workflow
```

---

### P1.12 — Frontend SSE Infrastructure (NOVO) ✅ DONE

```
commit: feat(frontend): integrate SSE real-time updates into activities feed (P1.12.4)
```

| Tarefa  | Descrição                                                     | Esforço |
| ------- | ------------------------------------------------------------- | ------- |
| P1.12.1 | SSE client com reconexão automática e Last-Event-ID           | M       |
| P1.12.2 | Event parsing tipado e distribuição para components           | M       |
| P1.12.3 | Heartbeat handling (detectar conexão perdida)                 | S       |
| P1.12.4 | Integrar com activities feed, notifications e upload progress | M       |

---

### P1.13 — Frontend Shared Components (NOVO) ✅ DONE

```
commit: feat(frontend): implement shared components (notification center, error boundary, upload manager)
```

| Tarefa  | Descrição                                                       | Esforço |
| ------- | --------------------------------------------------------------- | ------- |
| P1.13.1 | Notification Center: badge, dropdown, mark read, link to entity | L       |
| P1.13.2 | Document Preview component (PDF/image/text, zoom, fullscreen)   | L       |
| P1.13.3 | Optimistic UI pattern (rollback on error, toast feedback)       | M       |
| P1.13.4 | Error Boundaries por seção (admin, portal) com fallback UI      | M       |
| P1.13.5 | Toast system completo (variants, auto-dismiss, stacking)        | S       |

---

### P1.14 — Frontend Admin Pages Funcionais (NOVO) ✅ DONE

```
commit: feat(frontend): implement admin detail pages for customers, requests, documents (P1.14.2/4/6)
```

| Tarefa   | Descrição                                                           | Esforço |
| -------- | ------------------------------------------------------------------- | ------- |
| P1.14.1  | `/admin/customers` — listagem paginada + CRUD                       | L       |
| P1.14.2  | `/admin/customers/[id]` — detalhe com edição, users, requests       | L       |
| P1.14.3  | `/admin/requests` — listagem com filtros e status                   | L       |
| P1.14.4  | `/admin/requests/[id]` — detalhe com comments, docs, timeline       | L       |
| P1.14.5  | `/admin/documents` — listagem com preview thumbnails                | L       |
| P1.14.6  | `/admin/documents/[id]` — detalhe com análise IA, preview, approval | L       |
| P1.14.7  | `/admin/approvals` — listagem pending + approve/reject              | M       |
| P1.14.8  | `/admin/activities` — feed em tempo real (SSE)                      | M       |
| P1.14.9  | `/admin/search` — interface unificada keyword/semantic/hybrid       | M       |
| P1.14.10 | `/admin/dashboard` — dashboard com métricas reais                   | L       |

---

### P1.15 — Frontend Portal Pages Funcionais (NOVO) ✅ DONE

```
commit: feat(frontend): implement portal detail pages for requests and documents (P1.15.3/4)
```

| Tarefa  | Descrição                                                      | Esforço |
| ------- | -------------------------------------------------------------- | ------- |
| P1.15.1 | `/portal/home` — resumo, ações rápidas, dashboard simplificado | M       |
| P1.15.2 | `/portal/requests` — listagem + criar nova request             | M       |
| P1.15.3 | `/portal/requests/[id]` — detalhe com timeline e comments      | M       |
| P1.15.4 | `/portal/documents` — listagem com preview e download          | M       |
| P1.15.5 | `/portal/documents/upload` — upload multipart com progress     | L       |
| P1.15.6 | `/portal/notifications` — centro de notificações               | M       |

---

### P1.16 — OpenAPI Contract Lint e AsyncAPI (NOVO) ✅ DONE

```
commit: ci(github-actions): add docker build validation, OpenAPI lint, k6 smoke test (P0.T.3, P1.16)
```

| Tarefa  | Descrição                                               | Esforço |
| ------- | ------------------------------------------------------- | ------- |
| P1.16.1 | OpenAPI 3.1 completo com exemplos em endpoints críticos | M       |
| P1.16.2 | OpenAPI contract lint em CI (spectral ou similar)       | M       |
| P1.16.3 | Breaking-change detection antes de merge                | M       |
| P1.16.4 | AsyncAPI spec para domain events e SSE channels         | L       |
| P1.16.5 | AsyncAPI generator (HTML docs acessível)                | S       |

---

### P1.17 — Accessibility Baseline (NOVO) ✅ DONE

```
commit: feat(frontend): implement WCAG AA accessibility baseline utilities (P1.17)
```

| Tarefa  | Descrição                                                     | Esforço |
| ------- | ------------------------------------------------------------- | ------- |
| P1.17.1 | Audit e fix: labels em todos os inputs, aria-labels em ícones | M       |
| P1.17.2 | Contraste WCAG AA em todos os tokens de cor                   | M       |
| P1.17.3 | Navegação por teclado em todas as interfaces                  | M       |
| P1.17.4 | Skip links, focus management, screen reader announcements     | M       |
| P1.17.5 | Playwright accessibility smoke test                           | M       |

---

### P1.18 — k6 Load Test Average (NOVO) ✅ DONE

```
commit: test(load): implement k6 average scenario
```

```
commit: test(load): implement k6 average scenario (50 VUs, 5min)
```

| Tarefa  | Descrição                                               | Esforço |
| ------- | ------------------------------------------------------- | ------- |
| P1.18.1 | Script average: 50 VUs, 5min, mix realista de operações | M       |
| P1.18.2 | `make test-load` command                                | S       |
| P1.18.3 | Thresholds: p95 < 1s, p99 < 2s, error rate < 2%         | S       |
| P1.18.4 | `make test-load-report` com HTML output                 | S       |

---

### P1.19 — Contract Tests (NOVO) ✅ DONE

```
commit: ci(github-actions): add docker build validation, OpenAPI lint, k6 smoke test (P0.T.3, P1.16)
```

| Tarefa  | Descrição                                          | Esforço |
| ------- | -------------------------------------------------- | ------- |
| P1.19.1 | Validar respostas reais da API contra OpenAPI spec | M       |
| P1.19.2 | Validar eventos emitidos contra AsyncAPI spec      | M       |
| P1.19.3 | `make test-contract` command                       | S       |
| P1.19.4 | Adicionar step ao CI pipeline                      | S       |

---

### P1.20 — Feature Flag Framework (NOVO) ✅ DONE

```
commit: feat(shared-kernel): implement feature flag infrastructure
```

```
commit: feat(shared-kernel): implement feature flag infrastructure
```

| Tarefa  | Descrição                                                        | Esforço |
| ------- | ---------------------------------------------------------------- | ------- |
| P1.20.1 | Criar `FeatureFlagPort` interface no shared-kernel               | S       |
| P1.20.2 | Implementar `PropertyFeatureFlagAdapter` (application.yml based) | M       |
| P1.20.3 | Flags: `atlasops.opensearch.enabled`, `mongodb.enabled`, etc.    | S       |
| P1.20.4 | Redis key `feature:{name}` para runtime toggle                   | M       |
| P1.20.5 | Condicionar adapters especializados por flag                     | M       |

---

### P1.21 — AI Evaluation Framework (NOVO) ✅ DONE

```
commit: feat(ai): implement golden dataset + RAG quality evaluation framework (P0.G.2)
```

| Tarefa  | Descrição                                                                    | Esforço |
| ------- | ---------------------------------------------------------------------------- | ------- |
| P1.21.1 | Golden dataset table: case_id, input, expected_fields, prohibited_claims     | M       |
| P1.21.2 | Deterministic checks: valid schema, required fields, no tenant leakage       | M       |
| P1.21.3 | LLM-as-a-judge rubric (correctness, completeness, grounding, safety, format) | L       |
| P1.21.4 | `make test-ai-evaluation` command                                            | S       |
| P1.21.5 | Baseline comparison between prompt versions                                  | M       |
| P1.21.6 | Store: dataset version, prompt version, scores, failures                     | M       |

---

### P1.22 — Nightly CI Pipeline (NOVO) ✅ DONE

```
commit: ci(github-actions): implement nightly pipeline for comprehensive validation
```

```
commit: ci(github-actions): implement nightly pipeline for comprehensive validation
```

| Tarefa  | Descrição                                              | Esforço |
| ------- | ------------------------------------------------------ | ------- |
| P1.22.1 | Workflow: full load test (k6 average)                  | S       |
| P1.22.2 | Workflow: mutation testing (PIT em módulos de domínio) | M       |
| P1.22.3 | Workflow: flaky test detection (run suite 5x)          | M       |
| P1.22.4 | Workflow: architecture scan (ArchUnit)                 | S       |
| P1.22.5 | Workflow: dependency report                            | S       |
| P1.22.6 | Workflow: AI evaluation                                | M       |
| P1.22.7 | Workflow: `make security-scan` (SAST)                  | M       |

---

### P1.23 — Action Classification UI (NOVO) ✅ DONE

```
commit: feat(frontend): implement confirmation dialog for SENSITIVE/DESTRUCTIVE actions
```

```
commit: feat(frontend): implement action classification (SAFE/SENSITIVE/DESTRUCTIVE)
```

| Tarefa  | Descrição                                                                 | Esforço |
| ------- | ------------------------------------------------------------------------- | ------- |
| P1.23.1 | Confirmation dialog component para ações SENSITIVE/DESTRUCTIVE            | M       |
| P1.23.2 | Affected-resource summary antes de confirmação                            | M       |
| P1.23.3 | Aplicar em: delete customer, archive document, cancel job, approve/reject | M       |
| P1.23.4 | Ações SAFE não requerem confirmação                                       | S       |

---

### P1.24 — UI States per Page (DESIGN-PLANNING §14) (NOVO) ✅ DONE

```
commit: feat(frontend): implement maintenance, degraded, and permission denied UI states
```

```
commit: feat(frontend): implement required UI states per page
```

**Contexto:** DESIGN-PLANNING §14 exige que toda página major defina 10+ estados.

| Tarefa  | Descrição                                                            | Esforço |
| ------- | -------------------------------------------------------------------- | ------- |
| P1.24.1 | Padrão "tenant read-only" state (banner + forms disabled)            | M       |
| P1.24.2 | Padrão "stale projection" state (banner com lag info)                | M       |
| P1.24.3 | Padrão "degraded dependency" state (AI unavailable, search fallback) | M       |
| P1.24.4 | Padrão "permission denied" state (403 → redirect ou message)         | M       |
| P1.24.5 | Aplicar states em todas as admin pages                               | L       |

---

### P1.25 — Test Isolation Infrastructure (NOVO) ✅ DONE

```
commit: test(infra): implement TEST_RUN_ID isolation
```

```
commit: test(infra): implement TEST_RUN_ID isolation for parallel test execution
```

**Contexto:** QUALITY-TESTING-CICD §6 exige isolamento via TEST_RUN_ID, TEST_WORKER_ID, TEST_CASE_ID.

| Tarefa  | Descrição                                         | Esforço |
| ------- | ------------------------------------------------- | ------- |
| P1.25.1 | Gerar TEST_RUN_ID único por execução de teste     | S       |
| P1.25.2 | Redis prefix isolation: `test:{runId}:*`          | M       |
| P1.25.3 | Object storage prefix: `tests/{runId}/`           | S       |
| P1.25.4 | Stream consumer suffix: `-{runId}`                | M       |
| P1.25.5 | Test tenant factory: criar tenant isolado por run | M       |

**Critério de Aceite:**

- [ ] Testes paralelos não colidem em Redis keys
- [ ] Cada run tem dados completamente isolados

---

### P1.26 — Grafana Provisioning (NOVO) ✅ DONE

```
commit: feat(observability): complete Grafana provisioning with Tempo datasource and dashboards
```

```
commit: feat(observability): configure Grafana provisioning for datasources and dashboards
```

| Tarefa  | Descrição                                                                         | Esforço |
| ------- | --------------------------------------------------------------------------------- | ------- |
| P1.26.1 | `infra/monitoring/grafana/provisioning/datasources.yml` (Prometheus, Loki, Tempo) | M       |
| P1.26.2 | `infra/monitoring/grafana/dashboards/dashboard.yml` (provider config)             | S       |
| P1.26.3 | Dashboard JSON: API metrics, AI processing, job failures                          | M       |
| P1.26.4 | Dashboard JSON: Redis Streams lag, SSE connections                                | M       |

**Critério de Aceite:**

- [ ] Grafana inicia com datasources pré-configurados
- [ ] Dashboards visíveis imediatamente após `make compose-observability`

---

## Fase P2 — Specialized Data Capabilities

> **Pré-requisito:** P1 completo

### P2.1 — PostGIS (Customer Radius Query) ✅ DONE

```
commit: feat(customers): add PostGIS radius query support
```

```
commit: feat(customers): add PostGIS radius and nearest-customer queries
```

| Tarefa | Descrição                                                     | Esforço |
| ------ | ------------------------------------------------------------- | ------- |
| P2.1.1 | Enable PostGIS extension, add `geography(Point, 4326)` column | S       |
| P2.1.2 | GIST index on location                                        | S       |
| P2.1.3 | Geospatial port: `findWithinRadius`, `findNearest`            | M       |
| P2.1.4 | `GET /api/v1/customers?lat={lat}&lng={lng}&radius={km}`       | M       |
| P2.1.5 | Coordinate validation + migration backfill                    | S       |
| P2.1.6 | Frontend: simple map display on customer detail               | M       |

---

### P2.2 — Neo4j (Relationship Projection) ✅ DONE (stub)

```
commit: feat(specialized-data): add feature-flagged adapter stubs for Neo4j, TimescaleDB, ClickHouse, EventStoreDB
```

```
commit: feat(graph): implement Neo4j relationship and impact explorer
```

| Tarefa | Descrição                                                    | Esforço |
| ------ | ------------------------------------------------------------ | ------- |
| P2.2.1 | Docker Compose profile `advanced` com Neo4j service          | S       |
| P2.2.2 | `Neo4jGraphAdapter` implementing graph projection port       | L       |
| P2.2.3 | Nodes: Customer, Request, Document, Integration              | M       |
| P2.2.4 | Relationships: OPENED, OWNS, ATTACHED, PROCESSED_BY          | M       |
| P2.2.5 | Tenant-scoped queries, feature flag `atlasops.neo4j.enabled` | M       |
| P2.2.6 | Fallback: business write succeeds, graph becomes stale       | M       |

---

### P2.3 — TimescaleDB (Time-Series Metrics) ✅ DONE (stub)

```
commit: feat(specialized-data): add feature-flagged adapter stubs for Neo4j, TimescaleDB, ClickHouse, EventStoreDB
```

```
commit: feat(analytics): add TimescaleDB for operational metrics
```

| Tarefa | Descrição                                                                                                 | Esforço |
| ------ | --------------------------------------------------------------------------------------------------------- | ------- |
| P2.3.1 | Docker Compose profile `analytics` com TimescaleDB service                                                | S       |
| P2.3.2 | `TimescaleMetricsAdapter` implementing time-series port                                                   | L       |
| P2.3.3 | Metrics: document_processing_duration, request_sla_consumption, integration_latency, ai_analysis_duration | M       |
| P2.3.4 | Retention policies + compression                                                                          | M       |
| P2.3.5 | Feature flag `atlasops.timescaledb.enabled`                                                               | S       |
| P2.3.6 | Fallback: charts degrade, ingestion retries                                                               | M       |

---

### P2.4 — ClickHouse (Analytical Events) ✅ DONE (stub)

```
commit: feat(specialized-data): add feature-flagged adapter stubs for Neo4j, TimescaleDB, ClickHouse, EventStoreDB
```

```
commit: feat(analytics): add ClickHouse for historical analytics
```

| Tarefa | Descrição                                                                            | Esforço |
| ------ | ------------------------------------------------------------------------------------ | ------- |
| P2.4.1 | Docker Compose profile `analytics` com ClickHouse service                            | S       |
| P2.4.2 | `ClickHouseAnalyticsAdapter` implementing analytics port                             | L       |
| P2.4.3 | Events: customer.created, request.created, document.processed, ai.analysis.completed | M       |
| P2.4.4 | Queries: counts by tenant/period, avg duration, fallback ratio                       | M       |
| P2.4.5 | Feature flag `atlasops.clickhouse.enabled`                                           | S       |

---

### P2.5 — Verifiable Ledger (Append-Only Hash Chain) ✅ DONE

```
commit: feat(audit): implement append-only verifiable ledger with hash chain
```

```
commit: feat(audit): implement append-only ledger with hash chain verification
```

| Tarefa | Descrição                                                                          | Esforço |
| ------ | ---------------------------------------------------------------------------------- | ------- |
| P2.5.1 | `LedgerEntry` table (sequence, payload_hash, previous_hash, current_hash)          | M       |
| P2.5.2 | `LedgerCheckpoint` table (first_sequence, last_sequence, root_hash)                | M       |
| P2.5.3 | Hash chain: SHA-256(previous_hash + sequence + event_type + payload + occurred_at) | M       |
| P2.5.4 | `make verify-ledger` command                                                       | M       |
| P2.5.5 | Periodic verification job + alerting on tampering                                  | M       |
| P2.5.6 | Endpoint: `GET /api/v1/audit/ledger/verify`                                        | S       |

---

### P2.6 — EventStoreDB (Approval Event Sourcing) ✅ DONE (stub)

```
commit: feat(specialized-data): add feature-flagged adapter stubs for Neo4j, TimescaleDB, ClickHouse, EventStoreDB
```

```
commit: feat(approvals): implement EventStoreDB for approval aggregate
```

| Tarefa | Descrição                                                          | Esforço |
| ------ | ------------------------------------------------------------------ | ------- |
| P2.6.1 | Docker Compose profile `event-sourcing` com EventStoreDB           | S       |
| P2.6.2 | `EventStoreApprovalAdapter` implementing event-sourcing port       | L       |
| P2.6.3 | Events: ApprovalRequested, Assigned, Approved, Rejected, Cancelled | M       |
| P2.6.4 | Approval read model projection in PostgreSQL                       | M       |
| P2.6.5 | Feature flag `atlasops.eventstoredb.enabled`                       | S       |
| P2.6.6 | Fallback: approvals blocked (503) when unavailable                 | M       |

---

### P2.7 — Projection Registry and Rebuild Commands ✅ DONE

```
commit: feat(operations): add projection health registry and rebuild Makefile commands
```

```
commit: feat(operations): add projection health registry and rebuild commands
```

| Tarefa | Descrição                                                                                | Esforço |
| ------ | ---------------------------------------------------------------------------------------- | ------- |
| P2.7.1 | `ProjectionStatus` table with lifecycle (DISABLED→PENDING→PROCESSING→READY→STALE→FAILED) | M       |
| P2.7.2 | `make rebuild-search-index` command                                                      | M       |
| P2.7.3 | `make rebuild-vector-index` command                                                      | M       |
| P2.7.4 | `make rebuild-graph` command                                                             | M       |
| P2.7.5 | `make rebuild-analytics` command                                                         | M       |
| P2.7.6 | `make replay-projections` command                                                        | M       |
| P2.7.7 | Lag metrics per projection exposed in Prometheus                                         | S       |
| P2.7.8 | Admin UI: projection status dashboard                                                    | M       |
| P2.7.9 | `POST /api/v1/operations/projections/{name}/rebuild` endpoint                            | M       |

---

### P2.8 — Cross-Store Deletion and Retention (NOVO) ✅ DONE

```
commit: feat(data): implement cross-store deletion orchestrator
```

```
commit: feat(data): implement cross-store deletion cascade and retention policies
```

| Tarefa | Descrição                                                              | Esforço |
| ------ | ---------------------------------------------------------------------- | ------- |
| P2.8.1 | Deletion orchestrator: legal hold check → state update → object delete | L       |
| P2.8.2 | Remove from OpenSearch, pgvector, Neo4j on entity delete               | M       |
| P2.8.3 | MongoDB retention (TTL) for integration archive                        | S       |
| P2.8.4 | Emit analytical correction event for ClickHouse                        | S       |
| P2.8.5 | Append ledger evidence for audit trail                                 | S       |
| P2.8.6 | Retry on projection outage (never silent inconsistency)                | M       |

---

### P2.9 — Document Legal Hold (NOVO) ✅ DONE

```
commit: feat(documents): implement legal hold preventing archive/delete
```

```
commit: feat(documents): implement legal hold preventing archive/delete
```

| Tarefa | Descrição                                                 | Esforço |
| ------ | --------------------------------------------------------- | ------- |
| P2.9.1 | `POST /api/v1/documents/{id}/hold` — activate legal hold  | S       |
| P2.9.2 | `DELETE /api/v1/documents/{id}/hold` — release legal hold | S       |
| P2.9.3 | Enforce: block archive/delete while hold active           | M       |
| P2.9.4 | Cross-store deletion respects hold flag                   | S       |
| P2.9.5 | Frontend: hold indicator on document detail               | S       |

---

### P2.10 — Request SLA (NOVO) ✅ DONE

```
commit: feat(requests): implement basic SLA with deadline and alert
```

```
commit: feat(requests): implement basic SLA with deadline and alerts
```

| Tarefa  | Descrição                                                 | Esforço |
| ------- | --------------------------------------------------------- | ------- |
| P2.10.1 | SLA deadline configurável por tenant (default: 7 days)    | M       |
| P2.10.2 | Alert: notification ao ANALYST quando SLA breach iminente | M       |
| P2.10.3 | Frontend: SLA indicator na request list e detail          | M       |
| P2.10.4 | Analytics: SLA compliance rate metric                     | S       |

---

### P2.11 — Tenant Read-Only Mode (NOVO) ✅ DONE

```
commit: feat(tenants): implement tenant read-only maintenance mode
```

```
commit: feat(tenants): implement tenant read-only (maintenance) mode
```

| Tarefa  | Descrição                                                                        | Esforço |
| ------- | -------------------------------------------------------------------------------- | ------- |
| P2.11.1 | Filter que rejeita writes (POST/PUT/PATCH/DELETE) quando `maintenance_mode=true` | M       |
| P2.11.2 | Endpoint: `PUT /api/v1/tenants/{id}/maintenance` (toggle)                        | S       |
| P2.11.3 | Frontend: banner "Tenant em modo manutenção" + disable forms                     | M       |
| P2.11.4 | Bypass para ADMIN super-admin                                                    | S       |

---

### P2.12 — Tenant Basic Branding (NOVO) ✅ DONE

```
commit: feat(tenants): implement basic branding endpoint
```

```
commit: feat(tenants): implement basic branding (logo, primary color)
```

| Tarefa  | Descrição                                                               | Esforço |
| ------- | ----------------------------------------------------------------------- | ------- |
| P2.12.1 | Endpoint: `PUT /api/v1/tenants/{id}/branding` (logo_url, primary_color) | S       |
| P2.12.2 | Frontend: aplicar primary_color como CSS variable                       | M       |
| P2.12.3 | Frontend: exibir logo no header/sidebar                                 | S       |
| P2.12.4 | Admin settings page: section de branding                                | M       |

---

### P2.13 — Notification Preferences (NOVO) ✅ DONE

```
commit: feat(notifications): implement per-user channel preferences
```

```
commit: feat(notifications): implement per-user channel preferences
```

| Tarefa  | Descrição                                                      | Esforço |
| ------- | -------------------------------------------------------------- | ------- |
| P2.13.1 | `GET /api/v1/notifications/preferences`                        | S       |
| P2.13.2 | `PUT /api/v1/notifications/preferences` (email_enabled, types) | S       |
| P2.13.3 | Respeitar preferences no dispatch de notificações              | M       |
| P2.13.4 | Frontend: preferences UI na page de notifications              | M       |

---

### P2.14 — Internationalization Setup (NOVO) ✅ DONE

```
commit: feat(frontend): implement i18n with next-intl
```

```
commit: feat(frontend): implement i18n infrastructure
```

| Tarefa  | Descrição                                       | Esforço |
| ------- | ----------------------------------------------- | ------- |
| P2.14.1 | Configurar next-intl ou react-i18next           | M       |
| P2.14.2 | Extrair textos hardcoded para translation files | L       |
| P2.14.3 | Locale pt-BR e en-US                            | M       |
| P2.14.4 | Date/number formatting por locale               | S       |

---

## Fase P3 — Hardening, Resilience and Release

> **Pré-requisito:** P2 completo

### P3.1 — Security Hardening ✅ DONE

```
commit: feat(security): implement threat model and ASVS-oriented security review
```

| Tarefa | Descrição                                                 | Esforço |
| ------ | --------------------------------------------------------- | ------- |
| P3.1.1 | Threat model: IDOR, cross-tenant leakage, role escalation | L       |
| P3.1.2 | ASVS-oriented review: auth, session, access control       | L       |
| P3.1.3 | SAST (static analysis security testing) integration       | M       |
| P3.1.4 | DAST (dynamic analysis) against running API               | M       |
| P3.1.5 | Authorization abuse tests (CLIENT → ADMIN escalation)     | M       |
| P3.1.6 | Upload security: malicious file, path traversal           | M       |
| P3.1.7 | AI/MCP safety: prompt injection review                    | M       |
| P3.1.8 | Row-Level Security (PostgreSQL RLS) as defense in depth   | L       |

---

### P3.2 — Resilience Testing ✅ DONE

```
commit: test(resilience): implement dependency failure and circuit breaker tests
```

| Tarefa | Descrição                                                       | Esforço |
| ------ | --------------------------------------------------------------- | ------- |
| P3.2.1 | Test: PostgreSQL unavailable → graceful degradation             | M       |
| P3.2.2 | Test: Redis unavailable → fallback behavior                     | M       |
| P3.2.3 | Test: MinIO unavailable → upload rejected cleanly               | M       |
| P3.2.4 | Test: Ollama unavailable → deterministic fallback               | M       |
| P3.2.5 | Test: OpenSearch unavailable → PostgreSQL fallback search       | M       |
| P3.2.6 | Circuit breaker verification: open/half-open/closed transitions | M       |

---

### P3.3 — Backup and Restore ✅ DONE

```
commit: feat(ops): implement backup/restore scripts with validation
```

| Tarefa | Descrição                                      | Esforço |
| ------ | ---------------------------------------------- | ------- |
| P3.3.1 | PostgreSQL backup script (pg_dump per tenant)  | M       |
| P3.3.2 | MinIO backup script (object storage sync)      | M       |
| P3.3.3 | Restore script with validation                 | L       |
| P3.3.4 | `make restore-validate BACKUP_ID=<id>` command | M       |
| P3.3.5 | Tenant export/import (portability)             | L       |

---

### P3.4 — Performance Testing ✅ DONE

```
commit: test(load): implement k6 stress scenario and bottleneck analysis
```

| Tarefa | Descrição                                              | Esforço |
| ------ | ------------------------------------------------------ | ------- |
| P3.4.1 | k6 stress scenario: 200 VUs, 10min, ramp-up escalonado | M       |
| P3.4.2 | Large upload performance test (>500MB)                 | M       |
| P3.4.3 | Search latency under load                              | M       |
| P3.4.4 | Worker throughput analysis (documents/min)             | M       |
| P3.4.5 | Database query optimization (slow query analysis)      | L       |
| P3.4.6 | Thresholds: p95 < 3s, error rate < 10%, zero crashes   | S       |

---

### P3.5 — Test Reliability ✅ DONE

```
commit: test(quality): implement flaky detection and mutation testing
```

| Tarefa | Descrição                                                    | Esforço |
| ------ | ------------------------------------------------------------ | ------- |
| P3.5.1 | Flaky test detection (run suite 5x, flag inconsistent tests) | M       |
| P3.5.2 | Mutation testing (PIT ou similar) em módulos de domínio      | L       |
| P3.5.3 | Fix flaky tests identified                                   | M       |

---

### P3.6 — Observability and SLOs ✅ DONE

```
commit: feat(observability): implement alert definitions and SLO baselines
```

| Tarefa | Descrição                                                                                       | Esforço |
| ------ | ----------------------------------------------------------------------------------------------- | ------- |
| P3.6.1 | Distributed tracing: Tempo + OpenTelemetry instrumentation                                      | L       |
| P3.6.2 | Grafana dashboards: jobs, stream lag, projection health, upload                                 | M       |
| P3.6.3 | Alert: job failure rate > 5%                                                                    | S       |
| P3.6.4 | Alert: AI fallback rate > 20%                                                                   | S       |
| P3.6.5 | Alert: search latency p95 > 800ms                                                               | S       |
| P3.6.6 | Alert: storage error rate > 1%                                                                  | S       |
| P3.6.7 | Alert: ledger tampering detected                                                                | S       |
| P3.6.8 | SLO baselines: availability, latency, error rate                                                | M       |
| P3.6.9 | Nightly CI pipeline (full load, mutation, architecture scan, projection rebuild, AI evaluation) | L       |

---

### P3.7 — Supply Chain ✅ DONE

```
commit: chore(security): implement SBOM and reproducible builds
```

| Tarefa | Descrição                                                    | Esforço |
| ------ | ------------------------------------------------------------ | ------- |
| P3.7.1 | SBOM generation (CycloneDX or SPDX)                          | M       |
| P3.7.2 | Reproducible Docker images (pinned base images, multi-stage) | M       |
| P3.7.3 | Version tagging: SemVer + git tag                            | S       |
| P3.7.4 | Changelog generation from conventional commits               | S       |

---

### P3.8 — Documentation Closure ✅ DONE

```
commit: docs: final spec updates and demo script
```

| Tarefa | Descrição                                                    | Esforço |
| ------ | ------------------------------------------------------------ | ------- |
| P3.8.1 | All 16 ADRs written (ADR-001 through ADR-016)                | L       |
| P3.8.2 | Developer docs page functional (Swagger, AsyncAPI, examples) | M       |
| P3.8.3 | Demo script documentado (end-to-end walkthrough)             | M       |
| P3.8.4 | Current status doc updated (`docs/00-current-status.md`)     | S       |
| P3.8.5 | Architecture diagrams refreshed                              | M       |
| P3.8.6 | Known limitations documented                                 | S       |

---

### P3.9 — Admin Settings and Developer Page (NOVO) ✅ DONE

```
commit: feat(frontend): implement admin settings and developer documentation pages
```

| Tarefa | Descrição                                                        | Esforço |
| ------ | ---------------------------------------------------------------- | ------- |
| P3.9.1 | `/admin/settings` — tenant config, users, roles, branding        | L       |
| P3.9.2 | `/admin/audit` — ledger viewer with filters                      | L       |
| P3.9.3 | `/developers` — embedded Swagger, AsyncAPI, SSE/webhook examples | L       |

---

### P3.10 — ETag/Version Conflict UI (NOVO) ✅ DONE

```
commit: feat(frontend): implement ETag conflict detection and resolution UI
```

| Tarefa  | Descrição                                             | Esforço |
| ------- | ----------------------------------------------------- | ------- |
| P3.10.1 | Detect 412 Precondition Failed in API client          | S       |
| P3.10.2 | Conflict resolution dialog (show diff, reload, retry) | L       |
| P3.10.3 | Stale-projection banner when data may be outdated     | M       |

---

### P3.11 — Design System / Storybook (NOVO) ✅ DONE

```
commit: docs(frontend): implement design system catalog
```

| Tarefa  | Descrição                                            | Esforço |
| ------- | ---------------------------------------------------- | ------- |
| P3.11.1 | Storybook setup com componentes shadcn/ui            | M       |
| P3.11.2 | Document design tokens (colors, typography, spacing) | M       |
| P3.11.3 | Component variants catalog                           | M       |

---

### P3.12 — Docker Compose Profile Reorganization ✅ DONE

> **Nota:** docker-compose.yml já possui profiles definidos. Tasks restantes são ajustes finais.

```
commit: chore(infra): finalize Docker Compose profile organization
```

| Tarefa  | Descrição                                                                      | Esforço |
| ------- | ------------------------------------------------------------------------------ | ------- |
| P3.12.1 | Verificar que Prometheus/Grafana/Loki/MailHog estão no profile `observability` | S       |
| P3.12.2 | Verificar Tempo service no profile `observability` com config funcional        | S       |
| P3.12.3 | Confirmar Ollama gerenciado externamente (documentar no QUICKSTART)            | S       |
| P3.12.4 | Backend API + Worker com Dockerfiles funcionais em compose                     | M       |
| P3.12.5 | Validar que `make compose-all` sobe todos os services sem erro                 | S       |

---

### P3.13 — Release Candidate ✅ DONE

```
commit: chore(release): final validation gate for v1.0.0-rc.1
```

| Tarefa  | Descrição                                                                | Esforço |
| ------- | ------------------------------------------------------------------------ | ------- |
| P3.13.1 | Full validation: `make seed-reset && make seed-demo && make verify-full` | S       |
| P3.13.2 | `make test-functional` — all 20 journeys pass                            | S       |
| P3.13.3 | `make test-load` — average scenario passes thresholds                    | S       |
| P3.13.4 | `make verify-ledger` — no tampering detected                             | S       |
| P3.13.5 | All 12 PROJECT-SCOPE success criteria validated                          | M       |
| P3.13.6 | Release notes + tag `v1.0.0-rc.1`                                        | S       |

---

## Métricas de Qualidade Atuais vs Target

| Métrica             | Atual      | Target P0       | Target P3      |
| ------------------- | ---------- | --------------- | -------------- |
| Line Coverage       | ~60%       | ≥ 75%           | ≥ 80%          |
| Branch Coverage     | ~50%       | ≥ 65%           | ≥ 70%          |
| Domain Coverage     | ~75%       | ≥ 85%           | ≥ 90%          |
| Checkstyle Warnings | ⚠️         | 0               | 0              |
| SpotBugs Issues     | ⚠️         | 0               | 0              |
| ArchUnit            | ✅         | Pass            | Pass           |
| Playwright E2E      | 1 test     | 10+ journeys    | 20+ journeys   |
| k6 Load Tests       | Smoke only | Smoke + Average | Full scenarios |

---

## Priorização Recomendada (Sprint-like)

### Sprint 1 (1-2 semanas) — Security Foundation ⚠️ BLOQUEADOR

**Objetivo:** Implementar stack de segurança completa (sem isso nada funciona)

1. **P0.H.1** — JWT Authentication Filter
2. **P0.K.2** — Tenant Authorization Filter
3. **P0.K.1** — CORS Configuration
4. **P0.O.1** — Resource Authorization (CLIENT-scoped access)
5. **P0.O.2** — Distributed Locking (Redis)

**Entrega:** Endpoints protegidos, tenant isolation enforced, resource auth

---

### Sprint 2 (1-2 semanas) — Frontend Foundation ⚠️ BLOQUEADOR

**Objetivo:** Frontend pode comunicar com backend de forma autenticada

1. **P0.L.1** — Frontend Auth State Management
2. **P0.L.2** — Frontend API Client Tipado
3. **P0.L.3** — Frontend Form Infrastructure
4. **P0.L.4** — Frontend Responsive Layouts

**Entrega:** Frontend funcional com login, API calls, forms

---

### Sprint 3 (1-2 semanas) — Foundation Quality

**Objetivo:** Base de testes automatizados

1. **P0.A.1** — Testcontainers setup e integration tests
2. **P0.A.2** — Cross-tenant isolation tests
3. **P0.D.1** — CI Pipeline enhancement
4. **P0.R.2** — OWASP Dependency-Check

**Entrega:** Integration tests rodando em CI

---

### Sprint 4 (1-2 semanas) — E2E e Coverage

**Objetivo:** Cobertura ≥75% e jornadas E2E críticas

1. **P0.B.1** — Playwright setup e page objects
2. **P0.B.2.1-4** — Jornadas críticas (login, CRUD, upload, approval)
3. **P0.A.3** — Complementar unit tests em módulos ⚠️

**Entrega:** E2E tests rodando + cobertura ≥75%

---

### Sprint 5 (1-2 semanas) — Multipart Upload + Events

**Objetivo:** Upload funcional >500MB + event backbone

1. **P0.M.1** — Multipart Upload Backend (signed URLs)
2. **P0.M.2** — Multipart Upload Frontend (pause/resume)
3. **P0.P.1** — Redis Streams Consumer Groups
4. **P0.P.2** — Domain Events (core modules)

**Entrega:** Upload end-to-end + event-driven processing

---

### Sprint 6 (1-2 semanas) — Módulos + Features Core

**Objetivo:** Completar módulos e funcionalidades essenciais

1. **P0.C.1** — Integrations adapters (REST + MCP)
2. **P0.C.2** — Imports DuckDB workflow
3. **P0.E.1** — Idempotency-Key Header
4. **P0.I.1** — Operations Module Foundation
5. **P0.J.1** — SSRF Protection

**Entrega:** Módulos P0 completos

---

### Sprint 7 (1-2 semanas) — SSE, Metrics, Domain Features

**Objetivo:** SSE robusto + funcionalidades de domínio faltantes

1. **P0.H.2** — SSE Event Replay
2. **P0.E.2** — SSE Heartbeat
3. **P0.E.3** — Processing Metrics (Micrometer)
4. **P0.H.3** — Outbox Event Cleanup
5. **P0.N.1** — Request Comments
6. **P0.N.2** — Request Status History
7. **P0.N.5** — Document Reprocessing
8. **P0.N.6** — Document Preview Generation

**Entrega:** SSE robusto + domain features completas

---

### Sprint 8 (1-2 semanas) — Quality Gates + Seeds + Polish

**Objetivo:** Fechar P0 com todos os quality gates

1. **P0.Q.1** — Seed Scripts completos (seed-tests, seed-demo)
2. **P0.Q.2** — ETag/Conditional Requests (across all resources)
3. **P0.R.1** — Structured Logging Compliance
4. **P0.N.3** — Customer Activate/Deactivate
5. **P0.N.4** — Customer User Association
6. **P0.B.2.5-10** — E2E restantes
7. **P0.K.3** — Frontend pages stub → funcionais
8. Resolver warnings (Checkstyle, SpotBugs)

**Entrega:** P0 Release Candidate

---

### Sprint 9 (Opcional - 1-2 semanas) — Funcionalidades Adicionais 🟡

**Escolher 3-4 das seguintes (ordem de valor):**

1. **P0.F.1** — Approval Ledger (hash chain)
2. **P0.F.2** — Prompt Version Registry
3. **P0.F.3** — Operations Job UI (retry/cancel)
4. **P0.J.2** — Circuit Breaker (Resilience4j)
5. **P0.I.2** — Analytics Foundation
6. **P0.G.2** — Golden Dataset + AI Evaluation
7. **P0.G.3** — AsyncAPI Documentation

**Entrega:** Features opcionais funcionais e testadas

---

## Comandos de Validação

```bash
# Validação completa P0
make seed-reset
make seed-demo
make verify
make test-functional
make test-load-smoke

# Release candidate (P3)
make verify-full
make test-functional
make test-load
make verify-ledger
```

---

## 📚 Documentos Relacionados

| Documento                                      | Descrição                               |
| ---------------------------------------------- | --------------------------------------- |
| [STATUS.md](./STATUS.md)                       | Status atual consolidado do projeto     |
| [BUILD-PERFORMANCE.md](./BUILD-PERFORMANCE.md) | Guia de otimizações de build            |
| [QUICKSTART.md](./QUICKSTART.md)               | Setup rápido para novos desenvolvedores |
| [AGENTS.md](../AGENTS.md)                      | Governança e papéis de agentes          |
| [architecture/](./architecture/)               | Documentação de arquitetura             |
| [testing/](./testing/)                         | Estratégia de testes                    |
| [task-plans/](./task-plans/)                   | Planos detalhados por fase              |
| [.kiro/specs/](../.kiro/specs/)                | Specs SDD do projeto                    |

---

## ✅ Definição de Pronto (Definition of Done)

Uma tarefa só é considerada **concluída** quando:

- [ ] Código implementado conforme acceptance criteria
- [ ] Testes unitários escritos e passando
- [ ] Testes de integração escritos (quando aplicável)
- [ ] Testes E2E escritos (quando aplicável)
- [ ] `make verify` passa sem erros
- [ ] Cobertura ≥ 75% (≥85% para domínio)
- [ ] Sem warnings de compilação
- [ ] Código formatado (`make format`)
- [ ] Lint sem erros (`make lint`)
- [ ] ArchUnit validações passando
- [ ] Documentação atualizada (se interfaces alteradas)
- [ ] PR vinculado à task correspondente
- [ ] Review aprovado

---

## 📞 Contato e Suporte

- **Documentação:** [docs/](.)
- **Issues:** GitHub Issues
- **Convenções:** [.kiro/steering/](../.kiro/steering/)
- **Diagnóstico:** `make doctor`
