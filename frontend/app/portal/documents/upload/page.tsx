import { Suspense } from "react";
import PortalDocumentUploadPageClient from "./upload-page-client";

export default function PortalDocumentUploadPage() {
  return (
    <Suspense
      fallback={<div className="text-muted-foreground p-6 text-sm">Loading upload form...</div>}
    >
      <PortalDocumentUploadPageClient />
    </Suspense>
  );
}
