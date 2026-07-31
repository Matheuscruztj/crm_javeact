# AtlasOps AI — Roadmap de Quality Enforcement por Waves

> **Criado em:** 2026-07-27
> **Objetivo:** estruturar a implantação progressiva de checks de arquitetura, segurança, contrato, performance e desacoplamento
> **Modelo de execução:** waves paralelizáveis por trilha de agente (`backend`, `frontend`, `quality`)

---

## 1. Objetivo

Este roadmap organiza a evolução dos quality gates do AtlasOps AI em uma sequência prática e paralelizável.

Foco:

- reforçar desacoplamento estrutural;
- ampliar checks estáticos e dinâmicos;
- endurecer segurança de supply chain;
- validar contratos de API e integração;
- consolidar pre-build, pre-commit e esteira CI do GitHub;
- permitir execução paralela por agentes especializados.

O plano assume três trilhas de execução independentes:

- `1-backend`
- `frontend`
- `Quality Agent`

Cada wave define:

- objetivo;
- entregas;
- tasks por trilha;
- dependências;
- critérios de saída.

---

## 2. Princípios de Execução

### 2.1 Regras gerais

- Toda wave deve produzir evidência executável, não apenas documentação.
- Todo check novo deve ser classificado como `advisory` ou `blocking`.
- Checks pesados não devem entrar no `pre-commit`.
- Todo gate local deve ter equivalente rastreável na CI.
- Todo gate crítico de arquitetura ou segurança deve ser reproduzível sem depender de estado manual.

### 2.2 Distribuição por trilha

#### 1-backend

Responsável por:

- ArchUnit;
- annotation checks;
- inheritance checks;
- regras de desacoplamento entre módulos;
- geração de OpenAPI;
- testes de resiliência no backend;
- integração com Testcontainers e Toxiproxy.

#### frontend

Responsável por:

- lint e formatação frontend;
- regras arquiteturais TypeScript;
- boundary checks;
- organização de camadas;
- geração/consumo de contratos tipados;
- ergonomia dos hooks locais.

#### Quality Agent

Responsável por:

- esteira de pre-build;
- pre-commit e pre-push;
- CI GitHub Actions;
- security scanning;
- SBOM;
- SAST;
- contract lint;
- k6 e relatórios;
- policy de severidade e bloqueio.

---

## 3. Mapa Geral de Waves

| Wave | Nome | Objetivo principal | Paralelismo |
| --- | --- | --- | --- |
| W1 | Baseline Enforcement | Corrigir e endurecer os gates já existentes | `1-backend` + `frontend` + Quality |
| W2 | Structural Decoupling | Adicionar checks de arquitetura, herança, anotações e boundaries | `1-backend` + `frontend` |
| W3 | API and Contract Integrity | Tornar contrato API executável e verificável | `1-backend` + `frontend` + Quality |
| W4 | Security and Supply Chain | Expandir segurança além de dependency-check | `1-backend` + Quality |
| W5 | Runtime Resilience and Dynamic Coupling | Validar dependências em falha e degradação controlada | `1-backend` + Quality |
| W6 | Performance and Operational Evidence | Consolidar carga, relatórios e gates não-funcionais | `1-backend` + `frontend` + Quality |
| W7 | Governance and Continuous Hardening | Fechar manutenção contínua e debt operacional | Quality |

---

## 4. Wave W1 — Baseline Enforcement

### 4.1 Objetivo

Transformar checks já existentes em enforcement confiável e eliminar falsos positivos de “pipeline verde sem validação real”.

### 4.2 Entregas

- CI com gates reais e consistentes;
- scripts locais rápidos para desenvolvedor;
- correção de jobs que hoje pulam ou toleram falhas;
- classificação de todos os checks em `blocking` ou `advisory`.

### 4.3 Tasks — Backend Agent

- W1-BE-01: Revisar tasks Gradle existentes (`spotless`, `checkstyle`, `spotbugs`, `jacoco`, `testProperty`, `integrationTest`).
- W1-BE-02: Verificar se `ArchUnit` está sempre sendo executado de forma determinística.
- W1-BE-03: Remover tolerância indevida em execução de testes arquiteturais no CI.
- W1-BE-04: Consolidar uma task local `verify-backend-fast` para ciclo curto.
- W1-BE-05: Documentar tempo médio de execução de cada gate backend.

