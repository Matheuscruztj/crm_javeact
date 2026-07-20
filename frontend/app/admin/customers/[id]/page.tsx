"use client";

/**
 * Customer detail page: info, users, requests, activate/deactivate.
 * Validates: P1.14.2 — /admin/customers/[id]
 */

import { use } from "react";
import Link from "next/link";
import { useCustomer, useRequests } from "@/hooks/use-api";
import { useMutation } from "@/hooks/use-api";
import { getApiErrorMessage } from "@/lib/form-utils";
import { DataTable, type Column } from "@/components/shared/data-table";

interface ServiceRequest { id: string; title: string; status: string; priority: string; createdAt: string; }

const requestColumns: Column<ServiceRequest>[] = [
  { key: "title", header: "Title" },
  { key: "status", header: "Status", render: (r) => (
    <span className="rounded-full bg-muted px-2 py-0.5 text-xs">{r.status}</span>
  )},
  { key: "priority", header: "Priority" },
  { key: "createdAt", header: "Created", render: (r) => new Date(r.createdAt).toLocaleDateString() },
];

export default function CustomerDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const { data: customer, loading, error, refetch } = useCustomer(id);
  const { data: requestsData, loading: reqLoading } = useRequests(0);
  const activateMutation = useMutation(`/customers/${id}/activate`, "post");
  const deactivateMutation = useMutation(`/customers/${id}/deactivate`, "post");

  const handleToggle = async () => {
    try {
      if (customer?.status === "ACTIVE") {
        await deactivateMutation.mutate({});
      } else {
        await activateMutation.mutate({});
      }
      refetch();
    } catch {
      /* handled by mutation */
    }
  };

  if (loading) {
    return (
      <div className="p-6">
        <div className="h-8 w-48 animate-pulse rounded bg-muted mb-4" aria-busy="true" />
        <div className="h-4 w-96 animate-pulse rounded bg-muted" />
      </div>
    );
  }

  if (error || !customer) {
    return (
      <div className="p-6" role="alert">
        <p className="text-destructive">{getApiErrorMessage(error) || "Customer not found."}</p>
        <Link href="/admin/customers" className="mt-2 inline-block text-sm text-primary hover:underline">
          ← Back to customers
        </Link>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex items-start justify-between">
        <div>
          <Link href="/admin/customers" className="text-sm text-muted-foreground hover:underline">
            ← Customers
          </Link>
          <h1 className="mt-1 text-2xl font-bold">{customer.name}</h1>
          <p className="text-muted-foreground">{customer.email}</p>
        </div>
        <button
          onClick={handleToggle}
          disabled={activateMutation.loading || deactivateMutation.loading}
          className={`rounded-md px-4 py-2 text-sm font-medium disabled:opacity-50 ${
            customer.status === "ACTIVE"
              ? "bg-destructive/10 text-destructive hover:bg-destructive/20"
              : "bg-green-100 text-green-800 hover:bg-green-200"
          }`}
          aria-label={customer.status === "ACTIVE" ? "Deactivate customer" : "Activate customer"}
        >
          {customer.status === "ACTIVE" ? "Deactivate" : "Activate"}
        </button>
      </div>

      {/* Info card */}
      <div className="rounded-md border p-4 grid grid-cols-2 gap-4 text-sm">
        <div>
          <dt className="font-medium text-muted-foreground">Status</dt>
          <dd>
            <span className={`rounded-full px-2 py-0.5 text-xs font-medium ${
              customer.status === "ACTIVE" ? "bg-green-100 text-green-800" : "bg-gray-100 text-gray-700"
            }`}>
              {customer.status}
            </span>
          </dd>
        </div>
        <div>
          <dt className="font-medium text-muted-foreground">Created</dt>
          <dd>{new Date(customer.createdAt).toLocaleDateString()}</dd>
        </div>
      </div>

      {/* Requests */}
      <section aria-labelledby="requests-heading">
        <h2 id="requests-heading" className="mb-3 font-semibold">Recent Requests</h2>
        <DataTable
          columns={requestColumns}
          data={requestsData}
          loading={reqLoading}
          emptyMessage="No requests for this customer."
          onRowClick={(r) => window.location.assign(`/admin/requests/${r.id}`)}
        />
      </section>
    </div>
  );
}
