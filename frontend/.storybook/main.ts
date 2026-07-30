import type { StorybookConfig } from "@storybook/nextjs";

/**
 * Storybook configuration for AtlasOps AI design system catalog.
 * Validates: P3.11.1 — Storybook setup with shadcn/ui components
 */
const config: StorybookConfig = {
  stories: ["../components/**/*.stories.@(js|jsx|ts|tsx|mdx)"],
  addons: ["@storybook/addon-essentials", "@storybook/addon-a11y", "@storybook/addon-interactions"],
  framework: {
    name: "@storybook/nextjs",
    options: {},
  },
  docs: {
    autodocs: "tag",
  },
  staticDirs: ["../public"],
};

export default config;
