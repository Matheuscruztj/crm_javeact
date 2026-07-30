"use client";

/**
 * Admin Settings page: tenant config, branding, users, roles.
 * Validates: P3.9.1 — /admin/settings
 */

import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { api } from "@/lib/api-client";
import { mapApiErrorsToForm, getApiErrorMessage } from "@/lib/form-utils";

const brandingSchema = z.object({
  logoUrl: z.string().url("Must be a valid URL").optional().or(z.literal("")),
  primaryColor: z
    .string()
    .regex(/^#[0-9A-Fa-f]{6}$/, "Must be a valid hex color (#RRGGBB)")
    .optional()
    .or(z.literal("")),
});
type BrandingValues = z.infer<typeof brandingSchema>;

export default function SettingsPage() {
  const [saved, setSaved] = useState(false);
  const [globalError, setGlobalError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<BrandingValues>({
    resolver: zodResolver(brandingSchema),
  });

  const onBrandingSubmit = async (data: BrandingValues) => {
    setSaved(false);
    setGlobalError(null);
    try {
      await api.put(`/tenants/current/branding`, data);
      setSaved(true);
      setTimeout(() => setSaved(false), 3000);
    } catch (err) {
      mapApiErrorsToForm(err, setError);
      setGlobalError(getApiErrorMessage(err));
    }
  };

  return (
    <div className="max-w-2xl space-y-8 p-6">
      <div>
        <h1 className="text-2xl font-bold">Settings</h1>
        <p className="text-muted-foreground">System configuration and tenant preferences.</p>
      </div>

      {/* Branding Section */}
      <section aria-labelledby="branding-heading">
        <h2 id="branding-heading" className="mb-4 border-b pb-2 text-lg font-semibold">
          Branding
        </h2>
        {globalError && (
          <div
            role="alert"
            className="bg-destructive/10 text-destructive mb-4 rounded-md p-3 text-sm"
          >
            {globalError}
          </div>
        )}
        {saved && (
          <div role="status" className="mb-4 rounded-md bg-green-50 p-3 text-sm text-green-800">
            ✓ Branding settings saved.
          </div>
        )}
        <form onSubmit={handleSubmit(onBrandingSubmit)} noValidate className="space-y-4">
          <div>
            <label htmlFor="logoUrl" className="mb-1 block text-sm font-medium">
              Logo URL
            </label>
            <input
              id="logoUrl"
              type="url"
              {...register("logoUrl")}
              placeholder="https://your-domain.com/logo.png"
              className="focus:ring-ring w-full rounded-md border px-3 py-2 text-sm focus:ring-2 focus:outline-none"
              aria-describedby={errors.logoUrl ? "logoUrl-error" : undefined}
            />
            {errors.logoUrl && (
              <p id="logoUrl-error" role="alert" className="text-destructive mt-1 text-xs">
                {errors.logoUrl.message}
              </p>
            )}
          </div>
          <div>
            <label htmlFor="primaryColor" className="mb-1 block text-sm font-medium">
              Primary Color
            </label>
            <div className="flex items-center gap-2">
              <input
                id="primaryColor"
                type="text"
                {...register("primaryColor")}
                placeholder="#3B82F6"
                maxLength={7}
                className="focus:ring-ring w-32 rounded-md border px-3 py-2 font-mono text-sm focus:ring-2 focus:outline-none"
                aria-describedby={errors.primaryColor ? "primaryColor-error" : undefined}
              />
              <input
                type="color"
                className="h-9 w-9 cursor-pointer rounded border"
                aria-label="Color picker"
                onChange={(e) => {
                  const input = document.getElementById("primaryColor") as HTMLInputElement;
                  if (input) input.value = e.target.value;
                }}
              />
            </div>
            {errors.primaryColor && (
              <p id="primaryColor-error" role="alert" className="text-destructive mt-1 text-xs">
                {errors.primaryColor.message}
              </p>
            )}
          </div>
          <button
            type="submit"
            disabled={isSubmitting}
            className="bg-primary text-primary-foreground hover:bg-primary/90 rounded-md px-6 py-2 text-sm font-medium disabled:opacity-50"
            aria-busy={isSubmitting}
          >
            {isSubmitting ? "Saving…" : "Save Branding"}
          </button>
        </form>
      </section>

      {/* Feature Flags Section (read-only display) */}
      <section aria-labelledby="features-heading">
        <h2 id="features-heading" className="mb-4 border-b pb-2 text-lg font-semibold">
          Feature Flags
        </h2>
        <p className="text-muted-foreground mb-3 text-sm">
          Feature flags are configured via environment variables or Redis keys. Contact your
          administrator to enable specialized data stores.
        </p>
        <div className="grid grid-cols-2 gap-2 text-sm">
          {[
            { name: "OpenSearch", env: "FEATURE_OPENSEARCH" },
            { name: "Neo4j", env: "FEATURE_NEO4J" },
            { name: "TimescaleDB", env: "FEATURE_TIMESCALEDB" },
            { name: "ClickHouse", env: "FEATURE_CLICKHOUSE" },
            { name: "EventStoreDB", env: "FEATURE_EVENTSTOREDB" },
            { name: "MongoDB", env: "FEATURE_MONGODB" },
          ].map((f) => (
            <div key={f.name} className="flex items-center gap-2 rounded border p-2">
              <span className="h-2 w-2 rounded-full bg-gray-300" aria-hidden="true" />
              <span className="font-medium">{f.name}</span>
              <span className="text-muted-foreground ml-auto font-mono text-xs">{f.env}</span>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}
