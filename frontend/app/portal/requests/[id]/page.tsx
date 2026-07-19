/**
 * Portal Request Detail Page.
 * Task 23.2: Detail page with info, documents, comments thread, status timeline.
 * Requirements: 23.4
 */

"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { api } from "@/lib/api-client";

interface RequestDetail {
  id: string;
  title: string;
  description: string;
  status: "OPEN" | "IN_PROGRESS" | "RESOLVED" | "CLOSED";
  priority: "LOW" | "MEDIUM" | "HIGH" | "URGENT";
  createdAt: string;
  updatedAt: string;
  assignedAnalystName: string | null;
}

interface Document {
  id: string;
  fileName: string;
  status: string;
  uploadedAt: string;
}

interface Comment {
  id: string;
  content: string;
  authorName: string;
  authorRole: "ADMIN" | "ANALYST" | "CLIENT";
  createdAt: string;
}

interface StatusHistoryItem {
  status: string;
  changedAt: string;
  changedByName: string;
}

function getStatusBadgeClass(status: RequestDetail["status"]): string {
  switch (status) {
    case "OPEN":
      return "bg-blue-100 text-blue-800 border-blue-200";
    case "IN_PROGRESS":
      return "bg-yellow-100 text-yellow-800 border-yellow-200";
    case "RESOLVED":
      return "bg-green-100 text-green-800 border-green-200";
    case "CLOSED":
      return "bg-gray-100 text-gray-800 border-gray-200";
    default:
      return "";
  }
}

function getStatusLabel(status: RequestDetail["status"]): string {
  switch (status) {
    case "OPEN":
      return "Aberta";
    case "IN_PROGRESS":
      return "Em Andamento";
    case "RESOLVED":
      return "Resolvida";
    case "CLOSED":
      return "Fechada";
    default:
      return status;
  }
}

function getPriorityBadgeClass(priority: RequestDetail["priority"]): string {
  switch (priority) {
    case "LOW":
      return "bg-slate-100 text-slate-800 border-slate-200";
    case "MEDIUM":
      return "bg-blue-100 text-blue-800 border-blue-200";
    case "HIGH":
      return "bg-orange-100 text-orange-800 border-orange-200";
    case "URGENT":
      return "bg-red-100 text-red-800 border-red-200";
    default:
      return "";
  }
}

function getPriorityLabel(priority: RequestDetail["priority"]): string {
  switch (priority) {
    case "LOW":
      return "Baixa";
    case "MEDIUM":
      return "Média";
    case "HIGH":
      return "Alta";
    case "URGENT":
      return "Urgente";
    default:
      return priority;
  }
}

