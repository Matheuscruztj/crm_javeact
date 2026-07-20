"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { api } from "@/lib/api-client";
import { getApiErrorMessage } from "@/lib/form-utils";

interface Document {
  id: string;
  filename: string;
  contentType: string;
  sizeBytes: number;
  status: "UPLOADED" | "TEXT_EXTRACTED" | "ANALYZED" | "FAILED" | "PROCESSING";
  tenantId: string;
  createdAt: string;
  previewKey?: string;
}

interface AnalysisResult {
  summary?: string;
  category?: string;
  confidenceScore?: number;
  fallback?: boolean;
  riskIndicators?: string[];
}

/**
 * Admin document detail page.
 * Shows metadata, AI analysis results, and approval status.
 * Validates: P1.14.6 — /admin/documents/[id] detail (task 40)
 */
export default function DocumentDetailPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();

  const [doc, setDoc] = useState<Document | null>(null);
  const [analysis, setAnalysis] = useState<AnalysisResult | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reprocessing, setReprocessing] = useState(false);

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        const d = await api.get<Document>(`/documents/${id}`);
        setDoc(d);
        if (d.status === "ANALYZED") {
          try {
            const a = await api.get<AnalysisResult>(`/documents/${id}/analysis`);
            setAnalysis(a);
          } catch {
            // analysis not available — not critical
          }
        }
      } catch (err) {
        setError(getApiErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };
    void load();
  }, [id]);

  const handleReprocess = async () => {
    try {
      setReprocessing(true);
      await api.post(`/documents/${id}/reprocess`, {});
      setDoc((d) => d ? { ...d, status: "PROCESSING" } : d);
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setReprocessing(false);
    }
  };

  const STATUS_COLORS: Record<string, string> = {
    UPLOADED: "bg-blue-100 text-blue-800",
    TEXT_EXTRACTED: "bg-yellow-100 text-yellow-800",
    ANALYZED: "bg-green-100 text-green-800",
    FAILED: "bg-red-100 text-red-800",
    PROCESSING: "bg-purple-100 text-purple-800",
  };

  if (loading) return <div className="p-6 text-muted-foreground" aria-busy="true">Loading...</div>;
  if (error) return <div className="p-6 text-destructive" role="alert">{error}</div>;
  if (!doc) return <div className="p-6">Document not found.</div>;

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <button onClick={() => router.back()} className="mb-3 text-sm text-muted-foreground hover:underline">← Back</button>

      <div className="mb-6 flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-bold">{doc.filename}</h1>
          <p className="text-sm text-muted-foreground">{doc.contentType} · {(doc.sizeBytes / 1024).toFixed(1)} KB</p>
        </div>
        <div className="flex items-center gap-2">
          <span className={`rounded-full px-3 py-1 text-xs font-medium ${STATUS_COLORS[doc.status] ?? "bg-muted"}`}>{doc.status}</span>
          {(doc.status === "ANALYZED" || doc.status === "FAILED") && (
            <button onClick={handleReprocess} disabled={reprocessing}
              className="rounded border px-3 py-1 text-xs hover:bg-muted disabled:opacity-50">
              {reprocessing ? "Reprocessing…" : "Reprocess"}
            </button>
          )}
        </div>
      </div>

      {/* Metadata */}
      <section className="mb-6">
        <h2 className="mb-2 text-sm font-semibold">Metadata</h2>
        <dl className="grid gap-3 sm:grid-cols-2">
          <div><dt className="text-xs text-muted-foreground">Document ID</dt><dd className="font-mono text-sm">{doc.id}</dd></div>
          <div><dt className="text-xs text-muted-foreground">Tenant</dt><dd className="text-sm">{doc.tenantId}</dd></div>
          <div><dt className="text-xs text-muted-foreground">Created</dt><dd className="text-sm">{new Date(doc.createdAt).toLocaleString()}</dd></div>
        </dl>
      </section>

      {/* AI Analysis */}
      {analysis && (
        <section className="rounded border p-4">
          <h2 className="mb-3 text-sm font-semibold">AI Analysis</h2>
          <dl className="space-y-2">
            {analysis.summary && <div><dt className="text-xs text-muted-foreground">Summary</dt><dd className="text-sm">{analysis.summary}</dd></div>}
            {analysis.category && <div><dt className="text-xs text-muted-foreground">Category</dt><dd className="text-sm">{analysis.category}</dd></div>}
            {analysis.confidenceScore !== undefined && (
              <div>
                <dt className="text-xs text-muted-foreground">Confidence</dt>
                <dd className="text-sm">{(analysis.confidenceScore * 100).toFixed(1)}%{analysis.fallback && " (fallback)"}</dd>
              </div>
            )}
            {analysis.riskIndicators && analysis.riskIndicators.length > 0 && (
              <div>
                <dt className="text-xs text-muted-foreground">Risk Indicators</dt>
                <dd className="flex flex-wrap gap-1 mt-1">
                  {analysis.riskIndicators.map((r) => (
                    <span key={r} className="rounded-full bg-red-100 px-2 py-0.5 text-xs text-red-800">{r}</span>
                  ))}
                </dd>
              </div>
            )}
          </dl>
        </section>
      )}
    </div>
  );
}
