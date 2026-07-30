import type { Meta, StoryObj } from "@storybook/react";
import { useState } from "react";
import { ConflictDialog } from "./conflict-dialog";

/**
 * ConflictDialog stories — shows 412 version conflict resolution.
 * Validates: P3.11.3 — Component variants catalog
 */
const meta: Meta<typeof ConflictDialog> = {
  title: "Shared/ConflictDialog",
  component: ConflictDialog,
  parameters: {
    layout: "centered",
    docs: {
      description: {
        component:
          "Shown when a 412 Precondition Failed occurs. Lets user reload latest version or cancel.",
      },
    },
  },
  tags: ["autodocs"],
};

export default meta;
type Story = StoryObj<typeof meta>;

export const Open: Story = {
  args: {
    open: true,
    resourceType: "customer",
    onReload: () => alert("Reloaded"),
    onCancel: () => alert("Cancelled"),
  },
};

export const Controlled: Story = {
  render: () => <ControlledStory />,
};

function ControlledStory() {
  const [open, setOpen] = useState(false);
  return (
    <div>
      <button
        onClick={() => setOpen(true)}
        className="bg-primary text-primary-foreground rounded-md px-4 py-2 text-sm"
      >
        Trigger Conflict
      </button>
      <ConflictDialog
        open={open}
        resourceType="request"
        onReload={() => {
          setOpen(false);
          alert("Reloading...");
        }}
        onCancel={() => setOpen(false)}
      />
    </div>
  );
}
