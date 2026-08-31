# Requirements Document

## Introduction

Este documento define os requisitos para a adequação do projeto AtlasOps AI à nova visão arquitetural e funcional. O trabalho é dividido em duas frentes principais:

1. **Reestruturação da pasta `docs/`** — Reorganizar os 13 documentos atuais em uma estrutura de subpastas temáticas que sirva como documentação de referência do projeto.

2. **Adequação de módulos e infraestrutura** — Comparar o que está implementado (spec `monorepo-sdd-harness`) com o que a nova visão do projeto exige, definindo um plano de remoção/renomeação ANTES de implementar o que é novo.

A abordagem é: PRIMEIRO remover/alterar o que precisa ser removido/alterado, DEPOIS implementar o que é novo.

## Glossary

- **Docs_Structure**: Organização hierárquica da pasta `docs/` com subpastas temáticas (architecture, specifications, task-plans, runbooks, adr, testing, diagrams)
- **Adequation_Spec**: Documento de especificação que compara o estado atual implementado contra a visão alvo, identificando gaps e ações necessárias
- **Current_Implementation**: O código e infraestrutura existentes conforme definidos na spec `monorepo-sdd-harness` (módulos: auth, tenants, users, customers, documents, requests, pipeline, tasks, workflows, ai, analytics, audit, shared-kernel, app-boot)
- **Target_Vision**: A arquitetura e funcionalidade alvo definida nos documentos 01-PROJECT-SCOPE e 02-TECHNICAL-SPECIFICATION (módulos: auth, tenants, users, customers, requests, documents, approvals, activities, notifications, integrations, search, imports, operations, audit, analytics + shared-kernel + app-boot + worker)
- **Deprecated_Module**: Módulo existente que não faz parte da Target_Vision e deve ser removido ou ter sua responsabilidade absorvida por outro componente (pipeline, tasks, workflows)
- **New_Module**: Módulo que existe na Target_Vision mas não na Current_Implementation (approvals, activities, notifications, integrations, search, imports, operations)
- **Worker_Process**: Processo separado do backend principal que executa processamento assíncrono de documentos, geração de previews, análise de IA, retries, DLQ, notificações, imports, webhooks e projeções especializadas
- **Docker_Profile**: Perfil de Docker Compose que agrupa serviços por categoria (core, advanced, analytics, event-sourcing, observability)
- **Build_System**: Sistema de build Gradle multi-project com settings.gradle.kts e dependências entre módulos
- **Migration_Plan**: Plano ordenado que define a sequência segura de remoções, renomeações e adições no projeto

## Requirements

### Requisito 1: Reestruturação da Pasta docs/

**User Story:** Como desenvolvedor ou agente de IA, eu quero que a documentação do projeto esteja organizada em subpastas temáticas, para que eu consiga localizar rapidamente o documento que preciso sem navegar uma lista plana de 13+ arquivos.

#### Critérios de Aceitação

