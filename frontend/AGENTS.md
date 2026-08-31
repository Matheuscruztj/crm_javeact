# AtlasOps AI — AGENTS.md local para `frontend/`

> Fonte canônica: consulte o [AGENTS.md raiz](/home/matheus/zona_de_teste/crm_javeact/AGENTS.md) para regras globais de arquitetura, segurança, governança e quality gates.

## Escopo

- aplicação React/Next.js;
- rotas administrativas e portal;
- contratos de apresentação, acessibilidade e validação frontend;
- testes unitários, E2E e de performance frontend.

## Convenções

- preservar o contrato da API consumida;
- manter componentes e rotas alinhados ao fluxo de produto;
- evitar acoplamento indevido entre áreas administrativas e portal;
- usar validação e smoke tests como evidência antes de mudanças amplas.

## Validação relevante

- `npm run lint`
- `npm run typecheck`
- `npm run test`
- `npm run test:e2e`
- `npm run test:performance`

## Limites

- não introduzir segredos;
- não alterar contratos de backend sem coordenação;
- manter alterações compatíveis com o design system existente.
