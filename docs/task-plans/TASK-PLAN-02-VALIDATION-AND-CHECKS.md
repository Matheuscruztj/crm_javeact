# Task Plan 02 — Validation, Metrics and Blocking Checks

> Prioridade: alta, apos os ajustes de codigo/configuracao

## 1. Coverage e metricas: estado real

### Validacao atual

- o gate Gradle exige `75%` linhas e `65%` branches por modulo;
- o Sonar provisionado exige `80%` em novo codigo e `75%` global;
- isso esta muito abaixo de uma confianca de `90%`.

### Conclusao

- `SonarQube` e `Jacoco` estao configurados;
- as metricas atuais nao sao boas o suficiente para afirmar `90% de garantia`;
- qualquer declaracao de "fase concluida" precisa ser tratada como provisoria enquanto a coverage real estiver abaixo dos gates e sem consistencia entre modulos.

### Atividades

- rodar `./gradlew test testProperty jacocoTestReport aggregateJacocoReport jacocoTestCoverageVerification`;
- publicar um resumo por modulo em artifact unico;
- separar metricas de `overall`, `new code` e `critical modules`;
- so depois avaliar elevar gates para algo mais proximo de `90%`.

## 2. Transformar checks tolerantes em blocking quando o risco justificar

### Evidencia

- `.github/workflows/nightly.yml` ainda usa `|| true` ou `continue-on-error` em:
  - architecture scan
  - dependency report
  - full load test
- `.github/workflows/ci.yml` ainda suaviza falha em:
  - `Semgrep`
  - validacao dos Dockerfiles
  - lint AsyncAPI com `|| echo "::notice::..."`

### Atividades

- remover tolerancia de falha em arquitetura noturna;
- parar de mascarar falha de build de Dockerfile com warning;
- decidir se `Semgrep` continua advisory ou passa a blocking por severidade;
- manter testes pesados como nightly, mas com status final rastreavel e sem falso verde.

### Criterio de saida

- a pipeline nao fica verde quando um check relevante nao foi realmente validado.

## 3. Validar o pacote de seguranca e supply chain por ferramenta

### Estado por ferramenta

- `gitleaks`: configurado e blocking
- `OWASP Dependency-Check`: configurado, mas pode ser pulado sem `NVD_API_KEY`
- `CodeQL`: configurado
- `Semgrep`: configurado, porem advisory
- `Trivy`: configurado para filesystem e imagem Docker
- `Syft`: configurado para SBOM
- `CycloneDX Gradle plugin`: nao configurado; nightly assume fallback

### Atividades

- decidir se `Dependency-Check` sem `NVD_API_KEY` deve falhar ou marcar pipeline como incompleta;
- formalizar `Semgrep` como blocking ou deixar advisory com criterio explicito;
- remover placeholder do `cyclonedxBom` no nightly, adotando plugin real ou assumindo `Syft` como fonte oficial;
- registrar por check: `blocking`, `advisory` ou `non-blocking nightly`.

### Criterio de saida

- cada ferramenta tem status tecnico claro e nao depende de interpretação manual.

## 4. Fechar a validacao de contratos e arquitetura

### Evidencia

- existe `architecture-backend` e `architecture-frontend` na CI;
- nao existe breaking-change check de OpenAPI;
- `verify-contracts` continua sem execucao real;
- o lint de AsyncAPI esta incorreto para o formato.

### Atividades

- consolidar `verify-contracts` como gate local e CI;
- adicionar diff de breaking change para OpenAPI;
- rodar lint real de AsyncAPI com ruleset compativel;
- publicar artefatos versionados de contrato por build.

### Criterio de saida

- contrato e arquitetura viram checks reproduziveis localmente e em CI.

## 5. Validar resiliência e performance com criterio mensuravel

### Evidencia

- existe task `resilienceTest` em `backend/app-boot/build.gradle.kts`;
- nao existe job dedicado e blocking de `resilienceTest` na CI principal;
- existe `k6-smoke` na CI e `full-load-test` no nightly, mas o nightly tolera falhas e nao ha threshold de aprovacao centralizado.

### Atividades

- adicionar job dedicado para `resilienceTest`;
- separar smoke blocking de cenarios longos nightly;
- definir thresholds de performance por fluxo;
- publicar JSON/HTML como artifact obrigatorio quando testes de carga rodarem.

### Criterio de saida

- resiliencia e performance deixam de ser evidencia eventual e passam a ser gates rastreaveis.