1. THE Docs_Structure SHALL organizar a pasta `docs/` com as seguintes subpastas obrigatórias: `architecture/`, `specifications/`, `task-plans/`, `runbooks/`, `adr/`, `testing/`, `diagrams/`
2. THE Docs_Structure SHALL mover o documento `01-PROJECT-SCOPE.md` para `docs/specifications/PROJECT-SCOPE.md` preservando conteúdo byte-a-byte idêntico ao original (verificável por comparação de hash SHA-256)
3. THE Docs_Structure SHALL mover o documento `02-TECHNICAL-SPECIFICATION.md` para `docs/specifications/TECHNICAL-SPECIFICATION.md` preservando conteúdo byte-a-byte idêntico ao original
4. THE Docs_Structure SHALL mover o documento `03-SPECIFICATION-PLANNING.md` para `docs/specifications/SPECIFICATION-PLANNING.md` preservando conteúdo byte-a-byte idêntico ao original
5. THE Docs_Structure SHALL mover o documento `04-DESIGN-PLANNING.md` para `docs/architecture/DESIGN-PLANNING.md` preservando conteúdo byte-a-byte idêntico ao original
6. THE Docs_Structure SHALL mover o documento `05-ARCHITECTURE-DIAGRAMS-C4-MERMAID.md` para `docs/diagrams/ARCHITECTURE-DIAGRAMS-C4-MERMAID.md` preservando conteúdo byte-a-byte idêntico ao original
7. THE Docs_Structure SHALL mover os documentos `06-TASK-PLAN-P0-FOUNDATION-CORE.md`, `07-TASK-PLAN-P1-EXPERIENCE-INTEGRATIONS.md`, `08-TASK-PLAN-P2-SPECIALIZED-DATA.md` e `09-TASK-PLAN-P3-HARDENING-RELEASE.md` para `docs/task-plans/` com nomes `TASK-PLAN-P0-FOUNDATION-CORE.md`, `TASK-PLAN-P1-EXPERIENCE-INTEGRATIONS.md`, `TASK-PLAN-P2-SPECIALIZED-DATA.md` e `TASK-PLAN-P3-HARDENING-RELEASE.md` respectivamente, preservando conteúdo byte-a-byte idêntico ao original
8. THE Docs_Structure SHALL mover o documento `10-HARNESS-LOOP-ENGINEERING-AND-AGENTS.md` para `docs/architecture/HARNESS-LOOP-ENGINEERING-AND-AGENTS.md` preservando conteúdo byte-a-byte idêntico ao original
9. THE Docs_Structure SHALL mover o documento `11-DATA-ENTITIES-BY-DATABASE.md` para `docs/architecture/DATA-ENTITIES-BY-DATABASE.md` preservando conteúdo byte-a-byte idêntico ao original
10. THE Docs_Structure SHALL mover o documento `12-QUALITY-TESTING-CICD.md` para `docs/testing/QUALITY-TESTING-CICD.md` preservando conteúdo byte-a-byte idêntico ao original
11. THE Docs_Structure SHALL mover o documento `13-OPERATIONS-RUNBOOK.md` para `docs/runbooks/OPERATIONS-RUNBOOK.md` preservando conteúdo byte-a-byte idêntico ao original
12. THE Docs_Structure SHALL criar um arquivo `docs/README.md` contendo um índice organizado por subpasta, onde cada subpasta é representada como um heading de nível 2 (`##`) e cada documento listado como item de lista com link relativo funcional (e.g., `- [PROJECT-SCOPE](specifications/PROJECT-SCOPE.md)`)
13. THE Docs_Structure SHALL preservar o subdiretório `docs/adr/` já existente com todo o seu conteúdo inalterado, sem recriá-lo nem sobrescrever seus arquivos
14. IF algum documento `.md` permanecer na raiz de `docs/` após a aplicação dos mapeamentos dos critérios 2 a 11 (excluindo `docs/README.md`), THEN THE Docs_Structure SHALL movê-lo para a subpasta cuja descrição temática no `docs/README.md` mais se aproxima do título (heading H1) do documento, e adicioná-lo ao índice do `docs/README.md`
15. WHEN todas as movimentações forem concluídas, THE Docs_Structure SHALL garantir que a raiz `docs/` contenha apenas o arquivo `README.md` e as 7 subpastas listadas no critério 1, sem nenhum outro arquivo `.md` solto na raiz

---

### Requisito 2: Remoção dos Módulos Deprecados

**User Story:** Como arquiteto, eu quero remover os módulos que não fazem parte da visão alvo do projeto, para que o código fonte reflita apenas a arquitetura planejada e não carregue abstrações abandonadas.

#### Critérios de Aceitação

1. WHEN a remoção dos módulos deprecados for executada, THE Build_System SHALL remover o diretório `backend/pipeline/` e sua entrada `"backend:pipeline"` do arquivo `settings.gradle.kts`
2. WHEN a remoção dos módulos deprecados for executada, THE Build_System SHALL remover o diretório `backend/tasks/` e sua entrada `"backend:tasks"` do arquivo `settings.gradle.kts`
3. WHEN a remoção dos módulos deprecados for executada, THE Build_System SHALL remover o diretório `backend/workflows/` e sua entrada `"backend:workflows"` do arquivo `settings.gradle.kts`
4. WHEN um Deprecated_Module for removido do `settings.gradle.kts`, THE Build_System SHALL remover a linha `implementation(project(":backend:{módulo}"))` correspondente do arquivo `backend/app-boot/build.gradle.kts`
5. WHEN um Deprecated_Module for removido, THE Build_System SHALL verificar que nenhum outro módulo possui declaração de import de pacote `com.atlasops.{módulo removido}` em arquivos `.java`, nem declaração `implementation(project(":backend:{módulo removido}"))` em arquivos `build.gradle.kts`, removendo qualquer referência encontrada
6. WHEN todos os Deprecated_Modules forem removidos, THE Build_System SHALL compilar com sucesso via `./gradlew build` retornando exit code 0 sem erros de compilação
7. WHEN um Deprecated_Module for removido, THE Build_System SHALL remover a linha correspondente ao módulo na tabela "Módulos" do arquivo `AGENTS.md`
8. IF um Deprecated_Module contiver arquivos `.java` além de `package-info.java` em qualquer subpacote (`domain/`, `application/`, `infrastructure/`, `presentation/`), THEN THE Build_System SHALL interromper a remoção daquele módulo e reportar a lista de arquivos que necessitam realocação antes de prosseguir
9. WHEN todos os Deprecated_Modules forem removidos, THE Build_System SHALL atualizar o campo de contagem de módulos na seção "Estrutura do repositório" do `AGENTS.md` para refletir o total de módulos restantes

