/**
 * Shared Zod schemas for form validation.
 * Validates: P0.L.3 — Frontend Form Infrastructure
 */
import { z } from "zod";

// ─── Auth ─────────────────────────────────────────────────────────────────

export const loginSchema = z.object({
  email: z.string().email("Must be a valid email address"),
  password: z.string().min(8, "Password must be at least 8 characters"),
});
export type LoginFormValues = z.infer<typeof loginSchema>;

// ─── Customer ─────────────────────────────────────────────────────────────

export const createCustomerSchema = z.object({
  name: z
    .string()
    .min(1, "Name is required")
    .max(150, "Name must not exceed 150 characters"),
  email: z.string().email("Must be a valid email address"),
  phone: z.string().optional(),
  street: z.string().optional(),
  city: z.string().optional(),
  state: z.string().optional(),
  postalCode: z.string().optional(),
  country: z.string().optional(),
});
export type CreateCustomerFormValues = z.infer<typeof createCustomerSchema>;

// ─── Request ──────────────────────────────────────────────────────────────

export const createRequestSchema = z.object({
  title: z
    .string()
    .min(1, "Title is required")
    .max(255, "Title must not exceed 255 characters"),
  description: z.string().optional(),
  priority: z.enum(["LOW", "MEDIUM", "HIGH", "CRITICAL"]),
  customerId: z.string().min(1, "Customer is required"),
});
export type CreateRequestFormValues = z.infer<typeof createRequestSchema>;

export const addCommentSchema = z.object({
  text: z
    .string()
    .min(1, "Comment cannot be empty")
    .max(2000, "Comment must not exceed 2000 characters"),
});
export type AddCommentFormValues = z.infer<typeof addCommentSchema>;

// ─── Approval ─────────────────────────────────────────────────────────────

export const rejectApprovalSchema = z.object({
  reason: z
    .string()
    .min(10, "Rejection reason must be at least 10 characters")
    .max(1000, "Rejection reason must not exceed 1000 characters"),
});
export type RejectApprovalFormValues = z.infer<typeof rejectApprovalSchema>;

// ─── Webhook ──────────────────────────────────────────────────────────────

export const createWebhookSchema = z.object({
  targetUrl: z.string().url("Must be a valid HTTPS URL"),
  eventType: z.string().min(1, "Event type is required"),
});
export type CreateWebhookFormValues = z.infer<typeof createWebhookSchema>;
