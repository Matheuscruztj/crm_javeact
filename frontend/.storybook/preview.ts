import type { Preview } from "@storybook/react";
import "../app/globals.css";

/**
 * Storybook global preview configuration.
 * Includes global CSS (Tailwind + design tokens) for all stories.
 */
const preview: Preview = {
  parameters: {
    controls: {
      matchers: {
        color: /(background|color)$/i,
        date: /Date$/i,
      },
    },
    a11y: {
      config: {
        rules: [
          { id: "color-contrast", enabled: true },
          { id: "label", enabled: true },
        ],
      },
    },
    backgrounds: {
      default: "light",
      values: [
        { name: "light", value: "#ffffff" },
        { name: "dark", value: "#0f172a" },
      ],
    },
  },
};

export default preview;
