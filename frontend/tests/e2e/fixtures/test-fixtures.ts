import { test as base } from "@playwright/test";
import { LoginPage } from "../page-objects/LoginPage";
import { CustomerPage } from "../page-objects/CustomerPage";
import { RequestPage } from "../page-objects/RequestPage";
import { DocumentPage } from "../page-objects/DocumentPage";
import { ApprovalPage } from "../page-objects/ApprovalPage";
import { NotificationPage } from "../page-objects/NotificationPage";
import { OperationsPage } from "../page-objects/OperationsPage";
import { SearchPage } from "../page-objects/SearchPage";

/**
 * Extended test fixtures providing pre-initialized page objects.
 * Validates: P0.B.1 — Setup Playwright with test fixtures
 */
type AtlasOpsFixtures = {
  loginPage: LoginPage;
  customerPage: CustomerPage;
  requestPage: RequestPage;
  documentPage: DocumentPage;
  approvalPage: ApprovalPage;
  notificationPage: NotificationPage;
  operationsPage: OperationsPage;
  searchPage: SearchPage;
  adminLogin: void;
  clientLogin: void;
  analystLogin: void;
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
  documentPage: async ({ page }, use) => {
    await use(new DocumentPage(page));
  },
  approvalPage: async ({ page }, use) => {
    await use(new ApprovalPage(page));
  },
  notificationPage: async ({ page }, use) => {
    await use(new NotificationPage(page));
  },
  operationsPage: async ({ page }, use) => {
    await use(new OperationsPage(page));
  },
  searchPage: async ({ page }, use) => {
    await use(new SearchPage(page));
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
  analystLogin: async ({ page }, use) => {
    const loginPage = new LoginPage(page);
    await loginPage.goto();
    await loginPage.loginAndWait(
      process.env.ANALYST_EMAIL ?? "analyst@atlasops.test",
      process.env.ANALYST_PASSWORD ?? "analyst-password",
    );
    await use();
  },
});

export { expect } from "@playwright/test";
