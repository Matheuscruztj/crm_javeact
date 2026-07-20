"use client";

/**
 * Request detail page: info, comments, documents, status timeline.
 * Validates: P1.14.4 — /admin/requests/[id]
 */

import { use, useState } from "react";
import Link from "next/link";
import { useRequest } from "@/hooks/use-api";
import { api } from "@/lib/api-client";
import { getApiErrorMessage } from "@/lib/form-utils";

interface Comment { id: string; text: string; authorId: string; createdAt: string; }
interface StatusHistory { id: string; fromStatus: string; toStatus: string; reason?: string; createdAt: string; }

export default function RequestDetailPage({
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
      <div className="p-6" aria-busy="true">
        <div className="h-8 w-64 animate-pulse rounded bg-muted mb-4" />
        <div className="h-4 w-96 animate-pulse rounded bg-muted" />
      </div>
    );
  }

  if (error || !request) {
    return (
      <div className="p-6" role="alert">
        <p className="text-destructive">{getApiErrorMessage(error) || "Request not found."}</p>
        <Link href="/admin/requests" className="mt-2 inline-block text-sm text-primary hover:underline">
          ← Back to requests
        </Link>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-6">
      <div>
        <Link href="/admin/requests" className="text-sm text-muted-foreground hover:underline">
          ← Requests
        </Link>
        <h1 className="mt-1 text-2xl font-bold">{request.title}</h1>
        <div className="mt-1 flex gap-3 text-sm text-muted-foreground">
          <span className="rounded-full bg-muted px-2 py-0.5 text-xs font-medium">{request.status}</span>
          <span className="rounded-full bg-muted px-2 py-0.5 text-xs">{request.priority}</span>
          <span>Created {new Date(request.createdAt).toLocaleDateString()}</span>
        </div>
      </div>

      {/* Add comment */}
      <section aria-labelledby="comments-heading">
        <h2 id="comments-heading" className="mb-3 font-semibold">Comments</h2>
        <div className="space-y-2">
          <label htmlFor="comment-input" className="sr-only">Add a comment</label>
          <textarea
            id="comment-input"
            value={comment}
            onChange={(e) => setComment(e.target.value)}
            placeholder="Add a comment…"
            rows={3}
            maxLength={2000}
            className="w-full rounded-md border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring resize-none"
            aria-describedby={commentError ? "comment-error" : undefined}
          />
          {commentError && (
            <p id="comment-error" role="alert" className="text-xs text-destructive">{commentError}</p>
          )}
          <button
            onClick={handleAddComment}
            disabled={submitting || !comment.trim()}
            className="rounded-md bg-primary px-4 py-2 text-sm font-medium text-primary-foreground hover:bg-primary/90 disabled:opacity-50"
            aria-busy={submitting}
          >
            {submitting ? "Posting…" : "Post Comment"}
          </button>
        </div>
      </section>
    </div>
  );
}