### 4.4 Tasks — Frontend Agent

- W1-FE-01: Formalizar `prettier --check` como gate explícito.
- W1-FE-02: Revisar `eslint` atual e alinhar regras mínimas obrigatórias.
- W1-FE-03: Consolidar task `verify-frontend-fast`.
- W1-FE-04: Mapear gaps entre convenção frontend e enforcement real.
- W1-FE-05: Garantir que build frontend falhe em inconsistências estruturais relevantes.

### 4.5 Tasks — Quality Agent

- W1-QA-01: Corrigir workflow CI para remover `|| true` em checks críticos.
- W1-QA-02: Revisar jobs que “skippam” por ausência de arquivo e convertê-los em comportamento explícito.
- W1-QA-03: Criar matriz de checks por severidade: `local-fast`, `pre-commit`, `pre-push`, `ci-blocking`, `ci-advisory`.
- W1-QA-04: Criar targets `make verify-local-fast`, `make verify-prepush`, `make verify-contracts`.
- W1-QA-05: Publicar artifacts mínimos obrigatórios: testes, cobertura, lint, segurança.

### 4.6 Dependências

- Nenhuma. Wave inicial.

### 4.7 Critério de saída

- nenhum gate crítico passa “sem validar nada”;
- toda execução local rápida conclui em tempo aceitável;
- toda falha de arquitetura ou lint crítico quebra a CI.

### 4.8 Status de Implementação

- `W1-BE-01` concluído: task matrix Gradle revisada e comando `verifyFast`/`verifyFull` exposto na base existente.
- `W1-BE-03` concluído: tolerância indevida para arquitetura removida da CI.
- `W1-BE-04` concluído: `make verify-local-fast` publicado como alias de feedback curto.
- `W1-BE-05` pendente: ainda faltam métricas formais de tempo médio em documentação.
- `W1-FE-01` concluído: `prettier --check` foi formalizado como gate explícito no frontend e na CI.
- `W1-FE-02` pendente: lint frontend ainda precisa de revisão de regra mínima documentada.
- `W1-FE-03` concluído: `pnpm verify:fast` entrou como caminho rápido local e passou após a normalização.
- `W1-FE-04` pendente: gaps entre convenção e enforcement seguem em revisão.
- `W1-FE-05` pendente: build frontend verde, mas documentação estrutural ainda pode ser enriquecida.
- `W1-QA-01` concluído: CI backend não tolera falha de ArchUnit.
- `W1-QA-02` concluído em 2026-07-31: o fluxo local e a CI passaram a tratar explicitamente os checks disponíveis, sem placeholders operacionais em `verify-contracts`, `pre-commit`, `pre-push` e SBOM nightly.
- `W1-QA-03` concluído em 2026-07-31: a matriz prática de severidade ficou refletida em `verify-local-fast`, `pre-commit`, `pre-push`, CI principal e nightly.
- `W1-QA-04` concluído: aliases `verify-local-fast`, `verify-frontend-fast`, `verify-precommit`, `verify-prepush`, `verify-contracts` e `verify-security` adicionados.
- `W1-QA-05` concluído em 2026-07-31: artifacts mínimos já existem para cobertura backend, contrato OpenAPI, frontend coverage, k6 e SBOM; relatórios adicionais ainda podem evoluir em waves posteriores.

---

## 5. Wave W2 — Structural Decoupling

### 5.1 Objetivo

Adicionar enforcement explícito para desacoplamento, herança indevida, uso incorreto de anotações e boundaries entre camadas.

### 5.2 Entregas

- ArchUnit expandido;
- regras de arquitetura frontend automatizadas;
- boundaries por pacote e por diretório;
- checks anti-acoplamento estrutural.

### 5.3 Tasks — Backend Agent

- W2-BE-01: Criar `Inheritance Rules` no ArchUnit.
  - domínio não estende classes de framework;
  - controllers não servem de base herdável sem justificativa;
  - classes sensíveis devem ser `final` ou `sealed` quando aplicável.
