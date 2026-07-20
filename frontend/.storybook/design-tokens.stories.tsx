import type { Meta, StoryObj } from "@storybook/react";

/**
 * Design Tokens catalog — colors, typography, spacing.
 * Validates: P3.11.2 — Document design tokens (colors, typography, spacing)
 */
const meta: Meta = {
  title: "Design System/Tokens",
  parameters: {
    layout: "padded",
    docs: {
      description: {
        component:
          "AtlasOps AI design tokens following shadcn/ui CSS variable conventions. All tokens are defined in app/globals.css.",
      },
    },
  },
  tags: ["autodocs"],
};

export default meta;

const colors = [
  { name: "background", variable: "--background", description: "Page background" },
  { name: "foreground", variable: "--foreground", description: "Default text" },
  { name: "primary", variable: "--primary", description: "Primary action color" },
  { name: "primary-foreground", variable: "--primary-foreground", description: "Text on primary" },
  { name: "secondary", variable: "--secondary", description: "Secondary background" },
  { name: "muted", variable: "--muted", description: "Muted background" },
  { name: "muted-foreground", variable: "--muted-foreground", description: "Muted text" },
  { name: "accent", variable: "--accent", description: "Accent background" },
  { name: "destructive", variable: "--destructive", description: "Danger/error color" },
  { name: "border", variable: "--border", description: "Border color" },
  { name: "ring", variable: "--ring", description: "Focus ring" },
];

export const Colors: StoryObj = {
  render: () => (
    <div className="space-y-4">
      <h2 className="text-lg font-semibold">Color Tokens</h2>
      <div className="grid grid-cols-2 gap-3 md:grid-cols-3">
        {colors.map((token) => (
          <div key={token.name} className="rounded-md border overflow-hidden">
            <div
              className="h-12"
              style={{ backgroundColor: `hsl(var(${token.variable}))` }}
            />
            <div className="p-2">
              <p className="font-mono text-xs font-medium">{token.variable}</p>
              <p className="text-xs text-muted-foreground">{token.description}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  ),
};

const typographyScale = [
  { name: "text-xs", class: "text-xs", size: "12px", usage: "Labels, metadata" },
  { name: "text-sm", class: "text-sm", size: "14px", usage: "Body text, form labels" },
  { name: "text-base", class: "text-base", size: "16px", usage: "Default body" },
  { name: "text-lg", class: "text-lg", size: "18px", usage: "Section headers" },
  { name: "text-xl", class: "text-xl", size: "20px", usage: "Card titles" },
  { name: "text-2xl", class: "text-2xl", size: "24px", usage: "Page titles" },
];

export const Typography: StoryObj = {
  render: () => (
    <div className="space-y-4">
      <h2 className="text-lg font-semibold">Typography Scale</h2>
      <div className="space-y-2">
        {typographyScale.map((t) => (
          <div key={t.name} className="flex items-baseline gap-4 border-b pb-2">
            <span className="w-24 font-mono text-xs text-muted-foreground">{t.name}</span>
            <span className={t.class}>The quick brown fox — {t.size}</span>
            <span className="ml-auto text-xs text-muted-foreground">{t.usage}</span>
          </div>
        ))}
      </div>
    </div>
  ),
};

export const Spacing: StoryObj = {
  render: () => (
    <div className="space-y-4">
      <h2 className="text-lg font-semibold">Spacing Scale (Tailwind)</h2>
      <div className="space-y-2">
        {[1, 2, 3, 4, 6, 8, 10, 12, 16].map((n) => (
          <div key={n} className="flex items-center gap-4">
            <span className="w-8 font-mono text-xs text-muted-foreground">{n}</span>
            <div
              className="h-4 bg-primary rounded"
              style={{ width: `${n * 4}px` }}
            />
            <span className="text-xs text-muted-foreground">{n * 4}px</span>
          </div>
        ))}
      </div>
    </div>
  ),
};
