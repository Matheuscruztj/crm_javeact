/**
 * Portal Document Upload Page with drag-and-drop.
 * Task 23.3: Upload page with drag-and-drop, file type validation, progress indicator, cancel/retry.
 * Requirements: 23.6
 */

"use client";

import { useCallback, useState, useRef } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { getAccessToken, getTenantId } from "@/lib/api-client";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080/api/v1";

const ALLOWED_FILE_TYPES = [
  "application/pdf",
  "application/msword",
  "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
  "image/png",
  "image/jpeg",
  "text/plain",
];

const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

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
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + " " + sizes[i];
}

function getFileTypeLabel(mimeType: string): string {
  if (mimeType.includes("pdf")) return "PDF";
  if (mimeType.includes("word") || mimeType.includes("document")) return "Word";
  if (mimeType.includes("image")) return "Imagem";
  if (mimeType.includes("text/plain")) return "Texto";
  return "Arquivo";
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

export default function PortalDocumentUploadPage() {
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
      if (file?.abortController) {
        file.abortController.abort();
      }
      return prev.filter((f) => f.id !== id);
    });
  }, []);

  const uploadFile = useCallback(
    async (uploadFile: UploadFile) => {
      const abortController = new AbortController();

      setFiles((prev) =>
        prev.map((f) =>
          f.id === uploadFile.id
            ? { ...f, status: "uploading" as const, abortController }
            : f,
        ),
      );

      const formData = new FormData();
      formData.append("file", uploadFile.file);
      if (requestId) {
        formData.append("requestId", requestId);
      }

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
            f.id === uploadFile.id
              ? { ...f, progress: 100, status: "completed" as const }
              : f,
          ),
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
                  status: "error" as const,
                  error: err instanceof Error ? err.message : "Upload failed",
                }
              : f,
          ),
        );
      }
    },
    [requestId],
  );

  const uploadAll = useCallback(async () => {
    const pendingFiles = files.filter((f) => f.status === "pending");
    for (const file of pendingFiles) {
      await uploadFile(file);
    }
  }, [files, uploadFile]);

  const retryFile = useCallback(
    (id: string) => {
      setFiles((prev) =>
        prev.map((f) =>
          f.id === id
            ? { ...f, status: "pending" as const, error: undefined }
            : f,
        ),
      );
      const file = files.find((f) => f.id === id);
      if (file) {
        uploadFile({ ...file, status: "pending" });
      }
    },
    [files, uploadFile],
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
    [addFiles],
  );

  const handleFileInputChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      addFiles(e.target.files);
      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }
    },
    [addFiles],
  );

  const pendingCount = files.filter((f) => f.status === "pending").length;
  const completedCount = files.filter((f) => f.status === "completed").length;
  const hasErrors = files.some((f) => f.status === "error");

  return (
    <div className="p-6">
      <div className="mb-6">
        <Button
          variant="ghost"
          size="sm"
          onClick={() => router.back()}
          className="mb-4"
        >
          <svg
            className="mr-2 h-4 w-4"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            aria-hidden="true"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M15 19l-7-7 7-7"
            />
          </svg>
          Voltar
        </Button>

        <h1 className="text-2xl font-bold">Enviar Documento</h1>
        <p className="text-muted-foreground">
          Faça upload de novos documentos para análise.
          {requestId && " O documento será anexado à solicitação."}
        </p>
      </div>

      {/* Upload Zone */}
      <Card className="mb-6">
        <CardContent className="p-0">
          <div
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            onClick={() => fileInputRef.current?.click()}
            className={`flex flex-col items-center justify-center border-2 border-dashed rounded-lg p-12 cursor-pointer transition-colors ${
              isDragging
                ? "border-primary bg-primary/5"
                : "border-muted-foreground/25 hover:border-primary/50"
            }`}
          >
            <svg
              className="h-12 w-12 text-muted-foreground mb-4"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              aria-hidden="true"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12"
              />
            </svg>
            <p className="text-lg font-medium mb-1">
              {isDragging
                ? "Solte o arquivo aqui"
                : "Arraste e solte arquivos aqui"}
            </p>
            <p className="text-sm text-muted-foreground mb-4">
              ou clique para selecionar
            </p>
            <p className="text-xs text-muted-foreground">
              PDF, Word, imagens (PNG, JPEG) ou texto. Máximo 10MB.
            </p>
          </div>
          <input
            ref={fileInputRef}
            type="file"
            accept={ALLOWED_FILE_TYPES.join(",")}
            multiple
            onChange={handleFileInputChange}
            className="hidden"
          />
        </CardContent>
      </Card>

      {/* Files List */}
      {files.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="text-lg">
              Arquivos ({files.length})
              {completedCount > 0 && (
                <span className="text-sm font-normal text-muted-foreground ml-2">
                  — {completedCount} enviado(s)
                </span>
              )}
            </CardTitle>
            <CardDescription>
              {pendingCount > 0 &&
                `${pendingCount} arquivo(s) aguardando envio`}
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {files.map((uploadFile) => (
                <div
                  key={uploadFile.id}
                  className="flex items-center gap-4 rounded-lg border p-3"
                >
                  <div className="flex-shrink-0">
                    {uploadFile.status === "completed" ? (
                      <div className="flex h-10 w-10 items-center justify-center rounded-full bg-green-100">
                        <svg
                          className="h-5 w-5 text-green-600"
                          fill="none"
                          viewBox="0 0 24 24"
                          stroke="currentColor"
                          aria-hidden="true"
                        >
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M5 13l4 4L19 7"
                          />
                        </svg>
                      </div>
                    ) : uploadFile.status === "error" ? (
                      <div className="flex h-10 w-10 items-center justify-center rounded-full bg-red-100">
                        <svg
                          className="h-5 w-5 text-red-600"
                          fill="none"
                          viewBox="0 0 24 24"
                          stroke="currentColor"
                          aria-hidden="true"
                        >
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M6 18L18 6M6 6l12 12"
                          />
                        </svg>
                      </div>
                    ) : uploadFile.status === "uploading" ? (
                      <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10">
                        <svg
                          className="h-5 w-5 text-primary animate-spin"
                          fill="none"
                          viewBox="0 0 24 24"
                        >
                          <circle
                            className="opacity-25"
                            cx="12"
                            cy="12"
                            r="10"
                            stroke="currentColor"
                            strokeWidth="4"
                          />
                          <path
                            className="opacity-75"
                            fill="currentColor"
                            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                          />
                        </svg>
                      </div>
                    ) : (
                      <div className="flex h-10 w-10 items-center justify-center rounded-full bg-muted">
                        <svg
                          className="h-5 w-5 text-muted-foreground"
                          fill="none"
                          viewBox="0 0 24 24"
                          stroke="currentColor"
                          aria-hidden="true"
                        >
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
                          />
                        </svg>
                      </div>
                    )}
                  </div>

                  <div className="flex-1 min-w-0">
                    <p className="font-medium truncate">
                      {uploadFile.file.name}
                    </p>
                    <p className="text-sm text-muted-foreground">
                      {getFileTypeLabel(uploadFile.file.type)} •{" "}
                      {formatFileSize(uploadFile.file.size)}
                    </p>
                    {uploadFile.error && (
                      <p className="text-sm text-destructive">
                        {uploadFile.error}
                      </p>
                    )}
                  </div>

                  <div className="flex-shrink-0 flex gap-2">
                    {uploadFile.status === "error" && (
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => retryFile(uploadFile.id)}
                      >
                        Tentar novamente
                      </Button>
                    )}
                    {(uploadFile.status === "pending" ||
                      uploadFile.status === "error") && (
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => removeFile(uploadFile.id)}
                      >
                        <svg
                          className="h-4 w-4"
                          fill="none"
                          viewBox="0 0 24 24"
                          stroke="currentColor"
                          aria-hidden="true"
                        >
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth={2}
                            d="M6 18L18 6M6 6l12 12"
                          />
                        </svg>
                      </Button>
                    )}
                    {uploadFile.status === "uploading" && (
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => removeFile(uploadFile.id)}
                      >
                        Cancelar
                      </Button>
                    )}
                  </div>
                </div>
              ))}
            </div>

            {pendingCount > 0 && (
              <div className="mt-4 flex justify-end gap-2">
                <Button variant="outline" onClick={() => setFiles([])}>
                  Limpar tudo
                </Button>
                <Button onClick={uploadAll}>
                  Enviar {pendingCount} arquivo(s)
                </Button>
              </div>
            )}

            {completedCount > 0 && pendingCount === 0 && !hasErrors && (
              <div className="mt-4 flex justify-end gap-2">
                <Button variant="outline" onClick={() => setFiles([])}>
                  Enviar mais
                </Button>
                <Link href="/portal/documents">
                  <Button>Ver meus documentos</Button>
                </Link>
              </div>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  );
}
