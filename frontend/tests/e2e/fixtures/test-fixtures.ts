import { test as base } from "@playwright/test";
import { LoginPage } from "../page-objects/LoginPage";
import { CustomerPage } from "../page-objects/CustomerPage";
import { RequestPage } from "../page-objects/RequestPage";

/**
 * Extended test fixtures providing pre-initialized page objects.
 * Validates: P0.B.1 — Setup Playwright with test fixtures
 */
type AtlasOpsFixtures = {
  loginPage: LoginPage;
  customerPage: CustomerPage;
  requestPage: RequestPage;
  adminLogin: void;
  clientLogin: void;
};

export const test = base.extend<AtlasOpsFixtures>({
  loginPage: async ({ page }, use) => {
    await use(new LoginPage(page));
  },
  customerPage: async ({ page }, use) => {
    await use(new CustomerPage(page));
  },
  requestPage: async ({ page }, use) => {
    await use(new RequestPage(page));
  },
  adminLogin: async ({ page }, use) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    await loginPage.loginAndWait(
      process.env.ADMIN_EMAIL ?? "admin@atlasops.test",
      process.env.ADMIN_PASSWORD ?? "admin-password",
    );
    await use();
  },
  clientLogin: async ({ page }, use) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    await loginPage.loginAndWait(
      process.env.CLIENT_EMAIL ?? "client@atlasops.test",
      process.env.CLIENT_PASSWORD ?? "client-password",
    );
    await use();
  },
});

export { expect } from "@playwright/test";
