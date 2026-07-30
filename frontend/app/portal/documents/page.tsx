/**
 * Portal Documents Page with listing and upload link.
 * Task 23.3: Implement portal documents page with upload component.
 * - Listing documents with status and upload date
 * Requirements: 23.5
 */

"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { api, type PageResponse } from "@/lib/api-client";

interface Document {
  id: string;
  fileName: string;
  fileSize: number;
  contentType: string;
  status:
    "UPLOADED" | "TEXT_EXTRACTED" | "ANALYZED" | "APPROVED" | "REJECTED" | "PROCESSING_FAILED";
  uploadedAt: string;
  requestId?: string;
  requestTitle?: string;
}

function getStatusBadgeClass(status: Document["status"]): string {
  switch (status) {
    case "UPLOADED":
      return "bg-gray-100 text-gray-800 border-gray-200";
    case "TEXT_EXTRACTED":
      return "bg-blue-100 text-blue-800 border-blue-200";
    case "ANALYZED":
      return "bg-yellow-100 text-yellow-800 border-yellow-200";
    case "APPROVED":
      return "bg-green-100 text-green-800 border-green-200";
    case "REJECTED":
      return "bg-red-100 text-red-800 border-red-200";
    case "PROCESSING_FAILED":
      return "bg-red-100 text-red-800 border-red-200";
    default:
      return "";
  }
}

function getStatusLabel(status: Document["status"]): string {
  switch (status) {
    case "UPLOADED":
      return "Enviado";
    case "TEXT_EXTRACTED":
      return "Processando";
    case "ANALYZED":
      return "Analisado";
    case "APPROVED":
      return "Aprovado";
    case "REJECTED":
      return "Rejeitado";
    case "PROCESSING_FAILED":
      return "Falhou";
    default:
      return status;
  }
}

function getFileIcon(contentType: string): React.ReactNode {
  if (contentType.includes("pdf")) {
    return (
      <svg
        className="h-8 w-8 text-red-500"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
        aria-hidden="true"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M7 21h10a2 2 0 002-2V9.414a1 1 0 00-.293-.707l-5.414-5.414A1 1 0 0012.586 3H7a2 2 0 00-2 2v14a2 2 0 002 2z"
        />
      </svg>
    );
  }
  if (contentType.includes("image")) {
    return (
      <svg
        className="h-8 w-8 text-green-500"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
        aria-hidden="true"
      >
        <path
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeWidth={2}
          d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
        />
      </svg>
    );
  }
  if (contentType.includes("word") || contentType.includes("document")) {
    return (
      <svg
        className="h-8 w-8 text-blue-500"
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
    );
  }
  return (
    <svg
      className="h-8 w-8 text-gray-500"
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
  );
}

function formatFileSize(bytes: number): string {
  if (bytes === 0) return "0 B";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + " " + sizes[i];
}

