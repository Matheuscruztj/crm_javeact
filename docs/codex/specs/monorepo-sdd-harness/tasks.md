# Implementation Plan: Monorepo SDD Harness

## Overview

Este plano implementa a fundação técnica do AtlasOps AI: monorepo Gradle multi-project, backend Spring Boot 3.x com arquitetura hexagonal, integração Spring AI com RAG local (Ollama + pgvector), infraestrutura Docker Compose, fluxo SDD, e harness de engenharia com agentes. A implementação segue ordem incremental — estrutura de build primeiro, depois domínio compartilhado, infraestrutura local, módulos de negócio, e finalmente automação e quality gates.

## Tasks

- [x] 1. Estrutura do Monorepo e Build System Gradle
  - [x] 1.1 Criar estrutura de diretórios raiz e configuração Gradle multi-project
    - Criar `settings.gradle.kts` na raiz com `rootProject.name = "atlasops-ai"` e `include()` para todos os 14 módulos backend (shared-kernel, auth, tenants, users, customers, documents, requests, pipeline, tasks, workflows, ai, analytics, audit, app-boot)
    - Criar `gradle.properties` com versões centralizadas: Java 21, Spring Boot 3.2+, jqwik, ArchUnit, e libs compartilhadas
    - Criar `build.gradle.kts` raiz com plugins comuns (spotless, checkstyle, spotbugs, jacoco, owasp-dependencycheck)
    - Criar diretórios: `backend/`, `frontend/`, `infra/`, `docs/`, `shared/`, `.kiro/`
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.8_

  - [x] 1.2 Criar `build.gradle.kts` para cada módulo backend com dependências hexagonais
    - Cada módulo recebe `build.gradle.kts` com dependências explícitas (inter-módulo via `project(":backend:shared-kernel")`)
    - Módulo `app-boot` aplica plugin `org.springframework.boot` e agrega todos os outros
    - Demais módulos usam `java-library` sem plugin Spring Boot
    - Configurar source sets para separar unit tests e integration tests
    - _Requirements: 1.2, 1.3, 1.5, 1.9, 1.10, 2.1_

  - [x] 1.3 Criar estrutura de pacotes hexagonais em cada módulo backend
    - Para cada módulo: criar pacotes `domain/`, `domain/ports/`, `application/`, `infrastructure/`, `presentation/`
    - Criar `package-info.java` em cada pacote definindo namespace
    - _Requirements: 2.1, 2.2, 2.6, 2.9_

- [x] 2. Módulo shared-kernel e tipos base
  - [x] 2.1 Implementar tipos base do domínio compartilhado
    - Criar classes: `Entity<ID>`, `AggregateRoot<ID>`, `ValueObject`, `DomainEvent`
    - Criar interfaces de ports compartilhados: `Clock`, `IdGenerator`, `EventPublisher`
    - Criar value objects reutilizáveis: `TenantId`, `UserId`, `Email`, `CorrelationId`
    - _Requirements: 2.3_

  - [x] 2.2 Implementar validadores e utilitários do shared-kernel
    - Criar validador de feature-name (kebab-case, max 50 chars)
    - Criar utilitário de geração de correlation ID (UUID v4)
    - Criar modelo de erro padronizado (type, title, status, code, detail, traceId)
    - _Requirements: 6.1, 3.11, 13.3_

  - [x] 2.3 Write property test for feature-name validation
    - **Property 9: Spec Feature-Name Validation**
    - **Validates: Requirements 6.1**

  - [x] 2.4 Write property test for correlation ID generation
    - **Property 3: Correlation ID Propagation** (parcial — geração de UUID v4)
    - **Validates: Requirements 3.11, 11.4, 11.5**

- [x] 3. Checkpoint - Validar build e shared-kernel
  - Ensure all tests pass, ask the user if questions arise.