- W2-BE-02: Criar `Annotation Rules` no ArchUnit.
  - `domain` sem `@Component`, `@Service`, `@Entity`, `@RestController`, `@Transactional`;
  - `presentation` com anotações web adequadas;
  - proibir `@Autowired` em campo;
  - validar uso de `@Valid` nos pontos necessários.
- W2-BE-03: Expandir regras de desacoplamento entre módulos.
  - impedir acesso direto a `infrastructure` de outro módulo;
  - impedir uso direto de entidades de domínio de outro módulo quando deveria haver use case ou port.
- W2-BE-04: Criar rule set para evitar imports de tecnologia externa dentro de `domain`.
- W2-BE-05: Adicionar suíte de testes arquiteturais dedicada por categoria.

### 5.4 Tasks — Frontend Agent

- W2-FE-01: Introduzir `dependency-cruiser` ou `ts-arch`.
- W2-FE-02: Definir boundaries do frontend.
  - `app/` não importa detalhes de infra sem adapter;
  - `components/` não acessa camadas proibidas;
  - `hooks/` não dependem de páginas;
  - `lib/` separado por responsabilidade.
- W2-FE-03: Criar regra contra import circular.
- W2-FE-04: Criar regra para impedir acoplamento indevido entre admin e portal quando não compartilhado por contrato.
- W2-FE-05: Introduzir gate de arquitetura frontend na CI.

### 5.5 Tasks — Quality Agent

- W2-QA-01: Definir nomenclatura padrão para categorias de violation.
- W2-QA-02: Adicionar job CI dedicado `architecture-backend`.
- W2-QA-03: Adicionar job CI dedicado `architecture-frontend`.
- W2-QA-04: Classificar regras como `blocking` por estágio.
- W2-QA-05: Publicar relatório consolidado de violações arquiteturais.

### 5.6 Dependências

- Requer Wave W1 concluída.

### 5.7 Critério de saída

- backend com inheritance e annotation checks automatizados;
- frontend com boundary checks automatizados;
- ausência de imports circulares e acessos proibidos em trilhas críticas.

### 5.8 Status de Implementação

- `W2-BE-01` pendente: criar `Inheritance Rules` no ArchUnit.
- `W2-BE-02` pendente: criar `Annotation Rules` no ArchUnit.
- `W2-BE-03` pendente: expandir regras de desacoplamento entre módulos.
- `W2-BE-04` pendente: criar rule set para evitar imports de tecnologia externa dentro de `domain`.
- `W2-BE-05` pendente: adicionar suíte de testes arquiteturais dedicada por categoria.
- `W2-FE-01` concluído: `dependency-cruiser` já está introduzido e integrado ao frontend.
- `W2-FE-02` parcialmente concluído: boundaries básicas do frontend já são exercidas pelo `dependency-cruiser`, mas ainda não cobrem toda a taxonomia prevista.
- `W2-FE-03` parcialmente concluído: o gate atual de arquitetura já ajuda a conter acoplamentos, mas a regra explícita dedicada para circularidade ainda não foi registrada separadamente.
- `W2-FE-04` pendente: criar regra para impedir acoplamento indevido entre admin e portal quando não compartilhado por contrato.
- `W2-FE-05` concluído: existe job dedicado de arquitetura frontend na CI.
- `W2-QA-01` pendente: definir nomenclatura padrão para categorias de violation.
- `W2-QA-02` concluído: job CI dedicado `architecture-backend` já existe.
- `W2-QA-03` concluído: job CI dedicado `architecture-frontend` já existe.
- `W2-QA-04` pendente: classificar regras como `blocking` por estágio.
- `W2-QA-05` pendente: publicar relatório consolidado de violações arquiteturais.

---

## 6. Wave W3 — API and Contract Integrity

### 6.1 Objetivo

Converter contrato de API em artefato verificável e impedir drift entre implementação, documentação e consumidores.

### 6.2 Entregas

- `openapi.yaml` real gerado ou exportado no build;
- lint consistente de OpenAPI e AsyncAPI;
- estratégia definida para contract testing;
- validação de backward compatibility.

### 6.3 Tasks — Backend Agent

