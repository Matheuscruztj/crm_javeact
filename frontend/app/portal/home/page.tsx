/**
 * Portal Home Page with summary cards.
 * Task 23.1: Implement portal home page with summary cards.
 * - Open requests count
 * - Recent documents (last 5)
 * - Unread notifications count
 * Requirements: 23.1
 */

"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { api } from "@/lib/api-client";

interface PortalSummary {
  openRequestsCount: number;
  recentDocuments: RecentDocument[];
  unreadNotificationsCount: number;
}

interface RecentDocument {
  id: string;
  fileName: string;
  status:
    "UPLOADED" | "TEXT_EXTRACTED" | "ANALYZED" | "APPROVED" | "REJECTED" | "PROCESSING_FAILED";
  uploadedAt: string;
}

function getDocumentStatusBadgeClass(status: RecentDocument["status"]): string {
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

function getStatusLabel(status: RecentDocument["status"]): string {
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

interface StatCardProps {
  title: string;
  value: number;
  description: string;
  icon: React.ReactNode;
  href: string;
  isLoading?: boolean;
}

function StatCard({ title, value, description, icon, href, isLoading }: StatCardProps) {
  return (
    <Link href={href}>
      <Card className="hover:bg-accent/50 transition-colors">
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-sm font-medium">{title}</CardTitle>
          <div className="text-muted-foreground h-5 w-5">{icon}</div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <Skeleton className="h-8 w-16" />
          ) : (
            <div className="text-2xl font-bold">{value}</div>
          )}
          <p className="text-muted-foreground text-xs">{description}</p>
        </CardContent>
      </Card>
    </Link>
  );
}

