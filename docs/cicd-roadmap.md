# CI/CD Roadmap

Data de referência: 2026-09-03

Este documento acompanha a evolução da pipeline local-first e dos gates de CI/CD do AtlasOps AI.

## Concluído

- `detect-changes` adicionado ao CI principal para separar backend, frontend, infra, workflows e security.
- `detect-changes` adicionado ao nightly para evitar execução desnecessária de jobs caros em alterações pequenas.
- `Gitleaks` integrado em script local e em jobs de CI/nightly.
- `actionlint` integrado em script local e em jobs de CI/nightly.
- `zizmor` integrado em script local e em jobs de CI/nightly.
- `OSV-Scanner` integrado em script local e em jobs de CI/nightly com fallback seguro quando não há lockfiles suportados.
- `Dependency-Check` adicionado como gate explícito no CI para o backend.
- `pipeline-local.md` reescrito para refletir o estado atual do repositório.

## Em andamento

- Estabilizar `Dependency-Check` com `NVD API Key` em CI.
- Decidir a estratégia final para `OSV-Scanner` no monorepo, incluindo manifests suportados.
- Consolidar os jobs caros do nightly com condicionais mais específicas, se necessário.

## Pendências planejadas

- Cache persistente para Trivy, Gradle e pnpm em self-hosted runner.
- Registry interno para imagens base e scanners.
- Persistência de relatórios de segurança em um dashboard único.
- Avaliação de SonarQube self-hosted, Dependency-Track e DefectDojo.
- Suporte a `paths-filter` ou estratégia equivalente para reduzir a matriz em PRs maiores.

## Próximas ações recomendadas

1. Definir `NVD API Key` para o ambiente de CI.
2. Mapear quais lockfiles o frontend e o backend devem manter para tornar o `OSV-Scanner` efetivo.
3. Medir o impacto real dos jobs condicionais no tempo total do pipeline.
4. Planejar o runner self-hosted com cache e registry local.
