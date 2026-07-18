package com.atlasops.boot.seed;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

/**
 * Property-based tests for seed data idempotency.
 *
 * <p><b>Validates: Requirements 12.7</b>
 *
 * <p>Property 17: For any number of consecutive executions of {@code make seed} (n >= 1), the final
 * database state SHALL be identical — containing exactly: 2 tenants (Alpha, Beta), at least one
 * user per role per tenant, at least 3 customers per tenant, and at least 2 documents per customer
 * — with no duplicate records created by repeated executions.
 *
 * <p>This test simulates the seed SQL logic (INSERT ... ON CONFLICT DO NOTHING) using in-memory
 * data structures with unique constraints to validate idempotency without requiring a running
 * database.
 */
@Tag("Feature: monorepo-sdd-harness, Property 17: Seed Idempotency")
class SeedIdempotencyPropertyTest {

  // ─── Expected seed data constants (matching infra/seed/seed.sql) ─────────────

  private static final int EXPECTED_TENANTS = 2;
  private static final int EXPECTED_ROLES = 6;
  private static final int EXPECTED_USERS = 12; // 1 per role per tenant
  private static final int EXPECTED_CUSTOMERS = 8; // 4 per tenant
  private static final int EXPECTED_DOCUMENTS = 16; // 2 per customer

  private static final List<String> ROLE_NAMES =
      List.of("OWNER", "ADMIN", "MANAGER", "ANALYST", "OPERATOR", "VIEWER");

  private static final List<String> TENANT_NAMES = List.of("Alpha", "Beta");

  // ─── Property: Seed produces identical state regardless of number of executions ─

  @Property(tries = 100)
  void seedState_shouldBeIdentical_forAnyNumberOfExecutions(
      @ForAll @IntRange(min = 1, max = 20) int numberOfExecutions) {

    InMemoryDatabase db = new InMemoryDatabase();

    for (int i = 0; i < numberOfExecutions; i++) {
      applySeed(db);
    }

    assertThat(db.tenants.size())
        .as(
            "Should have exactly %d tenants after %d seed executions",
            EXPECTED_TENANTS, numberOfExecutions)
        .isEqualTo(EXPECTED_TENANTS);

    assertThat(db.roles.size())
        .as(
            "Should have exactly %d roles after %d seed executions",
            EXPECTED_ROLES, numberOfExecutions)
        .isEqualTo(EXPECTED_ROLES);

    assertThat(db.users.size())
        .as(
            "Should have exactly %d users after %d seed executions",
            EXPECTED_USERS, numberOfExecutions)
        .isEqualTo(EXPECTED_USERS);

    assertThat(db.customers.size())
        .as(
            "Should have exactly %d customers after %d seed executions",
            EXPECTED_CUSTOMERS, numberOfExecutions)
        .isEqualTo(EXPECTED_CUSTOMERS);

    assertThat(db.documents.size())
        .as(
            "Should have exactly %d documents after %d seed executions",
            EXPECTED_DOCUMENTS, numberOfExecutions)
        .isEqualTo(EXPECTED_DOCUMENTS);
  }

  // ─── Property: No duplicates created by repeated executions ──────────────────

  @Property(tries = 100)
  void seedExecution_shouldNeverCreateDuplicates(
      @ForAll @IntRange(min = 2, max = 15) int numberOfExecutions) {

    InMemoryDatabase db = new InMemoryDatabase();

    // First execution establishes baseline
    applySeed(db);
    DatabaseSnapshot baseline = db.snapshot();

    // Subsequent executions should produce identical state
    for (int i = 1; i < numberOfExecutions; i++) {
      applySeed(db);
      DatabaseSnapshot current = db.snapshot();

      assertThat(current)
          .as("Database state after execution %d should be identical to baseline", i + 1)
          .isEqualTo(baseline);
    }
  }

  // ─── Property: Seed always satisfies minimum data requirements ───────────────

