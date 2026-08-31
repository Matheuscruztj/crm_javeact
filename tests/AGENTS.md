# AtlasOps AI — AGENTS.md local para `tests/`

> Fonte canônica: consulte o [AGENTS.md raiz](/home/matheus/zona_de_teste/crm_javeact/AGENTS.md) para regras globais de arquitetura, segurança, governança e quality gates.

## Escopo

- suites unitárias, integração, propriedade, contrato, funcional, carga e performance;
- dados de teste, isolamento, fixtures e evidência reproduzível;
- utilitários e artefatos de reporte em `tests/`.

## Convenções

- manter testes determinísticos e isolados;
- preservar nomes de artefatos versionados quando existirem;
- garantir que cenários de carga e performance publiquem saída rastreável;
- não usar dados reais ou segredos.

## Validação relevante

- `make test-unit`
- `make test-integration`
- `make test-functional`
- `make test-load-smoke`
- `make test-load-low-resource`
- `make test-load-low-resource-report`

## Limites

- não alterar código de produção;
- não expor credenciais;
- não tornar suites mais lentas sem justificar o impacto.
