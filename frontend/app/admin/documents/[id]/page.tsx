"use client";

/**
 * Document detail page: metadata, AI analysis, preview, reprocess.
 * Validates: P1.14.6 — /admin/documents/[id]
 */

import { use } from "react";
import Link from "next/link";
import { useDocument } from "@/hooks/use-api";
import { useMutation } from "@/hooks/use-api";
import { getApiErrorMessage } from "@/lib/form-utils";

const STATUS_COLORS: Record<string, string> = {
  UPLOADED: "bg-blue-100 text-blue-800",
  TEXT_EXTRACTED: "bg-yellow-100 text-yellow-800",
  ANALYZED: "bg-green-100 text-green-800",
  FAILED: "bg-red-100 text-red-800",
  REPROCESSING: "bg-purple-100 text-purple-800",
};

export default function DocumentDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const { data: document, loading, error, refetch } = useDocument(id);
  const reprocessMutation = useMutation(`/documents/${id}/reprocess`, "post");

  const handleReprocess = async () => {
    try {
      await reprocessMutation.mutate({});
      refetch();
    } catch {
      /* handled */
    }
  };

  if (loading) {
    return (
      <div className="p-6" aria-busy="true">
        <div className="h-8 w-64 animate-pulse rounded bg-muted mb-4" />
        <div className="h-4 w-48 animate-pulse rounded bg-muted" />
      </div>
    );
  }

  if (error || !document) {
    return (
      <div className="p-6" role="alert">
        <p className="text-destructive">{getApiErrorMessage(error) || "Document not found."}</p>
        <Link href="/admin/documents" className="mt-2 inline-block text-sm text-primary hover:underline">
          ← Back to documents
        </Link>
      </div>
    );
  }

  const canReprocess = document.status === "FAILED" || document.status === "ANALYZED";

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex items-start justify-between">
        <div>
          <Link href="/admin/documents" className="text-sm text-muted-foreground hover:underline">
            ← Documents
          </Link>
          <h1 className="mt-1 text-2xl font-bold truncate">{document.filename}</h1>
          <div className="mt-1 flex items-center gap-2">
            <span
              className={`rounded-full px-2 py-0.5 text-xs font-medium ${
                STATUS_COLORS[document.status] ?? "bg-muted text-muted-foreground"
              }`}
            >
              {document.status}
            </span>
            <span className="text-xs text-muted-foreground">{document.contentType}</span>
          </div>
        </div>
        {canReprocess && (
          <button
            onClick={handleReprocess}
            disabled={reprocessMutation.loading}
            className="rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground hover:bg-primary/90 disabled:opacity-50"
            aria-label="Reprocess document with AI"
            aria-busy={reprocessMutation.loading}
          >
            {reprocessMutation.loading ? "Reprocessing…" : "Reprocess"}
          </button>
        )}
      </div>

      {reprocessMutation.error && (
        <div role="alert" className="rounded-md bg-destructive/10 p-3 text-sm text-destructive">
          {getApiErrorMessage(reprocessMutation.error)}
        </div>
      )}

      {/* Metadata */}
      <div className="rounded-md border p-4 grid grid-cols-2 gap-4 text-sm">
        <div>
          <dt className="font-medium text-muted-foreground">Created</dt>
          <dd>{new Date(document.createdAt).toLocaleDateString()}</dd>
        </div>
        <div>
          <dt className="font-medium text-muted-foreground">Type</dt>
          <dd>{document.contentType}</dd>
        </div>
      </div>

      {/* Processing status message */}
      {document.status === "ANALYZED" && (
        <div className="rounded-md bg-green-50 p-4 text-sm text-green-800">
          ✓ Document has been analyzed by AI. View the approval for the analysis results.
        </div>
      )}
      {document.status === "FAILED" && (
        <div role="alert" className="rounded-md bg-red-50 p-4 text-sm text-red-800">
          Document processing failed. Use the Reprocess button to retry AI analysis.
        </div>
      )}
    </div>
  );
}
