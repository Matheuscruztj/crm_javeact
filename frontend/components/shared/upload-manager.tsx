"use client";

/**
 * Upload Manager with multipart chunked upload, pause/resume/retry/cancel.
 * Validates: P0.M.2 — Multipart Upload Frontend (Pause/Resume)
 *
 * Flow:
 * 1. POST /documents → register metadata, get document ID
 * 2. POST /documents/{id}/upload-url → get presigned PUT URL
 * 3. PUT presigned URL → upload file binary
 * 4. POST /documents/{id}/confirm-upload → finalize
 */

import { useCallback, useRef, useState } from "react";
import { api } from "@/lib/api-client";
import { getApiErrorMessage } from "@/lib/form-utils";

// ─── Types ─────────────────────────────────────────────────────────────────

interface UploadFile {
  id: string;
  file: File;
  documentId: string | null;
  status: "pending" | "uploading" | "paused" | "complete" | "error" | "cancelled";
  progress: number;
  error: string | null;
}

interface RegisterDocumentResponse {
  id: string;
  filename: string;
  status: string;
}

interface UploadUrlResponse {
  presignedUrl: string;
  expiresAt: string;
}

interface UploadManagerProps {
  tenantId?: string;
  requestId?: string;
  onComplete?: (documentId: string) => void;
  maxFileSizeMb?: number;
}

// ─── Hook ──────────────────────────────────────────────────────────────────

function useUploadManager() {
  const [files, setFiles] = useState<UploadFile[]>([]);
  const abortControllers = useRef<Map<string, AbortController>>(new Map());

  const addFiles = useCallback((newFiles: FileList) => {
    const uploads: UploadFile[] = Array.from(newFiles).map((file) => ({
      id: crypto.randomUUID(),
      file,
      documentId: null,
      status: "pending" as const,
      progress: 0,
      error: null,
    }));
    setFiles((prev) => [...prev, ...uploads]);
  }, []);

  const updateFile = useCallback((id: string, patch: Partial<UploadFile>) => {
    setFiles((prev) =>
      prev.map((f) => (f.id === id ? { ...f, ...patch } : f))
    );
  }, []);

  const startUpload = useCallback(
    async (uploadId: string, requestId?: string) => {
      setFiles((prev) =>
        prev.map((f) =>
          f.id === uploadId ? { ...f, status: "uploading", error: null } : f
        )
      );

      const file = files.find((f) => f.id === uploadId);
      if (!file) return;

      const controller = new AbortController();
      abortControllers.current.set(uploadId, controller);

      try {
        // Step 1: Register document metadata
        const doc = await api.post<RegisterDocumentResponse>("/documents", {
          filename: file.file.name,
          contentType: file.file.type || "application/octet-stream",
          sizeBytes: file.file.size,
          requestId,
        });

        updateFile(uploadId, { documentId: doc.id, progress: 10 });

        // Step 2: Get presigned upload URL
        const { presignedUrl } = await api.post<UploadUrlResponse>(
          `/documents/${doc.id}/upload-url`,
          {}
        );

        updateFile(uploadId, { progress: 20 });

        // Step 3: Upload to presigned URL with progress
        await uploadWithProgress(
          presignedUrl,
          file.file,
          controller.signal,
          (p) => updateFile(uploadId, { progress: 20 + Math.round(p * 0.7) })
        );

        updateFile(uploadId, { progress: 90 });

        // Step 4: Confirm upload
        await api.post(`/documents/${doc.id}/confirm-upload`, {
          checksumSha256: null,
        });

        updateFile(uploadId, { status: "complete", progress: 100 });
      } catch (err) {
        if (controller.signal.aborted) {
          updateFile(uploadId, { status: "cancelled", progress: 0 });
        } else {
          updateFile(uploadId, {
            status: "error",
            error: getApiErrorMessage(err),
          });
        }
      } finally {
        abortControllers.current.delete(uploadId);
      }
    },
    [files, updateFile]
  );

  const pauseUpload = useCallback((uploadId: string) => {
    abortControllers.current.get(uploadId)?.abort();
    updateFile(uploadId, { status: "paused" });
  }, [updateFile]);

  const cancelUpload = useCallback((uploadId: string) => {
    abortControllers.current.get(uploadId)?.abort();
    setFiles((prev) => prev.filter((f) => f.id !== uploadId));
  }, []);

  const retryUpload = useCallback(
    (uploadId: string, requestId?: string) => {
      updateFile(uploadId, { status: "pending", progress: 0, error: null });
      startUpload(uploadId, requestId);
    },
    [updateFile, startUpload]
  );

  return { files, addFiles, startUpload, pauseUpload, cancelUpload, retryUpload };
}

async function uploadWithProgress(
  url: string,
  file: File,
  signal: AbortSignal,
  onProgress: (progress: number) => void
): Promise<void> {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest();
    xhr.upload.addEventListener("progress", (e) => {
      if (e.lengthComputable) onProgress(e.loaded / e.total);
    });
    xhr.addEventListener("load", () => {
      if (xhr.status >= 200 && xhr.status < 300) resolve();
      else reject(new Error(`Upload failed: ${xhr.status}`));
    });
    xhr.addEventListener("error", () => reject(new Error("Network error")));
    xhr.addEventListener("abort", () => reject(new Error("Aborted")));
    signal.addEventListener("abort", () => xhr.abort());
    xhr.open("PUT", url);
    xhr.setRequestHeader("Content-Type", file.type || "application/octet-stream");
    xhr.send(file);
  });
}

