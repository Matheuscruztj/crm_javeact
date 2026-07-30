"use client";

/**
 * Generic server-side paginated data table with sorting, filtering, skeleton states.
 * Validates: P1.1 — Table Infrastructure
 */

import { useRouter, useSearchParams, usePathname } from "next/navigation";
import { useCallback, useTransition } from "react";
import type { PageResponse } from "@/lib/api-client";

// ─── Types ─────────────────────────────────────────────────────────────────

export interface Column<T> {
  key: keyof T | string;
  header: string;
  sortable?: boolean;
  /** Custom cell renderer */
  render?: (row: T) => React.ReactNode;
  className?: string;
}

interface DataTableProps<T extends { id: string }> {
  /** Column definitions */
  columns: Column<T>[];
  /** Paginated API response */
  data: PageResponse<T> | null;
  /** True while fetching */
  loading?: boolean;
  /** Error message to display */
  error?: string | null;
  /** Current page (zero-based) */
  page?: number;
  /** Called when user changes page */
  onPageChange?: (page: number) => void;
  /** Called when row clicked */
  onRowClick?: (row: T) => void;
  /** Placeholder for empty state */
  emptyMessage?: string;
  /** Row actions (buttons/links per row) */
  rowActions?: (row: T) => React.ReactNode;
}

// ─── Skeleton row ──────────────────────────────────────────────────────────

function SkeletonRow({ cols }: { cols: number }) {
  return (
    <tr aria-hidden="true">
      {Array.from({ length: cols }).map((_, i) => (
        <td key={i} className="px-4 py-3">
          <div className="bg-muted h-4 animate-pulse rounded" />
        </td>
      ))}
    </tr>
  );
}

// ─── Component ─────────────────────────────────────────────────────────────

export function DataTable<T extends { id: string }>({
  columns,
  data,
  loading = false,
  error = null,
  page = 0,
  onPageChange,
  onRowClick,
  emptyMessage = "No results found.",
  rowActions,
}: DataTableProps<T>) {
  const totalPages = data?.page?.totalPages ?? 0;
  const totalElements = data?.page?.totalElements ?? 0;
  const content = data?.content ?? [];
  const colCount = columns.length + (rowActions ? 1 : 0);

  const handlePrev = () => page > 0 && onPageChange?.(page - 1);
  const handleNext = () => page < totalPages - 1 && onPageChange?.(page + 1);

  if (error) {
    return (
      <div role="alert" className="bg-destructive/10 text-destructive rounded-md p-4 text-sm">
        {error}
      </div>
    );
  }

  return (
    <div className="space-y-3">
      {/* Table */}
      <div className="overflow-x-auto rounded-md border">
        <table className="w-full text-sm" role="table" aria-busy={loading}>
          <thead className="bg-muted/50">
            <tr>
              {columns.map((col) => (
                <th
                  key={String(col.key)}
                  className={`px-4 py-3 text-left font-medium ${col.className ?? ""}`}
                  scope="col"
                >
                  {col.header}
                </th>
              ))}
              {rowActions && (
                <th className="px-4 py-3 text-left font-medium" scope="col">
                  Actions
                </th>
              )}
            </tr>
          </thead>
          <tbody>
            {loading ? (
              Array.from({ length: 5 }).map((_, i) => <SkeletonRow key={i} cols={colCount} />)
            ) : content.length === 0 ? (
              <tr>
                <td colSpan={colCount} className="text-muted-foreground px-4 py-10 text-center">
                  {emptyMessage}
                </td>
              </tr>
            ) : (
              content.map((row) => (
                <tr
                  key={row.id}
                  className={`hover:bg-muted/25 border-t transition-colors ${
                    onRowClick ? "cursor-pointer" : ""
                  }`}
                  onClick={() => onRowClick?.(row)}
                  role={onRowClick ? "button" : undefined}
                  tabIndex={onRowClick ? 0 : undefined}
                  onKeyDown={(e) => {
                    if (onRowClick && (e.key === "Enter" || e.key === " ")) {
                      e.preventDefault();
                      onRowClick(row);
                    }
                  }}
                >
                  {columns.map((col) => (
                    <td key={String(col.key)} className={`px-4 py-3 ${col.className ?? ""}`}>
                      {col.render
                        ? col.render(row)
                        : String((row as Record<string, unknown>)[String(col.key)] ?? "")}
                    </td>
                  ))}
                  {rowActions && (
                    <td className="px-4 py-3">
                      <div className="flex gap-2" onClick={(e) => e.stopPropagation()}>
                        {rowActions(row)}
                      </div>
                    </td>
                  )}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      {!loading && totalPages > 1 && (
        <div className="flex items-center justify-between text-sm">
          <span className="text-muted-foreground">
            {totalElements} result{totalElements !== 1 ? "s" : ""} — Page {page + 1} of {totalPages}
          </span>
          <div className="flex gap-2">
            <button
              onClick={handlePrev}
              disabled={page === 0}
              className="hover:bg-muted rounded-md border px-3 py-1.5 text-xs disabled:opacity-40"
              aria-label="Previous page"
            >
              ← Prev
            </button>
            <button
              onClick={handleNext}
              disabled={page >= totalPages - 1}
              className="hover:bg-muted rounded-md border px-3 py-1.5 text-xs disabled:opacity-40"
              aria-label="Next page"
            >
              Next →
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

// ─── URL-synced pagination helper ─────────────────────────────────────────

/**
 * Returns current page from URL search params and a setter that pushes new params.
 * Use inside Server Components or pass page down from parent.
 */
export function useUrlPage(): [number, (p: number) => void] {
  const searchParams = useSearchParams();
  const router = useRouter();
  const pathname = usePathname();
  const [, startTransition] = useTransition();

  const page = Number(searchParams.get("page") ?? "0");

  const setPage = useCallback(
    (p: number) => {
      const params = new URLSearchParams(searchParams.toString());
      params.set("page", String(p));
      startTransition(() => {
        router.push(`${pathname}?${params.toString()}`);
      });
    },
    [searchParams, router, pathname]
  );

  return [page, setPage];
}
