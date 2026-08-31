# AtlasOps AI — AGENTS.md local para `docs/`

> Fonte canônica: consulte o [AGENTS.md raiz](/home/matheus/zona_de_teste/crm_javeact/AGENTS.md) para regras globais de arquitetura, segurança, governança e quality gates.

## Escopo

- documentação técnica;
- ADRs;
- runbooks;
- status do projeto;
- backlog documental e evidências;
- diagramas e guias operacionais.

## Responsabilidades

- manter coerência entre `docs/00-current-status.md`, `docs/PENDING-ACTIVITIES.md` e os backlogs especializados;
- registrar evidências de execução sem expor segredos ou dados sensíveis;
- preservar links absolutos e caminhos reprodutíveis em artefatos documentais;
- evitar duplicação de regras que já existem no AGENTS raiz.

## Validação relevante

- revisar consistência textual e links internos;
- confirmar que docs citados refletem o estado atual do repositório;
- quando um runbook mudar, validar que os comandos continuam existentes no `Makefile`.

## Limites

- não alterar segredos, configs de produção ou migrations;
- não contradizer o AGENTS raiz;
- não introduzir instruções específicas de implementação de código fora do contexto documental.