---

### Requisito 3: Criação dos Novos Módulos Backend

**User Story:** Como desenvolvedor, eu quero que os novos módulos da visão alvo existam com estrutura hexagonal padronizada, para que o desenvolvimento de cada domínio possa começar imediatamente após a adequação.

#### Critérios de Aceitação

1. THE Build_System SHALL criar o módulo `approvals` em `backend/approvals/` com estrutura hexagonal completa (pacotes `domain/`, `domain/ports/`, `application/`, `infrastructure/`, `presentation/` com `package-info.java` em cada pacote, e diretórios de teste espelhados em `src/test/java/com/atlasops/approvals/`), usando pacote base `com.atlasops.approvals`
2. THE Build_System SHALL criar o módulo `activities` em `backend/activities/` com estrutura hexagonal completa (mesma estrutura de pacotes e testes do critério 1), usando pacote base `com.atlasops.activities`
3. THE Build_System SHALL criar o módulo `notifications` em `backend/notifications/` com estrutura hexagonal completa (mesma estrutura de pacotes e testes do critério 1), usando pacote base `com.atlasops.notifications`
4. THE Build_System SHALL criar o módulo `integrations` em `backend/integrations/` com estrutura hexagonal completa (mesma estrutura de pacotes e testes do critério 1), usando pacote base `com.atlasops.integrations`
5. THE Build_System SHALL criar o módulo `search` em `backend/search/` com estrutura hexagonal completa (mesma estrutura de pacotes e testes do critério 1), usando pacote base `com.atlasops.search`
6. THE Build_System SHALL criar o módulo `imports` em `backend/imports/` com estrutura hexagonal completa (mesma estrutura de pacotes e testes do critério 1), usando pacote base `com.atlasops.imports`
7. THE Build_System SHALL criar o módulo `operations` em `backend/operations/` com estrutura hexagonal completa (mesma estrutura de pacotes e testes do critério 1), usando pacote base `com.atlasops.operations`
8. WHEN um New_Module for criado, THE Build_System SHALL registrá-lo em `settings.gradle.kts` via `include()` e criar um `build.gradle.kts` com plugin `java-library` e dependência `api(project(":backend:shared-kernel"))`
9. WHEN todos os New_Modules forem criados, THE Build_System SHALL compilar com sucesso via `./gradlew build` retornando exit code 0 em no máximo 120 segundos
10. THE Build_System SHALL atualizar o AGENTS.md adicionando cada New_Module na tabela de módulos com uma descrição de responsabilidade de no máximo 150 caracteres, coerente com o nome do módulo (approvals: fluxos de aprovação; activities: registro de atividades; notifications: envio de notificações; integrations: conectores com sistemas externos; search: busca unificada; imports: importação de dados em lote; operations: operações administrativas e monitoramento)
11. WHEN um New_Module for criado, THE Build_System SHALL incluir pelo menos uma interface de port em `domain/ports/` que declare no mínimo um método e cujo nome siga a convenção `{Nome}Port` ou `{Nome}Repository`
12. IF um diretório de New_Module já existir em `backend/`, THEN THE Build_System SHALL interromper a criação daquele módulo e emitir mensagem de erro indicando que o módulo já existe

---

### Requisito 4: Scaffolding do Worker Process

**User Story:** Como arquiteto, eu quero que o Worker exista como processo separado no repositório, para que o processamento assíncrono pesado (documentos, previews, IA, imports) não concorra com a API principal por recursos.

#### Critérios de Aceitação

1. THE Build_System SHALL criar o diretório `backend/worker/` como um módulo Spring Boot separado com seu próprio `build.gradle.kts` aplicando os plugins `org.springframework.boot` e `io.spring.dependency-management`
2. THE Worker_Process SHALL declarar dependências de implementação para os módulos `shared-kernel`, `documents`, `ai` e outros módulos de domínio existentes no `settings.gradle.kts` que sejam necessários ao processamento assíncrono, e SHALL compilar independentemente do módulo `app-boot` via `./gradlew :backend:worker:build`
3. THE Worker_Process SHALL conter uma classe principal anotada com `@SpringBootApplication` em `com.atlasops.worker`
4. THE Build_System SHALL registrar o módulo `backend:worker` em `settings.gradle.kts` e SHALL compilar ambos os módulos (app-boot e worker) com sucesso via `./gradlew build` sem erros de compilação
5. THE Worker_Process SHALL incluir um arquivo `application.yml` contendo propriedades de conexão para Redis (host, port, consumer group name), PostgreSQL (url, username, password via variáveis de ambiente) e MinIO (endpoint, access-key, secret-key via variáveis de ambiente)
6. WHEN o Docker Compose for iniciado, THE Worker_Process SHALL estar incluído como serviço separado do serviço backend API na mesma rede `atlasops-network`, com dependência declarada nos serviços `postgres`, `redis` e `minio`, e com health check configurado
7. IF o comando `./gradlew :backend:worker:build` for executado sem que o módulo `app-boot` esteja compilado, THEN THE Build_System SHALL completar a compilação do worker com sucesso em no máximo 120 segundos

