# ADR-012 — Next.js 15 App Router with React 19 Server/Client Components

**Status:** Accepted | **Date:** 2025-01-20

## Context

The frontend serves two distinct user types (ADMIN portal, CLIENT portal) with different layouts and access patterns. We need to decide on routing strategy, state management, and rendering approach.

## Decision

Use **Next.js 15 App Router** with Server Components for layouts and Client Components (`"use client"`) for interactive pages.

- Route groups: `app/admin/` and `app/portal/` with independent layouts
- Tailwind CSS v4 exclusively (no styled-components or CSS Modules)
- shadcn/ui (Radix UI primitives) as component base
- React Hook Form + Zod for form validation
- `useApiQuery`/`usePagedQuery`/`useMutation` hooks for data fetching
- No global state library — hooks + React Context for shared state

## Consequences

**Positive:** SSR for SEO/initial load; nested layouts with code splitting; built-in caching; TypeScript strict mode  
**Negative:** `"use client"` boundary complexity; no server-side API calls in client components  
**Mitigation:** All API calls from client hooks; middleware handles auth redirects
