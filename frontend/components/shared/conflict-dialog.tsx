"use client";

/**
 * ConflictDialog: shown when a 412 Precondition Failed response is received.
 * Provides options to reload the current version or retry with latest data.
 * Validates: P3.10.2 — Conflict resolution dialog
 */

import { useEffect, useRef } from "react";
import { handleFocusTrap } from "@/lib/accessibility";

interface ConflictDialogProps {
  open: boolean;
  /** Called when user chooses to reload and discard changes */
  onReload: () => void;
  /** Called when user chooses to close dialog without reloading */
  onCancel: () => void;
  /** Resource type that was conflicted (e.g., "customer", "request") */
  resourceType?: string;
}

/**
 * Detects 412 Precondition Failed from API errors.
 * Use this to decide whether to show the ConflictDialog.
 */
export function is412Conflict(error: unknown): boolean {
  const apiError = error as { status?: number };
  return apiError?.status === 412;
}

export function ConflictDialog({
  open,
  onReload,
  onCancel,
  resourceType = "resource",
}: ConflictDialogProps) {
  const dialogRef = useRef<HTMLDivElement>(null);

  // Focus trap + Escape to cancel
  useEffect(() => {
    if (!open) return;
    const el = dialogRef.current;
    if (!el) return;

    const handler = (e: KeyboardEvent) => {
      if (e.key === "Escape") { onCancel(); return; }
      handleFocusTrap(e, el);
    };
    window.addEventListener("keydown", handler);

    // Focus first button
    const firstBtn = el.querySelector<HTMLButtonElement>("button");
    firstBtn?.focus();

    return () => window.removeEventListener("keydown", handler);
  }, [open, onCancel]);

  if (!open) return null;

  return (
    /* Backdrop */
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/50"
      onClick={onCancel}
      aria-hidden="true"
    >
      {/* Dialog */}
      <div
        ref={dialogRef}
        role="alertdialog"
        aria-modal="true"
        aria-labelledby="conflict-title"
        aria-describedby="conflict-desc"
        className="mx-4 max-w-md rounded-lg bg-background p-6 shadow-xl"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Icon */}
        <div className="mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-yellow-100">
          <svg
            className="h-6 w-6 text-yellow-600"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            aria-hidden="true"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M12 9v2m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"
            />
          </svg>
        </div>

        <h2 id="conflict-title" className="mb-2 text-lg font-semibold">
          Version Conflict
        </h2>
        <p id="conflict-desc" className="mb-6 text-sm text-muted-foreground">
          The {resourceType} was modified by another user while you were editing.
          Your changes could not be saved. You can reload the latest version and try again.
        </p>

        <div className="flex flex-col gap-2 sm:flex-row-reverse">
          <button
            onClick={onReload}
            className="rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground hover:bg-primary/90"
            aria-label="Reload latest version"
          >
            Reload Latest
          </button>
          <button
            onClick={onCancel}
            className="rounded-md border px-4 py-2 text-sm font-medium hover:bg-muted"
            aria-label="Cancel and keep editing"
          >
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
}
