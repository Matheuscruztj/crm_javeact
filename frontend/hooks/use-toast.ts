"use client";

/**
 * useToast: lightweight toast notification hook backed by Radix UI Toast.
 * Validates: P1.13.5 — Toast system completo (variants, auto-dismiss, stacking)
 *
 * Usage:
 *   const { toast } = useToast();
 *   toast({ title: "Saved!", variant: "success" });
 *   toast({ title: "Error", description: "Something went wrong", variant: "error" });
 */

import { useCallback, useEffect, useRef, useState } from "react";

export type ToastVariant = "default" | "success" | "error" | "warning" | "info";

export interface Toast {
  id: string;
  title: string;
  description?: string;
  variant: ToastVariant;
  duration?: number;
}

interface ToastOptions {
  title: string;
  description?: string;
  variant?: ToastVariant;
  /** Auto-dismiss duration in ms. 0 = no auto-dismiss. Default: 4000 */
  duration?: number;
}

interface UseToastReturn {
  toasts: Toast[];
  toast: (options: ToastOptions) => string;
  dismiss: (id: string) => void;
  dismissAll: () => void;
}

const MAX_TOASTS = 5;

export function useToast(): UseToastReturn {
  const [toasts, setToasts] = useState<Toast[]>([]);
  const timers = useRef<Map<string, ReturnType<typeof setTimeout>>>(new Map());

  const dismiss = useCallback((id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
    const timer = timers.current.get(id);
    if (timer) {
      clearTimeout(timer);
      timers.current.delete(id);
    }
  }, []);

  const dismissAll = useCallback(() => {
    timers.current.forEach((timer) => clearTimeout(timer));
    timers.current.clear();
    setToasts([]);
  }, []);

  const toast = useCallback(
    (options: ToastOptions): string => {
      const id = crypto.randomUUID();
      const newToast: Toast = {
        id,
        title: options.title,
        description: options.description,
        variant: options.variant ?? "default",
        duration: options.duration ?? 4000,
      };

      setToasts((prev) => {
        const next = [newToast, ...prev];
        // Keep max toasts stacked
        return next.slice(0, MAX_TOASTS);
      });

      // Auto-dismiss
      if (newToast.duration && newToast.duration > 0) {
        const timer = setTimeout(() => dismiss(id), newToast.duration);
        timers.current.set(id, timer);
      }

      return id;
    },
    [dismiss],
  );

  // Cleanup all timers on unmount
  useEffect(() => {
    return () => {
      timers.current.forEach((timer) => clearTimeout(timer));
    };
  }, []);

  return { toasts, toast, dismiss, dismissAll };
}

/**
 * Convenience helpers for common toast variants.
 */
export const toastHelpers = {
  success: (title: string, description?: string) =>
    ({ title, description, variant: "success" as ToastVariant }),
  error: (title: string, description?: string) =>
    ({ title, description, variant: "error" as ToastVariant, duration: 6000 }),
  warning: (title: string, description?: string) =>
    ({ title, description, variant: "warning" as ToastVariant }),
  info: (title: string, description?: string) =>
    ({ title, description, variant: "info" as ToastVariant }),
};
