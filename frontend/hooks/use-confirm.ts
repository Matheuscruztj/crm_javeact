"use client";

import { useCallback, useRef, useState } from "react";

export interface ConfirmOptions {
  title: string;
  description: string;
  variant?: "sensitive" | "destructive";
  confirmLabel?: string;
  cancelLabel?: string;
  affectedResources?: string[];
}

export interface UseConfirmReturn {
  confirm: (opts: ConfirmOptions) => Promise<boolean>;
  dialogProps: {
    open: boolean;
    title: string;
    description: string;
    variant: "sensitive" | "destructive";
    confirmLabel?: string;
    cancelLabel?: string;
    affectedResources?: string[];
    onConfirm: () => void;
    onCancel: () => void;
  };
}

/**
 * Hook that provides an imperative confirmation dialog API.
 * Returns a {@code confirm} function that resolves to true if the user
 * confirms and false if they cancel.
 *
 * Usage:
 * ```tsx
 * const { confirm, dialogProps } = useConfirm();
 *
 * const handleDelete = async () => {
 *   const ok = await confirm({
 *     title: "Delete customer?",
 *     description: "This action cannot be undone.",
 *     variant: "destructive",
 *   });
 *   if (ok) await deleteCustomer(id);
 * };
 *
 * return (
 *   <>
 *     <button onClick={handleDelete}>Delete</button>
 *     <ConfirmDialog {...dialogProps} />
 *   </>
 * );
 * ```
 */
export function useConfirm(): UseConfirmReturn {
  const [open, setOpen] = useState(false);
  const [options, setOptions] = useState<ConfirmOptions>({
    title: "",
    description: "",
    variant: "sensitive",
  });

  const resolveRef = useRef<((value: boolean) => void) | null>(null);

  const confirm = useCallback((opts: ConfirmOptions): Promise<boolean> => {
    setOptions(opts);
    setOpen(true);
    return new Promise<boolean>((resolve) => {
      resolveRef.current = resolve;
    });
  }, []);

  const handleConfirm = useCallback(() => {
    setOpen(false);
    resolveRef.current?.(true);
    resolveRef.current = null;
  }, []);

  const handleCancel = useCallback(() => {
    setOpen(false);
    resolveRef.current?.(false);
    resolveRef.current = null;
  }, []);

  return {
    confirm,
    dialogProps: {
      open,
      title: options.title,
      description: options.description,
      variant: options.variant ?? "sensitive",
      confirmLabel: options.confirmLabel,
      cancelLabel: options.cancelLabel,
      affectedResources: options.affectedResources,
      onConfirm: handleConfirm,
      onCancel: handleCancel,
    },
  };
}
