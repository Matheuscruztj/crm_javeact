# Task Plan 01 — Code and Config First

> Prioridade: alta
> Regra: nenhum item documental entra aqui

## 1. Reerguer a cobertura real do backend

### Evidencia

- `build.gradle.kts` exige `75%` de linhas e `65%` de branches por modulo.
- A validacao real falhou em 2026-07-27 para modulos centrais.
- Os relatorios locais atuais mostram cobertura muito baixa em modulos que o roadmap trata como prontos:
  - `approvals`
  - `auth`
  - `search`
  - `notifications`
  - `analytics`
  - `ai`
  - `app-boot`
  - `worker`

### Atividades

- adicionar testes unitarios e de propriedade nos modulos abaixo de `75/65`;
- complementar testes de aplicacao e infraestrutura em `app-boot`, `worker`, `search`, `auth`, `approvals`, `notifications`, `analytics` e `ai`;
- revisar se os testes atuais cobrem apenas value objects e nao os use cases/adapters que o roadmap declara como concluidos;
- eliminar lacunas em `customers`, `operations`, `audit` e `shared-kernel`, porque estes ja quebram o gate minimo;
- so depois discutir aumento de threshold. No estado atual, elevar direto para `90%` sem corrigir a base so mascara o problema.

### Criterio de saida

- `./gradlew jacocoTestCoverageVerification` passa integralmente;
- modulos criticos de dominio ficam no minimo em `85%` linhas;
- existe plano separado e mensuravel para aproximacao de `90%`.

## 2. Corrigir a fonte autoritativa do contrato OpenAPI

### Evidencia

- existe `openApiContractExportTest` em `backend/app-boot/build.gradle.kts`;
- existe job `openapi-contract-export` na CI;
- o job `openapi-lint` ainda valida `backend/app-boot/src/main/resources/openapi.yaml`, nao o artefato exportado em build;
- `verify-contracts` no `Makefile` ainda e placeholder.

### Atividades

- tornar o artefato exportado em `backend/app-boot/build/reports/openapi/openapi.json` a unica fonte validada na CI;
- ligar `make verify-contracts` ao export real do contrato e ao lint correspondente;
- remover dependencia operacional de `src/main/resources/openapi.yaml` como verdade primaria, se o objetivo e validar a aplicacao rodando;
- adicionar validacao de breaking change no pipeline de contrato.

### Criterio de saida

- o contrato e gerado pela aplicacao e validado no mesmo fluxo;
- a CI falha se o contrato exportado for invalido ou incompatível.

## 3. Corrigir a configuracao multi-modulo do SonarQube

### Evidencia

- `build.gradle.kts` define `sonar.sources=src/main/java` e `sonar.tests=src/test/java` na raiz;
- o repositorio real e multi-modulo em `backend/*` e `frontend/`;
- o gate do Sonar existe, mas a configuracao de origem dos fontes nao acompanha a estrutura do monorepo.

### Atividades

- ajustar a configuracao para apontar fontes e testes por subprojeto, ou deixar o plugin inferir por modulo sem sobrescrever com path raiz incorreto;
- alinhar `sonar.coverage.jacoco.xmlReportPaths` com o relatorio agregado quando ele for o artefato oficial;
- decidir explicitamente se o frontend entra no Sonar agora ou fica fora por decisao registrada.

### Criterio de saida

- a analise do Sonar representa o codigo real do monorepo;
- coverage e code smells no Sonar batem com os relatórios Jacoco e com a CI.

## 4. Completar a superficie de testes do frontend

### Evidencia

- `frontend/package.json` possui `format`, `lint`, `typecheck`, `architecture:check`, `test:e2e` e smoke de performance;
- nao ha `vitest`, `jest` ou gate de coverage de frontend.

### Atividades

- introduzir testes unitarios/componentes no frontend com cobertura;
- definir threshold minimo por linhas e branches para frontend;
- testar hooks, formatacao de dados, componentes de tabela, estados de erro e client adapters;
- manter `Playwright` para jornada, mas parar de usá-lo como substituto de cobertura de unidade.

### Criterio de saida

- frontend passa a ter cobertura automatizada e relatorio de qualidade proprio;
- `verify-frontend-fast` continua rapido, mas existe um gate completo com coverage.

## 5. Concluir a implementacao tecnica de contract/boundary checks

### Evidencia

- backend possui `architectureTest`;
- frontend possui `dependency-cruiser`;
- nao existe ainda verificacao de backward compatibility nem contrato consumidor/provedor;
- lint de AsyncAPI usa ruleset de OpenAPI.

### Atividades

- corrigir o ruleset de AsyncAPI para ferramenta/regras adequadas;
- introduzir diff de breaking change para OpenAPI;
- avaliar e adotar `Pact` ou `Spring Cloud Contract` apenas onde houver consumidor real;
- gerar tipos/cliente derivados do OpenAPI no frontend, substituindo tipos manuais fragis.

### Criterio de saida

- arquitetura e contrato deixam de ser checks isolados e passam a proteger integracao real.