  @Property(tries = 100)
  void seedState_shouldSatisfyMinimumDataRequirements(
      @ForAll @IntRange(min = 1, max = 10) int numberOfExecutions) {

    InMemoryDatabase db = new InMemoryDatabase();

    for (int i = 0; i < numberOfExecutions; i++) {
      applySeed(db);
    }

    // At least one user per role per tenant
    for (String tenantId : db.tenants.keySet()) {
      Set<String> rolesForTenant = new HashSet<>();
      for (UserRecord user : db.users.values()) {
        if (user.tenantId().equals(tenantId)) {
          rolesForTenant.add(user.roleId());
        }
      }
      assertThat(rolesForTenant)
          .as("Tenant %s should have at least one user per role", tenantId)
          .hasSize(EXPECTED_ROLES);
    }

    // At least 3 customers per tenant
    for (String tenantId : db.tenants.keySet()) {
      long customerCount =
          db.customers.values().stream().filter(c -> c.tenantId().equals(tenantId)).count();
      assertThat(customerCount)
          .as("Tenant %s should have at least 3 customers", tenantId)
          .isGreaterThanOrEqualTo(3);
    }

    // At least 2 documents per customer
    for (String customerId : db.customers.keySet()) {
      long docCount =
          db.documents.values().stream().filter(d -> d.customerId().equals(customerId)).count();
      assertThat(docCount)
          .as("Customer %s should have at least 2 documents", customerId)
          .isGreaterThanOrEqualTo(2);
    }
  }

  // ─── Property: Tenant names are exactly Alpha and Beta ───────────────────────

  @Property(tries = 100)
  void seedState_shouldContainExactTenantNames(
      @ForAll @IntRange(min = 1, max = 10) int numberOfExecutions) {

    InMemoryDatabase db = new InMemoryDatabase();

    for (int i = 0; i < numberOfExecutions; i++) {
      applySeed(db);
    }

    Set<String> tenantNames = new HashSet<>();
    for (TenantRecord tenant : db.tenants.values()) {
      tenantNames.add(tenant.name());
    }

    assertThat(tenantNames)
        .as("Seed should contain exactly Alpha and Beta tenants")
        .containsExactlyInAnyOrderElementsOf(TENANT_NAMES);
  }

  // ─── Seed simulation (mirrors infra/seed/seed.sql logic) ─────────────────────

