import React from "react";
import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { ConfirmDialog } from "./confirm-dialog";

describe("ConfirmDialog", () => {
  it("does not render when closed", () => {
    const { container } = render(
      <ConfirmDialog
        open={false}
        onConfirm={vi.fn()}
        onCancel={vi.fn()}
        title="Archive request"
        description="This request will be archived."
        variant="sensitive"
      />
    );

    expect(container).toBeEmptyDOMElement();
  });

  it("renders a destructive confirmation flow and handles enter/cancel", () => {
    const onConfirm = vi.fn();
    const onCancel = vi.fn();

    render(
      <ConfirmDialog
        open
        onConfirm={onConfirm}
        onCancel={onCancel}
        title="Delete customer"
        description="This action cannot be undone."
        variant="destructive"
        affectedResources={["Invoices", "Requests"]}
      />
    );

    expect(screen.getByRole("alertdialog")).toBeInTheDocument();
    expect(screen.getByText("Delete customer")).toBeInTheDocument();
    expect(screen.getByText("Invoices")).toBeInTheDocument();

    fireEvent.keyDown(document, { key: "Enter" });
    expect(onConfirm).toHaveBeenCalledTimes(1);

    fireEvent.click(screen.getByRole("button", { name: /cancel/i }));
    expect(onCancel).toHaveBeenCalledTimes(1);
  });

  it("renders the sensitive variant with default labels", () => {
    const onConfirm = vi.fn();
    const onCancel = vi.fn();

    render(
      <ConfirmDialog
        open
        onConfirm={onConfirm}
        onCancel={onCancel}
        title="Archive request"
        description="Move the request to the archive."
        variant="sensitive"
      />
    );

    expect(screen.getByRole("dialog")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Confirm" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Cancel" })).toBeInTheDocument();
  });
});
