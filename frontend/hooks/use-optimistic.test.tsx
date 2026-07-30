import { renderHook, act } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { useOptimistic } from "./use-optimistic";

describe("useOptimistic", () => {
  it("applies optimistic update and commits server response", async () => {
    const mutation = vi.fn().mockResolvedValue("server");
    const { result } = renderHook(() => useOptimistic("initial"));

    await act(async () => {
      await result.current.optimisticUpdate("optimistic", mutation);
    });

    expect(result.current.data).toBe("server");
    expect(result.current.isOptimistic).toBe(false);
    expect(result.current.error).toBeNull();
    expect(mutation).toHaveBeenCalledTimes(1);
  });

  it("rolls back on mutation failure", async () => {
    const mutation = vi.fn().mockRejectedValue(new Error("failed"));
    const { result } = renderHook(() => useOptimistic("initial"));

    await act(async () => {
      await result.current.optimisticUpdate("optimistic", mutation);
    });

    expect(result.current.data).toBe("initial");
    expect(result.current.error).toBe("failed");
    expect(result.current.isOptimistic).toBe(false);
  });
});