- W3-BE-01: Definir processo de geração/export de OpenAPI a partir da aplicação.
- W3-BE-02: Versionar ou publicar `openapi.yaml` como artifact da CI.
- W3-BE-03: Garantir cobertura mínima dos endpoints críticos no contrato.
- W3-BE-04: Adicionar testes que validem presença e consistência de schema em endpoints críticos.
- W3-BE-05: Avaliar adoção de `Pact` ou `Spring Cloud Contract` para integrações relevantes.

### 6.4 Tasks — Frontend Agent

- W3-FE-01: Mapear endpoints consumidos pelo frontend e aderência ao contrato.
- W3-FE-02: Avaliar geração de tipos a partir de OpenAPI.
- W3-FE-03: Isolar client HTTP para reduzir acoplamento a payloads ad hoc.
- W3-FE-04: Substituir tipos manuais frágeis por tipos derivados do contrato quando viável.
- W3-FE-05: Criar smoke de compatibilidade frontend-API em cenários críticos.

### 6.5 Tasks — Quality Agent

- W3-QA-01: Corrigir job `openapi-lint` para operar sobre spec real.
- W3-QA-02: Corrigir lint de AsyncAPI com ruleset apropriado.
- W3-QA-03: Adicionar verificação de breaking changes de contrato.
- W3-QA-04: Introduzir job `contract-tests` se `Pact`/`SCC` for adotado.
- W3-QA-05: Publicar artifacts de contrato por build.

### 6.6 Dependências

- Requer W1;
- idealmente W2 concluída para reduzir drift estrutural.

### 6.7 Critério de saída

- todo build produz contrato verificável;
- CI falha em caso de spec inválida ou breaking change não aprovada;
- consumidores críticos têm contrato executável.

### 6.8 Status de Implementação

- `W3-BE-01` concluído em 2026-07-31: o processo de geração/export de OpenAPI a partir da aplicação já está ligado ao fluxo de verificação.
- `W3-BE-02` concluído em 2026-07-31: o contrato exportado já é publicado como artifact da CI.
- `W3-BE-03` pendente: garantir cobertura mínima dos endpoints críticos no contrato.
- `W3-BE-04` pendente: adicionar testes que validem presença e consistência de schema em endpoints críticos.
- `W3-BE-05` pendente: avaliar adoção de `Pact` ou `Spring Cloud Contract` para integrações relevantes.
- `W3-FE-01` pendente: mapear endpoints consumidos pelo frontend e aderência ao contrato.
- `W3-FE-02` pendente: avaliar geração de tipos a partir de OpenAPI.
- `W3-FE-03` parcialmente concluído: já existe client HTTP isolado no frontend, mas sem derivação automática do contrato.
- `W3-FE-04` pendente: substituir tipos manuais frágeis por tipos derivados do contrato quando viável.
- `W3-FE-05` pendente: criar smoke de compatibilidade frontend-API em cenários críticos.
- `W3-QA-01` concluído em 2026-07-31: `verify-contracts` opera sobre a spec real exportada pela aplicação.
- `W3-QA-02` concluído em 2026-07-31: o fluxo atual usa validação AsyncAPI por CLI compatível, em vez de ruleset inadequado de OpenAPI.
- `W3-QA-03` pendente: adicionar verificação de breaking changes de contrato.
- `W3-QA-04` pendente: introduzir job `contract-tests` se `Pact`/`SCC` for adotado.
- `W3-QA-05` concluído em 2026-07-31: artifacts de contrato já são publicados por build.

---

## 7. Wave W4 — Security and Supply Chain

### 7.1 Objetivo

Expandir segurança para além de CVE tradicional, cobrindo SAST, secrets, container, SBOM e resposta rápida a vulnerabilidades emergentes.

### 7.2 Entregas

- SAST operacional;
- SBOM publicado;
- image scan;
- política de severidade;
- posture de resposta a “zero-day”.

### 7.3 Tasks — Backend Agent

- W4-BE-01: Revisar áreas sensíveis para regras Semgrep ou queries específicas.
- W4-BE-02: Identificar sinks críticos:
  - SSRF;
  - desserialização;
  - file upload;
  - SQL dinâmico;
  - chamadas externas.