- [x] 4. Infraestrutura Docker Compose
  - [x] 4.1 Criar `docker-compose.yml` com todos os serviços
    - Definir serviços: postgres (16-alpine + pgvector), redis (7-alpine), minio, ollama, prometheus, grafana, loki, mailhog
    - Imagens com tags fixas (sem `latest`)
    - Health checks para cada serviço (interval 10s, timeout 5s, retries 12)
    - `depends_on` com condição `service_healthy`
    - Rede bridge interna nomeada
    - Volumes nomeados para persistência (pgdata, redisdata, miniodata, ollamadata, grafanadata)
    - _Requirements: 1.6, 5.1, 5.2, 5.3, 5.4, 5.8, 5.9_

  - [x] 4.2 Criar `.env.example` e script de inicialização do PostgreSQL
    - `.env.example` com todas as variáveis: APP_PORT, DATABASE_URL, REDIS_URL, OBJECT_STORAGE_ENDPOINT, OBJECT_STORAGE_BUCKET, POSTGRES_PORT, REDIS_PORT, MINIO_PORT, GRAFANA_PORT, PROMETHEUS_PORT, MAILHOG_PORT, OLLAMA_PORT, JWT_ISSUER, JWT_AUDIENCE, LOG_LEVEL
    - Script SQL em `infra/init-db/` para criar banco com extensão pgvector (montado em `/docker-entrypoint-initdb.d/`)
    - _Requirements: 5.5, 5.6_

  - [x] 4.3 Configurar stack de observabilidade (Prometheus, Grafana, Loki)
    - Arquivo `infra/monitoring/prometheus.yml` com scrape config apontando para backend
    - Dashboards pré-configurados em `infra/monitoring/grafana/dashboards/` (request rate, latência p95, taxa de erros, logs por correlationId)
    - Configuração do Loki como datasource no Grafana
    - _Requirements: 11.1, 11.2, 11.7_

- [x] 5. Configuração Spring Boot 3.x (módulo app-boot)
  - [x] 5.1 Configurar application.yml e Spring Boot starter
    - `@SpringBootApplication` com component scan dos módulos
    - Profiles: `local`, `test`, `ci`
    - `application.yml` com configuração de datasource (PostgreSQL + Hikari), Redis (Lettuce), MinIO (AWS S3 SDK)
    - Spring Security com JWT stateless, CSRF desabilitado para API
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

  - [x] 5.2 Implementar validação de variáveis de ambiente no startup
    - Criar `EnvironmentValidator` que valida todas env vars obrigatórias (APP_ENV, APP_PORT, DATABASE_URL, REDIS_URL, OBJECT_STORAGE_ENDPOINT, OBJECT_STORAGE_BUCKET, JWT_ISSUER, JWT_AUDIENCE, LOG_LEVEL)
    - Fail-fast com listagem de todas as variáveis ausentes
    - Timeout de 30s para conexão PostgreSQL
    - _Requirements: 3.7, 3.12_

  - [x] 5.3 Write property test for environment variable validation
    - **Property 1: Environment Variable Validation**
    - **Validates: Requirements 3.7**

  - [x] 5.4 Implementar health checks e endpoints Actuator
    - Liveness: `/actuator/health/liveness` (HTTP 200 se processo ativo)
    - Readiness: `/actuator/health/readiness` (verifica PostgreSQL, Redis, MinIO com timeout 5s cada)
    - Prometheus metrics: `/actuator/prometheus`
    - _Requirements: 3.8, 3.9, 11.2, 11.9_

  - [x] 5.5 Implementar logging estruturado JSON e Correlation ID filter
    - Configurar Logback com JSON encoder (campos: timestamp, level, service, environment, tenantId, actorId, correlationId, traceId, event, resource, duration, errorCode)
    - Criar `CorrelationIdFilter` que extrai `X-Correlation-ID` do header ou gera UUID v4, propaga no MDC e inclui no response header
    - _Requirements: 3.10, 3.11, 11.1, 11.4, 11.5_

  - [x] 5.6 Write property test for structured log format
    - **Property 2: Structured Log Format Invariant**
    - **Validates: Requirements 3.10, 11.1**

  - [x] 5.7 Write property test for correlation ID propagation
    - **Property 3: Correlation ID Propagation**
    - **Validates: Requirements 3.11, 11.4, 11.5**

  - [x] 5.8 Implementar métricas Micrometer/Prometheus
    - Request count por endpoint e método HTTP
    - Histograma de latência (p50, p95, p99)
    - Contagem de erros por código HTTP (4xx, 5xx) e endpoint
    - Health check status por dependência
    - _Requirements: 11.2, 11.3_

- [x] 6. Checkpoint - Validar startup Spring Boot com Docker Compose
  - Ensure all tests pass, ask the user if questions arise.