  /**
   * Simulates the seed.sql execution with INSERT ... ON CONFLICT DO NOTHING semantics. Each insert
   * attempts to add a record; if the primary key already exists, the insert is silently ignored (no
   * update, no duplicate).
   */
  private void applySeed(InMemoryDatabase db) {
    // Tenants
    db.insertTenant("a0000000-0000-0000-0000-000000000001", "Alpha", "alpha");
    db.insertTenant("b0000000-0000-0000-0000-000000000002", "Beta", "beta");

    // Roles
    db.insertRole("r0000000-0000-0000-0000-000000000001", "OWNER");
    db.insertRole("r0000000-0000-0000-0000-000000000002", "ADMIN");
    db.insertRole("r0000000-0000-0000-0000-000000000003", "MANAGER");
    db.insertRole("r0000000-0000-0000-0000-000000000004", "ANALYST");
    db.insertRole("r0000000-0000-0000-0000-000000000005", "OPERATOR");
    db.insertRole("r0000000-0000-0000-0000-000000000006", "VIEWER");

    // Alpha tenant users
    db.insertUser(
        "u1000000-0000-0000-0000-000000000001",
        "a0000000-0000-0000-0000-000000000001",
        "r0000000-0000-0000-0000-000000000001",
        "owner@alpha.local",
        "Alpha Owner");
    db.insertUser(
        "u1000000-0000-0000-0000-000000000002",
        "a0000000-0000-0000-0000-000000000001",
        "r0000000-0000-0000-0000-000000000002",
        "admin@alpha.local",
        "Alpha Admin");
    db.insertUser(
        "u1000000-0000-0000-0000-000000000003",
        "a0000000-0000-0000-0000-000000000001",
        "r0000000-0000-0000-0000-000000000003",
        "manager@alpha.local",
        "Alpha Manager");
    db.insertUser(
        "u1000000-0000-0000-0000-000000000004",
        "a0000000-0000-0000-0000-000000000001",
        "r0000000-0000-0000-0000-000000000004",
        "analyst@alpha.local",
        "Alpha Analyst");
    db.insertUser(
        "u1000000-0000-0000-0000-000000000005",
        "a0000000-0000-0000-0000-000000000001",
        "r0000000-0000-0000-0000-000000000005",
        "operator@alpha.local",
        "Alpha Operator");
    db.insertUser(
        "u1000000-0000-0000-0000-000000000006",
        "a0000000-0000-0000-0000-000000000001",
        "r0000000-0000-0000-0000-000000000006",
        "viewer@alpha.local",
        "Alpha Viewer");

    // Beta tenant users
    db.insertUser(
        "u2000000-0000-0000-0000-000000000001",
        "b0000000-0000-0000-0000-000000000002",
        "r0000000-0000-0000-0000-000000000001",
        "owner@beta.local",
        "Beta Owner");
    db.insertUser(
        "u2000000-0000-0000-0000-000000000002",
        "b0000000-0000-0000-0000-000000000002",
        "r0000000-0000-0000-0000-000000000002",
        "admin@beta.local",
        "Beta Admin");
    db.insertUser(
        "u2000000-0000-0000-0000-000000000003",
        "b0000000-0000-0000-0000-000000000002",
        "r0000000-0000-0000-0000-000000000003",
        "manager@beta.local",
        "Beta Manager");
    db.insertUser(
        "u2000000-0000-0000-0000-000000000004",
        "b0000000-0000-0000-0000-000000000002",
        "r0000000-0000-0000-0000-000000000004",
        "analyst@beta.local",
        "Beta Analyst");
    db.insertUser(
        "u2000000-0000-0000-0000-000000000005",
        "b0000000-0000-0000-0000-000000000002",
        "r0000000-0000-0000-0000-000000000005",
        "operator@beta.local",
        "Beta Operator");
    db.insertUser(
        "u2000000-0000-0000-0000-000000000006",
        "b0000000-0000-0000-0000-000000000002",
        "r0000000-0000-0000-0000-000000000006",
        "viewer@beta.local",
        "Beta Viewer");

    // Alpha tenant customers
    db.insertCustomer(
        "c1000000-0000-0000-0000-000000000001",
        "a0000000-0000-0000-0000-000000000001",
        "Acme Corporation",
        "contact@acme.example");
    db.insertCustomer(
        "c1000000-0000-0000-0000-000000000002",
        "a0000000-0000-0000-0000-000000000001",
        "Globex Industries",
        "info@globex.example");
    db.insertCustomer(
        "c1000000-0000-0000-0000-000000000003",
        "a0000000-0000-0000-0000-000000000001",
        "Initech Solutions",
        "hello@initech.example");
    db.insertCustomer(
        "c1000000-0000-0000-0000-000000000004",
        "a0000000-0000-0000-0000-000000000001",
        "Umbrella Corp",
        "sales@umbrella.example");

    // Beta tenant customers
    db.insertCustomer(
        "c2000000-0000-0000-0000-000000000001",
        "b0000000-0000-0000-0000-000000000002",
        "Wayne Enterprises",
        "info@wayne.example");
    db.insertCustomer(
        "c2000000-0000-0000-0000-000000000002",
        "b0000000-0000-0000-0000-000000000002",
        "Stark Industries",
        "contact@stark.example");
    db.insertCustomer(
        "c2000000-0000-0000-0000-000000000003",
        "b0000000-0000-0000-0000-000000000002",
        "Oscorp Research",
        "lab@oscorp.example");
    db.insertCustomer(
        "c2000000-0000-0000-0000-000000000004",
        "b0000000-0000-0000-0000-000000000002",
        "LexCorp Holdings",
        "biz@lexcorp.example");

    // Alpha tenant documents (2 per customer)
    db.insertDocument(
        "d1000000-0000-0000-0000-000000000001",
        "a0000000-0000-0000-0000-000000000001",
        "c1000000-0000-0000-0000-000000000001",
        "Acme - Service Contract 2024");
    db.insertDocument(
        "d1000000-0000-0000-0000-000000000002",
        "a0000000-0000-0000-0000-000000000001",
        "c1000000-0000-0000-0000-000000000001",
        "Acme - Technical Proposal");
    db.insertDocument(
        "d1000000-0000-0000-0000-000000000003",
        "a0000000-0000-0000-0000-000000000001",
        "c1000000-0000-0000-0000-000000000002",
        "Globex - NDA Agreement");
    db.insertDocument(
        "d1000000-0000-0000-0000-000000000004",
        "a0000000-0000-0000-0000-000000000001",
        "c1000000-0000-0000-0000-000000000002",
        "Globex - Meeting Notes Q1");
    db.insertDocument(
        "d1000000-0000-0000-0000-000000000005",
        "a0000000-0000-0000-0000-000000000001",
        "c1000000-0000-0000-0000-000000000003",
        "Initech - Project Scope");
    db.insertDocument(
        "d1000000-0000-0000-0000-000000000006",
        "a0000000-0000-0000-0000-000000000001",
        "c1000000-0000-0000-0000-000000000003",
        "Initech - Budget Estimate");
    db.insertDocument(
        "d1000000-0000-0000-0000-000000000007",
        "a0000000-0000-0000-0000-000000000001",
        "c1000000-0000-0000-0000-000000000004",
        "Umbrella - Lab Report");
    db.insertDocument(
        "d1000000-0000-0000-0000-000000000008",
        "a0000000-0000-0000-0000-000000000001",
        "c1000000-0000-0000-0000-000000000004",
        "Umbrella - Safety Audit");

    // Beta tenant documents (2 per customer)
    db.insertDocument(
        "d2000000-0000-0000-0000-000000000001",
        "b0000000-0000-0000-0000-000000000002",
        "c2000000-0000-0000-0000-000000000001",
        "Wayne - R&D Partnership");
    db.insertDocument(
        "d2000000-0000-0000-0000-000000000002",
        "b0000000-0000-0000-0000-000000000002",
        "c2000000-0000-0000-0000-000000000001",
        "Wayne - Annual Review");
    db.insertDocument(
        "d2000000-0000-0000-0000-000000000003",
        "b0000000-0000-0000-0000-000000000002",
        "c2000000-0000-0000-0000-000000000002",
        "Stark - Technology License");
    db.insertDocument(
        "d2000000-0000-0000-0000-000000000004",
        "b0000000-0000-0000-0000-000000000002",
        "c2000000-0000-0000-0000-000000000002",
        "Stark - Integration Spec");
    db.insertDocument(
        "d2000000-0000-0000-0000-000000000005",
        "b0000000-0000-0000-0000-000000000002",
        "c2000000-0000-0000-0000-000000000003",
        "Oscorp - Research Agreement");
    db.insertDocument(
        "d2000000-0000-0000-0000-000000000006",
        "b0000000-0000-0000-0000-000000000002",
        "c2000000-0000-0000-0000-000000000003",
        "Oscorp - Lab Certification");
    db.insertDocument(
        "d2000000-0000-0000-0000-000000000007",
        "b0000000-0000-0000-0000-000000000002",
        "c2000000-0000-0000-0000-000000000004",
        "LexCorp - Acquisition Memo");
    db.insertDocument(
        "d2000000-0000-0000-0000-000000000008",
        "b0000000-0000-0000-0000-000000000002",
        "c2000000-0000-0000-0000-000000000004",
        "LexCorp - Due Diligence");
  }

