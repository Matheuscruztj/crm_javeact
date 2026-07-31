import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { ConflictDialog, is412Conflict } from "./conflict-dialog";

describe("ConflictDialog", () => {
  it("detects 412 conflicts", () => {
    expect(is412Conflict({ status: 412 })).toBe(true);
    expect(is412Conflict({ status: 409 })).toBe(false);
  });

  it("renders actions and handles cancel/reload", () => {
    const onReload = vi.fn();
    const onCancel = vi.fn();

    render(<ConflictDialog open onReload={onReload} onCancel={onCancel} resourceType="customer" />);

    expect(screen.getByRole("alertdialog", { hidden: true })).toBeInTheDocument();
    expect(screen.getByText("Version Conflict")).toBeInTheDocument();
    expect(screen.getByText(/The customer was modified/i)).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: /cancel/i, hidden: true }));
    expect(onCancel).toHaveBeenCalledTimes(1);

    fireEvent.click(screen.getByRole("button", { name: /reload latest version/i, hidden: true }));
    expect(onReload).toHaveBeenCalledTimes(1);
  });
});