---

### Requisito 5: Adequação do Docker Compose com Profiles

**User Story:** Como desenvolvedor, eu quero que o Docker Compose suporte múltiplos profiles (core, advanced, analytics, event-sourcing, observability), para que eu possa levantar apenas os serviços necessários para o contexto de trabalho atual.

#### Critérios de Aceitação

1. THE Docker_Profile SHALL definir o profile `core` contendo: PostgreSQL (com pgvector e PostGIS), Redis, MinIO, backend API e Worker_Process
2. THE Docker_Profile SHALL definir o profile `advanced` contendo: OpenSearch, MongoDB e Neo4j, além de todos os serviços do profile core
3. THE Docker_Profile SHALL definir o profile `analytics` contendo: TimescaleDB e ClickHouse, além de todos os serviços do profile core
4. THE Docker_Profile SHALL definir o profile `event-sourcing` contendo: EventStoreDB, além de todos os serviços do profile core
5. THE Docker_Profile SHALL definir o profile `observability` contendo: Prometheus, Grafana, Loki, Tempo e MailHog, além de todos os serviços do profile core
6. THE Build_System SHALL fornecer comandos Make para cada profile: `make compose-core`, `make compose-advanced`, `make compose-analytics`, `make compose-event-sourcing`, `make compose-observability`, `make compose-all`
7. WHEN o comando `make compose-core` for executado, THE Docker_Profile SHALL iniciar apenas os serviços do profile core com health checks e todos os serviços SHALL atingir estado healthy em no máximo 120 segundos
8. IF um profile não-core for iniciado sem o profile core ativo, THEN THE Docker_Profile SHALL iniciar automaticamente os serviços core como dependência via `depends_on` com condição `service_healthy`
9. THE Docker_Profile SHALL manter health checks para todos os serviços especializados (OpenSearch, MongoDB, Neo4j, TimescaleDB, ClickHouse, EventStoreDB) com intervalo de 10 segundos e timeout de 5 segundos
10. IF qualquer serviço de um profile falhar no health check após esgotar as retries (12 tentativas), THEN THE Docker_Profile SHALL manter o serviço no estado unhealthy sem reinício automático, permitindo diagnóstico via `docker compose logs <serviço>`

---

### Requisito 6: Adequação do Módulo AI para Nova Arquitetura

**User Story:** Como desenvolvedor, eu quero que o módulo AI siga o novo contrato de port (DocumentAnalysisPort) da visão alvo, para que a interface seja consistente com a especificação técnica e suporte o novo Worker_Process.

#### Critérios de Aceitação

1. THE Current_Implementation SHALL expor uma interface `DocumentAnalysisPort` no pacote `domain/ports` cujo método de análise aceite como input um record contendo: tenant ID (String não-vazio), document ID (String não-vazio), extracted text (String não-vazio, máximo 100.000 caracteres), prompt version (String no formato "name:vN") e output schema (String não-vazio identificando o schema esperado da resposta)
2. THE Current_Implementation SHALL garantir que o output da análise retornado por `DocumentAnalysisPort` seja um record contendo: summary (String não-vazio), category (String não-vazio), extracted fields (lista de pares chave-valor), risks (lista de Strings), missing information (lista de Strings), confidence score (double entre 0.0 e 1.0), provider metadata (String identificando modelo e versão utilizados) e fallback flag (boolean indicando se resposta veio de fallback)
3. IF a interface `AIAnalysisPort` existente for renomeada para `DocumentAnalysisPort`, THEN THE Current_Implementation SHALL atualizar todas as referências internas no módulo AI (adapters, use cases, testes) para utilizar o novo nome sem deixar imports quebrados
4. THE Current_Implementation SHALL manter a interface `DocumentIngestionPort` existente com a mesma assinatura de método `ingest(String documentId, String content)` retornando `IngestionResult`, sem alteração de contrato
5. WHEN o módulo AI for compilado via `./gradlew :backend:ai:build`, THE Build_System SHALL retornar exit code 0 sem warnings de compilação
6. WHEN os testes de propriedade (PBT) do módulo AI forem executados, THE Build_System SHALL reportar todos os testes de propriedade existentes passando com status verde

