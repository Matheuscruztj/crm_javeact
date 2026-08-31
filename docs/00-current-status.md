# AtlasOps AI - Status Atual

> **Atualizado em:** 2026-08-31
> **Fonte canônica:** este arquivo descreve o estado de implementação. O backlog detalhado está em [PENDING-ACTIVITIES.md](./PENDING-ACTIVITIES.md) e o programa de qualidade em [ROADMAP-QUALITY-ENFORCEMENT-WAVES.md](./ROADMAP-QUALITY-ENFORCEMENT-WAVES.md).

## Como interpretar

| Status | Significado |
| --- | --- |
| Concluído | Implementado e com evidência verificável no repositório. |
| Em andamento | Implementado parcialmente ou sem evidência suficiente para encerrar. |
| Pendente | Ainda não implementado ou depende de uma decisão ou ambiente externo. |

## Resumo executivo

| Área | Status | Evidência |
| --- | --- | --- |
| CRM multi-tenant e controle de acesso | Concluído | Módulos `auth`, `tenants`, `users`, `customers` e filtros de autorização. |
| Fluxo principal de clientes, solicitações e aprovações | Concluído | Módulos, controllers e páginas administrativas correspondentes. |
| Documentos e análise local por IA | Concluído | MinIO/presigned upload, Apache Tika, Ollama e pgvector. |
| Processamento assíncrono | Concluído | Worker separado, transactional outbox, Redis Streams e DLQ. |
| Frontend administrativo e portal do cliente | Concluído | Rotas Next.js para operações principais, portal e testes E2E. |
| Observabilidade e operação | Concluído | Prometheus, Grafana, Loki, runbooks e scripts de backup, restore e export. |
| Adapters especializados | Em andamento | OpenSearch, Neo4j, TimescaleDB, ClickHouse e EventStoreDB permanecem feature-flagged. |
| Qualidade final de release | Em andamento | `make verify-fast` passou em 2026-08-31; os gates completos e evidências de release ainda precisam ser fechados. |
| Profiling e carga mínima | Concluído | Runbook repetível de JFR e validação de host enxuto documentados; cenário k6 `low-resource`, baseline versionado e geração de artefatos JSON/HTML disponíveis no repositório. |

## Capacidades implementadas

- Autenticação JWT, rotação de refresh token, revogação de sessão e limitação de taxa.
- Isolamento multi-tenant, perfis ADMIN, ANALYST e CLIENT e auditoria.
- CRUD de clientes, solicitações, comentários, SLA, aprovações e ledger de hash encadeado.
- Upload multipart por URLs pré-assinadas, extração de texto, análise de IA e progresso em tempo real por SSE.
- Notificações in-app e email, busca PostgreSQL com isolamento por tenant, imports CSV e operações de jobs.
- OpenAPI, AsyncAPI, testes unitários/property-based, Playwright, cenários k6, testes de resiliência e profilagem por JFR.

## Em andamento

| Item | Estado atual | Próxima evidência necessária |
| --- | --- | --- |
| Cobertura de testes | O gate agregado permanece em 10%, abaixo das metas do projeto. | Elevar o gate e publicar relatório Jacoco que atinja as metas. |
| Contratos API | OpenAPI é exportado e validado, mas não há verificação automatizada de breaking changes. | Adicionar diff de contrato e cobertura dos endpoints críticos. |
| Integração frontend-API | Tipos manuais e smoke parcial. | Derivar tipos do OpenAPI ou justificar a alternativa; adicionar smoke crítico. |
| Resiliência e performance | Há suites e artefatos, mas o gate de release não é obrigatório. | Vincular relatórios aprovados à promoção de release. |
| Segurança de supply chain | CodeQL, Semgrep, Trivy, Dependency-Check e Gitleaks existem; governança está incompleta. | Definir severidade, ownership, exceções com expiração e resposta a vulnerabilidades. |
| Profiling e capacidade mínima | O roteiro repetível de JFR e k6 de host enxuto já está consolidado no runbook e nos targets do Makefile. | Manter artefatos de execução e revisar budgets conforme a evolução do runtime. |

## Pendências de produto e infraestrutura

- OAuth2/SSO e mitigação completa de prompt injection.
- Validação automatizada de uploads grandes em ambiente próximo de produção.
- Ativação operacional dos adapters especializados, quando a infraestrutura correspondente for aprovada.
- PostgreSQL Row-Level Security, caso seja adotado como camada adicional ao isolamento atual.
- Matriz única de checks, responsáveis, frequência e política `blocking` ou `advisory`.
- Correção dos avisos deprecatórios que impedem compatibilidade futura com Gradle 9.

## Comandos de validação

```bash
make verify-fast          # Validado com sucesso em 2026-08-31
make verify               # Gate completo de backend
make test-integration     # Requer infraestrutura Docker
make test-functional      # Jornadas Playwright
make test-load-smoke      # Smoke k6
```

## Documentos relacionados

| Documento | Uso |
| --- | --- |
| [README.md](./README.md) | Índice da documentação mantida. |
| [ROADMAP.md](./ROADMAP.md) | Direção e prioridades de produto. |
| [PENDING-ACTIVITIES.md](./PENDING-ACTIVITIES.md) | Backlog acionável por prioridade. |
| [ROADMAP-QUALITY-ENFORCEMENT-WAVES.md](./ROADMAP-QUALITY-ENFORCEMENT-WAVES.md) | Backlog técnico de qualidade. |
| [known-limitations.md](./known-limitations.md) | Limitações e decisões de escopo. |
