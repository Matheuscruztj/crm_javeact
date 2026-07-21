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
 * Portal request detail page for CLIENT users.
 * Shows description, status timeline, comments thread, and add comment form.
 * Validates: P1.15.3 — /portal/requests/[id] detail (task 43)
 */
export default function PortalRequestDetailPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();

  const [request, setRequest] = useState<Request | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [history, setHistory] = useState<StatusHistory[]>([]);
  const [newComment, setNewComment] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [tab, setTab] = useState<"overview" | "comments" | "timeline">("overview");

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

  if (loading) return <div className="p-4 text-muted-foreground" aria-busy="true">Loading...</div>;
  if (error) return <div className="p-4 text-destructive" role="alert">{error}</div>;
  if (!request) return <div className="p-4">Request not found.</div>;

  return (
    <div className="p-4 max-w-2xl mx-auto">
      <button onClick={() => router.back()} className="mb-3 text-sm text-muted-foreground hover:underline">← My Requests</button>

      <div className="mb-4">
        <h1 className="text-xl font-bold">{request.title}</h1>
        <span className="inline-block mt-1 rounded-full bg-muted px-3 py-0.5 text-xs">{request.status}</span>
      </div>

      <div className="mb-4 flex gap-3 border-b">
        {(["overview", "comments", "timeline"] as const).map((t) => (
          <button key={t} onClick={() => setTab(t)}
            className={`pb-2 text-sm capitalize ${tab === t ? "border-b-2 border-primary font-medium" : "text-muted-foreground"}`}
            aria-current={tab === t ? "true" : undefined}
          >
            {t}
          </button>
        ))}
      </div>

      {tab === "overview" && (
        <div className="space-y-3">
          <p className="text-sm">{request.description}</p>
          <dl className="grid gap-2 sm:grid-cols-2 text-sm">
            <div><dt className="text-xs text-muted-foreground">Priority</dt><dd>{request.priority}</dd></div>
            <div><dt className="text-xs text-muted-foreground">Created</dt><dd>{new Date(request.createdAt).toLocaleDateString()}</dd></div>
            {request.slaDeadline && <div><dt className="text-xs text-muted-foreground">Deadline</dt><dd>{new Date(request.slaDeadline).toLocaleDateString()}</dd></div>}
          </dl>
        </div>
      )}

      {tab === "comments" && (
        <div className="space-y-3">
          {comments.length === 0 ? <p className="text-sm text-muted-foreground">No comments yet.</p> : (
            <ul className="space-y-2">
              {comments.map((c) => (
                <li key={c.id} className="rounded border p-3">
                  <p className="text-sm">{c.content}</p>
                  <p className="mt-1 text-xs text-muted-foreground">{new Date(c.createdAt).toLocaleString()}</p>
                </li>
              ))}
            </ul>
          )}
          <div className="flex gap-2 pt-2">
            <input value={newComment} onChange={(e) => setNewComment(e.target.value)}
              placeholder="Write a comment..." className="flex-1 rounded border px-3 py-2 text-sm"
              aria-label="New comment" />
            <button onClick={handleAddComment} disabled={!newComment.trim()}
              className="rounded bg-primary px-4 py-2 text-sm text-primary-foreground disabled:opacity-50">Send</button>
          </div>
        </div>
      )}

      {tab === "timeline" && (
        <ol className="relative ml-3 border-l space-y-4">
          {history.length === 0 ? <p className="pl-4 text-sm text-muted-foreground">No history yet.</p> : history.map((h, i) => (
            <li key={i} className="ml-4">
              <div className="absolute -left-1.5 h-3 w-3 rounded-full border bg-primary" />
              <p className="text-sm font-medium">{h.from} → {h.to}</p>
              {h.reason && <p className="text-xs text-muted-foreground">{h.reason}</p>}
              <p className="text-xs text-muted-foreground">{new Date(h.changedAt).toLocaleString()}</p>
            </li>
          ))}
        </ol>
      )}
    </div>
  );
}
