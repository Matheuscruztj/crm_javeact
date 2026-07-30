/**
 * Admin Dashboard Page with summary cards.
 * Task 22.1: Implement admin dashboard page with summary cards.
 * - Create `/app/admin/dashboard/page.tsx` with cards: active customers, requests by status,
 *   documents processed, pending approvals
 * - Fetch summary data from API endpoints
 * Requirements: 22.1
 */

"use client";

import { useEffect, useState } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { api } from "@/lib/api-client";

interface DashboardStats {
  activeCustomers: number;
  totalRequests: number;
  requestsByStatus: {
    OPEN: number;
    IN_PROGRESS: number;
    RESOLVED: number;
    CLOSED: number;
  };
  documentsProcessed: number;
  pendingApprovals: number;
}

interface StatCardProps {
  title: string;
  value: number | string;
  description?: string;
  icon: React.ReactNode;
  isLoading?: boolean;
}

function StatCard({ title, value, description, icon, isLoading }: StatCardProps) {
  return (
    <Card>
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-sm font-medium">{title}</CardTitle>
        <div className="text-muted-foreground h-4 w-4">{icon}</div>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <Skeleton className="h-8 w-20" />
        ) : (
          <div className="text-2xl font-bold">{value}</div>
        )}
        {description && <p className="text-muted-foreground text-xs">{description}</p>}
      </CardContent>
    </Card>
  );
}

function RequestStatusCard({
  requestsByStatus,
  isLoading,
}: {
  requestsByStatus: DashboardStats["requestsByStatus"] | null;
  isLoading: boolean;
}) {
  const statuses = [
    { key: "OPEN", label: "Open", color: "bg-blue-500" },
    { key: "IN_PROGRESS", label: "In Progress", color: "bg-yellow-500" },
    { key: "RESOLVED", label: "Resolved", color: "bg-green-500" },
    { key: "CLOSED", label: "Closed", color: "bg-gray-500" },
  ] as const;

  return (
    <Card className="col-span-full lg:col-span-2">
      <CardHeader>
        <CardTitle className="text-sm font-medium">Requests by Status</CardTitle>
        <CardDescription>Distribution of service requests</CardDescription>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <div className="space-y-3">
            {statuses.map((status) => (
              <Skeleton key={status.key} className="h-8 w-full" />
            ))}
          </div>
        ) : (
          <div className="space-y-3">
            {statuses.map((status) => {
              const count = requestsByStatus?.[status.key] ?? 0;
              const total = requestsByStatus
                ? Object.values(requestsByStatus).reduce((a, b) => a + b, 0)
                : 0;
              const percentage = total > 0 ? (count / total) * 100 : 0;

              return (
                <div key={status.key} className="space-y-1">
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-muted-foreground">{status.label}</span>
                    <span className="font-medium">{count}</span>
                  </div>
                  <div className="bg-secondary h-2 w-full rounded-full">
                    <div
                      className={`h-2 rounded-full ${status.color}`}
                      style={{ width: `${percentage}%` }}
                    />
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </CardContent>
    </Card>
  );
}

export default function DashboardPage() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function fetchDashboardStats() {
      setIsLoading(true);
      setError(null);

      try {
        // Fetch all stats in parallel
        const [customersRes, requestsRes, documentsRes, approvalsRes] = await Promise.allSettled([
          api.get<{ totalActive: number }>("/customers/stats"),
          api.get<{
            total: number;
            byStatus: DashboardStats["requestsByStatus"];
          }>("/requests/stats"),
          api.get<{ processed: number }>("/documents/stats"),
          api.get<{ pending: number }>("/approvals/stats"),
        ]);

        // Extract values with fallbacks
        const activeCustomers =
          customersRes.status === "fulfilled" ? customersRes.value.totalActive : 0;
        const totalRequests = requestsRes.status === "fulfilled" ? requestsRes.value.total : 0;
        const requestsByStatus =
          requestsRes.status === "fulfilled"
            ? requestsRes.value.byStatus
            : { OPEN: 0, IN_PROGRESS: 0, RESOLVED: 0, CLOSED: 0 };
        const documentsProcessed =
          documentsRes.status === "fulfilled" ? documentsRes.value.processed : 0;
        const pendingApprovals =
          approvalsRes.status === "fulfilled" ? approvalsRes.value.pending : 0;

        setStats({
          activeCustomers,
          totalRequests,
          requestsByStatus,
          documentsProcessed,
          pendingApprovals,
        });
      } catch {
        setError("Failed to load dashboard statistics");
      } finally {
        setIsLoading(false);
      }
    }

    fetchDashboardStats();
  }, []);

  return (
    <div className="p-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold">Dashboard</h1>
        <p className="text-muted-foreground">Overview of system metrics and recent activity.</p>
      </div>

      {error && (
        <div
          className="bg-destructive/10 text-destructive mb-6 rounded-md p-4 text-sm"
          role="alert"
        >
          {error}
        </div>
      )}

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <StatCard
          title="Active Customers"
          value={stats?.activeCustomers ?? 0}
          description="Total registered customers"
          isLoading={isLoading}
          icon={
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
              />
            </svg>
          }
        />

        <StatCard
          title="Total Requests"
          value={stats?.totalRequests ?? 0}
          description="All service requests"
          isLoading={isLoading}
          icon={
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
              />
            </svg>
          }
        />

        <StatCard
          title="Documents Processed"
          value={stats?.documentsProcessed ?? 0}
          description="AI-analyzed documents"
          isLoading={isLoading}
          icon={
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
              />
            </svg>
          }
        />

        <StatCard
          title="Pending Approvals"
          value={stats?.pendingApprovals ?? 0}
          description="Awaiting decision"
          isLoading={isLoading}
          icon={
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
          }
        />

        <RequestStatusCard
          requestsByStatus={stats?.requestsByStatus ?? null}
          isLoading={isLoading}
        />
      </div>
    </div>
  );
}
