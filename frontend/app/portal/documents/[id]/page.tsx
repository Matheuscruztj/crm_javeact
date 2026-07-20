"use client";

/**
 * Portal document detail: metadata, status, download link.
 * Validates: P1.15.4 — /portal/documents
 */

import { use } from "react";
import Link from "next/link";
import { useDocument } from "@/hooks/use-api";
import { getApiErrorMessage } from "@/lib/form-utils";

export default function PortalDocumentDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const { data: document, loading, error } = useDocument(id);

  if (loading) {
    return (
      <div className="p-4" aria-busy="true">
        <div className="h-7 w-64 animate-pulse rounded bg-muted mb-3" />
        <div className="h-4 w-48 animate-pulse rounded bg-muted" />
      </div>
    );
  }

  if (error || !document) {
    return (
      <div className="p-4" role="alert">
        <p className="text-destructive">{getApiErrorMessage(error) || "Document not found."}</p>
        <Link href="/portal/documents" className="mt-2 inline-block text-sm text-primary hover:underline">
          ← My Documents
        </Link>
      </div>
    );
  }

  return (
    <div className="p-4 md:p-6 space-y-5">
      <div>
        <Link href="/portal/documents" className="text-sm text-muted-foreground hover:underline">
          ← My Documents
        </Link>
        <h1 className="mt-1 text-xl font-bold truncate">{document.filename}</h1>
        <div className="mt-1 flex flex-wrap items-center gap-2">
          <span className="rounded-full bg-muted px-2 py-0.5 text-xs font-medium">
            {document.status}
          </span>
          <span className="text-xs text-muted-foreground">{document.contentType}</span>
          <span className="text-xs text-muted-foreground">
            {new Date(document.createdAt).toLocaleDateString()}
          </span>
        </div>
      </div>

      {document.status === "ANALYZED" && (
        <div className="rounded-md bg-green-50 p-4 text-sm text-green-800">
          ✓ Document analyzed successfully.
        </div>
      )}

      {document.status === "FAILED" && (
        <div role="alert" className="rounded-md bg-red-50 p-4 text-sm text-red-800">
          Processing failed. Please contact support or re-upload the document.
        </div>
      )}

      {(document.status === "UPLOADED" || document.status === "TEXT_EXTRACTED") && (
        <div className="rounded-md bg-blue-50 p-4 text-sm text-blue-800" aria-live="polite">
          Document is being processed…
        </div>
      )}
    </div>
  );
}