function CommentThread({
  requestId,
  comments,
  onCommentAdded,
}: {
  requestId: string;
  comments: Comment[];
  onCommentAdded: () => void;
}) {
  const [newComment, setNewComment] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newComment.trim()) return;

    setIsSubmitting(true);
    try {
      await api.post(`/requests/${requestId}/comments`, {
        content: newComment.trim(),
      });
      setNewComment("");
      onCommentAdded();
    } catch {
      // Error handling
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="space-y-4">
      <form onSubmit={handleSubmit} className="flex gap-2">
        <Input
          value={newComment}
          onChange={(e) => setNewComment(e.target.value)}
          placeholder="Adicionar comentário..."
          disabled={isSubmitting}
          className="flex-1"
        />
        <Button type="submit" disabled={isSubmitting || !newComment.trim()}>
          Enviar
        </Button>
      </form>

      <div className="space-y-4">
        {comments.length === 0 ? (
          <p className="text-sm text-muted-foreground py-4 text-center">
            Nenhum comentário ainda.
          </p>
        ) : (
          comments.map((comment) => (
            <div key={comment.id} className="rounded-lg border p-4">
              <div className="flex items-center justify-between mb-2">
                <div className="flex items-center gap-2">
                  <span className="font-medium">{comment.authorName}</span>
                  <Badge variant="outline" className="text-xs">
                    {comment.authorRole === "CLIENT" ? "Cliente" : "Analista"}
                  </Badge>
                </div>
                <span className="text-xs text-muted-foreground">
                  {new Date(comment.createdAt).toLocaleString("pt-BR")}
                </span>
              </div>
              <p className="text-sm">{comment.content}</p>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

function StatusTimeline({ history }: { history: StatusHistoryItem[] }) {
  return (
    <div className="space-y-4">
      {history.length === 0 ? (
        <p className="text-sm text-muted-foreground py-4 text-center">
          Nenhum histórico disponível.
        </p>
      ) : (
        <div className="relative">
          <div className="absolute left-3 top-0 bottom-0 w-0.5 bg-border" />
          {history.map((item, index) => (
            <div key={index} className="relative flex items-start gap-4 pb-6">
              <div className="absolute left-0 w-6 h-6 rounded-full bg-primary flex items-center justify-center">
                <div className="w-2 h-2 rounded-full bg-primary-foreground" />
              </div>
              <div className="ml-10">
                <p className="font-medium">{item.status}</p>
                <p className="text-sm text-muted-foreground">
                  por {item.changedByName} em{" "}
                  {new Date(item.changedAt).toLocaleString("pt-BR")}
                </p>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default function PortalRequestDetailPage() {
  const params = useParams();
  const router = useRouter();
  const requestId = params.id as string;

  const [request, setRequest] = useState<RequestDetail | null>(null);
  const [documents, setDocuments] = useState<Document[]>([]);
  const [comments, setComments] = useState<Comment[]>([]);
  const [statusHistory, setStatusHistory] = useState<StatusHistoryItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchData = async () => {
    setIsLoading(true);
    setError(null);

    try {
      const [requestRes, docsRes, commentsRes, historyRes] =
        await Promise.allSettled([
          api.get<RequestDetail>(`/requests/${requestId}`),
          api.get<{ content: Document[] }>(`/requests/${requestId}/documents`),
          api.get<Comment[]>(`/requests/${requestId}/comments`),
          api.get<StatusHistoryItem[]>(`/requests/${requestId}/status-history`),
        ]);

      if (requestRes.status === "fulfilled") {
        setRequest(requestRes.value);
      } else {
        throw new Error("Solicitação não encontrada");
      }

      if (docsRes.status === "fulfilled") {
        setDocuments(docsRes.value.content);
      }

      if (commentsRes.status === "fulfilled") {
        setComments(commentsRes.value);
      }

      if (historyRes.status === "fulfilled") {
        setStatusHistory(historyRes.value);
      }
    } catch {
      setError("Falha ao carregar detalhes da solicitação");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (requestId) {
      fetchData();
    }
  }, [requestId]);

  if (isLoading) {
    return (
      <div className="p-6">
        <Skeleton className="h-8 w-48 mb-4" />
        <Skeleton className="h-4 w-96 mb-6" />
        <Card>
          <CardContent className="p-6 space-y-4">
            <Skeleton className="h-6 w-full" />
            <Skeleton className="h-6 w-3/4" />
            <Skeleton className="h-6 w-1/2" />
          </CardContent>
        </Card>
      </div>
    );
  }

  if (error || !request) {
    return (
      <div className="p-6">
        <div className="rounded-md bg-destructive/10 p-4 text-sm text-destructive mb-4">
          {error || "Solicitação não encontrada"}
        </div>
        <Button variant="outline" onClick={() => router.back()}>
          Voltar
        </Button>
      </div>
    );
  }

  return (
    <div className="p-6">
      {/* Header */}
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

        <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
          <div>
            <h1 className="text-2xl font-bold">{request.title}</h1>
            <p className="text-muted-foreground">
              Criada em{" "}
              {new Date(request.createdAt).toLocaleDateString("pt-BR")}
            </p>
          </div>
          <div className="flex gap-2">
            <Badge className={getStatusBadgeClass(request.status)}>
              {getStatusLabel(request.status)}
            </Badge>
            <Badge className={getPriorityBadgeClass(request.priority)}>
              {getPriorityLabel(request.priority)}
            </Badge>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="grid gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2 space-y-6">
          {/* Description */}
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Descrição</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="whitespace-pre-wrap">{request.description}</p>
            </CardContent>
          </Card>

          {/* Tabs for Documents, Comments, Timeline */}
          <Tabs defaultValue="documents">
            <TabsList>
              <TabsTrigger value="documents">
                Documentos ({documents.length})
              </TabsTrigger>
              <TabsTrigger value="comments">
                Comentários ({comments.length})
              </TabsTrigger>
              <TabsTrigger value="timeline">Histórico</TabsTrigger>
            </TabsList>

            <TabsContent value="documents" className="mt-4">
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg">Documentos</CardTitle>
                  <CardDescription>
                    Documentos anexados a esta solicitação
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  {documents.length === 0 ? (
                    <div className="text-center py-4">
                      <p className="text-muted-foreground mb-2">
                        Nenhum documento anexado.
                      </p>
                      <Link
                        href={`/portal/documents/upload?requestId=${requestId}`}
                      >
                        <Button variant="outline" size="sm">
                          Enviar Documento
                        </Button>
                      </Link>
                    </div>
                  ) : (
                    <div className="space-y-2">
                      {documents.map((doc) => (
                        <div
                          key={doc.id}
                          className="flex items-center justify-between rounded-md border p-3"
                        >
                          <div className="flex items-center gap-3">
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
                            <div>
                              <p className="text-sm font-medium">
                                {doc.fileName}
                              </p>
                              <p className="text-xs text-muted-foreground">
                                {new Date(doc.uploadedAt).toLocaleDateString(
                                  "pt-BR",
                                )}
                              </p>
                            </div>
                          </div>
                          <Badge variant="outline">{doc.status}</Badge>
                        </div>
                      ))}
                    </div>
                  )}
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="comments" className="mt-4">
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg">Comentários</CardTitle>
                  <CardDescription>
                    Discussão sobre esta solicitação
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <CommentThread
                    requestId={requestId}
                    comments={comments}
                    onCommentAdded={fetchData}
                  />
                </CardContent>
              </Card>
            </TabsContent>

            <TabsContent value="timeline" className="mt-4">
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg">Histórico de Status</CardTitle>
                  <CardDescription>
                    Linha do tempo das mudanças de status
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <StatusTimeline history={statusHistory} />
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Informações</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div>
                <p className="text-sm text-muted-foreground">
                  Analista Responsável
                </p>
                <p className="font-medium">
                  {request.assignedAnalystName || "Não atribuído"}
                </p>
              </div>
              <Separator />
              <div>
                <p className="text-sm text-muted-foreground">Criada em</p>
                <p className="font-medium">
                  {new Date(request.createdAt).toLocaleString("pt-BR")}
                </p>
              </div>
              <Separator />
              <div>
                <p className="text-sm text-muted-foreground">
                  Última atualização
                </p>
                <p className="font-medium">
                  {new Date(request.updatedAt).toLocaleString("pt-BR")}
                </p>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-lg">Ações</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2">
              <Link
                href={`/portal/documents/upload?requestId=${requestId}`}
                className="block"
              >
                <Button variant="outline" className="w-full">
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
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
