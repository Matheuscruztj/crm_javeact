import { renderHook, act } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { useConfirm } from "./use-confirm";

describe("useConfirm", () => {
  it("opens dialog and resolves true when confirmed", async () => {
    const { result } = renderHook(() => useConfirm());

    let promise!: Promise<boolean>;
    await act(async () => {
      promise = result.current.confirm({
        title: "Delete?",
        description: "This cannot be undone.",
        variant: "destructive",
        confirmLabel: "Delete",
      });
    });

    expect(result.current.dialogProps.open).toBe(true);
    expect(result.current.dialogProps.title).toBe("Delete?");
    expect(result.current.dialogProps.variant).toBe("destructive");

    await act(async () => {
      result.current.dialogProps.onConfirm();
    });

    await expect(promise).resolves.toBe(true);
    expect(result.current.dialogProps.open).toBe(false);
  });

  it("resolves false when cancelled", async () => {
    const { result } = renderHook(() => useConfirm());

    let promise!: Promise<boolean>;
    await act(async () => {
      promise = result.current.confirm({
        title: "Discard changes?",
        description: "Unsaved data will be lost.",
      });
    });

    await act(async () => {
      result.current.dialogProps.onCancel();
    });

    await expect(promise).resolves.toBe(false);
    expect(result.current.dialogProps.variant).toBe("sensitive");
  });
});
