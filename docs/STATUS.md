# AtlasOps AI — Status do Projeto

> **Última atualização:** 2026-07-27  
> **Versão:** 0.1.0-SNAPSHOT  
> **Fase:** P0 (MVP Foundation) — ~75% completo  
> **Próximo milestone:** Completar testes e integração E2E

---

## 📋 Visão Geral

AtlasOps AI é um CRM multi-tenant com análise de documentos via IA local (Ollama + pgvector). O projeto está em desenvolvimento ativo com a camada P0 (foundation) 75% completa. Performance de build e testes foram significativamente otimizados.

**Status consolidado:**

- ✅ **Arquitetura hexagonal** implementada em 19 módulos
- ✅ **Build otimizado** (~2-3min, 70% mais rápido)
- ✅ **Multi-tenancy** com isolamento completo
- ✅ **AI/RAG pipeline** funcional (Ollama + pgvector)
- ⚠️ **Testes de integração** precisam ser completados
- ⚠️ **E2E (Playwright)** apenas 1 teste implementado
- ⚠️ **Cobertura** em ~60% (meta: ≥75%)

---

## 🏗️ Stack Tecnológico

### Backend

- **Linguagem:** Java 21
- **Framework:** Spring Boot 3.2.5 + Spring Framework 6.x
- **Build:** Gradle multi-project (19 módulos) com Kotlin DSL
- **Arquitetura:** Hexagonal (ports & adapters) por módulo

### Frontend

- **Framework:** React 19 + Next.js 15 (App Router)
- **Linguagem:** TypeScript (strict mode)
- **Estilização:** Tailwind CSS v4 + shadcn/ui
- **Package Manager:** pnpm

### Infraestrutura

- **Banco de dados:** PostgreSQL 16 + pgvector
- **Cache/Mensageria:** Redis 7
- **Object Storage:** MinIO (S3-compatible)
- **IA Local:** Ollama + Spring AI
- **Observabilidade:** Prometheus, Grafana, Loki, Micrometer

### Testes

- **Unit:** JUnit 5 + Mockito
- **Property-Based:** jqwik
- **Integration:** Testcontainers
- **E2E:** Playwright (em progresso)
- **Architecture:** ArchUnit

---

## 📊 Status dos Módulos

| Módulo        | Domain | Application | Infrastructure | Presentation | Tests | Prioridade |
| ------------- | :----: | :---------: | :------------: | :----------: | :---: | :--------: |
| shared-kernel |   ✅   |     N/A     |      N/A       |     N/A      |  ✅   |    Core    |
| auth          |   ✅   |     ✅      |       ✅       |      ✅      |  ✅   |    Core    |
| tenants       |   ✅   |     ✅      |       ✅       |      ✅      |  ✅   |    Core    |
| users         |   ✅   |     ✅      |       ✅       |      ✅      |  ✅   |    Core    |
| customers     |   ✅   |     ✅      |       ✅       |      ✅      |  ✅   |     P0     |
| documents     |   ✅   |     ✅      |       ✅       |      ✅      |  ✅   |     P0     |
| requests      |   ✅   |     ✅      |       ✅       |      ✅      |  ✅   |     P0     |
| approvals     |   ✅   |     ✅      |       ✅       |      ✅      |  ⚠️   |     P0     |
| activities    |   ✅   |     ✅      |       ✅       |      ✅      |  ⚠️   |     P0     |
| notifications |   ✅   |     ✅      |       ✅       |      ✅      |  ⚠️   |     P0     |
| integrations  |   ✅   |     ✅      |       ⚠️       |      ⚠️      |  ⚠️   |     P1     |
| search        |   ✅   |     ✅      |       ✅       |      ✅      |  ⚠️   |     P1     |
| imports       |   ✅   |     ✅      |       ⚠️       |      ⚠️      |  ⚠️   |     P1     |
| operations    |   ✅   |     ✅      |       ✅       |      ✅      |  ⚠️   |     P0     |
| ai            |   ✅   |     ✅      |       ✅       |      ✅      |  ✅   |     P0     |
| analytics     |   ✅   |     ✅      |       ⚠️       |      ✅      |  ⚠️   |     P1     |
| audit         |   ✅   |     ✅      |       ✅       |      ✅      |  ⚠️   |     P0     |
| app-boot      |  N/A   |     N/A     |       ✅       |      ✅      |  ✅   |    Core    |
| worker        |  N/A   |     ✅      |       ✅       |     N/A      |  ⚠️   |     P0     |

