/**
 * Typed data-fetching hooks built on top of the API client.
 * Provides SWR-like semantics: { data, error, loading, refetch }.
 *
 * Validates: P0.L.2 — Frontend API Client Tipado
 */
"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { api, type ApiError, type PageResponse } from "@/lib/api-client";

// ─── Generic useApiQuery ───────────────────────────────────────────────────

interface UseApiQueryResult<T> {
  data: T | null;
  error: ApiError | null;
  loading: boolean;
  refetch: () => void;
}

/**
 * Generic hook for GET requests. Refetches when `url` changes.
 * Automatically retries once on 503.
 */
export function useApiQuery<T>(url: string | null): UseApiQueryResult<T> {
  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<ApiError | null>(null);
  const [loading, setLoading] = useState(!!url);
  const abortRef = useRef<AbortController | null>(null);

  const fetch = useCallback(async () => {
    if (!url) return;

    abortRef.current?.abort();
    const controller = new AbortController();
    abortRef.current = controller;

    setLoading(true);
    setError(null);

    try {
      const result = await api.get<T>(url);
      if (!controller.signal.aborted) {
        setData(result);
      }
    } catch (err) {
      if (!controller.signal.aborted) {
        const apiErr = err as ApiError;
        // Auto-retry once on 503
        if (apiErr?.status === 503) {
          await new Promise((r) => setTimeout(r, 1000));
          try {
            const retried = await api.get<T>(url);
            setData(retried);
            return;
          } catch {
            /* fall through to setError */
          }
        }
        setError(apiErr ?? { status: 500, title: "Unknown error", code: "INTERNAL", detail: "", traceId: "", type: "" });
      }
    } finally {
      if (!controller.signal.aborted) setLoading(false);
    }
  }, [url]);

  useEffect(() => {
    fetch();
    return () => abortRef.current?.abort();
  }, [fetch]);

  return { data, error, loading, refetch: fetch };
}

// ─── Paginated query ───────────────────────────────────────────────────────

interface UsePagedQueryResult<T> extends UseApiQueryResult<PageResponse<T>> {
  page: number;
  setPage: (p: number) => void;
  pageSize: number;
}

export function usePagedQuery<T>(
  baseUrl: string | null,
  initialPage = 0,
  pageSize = 20,
): UsePagedQueryResult<T> {
  const [page, setPage] = useState(initialPage);
  const url = baseUrl ? `${baseUrl}?page=${page}&size=${pageSize}` : null;
  const result = useApiQuery<PageResponse<T>>(url);

  return { ...result, page, setPage, pageSize };
}

// ─── Mutation hook ─────────────────────────────────────────────────────────

interface UseMutationResult<TData, TInput> {
  mutate: (input: TInput) => Promise<TData>;
  loading: boolean;
  error: ApiError | null;
  reset: () => void;
}

type HttpMutationMethod = "post" | "put" | "patch" | "delete";

export function useMutation<TData = void, TInput = unknown>(
  url: string,
  method: HttpMutationMethod = "post",
): UseMutationResult<TData, TInput> {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<ApiError | null>(null);

  const mutate = useCallback(
    async (input: TInput): Promise<TData> => {
      setLoading(true);
      setError(null);
      try {
        const result = await api[method]<TData>(url, input);
        return result;
      } catch (err) {
        const apiErr = err as ApiError;
        setError(apiErr);
        throw apiErr;
      } finally {
        setLoading(false);
      }
    },
    [url, method],
  );

  return { mutate, loading, error, reset: () => setError(null) };
}

// ─── Domain hooks ──────────────────────────────────────────────────────────

export interface Customer {
  id: string;
  name: string;
  email: string;
  status: "ACTIVE" | "INACTIVE";
  createdAt: string;
}

export interface ServiceRequest {
  id: string;
  title: string;
  status: string;
  priority: string;
  customerId: string;
  createdAt: string;
}

export interface Document {
  id: string;
  filename: string;
  status: string;
  contentType: string;
  createdAt: string;
}

export interface Notification {
  id: string;
  title: string;
  message: string;
  read: boolean;
  link?: string;
  createdAt: string;
}

export const useCustomers = (page = 0) =>
  usePagedQuery<Customer>("/customers", page);

export const useCustomer = (id: string | null) =>
  useApiQuery<Customer>(id ? `/customers/${id}` : null);

export const useRequests = (page = 0) =>
  usePagedQuery<ServiceRequest>("/requests", page);

export const useRequest = (id: string | null) =>
  useApiQuery<ServiceRequest>(id ? `/requests/${id}` : null);

export const useDocuments = (page = 0) =>
  usePagedQuery<Document>("/documents", page);

export const useDocument = (id: string | null) =>
  useApiQuery<Document>(id ? `/documents/${id}` : null);

export const useNotifications = (page = 0) =>
  usePagedQuery<Notification>("/notifications", page);

export const useUnreadCount = () =>
  useApiQuery<{ count: number }>("/notifications/unread-count");