- W4-BE-03: Preparar suppressions justificadas para falsos positivos.
- W4-BE-04: Garantir que configurações de segurança protegidas não sejam alteradas por automação indevida.

### 7.4 Tasks — Quality Agent

- W4-QA-01: Adicionar `CodeQL`.
- W4-QA-02: Adicionar `Semgrep`.
- W4-QA-03: Manter `gitleaks` como blocking.
- W4-QA-04: Reforçar `OWASP Dependency-Check`.
- W4-QA-05: Avaliar complementar com `Snyk` ou `OSV-Scanner`.
- W4-QA-06: Adicionar `Trivy` para filesystem e imagem Docker.
- W4-QA-07: Adicionar `Syft` para geração de SBOM.
- W4-QA-08: Publicar SBOM como artifact por build.
- W4-QA-09: Definir policy de bloqueio por severidade.
- W4-QA-10: Adicionar `Dependabot` ou `Renovate`.
- W4-QA-11: Criar runbook de resposta rápida a dependência crítica emergente.

### 7.5 Dependências

- Requer W1.

### 7.6 Critério de saída

- CI cobre SAST, secrets, SCA, image scan e SBOM;
- existe política explícita para severidade crítica;
- existe fluxo de contenção para vulnerabilidade emergente.

### 7.7 Status de Implementação

- `W4-BE-01` pendente: revisar áreas sensíveis para regras Semgrep ou queries específicas.
- `W4-BE-02` pendente: identificar sinks críticos.
- `W4-BE-03` pendente: preparar suppressions justificadas para falsos positivos.
- `W4-BE-04` pendente: garantir que configurações de segurança protegidas não sejam alteradas por automação indevida.
- `W4-QA-01` pendente: adicionar `CodeQL`.
- `W4-QA-02` pendente: adicionar `Semgrep`.
- `W4-QA-03` pendente: manter `gitleaks` como blocking.
- `W4-QA-04` parcialmente concluído: `OWASP Dependency-Check` existe e roda no nightly, mas a política final para ausência de `NVD_API_KEY` ainda precisa ser formalizada.
- `W4-QA-05` pendente: avaliar complementar com `Snyk` ou `OSV-Scanner`.
- `W4-QA-06` concluído em 2026-07-31: `Trivy` foi adicionado para filesystem e imagem Docker no fluxo local e nos workflows.
- `W4-QA-07` concluído: `Syft` foi adicionado para geração de SBOM.
- `W4-QA-08` concluído: SBOM é publicado como artifact no nightly.
- `W4-QA-09` pendente: definir policy de bloqueio por severidade.
- `W4-QA-10` pendente: adicionar `Dependabot` ou `Renovate`.
- `W4-QA-11` pendente: criar runbook de resposta rápida a dependência crítica emergente.

---

## 8. Wave W5 — Runtime Resilience and Dynamic Coupling

### 8.1 Objetivo

Medir e reduzir acoplamento dinâmico entre serviços, infraestrutura e adapters externos.

### 8.2 Entregas

- testes automatizados de indisponibilidade;
- injeção de falha controlada;
- degradação validada;
- evidência de retry, timeout, fallback e circuit breaker.

### 8.3 Tasks — Backend Agent

- W5-BE-01: Introduzir `Toxiproxy` com `Testcontainers`.
- W5-BE-02: Criar testes automatizados para:
  - PostgreSQL indisponível;
  - Redis indisponível;
  - MinIO indisponível;
  - Ollama indisponível;
  - OpenSearch indisponível.
- W5-BE-03: Adicionar cenários de:
  - latência alta;
  - timeout;
  - connection reset;
  - degradação parcial.
- W5-BE-04: Verificar comportamento de:
  - fallback;
  - bounded retry;
  - circuit breaker;
  - DLQ;
  - idempotência.
- W5-BE-05: Criar suíte `resilienceTest` separada de `integrationTest` quando necessário.

### 8.4 Tasks — Quality Agent

- W5-QA-01: Criar job CI `resilience-tests` com escopo controlado.
- W5-QA-02: Marcar testes longos como nightly quando não forem adequados para PR.
- W5-QA-03: Padronizar relatório de falha por dependência.
- W5-QA-04: Ligar resultados de resiliência a runbooks e critérios operacionais.

