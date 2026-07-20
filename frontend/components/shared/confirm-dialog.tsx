"use client";

import * as Dialog from "@radix-ui/react-dialog";
import { AlertTriangle, Info, X } from "lucide-react";
import { useEffect, useRef } from "react";
import { cn } from "@/lib/utils";

export interface ConfirmDialogProps {
  open: boolean;
  onConfirm: () => void;
  onCancel: () => void;
  title: string;
  description: string;
  variant: "sensitive" | "destructive";
  confirmLabel?: string;
  cancelLabel?: string;
  affectedResources?: string[];
}

/**
 * Confirmation dialog for SENSITIVE and DESTRUCTIVE actions.
 * - DESTRUCTIVE: red confirm button, warning icon, role="alertdialog"
 * - SENSITIVE: yellow confirm button, info icon
 * Supports keyboard shortcuts: Enter to confirm, Escape to cancel.
 */
export function ConfirmDialog({
  open,
  onConfirm,
  onCancel,
  title,
  description,
  variant,
  confirmLabel,
  cancelLabel,
  affectedResources,
}: ConfirmDialogProps) {
  const confirmRef = useRef<HTMLButtonElement>(null);
  const descriptionId = "confirm-dialog-description";

  const defaultConfirmLabel = variant === "destructive" ? "Delete" : "Confirm";
  const defaultCancelLabel = "Cancel";

  useEffect(() => {
    if (!open) return;

    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Enter") {
        e.preventDefault();
        onConfirm();
      }
    };

    document.addEventListener("keydown", handleKeyDown);
    return () => document.removeEventListener("keydown", handleKeyDown);
  }, [open, onConfirm]);

  return (
    <Dialog.Root open={open} onOpenChange={(isOpen) => !isOpen && onCancel()}>
      <Dialog.Portal>
        <Dialog.Overlay className="fixed inset-0 bg-black/50 z-50 data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0" />
        <Dialog.Content
          role={variant === "destructive" ? "alertdialog" : "dialog"}
          aria-describedby={descriptionId}
          className="fixed left-1/2 top-1/2 z-50 w-full max-w-md -translate-x-1/2 -translate-y-1/2 rounded-lg bg-white p-6 shadow-xl focus:outline-none"
        >
          {/* Header */}
          <div className="flex items-start gap-3 mb-4">
            {variant === "destructive" ? (
              <AlertTriangle className="h-6 w-6 text-red-500 flex-shrink-0 mt-0.5" aria-hidden="true" />
            ) : (
              <Info className="h-6 w-6 text-yellow-500 flex-shrink-0 mt-0.5" aria-hidden="true" />
            )}
            <div className="flex-1">
              <Dialog.Title className="text-base font-semibold text-gray-900">
                {title}
              </Dialog.Title>
            </div>
            <button
              onClick={onCancel}
              className="rounded-sm text-gray-400 hover:text-gray-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500"
              aria-label="Close dialog"
            >
              <X className="h-4 w-4" />
            </button>
          </div>

          {/* Description */}
          <Dialog.Description id={descriptionId} className="text-sm text-gray-600 mb-4">
            {description}
          </Dialog.Description>

          {/* Affected resources */}
          {affectedResources && affectedResources.length > 0 && (
            <div className="mb-4 rounded-md bg-gray-50 p-3">
              <p className="text-xs font-medium text-gray-700 mb-2">Affected resources:</p>
              <ul className="space-y-1">
                {affectedResources.map((resource, index) => (
                  <li key={index} className="text-xs text-gray-600 flex items-center gap-1.5">
                    <span className="h-1 w-1 rounded-full bg-gray-400 flex-shrink-0" aria-hidden="true" />
                    {resource}
                  </li>
                ))}
              </ul>
            </div>
          )}

          {/* Actions */}
          <div className="flex justify-end gap-3">
            <button
              onClick={onCancel}
              className="rounded-md px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500"
            >
              {cancelLabel ?? defaultCancelLabel}
            </button>
            <button
              ref={confirmRef}
              onClick={onConfirm}
              className={cn(
                "rounded-md px-4 py-2 text-sm font-medium text-white focus:outline-none focus:ring-2 focus:ring-offset-2",
                variant === "destructive"
                  ? "bg-red-600 hover:bg-red-700 focus:ring-red-500"
                  : "bg-yellow-500 hover:bg-yellow-600 focus:ring-yellow-400",
              )}
            >
              {confirmLabel ?? defaultConfirmLabel}
            </button>
          </div>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
}
