import { Page, Locator } from "@playwright/test";

/**
 * Page Object for the Requests pages.
 * Validates: P0.B.1 — Setup Playwright with page objects
 */
export class RequestPage {
  readonly page: Page;
  readonly createButton: Locator;
  readonly titleInput: Locator;
  readonly descriptionInput: Locator;
  readonly submitButton: Locator;
  readonly statusBadge: Locator;

  constructor(page: Page) {
    this.page = page;
    this.createButton = page.getByRole("button", {
      name: /new request|nova solicitação|criar/i,
    });
    this.titleInput = page.getByLabel(/title|título/i);
    this.descriptionInput = page.getByLabel(/description|descrição/i);
    this.submitButton = page.getByRole("button", {
      name: /submit|enviar|create|criar/i,
    });
    this.statusBadge = page.getByTestId("request-status");
  }

  async gotoAdmin() {
    await this.page.goto("/admin/requests");
  }

  async gotoPortal() {
    await this.page.goto("/portal/requests");
  }

  async createRequest(title: string, description: string) {
    await this.createButton.click();
    await this.titleInput.fill(title);
    await this.descriptionInput.fill(description);
    await this.submitButton.click();
  }
}