function DocumentCard({ document }: { document: Document }) {
  return (
    <Card className="hover:bg-accent/50 transition-colors">
      <CardContent className="p-4">
        <div className="flex items-start gap-4">
          <div className="flex-shrink-0">{getFileIcon(document.contentType)}</div>
          <div className="min-w-0 flex-1">
            <div className="flex items-start justify-between gap-2">
              <div>
                <h3 className="truncate font-medium">{document.fileName}</h3>
                <p className="text-muted-foreground text-sm">
                  {formatFileSize(document.fileSize)} •{" "}
                  {new Date(document.uploadedAt).toLocaleDateString("pt-BR")}
                </p>
              </div>
              <Badge className={getStatusBadgeClass(document.status)}>
                {getStatusLabel(document.status)}
              </Badge>
            </div>
            {document.requestTitle && (
              <Link
                href={`/portal/requests/${document.requestId}`}
                className="text-primary mt-2 inline-flex items-center gap-1 text-xs hover:underline"
              >
                <svg
                  className="h-3 w-3"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                  aria-hidden="true"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"
                  />
                </svg>
                {document.requestTitle}
              </Link>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  );
}

function Pagination({
  page,
  totalPages,
  onPageChange,
}: {
  page: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}) {
  return (
    <div className="flex items-center justify-between">
      <p className="text-muted-foreground text-sm">
        Página {page + 1} de {totalPages || 1}
      </p>
      <div className="flex gap-2">
        <Button
          variant="outline"
          size="sm"
          onClick={() => onPageChange(page - 1)}
          disabled={page === 0}
        >
          Anterior
        </Button>
        <Button
          variant="outline"
          size="sm"
          onClick={() => onPageChange(page + 1)}
          disabled={page >= totalPages - 1}
        >
          Próxima
        </Button>
      </div>
    </div>
  );
}

export default function PortalDocumentsPage() {
  const [documents, setDocuments] = useState<Document[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [statusFilter, setStatusFilter] = useState<string>("ALL");

  const fetchDocuments = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    try {
      const params = new URLSearchParams({
        page: String(page),
        size: "12",
        sort: "uploadedAt,desc",
      });

      if (statusFilter !== "ALL") {
        params.append("status", statusFilter);
      }

      const response = await api.get<PageResponse<Document>>(`/documents/my?${params.toString()}`);

      setDocuments(response.content);
      setTotalPages(response.page.totalPages);
      setTotalElements(response.page.totalElements);
    } catch {
      setError("Falha ao carregar documentos");
    } finally {
      setIsLoading(false);
    }
  }, [page, statusFilter]);

  useEffect(() => {
    fetchDocuments();
  }, [fetchDocuments]);

  return (
    <div className="p-6">
      <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold">Meus Documentos</h1>
          <p className="text-muted-foreground">
            Visualize e gerencie seus documentos enviados.
            {totalElements > 0 && ` (${totalElements} total)`}
          </p>
        </div>

        <Link href="/portal/documents/upload">
          <Button>
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
                d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-8l-4-4m0 0L8 8m4-4v12"
              />
            </svg>
            Enviar Documento
          </Button>
        </Link>
      </div>

      {/* Filter */}
      <div className="mb-4">
        <select
          value={statusFilter}
          onChange={(e) => {
            setStatusFilter(e.target.value);
            setPage(0);
          }}
          className="border-input bg-background ring-offset-background focus:ring-ring rounded-md border px-3 py-2 text-sm focus:ring-2 focus:outline-none"
          aria-label="Filtrar por status"
        >
          <option value="ALL">Todos os Status</option>
          <option value="UPLOADED">Enviado</option>
          <option value="TEXT_EXTRACTED">Processando</option>
          <option value="ANALYZED">Analisado</option>
          <option value="APPROVED">Aprovado</option>
          <option value="REJECTED">Rejeitado</option>
          <option value="PROCESSING_FAILED">Falhou</option>
        </select>
      </div>

      {error && (
        <div
          className="bg-destructive/10 text-destructive mb-4 rounded-md p-4 text-sm"
          role="alert"
        >
          {error}
        </div>
      )}

      {/* Documents Grid */}
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {isLoading ? (
          Array.from({ length: 6 }).map((_, i) => (
            <Card key={i}>
              <CardContent className="p-4">
                <div className="flex items-start gap-4">
                  <Skeleton className="h-8 w-8" />
                  <div className="flex-1">
                    <Skeleton className="mb-2 h-5 w-32" />
                    <Skeleton className="h-4 w-24" />
                  </div>
                </div>
              </CardContent>
            </Card>
          ))
        ) : documents.length === 0 ? (
          <div className="col-span-full">
            <Card>
              <CardContent className="py-8 text-center">
                <svg
                  className="text-muted-foreground mx-auto mb-4 h-12 w-12"
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
                <p className="text-muted-foreground mb-4">Nenhum documento encontrado.</p>
                <Link href="/portal/documents/upload">
                  <Button>Enviar primeiro documento</Button>
                </Link>
              </CardContent>
            </Card>
          </div>
        ) : (
          documents.map((doc) => <DocumentCard key={doc.id} document={doc} />)
        )}
      </div>

      {/* Pagination */}
      {!isLoading && documents.length > 0 && (
        <div className="mt-4">
          <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </div>
      )}
    </div>
  );
}