### 8.5 Dependências

- Requer W1;
- idealmente W4 para correlação com postura de segurança;
- recomendável W3 concluída para padronização de erros.

### 8.6 Critério de saída

- principais dependências externas possuem teste automatizado de falha;
- sistema degrada com comportamento conhecido e documentado;
- não há cascata silenciosa em cenários prioritários.

### 8.7 Status de Implementação

- `W5-BE-01` parcialmente concluído: já existe base com `Toxiproxy`/fault injection em partes do conjunto, mas a cobertura ainda não é uniforme.
- `W5-BE-02` parcialmente concluído: existem testes automatizados de falha para dependências principais, porém ainda em níveis distintos de maturidade.
- `W5-BE-03` parcialmente concluído: cenários de indisponibilidade e degradação já existem em parte da suíte, mas ainda não cobrem toda a matriz desejada.
- `W5-BE-04` parcialmente concluído: fallback, bounded retry, circuit breaker e caminhos de falha já têm cobertura parcial.
- `W5-BE-05` concluído: a suíte `resilienceTest` já existe de forma separada.
- `W5-QA-01` concluído em 2026-07-31: a CI principal já possui job `resilience-tests`.
- `W5-QA-02` parcialmente concluído: parte dos cenários pesados segue no nightly, mas a segmentação ainda pode evoluir.
- `W5-QA-03` pendente: padronizar relatório de falha por dependência.
- `W5-QA-04` parcialmente concluído: já existe ligação com runbooks e documentação operacional, mas sem matriz consolidada única.

---

## 9. Wave W6 — Performance and Operational Evidence

### 9.1 Objetivo

Consolidar evidência não-funcional com execução reproduzível de carga e publicação de resultados.

### 9.2 Entregas

- smoke de carga real;
- baseline por cenário;
- organização de scripts k6;
- artifacts de performance;
- thresholds explícitos.

### 9.3 Tasks — Backend Agent

- W6-BE-01: Revisar endpoints críticos para cenários k6.
- W6-BE-02: Padronizar payloads e dados de teste de carga.
- W6-BE-03: Expor métricas adequadas para análise de gargalo.

### 9.4 Tasks — Frontend Agent

- W6-FE-01: Identificar jornadas frontend críticas para smoke funcional correlacionado.
- W6-FE-02: Medir impacto de bundle e requests em rotas críticas.
- W6-FE-03: Preparar smoke de integração UI + API quando aplicável.

### 9.5 Tasks — Quality Agent

- W6-QA-01: Corrigir path e estratégia do job k6 na CI.
- W6-QA-02: Organizar cenários:
  - smoke;
  - baseline;
  - stress;
  - soak.
- W6-QA-03: Publicar relatórios JSON/HTML.
- W6-QA-04: Definir thresholds por endpoint ou fluxo.
- W6-QA-05: Criar execução agendada para testes pesados.
- W6-QA-06: Integrar evidência de performance ao processo de release.

### 9.6 Dependências

- Requer W1;
- idealmente W5 para correlação entre carga e degradação.

### 9.7 Critério de saída

- smoke de performance roda de forma consistente;
- baseline reproduzível existe para os fluxos críticos;
- resultados ficam disponíveis como artifacts e referência de regressão.

### 9.8 Status de Implementação

- `W6-BE-01` parcialmente concluído: os cenários k6 existentes já cobrem fluxos críticos principais.
- `W6-BE-02` parcialmente concluído: payloads e scripts-base já existem, mas ainda sem consolidação completa por módulo.
- `W6-BE-03` parcialmente concluído: métricas e dashboards operacionais já suportam parte da análise.
- `W6-FE-01` parcialmente concluído: já existe smoke de caminho crítico frontend.
- `W6-FE-02` pendente: medir impacto de bundle e requests em rotas críticas.
- `W6-FE-03` pendente: preparar smoke de integração UI + API quando aplicável.
- `W6-QA-01` concluído: a estratégia do job k6 já está organizada entre CI principal e nightly.
- `W6-QA-02` concluído: cenários `smoke`, `average` e `stress` já estão organizados; `soak` ainda não.
- `W6-QA-03` concluído: relatórios JSON/HTML já são publicados.
- `W6-QA-04` parcialmente concluído: existem thresholds parametrizáveis, mas ainda não há política central única por fluxo.
- `W6-QA-05` concluído: existe execução agendada para testes pesados no nightly.
- `W6-QA-06` parcialmente concluído: já há evidência ligada ao processo de release, mas ainda não como gate final obrigatório.

