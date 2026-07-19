import { defineConfig, devices } from "@playwright/test";

/**
 * Playwright configuration for AtlasOps AI E2E tests.
 * Validates: P0.B.1 — Setup Playwright with page objects and test fixtures
 */
export default defineConfig({
  testDir: "./tests/e2e",
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: [
    ["html", { outputFolder: "playwright-report", open: "never" }],
    ["list"],
  ],
  use: {
    baseURL: process.env.BASE_URL ?? "http://localhost:3000",
    trace: "on-first-retry",
    screenshot: "only-on-failure",
    video: "on-first-retry",
  },
  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"] },
    },
  ],
  // Dev server not auto-started — run `pnpm dev` separately or provide BASE_URL
  // webServer: { command: 'pnpm dev', url: 'http://localhost:3000', reuseExistingServer: true },
});
