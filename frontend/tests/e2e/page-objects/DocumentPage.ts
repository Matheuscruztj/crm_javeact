import { Page, Locator } from "@playwright/test";

/**
 * Page Object for document management pages.
 * Validates: P0.B.1 — Setup Playwright with page objects
 */
export class DocumentPage {
  readonly page: Page;
  readonly uploadButton: Locator;
  readonly fileInput: Locator;
  readonly documentTable: Locator;
  readonly processingStatus: Locator;
  readonly analyzedStatus: Locator;

  constructor(page: Page) {
    this.page = page;
    this.uploadButton = page.getByRole("button", {
      name: /upload|enviar documento/i,
    });
    this.fileInput = page.locator('input[type="file"]');
    this.documentTable = page.getByRole("table");
    this.processingStatus = page.getByText(/processing|processando/i);
    this.analyzedStatus = page.getByText(/analyzed|analisado/i);
  }

  async gotoAdmin() {
    await this.page.goto("/admin/documents");
  }

  async gotoPortal() {
    await this.page.goto("/portal/documents");
  }

  async waitForAnalyzed(timeout = 30000) {
    await this.analyzedStatus.waitFor({ state: "visible", timeout });
  }
}
