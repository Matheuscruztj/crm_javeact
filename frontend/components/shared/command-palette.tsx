/**
 * Global Search Command Palette Component.
 * Task 24.1: Implement global search with command palette (Ctrl+K / Cmd+K).
 * - Display results grouped by entity type (customers, requests, documents)
 * - Keyboard navigation (arrow keys, Enter to select, Escape to close)
 * - Debounce input by 300ms
 * - Loading skeleton while fetching, "No results found" for empty results
 * - Navigate to entity detail page on selection
 * Requirements: 24.1, 24.2, 24.3, 24.4, 24.5, 24.6, 24.7
 */

"use client";

import { useCallback, useEffect, useState, useRef, KeyboardEvent } from "react";
import { useRouter } from "next/navigation";
import { Dialog, DialogContent } from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import { api } from "@/lib/api-client";
import { cn } from "@/lib/utils";

interface SearchResult {
  id: string;
  entityType: "CUSTOMER" | "REQUEST" | "DOCUMENT";
  title: string;
  subtitle: string;
  highlight?: string;
}

interface SearchResponse {
  content: SearchResult[];
  page: {
    totalElements: number;
  };
}

function getEntityIcon(
  entityType: SearchResult["entityType"],
): React.ReactNode {
  switch (entityType) {
    case "CUSTOMER":
      return (
        <svg
          className="h-4 w-4"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          aria-hidden="true"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
          />
        </svg>
      );
    case "REQUEST":
      return (
        <svg
          className="h-4 w-4"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          aria-hidden="true"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
          />
        </svg>
      );
    case "DOCUMENT":
      return (
        <svg
          className="h-4 w-4"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          aria-hidden="true"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
          />
        </svg>
      );
    default:
      return null;
  }
}

function getEntityLabel(entityType: SearchResult["entityType"]): string {
  switch (entityType) {
    case "CUSTOMER":
      return "Clientes";
    case "REQUEST":
      return "Solicitações";
    case "DOCUMENT":
      return "Documentos";
    default:
      return entityType;
  }
}

function getEntityPath(result: SearchResult, isPortal: boolean): string {
  const base = isPortal ? "/portal" : "/admin";
  switch (result.entityType) {
    case "CUSTOMER":
      return `${base}/customers/${result.id}`;
    case "REQUEST":
      return `${base}/requests/${result.id}`;
    case "DOCUMENT":
      return `${base}/documents/${result.id}`;
    default:
      return base;
  }
}

interface CommandPaletteProps {
  isPortal?: boolean;
}

