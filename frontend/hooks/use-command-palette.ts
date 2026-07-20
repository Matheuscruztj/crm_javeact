/**
 * useCommandPalette: global state for the command palette (Ctrl+K / Cmd+K).
 * Validates: P1.5.3 — Frontend Command Palette component (⌘K)
 *
 * Usage:
 *   const { open, setOpen, toggle } = useCommandPalette();
 *   // In your layout: add <CommandPalette open={open} onClose={() => setOpen(false)} />
 */
"use client";

import { useCallback, useEffect, useState } from "react";

interface UseCommandPaletteReturn {
  open: boolean;
  setOpen: (open: boolean) => void;
  toggle: () => void;
}

// Singleton state via module-level subscribers (no external state lib needed)
let globalOpen = false;
const subscribers = new Set<(open: boolean) => void>();

function setGlobalOpen(open: boolean) {
  globalOpen = open;
  subscribers.forEach((fn) => fn(open));
}

export function useCommandPalette(): UseCommandPaletteReturn {
  const [open, setLocalOpen] = useState(globalOpen);

  useEffect(() => {
    const handler = (value: boolean) => setLocalOpen(value);
    subscribers.add(handler);
    return () => { subscribers.delete(handler); };
  }, []);

  const setOpen = useCallback((value: boolean) => setGlobalOpen(value), []);
  const toggle = useCallback(() => setGlobalOpen(!globalOpen), []);

  // Register global Ctrl+K / Cmd+K shortcut
  useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === "k") {
        e.preventDefault();
        setGlobalOpen(true);
      }
      if (e.key === "Escape") {
        setGlobalOpen(false);
      }
    };
    window.addEventListener("keydown", handler);
    return () => window.removeEventListener("keydown", handler);
  }, []);

  return { open, setOpen, toggle };
}
