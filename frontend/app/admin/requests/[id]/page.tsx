"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { api } from "@/lib/api-client";
import { getApiErrorMessage } from "@/lib/form-utils";

interface Request {
  id: string;
  title: string;
  description: string;
  status: string;
  priority: string;
  tenantId: string;
  customerId: string;
  createdAt: string;
  slaDeadline?: string;
}

interface Comment {
  id: string;
  authorId: string;
  content: string;
  createdAt: string;
}

interface StatusHistory {
  from: string;
  to: string;
  reason?: string;
  changedAt: string;
}

/**
 * Admin request detail page.
 * Shows description, status history timeline, comments, and analyst actions.
 * Validates: P1.14.4 — /admin/requests/[id] detail (task 41)
 */
export default function RequestDetailPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();

  const [request, setRequest] = useState<Request | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [history, setHistory] = useState<StatusHistory[]>([]);
  const [newComment, setNewComment] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [tab, setTab] = useState<"info" | "comments" | "history">("info");

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        const [req, cmts, hist] = await Promise.all([
          api.get<Request>(`/requests/${id}`),
          api.get<{ content: Comment[] }>(`/requests/${id}/comments`),
          api.get<StatusHistory[]>(`/requests/${id}/history`),
        ]);
        setRequest(req);
        setComments(cmts.content ?? []);
        setHistory(Array.isArray(hist) ? hist : []);
      } catch (err) {
        setError(getApiErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };
    void load();
  }, [id]);

  const handleAddComment = async () => {
    if (!newComment.trim()) return;
    try {
      const c = await api.post<Comment>(`/requests/${id}/comments`, { content: newComment });
      setComments((prev) => [...prev, c]);
      setNewComment("");
    } catch (err) {
      setError(getApiErrorMessage(err));
    }
  };

  if (loading)
    return (
      <div className="text-muted-foreground p-6" aria-busy="true">
        Loading...
      </div>
    );
  if (error)
    return (
      <div className="text-destructive p-6" role="alert">
        {error}
      </div>
    );
  if (!request) return <div className="p-6">Request not found.</div>;

  return (
    <div className="mx-auto max-w-4xl p-6">
      <button
        onClick={() => router.back()}
        className="text-muted-foreground mb-3 text-sm hover:underline"
      >
        ← Back
      </button>
      <div className="mb-6 flex items-start justify-between">
        <div>
          <h1 className="text-2xl font-bold">{request.title}</h1>
          <p className="text-muted-foreground mt-1 text-sm">{request.description}</p>
        </div>
        <span className="bg-muted rounded-full px-3 py-1 text-xs font-medium">
          {request.status}
        </span>
      </div>

      {/* Tabs */}
      <div className="mb-4 flex gap-4 border-b">
        {(["info", "comments", "history"] as const).map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={`pb-2 text-sm capitalize ${tab === t ? "border-primary border-b-2 font-medium" : "text-muted-foreground"}`}
            aria-current={tab === t ? "true" : undefined}
          >
            {t}
          </button>
        ))}
      </div>

      {tab === "info" && (
        <dl className="grid gap-4 sm:grid-cols-2">
          <div>
            <dt className="text-muted-foreground text-xs">Priority</dt>
            <dd>{request.priority}</dd>
          </div>
          <div>
            <dt className="text-muted-foreground text-xs">Customer</dt>
            <dd className="font-mono text-sm">{request.customerId}</dd>
          </div>
          <div>
            <dt className="text-muted-foreground text-xs">Created</dt>
            <dd className="text-sm">{new Date(request.createdAt).toLocaleString()}</dd>
          </div>
          {request.slaDeadline && (
            <div>
              <dt className="text-muted-foreground text-xs">SLA Deadline</dt>
              <dd className="text-sm">{new Date(request.slaDeadline).toLocaleString()}</dd>
            </div>
          )}
        </dl>
      )}

      {tab === "comments" && (
        <div className="space-y-4">
          {comments.length === 0 ? (
            <p className="text-muted-foreground text-sm">No comments yet.</p>
          ) : (
            <ul className="space-y-2">
              {comments.map((c) => (
                <li key={c.id} className="rounded border p-3">
                  <p className="text-sm">{c.content}</p>
                  <p className="text-muted-foreground mt-1 text-xs">
                    {new Date(c.createdAt).toLocaleString()}
                  </p>
                </li>
              ))}
            </ul>
          )}
          <div className="flex gap-2 pt-2">
            <input
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              placeholder="Add a comment..."
              className="flex-1 rounded border px-3 py-2 text-sm"
              aria-label="New comment"
            />
            <button
              onClick={handleAddComment}
              disabled={!newComment.trim()}
              className="bg-primary text-primary-foreground rounded px-4 py-2 text-sm disabled:opacity-50"
            >
              Send
            </button>
          </div>
        </div>
      )}

      {tab === "history" && (
        <ol className="relative ml-3 border-l">
          {history.length === 0 ? (
            <p className="text-muted-foreground pl-4 text-sm">No history yet.</p>
          ) : (
            history.map((h, i) => (
              <li key={i} className="mb-4 ml-4">
                <div className="bg-primary absolute -left-1.5 h-3 w-3 rounded-full border" />
                <p className="text-sm font-medium">
                  {h.from} → {h.to}
                </p>
                {h.reason && <p className="text-muted-foreground text-xs">{h.reason}</p>}
                <p className="text-muted-foreground text-xs">
                  {new Date(h.changedAt).toLocaleString()}
                </p>
              </li>
            ))
          )}
        </ol>
      )}
    </div>
  );
}
