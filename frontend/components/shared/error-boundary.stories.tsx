import type { Meta, StoryObj } from "@storybook/react";
import { ErrorBoundary } from "./error-boundary";

/**
 * ErrorBoundary component stories.
 * Validates: P3.11.3 — Component variants catalog
 */
const meta: Meta<typeof ErrorBoundary> = {
  title: "Shared/ErrorBoundary",
  component: ErrorBoundary,
  parameters: {
    layout: "padded",
    docs: {
      description: {
        component:
          "Catches render errors and shows fallback UI. Supports custom fallback via prop.",
      },
    },
  },
  tags: ["autodocs"],
};

export default meta;
type Story = StoryObj<typeof meta>;

function ThrowingChild({ shouldThrow }: { shouldThrow: boolean }) {
  if (shouldThrow) throw new Error("Simulated component error");
  return <div className="rounded-md border p-4">Normal content — no error.</div>;
}

export const NoError: Story = {
  args: { section: "admin" },
  render: (args) => (
    <ErrorBoundary {...args}>
      <ThrowingChild shouldThrow={false} />
    </ErrorBoundary>
  ),
};

export const WithError: Story = {
  args: { section: "admin" },
  render: (args) => (
    <ErrorBoundary {...args}>
      <ThrowingChild shouldThrow={true} />
    </ErrorBoundary>
  ),
};

export const CustomFallback: Story = {
  args: {
    section: "portal",
    fallback: (error: Error | null, reset: () => void) => (
      <div className="rounded-md bg-yellow-50 p-4">
        <p className="text-sm font-medium text-yellow-800">Custom fallback: {error?.message}</p>
        <button onClick={reset} className="mt-2 text-xs text-yellow-700 underline">
          Retry
        </button>
      </div>
    ),
  },
  render: (args) => (
    <ErrorBoundary {...args}>
      <ThrowingChild shouldThrow={true} />
    </ErrorBoundary>
  ),
};
