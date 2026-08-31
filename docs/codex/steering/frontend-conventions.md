# Convenções Frontend — AtlasOps AI

> **Versão:** 1.0  
> **Última atualização:** 2025-01-20

---

## Stack

- **React 19** com Server Components e Client Components
- **Next.js 15** com App Router (diretório `app/`)
- **TypeScript** com `strict: true` habilitado (nenhum `any` permitido)
- **Tailwind CSS v4** como framework de estilização exclusivo
- **shadcn/ui** como biblioteca de componentes base (Radix UI primitives)
- **pnpm** como package manager

---

## Estrutura de Diretórios

```
frontend/
├── app/                    # Rotas (App Router)
│   ├── globals.css         # Design tokens (CSS variables)
│   ├── layout.tsx          # Root layout
│   ├── admin/              # Rotas administrativas
│   │   ├── layout.tsx      # Layout admin (sidebar + header)
│   │   └── {rota}/page.tsx
│   └── portal/             # Rotas portal cliente
│       ├── layout.tsx      # Layout portal (nav simplificada)
│       └── {rota}/page.tsx
├── components/
│   ├── ui/                 # Componentes shadcn/ui gerados (não editar manualmente)
│   ├── shared/             # Componentes reutilizáveis compostos (layouts, navigation, data-table)
│   ├── admin/              # Componentes específicos do admin
│   └── portal/             # Componentes específicos do portal cliente
├── lib/                    # Utils, API client, helpers
├── hooks/                  # Custom hooks
├── package.json
├── tsconfig.json
├── next.config.ts
├── tailwind.config.ts
└── components.json         # Configuração shadcn/ui
```

### Regras de organização

- Cada rota é um arquivo `page.tsx` dentro de `app/`
- Layouts de seção em `layout.tsx` no diretório correspondente
- Componentes de UI reutilizáveis em `components/shared/`
- Componentes específicos de contexto em `components/admin/` ou `components/portal/`
- Nunca colocar lógica de negócio diretamente em `app/` — extrair para `hooks/` ou `lib/`

---

## Naming

| Tipo                   | Convenção          | Exemplo                                   |
| ---------------------- | ------------------ | ----------------------------------------- |
| Componente (export)    | PascalCase         | `CustomerTable`, `SidebarNav`             |
| Arquivo de componente  | kebab-case         | `customer-table.tsx`, `sidebar-nav.tsx`   |
| Hook                   | `use` prefix       | `useCustomers`, `useDebounce`             |
| Arquivo de hook        | kebab-case         | `use-customers.ts`, `use-debounce.ts`     |
| Constante              | UPPER_SNAKE_CASE   | `MAX_PAGE_SIZE`, `API_BASE_URL`           |
| Tipo/Interface (props) | PascalCase + Props | `CustomerTableProps`, `SidebarNavProps`   |
| Utilitário             | camelCase          | `formatDate`, `buildQueryString`          |
| Arquivo de utilitário  | kebab-case         | `format-date.ts`, `build-query-string.ts` |
| Diretório de rota      | kebab-case         | `admin/customers/[id]/`                   |

### Exemplos

```typescript
// components/shared/customer-table.tsx
export function CustomerTable({ customers }: CustomerTableProps) { ... }

// hooks/use-customers.ts
export function useCustomers(tenantId: string) { ... }

// lib/constants.ts
export const MAX_PAGE_SIZE = 100;
export const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL;
```

---

## Estilização

### Tailwind CSS — Uso exclusivo

- Utilizar **apenas** Tailwind utility classes para estilização
- Usar `cn()` (clsx + tailwind-merge) para composição condicional de classes
- CSS variables para design tokens seguindo convenção shadcn/ui
- Breakpoints responsivos via classes Tailwind (`sm:`, `md:`, `lg:`, `xl:`, `2xl:`)

### Função `cn()` — Composição condicional

```typescript
import { cn } from "@/lib/utils";

export function Button({ variant, className, ...props }: ButtonProps) {
  return (
    <button
      className={cn(
        "rounded-md px-4 py-2 font-medium",
        variant === "primary" && "bg-primary text-primary-foreground",
        variant === "ghost" && "hover:bg-accent hover:text-accent-foreground",
        className
      )}
      {...props}
    />
  );
}
```