**Legenda:**

- ✅ Completo e testado
- ⚠️ Parcial ou precisa melhorias
- ❌ Não iniciado
- **Core:** Infraestrutura essencial
- **P0:** MVP critical path
- **P1:** Próxima fase

---

## ✅ Recentemente Concluído (2026-07)

### Infraestrutura e Performance

- [x] **Build Performance Optimizations** — Build 70% mais rápido (~2-3min)
- [x] **Test Parallelization** — Testes unitários 60% mais rápidos (~1-2min)
- [x] **Novos comandos de teste** (`make test-fast`, `make test-property`, `make verify-fast`)
- [x] **Configuration Cache** e **Parallel Execution** no Gradle
- [x] **Quality Baseline W1** — `verify-local-fast`, `verify-frontend-fast`, `verify-precommit`, `verify-prepush`, `verify-contracts`
- [x] **Frontend Formatting Gate** — `prettier --check` integrado à CI e ao fluxo local
- [x] **ArchUnit CI Enforcement** — remoção do bypass `|| true`
- [x] **Frontend Quality Check** — `pnpm --dir frontend run verify:fast`, `lint`, `typecheck` e `build` verdes
- [x] **Backend Fast Path** — `make verify-local-fast` verde após estabilização de testes e contratos

### Features de Segurança

- [x] **Transactional Outbox Pattern** — Garantia de entrega de eventos
- [x] **Refresh Token Rotation** — Token families com detecção de replay
- [x] **Session Revocation** — Endpoint `POST /api/v1/auth/revoke-all-sessions`
- [x] **Rate Limiting** — Redis-backed, tiered por tipo de endpoint
- [x] **ETag/Optimistic Concurrency** — Version columns + interface `Versioned`

### Funcionalidades de Domínio

- [x] **Seed Commands** — `make seed`, `make seed-reset`, `make seed-demo`, `make seed-tests`
- [x] **Apache Tika Text Extraction** — Suporte a 1000+ formatos (PDF, DOCX, etc.)
- [x] **OpenAPI/Swagger Documentation** — SpringDoc integrado

---

## 🚧 Em Progresso / Próximos Passos

### 🔴 Alta Prioridade (MVP Critical Path)

#### Testes

- [ ] **Testcontainers Integration Tests** — Setup base + testes por módulo
- [ ] **Cross-Tenant Isolation Tests** — Provar isolamento entre tenants
- [ ] **Playwright E2E Journeys** — 10+ cenários críticos (login, CRUD, upload, aprovação)
- [ ] **Test Coverage Improvement** — Módulos ⚠️ precisam atingir ≥75%

#### Módulos Incompletos

- [ ] **Integrations Module** — REST e MCP adapters com SSRF protection
- [ ] **Imports Module** — DuckDB CSV import workflow completo
- [ ] **Analytics Infrastructure** — Queries de agregação e cache Redis

#### Funcionalidades Faltantes

- [ ] **Idempotency-Key Header** — Suporte em endpoints POST críticos
- [ ] **SSE Heartbeat** — Reconexão com Last-Event-ID
- [ ] **Processing Metrics** — Micrometer counters/timers para AI

### 🟡 Média Prioridade (Qualidade)

- [ ] **CI/CD Enhancement** — Integration tests e E2E na pipeline
- [ ] **Approval Ledger** — Append-only hash chain para auditoria
- [ ] **Prompt Version Registry** — Versionamento de prompts de IA
- [ ] **Operations Job UI** — Interface para retry/cancel de jobs

