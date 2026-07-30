import { renderHook, waitFor, act } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

const api = vi.hoisted(() => ({
  get: vi.fn(),
  post: vi.fn(),
  put: vi.fn(),
  patch: vi.fn(),
  delete: vi.fn(),
}));

vi.mock("@/lib/api-client", () => ({
  api,
}));

import { useApiQuery, useMutation, usePagedQuery } from "./use-api";

describe("useApi hooks", () => {
  beforeEach(() => {
    api.get.mockReset();
    api.post.mockReset();
    api.put.mockReset();
    api.patch.mockReset();
    api.delete.mockReset();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it("loads data and exposes refetch", async () => {
    api.get.mockResolvedValueOnce({ id: "customer-1" });

    const { result } = renderHook(() => useApiQuery<{ id: string }>("/customers/1"));

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(result.current.data).toEqual({ id: "customer-1" });

    api.get.mockResolvedValueOnce({ id: "customer-2" });

    await act(async () => {
      await result.current.refetch();
    });

    expect(api.get).toHaveBeenCalledTimes(2);
    expect(result.current.data).toEqual({ id: "customer-2" });
  });

  it("retries once on 503 and falls back to a generic error when needed", async () => {
    vi.useFakeTimers();
    api.get
      .mockRejectedValueOnce({ status: 503, title: "Service unavailable" })
      .mockRejectedValueOnce({ status: 503, title: "Service unavailable" });

    const { result } = renderHook(() => useApiQuery<{ id: string }>("/customers/1"));

    await act(async () => {
      await vi.advanceTimersByTimeAsync(1000);
    });

    expect(result.current.loading).toBe(false);
    expect(api.get).toHaveBeenCalledTimes(2);
    expect(result.current.error).toEqual({ status: 503, title: "Service unavailable" });

    api.get.mockReset();
    api.get.mockRejectedValueOnce(undefined);

    const { result: emptyErrorResult } = renderHook(() => useApiQuery<{ id: string }>("/customers/2"));

    await act(async () => {
      await Promise.resolve();
    });

    expect(emptyErrorResult.current.loading).toBe(false);
    expect(emptyErrorResult.current.error).toEqual({
      status: 500,
      title: "Unknown error",
      code: "INTERNAL",
      detail: "",
      traceId: "",
      type: "",
    });
  });

  it("builds paged urls", async () => {
    api.get.mockResolvedValueOnce({ content: [], page: { number: 0, size: 20, totalElements: 0, totalPages: 0 } });

    const { result } = renderHook(() => usePagedQuery<{ id: string }>("/customers", 2, 50));

    await waitFor(() => expect(result.current.loading).toBe(false));
    expect(api.get).toHaveBeenCalledWith("/customers?page=2&size=50");
    expect(result.current.page).toBe(2);
    expect(result.current.pageSize).toBe(50);
  });

  it("creates mutations and surfaces api errors", async () => {
    api.post.mockResolvedValueOnce({ id: "req-1" });
    const { result } = renderHook(() => useMutation<{ id: string }, { title: string }>("/requests"));

    await act(async () => {
      await expect(result.current.mutate({ title: "Need help" })).resolves.toEqual({ id: "req-1" });
    });

    api.post.mockRejectedValueOnce({ status: 400, title: "Bad request" });
    await expect(result.current.mutate({ title: "Bad" })).rejects.toEqual({
      status: 400,
      title: "Bad request",
    });

    act(() => {
      result.current.reset();
    });
    expect(result.current.error).toBeNull();
  });

  it("returns null state when query url is null and supports page updates", async () => {
    const { result } = renderHook(() => useApiQuery<{ id: string }>(null));
    expect(result.current.loading).toBe(false);
    expect(result.current.data).toBeNull();
    expect(result.current.error).toBeNull();

    api.get.mockResolvedValueOnce({ content: [], page: { number: 0, size: 20, totalElements: 0, totalPages: 0 } });
    const paged = renderHook(() => usePagedQuery<{ id: string }>("/customers", 0, 20));
    await waitFor(() => expect(paged.result.current.loading).toBe(false));

    api.get.mockResolvedValueOnce({ content: [], page: { number: 1, size: 20, totalElements: 0, totalPages: 0 } });
    await act(async () => {
      paged.result.current.setPage(1);
    });

    await waitFor(() => expect(api.get).toHaveBeenLastCalledWith("/customers?page=1&size=20"));
  });
});
