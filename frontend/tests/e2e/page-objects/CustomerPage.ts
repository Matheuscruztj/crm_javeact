import { Page, Locator } from "@playwright/test";

/**
 * Page Object for the Customers admin page.
 * Validates: P0.B.1 — Setup Playwright with page objects
 */
export class CustomerPage {
  readonly page: Page;
  readonly createButton: Locator;
  readonly customerTable: Locator;
  readonly nameInput: Locator;
  readonly emailInput: Locator;
  readonly saveButton: Locator;

  constructor(page: Page) {
    this.page = page;
    this.createButton = page.getByRole("button", {
      name: /new customer|novo cliente|criar/i,
    });
    this.customerTable = page.getByRole("table");
    this.nameInput = page.getByLabel(/name|nome/i);
    this.emailInput = page.getByLabel(/email/i);
    this.saveButton = page.getByRole("button", {
      name: /save|salvar|create|criar/i,
    });
  }

  async goto() {
    await this.page.goto("/admin/customers");
  }

  async createCustomer(name: string, email: string) {
    await this.createButton.click();
    await this.nameInput.fill(name);
    await this.emailInput.fill(email);
    await this.saveButton.click();
  }
}