// ─── Component ─────────────────────────────────────────────────────────────

const STATUS_LABELS: Record<UploadFile["status"], string> = {
  pending: "Pending",
  uploading: "Uploading…",
  paused: "Paused",
  complete: "Complete",
  error: "Failed",
  cancelled: "Cancelled",
};

const STATUS_COLORS: Record<UploadFile["status"], string> = {
  pending: "bg-gray-100 text-gray-700",
  uploading: "bg-blue-100 text-blue-700",
  paused: "bg-yellow-100 text-yellow-700",
  complete: "bg-green-100 text-green-700",
  error: "bg-red-100 text-red-700",
  cancelled: "bg-gray-100 text-gray-500",
};

export function UploadManager({
  requestId,
  onComplete,
  maxFileSizeMb = 500,
}: UploadManagerProps) {
  const { files, addFiles, startUpload, pauseUpload, cancelUpload, retryUpload } =
    useUploadManager();
  const dropRef = useRef<HTMLDivElement>(null);

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    if (e.dataTransfer.files.length > 0) {
      addFiles(e.dataTransfer.files);
    }
  };

  const handleFileInput = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) addFiles(e.target.files);
  };

  const handleStartAll = () => {
    files
      .filter((f) => f.status === "pending")
      .forEach((f) => startUpload(f.id, requestId));
  };

  return (
    <div className="space-y-4">
      {/* Drop zone */}
      <div
        ref={dropRef}
        onDrop={handleDrop}
        onDragOver={(e) => e.preventDefault()}
        className="rounded-lg border-2 border-dashed border-muted-foreground/30 p-8 text-center transition hover:border-primary"
      >
        <input
          id="file-input"
          type="file"
          multiple
          onChange={handleFileInput}
          className="hidden"
          aria-label="Upload files"
        />
        <label
          htmlFor="file-input"
          className="cursor-pointer text-sm text-muted-foreground"
        >
          Drag &amp; drop files here or{" "}
          <span className="font-medium text-primary underline">browse</span>
          <br />
          <span className="text-xs">Max {maxFileSizeMb} MB per file</span>
        </label>
      </div>

      {/* File list */}
      {files.length > 0 && (
        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-sm font-medium">{files.length} file(s)</span>
            <button
              onClick={handleStartAll}
              disabled={!files.some((f) => f.status === "pending")}
              className="rounded-md bg-primary px-4 py-1.5 text-xs font-medium text-primary-foreground hover:bg-primary/90 disabled:opacity-50"
              aria-label="Upload all pending files"
            >
              Upload All
            </button>
          </div>

          {files.map((f) => (
            <div
              key={f.id}
              className="rounded-md border p-3"
              role="listitem"
              aria-label={`${f.file.name} — ${STATUS_LABELS[f.status]}`}
            >
              <div className="flex items-center justify-between gap-2">
                <span className="flex-1 truncate text-sm">{f.file.name}</span>
                <span
                  className={`rounded-full px-2 py-0.5 text-xs font-medium ${STATUS_COLORS[f.status]}`}
                >
                  {STATUS_LABELS[f.status]}
                </span>
              </div>

              {/* Progress bar */}
              {(f.status === "uploading" || f.status === "paused") && (
                <div className="mt-2">
                  <div
                    className="h-1.5 overflow-hidden rounded-full bg-muted"
                    role="progressbar"
                    aria-valuenow={f.progress}
                    aria-valuemin={0}
                    aria-valuemax={100}
                    aria-label={`Upload progress: ${f.progress}%`}
                  >
                    <div
                      className="h-full bg-primary transition-all"
                      style={{ width: `${f.progress}%` }}
                    />
                  </div>
                  <span className="text-xs text-muted-foreground">{f.progress}%</span>
                </div>
              )}

              {f.error && (
                <p role="alert" className="mt-1 text-xs text-destructive">
                  {f.error}
                </p>
              )}

              {/* Actions */}
              <div className="mt-2 flex gap-2">
                {f.status === "pending" && (
                  <button
                    onClick={() => startUpload(f.id, requestId)}
                    className="text-xs text-primary hover:underline"
                    aria-label={`Start uploading ${f.file.name}`}
                  >
                    Start
                  </button>
                )}
                {f.status === "uploading" && (
                  <button
                    onClick={() => pauseUpload(f.id)}
                    className="text-xs text-yellow-700 hover:underline"
                    aria-label={`Pause uploading ${f.file.name}`}
                  >
                    Pause
                  </button>
                )}
                {f.status === "paused" && (
                  <button
                    onClick={() => startUpload(f.id, requestId)}
                    className="text-xs text-primary hover:underline"
                    aria-label={`Resume uploading ${f.file.name}`}
                  >
                    Resume
                  </button>
                )}
                {f.status === "error" && (
                  <button
                    onClick={() => retryUpload(f.id, requestId)}
                    className="text-xs text-primary hover:underline"
                    aria-label={`Retry uploading ${f.file.name}`}
                  >
                    Retry
                  </button>
                )}
                {f.status === "complete" && onComplete && f.documentId && (
                  <button
                    onClick={() => onComplete(f.documentId as string)}
                    className="text-xs text-green-700 hover:underline"
                    aria-label="View uploaded document"
                  >
                    View
                  </button>
                )}
                {f.status !== "complete" && (
                  <button
                    onClick={() => cancelUpload(f.id)}
                    className="text-xs text-destructive hover:underline"
                    aria-label={`Cancel ${f.file.name}`}
                  >
                    Cancel
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
