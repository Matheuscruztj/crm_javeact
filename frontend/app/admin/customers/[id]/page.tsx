"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { api } from "@/lib/api-client";
import { getApiErrorMessage } from "@/lib/form-utils";

interface Customer {
  id: string;
  name: string;
  email: string;
  status: "ACTIVE" | "INACTIVE";
  tenantId: string;
  createdAt: string;
}

interface Request {
  id: string;
  title: string;
  status: string;
  priority: string;
  createdAt: string;
}

interface AssociatedUser {
  userId: string;
  email: string;
  role: string;
}

/**
 * Admin customer detail page.
 * Shows customer info, associated users, and linked requests.
 * Validates: P1.14.2 — /admin/customers/[id] detail (task 39)
 */
export default function CustomerDetailPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();

  const [customer, setCustomer] = useState<Customer | null>(null);
  const [requests, setRequests] = useState<Request[]>([]);
  const [users, setUsers] = useState<AssociatedUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [tab, setTab] = useState<"info" | "requests" | "users">("info");

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        const [cust, reqs] = await Promise.all([
          api.get<Customer>(`/customers/${id}`),
          api.get<{ content: Request[] }>(`/requests?customerId=${id}&size=20`),
        ]);
        setCustomer(cust);
        setRequests(reqs.content ?? []);
      } catch (err) {
        setError(getApiErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };
    void load();
  }, [id]);

  const handleActivate = async () => {
    try {
      await api.patch(`/customers/${id}/activate`, {});
      setCustomer((c) => c ? { ...c, status: "ACTIVE" } : c);
    } catch (err) {
      setError(getApiErrorMessage(err));
    }
  };

  const handleDeactivate = async () => {
    try {
      await api.patch(`/customers/${id}/deactivate`, {});
      setCustomer((c) => c ? { ...c, status: "INACTIVE" } : c);
    } catch (err) {
      setError(getApiErrorMessage(err));
    }
  };

  if (loading) return <div className="p-6 text-muted-foreground" aria-busy="true">Loading...</div>;
  if (error) return <div className="p-6 text-destructive" role="alert">{error}</div>;
  if (!customer) return <div className="p-6">Customer not found.</div>;

  return (
    <div className="p-6 max-w-4xl mx-auto">
      {/* Header */}
      <div className="mb-6 flex items-start justify-between">
        <div>
          <button onClick={() => router.back()} className="mb-2 text-sm text-muted-foreground hover:underline" aria-label="Go back">
            ← Back
          </button>
          <h1 className="text-2xl font-bold">{customer.name}</h1>
          <p className="text-muted-foreground">{customer.email}</p>
        </div>
        <div className="flex items-center gap-2">
          <span className={`rounded-full px-3 py-1 text-xs font-medium ${customer.status === "ACTIVE" ? "bg-green-100 text-green-800" : "bg-gray-100 text-gray-600"}`}>
            {customer.status}
          </span>
          {customer.status === "ACTIVE" ? (
            <button onClick={handleDeactivate} className="rounded border px-3 py-1 text-xs hover:bg-destructive/10">
              Deactivate
            </button>
          ) : (
            <button onClick={handleActivate} className="rounded border px-3 py-1 text-xs hover:bg-green-50">
              Activate
            </button>
          )}
        </div>
      </div>

      {/* Tabs */}
      <div className="mb-4 flex gap-4 border-b">
        {(["info", "requests", "users"] as const).map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={`pb-2 text-sm capitalize ${tab === t ? "border-b-2 border-primary font-medium" : "text-muted-foreground"}`}
            aria-selected={tab === t}
          >
            {t}
          </button>
        ))}
      </div>

      {/* Tab content */}
      {tab === "info" && (
        <dl className="grid gap-4 sm:grid-cols-2">
          <div><dt className="text-xs text-muted-foreground">ID</dt><dd className="font-mono text-sm">{customer.id}</dd></div>
          <div><dt className="text-xs text-muted-foreground">Tenant</dt><dd className="text-sm">{customer.tenantId}</dd></div>
          <div><dt className="text-xs text-muted-foreground">Created</dt><dd className="text-sm">{new Date(customer.createdAt).toLocaleString()}</dd></div>
        </dl>
      )}

      {tab === "requests" && (
        <div>
          {requests.length === 0 ? (
            <p className="text-muted-foreground text-sm">No requests found.</p>
          ) : (
            <ul className="divide-y rounded border">
              {requests.map((r) => (
                <li key={r.id} className="flex items-center justify-between px-4 py-3 hover:bg-muted/25">
                  <span className="text-sm">{r.title}</span>
                  <span className={`rounded-full px-2 py-0.5 text-xs font-medium bg-muted`}>{r.status}</span>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}

      {tab === "users" && (
        <div>
          {users.length === 0 ? (
            <p className="text-muted-foreground text-sm">No associated users.</p>
          ) : (
            <ul className="divide-y rounded border">
              {users.map((u) => (
                <li key={u.userId} className="flex justify-between px-4 py-3 text-sm">
                  <span>{u.email}</span>
                  <span className="text-muted-foreground">{u.role}</span>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}
