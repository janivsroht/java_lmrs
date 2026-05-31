# AGENTS.md

> Guidelines for AI coding agents working on this repository.

---

## Project Overview

**LMRS** (Lodging & Restaurant Management Reservation System) — a multi-tenant hotel management platform with restaurant operations, partner API integrations, and an AI concierge.

### Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Java 26 |
| **Framework** | Spring Boot 4.0.6 |
| **Build Tool** | Gradle 9.4.1 |
| **Frontend** | Thymeleaf + Vanilla JavaScript + Bootstrap 5.3.2 |
| **Database** | MySQL 8.x (`lrms_db`) |
| **Migrations** | Flyway 10.21.0 (7 migrations: V1–V7) |
| **ORM** | Spring Data JPA / Hibernate (validate mode) |
| **Auth** | Spring Security + JWT (jjwt 0.12.5) + BCrypt (strength 12) |
| **AI** | Groq API (LLaMA 3 70B) |
| **Testing** | JUnit 5 + Spring Boot Test |
| **Utilities** | Lombok, Jakarta Bean Validation |

### Architecture Pattern

**Controller → Service → Repository** with multi-tenant isolation.

```
com.project.lmrs/
├── config/           # SecurityConfig, JwtConfig, GroqConfig
├── controller/       # 32 controllers (REST + Page)
├── dto/
│   ├── request/      # 22 request DTOs
│   └── response/     # 18 response DTOs
├── entity/           # 22 JPA entities
├── enums/            # 10 enums (UserRole, RoomStatus, OrderStatus, etc.)
├── exception/        # GlobalExceptionHandler + custom exceptions
├── filter/           # PartnerApiKeyFilter
├── repository/       # 22 Spring Data repositories
├── security/         # JWT filter, token provider, user details service
└── service/          # 20 service classes
```

### Database Schema

22 tables across 7 Flyway migrations. Key domains:
- **Tenants & Users** — multi-tenant isolation (V1)
- **Guests, Rooms, Room Types** — core hotel (V2)
- **Reservations, Folios, Payments** — booking & billing (V3)
- **Restaurant Tables, Table Reservations, Menu** — F&B operations (V4)
- **Orders, Inventory, Housekeeping, Loyalty, Audit** — operations (V5)
- **Partner Accounts, API Usage** — external integrations (V6)
- **Indexes & Constraints** — performance optimization (V7)

### Key Design Patterns

- **Multi-tenancy**: Every entity has a `tenant_id` FK. All queries filter by tenant.
- **Soft delete**: Entities use `is_deleted` + `deleted_at` (never physical deletes).
- **UUID primary keys**: All entities use `UUID` as PK (char(36) in MySQL).
- **Audit timestamps**: `created_at`, `updated_at` managed by `@PrePersist`/`@PreUpdate`.
- **Stateless auth**: JWT tokens, no HTTP sessions.

---

## Executable Commands

### Build & Run

```bash
# Build the production JAR
./gradlew build

# Build without tests
./gradlew build -x test

# Run the application locally
./gradlew bootRun

# Run in background (Unix)
nohup ./gradlew bootRun > app.log 2>&1 &
```

### Testing

```bash
# Run all tests
./gradlew test

# Run a specific test class
./gradlew test --tests "com.project.lmrs.LmrsApplicationTests"

# Run tests with coverage
./gradlew test jacocoTestReport
```

### Database Migrations

```bash
# Run Flyway migrations (auto-runs on startup)
./gradlew flywayMigrate

# Check migration status
./gradlew flywayInfo

# Validate migrations
./gradlew flywayValidate
```

### Dependencies

```bash
# Download all dependencies
./gradlew dependencies

# Clean build cache
./gradlew clean
```

### Production JAR

```bash
# After build, the JAR is at:
# build/libs/lmrs-0.0.1-SNAPSHOT.jar

# Run the production JAR
java -jar build/libs/lmrs-0.0.1-SNAPSHOT.jar
```

---

## Code Style & Conventions

### General Principles

- **Constructor injection** via Lombok's `@RequiredArgsConstructor` — never use `@Autowired` on fields.
- **Lombok annotations** on all entities: `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder`.
- **Jakarta Validation** (`@NotBlank`, `@NotNull`, `@Min`, `@PositiveOrZero`) on all request DTOs.
- **DTOs for API boundaries**: Request DTOs in `dto/request/`, Response DTOs in `dto/response/`. Never expose raw entities from controllers.
- **Service layer** contains business logic. Controllers delegate to services.
- **Repository layer** contains only query definitions (Spring Data JPA derived queries or `@Query`).

### Entity Conventions

```java
@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", length = 36)
    private String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }
}
```

### Controller Conventions

- REST controllers use `@RestController` + `@RequestMapping("/api/v1/...")`.
- Page controllers use `@Controller` + `@RequestMapping("/dashboard")`.
- Use `@PreAuthorize("hasAnyRole('...')")` for role-based access.
- Always extract `tenantId` from `SecurityUtils.getCurrentTenantId()`.
- Return `ResponseEntity<T>` with appropriate HTTP status codes.

```java
@RestController
@RequestMapping("/api/v1/guests")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGER','PROPERTY_ADMIN','SUPER_ADMIN','FRONT_DESK')")
public class GuestController {
    private final GuestService guestService;

    @GetMapping
    public ResponseEntity<List<GuestResponse>> getAllGuests() {
        String tenantId = SecurityUtils.getCurrentTenantId();
        return ResponseEntity.ok(guestService.getAllGuests(tenantId));
    }
}
```