---

### Requisito 7: Adequação do Sistema de Persistência

**User Story:** Como arquiteto, eu quero que o sistema declare e configure os adapters para as novas tecnologias de persistência da visão alvo, para que cada base de dados especializada tenha um ponto de integração definido.

#### Critérios de Aceitação

1. THE Current_Implementation SHALL manter PostgreSQL como source of truth transacional com suporte a pgvector e PostGIS
2. THE Target_Vision SHALL declarar, para cada base de dados especializada, uma interface de port em `domain/ports/` do módulo consumidor e um adapter stub em `infrastructure/` que implemente o port: OpenSearch (busca textual e autocomplete), MongoDB (archive de execuções REST/MCP), Neo4j (projeções de relacionamento), TimescaleDB (métricas operacionais selecionadas), ClickHouse (projeção analítica histórica), DuckDB (processamento temporário de CSV/Parquet) e EventStoreDB (event sourcing de Approvals)
3. WHEN um adapter de banco especializado for implementado, THE Build_System SHALL colocá-lo exclusivamente na camada `infrastructure/` do módulo que o utiliza, com port correspondente em `domain/ports/`
4. THE Target_Vision SHALL tratar cada base de dados especializada como opcional em runtime, habilitável por propriedade de configuração Spring (`atlasops.{database}.enabled=true|false`) associada ao Docker_Profile correspondente
5. IF uma base de dados especializada estiver indisponível (conexão recusada ou timeout de conexão superior a 5 segundos), THEN THE Current_Implementation SHALL degradar conforme o seguinte mapeamento: OpenSearch → fallback para text search PostgreSQL com LIKE/tsvector; MongoDB → metadata persiste em PostgreSQL e archive enfileira retry com no máximo 5 tentativas em intervalos de 30 segundos; Neo4j → write principal sucede normalmente e projeção de grafo fica stale até reconexão; TimescaleDB/ClickHouse → endpoints de charts retornam indicação de dados indisponíveis e ingestão enfileira retry com no máximo 5 tentativas em intervalos de 30 segundos; DuckDB → processamento de CSV/Parquet retorna erro indicando serviço temporariamente indisponível; AI → fallback determinístico conforme definido no módulo ai
6. IF o número máximo de tentativas de retry for atingido para qualquer base especializada, THEN THE Current_Implementation SHALL registrar o evento em log de nível ERROR com o nome do adapter e o timestamp, e SHALL cessar novas tentativas até que um health check periódico (intervalo de 60 segundos) detecte a reconexão
7. WHEN uma base de dados especializada anteriormente indisponível voltar a responder ao health check, THEN THE Current_Implementation SHALL retomar automaticamente o fluxo normal de escrita/leitura no adapter correspondente em até 60 segundos

---

### Requisito 8: Adequação do Frontend (Scaffolding)

**User Story:** Como desenvolvedor frontend, eu quero que a estrutura base do frontend React/Next.js exista no repositório com as páginas da visão alvo definidas, para que o desenvolvimento de UI possa começar em paralelo ao backend.

#### Critérios de Aceitação

