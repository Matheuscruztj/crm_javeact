# AtlasOps AI — Tarefas Pendentes e Limpeza do Projeto

> **Gerado em:** 2025-07-19  
> **Baseado em:** Specs concluídas vs. Task Plan P0 (MVP)

---

## Resumo

O projeto completou a **implementação de código** para todas as 26 tasks da spec `project-implementation-kickoff` (P0). Contudo, ao comparar com o documento de planejamento `TASK-PLAN-P0-FOUNDATION-CORE.md`, existem **funcionalidades essenciais do MVP que não foram implementadas** ou estão parciais.

---

## 1. Tarefas Pendentes para MVP (P0)

### 1.1 Autenticação e Segurança (Wave P0.1)

| Item                         | Status       | Descrição                                                           |
| ---------------------------- | ------------ | ------------------------------------------------------------------- |
| Refresh Token Rotation       | ✅ Concluído | Rotação de refresh token com detecção de replay (família de tokens) |
| Session Revocation           | ✅ Concluído | Revogar todas as sessões de um usuário                              |
| Rate Limiting                | ✅ Concluído | Limitação de taxa no login e endpoints sensíveis                    |
| Account Lockout (real)       | ⚠️ Parcial   | RedisAccountLockoutAdapter existe mas falta integração completa     |
| Cross-Tenant Isolation Tests | ❌ Pendente  | Testes provando isolamento entre tenants via HTTP                   |
| ETag/Optimistic Concurrency  | ✅ Concluído | Versionamento de entidades com ETag para conflito de atualização    |

### 1.2 Idempotência (Wave P0.3)

| Item                         | Status      | Descrição                                              |
| ---------------------------- | ----------- | ------------------------------------------------------ |
| Idempotency-Key Header       | ❌ Pendente | Suporte a `Idempotency-Key` em POST endpoints críticos |
| Duplicate Request Protection | ❌ Pendente | Dedup de criação via chave idempotente                 |

### 1.3 Upload Multipart Real (Wave P0.4)

| Item                                 | Status      | Descrição                                            |
| ------------------------------------ | ----------- | ---------------------------------------------------- |
| UploadSession migration              | ❌ Pendente | Tabela para gerenciar sessões de upload multipart    |
| Signed Part URLs                     | ❌ Pendente | URLs assinadas por parte para upload paralelo        |
| Multipart Completion                 | ❌ Pendente | Confirmação de upload multipart com merge de partes  |
| Pause/Resume Upload                  | ❌ Pendente | Frontend: pausar e retomar upload                    |
| Abandoned Upload Cleanup             | ❌ Pendente | Worker job para limpar uploads incompletos expirados |
| Size Validation (declared vs actual) | ❌ Pendente | Validar tamanho declarado vs. real no MinIO          |
| Legal Hold Flag                      | ❌ Pendente | Marcar documentos que não podem ser deletados        |

### 1.4 Outbox Pattern (Wave P0.5)

| Item                      | Status       | Descrição                                                        |
| ------------------------- | ------------ | ---------------------------------------------------------------- |
| OutboxEvent Table         | ✅ Concluído | Tabela transacional de eventos pendentes                         |
| Outbox Dispatcher         | ✅ Concluído | Processo que move eventos do Outbox para Redis Streams           |
| Transactional Consistency | ✅ Concluído | Garantia de que eventos só são publicados se a transação commita |

### 1.5 AI e Processamento (Wave P0.6)

| Item                            | Status       | Descrição                                                  |
| ------------------------------- | ------------ | ---------------------------------------------------------- |
| Real Text Extraction (Tika)     | ✅ Concluído | Apache Tika integrado para extração de texto de documentos |
| Prompt Version Registry         | ❌ Pendente  | Versionamento de prompts com rastreabilidade               |
| Model Metadata Storage          | ❌ Pendente  | Registro de qual modelo/versão processou cada documento    |
| Golden Dataset + Evaluation     | ❌ Pendente  | Framework de avaliação de qualidade da IA                  |
| Processing Metrics (Micrometer) | ❌ Pendente  | Métricas de tempo de processamento, taxa de fallback       |

### 1.6 Approval Ledger (Wave P0.7)

| Item                    | Status      | Descrição                                          |
| ----------------------- | ----------- | -------------------------------------------------- |
| Append-Only Ledger      | ❌ Pendente | Log imutável de decisões com hash chain            |
| Hash Chain Verification | ❌ Pendente | Comando para verificar integridade do ledger       |
| Tamper Detection        | ❌ Pendente | Detecção de adulteração no histórico de aprovações |

