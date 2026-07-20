/**
 * Form utilities: zod schema helpers and API error mapping.
 * Validates: P0.L.3 — Frontend Form Infrastructure
 */
import { FieldPath, FieldValues, UseFormSetError } from "react-hook-form";
import { ApiError } from "./api-client";

/**
 * Maps RFC 7807 API validation violations to react-hook-form field errors.
 * Enables server-side validation errors to appear inline in form fields.
 *
 * @param error - The ApiError from a failed API request
 * @param setError - react-hook-form's setError function
 *
 * @example
 * try {
 *   await api.post('/customers', data);
 * } catch (err) {
 *   mapApiErrorsToForm(err as ApiError, setError);
 * }
 */
export function mapApiErrorsToForm<T extends FieldValues>(
  error: ApiError | unknown,
  setError: UseFormSetError<T>,
): void {
  const apiError = error as ApiError;
  if (!apiError?.violations) return;

  for (const violation of apiError.violations) {
    setError(violation.field as FieldPath<T>, {
      type: "server",
      message: violation.message,
    });
  }
}

/**
 * Extracts a human-readable error message from an API error.
 * Falls back to the error's detail, title, or a generic message.
 */
export function getApiErrorMessage(error: unknown): string {
  const apiError = error as ApiError;
  if (apiError?.detail) return apiError.detail;
  if (apiError?.title) return apiError.title;
  if (error instanceof Error) return error.message;
  return "An unexpected error occurred. Please try again.";
}

/**
 * Checks if an error is an API error with a specific code.
 */
export function isApiErrorCode(error: unknown, code: string): boolean {
  const apiError = error as ApiError;
  return apiError?.code === code;
}