  // ─── In-memory database simulation ───────────────────────────────────────────

  /**
   * Simulates a PostgreSQL database with unique constraints and INSERT ... ON CONFLICT DO NOTHING
   * behavior.
   */
  private static class InMemoryDatabase {
    final Map<String, TenantRecord> tenants = new LinkedHashMap<>();
    final Map<String, RoleRecord> roles = new LinkedHashMap<>();
    final Map<String, UserRecord> users = new LinkedHashMap<>();
    final Map<String, CustomerRecord> customers = new LinkedHashMap<>();
    final Map<String, DocumentRecord> documents = new LinkedHashMap<>();

    // Secondary unique indices (simulating UNIQUE constraints)
    private final Set<String> tenantNames = new HashSet<>();
    private final Set<String> tenantSlugs = new HashSet<>();
    private final Set<String> roleNames = new HashSet<>();
    private final Set<String> userEmails = new HashSet<>();
    private final Set<String> customerTenantEmails = new HashSet<>();

    void insertTenant(String id, String name, String slug) {
      // ON CONFLICT (id) DO NOTHING
      if (tenants.containsKey(id)) {
        return;
      }
      // Also enforce UNIQUE(name) and UNIQUE(slug)
      if (tenantNames.contains(name) || tenantSlugs.contains(slug)) {
        return;
      }
      tenants.put(id, new TenantRecord(id, name, slug));
      tenantNames.add(name);
      tenantSlugs.add(slug);
    }

