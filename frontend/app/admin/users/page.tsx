"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api-client";
import { getApiErrorMessage } from "@/lib/form-utils";

interface User {
  id: string;
  name: string;
  email: string;
  role: "ADMIN" | "ANALYST" | "CLIENT";
  active: boolean;
  tenantId: string;
  createdAt: string;
}

const ROLE_COLORS: Record<string, string> = {
  ADMIN: "bg-purple-100 text-purple-800",
  ANALYST: "bg-blue-100 text-blue-800",
  CLIENT: "bg-gray-100 text-gray-700",
};

/**
 * Admin users management page.
 * Lists all users in the tenant with role badges and status.
 * Validates: Requirements 7.x — Users Module (task 16)
 */
export default function UsersPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState("");

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        const result = await api.get<{ content: User[] }>("/users?size=100");
        setUsers(result.content ?? []);
      } catch (err) {
        setError(getApiErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };
    void load();
  }, []);

  const filtered = users.filter(
    (u) =>
      search.trim() === "" ||
      u.name.toLowerCase().includes(search.toLowerCase()) ||
      u.email.toLowerCase().includes(search.toLowerCase())
  );

  if (loading)
    return (
      <div className="text-muted-foreground p-6" aria-busy="true">
        Loading users…
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
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold">Users</h1>
        <span className="text-muted-foreground text-sm">{users.length} total</span>
      </div>

      {/* Search */}
      <div className="mb-4">
        <input
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          placeholder="Search by name or email…"
          className="w-full max-w-sm rounded-lg border px-4 py-2 text-sm"
          aria-label="Search users"
        />
      </div>

      {filtered.length === 0 ? (
        <div className="text-muted-foreground rounded-lg border p-8 text-center">
          {search ? "No users match your search." : "No users found."}
        </div>
      ) : (
        <div className="overflow-x-auto rounded-lg border">
          <table className="w-full text-sm">
            <thead className="bg-muted/50">
              <tr>
                <th className="px-4 py-3 text-left font-medium">Name</th>
                <th className="px-4 py-3 text-left font-medium">Email</th>
                <th className="px-4 py-3 text-left font-medium">Role</th>
                <th className="px-4 py-3 text-left font-medium">Status</th>
                <th className="px-4 py-3 text-left font-medium">Created</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((u) => (
                <tr key={u.id} className="hover:bg-muted/25 border-t">
                  <td className="px-4 py-3 font-medium">{u.name}</td>
                  <td className="text-muted-foreground px-4 py-3">{u.email}</td>
                  <td className="px-4 py-3">
                    <span
                      className={`rounded-full px-2 py-0.5 text-xs font-medium ${ROLE_COLORS[u.role] ?? "bg-muted"}`}
                    >
                      {u.role}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <span
                      className={`rounded-full px-2 py-0.5 text-xs ${u.active ? "bg-green-100 text-green-700" : "bg-gray-100 text-gray-500"}`}
                    >
                      {u.active ? "Active" : "Inactive"}
                    </span>
                  </td>
                  <td className="text-muted-foreground px-4 py-3 text-xs">
                    {new Date(u.createdAt).toLocaleDateString()}
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
