"use client";

import { useEffect, useState } from "react";
import Image from "next/image";
import { api } from "@/lib/api-client";
import { getApiErrorMessage } from "@/lib/form-utils";

interface Tenant {
  id: string;
  name: string;
  active: boolean;
  maintenanceMode: boolean;
  logoUrl?: string;
  primaryColor?: string;
  createdAt: string;
}

/**
 * Admin tenants management page.
 * Lists all tenants with maintenance mode toggle and branding info.
 * Validates: P2.11 — Tenant read-only mode, P2.12 — Branding (task 17)
 */
export default function TenantsPage() {
  const [tenants, setTenants] = useState<Tenant[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState<string | null>(null);

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        const result = await api.get<{ content: Tenant[] }>("/tenants?size=50");
        setTenants(result.content ?? []);
      } catch (err) {
        setError(getApiErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };
    void load();
  }, []);

  const toggleMaintenance = async (tenantId: string, currentMode: boolean) => {
    try {
      setActionLoading(tenantId);
      await api.put(`/tenants/${tenantId}/maintenance`, { maintenanceMode: !currentMode });
      setTenants((prev) =>
        prev.map((t) => (t.id === tenantId ? { ...t, maintenanceMode: !t.maintenanceMode } : t))
      );
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setActionLoading(null);
    }
  };

  if (loading)
    return (
      <div className="text-muted-foreground p-6" aria-busy="true">
        Loading tenants…
      </div>
    );
  if (error)
    return (
      <div className="text-destructive p-6" role="alert">
        {error}
      </div>
    );

  return (
    <div className="p-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold">Tenants</h1>
        <p className="text-muted-foreground text-sm">{tenants.length} tenants registered</p>
      </div>

      {tenants.length === 0 ? (
        <div className="text-muted-foreground rounded-lg border p-8 text-center">
          No tenants found.
        </div>
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          {tenants.map((t) => (
            <div key={t.id} className="rounded-lg border p-4">
              <div className="flex items-start justify-between">
                <div className="flex items-center gap-3">
                  {t.logoUrl ? (
                    <Image
                      src={t.logoUrl}
                      alt={`${t.name} logo`}
                      width={32}
                      height={32}
                      className="rounded object-contain"
                    />
                  ) : (
                    <div className="bg-muted flex h-8 w-8 items-center justify-center rounded text-xs font-bold">
                      {t.name.charAt(0).toUpperCase()}
                    </div>
                  )}
                  <div>
                    <p className="font-semibold">{t.name}</p>
                    <p className="text-muted-foreground font-mono text-xs">{t.id}</p>
                  </div>
                </div>
                <div className="flex flex-col items-end gap-1">
                  <span
                    className={`rounded-full px-2 py-0.5 text-xs ${t.active ? "bg-green-100 text-green-700" : "bg-gray-100 text-gray-500"}`}
                  >
                    {t.active ? "Active" : "Inactive"}
                  </span>
                  {t.maintenanceMode && (
                    <span className="rounded-full bg-orange-100 px-2 py-0.5 text-xs text-orange-700">
                      Maintenance
                    </span>
                  )}
                </div>
              </div>

              <div className="mt-3 flex items-center justify-between">
                <p className="text-muted-foreground text-xs">
                  Created {new Date(t.createdAt).toLocaleDateString()}
                </p>
                <button
                  onClick={() => void toggleMaintenance(t.id, t.maintenanceMode)}
                  disabled={actionLoading === t.id}
                  className="hover:bg-muted rounded border px-3 py-1 text-xs disabled:opacity-50"
                  aria-label={
                    t.maintenanceMode ? "Disable maintenance mode" : "Enable maintenance mode"
                  }
                >
                  {actionLoading === t.id
                    ? "…"
                    : t.maintenanceMode
                      ? "Exit maintenance"
                      : "Maintenance mode"}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
