import { fileURLToPath } from "node:url";
import { dirname, resolve } from "node:path";
import { defineConfig } from "vitest/config";

const rootDir = dirname(fileURLToPath(import.meta.url));

export default defineConfig({
  resolve: {
    alias: {
      "@": resolve(rootDir, "."),
    },
  },
  test: {
    environment: "jsdom",
    setupFiles: ["./vitest.setup.ts"],
    include: [
      "lib/**/*.test.ts",
      "lib/**/*.test.tsx",
      "hooks/**/*.test.ts",
      "hooks/**/*.test.tsx",
      "components/shared/**/*.test.ts",
      "components/shared/**/*.test.tsx",
    ],
    exclude: [
      "**/node_modules/**",
      "**/*.spec.ts",
      "**/*.spec.tsx",
      "tests/e2e/**",
      "tests/performance/**",
    ],
    coverage: {
      provider: "v8",
      reporter: ["text", "html", "json"],
      thresholds: {
        lines: 10,
        branches: 10,
        functions: 10,
        statements: 10,
      },
      include: [
        "lib/**/*.ts",
        "hooks/**/*.ts",
        "hooks/**/*.tsx",
        "components/shared/confirm-dialog.tsx",
        "components/shared/conflict-dialog.tsx",
      ],
      exclude: ["**/*.stories.tsx", "**/*.d.ts"],
    },
  },
});
