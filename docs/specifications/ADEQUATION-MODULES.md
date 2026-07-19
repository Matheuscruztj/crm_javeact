# Adequação — Módulos Backend

> **Referência:** Seção 2.1 (Modular monolith) da TECHNICAL-SPECIFICATION e Seção 8 (Deliberately excluded functionality) do PROJECT-SCOPE  
> **Estado Atual:** Definido pela spec `monorepo-sdd-harness` e código em `backend/`  
> **Estado Alvo:** Definido por PROJECT-SCOPE e TECHNICAL-SPECIFICATION

---

## Visão Geral

O sistema original (spec `monorepo-sdd-harness`) definia 14 módulos backend: `auth`, `tenants`, `users`, `customers`, `documents`, `requests`, `pipeline`, `tasks`, `workflows`, `ai`, `analytics`, `audit`, `shared-kernel` e `app-boot`.

A visão alvo (PROJECT-SCOPE + TECHNICAL-SPECIFICATION) define 18 módulos + Worker Process: `auth`, `tenants`, `users`, `customers`, `requests`, `documents`, `approvals`, `activities`, `notifications`, `integrations`, `search`, `imports`, `operations`, `ai`, `analytics`, `audit`, `shared-kernel`, `app-boot` e `worker`.

A adequação envolve três categorias de ação:

1. **Remover** — módulos que não fazem parte da Target_Vision (`pipeline`, `tasks`, `workflows`)
2. **Criar** — módulos novos exigidos pela Target_Vision (`approvals`, `activities`, `notifications`, `integrations`, `search`, `imports`, `operations`)
3. **Alterar** — módulos existentes que precisam de refatoração para atender aos novos contratos (`ai`, `documents`, `requests`)

---

## 1. Módulos a Remover

| Aspecto                              | Estado Atual                                                                                                                                                                | Estado Alvo                                                                                                                   | Ação Necessária | Prioridade |
| ------------------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| Módulo `pipeline`                    | Existia em `backend/pipeline/` com estrutura hexagonal; responsável por pipeline comercial de vendas e estágios de negociação                                               | Não existe na Target_Vision — funcionalidade explicitamente excluída em PROJECT-SCOPE seção 8 ("commercial pipeline")         | Remover         | P0         |
| Módulo `tasks`                       | Existia em `backend/tasks/` com estrutura hexagonal; responsável por gerenciamento independente de tarefas de agentes                                                       | Não existe na Target_Vision — funcionalidade explicitamente excluída em PROJECT-SCOPE seção 8 ("independent task management") | Remover         | P0         |
| Módulo `workflows`                   | Existia em `backend/workflows/` com estrutura hexagonal; responsável por designer de workflows e automações                                                                 | Não existe na Target_Vision — funcionalidade explicitamente excluída em PROJECT-SCOPE seção 8 ("workflow designer")           | Remover         | P0         |
| Referências em `settings.gradle.kts` | Entries `include("backend:pipeline")`, `include("backend:tasks")`, `include("backend:workflows")` registradas                                                               | Nenhuma referência a módulos removidos no build system                                                                        | Remover         | P0         |
| Dependências em `app-boot`           | `implementation(project(":backend:pipeline"))`, `implementation(project(":backend:tasks"))`, `implementation(project(":backend:workflows"))` em `app-boot/build.gradle.kts` | Nenhuma dependência a módulos removidos                                                                                       | Remover         | P0         |
| Imports residuais                    | Possíveis imports de `com.atlasops.pipeline`, `com.atlasops.tasks`, `com.atlasops.workflows` em outros módulos                                                              | Zero referências a pacotes de módulos removidos em todo o repositório                                                         | Remover         | P0         |

---

## 2. Módulos a Criar

| Aspecto                | Estado Atual                                                                 | Estado Alvo                                                                                                                                                                                      | Ação Necessária | Prioridade |
| ---------------------- | ---------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | --------------- | ---------- |
| Módulo `approvals`     | Não existia — aprovações estavam embutidas em `requests` sem domínio próprio | Módulo independente com `ApprovalPort`; fluxo de aprovação genérico (documentos, ações MCP/AI, replay destrutivo); statuses PENDING/APPROVED/REJECTED/CANCELLED; event sourcing via EventStoreDB | Criar           | P1         |
| Módulo `activities`    | Não existia — sem registro de atividades ou timeline                         | Módulo independente com `ActivityRepository`; registro de atividades como fatos do sistema; timeline por entidade; feed em real-time via SSE                                                     | Criar           | P1         |
| Módulo `notifications` | Não existia — sem notificações multi-canal                                   | Módulo independente com `NotificationPort`; envio multi-canal (in-app, email, SSE); gerenciamento de preferências por usuário; integração com Worker para envio assíncrono                       | Criar           | P1         |
| Módulo `integrations`  | Não existia — sem conectores externos                                        | Módulo independente com `IntegrationPort`; REST API integration; MCP server externo; webhooks outbound; connectivity test; enable/disable; payload archive em MongoDB; execution history         | Criar           | P1         |
| Módulo `search`        | Não existia — busca textual não implementada                                 | Módulo independente com `SearchPort`; busca unificada cross-entity (customers, requests, documents); modos keyword/semantic/hybrid; command palette; OpenSearch com fallback PostgreSQL tsvector | Criar           | P1         |
| Módulo `imports`       | Não existia — importação de dados não implementada                           | Módulo independente com `ImportPort`; importação de customer CSV; schema inference; preview; validação; row-level errors; processamento assíncrono via Worker; idempotência; DuckDB para parsing | Criar           | P1         |
| Módulo `operations`    | Não existia — sem área de operações administrativa                           | Módulo independente com `OperationsPort`; visualização de jobs/attempts/failures; DLQ management; retry/replay/cancellation; execution details; health checks do sistema                         | Criar           | P1         |

