import type { Meta, StoryObj } from "@storybook/react";
import { useToast } from "./use-toast";

/**
 * useToast hook demo — toast system with variants, auto-dismiss, stacking.
 * Validates: P1.13.5 — Toast system completo
 */
const meta: Meta = {
  title: "Hooks/useToast",
  parameters: { layout: "padded" },
  tags: ["autodocs"],
};
export default meta;

function ToastDemo() {
  const { toast, toasts, dismiss } = useToast();
  return (
    <div className="space-y-4">
      <div className="flex flex-wrap gap-2">
        {(["default", "success", "error", "warning", "info"] as const).map((v) => (
          <button
            key={v}
            onClick={() => toast({ title: `${v} toast`, description: "Auto-dismisses in 4s", variant: v })}
            className="rounded-md border px-3 py-1.5 text-sm capitalize hover:bg-muted"
          >
            {v}
          </button>
        ))}
        <button
          onClick={() => toast({ title: "Persistent", description: "No auto-dismiss", duration: 0 })}
          className="rounded-md border px-3 py-1.5 text-sm hover:bg-muted"
        >
          Persistent
        </button>
      </div>
      {/* Toast stack preview */}
      <div className="fixed bottom-4 right-4 space-y-2">
        {toasts.map((t) => (
          <div
            key={t.id}
            className={`rounded-md border p-3 shadow-md ${
              t.variant === "success" ? "bg-green-50 border-green-200" :
              t.variant === "error" ? "bg-red-50 border-red-200" :
              t.variant === "warning" ? "bg-yellow-50 border-yellow-200" :
              "bg-background"
            }`}
          >
            <div className="flex items-start justify-between gap-4">
              <div>
                <p className="text-sm font-medium">{t.title}</p>
                {t.description && <p className="text-xs text-muted-foreground">{t.description}</p>}
              </div>
              <button onClick={() => dismiss(t.id)} className="text-muted-foreground hover:text-foreground text-xs">✕</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export const Interactive: StoryObj = { render: () => <ToastDemo /> };
