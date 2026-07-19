package com.atlasops.auth.domain;

/**
 * Thread-local holder for the validated tenant identifier of the current request.
 *
 * <p>Set by {@code TenantContextFilter} after validating the X-Tenant-ID header matches the JWT
 * tenant claim. Cleared after request completion.
 */
public final class TenantContext {

  private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

  private TenantContext() {
    // Utility class — not instantiable
  }

  /**
   * Sets the tenant identifier for the current thread.
   *
   * @param tenantId the validated tenant identifier
   */
  public static void setTenantId(String tenantId) {
    CURRENT_TENANT.set(tenantId);
  }

  /**
   * Returns the tenant identifier for the current thread.
   *
   * @return the current tenant identifier, or {@code null} if not set
   */
  public static String getTenantId() {
    return CURRENT_TENANT.get();
  }

  /**
   * Clears the tenant identifier from the current thread. Must be called after request processing
   * to prevent thread-local leaks.
   */
  public static void clear() {
    CURRENT_TENANT.remove();
  }
}
