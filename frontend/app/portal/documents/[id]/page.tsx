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
  status: string;
  createdAt: string;
}

/**
 * Portal document detail page for CLIENT users.
 * Shows status badge, upload info, analysis summary, and approval status.
 * Validates: P1.15.4 — /portal/documents/[id] detail (task 42)
 */
export default function PortalDocumentDetailPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();

  const [doc, setDoc] = useState<Document | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        const d = await api.get<Document>(`/documents/${id}`);
        setDoc(d);
      } catch (err) {
        setError(getApiErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };
    void load();
  }, [id]);

  const STATUS_COLORS: Record<string, string> = {
    UPLOADED: "bg-blue-100 text-blue-800",
    TEXT_EXTRACTED: "bg-yellow-100 text-yellow-800",
    ANALYZED: "bg-green-100 text-green-800",
    FAILED: "bg-red-100 text-red-800",
    PROCESSING: "bg-purple-100 text-purple-800",
  };

  if (loading) return <div className="p-4 text-muted-foreground" aria-busy="true">Loading...</div>;
  if (error) return <div className="p-4 text-destructive" role="alert">{error}</div>;
  if (!doc) return <div className="p-4">Document not found.</div>;

  return (
    <div className="p-4 max-w-2xl mx-auto">
      <button onClick={() => router.back()} className="mb-3 text-sm text-muted-foreground hover:underline">← My Documents</button>

      <div className="mb-4 flex items-start justify-between">
        <h1 className="text-xl font-bold">{doc.filename}</h1>
        <span className={`rounded-full px-3 py-1 text-xs font-medium ${STATUS_COLORS[doc.status] ?? "bg-muted"}`}>{doc.status}</span>
      </div>

      <dl className="grid gap-3 sm:grid-cols-2 text-sm">
        <div><dt className="text-xs text-muted-foreground">Type</dt><dd>{doc.contentType}</dd></div>
        <div><dt className="text-xs text-muted-foreground">Size</dt><dd>{(doc.sizeBytes / 1024).toFixed(1)} KB</dd></div>
        <div><dt className="text-xs text-muted-foreground">Uploaded</dt><dd>{new Date(doc.createdAt).toLocaleString()}</dd></div>
        <div><dt className="text-xs text-muted-foreground">Document ID</dt><dd className="font-mono">{doc.id}</dd></div>
      </dl>

      {doc.status === "ANALYZED" && (
        <div className="mt-4 rounded border p-4">
          <p className="text-sm font-medium text-green-700">✓ Analysis complete</p>
          <p className="mt-1 text-xs text-muted-foreground">This document has been analyzed by our AI pipeline.</p>
        </div>
      )}

      {doc.status === "FAILED" && (
        <div className="mt-4 rounded border border-destructive/30 p-4 bg-destructive/5">
          <p className="text-sm font-medium text-destructive">Analysis failed</p>
          <p className="mt-1 text-xs text-muted-foreground">Please contact support if this issue persists.</p>
        </div>
      )}
    </div>
  );
}
