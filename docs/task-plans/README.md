# AtlasOps AI — Task Plans Reordered

> Atualizado em 2026-07-27
> Fontes validadas: `docs/ROADMAP-QUALITY-ENFORCEMENT-WAVES.md`, `docs/00-current-status.md`, `Makefile`, `build.gradle.kts`, `.github/workflows/ci.yml`, `.github/workflows/nightly.yml`

## Objetivo

Reorganizar os planos em ordem operacional:

1. implementar ou ajustar codigo e configuracao;
2. validar indicadores, coverage e quality checks;
3. so no final atualizar documentacao e consolidar o roadmap.

## Escopo excluido

- SLA, SLO e definicoes burocraticas de governanca;
- "definicao do usuario" e demais itens nao tecnicos;
- claims de roadmap sem evidencia executavel.

## Conclusao da validacao

- O roadmap histórico removido superdeclarava conclusão. Capacidades sem evidência suficiente continuam nos backlogs ativos.
- `docs/ROADMAP-QUALITY-ENFORCEMENT-WAVES.md` descreve parte do estado real, mas varios itens marcados como "concluidos" ainda estao incompletos na esteira.
- A pasta `docs/task-plans` foi recriada para refletir o que realmente falta em codigo e validacao.

## Estado atual das ferramentas

### Ja configurado

- `Jacoco` por modulo e `aggregateJacocoReport`
- `SonarQube` no Gradle e job dedicado na CI
- `Checkstyle`, `SpotBugs`, `Spotless`
- `OWASP Dependency-Check`
- `gitleaks`
- `CodeQL`
- `Semgrep`
- `Trivy`
- `Syft` para SBOM
- `dependency-cruiser` no frontend
- `Playwright`
- `k6`

### Ainda nao confiavel o suficiente

- coverage atual nao sustenta "90% de garantia";
- `verify-contracts` já executa export OpenAPI, lint OpenAPI e validação AsyncAPI;
- lint de OpenAPI/AsyncAPI esta desalinhado do artefato autoritativo;
- jobs noturnos ainda toleram falhas em pontos relevantes;
- frontend nao tem cobertura de testes unitarios configurada;
- SonarQube esta configurado, mas a configuracao raiz de `sonar.sources` e `sonar.tests` nao representa corretamente o monorepo multi-modulo.

## Evidencia objetiva capturada

- `./gradlew jacocoTestCoverageVerification --continue` falhou em 2026-07-27 para `audit`, `operations`, `customers`, `app-boot` e `shared-kernel` antes da execucao ser interrompida.
- Os relatórios Jacoco existentes em `backend/*/build/reports/jacoco/test/jacocoTestReport.xml` mostram total agregado local de aproximadamente:
  - linhas: `40.59%`
  - branches: `34.69%`
- Nos relatórios locais atuais, `19` modulos ficam abaixo de `90%` em linhas ou branches.
- O gate atual do Sonar provisionado em `infra/sonar/provision-quality-gate.sh` exige:
  - `new_coverage >= 80%`
  - `coverage >= 75%`
  - nao existe gate de `90%`.

## Ordem de leitura

1. `TASK-PLAN-01-CODE-AND-CONFIG.md`
2. `TASK-PLAN-02-VALIDATION-AND-CHECKS.md`
3. `TASK-PLAN-03-DOCS-AND-ROADMAP-CLEANUP.md`

## Cobertura de agentes

- `1-backend`: cobre backend, contratos, coverage Java, Sonar e checks de arquitetura backend.
- `frontend`: cobre frontend, coverage unitário, boundaries TypeScript e lint/compile do app web.
- `Quality Agent`: cobre a orquestração dos gates, CI, segurança, performance e artefatos.
