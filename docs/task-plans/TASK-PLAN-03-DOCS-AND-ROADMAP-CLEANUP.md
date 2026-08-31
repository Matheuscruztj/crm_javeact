# Task Plan 03 — Docs and Roadmap Cleanup Last

> Prioridade: somente apos codigo/configuracao e validacoes tecnicas

## 1. Corrigir claims excessivas em roadmap

### Evidencia

- o roadmap histórico removido marcava `P0`, `P1`, `P2` e `P3` como completos;
- os gates reais de coverage, contrato e quality enforcement ainda nao sustentam essa afirmacao.

### Atividades

- reclassificar itens como:
  - `implemented but not fully validated`
  - `validated and blocking`
  - `documented only`
- remover status `COMPLETO` de fases cuja validacao tecnica ainda falha.

## 2. Atualizar roadmap de quality com base no estado real

### Atividades

- manter em `docs/ROADMAP-QUALITY-ENFORCEMENT-WAVES.md` apenas conclusoes com evidencia executavel;
- mover itens como `verify-contracts` placeholder, AsyncAPI lint incorreto, nightly tolerante a falha e cobertura abaixo do gate para a lista de pendencias reais;
- registrar datas absolutas de validacao sempre que um item for marcado como concluido.

## 3. Consolidar documentacao operacional de qualidade

### Atividades

- alinhar `docs/testing/QUALITY-TESTING-CICD.md` e `docs/00-current-status.md` com os comandos que realmente executam;
- remover referencias a comandos inexistentes ou placeholders;
- listar separadamente:
  - comandos locais rapidos
  - comandos completos
  - checks advisory
  - checks blocking

## 4. Manter a pasta `docs/task-plans` enxuta

### Regra

- nenhum plano novo deve voltar a misturar feature roadmap, quality backlog e documentacao em um unico arquivo;
- novos planos devem sempre obedecer esta ordem:
  1. codigo/configuracao
  2. validacao/checks
  3. documentacao
