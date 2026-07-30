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
            onClick={() =>
              toast({ title: `${v} toast`, description: "Auto-dismisses in 4s", variant: v })
            }
            className="hover:bg-muted rounded-md border px-3 py-1.5 text-sm capitalize"
          >
            {v}
          </button>
        ))}
        <button
          onClick={() =>
            toast({ title: "Persistent", description: "No auto-dismiss", duration: 0 })
          }
          className="hover:bg-muted rounded-md border px-3 py-1.5 text-sm"
        >
          Persistent
        </button>
      </div>
      {/* Toast stack preview */}
      <div className="fixed right-4 bottom-4 space-y-2">
        {toasts.map((t) => (
          <div
            key={t.id}
            className={`rounded-md border p-3 shadow-md ${
              t.variant === "success"
                ? "border-green-200 bg-green-50"
                : t.variant === "error"
                  ? "border-red-200 bg-red-50"
                  : t.variant === "warning"
                    ? "border-yellow-200 bg-yellow-50"
                    : "bg-background"
            }`}
          >
            <div className="flex items-start justify-between gap-4">
              <div>
                <p className="text-sm font-medium">{t.title}</p>
                {t.description && <p className="text-muted-foreground text-xs">{t.description}</p>}
              </div>
              <button
                onClick={() => dismiss(t.id)}
                className="text-muted-foreground hover:text-foreground text-xs"
              >
                ✕
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export const Interactive: StoryObj = { render: () => <ToastDemo /> };
