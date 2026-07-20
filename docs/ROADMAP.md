# AtlasOps AI — Roadmap

> **Atualizado em:** 2026-07-20 (Iteração 9 — reorganização pós-MVP)
> **Status:** v1.0.0-rc.1 — MVP COMPLETO
> **Arquivo histórico:** [ROADMAP-DONE.md](./ROADMAP-DONE.md)

---

## 📊 Status Atual

| Fase                | Status      | Tasks                 |
| ------------------- | ----------- | --------------------- |
| P0 Foundation       | ✅ COMPLETO | 22 seções, 100+ tasks |
| P1 Experience       | ✅ COMPLETO | 26 seções             |
| P2 Specialized Data | ✅ COMPLETO | 14 seções             |
| P3 Hardening        | ✅ COMPLETO | 13 seções             |

---

## ✅ Fortalezas Implementadas

- Arquitetura hexagonal completa (19 módulos backend)
- Multi-tenancy com isolamento de dados e enforcement via filtros
- AI/RAG pipeline funcional (Ollama + pgvector + Spring AI)
- JWT auth, CORS, Tenant Authorization Filter, Resource Authorization
- Multipart upload com pause/resume/retry/cancel (arquivos >500MB)
- Redis Streams consumer groups com DLQ e at-least-once delivery
- Approval ledger append-only com hash chain (SHA-256)
- Verifiable audit ledger com `make verify-ledger`
- Circuit breakers (Resilience4j) em Ollama, MinIO e webhooks
- SSRF protection para chamadas HTTP outbound
- Distributed locking (Redis) com fencing token
- Testes E2E Playwright: 20+ jornadas críticas cobertas
- Testes de integração com Testcontainers em 9 módulos
- CI/CD completo: lint, coverage enforcement, ArchUnit, OWASP, secret scan
- Frontend funcional: admin + portal, SSE real-time, command palette (⌘K)
- WCAG AA baseline implementado
- k6 load tests: smoke + average + stress scenarios
- Grafana provisionado com datasources e dashboards automáticos
- Feature flags para adapters especializados (OpenSearch, Neo4j, etc.)
- Nightly pipeline: mutation testing, flaky detection, AI evaluation
- SBOM gerado, imagens Docker reproduzíveis, tag v1.0.0-rc.1

---

## ⚠️ Gaps Conhecidos / Próximas Atividades

- **Checkstyle/SpotBugs warnings** ainda presentes — `make lint` não passa com 0 erros
- **PostGIS/Neo4j/TimescaleDB/ClickHouse/EventStoreDB** são stubs feature-flagged off — end-to-end não funcional
- **OAuth2/SSO** não implementado
- **PostgreSQL Row-Level Security** (P3.1.8) não configurado — apenas descrito no threat model
- **DAST** (dynamic security testing) especificado mas não executado contra API em execução
- **Job de verificação periódica do ledger** não agendado (apenas `make verify-ledger` manual)
- **ADR-011** é duplicata do ADR-001 (hexagonal architecture) — precisa ser renomeado para cobrir tema diferente
- **TextExtractionConsumerTest** usa factory de forma potencialmente incorreta (signature mismatch pode causar falha de compilação)
- **CheckSlaBreachesUseCaseTest** factory method signature precisa verificação

---

## 🎯 Próximo Ciclo de Desenvolvimento

Sugestões para o próximo sprint (ordem de prioridade):

1. Corrigir Checkstyle/SpotBugs warnings (`make lint` deve passar com 0 erros)
2. Renomear ADR-011 para cobrir tema diferente do ADR-001
3. Implementar PostGIS radius queries end-to-end (remover stub)
4. Configurar PostgreSQL Row-Level Security como defense in depth
5. Executar DAST contra API em execução (OWASP ZAP ou similar)
6. Agendar job periódico de verificação do ledger (ex: cron diário)
7. Implementar OAuth2/SSO integration
8. Verificar e corrigir factory method signatures em TextExtractionConsumerTest e CheckSlaBreachesUseCaseTest
9. Aumentar cobertura dos módulos de domínio para ≥85%
10. Implementar Neo4j end-to-end (remover stub, habilitar feature flag)

---

## 📋 Definition of Done

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

## 📚 Documentos Relacionados

| Documento                                      | Descrição                                         |
| ---------------------------------------------- | ------------------------------------------------- |
| [ROADMAP-DONE.md](./ROADMAP-DONE.md)           | Histórico completo de todas as tarefas concluídas |
| [00-current-status.md](./00-current-status.md) | Status atual consolidado do projeto               |
| [STATUS.md](./STATUS.md)                       | Status detalhado por módulo                       |
| [adr/](./adr/)                                 | Architecture Decision Records (ADR-001 a ADR-019) |
| [../AGENTS.md](../AGENTS.md)                   | Governança e papéis de agentes                    |