export function CommandPalette({ isPortal = false }: CommandPaletteProps) {
  const router = useRouter();
  const [isOpen, setIsOpen] = useState(false);
  const [query, setQuery] = useState("");
  const [results, setResults] = useState<SearchResult[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const inputRef = useRef<HTMLInputElement>(null);
  const listRef = useRef<HTMLDivElement>(null);

  // Group results by entity type
  const groupedResults = results.reduce(
    (acc, result) => {
      if (!acc[result.entityType]) {
        acc[result.entityType] = [];
      }
      acc[result.entityType].push(result);
      return acc;
    },
    {} as Record<string, SearchResult[]>,
  );

  // Flatten for keyboard navigation
  const flatResults = Object.values(groupedResults).flat();

  // Search with debounce
  useEffect(() => {
    if (!query.trim() || query.length < 2) {
      setResults([]);
      return;
    }

    const timer = setTimeout(async () => {
      setIsLoading(true);
      try {
        const response = await api.get<SearchResponse>(
          `/search?q=${encodeURIComponent(query)}&size=20`,
        );
        setResults(response.content);
        setSelectedIndex(0);
      } catch {
        setResults([]);
      } finally {
        setIsLoading(false);
      }
    }, 300);

    return () => clearTimeout(timer);
  }, [query]);

  // Keyboard shortcut to open (Ctrl+K / Cmd+K)
  useEffect(() => {
    const handleKeyDown = (e: globalThis.KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === "k") {
        e.preventDefault();
        setIsOpen(true);
      }
    };

    document.addEventListener("keydown", handleKeyDown);
    return () => document.removeEventListener("keydown", handleKeyDown);
  }, []);

  // Focus input when dialog opens
  useEffect(() => {
    if (isOpen) {
      setTimeout(() => inputRef.current?.focus(), 0);
    } else {
      setQuery("");
      setResults([]);
      setSelectedIndex(0);
    }
  }, [isOpen]);

  // Scroll selected item into view
  useEffect(() => {
    if (listRef.current && flatResults.length > 0) {
      const selectedElement = listRef.current.querySelector(
        `[data-index="${selectedIndex}"]`,
      );
      if (selectedElement) {
        selectedElement.scrollIntoView({ block: "nearest" });
      }
    }
  }, [selectedIndex, flatResults.length]);

  const handleSelect = useCallback(
    (result: SearchResult) => {
      setIsOpen(false);
      router.push(getEntityPath(result, isPortal));
    },
    [router, isPortal],
  );

  const handleKeyDown = useCallback(
    (e: KeyboardEvent<HTMLInputElement>) => {
      switch (e.key) {
        case "ArrowDown":
          e.preventDefault();
          setSelectedIndex((prev) =>
            prev < flatResults.length - 1 ? prev + 1 : prev,
          );
          break;
        case "ArrowUp":
          e.preventDefault();
          setSelectedIndex((prev) => (prev > 0 ? prev - 1 : 0));
          break;
        case "Enter":
          e.preventDefault();
          if (flatResults[selectedIndex]) {
            handleSelect(flatResults[selectedIndex]);
          }
          break;
        case "Escape":
          e.preventDefault();
          setIsOpen(false);
          break;
      }
    },
    [flatResults, selectedIndex, handleSelect],
  );

  let flatIndex = 0;

  return (
    <>
      {/* Trigger Button */}
      <button
        onClick={() => setIsOpen(true)}
        className="flex items-center gap-2 rounded-md border border-input bg-background px-3 py-2 text-sm text-muted-foreground hover:bg-accent hover:text-accent-foreground"
      >
        <svg
          className="h-4 w-4"
          fill="none"
          viewBox="0 0 24 24"
          stroke="currentColor"
          aria-hidden="true"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
          />
        </svg>
        <span className="hidden sm:inline">Buscar...</span>
        <kbd className="pointer-events-none hidden h-5 select-none items-center gap-1 rounded border bg-muted px-1.5 font-mono text-xs font-medium opacity-100 sm:flex">
          <span className="text-xs">⌘</span>K
        </kbd>
      </button>

      {/* Dialog */}
      <Dialog open={isOpen} onOpenChange={setIsOpen}>
        <DialogContent className="gap-0 p-0 sm:max-w-xl">
          {/* Search Input */}
          <div className="flex items-center border-b px-3">
            <svg
              className="mr-2 h-4 w-4 shrink-0 text-muted-foreground"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              aria-hidden="true"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
              />
            </svg>
            <Input
              ref={inputRef}
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Buscar clientes, solicitações, documentos..."
              className="h-12 border-0 focus-visible:ring-0 focus-visible:ring-offset-0"
            />
            {isLoading && (
              <svg
                className="h-4 w-4 animate-spin text-muted-foreground"
                fill="none"
                viewBox="0 0 24 24"
              >
                <circle
                  className="opacity-25"
                  cx="12"
                  cy="12"
                  r="10"
                  stroke="currentColor"
                  strokeWidth="4"
                />
                <path
                  className="opacity-75"
                  fill="currentColor"
                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                />
              </svg>
            )}
          </div>

          {/* Results */}
          <div ref={listRef} className="max-h-96 overflow-y-auto p-2">
            {query.length < 2 ? (
              <div className="px-4 py-8 text-center text-sm text-muted-foreground">
                Digite pelo menos 2 caracteres para buscar
              </div>
            ) : isLoading ? (
              <div className="space-y-4 p-2">
                {["Clientes", "Solicitações", "Documentos"].map((group) => (
                  <div key={group}>
                    <div className="px-2 py-1.5 text-xs font-medium text-muted-foreground">
                      {group}
                    </div>
                    {Array.from({ length: 2 }).map((_, i) => (
                      <div
                        key={i}
                        className="flex items-center gap-3 px-2 py-2"
                      >
                        <Skeleton className="h-4 w-4" />
                        <div className="flex-1">
                          <Skeleton className="h-4 w-32 mb-1" />
                          <Skeleton className="h-3 w-24" />
                        </div>
                      </div>
                    ))}
                  </div>
                ))}
              </div>
            ) : results.length === 0 ? (
              <div className="px-4 py-8 text-center text-sm text-muted-foreground">
                Nenhum resultado encontrado para "{query}"
              </div>
            ) : (
              Object.entries(groupedResults).map(([entityType, items]) => (
                <div key={entityType} className="mb-4">
                  <div className="px-2 py-1.5 text-xs font-medium text-muted-foreground">
                    {getEntityLabel(entityType as SearchResult["entityType"])}
                  </div>
                  {items.map((result) => {
                    const currentIndex = flatIndex++;
                    return (
                      <button
                        key={result.id}
                        data-index={currentIndex}
                        onClick={() => handleSelect(result)}
                        className={cn(
                          "flex w-full items-center gap-3 rounded-md px-2 py-2 text-left text-sm transition-colors",
                          currentIndex === selectedIndex
                            ? "bg-accent text-accent-foreground"
                            : "hover:bg-accent/50",
                        )}
                      >
                        <span className="text-muted-foreground">
                          {getEntityIcon(result.entityType)}
                        </span>
                        <div className="flex-1 min-w-0">
                          <div className="font-medium truncate">
                            {result.title}
                          </div>
                          <div className="text-xs text-muted-foreground truncate">
                            {result.subtitle}
                          </div>
                        </div>
                        <svg
                          className="h-4 w-4 text-muted-foreground"
                          fill="none"
                          viewBox="0 0 24 24"
                          stroke="currentColor"
                          aria-hidden="true"
                        >
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M9 5l7 7-7 7"
                          />
                        </svg>
                      </button>
                    );
                  })}
                </div>
              ))
            )}
          </div>

          {/* Footer */}
          <div className="flex items-center justify-between border-t px-3 py-2 text-xs text-muted-foreground">
            <div className="flex gap-2">
              <span className="flex items-center gap-1">
                <kbd className="rounded border bg-muted px-1">↑↓</kbd> navegar
              </span>
              <span className="flex items-center gap-1">
                <kbd className="rounded border bg-muted px-1">↵</kbd> selecionar
              </span>
              <span className="flex items-center gap-1">
                <kbd className="rounded border bg-muted px-1">esc</kbd> fechar
              </span>
            </div>
            {results.length > 0 && <span>{results.length} resultado(s)</span>}
          </div>
        </DialogContent>
      </Dialog>
    </>
  );
}