function RecentDocumentsCard({
  documents,
  isLoading,
}: {
  documents: RecentDocument[];
  isLoading: boolean;
}) {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-lg">Documentos Recentes</CardTitle>
        <CardDescription>Últimos documentos enviados</CardDescription>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <div className="space-y-3">
            {Array.from({ length: 5 }).map((_, i) => (
              <div key={i} className="flex items-center justify-between">
                <Skeleton className="h-4 w-48" />
                <Skeleton className="h-6 w-20" />
              </div>
            ))}
          </div>
        ) : documents.length === 0 ? (
          <div className="text-muted-foreground py-4 text-center text-sm">
            Nenhum documento enviado ainda.
            <Link href="/portal/documents/upload" className="text-primary ml-1 hover:underline">
              Enviar primeiro documento
            </Link>
          </div>
        ) : (
          <div className="space-y-3">
            {documents.map((doc) => (
              <Link
                key={doc.id}
                href={`/portal/documents/${doc.id}`}
                className="hover:bg-accent/50 flex items-center justify-between rounded-md p-2 transition-colors"
              >
                <div className="flex items-center gap-3">
                  <svg
                    className="text-muted-foreground h-5 w-5"
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
                  <div>
                    <p className="max-w-[200px] truncate text-sm font-medium">{doc.fileName}</p>
                    <p className="text-muted-foreground text-xs">
                      {new Date(doc.uploadedAt).toLocaleDateString("pt-BR")}
                    </p>
                  </div>
                </div>
                <Badge className={getDocumentStatusBadgeClass(doc.status)}>
                  {getStatusLabel(doc.status)}
                </Badge>
              </Link>
            ))}
          </div>
        )}
        {documents.length > 0 && (
          <div className="mt-4 border-t pt-4">
            <Link href="/portal/documents" className="text-primary text-sm hover:underline">
              Ver todos os documentos →
            </Link>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

export default function PortalHomePage() {
  const [summary, setSummary] = useState<PortalSummary | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function fetchSummary() {
      setIsLoading(true);
      setError(null);

      try {
        // Fetch all summary data in parallel
        const [requestsRes, documentsRes, notificationsRes] = await Promise.allSettled([
          api.get<{ openCount: number }>("/requests/my/stats"),
          api.get<{ content: RecentDocument[] }>("/documents/my?size=5&sort=uploadedAt,desc"),
          api.get<{ unreadCount: number }>("/notifications/unread-count"),
        ]);

        const openRequestsCount =
          requestsRes.status === "fulfilled" ? requestsRes.value.openCount : 0;
        const recentDocuments =
          documentsRes.status === "fulfilled" ? documentsRes.value.content : [];
        const unreadNotificationsCount =
          notificationsRes.status === "fulfilled" ? notificationsRes.value.unreadCount : 0;

        setSummary({
          openRequestsCount,
          recentDocuments,
          unreadNotificationsCount,
        });
      } catch {
        setError("Falha ao carregar dados do portal");
      } finally {
        setIsLoading(false);
      }
    }

    fetchSummary();
  }, []);

  return (
    <div className="p-6">
      <div className="mb-6">
        <h1 className="text-2xl font-bold">Início</h1>
        <p className="text-muted-foreground">Bem-vindo ao portal do cliente AtlasOps.</p>
      </div>

      {error && (
        <div
          className="bg-destructive/10 text-destructive mb-6 rounded-md p-4 text-sm"
          role="alert"
        >
          {error}
        </div>
      )}

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        <StatCard
          title="Solicitações Abertas"
          value={summary?.openRequestsCount ?? 0}
          description="Solicitações em andamento"
          href="/portal/requests"
          isLoading={isLoading}
          icon={
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
              />
            </svg>
          }
        />

        <StatCard
          title="Notificações"
          value={summary?.unreadNotificationsCount ?? 0}
          description="Notificações não lidas"
          href="/portal/notifications"
          isLoading={isLoading}
          icon={
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
              />
            </svg>
          }
        />

        <StatCard
          title="Documentos"
          value={summary?.recentDocuments.length ?? 0}
          description="Últimos enviados"
          href="/portal/documents"
          isLoading={isLoading}
          icon={
            <svg fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"
              />
            </svg>
          }
        />
      </div>

      <div className="mt-6">
        <RecentDocumentsCard documents={summary?.recentDocuments ?? []} isLoading={isLoading} />
      </div>

      {/* Quick Actions */}
      <div className="mt-6">
        <h2 className="mb-4 text-lg font-semibold">Ações Rápidas</h2>
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <Link href="/portal/requests/new">
            <Card className="hover:bg-accent/50 cursor-pointer transition-colors">
              <CardContent className="flex items-center gap-4 pt-6">
                <div className="bg-primary/10 rounded-full p-3">
                  <svg
                    className="text-primary h-6 w-6"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                    aria-hidden="true"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M12 4v16m8-8H4"
                    />
                  </svg>
                </div>
                <div>
                  <p className="font-medium">Nova Solicitação</p>
                  <p className="text-muted-foreground text-sm">Abrir nova solicitação de serviço</p>
                </div>
              </CardContent>
            </Card>
          </Link>

          <Link href="/portal/documents/upload">
            <Card className="hover:bg-accent/50 cursor-pointer transition-colors">
              <CardContent className="flex items-center gap-4 pt-6">
                <div className="bg-primary/10 rounded-full p-3">
                  <svg
                    className="text-primary h-6 w-6"
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
                </div>
                <div>
                  <p className="font-medium">Enviar Documento</p>
                  <p className="text-muted-foreground text-sm">Upload de novo documento</p>
                </div>
              </CardContent>
            </Card>
          </Link>

          <Link href="/portal/notifications">
            <Card className="hover:bg-accent/50 cursor-pointer transition-colors">
              <CardContent className="flex items-center gap-4 pt-6">
                <div className="bg-primary/10 rounded-full p-3">
                  <svg
                    className="text-primary h-6 w-6"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                    aria-hidden="true"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
                    />
                  </svg>
                </div>
                <div>
                  <p className="font-medium">Ver Notificações</p>
                  <p className="text-muted-foreground text-sm">Acompanhar atualizações</p>
                </div>
              </CardContent>
            </Card>
          </Link>
        </div>
      </div>
    </div>
  );
}
