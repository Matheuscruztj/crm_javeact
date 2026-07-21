"use client";

import { useState } from "react";
import { api } from "@/lib/api-client";
import { getApiErrorMessage } from "@/lib/form-utils";

interface PromptTemplate {
  id: string;
  name: string;
  version: number;
  versionIdentifier: string;
  active: boolean;
  createdAt: string;
}

/**
 * Admin AI management page — prompt version registry.
 * Shows active prompts and allows registering new versions.
 * Validates: P0.F.2 — Prompt Version Registry (task 14)
 */
export default function AIAdminPage() {
  const [prompts, setPrompts] = useState<PromptTemplate[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [lookupName, setLookupName] = useState("");
  const [newContent, setNewContent] = useState("");
  const [newName, setNewName] = useState("");
  const [registering, setRegistering] = useState(false);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  const lookupActivePrompt = async () => {
    if (!lookupName.trim()) return;
    try {
      setLoading(true);
      const result = await api.get<PromptTemplate>(`/admin/prompts/${lookupName}/active`);
      setPrompts([result]);
      setError(null);
    } catch (err) {
      setError(getApiErrorMessage(err));
      setPrompts([]);
    } finally {
      setLoading(false);
    }
  };

  const registerNewVersion = async () => {
    if (!newName.trim() || !newContent.trim()) return;
    try {
      setRegistering(true);
      await api.post<PromptTemplate>(`/admin/prompts/${newName}`, {
        content: newContent,
        active: true,
      });
      setSuccessMsg(`Prompt "${newName}" registered as new active version`);
      setNewContent("");
      setNewName("");
      setError(null);
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setRegistering(false);
    }
  };

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <h1 className="mb-6 text-2xl font-bold">AI Management</h1>

      {error && <div className="mb-4 rounded bg-destructive/10 p-3 text-sm text-destructive" role="alert">{error}</div>}
      {successMsg && <div className="mb-4 rounded bg-green-50 p-3 text-sm text-green-700">{successMsg}</div>}

      {/* Lookup active prompt */}
      <section className="mb-8 rounded-lg border p-4">
        <h2 className="mb-3 text-sm font-semibold">Lookup Active Prompt</h2>
        <div className="flex gap-2">
          <input value={lookupName} onChange={(e) => setLookupName(e.target.value)}
            placeholder="Prompt name (e.g. document-analysis)"
            className="flex-1 rounded border px-3 py-2 text-sm"
            aria-label="Prompt name to look up" />
          <button onClick={lookupActivePrompt} disabled={loading || !lookupName.trim()}
            className="rounded bg-primary px-4 py-2 text-sm text-primary-foreground disabled:opacity-50">
            {loading ? "Loading…" : "Look up"}
          </button>
        </div>

        {prompts.length > 0 && (
          <ul className="mt-3 divide-y rounded border">
            {prompts.map((p) => (
              <li key={p.id} className="flex items-center justify-between px-4 py-3">
                <div>
                  <p className="text-sm font-medium">{p.name} <span className="text-muted-foreground">v{p.version}</span></p>
                  <p className="text-xs text-muted-foreground">{new Date(p.createdAt).toLocaleString()}</p>
                </div>
                {p.active && <span className="rounded-full bg-green-100 px-2 py-0.5 text-xs text-green-700">Active</span>}
              </li>
            ))}
          </ul>
        )}
      </section>

      {/* Register new version */}
      <section className="rounded-lg border p-4">
        <h2 className="mb-3 text-sm font-semibold">Register New Prompt Version</h2>
        <div className="space-y-3">
          <input value={newName} onChange={(e) => setNewName(e.target.value)}
            placeholder="Prompt name" className="w-full rounded border px-3 py-2 text-sm"
            aria-label="New prompt name" />
          <textarea value={newContent} onChange={(e) => setNewContent(e.target.value)}
            placeholder="Prompt template content (use {{variable}} for placeholders)"
            rows={6} className="w-full rounded border px-3 py-2 text-sm font-mono"
            aria-label="Prompt template content" />
          <button onClick={registerNewVersion} disabled={registering || !newName.trim() || !newContent.trim()}
            className="rounded bg-primary px-4 py-2 text-sm text-primary-foreground disabled:opacity-50">
            {registering ? "Registering…" : "Register & Activate"}
          </button>
        </div>
      </section>
    </div>
  );
}