1. THE Build_System SHALL criar a estrutura do frontend em `frontend/` contendo no mínimo: `package.json` com dependências React, Next.js e TypeScript, `tsconfig.json` com strict mode habilitado, `next.config.ts` configurado para App Router, e o diretório `app/` como raiz de rotas
2. THE Build_System SHALL definir as rotas admin criando um arquivo `page.tsx` que exporta um React component default para cada um dos seguintes paths dentro de `app/`: `/admin/dashboard`, `/admin/customers`, `/admin/customers/[id]`, `/admin/requests`, `/admin/requests/[id]`, `/admin/documents`, `/admin/documents/[id]`, `/admin/approvals`, `/admin/search`, `/admin/activities`, `/admin/operations`, `/admin/integrations`, `/admin/imports`, `/admin/audit`, `/admin/settings`, `/developers`
3. THE Build_System SHALL definir as rotas do portal cliente criando um arquivo `page.tsx` que exporta um React component default para cada um dos seguintes paths dentro de `app/`: `/portal/home`, `/portal/requests`, `/portal/requests/[id]`, `/portal/documents`, `/portal/documents/upload`, `/portal/notifications`
4. THE Build_System SHALL configurar pnpm como package manager com um arquivo `pnpm-workspace.yaml` na raiz do monorepo que inclua `frontend/` como membro do workspace e um campo `packageManager` no `package.json` raiz especificando a versão do pnpm
5. WHEN o comando `pnpm --filter frontend build` for executado, THE Build_System SHALL compilar o frontend com zero erros de TypeScript e gerar o output em `frontend/.next/` em no máximo 120 segundos
6. WHEN o comando `pnpm --filter frontend build` falhar por erro de TypeScript ou dependência ausente, THE Build_System SHALL exibir a mensagem de erro indicando o arquivo e a linha do problema
7. THE Build_System SHALL configurar Tailwind CSS v4 como framework de estilização com o arquivo `tailwind.config.ts` contendo: content paths para `app/` e `components/`, tema estendido com cores do design system (primary, secondary, destructive, muted, accent, card, popover) e breakpoints responsivos padrão (sm: 640px, md: 768px, lg: 1024px, xl: 1280px, 2xl: 1536px)
8. THE Build_System SHALL inicializar shadcn/ui como biblioteca de componentes com: `components.json` configurado apontando para `components/ui/`, style "default", tema com CSS variables habilitado, e pelo menos os seguintes componentes base instalados: button, input, card, dialog, dropdown-menu, table, badge, toast, tabs, separator, skeleton
9. THE Build_System SHALL organizar os componentes em `frontend/components/` com a seguinte estrutura: `ui/` (componentes shadcn/ui gerados), `shared/` (componentes reutilizáveis compostos como layouts, navigation, data-table), `admin/` (componentes específicos do admin) e `portal/` (componentes específicos do portal cliente)
10. THE Build_System SHALL implementar layouts responsivos com as seguintes regras: desktop (≥1024px) usa sidebar de navegação persistente e tabelas densas; tablet (768px–1023px) usa sidebar colapsável e filtros em drawer; mobile (<768px) usa bottom navigation no portal cliente e sidebar em overlay no admin, com tabelas adaptadas para cards empilhados
11. THE Build_System SHALL criar um layout admin em `app/admin/layout.tsx` e um layout portal em `app/portal/layout.tsx`, ambos usando componentes shadcn/ui para sidebar, header e breadcrumbs, com suporte a navegação responsiva via Tailwind CSS breakpoints
12. THE Build_System SHALL definir tokens de design como CSS variables em `app/globals.css` seguindo a convenção shadcn/ui (--background, --foreground, --primary, --secondary, --muted, --accent, --destructive, --border, --input, --ring, --radius) com suporte a dark mode via classe `.dark` no `<html>`

---

### Requisito 9: Adequação da Infraestrutura de Testes

**User Story:** Como engenheiro de qualidade, eu quero que a infraestrutura de testes inclua suporte a Playwright (testes funcionais) e k6 (testes de carga), para que a cobertura de testes atenda à visão alvo do projeto.

#### Critérios de Aceitação

1. THE Build_System SHALL criar o diretório `tests/functional/` contendo um arquivo de configuração Playwright que defina pelo menos um projeto de browser (chromium), a base URL da aplicação local, e um timeout global de no máximo 30 segundos por teste
2. THE Build_System SHALL criar o diretório `tests/load/` contendo pelo menos um script k6 executável que defina cenários de carga com stages configuráveis (VUs e duração)
3. THE Build_System SHALL fornecer os comandos Make: `make test-functional`, `make test-functional-headed`, `make test-functional-report`, `make test-load-smoke`, `make test-load`, `make test-load-report`
4. WHEN qualquer comando Make de teste (`make test-functional`, `make test-functional-headed`, `make test-load-smoke`, `make test-load`) for executado e pelo menos um teste falhar, THE Build_System SHALL retornar exit code diferente de zero
5. WHEN o comando `make test-load-smoke` for executado, THE Build_System SHALL executar um cenário de carga mínima com no máximo 5 VUs simultâneos por no máximo 60 segundos contra a API local na porta definida em variável de ambiente
6. THE Build_System SHALL configurar Playwright para capturar screenshots, vídeos e traces automaticamente quando um teste terminar com status de falha, armazenando os artefatos no diretório `tests/functional/test-results/`
7. WHEN o comando `make test-functional-report` ou `make test-load-report` for executado, THE Build_System SHALL gerar um relatório em formato HTML no diretório `tests/functional/playwright-report/` ou `tests/load/reports/` respectivamente

---

### Requisito 10: Criação dos Documentos de Adequação (Gap Analysis)

**User Story:** Como líder técnico, eu quero documentos de adequação que comparem cada área funcional entre o estado atual e a visão alvo, para que a equipe saiba exatamente o que remover, alterar e adicionar em cada frente.

#### Critérios de Aceitação

