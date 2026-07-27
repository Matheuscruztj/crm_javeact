import { Suspense } from "react";
import ApprovalsPageClient from "./approvals-page-client";

export default function ApprovalsPage() {
  return (
    <Suspense
      fallback={<div className="p-6 text-sm text-muted-foreground">Loading approvals...</div>}
    >
      <ApprovalsPageClient />
    </Suspense>
  );
}
