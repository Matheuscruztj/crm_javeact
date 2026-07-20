import { Page, Locator } from "@playwright/test";

/**
 * Page Object for the Operations admin page — job monitoring.
 * Validates: P0.B.1 — Setup Playwright with page objects
 */
export class OperationsPage {
  readonly page: Page;
  readonly jobTable: Locator;
  readonly statusFilter: Locator;
  readonly retryButton: Locator;
  readonly cancelButton: Locator;
  readonly healthStatus: Locator;

  constructor(page: Page) {
    this.page = page;
    this.jobTable = page.getByRole("table");
    this.statusFilter = page.getByLabel(/status filter|filtro de status/i);
    this.retryButton = page.getByRole("button", {
      name: /retry|tentar novamente/i,
    });
    this.cancelButton = page.getByRole("button", { name: /cancel|cancelar/i });
    this.healthStatus = page.getByTestId("health-status");
  }

  async goto() {
    await this.page.goto("/admin/operations");
  }

  async waitForJobsLoaded() {
    await this.jobTable.waitFor({ state: "visible", timeout: 10000 });
  }
}