- [-] 7. Integração Spring AI com RAG Local (módulo ai)
  - [x] 7.1 Implementar ports e domain do módulo AI
    - Criar interface `AIAnalysisPort` (analyze, searchRelevant)
    - Criar interface `DocumentIngestionPort` (ingest)
    - Criar records de domínio: `AnalysisRequest`, `AnalysisResult`, `RelevantChunk`, `IngestionResult`
    - Criar modelo `AIAnalysisRecord` (model, promptVersion, inputHash, durationMs, confidenceScore, fallback, result, chunksUsed)
    - _Requirements: 4.5, 4.8_

  - [x] 7.2 Implementar adapter Ollama e DocumentChunker
    - `OllamaAIAdapter` implementa `AIAnalysisPort` via Spring AI Ollama starter (timeout 120s)
    - `DocumentChunker` divide texto em chunks de max 1000 tokens com overlap de 200 tokens
    - Fallback response com `fallback=true` quando Ollama indisponível
    - _Requirements: 4.1, 4.3, 4.6_

  - [x] 7.3 Write property test for document chunking constraints
    - **Property 4: Document Chunking Constraints**
    - **Validates: Requirements 4.3**

  - [x] 7.4 Implementar PgVectorSearchAdapter e RAG query pipeline
    - `PgVectorSearchAdapter` para busca de similaridade (max 5 chunks, score >= 0.7)
    - Pipeline de consulta: pergunta → busca pgvector → compose prompt → Ollama → resposta com chunk IDs
    - Erro isolado se pgvector indisponível (restante do sistema funciona)
    - _Requirements: 4.2, 4.4, 4.7_

  - [x] 7.5 Write property test for RAG query result bounds
    - **Property 5: RAG Query Result Bounds**
    - **Validates: Requirements 4.4**

  - [-] 7.6 Implementar persistência de análise e versionamento de prompts
    - Persistir `AIAnalysisRecord` para toda análise (model, promptVersion, inputHash SHA-256, durationMs, confidenceScore, fallback, result)
    - Suporte a versionamento de prompts (nome + versão numérica sequencial, seleção de versão ativa)
    - **Status**: Ports/interfaces definidas (AIAnalysisRecordRepository, PromptTemplateRepository), domain records completos. Falta implementação JPA/persistência real e integração com vector store para embeddings no DocumentIngestionAdapter.
    - _Requirements: 4.8, 4.9_

  - [x] 7.7 Write property test for AI analysis record completeness
    - **Property 6: AI Analysis Record Completeness**
    - **Validates: Requirements 4.8**

  - [x] 7.8 Write property test for prompt version selection
    - **Property 7: Prompt Version Selection**
    - **Validates: Requirements 4.9**

  - [x] 7.9 Implementar human-in-the-loop (PendingApproval)
    - Se análise propõe ação mutável (CREATE, UPDATE, DELETE) → criar `PendingApproval` com status PENDING_APPROVAL
    - Campos: analysisId, actionType, targetResource, payload, status
    - Nenhuma mutação executada sem aprovação humana
    - _Requirements: 4.10_

  - [x] 7.10 Write property test for mutable action approval
    - **Property 8: Mutable AI Action Creates Approval**
    - **Validates: Requirements 4.10**

  - [x] 7.11 Implementar rejeição de documentos sem texto
    - Documento submetido sem texto extraível → status FAILED com motivo, sem gerar embeddings
    - _Requirements: 4.11_

- [-] 8. Checkpoint - Validar módulo AI e RAG pipeline
  - Ensure all tests pass, ask the user if questions arise.
  - Note: Task 7.6 still has pending JPA/persistence implementation for AIAnalysisRecordRepository and PromptTemplateRepository, and vector store embedding storage in DocumentIngestionAdapter.

