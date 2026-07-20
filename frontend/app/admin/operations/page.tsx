"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api-client";
import { getApiErrorMessage } from "@/lib/form-utils";

interface Job {
  id: string;
  type: string;
  status: "QUEUED" | "RUNNING" | "COMPLETED" | "FAILED" | "CANCELLED";
  tenantId: string;
  progressPercent: number;
  errorMessage?: string;
  createdAt: string;
  startedAt?: string;
  completedAt?: string;
}

interface PageResult {
  content: Job[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
}

const STATUS_COLORS: Record<Job["status"], string> = {
  QUEUED: "bg-yellow-100 text-yellow-800",
  RUNNING: "bg-blue-100 text-blue-800",
  COMPLETED: "bg-green-100 text-green-800",
  FAILED: "bg-red-100 text-red-800",
  CANCELLED: "bg-gray-100 text-gray-800",
};

/**
 * Operations page: job monitoring with retry/cancel actions.
 * Validates: P0.K.3.3 — admin/operations/page.tsx — Job list + health status
 */
export default function OperationsPage() {
  const [jobs, setJobs] = useState<Job[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState<string | null>(null);

  const loadJobs = async () => {
    try {
      setLoading(true);
      const result = await api.get<PageResult>("/operations/jobs?size=20");
      setJobs(result.content);
      setError(null);
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadJobs();
  }, []);

  const handleRetry = async (jobId: string) => {
    try {
      setActionLoading(jobId);
      await api.post(`/operations/jobs/${jobId}/retry`);
      await loadJobs();
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setActionLoading(null);
    }
  };

  const handleCancel = async (jobId: string) => {
    try {
      setActionLoading(jobId);
      await api.post(`/operations/jobs/${jobId}/cancel`);
      await loadJobs();
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setActionLoading(null);
    }
  };

  return (
    <div className="p-6">
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Operations</h1>
          <p className="text-muted-foreground">
            System health monitoring and background job management.
          </p>
        </div>
        <button
          onClick={loadJobs}
          className="rounded-md border px-4 py-2 text-sm hover:bg-accent"
          aria-label="Refresh jobs list"
        >
          Refresh
        </button>
      </div>

      {error && (
        <div
          role="alert"
          className="mb-4 rounded-md bg-destructive/10 p-4 text-destructive"
        >
          {error}
        </div>
      )}

      {loading ? (
        <div
          aria-busy="true"
          aria-live="polite"
          className="text-muted-foreground"
        >
          Loading jobs...
        </div>
      ) : jobs.length === 0 ? (
        <div className="rounded-md border p-8 text-center text-muted-foreground">
          No jobs found.
        </div>
      ) : (
        <div className="overflow-x-auto rounded-md border">
          <table className="w-full text-sm">
            <thead className="bg-muted/50">
              <tr>
                <th className="px-4 py-3 text-left font-medium">Job ID</th>
                <th className="px-4 py-3 text-left font-medium">Type</th>
                <th className="px-4 py-3 text-left font-medium">Status</th>
                <th className="px-4 py-3 text-left font-medium">Progress</th>
                <th className="px-4 py-3 text-left font-medium">Created</th>
                <th className="px-4 py-3 text-left font-medium">Actions</th>
              </tr>
            </thead>
            <tbody>
              {jobs.map((job) => (
                <tr key={job.id} className="border-t hover:bg-muted/25">
                  <td className="px-4 py-3 font-mono text-xs">
                    {job.id.slice(0, 8)}…
                  </td>
                  <td className="px-4 py-3">{job.type}</td>
                  <td className="px-4 py-3">
                    <span
                      className={`rounded-full px-2 py-1 text-xs font-medium ${STATUS_COLORS[job.status]}`}
                    >
                      {job.status}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-2">
                      <div className="h-2 w-24 overflow-hidden rounded-full bg-muted">
                        <div
                          className="h-full bg-primary transition-all"
                          style={{ width: `${job.progressPercent ?? 0}%` }}
                        />
                      </div>
                      <span className="text-xs">
                        {job.progressPercent ?? 0}%
                      </span>
                    </div>
                  </td>
                  <td className="px-4 py-3 text-xs text-muted-foreground">
                    {new Date(job.createdAt).toLocaleString()}
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex gap-2">
                      {job.status === "FAILED" && (
                        <button
                          onClick={() => handleRetry(job.id)}
                          disabled={actionLoading === job.id}
                          className="rounded px-2 py-1 text-xs bg-blue-100 text-blue-800 hover:bg-blue-200 disabled:opacity-50"
                          aria-label={`Retry job ${job.id}`}
                        >
                          {actionLoading === job.id ? "…" : "Retry"}
                        </button>
                      )}
                      {(job.status === "QUEUED" ||
                        job.status === "RUNNING") && (
                        <button
                          onClick={() => handleCancel(job.id)}
                          disabled={actionLoading === job.id}
                          className="rounded px-2 py-1 text-xs bg-red-100 text-red-800 hover:bg-red-200 disabled:opacity-50"
                          aria-label={`Cancel job ${job.id}`}
                        >
                          {actionLoading === job.id ? "…" : "Cancel"}
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
