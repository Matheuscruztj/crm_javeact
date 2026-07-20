"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { api } from "@/lib/api-client";
import { mapApiErrorsToForm, getApiErrorMessage } from "@/lib/form-utils";
import {
  createWebhookSchema,
  type CreateWebhookFormValues,
} from "@/lib/schemas";

interface DispatchResult {
  dispatchId: string;
  status: string;
  httpStatusCode: number;
}

/**
 * Integrations page: webhook dispatch with SSRF-safe URL validation.
 * Validates: P0.K.3.2 — admin/integrations/page.tsx — List + create webhook
 */
export default function IntegrationsPage() {
  const [result, setResult] = useState<DispatchResult | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [globalError, setGlobalError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    setError,
    reset,
    formState: { errors },
  } = useForm<CreateWebhookFormValues>({
    resolver: zodResolver(createWebhookSchema),
  });

  const onSubmit = async (data: CreateWebhookFormValues) => {
    try {
      setSubmitting(true);
      setGlobalError(null);
      const dispatch = await api.post<DispatchResult>(
        "/integrations/webhooks/dispatch",
        { targetUrl: data.targetUrl, eventType: data.eventType, payload: "{}" },
      );
      setResult(dispatch);
      reset();
    } catch (err) {
      mapApiErrorsToForm(err, setError);
      setGlobalError(getApiErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="p-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold">Integrations</h1>
        <p className="text-muted-foreground">
          Manage external system connectors and webhook dispatch.
        </p>
      </div>

      <div className="max-w-lg rounded-md border p-6">
        <h2 className="mb-4 font-semibold">Dispatch Webhook</h2>

        {globalError && (
          <div
            role="alert"
            className="mb-4 rounded-md bg-destructive/10 p-3 text-sm text-destructive"
          >
            {globalError}
          </div>
        )}

        <form onSubmit={handleSubmit(onSubmit)} noValidate>
          <div className="mb-4">
            <label
              htmlFor="targetUrl"
              className="mb-1 block text-sm font-medium"
            >
              Target URL
            </label>
            <input
              id="targetUrl"
              type="url"
              {...register("targetUrl")}
              placeholder="https://example.com/webhook"
              className="w-full rounded-md border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
              aria-describedby={
                errors.targetUrl ? "targetUrl-error" : undefined
              }
            />
            {errors.targetUrl && (
              <p
                id="targetUrl-error"
                role="alert"
                className="mt-1 text-xs text-destructive"
              >
                {errors.targetUrl.message}
              </p>
            )}
          </div>

          <div className="mb-6">
            <label
              htmlFor="eventType"
              className="mb-1 block text-sm font-medium"
            >
              Event Type
            </label>
            <input
              id="eventType"
              type="text"
              {...register("eventType")}
              placeholder="customer.created"
              className="w-full rounded-md border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
              aria-describedby={
                errors.eventType ? "eventType-error" : undefined
              }
            />
            {errors.eventType && (
              <p
                id="eventType-error"
                role="alert"
                className="mt-1 text-xs text-destructive"
              >
                {errors.eventType.message}
              </p>
            )}
          </div>

          <button
            type="submit"
            disabled={submitting}
            className="w-full rounded-md bg-primary py-2 text-sm font-medium text-primary-foreground hover:bg-primary/90 disabled:opacity-50"
            aria-busy={submitting}
          >
            {submitting ? "Dispatching…" : "Dispatch Webhook"}
          </button>
        </form>

        {result && (
          <div className="mt-4 rounded-md bg-green-50 p-4 text-sm">
            <p className="font-medium text-green-800">
              Dispatch ID: {result.dispatchId}
            </p>
            <p className="text-green-700">
              Status: {result.status} (HTTP {result.httpStatusCode})
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