### 🔄 W5/W6 Quality Enforcement Status

- **W5 Runtime Resilience**: mostly in place. Circuit breaker baseline, dedicated `resilienceTest`, operator runbook, CI routing, and initial fault-injection coverage are present; additional outage suites for PostgreSQL, Redis and OpenSearch are still pending.
- **W6 Performance Evidence**: mostly in place. k6 scenarios, CI smoke execution, nightly average test, JSON + HTML artifact targets, and documented metadata/threshold inputs exist; frontend performance evidence is still pending.

### 🟢 Baixa Prioridade (Nice-to-have P0)

- [ ] **Multipart Upload** — Signed part URLs, pause/resume
- [ ] **Golden Dataset** — Framework de avaliação de IA
- [ ] **AsyncAPI Documentation** — Documentação de eventos assíncronos

---

## ⚡ Performance de Build

| Métrica         | Antes      | Depois   | Melhoria   |
| --------------- | ---------- | -------- | ---------- |
| **Full Build**  | ~8-10 min  | ~2-3 min | **70%** ⬇️ |
| **Unit Tests**  | ~4-5 min   | ~1-2 min | **60%** ⬇️ |
| **Fast Tests**  | N/A        | ~30s     | Novo ✨    |
| **Full Verify** | ~15-20 min | ~5-8 min | **60%** ⬇️ |

**Principais otimizações:**

- Configuration Cache (`org.gradle.configuration-cache=true`)
- Parallel Execution (`org.gradle.parallel=true`)
- Build Cache (`org.gradle.caching=true`)
- JUnit 5 Parallel Test Execution
- Multi-fork test execution

📖 Detalhes completos: [BUILD-PERFORMANCE.md](./BUILD-PERFORMANCE.md)

---

## 🐳 Infraestrutura (Docker Compose)

| Serviço        | Status | Versão                | Porta(s)   | Descrição                  |
| -------------- | :----: | --------------------- | ---------- | -------------------------- |
| **PostgreSQL** |   ✅   | 16 (pgvector enabled) | 5432       | Banco de dados principal   |
| **Redis**      |   ✅   | 7-alpine              | 6379       | Cache e mensageria         |
| **MinIO**      |   ✅   | RELEASE.2024-01-01    | 9000, 9001 | Object storage (S3-compat) |
| **Ollama**     |   ✅   | 0.3.4                 | 11434      | IA local (LLM)             |
| **Prometheus** |   ✅   | v2.48.0               | 9090       | Coleta de métricas         |
| **Grafana**    |   ✅   | 10.2.0                | 3000       | Dashboards e visualização  |
| **Loki**       |   ✅   | 2.9.0                 | 3100       | Agregação de logs          |
| **MailHog**    |   ✅   | v1.0.1                | 1025, 8025 | SMTP mock (dev)            |

**Comandos:**

```bash
make compose-up      # Inicia toda infraestrutura
make compose-down    # Para todos os serviços
make compose-reset   # Recria do zero (remove volumes)
```

---

## 📈 Métricas de Qualidade

| Métrica                    | Atual   | Meta P0 | Meta P3 | Status |
| -------------------------- | ------- | ------- | ------- | :----: |
| **Line Coverage**          | ~60%    | ≥ 75%   | ≥ 80%   |   ⚠️   |
| **Branch Coverage**        | ~50%    | ≥ 65%   | ≥ 70%   |   ⚠️   |
| **Domain Coverage**        | ~75%    | ≥ 85%   | ≥ 90%   |   ⚠️   |
| **Compilation**            | 0 erros | 0 erros | 0 erros |   ✅   |
| **Lint (Checkstyle)**      | ⚠️      | 0 warns | 0 warns |   ⚠️   |
| **SpotBugs**               | ⚠️      | 0 bugs  | 0 bugs  |   ⚠️   |
| **ArchUnit**               | Pass    | Pass    | Pass    |   ✅   |
| **Integration Tests**      | 0       | ≥ 50    | ≥ 100   |   ❌   |
| **E2E Tests (Playwright)** | 1       | ≥ 10    | ≥ 20    |   ❌   |

