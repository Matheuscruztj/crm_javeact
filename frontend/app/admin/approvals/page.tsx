import { Suspense } from "react";
import ApprovalsPageClient from "./approvals-page-client";

export default function ApprovalsPage() {
  return (
    <Suspense
      fallback={<div className="text-muted-foreground p-6 text-sm">Loading approvals...</div>}
    >
      <ApprovalsPageClient />
    </Suspense>
  );
}
