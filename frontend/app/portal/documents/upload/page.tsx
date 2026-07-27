import { Suspense } from "react";
import PortalDocumentUploadPageClient from "./upload-page-client";

export default function PortalDocumentUploadPage() {
  return (
    <Suspense
      fallback={<div className="p-6 text-sm text-muted-foreground">Loading upload form...</div>}
    >
      <PortalDocumentUploadPageClient />
    </Suspense>
  );
}
