import { describe, expect, it } from "vitest";
import {
  addCommentSchema,
  createCustomerSchema,
  createRequestSchema,
  createWebhookSchema,
  loginSchema,
  rejectApprovalSchema,
} from "./schemas";

describe("schemas", () => {
  it("accepts valid login payload", () => {
    expect(
      loginSchema.parse({ email: "admin@example.com", password: "password123" })
    ).toEqual({ email: "admin@example.com", password: "password123" });
  });

  it("rejects invalid login payload", () => {
    expect(() => loginSchema.parse({ email: "invalid", password: "short" })).toThrow();
  });

  it("validates customer, request, comment, approval and webhook payloads", () => {
    expect(
      createCustomerSchema.parse({
        name: "Acme",
        email: "acme@example.com",
      })
    ).toMatchObject({ name: "Acme", email: "acme@example.com" });

    expect(
      createRequestSchema.parse({
        title: "Need help",
        priority: "HIGH",
        customerId: "customer-1",
      })
    ).toMatchObject({ priority: "HIGH" });

    expect(addCommentSchema.parse({ text: "A valid comment" })).toEqual({
      text: "A valid comment",
    });

    expect(rejectApprovalSchema.parse({ reason: "Insufficient documentation" })).toEqual({
      reason: "Insufficient documentation",
    });

    expect(
      createWebhookSchema.parse({
        targetUrl: "https://example.com/webhook",
        eventType: "request.created",
      })
    ).toEqual({
      targetUrl: "https://example.com/webhook",
      eventType: "request.created",
    });
  });
});