---

## 3. Módulos a Refatorar

| Aspecto                                | Estado Atual                                                                                        | Estado Alvo                                                                                                                                                                                                                                                                                                     | Ação Necessária | Prioridade |
| -------------------------------------- | --------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------- | ---------- |
| Módulo `ai` — Port principal           | `AIAnalysisPort` com contrato simplificado (input: documentId + content; output: resultado textual) | `DocumentAnalysisPort` com contrato enriquecido: input record com tenantId, documentId, extractedText (≤100k chars), promptVersion (formato name:vN), outputSchema; output record com summary, category, extractedFields, risks, missingInformation, confidenceScore (0.0–1.0), providerMetadata, fallback flag | Alterar         | P0         |
| Módulo `ai` — Records de domínio       | Records básicos sem validação rigorosa no compact constructor                                       | `DocumentAnalysisRequest` e `DocumentAnalysisResult` com validações completas (blank checks, regex, range bounds) e imutabilidade de listas via `List.copyOf()`                                                                                                                                                 | Alterar         | P0         |
| Módulo `ai` — Fallback                 | Fallback determinístico existente sem estruturação formal                                           | Fallback determinístico formalizado com flag `fallback=true` no result; circuit breaker pattern; retry 5x com intervalo 30s; health check periódico 60s para reconexão                                                                                                                                          | Alterar         | P2         |
| Módulo `ai` — Worker integration       | Análise executada sincronamente na API                                                              | Análise executada assincronamente pelo Worker Process; API apenas enfileira pedido via Redis Streams; Worker consome e processa                                                                                                                                                                                 | Alterar         | P2         |
| Módulo `documents` — Upload            | Upload simplificado via endpoint REST direto                                                        | Multipart upload direto para MinIO com signed URLs; suporte a arquivos >500 MB; pause/resume/retry/cancel; checksum validation; upload session com expiração                                                                                                                                                    | Alterar         | P2         |
| Módulo `documents` — Processamento     | Processamento síncrono básico                                                                       | Processamento assíncrono via Worker: validação, preview generation, text extraction, AI analysis trigger; statuses com state machine (UPLOADING → PROCESSING → READY → ARCHIVED)                                                                                                                                | Alterar         | P2         |
| Módulo `documents` — Aprovação         | Aprovação embutida no módulo documents                                                              | Aprovação delegada ao módulo `approvals` via port; documents apenas emite evento `document.analyzed.v1`; approvals reage e gerencia o fluxo                                                                                                                                                                     | Alterar         | P2         |
| Módulo `documents` — Legal hold        | Não implementado                                                                                    | Suporte a document legal hold que impede archive/delete enquanto ativo                                                                                                                                                                                                                                          | Alterar         | P3         |
| Módulo `requests` — Statuses           | Status machine básica sem validação de transições                                                   | State machine formal com transições validadas: OPEN → IN_PROGRESS → WAITING_CUSTOMER → COMPLETED/CANCELLED; transições inválidas rejeitadas com `INVALID_STATE_TRANSITION`                                                                                                                                      | Alterar         | P2         |
| Módulo `requests` — SLA                | Sem controle de SLA                                                                                 | SLA básico com deadline configurável por tenant; alertas de SLA breach via módulo `notifications`                                                                                                                                                                                                               | Alterar         | P3         |
| Módulo `requests` — Analyst assignment | Assignment básico sem regras                                                                        | Assignment com validação de role ANALYST; regras de redistribuição e load balancing simples                                                                                                                                                                                                                     | Alterar         | P3         |
| Módulo `requests` — Comments           | Sem sistema de comentários                                                                          | Comentários simples por request; associação a usuário e timestamp; sem threading (flat)                                                                                                                                                                                                                         | Alterar         | P2         |
| Módulo `requests` — ETag/versioning    | Sem controle de concorrência                                                                        | ETag-based conditional updates com `If-Match`; version conflict retorna 412; resource version increment a cada update                                                                                                                                                                                           | Alterar         | P2         |

---

## Resumo por Prioridade

| Prioridade | Descrição                                                           | Contagem |
| ---------- | ------------------------------------------------------------------- | -------- |
| P0         | Remover módulos deprecados e alterar contratos quebrados primeiro   | 8        |
| P1         | Criar scaffolding dos novos módulos com ports e estrutura hexagonal | 7        |
| P2         | Implementar funcionalidade nos módulos novos e refatorados          | 12       |
| P3         | Hardening, polish e funcionalidades complementares                  | 3        |
