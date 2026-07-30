"use client";

/**
 * useOptimistic: client-side optimistic update pattern with rollback on error.
 * Validates: P1.13.3 — Optimistic UI pattern
 *
 * Usage:
 *   const { data, optimisticUpdate } = useOptimistic(initialData);
 *   await optimisticUpdate(
 *     newData,              // optimistically applied immediately
 *     () => api.put(...)    // actual API call
 *   );
 */

import { useCallback, useState } from "react";
import { getApiErrorMessage } from "@/lib/form-utils";

interface UseOptimisticReturn<T> {
  data: T;
  isOptimistic: boolean;
  error: string | null;
  optimisticUpdate: (
    nextData: T,
    mutation: () => Promise<T>,
    onSuccess?: (result: T) => void
  ) => Promise<void>;
  reset: () => void;
}

export function useOptimistic<T>(initialData: T): UseOptimisticReturn<T> {
  const [data, setData] = useState<T>(initialData);
  const [previousData, setPreviousData] = useState<T>(initialData);
  const [isOptimistic, setIsOptimistic] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const optimisticUpdate = useCallback(
    async (nextData: T, mutation: () => Promise<T>, onSuccess?: (result: T) => void) => {
      // 1. Save current state for rollback
      setPreviousData(data);
      // 2. Optimistically apply the update
      setData(nextData);
      setIsOptimistic(true);
      setError(null);

      try {
        // 3. Perform actual mutation
        const result = await mutation();
        // 4. Apply server-confirmed data
        setData(result);
        onSuccess?.(result);
      } catch (err) {
        // 5. Rollback on failure
        setData(previousData);
        setError(getApiErrorMessage(err));
      } finally {
        setIsOptimistic(false);
      }
    },
    [data, previousData]
  );

  const reset = useCallback(() => {
    setData(previousData);
    setError(null);
    setIsOptimistic(false);
  }, [previousData]);

  return { data, isOptimistic, error, optimisticUpdate, reset };
}