1. THE Adequation_Spec SHALL criar o documento `docs/specifications/ADEQUATION-MODULES.md` contendo uma tabela comparativa com no mínimo uma entrada para cada módulo a remover (pipeline, tasks, workflows), cada módulo a criar (approvals, activities, notifications, integrations, search, imports, operations) e cada módulo a refatorar (ai, documents, requests), referenciando como "Estado Atual" o código existente no diretório `backend/` e a spec `monorepo-sdd-harness`, e como "Estado Alvo" os documentos PROJECT-SCOPE e TECHNICAL-SPECIFICATION
2. THE Adequation_Spec SHALL criar o documento `docs/specifications/ADEQUATION-INFRASTRUCTURE.md` contendo uma tabela comparativa com no mínimo uma entrada para cada novo serviço Docker Compose, cada novo profile (core, advanced, analytics, event-sourcing, observability), o Worker_Process, cada novo comando Make e cada nova variável de ambiente
3. THE Adequation_Spec SHALL criar o documento `docs/specifications/ADEQUATION-PERSISTENCE.md` contendo uma tabela comparativa com no mínimo uma entrada para cada nova tecnologia de banco (OpenSearch, MongoDB, Neo4j, TimescaleDB, ClickHouse, DuckDB, EventStoreDB), incluindo para cada uma: adapter necessário, módulo consumidor, comportamento de fallback de degradação e lifecycle de projeções
4. THE Adequation_Spec SHALL criar o documento `docs/specifications/ADEQUATION-API-EVENTS.md` contendo uma tabela comparativa com no mínimo uma entrada para cada novo contrato REST, cada novo evento de domínio, cada SSE stream, cada webhook outbound e a MCP integration, referenciando como "Estado Alvo" a Target_Vision
5. THE Adequation_Spec SHALL criar o documento `docs/specifications/ADEQUATION-FRONTEND.md` contendo uma tabela comparativa com no mínimo uma entrada para cada página admin e portal definidas na Target_Vision, e para cada componente de infraestrutura compartilhada (upload manager, notification center, command palette, document preview, design system)
6. THE Adequation_Spec SHALL criar o documento `docs/specifications/ADEQUATION-TESTING.md` contendo uma tabela comparativa com no mínimo uma entrada para: configuração Playwright, cada cenário funcional do end-to-end journey principal, configuração k6, cada cenário de carga (smoke, average, stress) e seed scripts
7. WHEN um documento de adequação for criado, THE Adequation_Spec SHALL usar formato de tabela comparativa com exatamente 5 colunas: Aspecto, Estado Atual, Estado Alvo, Ação Necessária, Prioridade — onde "Estado Atual" descreve o que existe no código/infraestrutura implementado, "Estado Alvo" descreve o que a Target_Vision exige, e "Ação Necessária" indica uma das categorias: Remover, Alterar, Criar ou Manter
8. THE Adequation_Spec SHALL classificar cada linha de ação nas tabelas com exatamente uma das prioridades: P0 (remover/alterar primeiro), P1 (criar scaffolding), P2 (implementar funcionalidade), P3 (hardening e polish)
9. WHEN todos os 6 documentos de adequação forem criados, THE Adequation_Spec SHALL garantir que cada módulo, serviço ou componente mencionado na Target_Vision aparece em pelo menos um dos documentos, sem itens órfãos entre documentos

---

### Requisito 11: Atualização do AGENTS.md e Steering Files

**User Story:** Como agente de IA ou desenvolvedor, eu quero que o AGENTS.md e os steering files reflitam a nova estrutura do projeto após a adequação, para que toda orientação esteja atualizada e consistente.

#### Critérios de Aceitação

