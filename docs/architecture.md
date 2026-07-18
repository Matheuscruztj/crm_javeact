# Arquitetura Geral — AtlasOps AI

> **Versão:** 1.0  
> **Última atualização:** 2025-01-20

---

## Visão Geral

O AtlasOps AI é um CRM inteligente multi-tenant com análise de documentos via IA local. O sistema é organizado como **monorepo com Gradle multi-project**, onde cada módulo de negócio segue **arquitetura hexagonal** (ports and adapters).

---

## Princípios Arquiteturais

1. **Domínio isolado de frameworks** — A camada `domain/` não importa Spring, JPA, Redis ou qualquer biblioteca externa
2. **Ports definem contratos** — Interfaces em `domain/ports/` descrevem o que o domínio precisa do mundo externo
3. **Adapters são substituíveis** — Implementações em `infrastructure/` podem ser trocadas sem impacto nas regras de negócio
4. **Sem ciclos entre módulos** — Validado automaticamente por ArchUnit
5. **Shared-kernel mínimo** — Apenas tipos base e interfaces compartilhadas entre 2+ módulos
6. **Segurança por design** — JWT stateless, multi-tenancy com isolamento, human-in-the-loop para ações de IA

---

## Estrutura do Repositório

```
atlasops-ai/
├── backend/                    # Gradle multi-project (14 módulos)
│   ├── shared-kernel/          # Tipos base, ports compartilhados, value objects
│   ├── auth/                   # Autenticação JWT, controle de acesso
│   ├── tenants/                # Gestão multi-tenant
│   ├── users/                  # Cadastro de usuários e papéis
│   ├── customers/              # Clientes por tenant
│   ├── documents/              # Upload e gestão de documentos (MinIO)
│   ├── requests/               # Solicitações de serviço
│   ├── pipeline/               # Pipeline de processamento
│   ├── tasks/                  # Gestão de tarefas
│   ├── workflows/              # Engine de workflows
│   ├── ai/                     # Spring AI, RAG, Ollama + pgvector
│   ├── analytics/              # Dashboards e métricas de negócio
│   ├── audit/                  # Log de auditoria
│   └── app-boot/               # Ponto de entrada Spring Boot
├── frontend/                   # React/Next.js SPA
├── infra/                      # Docker Compose, scripts, monitoring
├── docs/                       # ADRs, runbooks, diagramas
├── shared/                     # Artefatos cross-stack
├── .kiro/                      # Specs SDD, steering, skills, hooks
├── Makefile                    # Comandos padronizados
└── AGENTS.md                   # Governança de agentes
```

---

## Arquitetura Hexagonal (por módulo)

```
┌─────────────────────────────────────────────────┐
│                 presentation/                    │
│          (Controllers, Event Consumers)          │
├─────────────────────────────────────────────────┤
│                 application/                     │
│           (Use Cases, Commands, DTOs)            │
├─────────────────────────────────────────────────┤
│                   domain/                        │
│     (Entities, Value Objects, Domain Events)     │
│                 domain/ports/                    │
│          (Interfaces de entrada/saída)           │
├─────────────────────────────────────────────────┤
│                infrastructure/                   │
│       (JPA, Redis, S3, AI Adapters)              │
└─────────────────────────────────────────────────┘
```

**Fluxo de dependência:**

```
presentation → application → domain ← infrastructure
                                ↑
                          shared-kernel
```

---

## Stack Tecnológico

| Camada          | Tecnologia                                        |
| --------------- | ------------------------------------------------- |
| Linguagem       | Java 21                                           |
| Framework       | Spring Boot 3.2+, Spring Framework 6.x            |
| Build           | Gradle (Kotlin DSL, multi-project)                |
| Banco de dados  | PostgreSQL 16 + pgvector                          |
| Cache           | Redis 7                                           |
| Object Storage  | MinIO (S3-compatível)                             |
| IA Local        | Ollama + Spring AI                                |
| Vector Store    | pgvector (embeddings)                             |
| Observabilidade | Prometheus, Grafana, Loki, Micrometer             |
| Testes          | JUnit 5, jqwik, Mockito, ArchUnit, Testcontainers |
| Qualidade       | Spotless, Checkstyle, SpotBugs, Jacoco, OWASP     |
| Containers      | Docker Compose                                    |

---

## RAG Pipeline (Módulo AI)

```
Documento → DocumentChunker → Embeddings (Ollama) → pgvector
                                                        ↓
Query → Embedding → Busca de similaridade (top 5, score ≥ 0.7) → Compose Prompt → Ollama → Resposta
```

**Características:**

- Chunk size: max 1000 tokens, overlap 200 tokens
- Máximo 5 chunks por consulta
- Score mínimo de similaridade: 0.7
- Fallback response se Ollama indisponível
- Human-in-the-loop para ações mutáveis (PendingApproval)

---

## Multi-Tenancy

- Todo recurso pertence a um tenant (isolamento lógico por `tenantId`)
- Header `X-Tenant-ID` obrigatório em requisições autenticadas
- Queries sempre filtradas por tenant
- Sandbox de agentes usa namespace isolado por issue

---

## Segurança

- JWT stateless com Spring Security
- Papéis: OWNER, ADMIN, MANAGER, ANALYST, OPERATOR, VIEWER
- CSRF desabilitado (API REST)
- Variáveis sensíveis via `.env` (gitignored)
- OWASP Dependency-Check bloqueia CRITICAL (CVSS ≥ 9.0)
- Ações de IA mutáveis requerem aprovação humana

---

## Observabilidade

- **Logs:** JSON estruturado com campos obrigatórios (timestamp, level, service, correlationId, tenantId, etc.)
- **Métricas:** Micrometer → Prometheus → Grafana (request rate, latência, erros)
- **Tracing:** Correlation ID propagado via header `X-Correlation-ID` e MDC
- **Health:** Liveness e Readiness probes via Spring Actuator

---

## ADRs (Architecture Decision Records)

Decisões arquiteturais são documentadas em `docs/adr/`:

- [ADR-001: Monorepo com Gradle Multi-Project e Arquitetura Hexagonal](adr/ADR-001-monorepo-gradle-hexagonal.md)

---

## Referências

- Design Document completo: `.kiro/specs/monorepo-sdd-harness/design.md`
- Steering files: `.kiro/steering/`
- AGENTS.md (governança): `AGENTS.md`