- [x] 9. Fluxo SDD e Harness Engineering
  - [x] 9.1 Implementar validação de specs e estrutura SDD
    - Criar validador de spec completeness: verifica presença de requirements.md, design.md, tasks.md, .config.kiro em cada spec dir
    - Modelo `.config.kiro` com campos: specId, workflowType, specType
    - Validação de feature-name: kebab-case, max 50 caracteres
    - _Requirements: 6.1, 6.2, 6.8_

  - [x] 9.2 Write property test for spec completeness detection
    - **Property 11: Spec Completeness Detection**
    - **Validates: Requirements 6.8**

  - [x] 9.3 Implementar task status e representação markdown
    - Parser de tasks.md: suporta `- [ ]` (todo), `- [x]` (done), `- [ ] [in_progress]`, `- [ ] [blocked]`
    - Transições válidas: todo→in_progress, todo→done, in_progress→done, in_progress→blocked
    - _Requirements: 6.3, 6.4_

  - [x] 9.4 Write property test for task status markdown representation
    - **Property 10: Task Status Markdown Representation**
    - **Validates: Requirements 6.3, 6.4**

  - [x] 9.5 Implementar validação de Agent Task e Sandbox Manager
    - Criar `AgentTaskValidator`: valida campos obrigatórios (objective, context, outOfScope, acceptanceCriteria, affectedInterfaces, risks, requiredTests, validationCommands)
    - Criar `SandboxManager`: provision (branch, DB, compose, bucket), cleanup, validateAccess
    - Naming convention: run*id={issue}-agent-{role}, database=atlasops*{issue}, compose*project=atlasops*{issue}, bucket_prefix={issue}/
    - TTL de 24h, cleanup automático em 5min após conclusão
    - _Requirements: 8.5, 8.7, 9.1, 9.2, 9.3, 9.7_

  - [x] 9.6 Write property test for agent task field validation
    - **Property 12: Agent Task Field Validation**
    - **Validates: Requirements 8.5, 8.7**

  - [x] 9.7 Write property test for sandbox naming convention
    - **Property 14: Sandbox Naming Convention**
    - **Validates: Requirements 9.2**

  - [x] 9.8 Implementar controles de segurança e isolamento
    - Prohibited action enforcement: bloquear acesso a produção, merge em branches protegidas, comandos destrutivos fora de sandbox
    - Cross-sandbox isolation: validar que operações usam exclusivamente namespace do run_id
    - Audit log para tentativas bloqueadas (timestamp, agent, ação, motivo)
    - _Requirements: 8.6, 8.8, 9.4, 9.5_

  - [x] 9.9 Write property test for prohibited action enforcement
    - **Property 13: Prohibited Action Enforcement**
    - **Validates: Requirements 8.6, 8.8**

  - [x] 9.10 Write property test for cross-sandbox isolation
    - **Property 15: Cross-Sandbox Isolation**
    - **Validates: Requirements 9.4**

  - [x] 9.11 Write property test for sandbox destructive command blocking
    - **Property 16: Sandbox Destructive Command Blocking**
    - **Validates: Requirements 9.5**

- [x] 10. Checkpoint - Validar SDD workflow e sandbox isolation
  - Ensure all tests pass, ask the user if questions arise.

- [x] 11. Quality Gates, Makefile e Automação
  - [x] 11.1 Criar Makefile com todos os targets padronizados
    - Targets: bootstrap, verify, test-unit, test-integration, build, compose-up, compose-down, compose-reset, migrate, seed, format, lint, doctor
    - `make help` com descrição de cada target
    - _Requirements: 12.1, 12.6_

  - [x] 11.2 Implementar `make bootstrap` e `make doctor`
    - `bootstrap`: verifica pré-requisitos (Java 21+, Docker 24+, portas livres), instala deps, gera .env, inicia compose, executa migrations
    - `doctor`: verifica Java, Docker, portas, env vars, conectividade com serviços
    - Falha com mensagem clara se pré-requisito não atendido
    - _Requirements: 12.2, 12.3, 12.4_

  - [x] 11.3 Implementar `make verify` e quality gate pipeline
    - Sequência: format-check → lint → compile → unit tests → architecture check → build
    - Interrupção imediata na primeira falha com indicação de etapa, módulo e erro
    - Tempo máximo de 10 min em CI
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 12.5_

  - [x] 11.4 Configurar ArchUnit para validação de arquitetura
    - Teste ArchUnit: ausência de ciclos entre módulos
    - domain não importa infrastructure/presentation
    - application não importa infrastructure/presentation
    - presentation depende apenas de application e shared-kernel
    - _Requirements: 2.4, 2.5, 2.7, 2.8, 10.2_

  - [x] 11.5 Configurar Jacoco, OWASP Dependency-Check e integration tests
    - Jacoco: cobertura mínima 75% linhas, 65% branches (total), 85% linhas (domínio)
    - OWASP: falha em CRITICAL (CVSS >= 9.0)
    - Integration tests via `./gradlew integrationTest` (requer Docker)
    - Mensagem clara se Docker não ativo
    - _Requirements: 10.5, 10.6, 10.7, 10.8_

  - [x] 11.6 Implementar `make seed` com dados idempotentes
    - Popular: 2 tenants (Alpha, Beta), usuários por papel (OWNER, ADMIN, MANAGER, ANALYST, OPERATOR, VIEWER) por tenant, 3+ clientes por tenant, 2+ documentos por cliente
    - Idempotente: múltiplas execuções produzem mesmo estado final sem duplicação
    - _Requirements: 12.7_

  - [x] 11.7 Write property test for seed idempotency
    - **Property 17: Seed Idempotency**
    - **Validates: Requirements 12.7**

  - [x] 11.8 Implementar `make compose-reset`
    - Parar containers, remover volumes (pgdata, redisdata, miniodata), recriar serviços do zero, executar migrations
    - _Requirements: 5.7, 12.8_