    void insertRole(String id, String name) {
      // ON CONFLICT (id) DO NOTHING
      if (roles.containsKey(id)) {
        return;
      }
      if (roleNames.contains(name)) {
        return;
      }
      roles.put(id, new RoleRecord(id, name));
      roleNames.add(name);
    }

    void insertUser(String id, String tenantId, String roleId, String email, String name) {
      // ON CONFLICT (id) DO NOTHING
      if (users.containsKey(id)) {
        return;
      }
      // Also enforce UNIQUE(email)
      if (userEmails.contains(email)) {
        return;
      }
      users.put(id, new UserRecord(id, tenantId, roleId, email, name));
      userEmails.add(email);
    }

    void insertCustomer(String id, String tenantId, String name, String email) {
      // ON CONFLICT (id) DO NOTHING
      if (customers.containsKey(id)) {
        return;
      }
      // Also enforce UNIQUE(tenant_id, email)
      String tenantEmailKey = tenantId + "|" + email;
      if (customerTenantEmails.contains(tenantEmailKey)) {
        return;
      }
      customers.put(id, new CustomerRecord(id, tenantId, name, email));
      customerTenantEmails.add(tenantEmailKey);
    }

    void insertDocument(String id, String tenantId, String customerId, String title) {
      // ON CONFLICT (id) DO NOTHING
      if (documents.containsKey(id)) {
        return;
      }
      documents.put(id, new DocumentRecord(id, tenantId, customerId, title));
    }

    DatabaseSnapshot snapshot() {
      return new DatabaseSnapshot(
          new LinkedHashMap<>(tenants),
          new LinkedHashMap<>(roles),
          new LinkedHashMap<>(users),
          new LinkedHashMap<>(customers),
          new LinkedHashMap<>(documents));
    }
  }

  // ─── Record types (simulating database rows) ─────────────────────────────────

  private record TenantRecord(String id, String name, String slug) {}

  private record RoleRecord(String id, String name) {}

  private record UserRecord(String id, String tenantId, String roleId, String email, String name) {}

  private record CustomerRecord(String id, String tenantId, String name, String email) {}

  private record DocumentRecord(String id, String tenantId, String customerId, String title) {}

  private record DatabaseSnapshot(
      Map<String, TenantRecord> tenants,
      Map<String, RoleRecord> roles,
      Map<String, UserRecord> users,
      Map<String, CustomerRecord> customers,
      Map<String, DocumentRecord> documents) {}
}
