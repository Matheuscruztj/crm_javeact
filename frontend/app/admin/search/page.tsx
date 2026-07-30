import { Suspense } from "react";
import SearchPageClient from "./search-page-client";

export default function SearchPage() {
  return (
    <Suspense fallback={<div className="text-muted-foreground p-6 text-sm">Loading search...</div>}>
      <SearchPageClient />
    </Suspense>
  );
}