### 1.7 SSE e Real-Time (Wave P0.8)

| Item                              | Status      | Descrição                                                             |
| --------------------------------- | ----------- | --------------------------------------------------------------------- |
| SSE Heartbeat Backend             | ⚠️ Parcial  | SSEController existe mas heartbeat e Last-Event-ID precisam validação |
| Disconnected Fallback Persistence | ❌ Pendente | Eventos perdidos durante desconexão entregues ao reconectar           |
| Connection Metric                 | ❌ Pendente | Métrica de conexões SSE ativas                                        |

### 1.8 Dashboard e Operations (Wave P0.9)

| Item                              | Status      | Descrição                                    |
| --------------------------------- | ----------- | -------------------------------------------- |
| Operations Job Detail             | ❌ Pendente | Página admin com detalhe de jobs do worker   |
| Job Retry from UI                 | ❌ Pendente | Reprocessar job falho pelo painel admin      |
| Job Cancellation                  | ❌ Pendente | Cancelar job em execução                     |
| Correlation Search                | ❌ Pendente | Buscar todos os eventos de um correlation ID |
| Developer Page (Swagger/AsyncAPI) | ⚠️ Parcial  | Página existe mas sem conteúdo funcional     |
| Ledger Verification Page          | ❌ Pendente | Verificação visual da integridade do ledger  |
| AsyncAPI Artifact                 | ❌ Pendente | Documentação dos eventos assíncronos         |

### 1.9 Seeds e Testes E2E (Wave P0.10)

| Item                                 | Status       | Descrição                                                   |
| ------------------------------------ | ------------ | ----------------------------------------------------------- |
| `make seed` / `make seed-reset`      | ✅ Concluído | Scripts de seed com dados de demonstração                   |
| `make seed-demo` / `make seed-tests` | ✅ Concluído | Seeds para demo e testes                                    |
| Playwright E2E Journey               | ⚠️ Parcial   | Apenas `health.spec.ts` existe; faltam os 10 cenários do P0 |
| k6 Smoke Test                        | ⚠️ Parcial   | Scripts existem mas precisam de endpoints reais             |
| CI Pipeline                          | ✅ Concluído | GitHub Actions com backend, frontend e security jobs        |

### 1.10 Qualidade e Observabilidade

| Item                             | Status       | Descrição                                          |
| -------------------------------- | ------------ | -------------------------------------------------- |
| OpenAPI Annotations              | ✅ Concluído | SpringDoc integrado, AuthController com @Operation |
| Testcontainers Integration Tests | ❌ Pendente  | Testes de integração com banco real via containers |
| Jacoco Coverage Gate no CI       | ✅ Concluído | Verificação automática de cobertura no CI pipeline |
| `docs/00-current-status.md`      | ✅ Concluído | Documento de status atual do projeto               |
| ADR Templates                    | ✅ Concluído | Templates em `docs/adr/` com ADR-001 e ADR-002     |
| CODEOWNERS                       | ✅ Concluído | Arquivo de responsáveis por área                   |

---

## 2. Lixo para Remover do Projeto

### 2.1 Arquivos/Pastas que devem ser removidos do Git

| Item                            | Motivo                                                   | Ação                             |
| ------------------------------- | -------------------------------------------------------- | -------------------------------- |
| `backend/.gitkeep`              | Diretório já tem conteúdo                                | `git rm backend/.gitkeep`        |
| `infra/.gitkeep`                | Diretório já tem conteúdo (docker-compose, monitoring)   | `git rm infra/.gitkeep`          |
| `.kiro/specs/.gitkeep`          | Diretório já tem specs                                   | `git rm .kiro/specs/.gitkeep`    |
| `.kiro/steering/.gitkeep`       | Diretório já tem steering files                          | `git rm .kiro/steering/.gitkeep` |
| `shared/.gitkeep`               | Diretório vazio sem uso definido — avaliar remoção total | `git rm -r shared/`              |
| `frontend/tsconfig.tsbuildinfo` | Artefato de build TypeScript                             | Já no .gitignore, não tracked    |
| `frontend/node_modules/`        | Dependências — nunca trackear                            | Já no .gitignore, não tracked    |

