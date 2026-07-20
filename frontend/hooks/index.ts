/**
 * Hooks exports.
 */

export { useAuth, getRedirectPath } from "./use-auth";
export {
  useSSE,
  useSSENotifications,
  useSSEDocumentProgress,
  type SSEEvent,
  type SSEEventType,
  type SSECallbacks,
} from "./use-sse";
export {
  useApiQuery,
  usePagedQuery,
  useMutation,
  useCustomers,
  useCustomer,
  useRequests,
  useRequest,
  useDocuments,
  useDocument,
  useNotifications,
  useUnreadCount,
} from "./use-api";
export { useCommandPalette } from "./use-command-palette";
export { useConfirm } from "./use-confirm";
export { useOptimistic } from "./use-optimistic";
export { useToast, toastHelpers, type ToastVariant } from "./use-toast";
