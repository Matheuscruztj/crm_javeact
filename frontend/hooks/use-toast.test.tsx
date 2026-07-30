import { renderHook, act } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { toastHelpers, useToast } from "./use-toast";

describe("useToast", () => {
  beforeEach(() => {
    vi.stubGlobal("crypto", { randomUUID: vi.fn(() => "toast-1") });
  });

  afterEach(() => {
    vi.useRealTimers();
    vi.unstubAllGlobals();
  });

  it("adds and dismisses toasts", () => {
    vi.useFakeTimers();
    const { result } = renderHook(() => useToast());

    act(() => {
      const id = result.current.toast({ title: "Saved", variant: "success", duration: 1000 });
      expect(id).toBe("toast-1");
    });

    expect(result.current.toasts).toHaveLength(1);
    expect(result.current.toasts[0].variant).toBe("success");

    act(() => {
      vi.advanceTimersByTime(1000);
    });

    expect(result.current.toasts).toHaveLength(0);
  });

  it("dismisses all toasts and exposes helpers", () => {
    const { result } = renderHook(() => useToast());

    act(() => {
      result.current.toast({ title: "One" });
      result.current.toast({ title: "Two", variant: "warning" });
    });

    expect(result.current.toasts).toHaveLength(2);

    act(() => {
      result.current.dismissAll();
    });

    expect(result.current.toasts).toHaveLength(0);
    expect(toastHelpers.error("Oops", "Failed")).toEqual({
      title: "Oops",
      description: "Failed",
      variant: "error",
      duration: 6000,
    });
  });

  it("keeps the stack capped and skips auto-dismiss when duration is zero", () => {
    vi.useFakeTimers();
    vi.stubGlobal("crypto", { randomUUID: vi.fn(() => `toast-${Math.random()}`) });
    const { result, unmount } = renderHook(() => useToast());

    act(() => {
      for (let index = 0; index < 6; index += 1) {
        result.current.toast({ title: `Toast ${index}`, duration: 0 });
      }
    });

    expect(result.current.toasts).toHaveLength(5);
    expect(result.current.toasts[0].title).toBe("Toast 5");
    expect(result.current.toasts[4].title).toBe("Toast 1");

    act(() => {
      result.current.dismiss(result.current.toasts[0].id);
    });

    expect(result.current.toasts).toHaveLength(4);

    unmount();
    expect(vi.getTimerCount()).toBe(0);
  });
});