---

## 10. Wave W7 — Governance and Continuous Hardening

### 10.1 Objetivo

Fechar a operação contínua dos checks para que o sistema continue saudável após a implantação inicial.

### 10.2 Entregas

- governança por severidade;
- ownership por check;
- manutenção periódica;
- exceções rastreáveis;
- debt visível.

### 10.3 Tasks — Quality Agent

- W7-QA-01: Criar matriz `check -> owner -> frequência -> blocking/advisory`.
- W7-QA-02: Definir processo de exception handling com expiração.
- W7-QA-03: Definir SLA de correção por severidade.
- W7-QA-04: Criar dashboard simples de qualidade.
- W7-QA-05: Revisar mensalmente suppressions e bypasses.
- W7-QA-06: Criar critério para promoção de checks de advisory para blocking.
- W7-QA-07: Atualizar roadmap principal após cada wave concluída.

### 10.4 Dependências

- Requer W1 a W6 em operação mínima.

### 10.5 Critério de saída

- todos os checks têm owner e política;
- exceções são temporárias e rastreáveis;
- qualidade não depende de memória informal do time.

### 10.6 Status de Implementação

- `W7-QA-01` pendente: criar matriz `check -> owner -> frequência -> blocking/advisory`.
- `W7-QA-02` pendente: definir processo de exception handling com expiração.
- `W7-QA-03` pendente: definir SLA de correção por severidade.
- `W7-QA-04` pendente: criar dashboard simples de qualidade.
- `W7-QA-05` pendente: revisar mensalmente suppressions e bypasses.
- `W7-QA-06` pendente: criar critério para promoção de checks de advisory para blocking.
- `W7-QA-07` pendente: atualizar roadmap principal após cada wave concluída.

---

## 11. Pre-Build, Pre-Commit, Pre-Push e CI

## 11.1 Pre-build local

Objetivo:

- feedback rápido;
- baixo custo;
- foco em erros frequentes.

Escopo recomendado:

- backend fast verification;
- frontend lint + typecheck + format check;
- architecture subset;
- contract generation/lint rápido quando possível.

Tasks:

- PB-01: Criar `make verify-local-fast`.
- PB-02: Separar tasks rápidas e lentas.
- PB-03: Garantir que output local seja legível por trilha.

### 11.1 Status de Implementação

- `PB-01` concluído: `make verify-local-fast` criado.
- `PB-02` concluído em 2026-07-31: tasks rápidas e lentas estão explicitamente separadas entre `verify-local-fast`, `pre-commit`, `pre-push`, CI principal e nightly.

## 11.2 Pre-commit

Objetivo:

- barrar erro óbvio sem inviabilizar fluxo.

Escopo recomendado:

- formatter;
- lint incremental;
- prettier check;
- secret scan staged;
- frontend boundary/lint leve.

Tasks:

- PC-01: Escolher `pre-commit` ou `lefthook`.
- PC-02: Implementar hook staged.
- PC-03: Garantir execução em menos de janela aceitável.

### 11.2 Status de Implementação

- `PC-01` concluído: hooks locais foram implementados diretamente via Git hooks instalados por script.
- `PC-02` concluído em 2026-07-31: `pre-commit` opera sobre staged files, com checks leves de diff, shell, YAML e JSON.
- `PC-03` parcialmente concluído: o escopo do `pre-commit` foi reduzido para custo baixo, mas ainda faltam métricas formais de tempo.

## 11.3 Pre-push

Objetivo:

- validar conjunto mais forte antes de subir branch.

Escopo recomendado:

- testes rápidos;
- arquitetura;
- contrato;
- subset de integração.

Tasks:

- PP-01: Criar `make verify-prepush`.
- PP-02: Incluir `ArchUnit`, `dependency-cruiser`, `OpenAPI lint`.
- PP-03: Avaliar subset de integração por módulo alterado.