- [x] 12. AGENTS.md, Steering Files e Documentação
  - [x] 12.1 Criar AGENTS.md completo na raiz do repositório
    - Seções obrigatórias: Objetivo do Produto, Arquitetura, Módulos, Comandos Disponíveis (tabela), Definição de Pronto, Regras de Segurança, Padrões de Testes
    - Definição de todos os papéis (A1-A11) com responsabilidades, saídas, permissões e restrições
    - Seção "Arquivos Protegidos"
    - Seção "Procedimentos" (criar migration, rodar local, registrar ADR, provar tarefa concluída)
    - Fluxo de orquestração padrão e simplificado
    - _Requirements: 7.1, 7.2, 7.3, 7.4, 8.1, 8.2, 8.3, 8.4_

  - [x] 12.2 Criar steering files em `.kiro/steering/`
    - `java-conventions.md`: nomenclatura, formato de testes, imports
    - `module-structure.md`: template de novo módulo, pacotes obrigatórios, testes mínimos
    - `api-conventions.md`: versionamento, formato de erros, paginação, naming, OpenAPI
    - `testing-patterns.md`: naming, builders, fixtures, isolamento, mocking
    - `git-conventions.md`: Conventional Commits, naming de branches, tamanho de PR, checklist
    - Cada arquivo com data de última atualização e versão no cabeçalho
    - _Requirements: 6.5, 13.1, 13.2, 13.3, 13.4, 13.5, 13.6_

  - [x] 12.3 Criar `docs/00-current-status.md` e estrutura de docs
    - Seções: Última Atualização, Wave Atual, Funcionalidades Concluídas, Quality Gates Ativos, Pendências, Problemas Conhecidos, Próximas Tarefas
    - Diretório `docs/adr/` com ADR-001, `docs/runbooks/`, `docs/diagrams/`
    - _Requirements: 1.7, 7.5, 7.6_

  - [x] 12.4 Criar hooks SDD em `.kiro/hooks/`
    - Hooks JSON para validação automatizada (glob patterns, ações de validação)
    - _Requirements: 6.7_

- [x] 13. Checkpoint final - Validar todos os quality gates
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document using jqwik
- Unit tests validate specific examples and edge cases using JUnit 5 + Mockito
- A linguagem de implementação é Java 21 com Spring Boot 3.2+, Gradle como build tool
- Requirements 14 (Upload) e 15-16 (Frontend) não estão incluídos neste plano pois pertencem a waves posteriores; o foco é a fundação técnica

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["1.2", "4.1", "4.2"] },
    { "id": 2, "tasks": ["1.3", "4.3", "2.1"] },
    { "id": 3, "tasks": ["2.2", "5.1"] },
    { "id": 4, "tasks": ["2.3", "2.4", "5.2", "5.4"] },
    { "id": 5, "tasks": ["5.3", "5.5", "5.8"] },
    { "id": 6, "tasks": ["5.6", "5.7", "7.1"] },
    { "id": 7, "tasks": ["7.2", "7.4", "7.6"] },
    { "id": 8, "tasks": ["7.3", "7.5", "7.7", "7.8", "7.9", "7.11"] },
    { "id": 9, "tasks": ["7.10", "9.1", "9.3"] },
    { "id": 10, "tasks": ["9.2", "9.4", "9.5"] },
    { "id": 11, "tasks": ["9.6", "9.7", "9.8"] },
    { "id": 12, "tasks": ["9.9", "9.10", "9.11", "11.1"] },
    { "id": 13, "tasks": ["11.2", "11.3", "11.4"] },
    { "id": 14, "tasks": ["11.5", "11.6", "11.8"] },
    { "id": 15, "tasks": ["11.7", "12.1", "12.2"] },
    { "id": 16, "tasks": ["12.3", "12.4"] }
  ]
}
```