### Service Conventions

- Use `@Transactional` on write methods.
- Use `@RequiredArgsConstructor` for dependency injection.
- Throw `ResourceNotFoundException` for missing entities.
- Throw `BusinessRuleException` for business logic violations.
- Log audit events via `AuditLogService.log()`.

### Exception Handling

All exceptions are handled by `GlobalExceptionHandler` (`@RestControllerAdvice`):

| Exception | HTTP Status | Usage |
|-----------|-------------|-------|
| `ResourceNotFoundException` | 404 | Entity not found |
| `BusinessRuleException` | 400 | Business logic violation |
| `UnauthorizedException` | 401 | Auth failure |
| `BadCredentialsException` | 401 | Wrong credentials |
| `AccessDeniedException` | 403 | Insufficient permissions |
| `MethodArgumentNotValidException` | 400 | Validation failure |

### Multi-Tenant Isolation

Every entity has a `tenant_id` foreign key. All service methods that read/write data must:
1. Accept `tenantId` parameter (from `SecurityUtils.getCurrentTenantId()`)
2. Use tenant-filtered repository methods (e.g., `findByXAndTenant_TenantIdAndIsDeletedFalse`)
3. Never use plain `findById()` without tenant filtering

### Frontend Conventions

- Thymeleaf templates in `src/main/resources/templates/dashboard/`.
- Vanilla JavaScript in `src/main/resources/static/js/`.
- Use `Auth.authFetch()` for authenticated API calls (auto-attaches JWT).
- Use `showToast(message, type)` for user notifications.
- Use `esc(s)` for XSS-safe HTML escaping.
- Bootstrap 5 classes for UI components.

---

## Boundaries & Rules

### Always Do

- **Run `./gradlew build`** before committing to verify compilation and tests pass.
- **Use `SecurityUtils.getCurrentTenantId()`** in all controller methods that access data.
- **Filter by tenant** in all repository queries — never use plain `findById()` without tenant.
- **Use DTOs** for request/response — never expose raw JPA entities from controllers.
- **Add `@Valid`** on `@RequestBody` parameters that have validation annotations.
- **Soft delete** entities (set `isDeleted=true`, `deletedAt=now()`) — never physical deletes.
- **Create Flyway migration** (V8__, V9__, etc.) when modifying the database schema.
- **Use `@RequiredArgsConstructor`** for constructor injection.
- **Handle errors gracefully** in frontend JS with `.catch()` and `showToast()`.
- **Use `esc()` function** when injecting user-provided data into HTML to prevent XSS.

### Ask First

- **Adding new dependencies** to `build.gradle` — verify compatibility with Spring Boot 4.x.
- **Modifying `SecurityConfig`** — security changes affect all endpoints.
- **Changing JWT configuration** (`jwt.secret`, expiry values) — affects all tokens.
- **Modifying Flyway migrations** (V1–V7) — these are already applied and cannot be changed.
- **Changing database schema** — requires new migration file + entity update.
- **Adding new API endpoints** — follow existing patterns (`/api/v1/{resource}`).
- **Modifying `PartnerApiKeyFilter`** — affects external partner integrations.
- **Changing `GroqAiService`** — affects AI concierge functionality.

### Never Do

- **Never commit hardcoded secrets** — use environment variables (`${DB_USERNAME}`, `${JWT_SECRET}`, `${GROQ_API_KEY}`).
- **Never modify applied Flyway migrations** (V1–V7) — create new ones instead.
- **Never use `@Autowired` on fields** — use constructor injection via `@RequiredArgsConstructor`.
- **Never expose raw entities** from controllers — always use DTOs.
- **Never skip tenant isolation** — every data access must filter by `tenantId`.
- **Never use `System.out.println()`** — use SLF4J logger (`@Slf4j` from Lombok).
- **Never use `spring.jpa.hibernate.ddl-auto=update`** — schema is managed by Flyway.
- **Never delete audit logs or loyalty transactions** — they are append-only.
- **Never hardcode tenant IDs** — always extract from `SecurityUtils`.
- **Never bypass `@PreAuthorize`** — every endpoint must have role-based access control.
- **Never modify `application.properties` secrets directly** — use environment variables.
- **Never use field injection** — always constructor injection.

---

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_USERNAME` | `root` | MySQL username |
| `DB_PASSWORD` | `1234` | MySQL password |
| `JWT_SECRET` | (placeholder) | HMAC-SHA secret for JWT signing |
| `GROQ_API_KEY` | (placeholder) | Groq API key for AI features |

---

## Key Files Reference

| File | Purpose |
|------|---------|
| `build.gradle` | Dependencies, plugins, Flyway config |
| `application.properties` | App config, DB, JWT, Groq settings |
| `SecurityConfig.java` | Spring Security filter chain |
| `JwtTokenProvider.java` | JWT creation/validation |
| `JwtAuthenticationFilter.java` | JWT auth filter |
| `GlobalExceptionHandler.java` | Centralized error handling |
| `PartnerApiKeyFilter.java` | Partner API key auth + rate limiting |
| `GroqAiService.java` | Groq API integration |
| `LmrsApplication.java` | Application entry point |