1. WHEN os Deprecated_Modules (pipeline, tasks, workflows) forem removidos e os New_Modules (approvals, activities, notifications, integrations, search, imports, operations) criados, THE Build_System SHALL atualizar a tabela de módulos no AGENTS.md para listar apenas os módulos presentes no `settings.gradle.kts` da Target_Vision
2. WHEN o Worker_Process for criado, THE Build_System SHALL adicionar uma seção de nível 3 (`###`) no AGENTS.md após a seção "Módulos", com título "Worker Process", contendo: propósito (1 parágrafo), responsabilidades (lista com no mínimo 5 itens), comando de execução (`./gradlew :backend:worker:bootRun`), e relação com o backend API (1 parágrafo explicando separação de processos)
3. THE Build_System SHALL atualizar a tabela de comandos no AGENTS.md adicionando os seguintes comandos com suas descrições: `make compose-core`, `make compose-advanced`, `make compose-analytics`, `make compose-event-sourcing`, `make compose-observability`, `make compose-all`, `make test-functional`, `make test-load-smoke`, `make test-load`
4. THE Build_System SHALL atualizar o steering file `module-structure.md` substituindo referências a módulos `pipeline`, `tasks` e `workflows` pelos novos módulos `approvals`, `activities`, `notifications`, `integrations`, `search`, `imports` e `operations`
5. THE Build_System SHALL atualizar o steering file `git-conventions.md` adicionando os scopes `approvals`, `activities`, `notifications`, `integrations`, `search`, `imports`, `operations` e `worker` à lista de scopes comuns de módulos, e removendo os scopes `pipeline`, `tasks` e `workflows`
6. WHEN o frontend for scaffolded, THE Build_System SHALL criar um steering file `frontend-conventions.md` em `.kiro/steering/` contendo no mínimo as seguintes seções:
   - **Stack**: React 19, Next.js 15 (App Router), TypeScript strict, Tailwind CSS v4, shadcn/ui
   - **Estrutura de diretórios**: `app/` (rotas), `components/ui/` (shadcn/ui), `components/shared/` (compostos), `components/admin/`, `components/portal/`, `lib/` (utils, API client, hooks), `hooks/` (custom hooks)
   - **Naming**: componentes em PascalCase, arquivos de componente em kebab-case, hooks prefixados com `use`, constantes em UPPER_SNAKE_CASE
   - **Estilização**: uso exclusivo de Tailwind CSS utility classes, proibição de CSS modules ou styled-components, uso de `cn()` (clsx + tailwind-merge) para composição condicional de classes, variáveis CSS para tokens de design seguindo convenção shadcn/ui
   - **Componentes**: sempre usar componentes shadcn/ui como base antes de criar custom, composição sobre herança, props tipadas com interface dedicada, evitar prop drilling (usar context ou zustand para estado compartilhado)
   - **Responsividade**: mobile-first approach, breakpoints Tailwind padrão (sm/md/lg/xl/2xl), layouts adaptativos obrigatórios em todas as páginas, portal cliente deve ser totalmente usável em mobile, admin deve ser inspecionável em mobile com funcionalidade completa em desktop
   - **Acessibilidade**: componentes shadcn/ui preservam acessibilidade nativa (Radix UI), labels obrigatórios em forms, contraste mínimo WCAG AA, navegação por teclado funcional, aria-labels em ícones interativos
   - **Linting**: ESLint com config Next.js + Prettier + plugin tailwindcss (ordenação de classes)
   - **Proibições**: ❌ CSS inline via `style={}` (exceto valores dinâmicos calculados), ❌ `!important`, ❌ componentes sem tipagem TypeScript, ❌ `any` type, ❌ lógica de negócio em componentes de UI (separar em hooks ou services)

---

### Requisito 12: Sequenciamento Seguro da Adequação

**User Story:** Como líder técnico, eu quero que a adequação siga uma ordem segura (remover → alterar → criar → testar), para que o projeto nunca fique em estado inconsistente durante o processo.

#### Critérios de Aceitação

1. THE Migration_Plan SHALL definir a seguinte ordem de execução: Fase 1 (reestruturar docs), Fase 2 (remover módulos deprecados), Fase 3 (criar novos módulos com estrutura mínima compilável contendo build.gradle.kts e package-info.java), Fase 4 (criar Worker_Process), Fase 5 (adequar Docker Compose com profiles), Fase 6 (scaffolding frontend), Fase 7 (criar documentos de adequação), Fase 8 (atualizar AGENTS.md e steering files)
2. WHEN cada fase for concluída, THE Migration_Plan SHALL validar que `./gradlew build` retorna exit code 0 dentro de no máximo 300 segundos antes de prosseguir para a próxima fase
3. IF uma fase falhar na validação de build, THEN THE Migration_Plan SHALL tentar corrigir os erros em no máximo 3 tentativas, e se após 3 tentativas o build ainda falhar, SHALL reverter as mudanças da fase ao estado anterior (último commit estável) e reportar o erro ao líder técnico
4. THE Migration_Plan SHALL tratar cada fase como um commit independente com mensagem seguindo Conventional Commits, permitindo rollback via `git revert` do commit correspondente sem afetar outras fases
5. WHEN todas as fases forem concluídas, THE Migration_Plan SHALL validar que: `./gradlew build` passa com exit code 0, todos os testes unitários existentes passam via `make test-unit` com exit code 0, e o `AGENTS.md` lista todos os módulos presentes em `settings.gradle.kts` e não referencia módulos inexistentes no repositório
6. IF o comando `make test-unit` falhar após a conclusão de todas as fases, THEN THE Migration_Plan SHALL identificar quais testes falharam, reportar a lista de testes com falha, e não considerar a adequação como concluída até que todos os testes passem