### 11.3 Status de Implementação

- `PP-01` concluído: `make verify-prepush` existe.
- `PP-02` concluído em 2026-07-31: `pre-push` inclui backend fast/static/property/architecture, frontend fast/architecture/coverage, `verify-contracts`, resiliência e segurança.
- `PP-03` pendente: ainda não há subset dinâmico de integração por módulo alterado.

## 11.4 CI GitHub

Objetivo:

- enforcement definitivo;
- relatórios;
- rastreabilidade.

Jobs recomendados:

- `backend-quality`
- `frontend-quality`
- `architecture-backend`
- `architecture-frontend`
- `integration-tests`
- `api-contract`
- `security`
- `resilience-tests`
- `performance-smoke`
- `nightly-heavy`

### 11.4 Status de Implementação

- `CI-01` concluído: a CI principal já está segmentada em jobs de backend, frontend, contratos, segurança, resiliência e Sonar.
- `CI-02` concluído: o nightly já concentra os checks mais pesados de performance, SBOM, flaky detection e mutation testing opcional.
- `CI-03` pendente: ainda falta uma matriz documental única ligando cada job a owner, severidade e política de bloqueio.

---

## 12. Sequência de Paralelização Recomendada

### Wave set A — Execução imediata

Pode rodar em paralelo:

- Backend Agent: W1-BE + W2-BE preparo
- Frontend Agent: W1-FE + W2-FE preparo
- Quality Agent: W1-QA

Saída esperada:

- baseline confiável;
- correção dos gates atuais;
- preparação para checks estruturais.

### Wave set B — Estrutura e contrato

Pode rodar em paralelo após W1:

- Backend Agent: W2-BE + W3-BE
- Frontend Agent: W2-FE + W3-FE
- Quality Agent: W2-QA + W3-QA

Saída esperada:

- desacoplamento estrutural automatizado;
- contrato API verificável;
- fronteiras de frontend e backend estabilizadas.

### Wave set C — Segurança e runtime

Pode rodar em paralelo após W1, com melhor resultado se W2/W3 estiverem maduros:

- Backend Agent: W4-BE + W5-BE
- Quality Agent: W4-QA + W5-QA

Saída esperada:

- segurança fortalecida;
- dynamic coupling medido e reduzido.

### Wave set D — Evidência operacional

Pode rodar após W5:

- Backend Agent: W6-BE
- Frontend Agent: W6-FE
- Quality Agent: W6-QA + W7-QA

Saída esperada:

- baseline de performance;
- governança contínua dos gates.

---

## 13. Ordem Recomendada de Implementação

1. W1 — corrigir enforcement existente
2. W2 — arquitetura, annotations, inheritance, decoupling
3. W3 — OpenAPI, AsyncAPI, contratos
4. W4 — SAST, SBOM, supply chain
5. W5 — resiliência automatizada
6. W6 — performance e evidência
7. W7 — governança contínua

---

## 14. Critérios de Sucesso do Programa

O programa será considerado bem-sucedido quando:

- backend e frontend tiverem checks arquiteturais executáveis;
- contratos de API forem artifacts verificáveis;
- segurança cobrir código, dependência, segredo, container e supply chain;
- falhas de dependência prioritárias tiverem teste automatizado;
- carga tiver baseline reproduzível;
- pre-build, pre-commit, pre-push e CI tiverem papéis claramente separados;
- cada trilha de agente puder operar com autonomia e baixo acoplamento.

---

## 15. Documentos Relacionados

| Documento | Relação |
| --- | --- |
| [ROADMAP.md](./ROADMAP.md) | roadmap principal do produto |
| [ROADMAP-DONE.md](./ROADMAP-DONE.md) | histórico de entregas concluídas |
| [task-plans/TASK-PLAN-P3-HARDENING-RELEASE.md](./task-plans/TASK-PLAN-P3-HARDENING-RELEASE.md) | referência de hardening e release |
| [runbooks/RESILIENCE-TESTING.md](./runbooks/RESILIENCE-TESTING.md) | base de resiliência atual |
| [00-current-status.md](./00-current-status.md) | status consolidado do projeto |
