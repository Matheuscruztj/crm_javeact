"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api-client";
import { getApiErrorMessage } from "@/lib/form-utils";

interface DashboardMetrics {
  tenantId: string;
  metrics: Record<string, number>;
  computedAt: string;
}

const METRIC_LABELS: Record<string, string> = {
  CUSTOMER_COUNT: "Total Customers",
  REQUEST_COUNT: "Total Requests",
  ACTIVE_REQUEST_COUNT: "Active Requests",
  REQUEST_COUNT_LAST_30D: "Requests (30 days)",
  DOCUMENT_COUNT: "Total Documents",
  AI_ANALYZED_DOCUMENT_COUNT: "AI Analyzed Docs",
  AI_AVG_CONFIDENCE: "Avg AI Confidence",
  PENDING_APPROVAL_COUNT: "Pending Approvals",
};

/**
 * Admin analytics dashboard page.
 * Displays key metrics from GET /api/v1/analytics/dashboard.
 * Validates: P0.I.2 — Analytics Module Foundation (task 13)
 */
export default function AnalyticsPage() {
  const [data, setData] = useState<DashboardMetrics | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        const result = await api.get<DashboardMetrics>("/analytics/dashboard");
        setData(result);
      } catch (err) {
        setError(getApiErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };
    void load();
  }, []);

  if (loading) return <div className="p-6 text-muted-foreground" aria-busy="true">Loading analytics…</div>;
  if (error) return <div className="p-6 text-destructive" role="alert">{error}</div>;

  const metrics = data?.metrics ?? {};

  return (
    <div className="p-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold">Analytics</h1>
        <p className="text-sm text-muted-foreground">
          Key metrics for your tenant.
          {data?.computedAt && (
            <span className="ml-2 text-xs">Updated {new Date(data.computedAt).toLocaleString()}</span>
          )}
        </p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        {Object.entries(metrics).map(([key, value]) => (
          <div key={key} className="rounded-lg border p-4">
            <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
              {METRIC_LABELS[key] ?? key.replace(/_/g, " ")}
            </p>
            <p className="mt-2 text-3xl font-bold">
              {key === "AI_AVG_CONFIDENCE"
                ? `${(value * 100).toFixed(1)}%`
                : value.toLocaleString()}
            </p>
          </div>
        ))}

        {Object.keys(metrics).length === 0 && (
          <div className="col-span-4 rounded-lg border p-8 text-center text-muted-foreground">
            No metrics available yet.
          </div>
        )}
      </div>
    </div>
  );
}
