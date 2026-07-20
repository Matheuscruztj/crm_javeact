import { Page, Locator } from "@playwright/test";

/**
 * Page Object for approval workflow pages.
 * Validates: P0.B.1 — Setup Playwright with page objects
 */
export class ApprovalPage {
  readonly page: Page;
  readonly approveButton: Locator;
  readonly rejectButton: Locator;
  readonly rejectionReasonInput: Locator;
  readonly confirmButton: Locator;
  readonly approvalStatusBadge: Locator;

  constructor(page: Page) {
    this.page = page;
    this.approveButton = page.getByRole("button", {
      name: /approve|aprovar/i,
    });
    this.rejectButton = page.getByRole("button", {
      name: /reject|rejeitar/i,
    });
    this.rejectionReasonInput = page.getByLabel(/reason|motivo/i);
    this.confirmButton = page.getByRole("button", {
      name: /confirm|confirmar/i,
    });
    this.approvalStatusBadge = page.getByTestId("approval-status");
  }

  async gotoAdmin() {
    await this.page.goto("/admin/approvals");
  }

  async approveDocument(approvalId: string) {
    await this.page.goto(`/admin/approvals/${approvalId}`);
    await this.approveButton.click();
    await this.confirmButton.click();
  }

  async rejectDocument(approvalId: string, reason: string) {
    await this.page.goto(`/admin/approvals/${approvalId}`);
    await this.rejectButton.click();
    await this.rejectionReasonInput.fill(reason);
    await this.confirmButton.click();
  }
}