**Próximas ações:**

1. Implementar integration tests com Testcontainers (P0.A.1)
2. Complementar unit tests em módulos ⚠️ (P0.A.3)
3. Criar jornadas E2E Playwright (P0.B.2)
4. Corrigir warnings de Checkstyle e SpotBugs

---

## 🛠️ Comandos Principais

### Ambiente

```bash
make bootstrap    # Setup completo (novo desenvolvedor)
make doctor       # Diagnóstico de ambiente
make compose-up   # Inicia infraestrutura Docker
make compose-down # Para infraestrutura
```

### Build (Otimizado)

```bash
make build-fast   # Build rápido sem testes (~30s)
make build        # Build completo com cache (~2-3min)
make clean        # Limpa artifacts
make clean-cache  # Limpa caches do Gradle
```

### Testes (Paralelizados)

```bash
make test-fast        # Testes unitários rápidos (~30s)
make test-unit        # Todos os unit tests (~1-2min)
make test-property    # Property-based tests (~3-5min)
make test-integration # Integration tests (Docker) (~5-10min)
make test-all         # Todos os testes (~10-15min)
```

### Quality Gates

```bash
make verify-fast # Verificação rápida (~1min)
make verify      # Todos os quality gates (~5min)
make verify-full # Gates + integration + coverage (~15min)
```

### Outros

```bash
make seed         # Popula dados demo (idempotente)
make seed-reset   # Limpa e repopula
make migrate      # Executa migrations (Flyway)
make coverage     # Relatório de cobertura agregado
```

📖 Referência completa de comandos: [Makefile](../Makefile)

---

## 📚 Documentos Relacionados

| Documento                                      | Descrição                                              |
| ---------------------------------------------- | ------------------------------------------------------ |
| [ROADMAP.md](./ROADMAP.md)                     | **Roadmap completo e tarefas pendentes (P0→P1→P2→P3)** |
| [BUILD-PERFORMANCE.md](./BUILD-PERFORMANCE.md) | **Guia de otimizações de build e testes**              |
| [QUICKSTART.md](./QUICKSTART.md)               | Começar a desenvolver em 5 minutos                     |
| [AGENTS.md](../AGENTS.md)                      | Governança do projeto e papéis de agentes              |
| [architecture/](./architecture/)               | Documentação de arquitetura                            |
| [testing/](./testing/)                         | Estratégia de testes e cobertura                       |
| [task-plans/](./task-plans/)                   | Planos detalhados por fase P0/P1/P2/P3                 |
| [.kiro/steering/](../.kiro/steering/)          | Convenções técnicas (Java, Git, API, Testes)           |

---

## 🎯 Próximos Milestones

### Milestone 1: Testes Foundation (2 semanas)

- ✅ Setup Testcontainers base
- ✅ Integration tests para módulos core
- ✅ Cross-tenant isolation tests
- 🎯 **Meta:** Cobertura ≥ 75%

### Milestone 2: E2E e CI (2 semanas)

- ✅ Playwright setup e page objects
- ✅ 10+ jornadas críticas E2E
- ✅ CI pipeline com integration + E2E
- 🎯 **Meta:** Pipeline verde completo

### Milestone 3: Módulos P0 Completos (1-2 semanas)

- ✅ Integrations adapters (REST + MCP)
- ✅ Imports DuckDB workflow
- ✅ Analytics infrastructure
- 🎯 **Meta:** Todos os módulos P0 funcionais

### Milestone 4: P0 Release Candidate (1 semana)

- ✅ Todos os quality gates passando
- ✅ Documentação atualizada
- ✅ Demo script completo
- 🎯 **Meta:** MVP pronto para validação
