"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { api } from "@/lib/api-client";
import { getApiErrorMessage } from "@/lib/form-utils";

interface SearchResult {
  id: string;
  entityType: "CUSTOMER" | "REQUEST" | "DOCUMENT";
  title: string;
  snippet: string;
  score: number;
  tenantId: string;
}

interface SearchPage {
  content: SearchResult[];
  page: { number: number; size: number; totalElements: number; totalPages: number };
}

const ENTITY_ICONS: Record<string, string> = {
  CUSTOMER: "👤",
  REQUEST: "📋",
  DOCUMENT: "📄",
};

const ENTITY_ROUTES: Record<string, string> = {
  CUSTOMER: "/admin/customers",
  REQUEST: "/admin/requests",
  DOCUMENT: "/admin/documents",
};

/**
 * Admin unified search page.
 * Wired to GET /api/v1/search?q=...&page=...&size=...
 * Results grouped by entity type, with pagination.
 * Validates: P1.14.9 — /admin/search (task 44)
 */
export default function SearchPage() {
  const searchParams = useSearchParams();
  const router = useRouter();

  const [query, setQuery] = useState(searchParams.get("q") ?? "");
  const [results, setResults] = useState<SearchResult[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const search = useCallback(async (q: string, pageNum: number) => {
    if (!q.trim()) { setResults([]); setTotalElements(0); return; }
    try {
      setLoading(true);
      const data = await api.get<SearchPage>(
        `/search?q=${encodeURIComponent(q)}&page=${pageNum}&size=20`
      );
      setResults(data.content ?? []);
      setTotalElements(data.page?.totalElements ?? 0);
      setError(null);
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (debounceRef.current) clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      void search(query, page);
    }, 300);
    return () => { if (debounceRef.current) clearTimeout(debounceRef.current); };
  }, [query, page, search]);

  // Group by entity type
  const grouped = results.reduce<Record<string, SearchResult[]>>((acc, r) => {
    (acc[r.entityType] ??= []).push(r);
    return acc;
  }, {});

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <h1 className="mb-4 text-2xl font-bold">Search</h1>

      {/* Search input */}
      <div className="mb-6 relative">
        <input
          type="search"
          value={query}
          onChange={(e) => { setQuery(e.target.value); setPage(0); }}
          placeholder="Search customers, requests, documents…"
          className="w-full rounded-lg border px-4 py-3 pr-10 text-sm focus:outline-none focus:ring-2 focus:ring-primary"
          aria-label="Search query"
          autoFocus
        />
        {loading && (
          <span className="absolute right-3 top-3 text-muted-foreground text-xs" aria-live="polite">Searching…</span>
        )}
      </div>

      {error && <div className="mb-4 rounded bg-destructive/10 p-3 text-sm text-destructive" role="alert">{error}</div>}

      {/* Results */}
      {!loading && query && results.length === 0 && (
        <p className="text-center text-muted-foreground py-12">No results for "{query}"</p>
      )}

      {Object.entries(grouped).map(([type, items]) => (
        <section key={type} className="mb-6">
          <h2 className="mb-2 flex items-center gap-2 text-sm font-semibold text-muted-foreground uppercase tracking-wide">
            <span>{ENTITY_ICONS[type]}</span>
            <span>{type}S</span>
            <span className="rounded-full bg-muted px-2 py-0.5 text-xs">{items.length}</span>
          </h2>
          <ul className="divide-y rounded-lg border">
            {items.map((r) => (
              <li key={r.id}>
                <button
                  onClick={() => router.push(`${ENTITY_ROUTES[r.entityType]}/${r.id}`)}
                  className="w-full text-left px-4 py-3 hover:bg-muted/30 transition-colors"
                >
                  <p className="text-sm font-medium">{r.title}</p>
                  {r.snippet && <p className="text-xs text-muted-foreground mt-0.5 line-clamp-2">{r.snippet}</p>}
                </button>
              </li>
            ))}
          </ul>
        </section>
      ))}

      {/* Pagination */}
      {totalElements > 20 && (
        <div className="mt-6 flex justify-center gap-2">
          <button onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={page === 0}
            className="rounded border px-3 py-1 text-sm disabled:opacity-40">Previous</button>
          <span className="px-3 py-1 text-sm text-muted-foreground">Page {page + 1}</span>
          <button onClick={() => setPage((p) => p + 1)} disabled={(page + 1) * 20 >= totalElements}
            className="rounded border px-3 py-1 text-sm disabled:opacity-40">Next</button>
        </div>
      )}
    </div>
  );
}

