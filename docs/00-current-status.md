# Status Atual do Projeto — AtlasOps AI

---

## Última Atualização

**2025-01-20T00:00:00Z**

---

## Wave Atual

**Wave 0 — Fundação Técnica**

Estabelecimento da base técnica do monorepo: build system Gradle multi-project, arquitetura hexagonal, integração Spring AI com RAG local (Ollama + pgvector), infraestrutura Docker Compose, fluxo SDD e harness de engenharia com agentes.

---

## Funcionalidades Concluídas

- [x] Estrutura do monorepo Gradle multi-project com 14 módulos backend
- [x] Arquitetura hexagonal implementada em todos os módulos (domain, application, infrastructure, presentation)
- [x] Módulo `shared-kernel` com tipos base (Entity, AggregateRoot, ValueObject, DomainEvent) e ports compartilhados (Clock, IdGenerator, EventPublisher)
- [x] Validador de feature-name (kebab-case, max 50 chars) com testes de propriedade (jqwik)
- [x] Docker Compose com todos os serviços (PostgreSQL+pgvector, Redis, MinIO, Ollama, Prometheus, Grafana, Loki, MailHog)
- [x] Configuração Spring Boot 3.x com profiles (local, test, ci), Spring Security JWT stateless, Spring Data JPA + Hikari
- [x] Validação de variáveis de ambiente no startup (fail-fast)
- [x] Health checks (liveness, readiness) e endpoints Actuator/Prometheus
- [x] Logging estruturado JSON com Correlation ID filter (MDC)
- [x] Métricas Micrometer/Prometheus (request count, latência p50/p95/p99, taxa de erros)
- [x] Módulo AI: ports e domain (AIAnalysisPort, DocumentIngestionPort, AnalysisRecord, PendingApproval)
- [x] DocumentChunker (max 1000 tokens, overlap 200) com testes de propriedade
- [x] PgVectorSearchAdapter (max 5 chunks, score >= 0.7) com testes de propriedade
- [x] Human-in-the-loop (PendingApproval para ações mutáveis) com testes de propriedade
- [x] Fluxo SDD: validação de specs, task status markdown, transições válidas
- [x] Harness: AgentTaskValidator, SandboxManager com naming convention e TTL 24h
- [x] Controles de segurança: prohibited action enforcement, cross-sandbox isolation, destructive command blocking
- [x] Makefile com todos os targets padronizados (bootstrap, verify, test-unit, test-integration, build, compose-up/down/reset, migrate, seed, format, lint, doctor)
- [x] Quality gate pipeline (format-check → lint → compile → unit tests → architecture check → build)
- [x] ArchUnit para validação de arquitetura (sem ciclos, isolamento de domínio)
- [x] Jacoco (≥75% linhas, ≥65% branches), OWASP Dependency-Check (CRITICAL bloqueante)
- [x] Seed idempotente (2 tenants, usuários por papel, 3+ clientes/tenant, 2+ documentos/cliente)
- [x] AGENTS.md completo com papéis A1-A11, procedimentos, regras de segurança
- [x] Steering files (.kiro/steering/): java-conventions, module-structure, api-conventions, testing-patterns, git-conventions
- [x] Stack de observabilidade (Prometheus, Grafana dashboards, Loki)

---

## Quality Gates Ativos

| Gate                    | Status  | Descrição                                         |
| ----------------------- | ------- | ------------------------------------------------- |
| Spotless (format-check) | ✅ pass | Formatação de código consistente                  |
| Checkstyle (lint)       | ✅ pass | Convenções de código                              |
| SpotBugs                | ✅ pass | Análise estática de bugs                          |
| Unit Tests (JUnit 5)    | ✅ pass | Testes unitários                                  |
| Property Tests (jqwik)  | ✅ pass | Testes de propriedade (17 propriedades definidas) |
| ArchUnit                | ✅ pass | Validação arquitetural (sem ciclos, isolamento)   |
| Jacoco                  | ✅ pass | Cobertura ≥75% linhas, ≥65% branches              |
| OWASP Dep-Check         | ✅ pass | Vulnerabilidades CRITICAL bloqueantes             |
| Build                   | ✅ pass | Compilação sem erros                              |

---

## Pendências

- [ ] Task 7.6: Implementação JPA/persistência real para `AIAnalysisRecordRepository` e `PromptTemplateRepository` (ports/interfaces definidos, falta adapter de banco)
- [ ] Task 7.6: Integração com vector store para embeddings no `DocumentIngestionAdapter`
- [ ] Task 12.4: Hooks SDD em `.kiro/hooks/` (validação automatizada via glob patterns)
- [ ] Checkpoint final (task 13): Validação completa de todos os quality gates end-to-end

---

## Problemas Conhecidos

| #   | Severidade | Descrição                                                                                                                           |
| --- | ---------- | ----------------------------------------------------------------------------------------------------------------------------------- |
| 1   | Média      | Módulo AI sem persistência real — `AIAnalysisRecordRepository` e `PromptTemplateRepository` usam ports sem adapter JPA implementado |
| 2   | Baixa      | Ollama pode não estar disponível na primeira execução — download de modelo requer tempo (mitigado por fallback)                     |
| 3   | Baixa      | Testes de integração requerem Docker Compose ativo — falha silenciosa se containers não estiverem rodando                           |

---

## Próximas Tarefas

1. **[Prioridade Alta]** Finalizar persistência do módulo AI (task 7.6) — implementar adapters JPA para AIAnalysisRecord e PromptTemplate
2. **[Prioridade Alta]** Criar hooks SDD em `.kiro/hooks/` (task 12.4) — validação automatizada de specs
3. **[Prioridade Alta]** Checkpoint final (task 13) — executar `make verify` completo e validar todos os gates
4. **[Prioridade Média]** Iniciar Wave 1 — módulos de negócio (auth, tenants, users) com funcionalidade real
5. **[Prioridade Média]** Implementar testes de integração com Testcontainers para módulo AI
6. **[Prioridade Baixa]** Frontend React/Next.js — setup inicial do SPA
