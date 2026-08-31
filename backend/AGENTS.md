# AtlasOps AI — AGENTS.md local para `backend/`

> Fonte canônica: consulte o [AGENTS.md raiz](/home/matheus/zona_de_teste/crm_javeact/AGENTS.md) para regras globais de arquitetura, segurança, governança e quality gates.

## Escopo

- backend Spring Boot multi-módulo;
- arquitetura hexagonal por módulo;
- testes unitários, integração e propriedade no backend;
- validação de build, lint, cobertura e arquitetura.

## Convenções

- respeitar `presentation -> application -> domain <- infrastructure`;
- manter dependências de domínio livres de framework;
- preservar isolamento multi-tenant e ports/adapters;
- preferir mudanças pequenas e verificáveis por módulo.

## Validação relevante

- `make test-unit`
- `make test-integration`
- `make verify`
- `make lint`
- testes ArchUnit quando afetarem fronteiras de módulo.

## Arquivos e limites

- evitar alteração de migrations já aplicadas;
- respeitar configurações de segurança protegidas;
- não cruzar módulos sem justificativa arquitetural explícita.
