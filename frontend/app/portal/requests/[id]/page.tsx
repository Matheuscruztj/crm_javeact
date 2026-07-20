"use client";

/**
 * Portal request detail: timeline, comments, documents.
 * Validates: P1.15.3 — /portal/requests/[id]
 */

import { use, useState } from "react";
import Link from "next/link";
import { useRequest } from "@/hooks/use-api";
import { api } from "@/lib/api-client";
import { getApiErrorMessage } from "@/lib/form-utils";

export default function PortalRequestDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const { data: request, loading, error, refetch } = useRequest(id);
  const [comment, setComment] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [commentError, setCommentError] = useState<string | null>(null);

  const handleAddComment = async () => {
    if (!comment.trim()) return;
    setSubmitting(true);
    setCommentError(null);
    try {
      await api.post(`/requests/${id}/comments`, { text: comment });
      setComment("");
      refetch();
    } catch (err) {
      setCommentError(getApiErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="p-4" aria-busy="true">
        <div className="h-7 w-56 animate-pulse rounded bg-muted mb-3" />
        <div className="h-4 w-80 animate-pulse rounded bg-muted" />
      </div>
    );
  }

  if (error || !request) {
    return (
      <div className="p-4" role="alert">
        <p className="text-destructive">{getApiErrorMessage(error) || "Request not found."}</p>
        <Link href="/portal/requests" className="mt-2 inline-block text-sm text-primary hover:underline">
          ← My Requests
        </Link>
      </div>
    );
  }

  return (
    <div className="p-4 md:p-6 space-y-5">
      <div>
        <Link href="/portal/requests" className="text-sm text-muted-foreground hover:underline">
          ← My Requests
        </Link>
        <h1 className="mt-1 text-xl font-bold">{request.title}</h1>
        <div className="mt-1 flex flex-wrap gap-2">
          <span className="rounded-full bg-muted px-2 py-0.5 text-xs font-medium">
            {request.status}
          </span>
          <span className="rounded-full bg-muted px-2 py-0.5 text-xs">
            {request.priority}
          </span>
          <span className="text-xs text-muted-foreground">
            {new Date(request.createdAt).toLocaleDateString()}
          </span>
        </div>
      </div>

      {/* Comments */}
      <section aria-labelledby="comments-section">
        <h2 id="comments-section" className="mb-3 font-semibold text-sm">Add Comment</h2>
        <div className="space-y-2">
          <label htmlFor="portal-comment" className="sr-only">Write a comment</label>
          <textarea
            id="portal-comment"
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            placeholder="Write a comment…"
            rows={3}
            maxLength={2000}
            className="w-full rounded-md border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring resize-none"
            aria-describedby={commentError ? "portal-comment-error" : undefined}
          />
          {commentError && (
            <p id="portal-comment-error" role="alert" className="text-xs text-destructive">
              {commentError}
            </p>
          )}
          <button
            onClick={handleAddComment}
            disabled={submitting || !comment.trim()}
            className="w-full rounded-md bg-primary py-2 text-sm font-medium text-primary-foreground hover:bg-primary/90 disabled:opacity-50 sm:w-auto sm:px-6"
            aria-busy={submitting}
          >
            {submitting ? "Posting…" : "Post Comment"}
          </button>
        </div>
      </section>
    </div>
  );
}