### Design Tokens (CSS Variables)

Tokens definidos em `app/globals.css` seguindo padrão shadcn/ui:

```css
:root {
  --background: 0 0% 100%;
  --foreground: 222.2 84% 4.9%;
  --primary: 222.2 47.4% 11.2%;
  --secondary: 210 40% 96.1%;
  --muted: 210 40% 96.1%;
  --accent: 210 40% 96.1%;
  --destructive: 0 84.2% 60.2%;
  --border: 214.3 31.8% 91.4%;
  --input: 214.3 31.8% 91.4%;
  --ring: 222.2 84% 4.9%;
  --radius: 0.5rem;
}

.dark {
  --background: 222.2 84% 4.9%;
  --foreground: 210 40% 98%;
  /* ... */
}
```

### Proibições de estilização

- ❌ CSS Modules (`*.module.css`)
- ❌ styled-components ou Emotion
- ❌ Sass/SCSS
- ❌ `@apply` excessivo (preferir classes inline)
- ❌ Valores hardcoded de cores (usar tokens)

---

## Componentes

### Hierarquia de decisão

1. **shadcn/ui** — Sempre verificar se existe um componente shadcn/ui antes de criar custom
2. **Composição** — Compor componentes shadcn/ui para criar componentes maiores
3. **Custom** — Criar componente custom apenas quando não há primitiva adequada

### Props tipadas

Todo componente deve ter interface de props dedicada:

```typescript
interface DataTableProps<T> {
  data: T[];
  columns: ColumnDef<T>[];
  isLoading?: boolean;
  onRowClick?: (row: T) => void;
}

export function DataTable<T>({
  data,
  columns,
  isLoading,
  onRowClick,
}: DataTableProps<T>) {
  // ...
}
```

### Composição sobre herança

```typescript
// ✅ Correto — composição
export function CustomerCard({ customer }: CustomerCardProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>{customer.name}</CardTitle>
      </CardHeader>
      <CardContent>
        <Badge variant={customer.status === "active" ? "default" : "secondary"}>
          {customer.status}
        </Badge>
      </CardContent>
    </Card>
  );
}

// ❌ Incorreto — herança / extensão de componente
class CustomerCard extends Card { ... }
```

### Evitar prop drilling

- Usar React Context para estado de UI compartilhado (theme, sidebar state)
- Usar hooks customizados para estado de negócio (`useCustomers`, `useAuth`)
- Máximo **3 níveis** de passagem de props antes de extrair para context ou hook

---

## Responsividade

### Mobile-first approach

Escrever estilos base para mobile, adicionar complexidade com breakpoints:

```typescript
// ✅ Correto — mobile-first
<div className="flex flex-col gap-2 md:flex-row md:gap-4 lg:gap-6">

// ❌ Incorreto — desktop-first
<div className="flex flex-row gap-6 sm:flex-col sm:gap-2">
```

### Breakpoints Tailwind padrão

| Breakpoint | Tamanho | Uso                   |
| ---------- | ------- | --------------------- |
| `sm`       | 640px   | Smartphones landscape |
| `md`       | 768px   | Tablets               |
| `lg`       | 1024px  | Desktop               |
| `xl`       | 1280px  | Desktop wide          |
| `2xl`      | 1536px  | Desktop ultra-wide    |

### Layouts adaptativos obrigatórios

| Contexto      | Mobile (<768px)          | Tablet (768–1023px)          | Desktop (≥1024px)             |
| ------------- | ------------------------ | ---------------------------- | ----------------------------- |
| Admin sidebar | Overlay (hamburger menu) | Colapsável (ícones)          | Persistente (ícones + labels) |
| Portal nav    | Bottom navigation        | Sidebar compacta             | Sidebar com labels            |
| Tabelas       | Cards empilhados         | Tabela com scroll horizontal | Tabela densa completa         |
| Filtros       | Bottom sheet / drawer    | Drawer lateral               | Inline acima da tabela        |
| Formulários   | Stack vertical completo  | 2 colunas                    | 2–3 colunas                   |