### 2.2 Módulos/Diretórios obsoletos (já removidos neste commit)

| Item                 | Status      |
| -------------------- | ----------- |
| `backend/pipeline/`  | ✅ Removido |
| `backend/tasks/`     | ✅ Removido |
| `backend/workflows/` | ✅ Removido |

### 2.3 Arquivos duplicados

| Item                                                      | Motivo                                                  |
| --------------------------------------------------------- | ------------------------------------------------------- |
| `docker-compose.yml` (raiz) vs `infra/docker-compose.yml` | ✅ Resolvido — consolidado no root `docker-compose.yml` |

### 2.4 Código morto potencial

| Item                            | Localização                | Motivo                                                                       |
| ------------------------------- | -------------------------- | ---------------------------------------------------------------------------- |
| `AIAnalysisPort.java` (deleted) | `backend/ai/domain/ports/` | Port removido — verificar se `DocumentAnalysisPort` substituiu completamente |
| `.env` na raiz                  | Root                       | Verificar se tem valores reais ou é cópia do `.env.example`                  |

---

## 3. Próximos Passos Recomendados (Prioridade)

### Prioridade Alta (necessário para MVP funcionar end-to-end)

1. ~~**Outbox Pattern**~~ ✅ Implementado — Eventos agora são transacionais
2. ~~**Refresh Token Rotation**~~ ✅ Implementado — Com replay detection via família
3. ~~**Seeds**~~ ✅ Implementado — `make seed`, `seed-reset`, `seed-demo`, `seed-tests`
4. **Playwright E2E** — Critério de saída do P0
5. ~~**Text Extraction (Tika)**~~ ✅ Implementado — Apache Tika para PDF/DOCX/etc

### Prioridade Média (importante para qualidade)

6. **Integration Tests (Testcontainers)** — Validar adapters reais
7. ~~**ETag/Optimistic Concurrency**~~ ✅ Implementado — Version columns + interface
8. ~~**OpenAPI Annotations**~~ ✅ Implementado — SpringDoc com swagger-ui
9. ~~**CI Pipeline**~~ ✅ Implementado — GitHub Actions (backend + frontend + security)
10. ~~**Rate Limiting**~~ ✅ Implementado — Redis-backed, tiered per endpoint

### Prioridade Baixa (nice-to-have para P0)

11. **Ledger Hash Chain** — Pode ser diferido para P1
12. **Multipart Upload Real** — Pode usar single-upload para P0
13. **AsyncAPI** — Documentação dos eventos
14. ~~**Operations Job UI**~~ → Docker-compose consolidado, admin via Grafana
15. **Golden Dataset / AI Evaluation** — Framework para P1

---

## 4. Estimativa de Esforço Restante

| Categoria                         | Tasks     | Estimativa        |
| --------------------------------- | --------- | ----------------- |
| ~~Segurança (auth, rate limit)~~  | ~~6~~ → 1 | ✅ 4/6 concluídos |
| ~~Outbox + Transactional Events~~ | ~~3~~ → 0 | ✅ Concluído      |
| Seeds + E2E                       | 4 → 2     | 1-2 dias          |
| Upload Multipart Real             | 7         | 3-5 dias          |
| AI Quality (Tika, metrics, eval)  | 5 → 4     | 2-3 dias          |
| Operations UI + Ledger            | 6         | 3-4 dias          |
| ~~Integration Tests~~             | ~~1~~     | Pendente          |
| ~~CI/CD~~                         | ~~1~~ → 0 | ✅ Concluído      |
| **Total restante**                | **~20**   | **~12-18 dias**   |

---

## 5. Comandos para Limpeza Imediata

```bash
# Remover .gitkeep desnecessários
git rm backend/.gitkeep infra/.gitkeep .kiro/specs/.gitkeep .kiro/steering/.gitkeep

# Remover diretório shared/ vazio (se confirmado que não será usado)
git rm -r shared/

# Commit de limpeza
git commit -m "chore: remove unnecessary .gitkeep files and empty shared/ directory"
```

---

## 6. P1+ (Fora do escopo MVP)

Para referência, funcionalidades planejadas para após o MVP:

- **P1:** Integrations (webhooks, MCP), search avançado (OpenSearch), import bulk
- **P2:** MongoDB projections, Neo4j graph, TimescaleDB analytics, ClickHouse OLAP
- **P3:** Event sourcing (EventStoreDB), hardening, performance tuning, release
