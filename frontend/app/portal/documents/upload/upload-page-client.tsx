"use client";

/**
 * Portal Document Upload Page with drag-and-drop.
 * Task 23.3: Upload page with drag-and-drop, file type validation, progress indicator, cancel/retry.
 * Requirements: 23.6
 */

import { useCallback, useRef, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { getAccessToken, getTenantId } from "@/lib/api-client";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080/api/v1";

const ALLOWED_FILE_TYPES = [
  "application/pdf",
  "application/msword",
  "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
  "image/png",
  "image/jpeg",
  "text/plain",
];

const MAX_FILE_SIZE = 10 * 1024 * 1024;

interface UploadFile {
  id: string;
  file: File;
  progress: number;
  status: "pending" | "uploading" | "completed" | "error";
  error?: string;
  abortController?: AbortController;
}

function formatFileSize(bytes: number): string {
  if (bytes === 0) return "0 B";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(1))} ${sizes[i]}`;
}

function validateFile(file: File): string | null {
  if (!ALLOWED_FILE_TYPES.includes(file.type)) {
    return `Tipo de arquivo não suportado: ${file.type || "desconhecido"}`;
  }
  if (file.size > MAX_FILE_SIZE) {
    return `Arquivo muito grande: ${formatFileSize(file.size)} (máximo: ${formatFileSize(MAX_FILE_SIZE)})`;
  }
  return null;
}

export default function PortalDocumentUploadPageClient() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const requestId = searchParams.get("requestId");
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [files, setFiles] = useState<UploadFile[]>([]);
  const [isDragging, setIsDragging] = useState(false);

  const addFiles = useCallback((newFiles: FileList | null) => {
    if (!newFiles) return;
    const uploadFiles: UploadFile[] = Array.from(newFiles).map((file) => {
      const error = validateFile(file);
      return {
        id: `${file.name}-${Date.now()}-${Math.random().toString(36).slice(2)}`,
        file,
        progress: 0,
        status: error ? "error" : "pending",
        error: error ?? undefined,
      };
    });
    setFiles((prev) => [...prev, ...uploadFiles]);
  }, []);

  const removeFile = useCallback((id: string) => {
    setFiles((prev) => {
      const file = prev.find((f) => f.id === id);
      file?.abortController?.abort();
      return prev.filter((f) => f.id !== id);
    });
  }, []);

  const uploadFile = useCallback(
    async (uploadFile: UploadFile) => {
      const abortController = new AbortController();
      setFiles((prev) =>
        prev.map((f) =>
          f.id === uploadFile.id ? { ...f, status: "uploading", abortController } : f
        )
      );

      const formData = new FormData();
      formData.append("file", uploadFile.file);
      if (requestId) formData.append("requestId", requestId);

      try {
        const response = await fetch(`${API_BASE_URL}/documents/upload`, {
          method: "POST",
          headers: {
            Authorization: `Bearer ${getAccessToken()}`,
            "X-Tenant-ID": getTenantId() ?? "",
          },
          body: formData,
          signal: abortController.signal,
        });

        if (!response.ok) {
          const error = await response.json();
          throw new Error(error.detail || "Upload failed");
        }

        setFiles((prev) =>
          prev.map((f) =>
            f.id === uploadFile.id ? { ...f, progress: 100, status: "completed" } : f
          )
        );
      } catch (err) {
        if (err instanceof Error && err.name === "AbortError") {
          setFiles((prev) => prev.filter((f) => f.id !== uploadFile.id));
          return;
        }
        setFiles((prev) =>
          prev.map((f) =>
            f.id === uploadFile.id
              ? {
                  ...f,
                  status: "error",
                  error: err instanceof Error ? err.message : "Upload failed",
                }
              : f
          )
        );
      }
    },
    [requestId]
  );

  const uploadAll = useCallback(async () => {
    const pendingFiles = files.filter((f) => f.status === "pending");
    for (const file of pendingFiles) await uploadFile(file);
  }, [files, uploadFile]);

  const retryFile = useCallback(
    (id: string) => {
      setFiles((prev) =>
        prev.map((f) => (f.id === id ? { ...f, status: "pending", error: undefined } : f))
      );
      const file = files.find((f) => f.id === id);
      if (file) uploadFile({ ...file, status: "pending" });
    },
    [files, uploadFile]
  );

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(true);
  }, []);

  const handleDragLeave = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
  }, []);

  const handleDrop = useCallback(
    (e: React.DragEvent) => {
      e.preventDefault();
      setIsDragging(false);
      addFiles(e.dataTransfer.files);
    },
    [addFiles]
  );

  const handleFileInputChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      addFiles(e.target.files);
      if (fileInputRef.current) fileInputRef.current.value = "";
    },
    [addFiles]
  );

  const pendingCount = files.filter((f) => f.status === "pending").length;
  const completedCount = files.filter((f) => f.status === "completed").length;
  const hasErrors = files.some((f) => f.status === "error");

  return (
    <div className="p-6">
      <div className="mb-6">
        <Button variant="ghost" size="sm" onClick={() => router.back()} className="mb-4">
          <svg className="mr-2 h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M15 19l-7-7 7-7"
            />
          </svg>
          Back
        </Button>
        <Card>
          <CardHeader>
            <CardTitle>Upload documents</CardTitle>
            <CardDescription>Drag and drop files or choose them manually.</CardDescription>
          </CardHeader>
          <CardContent>
            <div
              onDragOver={handleDragOver}
              onDragLeave={handleDragLeave}
              onDrop={handleDrop}
              className={`rounded-lg border-2 border-dashed p-8 text-center ${isDragging ? "border-primary bg-primary/5" : "border-muted"}`}
            >
              <input
                ref={fileInputRef}
                type="file"
                multiple
                onChange={handleFileInputChange}
                className="hidden"
              />
              <p className="text-muted-foreground mb-2 text-sm">Drop files here</p>
              <Button type="button" onClick={() => fileInputRef.current?.click()}>
                Choose files
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
      <div className="flex gap-2">
        <Button onClick={uploadAll} disabled={pendingCount === 0}>
          Upload all
        </Button>
        <Link href="/portal/documents">Cancel</Link>
      </div>
      <div className="text-muted-foreground mt-4 text-sm">
        Pending: {pendingCount} Completed: {completedCount} {hasErrors ? "Errors present" : ""}
      </div>
      <div className="mt-4 space-y-2">
        {files.map((file) => (
          <div key={file.id} className="rounded border p-3">
            <div className="flex items-center justify-between">
              <span>{file.file.name}</span>
              <Button variant="ghost" size="sm" onClick={() => removeFile(file.id)}>
                Remove
              </Button>
            </div>
            {file.error && <p className="text-destructive text-sm">{file.error}</p>}
            {file.status === "error" && (
              <Button variant="outline" size="sm" onClick={() => retryFile(file.id)}>
                Retry
              </Button>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