### Regras de responsividade

- **Portal cliente**: totalmente usável em mobile (todas as funcionalidades acessíveis)
- **Admin**: inspecionável em mobile com funcionalidade completa em desktop
- Todas as páginas devem ter layout adaptativo — nenhuma página pode quebrar em mobile
- Testar em viewports: 375px, 768px, 1024px, 1440px

---

## Acessibilidade

### Radix UI — Acessibilidade nativa

Componentes shadcn/ui (baseados em Radix UI) preservam acessibilidade por padrão:

- Gerenciamento de foco automático em modais e dropdowns
- Roles ARIA corretas sem configuração manual
- Suporte a screen readers nativo
- Navegação por teclado built-in

### Regras obrigatórias

| Regra                  | Descrição                                                              |
| ---------------------- | ---------------------------------------------------------------------- |
| Labels em forms        | Todo `<input>` deve ter `<Label>` associado (via `htmlFor` ou nesting) |
| Contraste WCAG AA      | Mínimo 4.5:1 para texto normal, 3:1 para texto grande                  |
| Navegação por teclado  | Todos os elementos interativos acessíveis via Tab/Enter/Space/Escape   |
| aria-labels em ícones  | Ícones interativos (botões com apenas ícone) devem ter `aria-label`    |
| Alt text em imagens    | Todas as imagens decorativas: `alt=""`, informativas: alt descritivo   |
| Focus visible          | Indicador visual de foco obrigatório (ring do Tailwind)                |
| Hierarquia de headings | H1 → H2 → H3 sem pular níveis                                          |
| Anúncio de estado      | Loading states anunciados via `aria-live` ou `aria-busy`               |

### Exemplo

```typescript
// ✅ Correto
<Button variant="ghost" size="icon" aria-label="Fechar diálogo">
  <X className="h-4 w-4" />
</Button>

<Label htmlFor="customer-name">Nome do cliente</Label>
<Input id="customer-name" placeholder="Digite o nome..." />

// ❌ Incorreto
<Button variant="ghost" size="icon">
  <X className="h-4 w-4" />
</Button>

<Input placeholder="Nome..." />  {/* sem label */}
```

---

## Linting

### Ferramentas obrigatórias

| Ferramenta                | Responsabilidade                  |
| ------------------------- | --------------------------------- |
| ESLint                    | Análise estática + regras Next.js |
| Prettier                  | Formatação consistente            |
| eslint-plugin-tailwindcss | Ordenação de classes Tailwind     |

### Configuração base

```json
// .eslintrc.json
{
  "extends": [
    "next/core-web-vitals",
    "next/typescript",
    "plugin:tailwindcss/recommended"
  ],
  "rules": {
    "@typescript-eslint/no-explicit-any": "error",
    "@typescript-eslint/no-unused-vars": "error",
    "tailwindcss/classnames-order": "warn",
    "tailwindcss/no-custom-classname": "off"
  }
}
```

### Prettier

```json
// .prettierrc
{
  "semi": true,
  "singleQuote": false,
  "tabWidth": 2,
  "trailingComma": "es5",
  "printWidth": 100,
  "plugins": ["prettier-plugin-tailwindcss"]
}
```

---

## Proibições

- ❌ CSS inline via `style={}` — exceto valores dinâmicos calculados em runtime (ex: `style={{ width: `${progress}%` }}`)
- ❌ `!important` em qualquer lugar — resolver especificidade com `cn()` ou reestruturação
- ❌ Componentes sem tipagem TypeScript — todo componente deve ter interface de props
- ❌ `any` type — usar tipos específicos, generics, ou `unknown` com type guards
- ❌ Lógica de negócio em componentes de UI — separar em hooks (`hooks/`) ou services (`lib/`)
- ❌ `useEffect` para fetch de dados — usar React Server Components ou hooks de data fetching
- ❌ Estado global sem justificativa — preferir estado local e composição
- ❌ Imports relativos profundos (`../../../`) — usar path aliases (`@/components/`, `@/lib/`)
- ❌ Componentes com mais de 200 linhas — extrair sub-componentes
- ❌ Props opcionais sem default — definir defaults explícitos ou tratar `undefined`
