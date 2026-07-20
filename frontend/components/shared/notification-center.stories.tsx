import type { Meta, StoryObj } from "@storybook/react";
import { NotificationCenter } from "./notification-center";

/**
 * NotificationCenter component stories.
 * Validates: P3.11.3 — Component variants catalog
 */
const meta: Meta<typeof NotificationCenter> = {
  title: "Shared/NotificationCenter",
  component: NotificationCenter,
  parameters: {
    layout: "padded",
    docs: {
      description: {
        component:
          "Bell button with unread badge, dropdown panel with notification list and mark-read actions.",
      },
    },
  },
  tags: ["autodocs"],
};

export default meta;
type Story = StoryObj<typeof meta>;

export const Default: Story = {};
