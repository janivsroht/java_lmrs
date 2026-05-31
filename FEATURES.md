# LMRS — Lodging & Restaurant Management Reservation System

## Complete Feature & Functionality Documentation

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Technology Stack](#2-technology-stack)
3. [Architecture Pattern](#3-architecture-pattern)
4. [Authentication & Security](#4-authentication--security)
5. [Multi-Tenancy](#5-multi-tenancy)
6. [Database Schema](#6-database-schema)
7. [Feature Modules](#7-feature-modules)
8. [Frontend Pages](#8-frontend-pages)
9. [API Reference](#9-api-reference)
10. [Enums Reference](#10-enums-reference)
11. [DTO Reference](#11-dto-reference)
12. [Configuration Properties](#12-configuration-properties)
13. [Flyway Migrations](#13-flyway-migrations)
14. [Error Handling](#14-error-handling)
15. [Seed Data](#15-seed-data)

---

## 1. Project Overview

**LMRS** (Lodging & Restaurant Management Reservation System) is a comprehensive, multi-tenant hotel and restaurant management platform. It handles the complete lifecycle of hospitality operations — from guest check-in to restaurant order fulfillment — with an integrated AI concierge and partner API system.

### Core Capabilities

| Domain | Features |
|--------|----------|
| **Guest Management** | Guest profiles, loyalty tiers, document tracking, search |
| **Room Management** | Room types, availability tracking, status management, floor mapping |
| **Reservation Management** | Multi-channel bookings, conflict detection, check-in/out workflows, no-show handling |
| **Folio & Billing** | Per-reservation folios, line-item charges, multiple payment methods, auto-close |
| **Restaurant Operations** | Table management, table reservations, menu CRUD, order state machine |
| **Order Management** | Full order lifecycle (OPEN → SUBMITTED → IN_KITCHEN → READY → SERVED → CLOSED), voiding |
| **Inventory Management** | Stock tracking, reorder alerts, adjustment with reason logging |
| **Housekeeping** | Task scheduling, assignment, priority levels, completion tracking |
| **Loyalty Program** | Points earn/redeem, 4 tiers (Bronze → Platinum), auto-tier recalculation |
| **Reporting** | Revenue reports, occupancy analytics, channel distribution, top menu items, CSV export |
| **AI Concierge** | Groq-powered guest query assistant, feedback sentiment analysis, menu description generation |
| **Partner API** | External API integration with API key auth, rate limiting (100 req/min), usage analytics |
| **Audit Logging** | Append-only audit trail with old/new values, IP tracking, entity-level history |

---

## 2. Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| **Language** | Java | 26 |
| **Framework** | Spring Boot | 4.0.6 |
| **Build Tool** | Gradle | 9.4.1 |
| **Database** | MySQL | 8.x |
| **Migrations** | Flyway | 10.21.0 |
| **ORM** | Spring Data JPA / Hibernate | (validate mode) |
| **Auth** | Spring Security + JWT | jjwt 0.12.5 |
| **Password Hashing** | BCrypt | strength 12 |
| **Frontend** | Thymeleaf + Vanilla JS | Bootstrap 5.3.2 |
| **Charts** | Chart.js | (bundled via CDN) |
| **AI** | Groq API | LLaMA 3.1-8B Instant |
| **Testing** | JUnit 5 | Spring Boot Test |
| **Utilities** | Lombok, Jakarta Bean Validation | — |

### Key Dependencies (`build.gradle`)

```
spring-boot-starter-actuator
spring-boot-starter-data-jpa
spring-boot-starter-flyway
spring-boot-starter-security
spring-boot-starter-validation
spring-boot-starter-webmvc
spring-boot-starter-thymeleaf
flyway-mysql
mysql-connector-j
jjwt-api / jjwt-impl / jjwt-jackson (0.12.5)
lombok
```

---

## 3. Architecture Pattern

### Design Pattern: Controller → Service → Repository

```
com.project.lmrs/
├── LmrsApplication.java              # Application entry point
├── config/                            # Spring configuration beans
│   ├── SecurityConfig.java            # Spring Security filter chain
│   ├── JwtConfig.java                 # JWT secret & expiry properties
│   ├── GroqConfig.java                # Groq AI API configuration
│   ├── FlywayConfig.java              # Flyway migration settings
│   ├── WebMvcConfig.java              # View controller mappings
│   └── DataInitializer.java           # Seed data on startup (CommandLineRunner)
├── controller/                        # 36 controllers (23 REST + 13 Page)
│   ├── *Controller.java               # REST API controllers (@RestController)
│   └── *PageController.java           # Thymeleaf page controllers (@Controller)
├── dto/
│   ├── request/                       # 28 request DTOs with Jakarta Validation
│   └── response/                      # 29 response DTOs (10 nested inner classes)
├── entity/                            # 22 JPA entities
├── enums/                             # 10 enums
├── exception/                         # GlobalExceptionHandler + 3 custom exceptions
├── filter/                            # PartnerApiKeyFilter (API key auth + rate limiting)
├── repository/                        # 22 Spring Data JPA repositories
├── security/                          # JWT filter, token provider, user details, utils
└── service/                           # 20 service classes
```

### Key Design Patterns

| Pattern | Implementation |
|---------|---------------|
| **Multi-tenancy** | Every entity has a `tenant_id` FK. All queries filter by tenant. `SecurityUtils.getCurrentTenantId()` extracts tenant from JWT. |
| **Soft delete** | Entities use `is_deleted` + `deleted_at` (never physical deletes). Exceptions: `AuditLog`, `LoyaltyTransaction`, `PartnerApiUsage` are append-only. |
| **UUID primary keys** | All entities use `UUID` as PK (`char(36)` in MySQL, `GenerationType.UUID` in JPA). |
| **Audit timestamps** | `created_at` / `updated_at` managed by `@PrePersist` / `@PreUpdate` lifecycle callbacks. |
| **Stateless auth** | JWT access + refresh tokens. No HTTP sessions. CSRF disabled. |
| **Constructor injection** | All DI via Lombok `@RequiredArgsConstructor`. No `@Autowired` on fields. |
| **DTO boundaries** | Controllers never expose raw JPA entities. All responses go through Response DTOs. |

### Frontend Architecture

```
src/main/resources/
├── templates/
│   ├── login.html                     # Standalone login page
│   ├── register.html                  # Standalone registration page
│   ├── fragments/
│   │   └── sidebar.html               # Reusable sidebar navigation fragment
│   └── dashboard/
│       ├── dashboard.html             # Main dashboard with KPIs + charts
│       ├── guests.html                # Guest management
│       ├── rooms.html                 # Room management + availability
│       ├── reservations.html          # Hotel reservation management
│       ├── folios.html                # Folio & payment management
│       ├── orders.html                # Restaurant order management
│       ├── menu.html                  # Menu management (categories + items)
│       ├── tables.html                # Restaurant table management
│       ├── table-reservations.html    # Table reservation management
│       ├── housekeeping.html          # Housekeeping task management
│       ├── inventory.html             # Inventory management
│       ├── users.html                 # User management
│       ├── reports.html               # Reporting & analytics
│       ├── ai.html                    # AI assistant (3 tabs)
│       └── profile.html               # User profile & password change
├── static/
│   ├── css/dashboard.css              # Custom dashboard styles
│   ├── favicon.svg                    # Application favicon
│   └── js/                            # 17 JavaScript files
│       ├── auth.js                    # JWT auth module (used by all pages)
│       ├── toast.js                   # Toast notification utility
│       ├── dashboard.js               # Dashboard charts + KPIs
│       ├── guests.js                  # Guest CRUD + pagination
│       ├── rooms.js                   # Room CRUD + availability
│       ├── reservations.js            # Reservation CRUD + workflow
│       ├── folios.js                  # Folio charges + payments
│       ├── orders.js                  # Order CRUD + state machine
│       ├── menu.js                    # Menu category + item management
│       ├── tables.js                  # Restaurant table CRUD
│       ├── table-reservations.js      # Table reservation CRUD
│       ├── housekeeping.js            # Housekeeping task management
│       ├── inventory.js               # Inventory + stock adjustments
│       ├── users.js                   # User management
│       ├── reports.js                 # Report rendering + CSV export
│       ├── ai-assistant.js            # AI concierge + feedback + menu desc
│       └── profile.js                 # Profile display + password change
└── db/migration/                      # 7 Flyway SQL migrations (V1–V7)
```

---

## 4. Authentication & Security

### Authentication Flow

```
┌──────────┐     POST /api/v1/auth/login      ┌────────────┐
│  Client   │ ──────────────────────────────►  │ AuthController│
│           │  { email, password }             │             │
│           │ ◄──────────────────────────────  │             │
│           │  { accessToken, refreshToken,    │             │
│           │    userId, email, role, tenantId }│             │
└──────────┘                                   └────────────┘
       │
       │  Subsequent requests:
       │  Authorization: Bearer <accessToken>
       │
       ▼
┌────────────────────┐
│ JwtAuthenticationFilter │  (OncePerRequestFilter)
│ - Extracts Bearer token │
│ - Validates JWT signature│
│ - Loads UserDetails      │
│ - Sets SecurityContext   │
│ - Sets tenantId/userId   │
│   as request attributes  │
└────────────────────┘
```

### Token Configuration

| Token | Expiry | Claims |
|-------|--------|--------|
| **Access Token** | 15 minutes (900000ms) | `sub` (email), `userId`, `tenantId`, `role`, `tokenType="access"` |
| **Refresh Token** | 7 days (604800000ms) | `sub` (email), `tokenType="refresh"` |

### Password Security

- **Hashing:** BCrypt with strength 12
- **Minimum password length:** 8 characters (enforced via `@Size(min=8)` on DTOs)

### User Roles (9 total)

| Role | Description | Access Level |
|------|-------------|-------------|
| `SUPER_ADMIN` | System-wide administrator | All tenants, all endpoints, audit logs |
| `PROPERTY_ADMIN` | Property-level administrator | Own tenant, user management, all modules |
| `MANAGER` | Hotel/restaurant manager | Most modules, reports, AI tools |
| `FRONT_DESK` | Front desk receptionist | Reservations, guests, rooms, folios, payments |
| `HOUSEKEEPER` | Housekeeping staff | Housekeeping tasks (own assignments + view) |
| `SERVER` | Restaurant server | Orders, menu, tables, table reservations |
| `KITCHEN` | Kitchen staff | Orders (view + status updates), menu (view) |
| `FINANCE` | Finance/accounting staff | Folios, payments, inventory, reports |
| `GUEST` | External guest (limited) | — (reserved for future guest portal) |

### Security Configuration (`SecurityConfig`)

- **Sessions:** Stateless (no HTTP sessions)
- **CSRF:** Disabled (stateless API)
- **Filter order:** `JwtAuthenticationFilter` → `UsernamePasswordAuthenticationFilter`
- **Password encoder:** `BCryptPasswordEncoder(12)`

### Publicly Accessible Endpoints

| Path | Purpose |
|------|---------|
| `/login`, `/register` | Auth pages |
| `/dashboard/**` | All dashboard page renders |
| `/api/v1/auth/**` | Login, register, refresh tokens |
| `/api/v1/rooms/availability` | Room availability check |
| `/api/v1/menu/items`, `/api/v1/menu/categories` | Public menu listing |
| `/api/v1/partner/**` | Partner API (uses API key filter, not JWT) |
| Static assets (`/css/**`, `/js/**`, `/favicon.ico`) | Frontend resources |

### Protected Endpoints

- `/api/v1/ai/**` — Requires authentication (any role)
- All other `/api/v1/**` endpoints — Requires authentication + specific role via `@PreAuthorize`

---

## 5. Multi-Tenancy

### Isolation Pattern

Every entity (except `AuditLog`, `LoyaltyTransaction`, `PartnerApiUsage`) has a `tenant_id` foreign key referencing the `tenants` table. This ensures complete data isolation between properties.

```
┌──────────────────────────────────────────────────────────┐
│                    JWT Token Claims                       │
│  { tenantId: "abc-123", userId: "def-456", role: "..." } │
└──────────────────────────────────────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────────┐
│              JwtAuthenticationFilter                      │
│  - Extracts tenantId from JWT                             │
│  - Sets request.setAttribute("tenantId", tenantId)       │
└──────────────────────────────────────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────────┐
│                SecurityUtils (Utility)                    │
│  - getCurrentTenantId() → returns request.getAttribute() │
│  - getCurrentUserId()   → returns request.getAttribute() │
│  - getCurrentUserEmail()→ returns auth.getName()          │
└──────────────────────────────────────────────────────────┘
                          │
                          ▼
┌──────────────────────────────────────────────────────────┐
│                  Service Layer                            │
│  String tenantId = SecurityUtils.getCurrentTenantId();    │
│  repository.findByXAndTenant_TenantIdAndIsDeletedFalse() │
└──────────────────────────────────────────────────────────┘
```

### Tenant Entity

| Field | Type | Description |
|-------|------|-------------|
| `tenantId` | UUID (PK) | Unique tenant identifier |
| `name` | String | Display name (e.g., "Grand Horizon Hotel") |
| `subdomain` | String (unique) | URL subdomain identifier |
| `configJson` | JSON | Flexible configuration (timezone, currency, etc.) |
| `isActive` | boolean | Whether tenant is active |
| `isDeleted` | boolean | Soft delete flag |
| `createdAt` / `updatedAt` / `deletedAt` | LocalDateTime | Audit timestamps |

### Key Rule

**Every service method that reads or writes data must:**
1. Accept `tenantId` parameter (from `SecurityUtils.getCurrentTenantId()`)
2. Use tenant-filtered repository queries (e.g., `findByXAndTenant_TenantIdAndIsDeletedFalse`)
3. Never use plain `findById()` without tenant filtering

---

## 6. Database Schema

The database contains 22 tables across 7 Flyway migrations. Below is the complete schema organized by domain.

### Conventions

- **Primary keys:** All tables use `CHAR(36)` UUID primary keys with `DEFAULT UUID()`
- **Foreign keys:** All tenant-scoped tables have `tenant_id CHAR(36) NOT NULL FK → tenants(tenant_id)`
- **Soft delete:** Tables have `is_deleted TINYINT(1) DEFAULT 0` and `deleted_at DATETIME`
- **Audit timestamps:** `created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP`, `updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`
- **Enum storage:** Enums are stored as VARCHAR with `_enum` suffix (e.g., `status_enum`, `role_enum`)

### 6.1 Tenant & Users Domain (V1)

**`tenants`** — Root entity for multi-tenant isolation

| Column | Type | Constraints |
|--------|------|-------------|
| `tenant_id` | `CHAR(36)` | PK, DEFAULT UUID() |
| `name` | `VARCHAR(255)` | NOT NULL |
| `subdomain` | `VARCHAR(100)` | NOT NULL, UNIQUE |
| `config_json` | `JSON` | — |
| `is_active` | `TINYINT(1)` | NOT NULL, DEFAULT 1 |
| `created_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP |
| `updated_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP ON UPDATE |
| `deleted_at` | `DATETIME` | — |
| `is_deleted` | `TINYINT(1)` | NOT NULL, DEFAULT 0 |

**`users`** — System users with role-based access

| Column | Type | Constraints |
|--------|------|-------------|
| `user_id` | `CHAR(36)` | PK, DEFAULT UUID() |
| `tenant_id` | `CHAR(36)` | NOT NULL, FK → tenants |
| `email` | `VARCHAR(255)` | NOT NULL |
| `password_hash` | `VARCHAR(255)` | NOT NULL |
| `role_enum` | `VARCHAR(50)` | NOT NULL |
| `is_active` | `TINYINT(1)` | NOT NULL, DEFAULT 1 |
| `last_login` | `DATETIME` | — |
| Standard audit + soft delete columns | | |

**Indexes (V7):** `idx_users_email` (email), `idx_users_tenant_active` (tenant_id, is_deleted, is_active)

### 6.2 Guest & Room Domain (V2)

**`guests`** — Hotel guests with loyalty tracking

| Column | Type | Constraints |
|--------|------|-------------|
| `guest_id` | `CHAR(36)` | PK, DEFAULT UUID() |
| `tenant_id` | `CHAR(36)` | NOT NULL, FK → tenants |
| `first_name` | `VARCHAR(100)` | NOT NULL |
| `last_name` | `VARCHAR(100)` | NOT NULL |
| `email` | `VARCHAR(255)` | — |
| `phone` | `VARCHAR(50)` | — |
| `dob` | `DATE` | — |
| `nationality` | `VARCHAR(100)` | — |
| `id_doc_type` | `VARCHAR(50)` | — |
| `id_doc_number` | `VARCHAR(100)` | — |
| `loyalty_tier` | `VARCHAR(20)` | DEFAULT 'BRONZE' |
| Standard audit + soft delete | | |

**Indexes (V7):** `idx_guests_tenant_deleted` (tenant_id, is_deleted), `idx_guests_email_tenant` (email, tenant_id)

**`room_types`** — Room category definitions

| Column | Type | Constraints |
|--------|------|-------------|
| `room_type_id` | `CHAR(36)` | PK, DEFAULT UUID() |
| `tenant_id` | `CHAR(36)` | NOT NULL, FK → tenants |
| `name` | `VARCHAR(100)` | NOT NULL |
| `max_occupancy` | `INT` | NOT NULL |
| `description` | `TEXT` | — |
| `amenities_json` | `JSON` | — |
| Standard audit + soft delete | | |

**`rooms`** — Individual rooms with status tracking

| Column | Type | Constraints |
|--------|------|-------------|
| `room_id` | `CHAR(36)` | PK, DEFAULT UUID() |
| `tenant_id` | `CHAR(36)` | NOT NULL, FK → tenants |
| `room_number` | `VARCHAR(20)` | NOT NULL |
| `room_type_id` | `CHAR(36)` | NOT NULL, FK → room_types |
| `floor` | `INT` | — |
| `status_enum` | `VARCHAR(30)` | NOT NULL, DEFAULT 'AVAILABLE' |
| `housekeeping_status_enum` | `VARCHAR(30)` | NOT NULL, DEFAULT 'CLEAN' |
| `base_rate` | `DECIMAL(10,2)` | NOT NULL |
| Standard audit + soft delete | | |

### 6.3 Reservation & Billing Domain (V3)

**`reservations`** — Hotel room bookings

| Column | Type | Constraints |
|--------|------|-------------|
| `reservation_id` | `CHAR(36)` | PK, DEFAULT UUID() |
| `tenant_id` | `CHAR(36)` | NOT NULL, FK → tenants |
| `guest_id` | `CHAR(36)` | NOT NULL, FK → guests |
| `room_id` | `CHAR(36)` | NOT NULL, FK → rooms |
| `check_in_date` | `DATE` | NOT NULL |
| `check_out_date` | `DATE` | NOT NULL |
| `status_enum` | `VARCHAR(30)` | NOT NULL, DEFAULT 'PENDING' |
| `channel_enum` | `VARCHAR(30)` | NOT NULL, DEFAULT 'DIRECT' |
| `rate_applied` | `DECIMAL(10,2)` | NOT NULL |
| `special_requests` | `TEXT` | — |
| Standard audit + soft delete | | |

**Indexes (V7):** `idx_res_room_dates` (room_id, is_deleted, check_in_date, check_out_date), `idx_res_tenant_status` (tenant_id, status_enum, is_deleted), `idx_res_tenant_created` (tenant_id, created_at)

**`folios`** — Per-reservation billing folio

| Column | Type | Constraints |
|--------|------|-------------|
| `folio_id` | `CHAR(36)` | PK, DEFAULT UUID() |
| `reservation_id` | `CHAR(36)` | NOT NULL, FK → reservations (UNIQUE via @OneToOne) |
| `guest_id` | `CHAR(36)` | NOT NULL, FK → guests |
| `status_enum` | `VARCHAR(30)` | NOT NULL, DEFAULT 'OPEN' |
| `total_amount` | `DECIMAL(10,2)` | NOT NULL, DEFAULT 0.00 |
| `currency` | `CHAR(3)` | NOT NULL, DEFAULT 'INR' |
| Standard audit + soft delete | | |

**`folio_line_items`** — Individual charges on a folio

| Column | Type | Constraints |
|--------|------|-------------|
| `line_item_id` | `CHAR(36)` | PK, DEFAULT UUID() |
| `folio_id` | `CHAR(36)` | NOT NULL, FK → folios |
| `description` | `VARCHAR(255)` | NOT NULL |
| `amount` | `DECIMAL(10,2)` | NOT NULL |
| `charge_type_enum` | `VARCHAR(30)` | NOT NULL |
| `posted_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP |
| `posted_by_user_id` | `CHAR(36)` | FK → users (added V7) |
| Standard audit + soft delete | | |

**Indexes (V7):** `idx_folio_line_folio` (folio_id, is_deleted)

**`payments`** — Payments applied to folios

| Column | Type | Constraints |
|--------|------|-------------|
| `payment_id` | `CHAR(36)` | PK, DEFAULT UUID() |
| `folio_id` | `CHAR(36)` | NOT NULL, FK → folios |
| `amount` | `DECIMAL(10,2)` | NOT NULL |
| `currency` | `CHAR(3)` | NOT NULL, DEFAULT 'INR' |
| `method_enum` | `VARCHAR(30)` | NOT NULL |
| `gateway_ref` | `VARCHAR(255)` | — |
| `status_enum` | `VARCHAR(30)` | NOT NULL, DEFAULT 'PENDING' |
| `paid_at` | `DATETIME` | — |
| Standard audit + soft delete | | |

**Indexes (V7):** `idx_payments_folio` (folio_id, is_deleted), `idx_payments_folio_status` (folio_id, is_deleted, status_enum)

### 6.4 Restaurant Domain (V4)

**`restaurant_tables`** — Physical restaurant tables

| Column | Type | Constraints |
|--------|------|-------------|
| `table_id` | `CHAR(36)` | PK, DEFAULT UUID() |
| `tenant_id` | `CHAR(36)` | NOT NULL, FK → tenants |
| `table_number` | `VARCHAR(20)` | NOT NULL |
| `zone_enum` | `VARCHAR(30)` | — |
| `capacity` | `INT` | NOT NULL |
| `status_enum` | `VARCHAR(30)` | NOT NULL, DEFAULT 'AVAILABLE' |
| `position_x` / `position_y` | `DECIMAL(8,2)` | — |
| Standard audit + soft delete | | |

**`table_reservations`** — Restaurant table bookings

| Column | Type | Constraints |
|--------|------|-------------|
| `table_res_id` | `CHAR(36)` | PK, DEFAULT UUID() |
| `tenant_id` | `CHAR(36)` | NOT NULL, FK → tenants |
| `guest_id` | `CHAR(36)` | FK → guests (nullable for walk-ins) |
| `table_id` | `CHAR(36)` | NOT NULL, FK → restaurant_tables |
| `party_size` | `INT` | NOT NULL |
| `reservation_dt` | `DATETIME` | NOT NULL |
| `status_enum` | `VARCHAR(30)` | NOT NULL, DEFAULT 'PENDING' |
| `special_notes` | `TEXT` | — |
| Standard audit + soft delete | | |

**`menu_categories`** — Menu category grouping

| Column | Type | Constraints |
|--------|------|-------------|
| `category_id` | `CHAR(36)` | PK, DEFAULT UUID() |
| `tenant_id` | `CHAR(36)` | NOT NULL, FK → tenants |
| `name` | `VARCHAR(100)` | NOT NULL |
| `display_order` | `INT` | DEFAULT 0 |
| `is_active` | `TINYINT(1)` | NOT NULL, DEFAULT 1 |
| Standard audit + soft delete | | |

**`menu_items`** — Individual menu items

| Column | Type | Constraints |
|--------|------|-------------|
| `item_id` | `CHAR(36)` | PK, DEFAULT UUID() |
| `tenant_id` | `CHAR(36)` | NOT NULL, FK → tenants |
| `category_id` | `CHAR(36)` | NOT NULL, FK → menu_categories |
| `name` | `VARCHAR(255)` | NOT NULL |
| `description` | `TEXT` | — |
| `base_price` | `DECIMAL(10,2)` | NOT NULL |
| `allergens_json` | `JSON` | — |
| `dietary_flags_json` | `JSON` | — |
| `is_available` | `TINYINT(1)` | NOT NULL, DEFAULT 1 |
| Standard audit + soft delete | | |

**Indexes (V7):** `idx_menu_items_tenant` (tenant_id, is_deleted)

**`menu_item_variants`** — Size/option variants for menu items

| Column | Type | Constraints |
|--------|------|-------------|
| `variant_id` | `CHAR(36)` | PK, DEFAULT UUID() |
| `item_id` | `CHAR(36)` | NOT NULL, FK → menu_items |
| `name` | `VARCHAR(100)` | NOT NULL |
| `price_modifier` | `DECIMAL(10,2)` | NOT NULL, DEFAULT 0.00 |
| Standard audit + soft delete | | |

### 6.5 Operations Domain (V5)

**`orders`** — Restaurant food orders

| Column | Type | Constraints |
|--------|------|-------------|
| `order_id` | `CHAR(36)` | PK, DEFAULT UUID() |
| `tenant_id` | `CHAR(36)` | NOT NULL, FK → tenants |
| `table_id` | `CHAR(36)` | FK → restaurant_tables |
| `server_user_id` | `CHAR(36)` | FK → users (added V7) |
| `guest_id` | `CHAR(36)` | FK → guests (added V7) |
| `status_enum` | `VARCHAR(30)` | NOT NULL, DEFAULT 'OPEN' |
| `opened_at` / `closed_at` | `DATETIME` | — |
| `total_amount` | `DECIMAL(10,2)` | NOT NULL, DEFAULT 0.00 |
| Standard audit + soft delete | | |

**Indexes (V7):** `idx_orders_tenant_status` (tenant_id, status_enum, is_deleted)

**`order_items`** — Line items within orders

| Column | Type | Constraints |
|--------|------|-------------|
| `order_item_id` | `CHAR(36)` | PK, DEFAULT UUID() |
| `order_id` | `CHAR(36)` | NOT NULL, FK → orders |
| `item_id` | `CHAR(36)` | NOT NULL, FK → menu_items |
| `variant_id` | `CHAR(36)` | FK → menu_item_variants (added V7) |
| `quantity` | `INT` | NOT NULL, DEFAULT 1 |
| `unit_price` | `DECIMAL(10,2)` | NOT NULL |
| `modifiers_json` | `JSON` | — |
| `status_enum` | `VARCHAR(30)` | NOT NULL, DEFAULT 'PENDING' |
| `fired_at` / `completed_at` | `DATETIME` | — |
| Standard audit + soft delete | | |

**Indexes (V7):** `idx_order_items_order` (order_id, is_deleted)

**`inventory_items`** — Kitchen/restaurant inventory

| Column | Type | Constraints |
|--------|------|-------------|
| `inventory_id` | `CHAR(36)` | PK, DEFAULT UUID() |
| `tenant_id` | `CHAR(36)` | NOT NULL, FK → tenants |
| `name` | `VARCHAR(255)` | NOT NULL |
| `unit_enum` | `VARCHAR(30)` | NOT NULL |
| `current_stock` | `DECIMAL(10,3)` | NOT NULL, DEFAULT 0 |
| `reorder_threshold` | `DECIMAL(10,3)` | NOT NULL, DEFAULT 0 |
| `cost_per_unit` | `DECIMAL(10,2)` | NOT NULL, DEFAULT 0.00 |
| Standard audit + soft delete | | |

**Indexes (V7):** `idx_inv_tenant_deleted` (tenant_id, is_deleted)

**`housekeeping_tasks`** — Room cleaning/maintenance tasks

| Column | Type | Constraints |
|--------|------|-------------|
| `task_id` | `CHAR(36)` | PK, DEFAULT UUID() |
| `tenant_id` | `CHAR(36)` | NOT NULL, FK → tenants |
| `room_id` | `CHAR(36)` | NOT NULL, FK → rooms |
| `assigned_user_id` | `CHAR(36)` | FK → users (added V7) |
| `task_type_enum` | `VARCHAR(50)` | NOT NULL |
| `status_enum` | `VARCHAR(30)` | NOT NULL, DEFAULT 'PENDING' |
| `priority_enum` | `VARCHAR(20)` | NOT NULL, DEFAULT 'NORMAL' |
| `scheduled_date` | `DATE` | NOT NULL |
| `completed_at` | `DATETIME` | — |
| Standard audit + soft delete | | |

**Indexes (V7):** `idx_hk_tenant_date` (tenant_id, scheduled_date, is_deleted)

**`loyalty_transactions`** — Append-only loyalty points ledger

| Column | Type | Constraints |
|--------|------|-------------|
| `loyalty_tx_id` | `CHAR(36)` | PK, DEFAULT UUID() |
| `guest_id` | `CHAR(36)` | NOT NULL, FK → guests |
| `transaction_type_enum` | `VARCHAR(30)` | NOT NULL |
| `points` | `INT` | NOT NULL |
| `reference_id` | `CHAR(36)` | — |
| `reference_type_enum` | `VARCHAR(50)` | — |
| `created_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP |

**No soft delete** — Loyalty transactions are append-only.

**`audit_logs`** — Append-only audit trail for all module changes

| Column | Type | Constraints |
|--------|------|-------------|
| `log_id` | `CHAR(36)` | PK, DEFAULT UUID() |
| `tenant_id` | `CHAR(36)` | FK → tenants (added V7) |
| `user_id` | `CHAR(36)` | FK → users (added V7) |
| `action` | `VARCHAR(100)` | NOT NULL |
| `entity_type` | `VARCHAR(100)` | NOT NULL |
| `entity_id` | `CHAR(36)` | — |
| `old_value_json` | `JSON` | — |
| `new_value_json` | `JSON` | — |
| `ip_address` | `VARCHAR(45)` | — |
| `created_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP |

**No soft delete** — Audit logs are append-only, immutable.

### 6.6 Partner API Domain (V6)

**`partner_accounts`** — External partner integrations (e.g., Zomato, Swiggy, OTA)

| Column | Type | Constraints |
|--------|------|-------------|
| `partner_id` | `CHAR(36)` | PK, DEFAULT UUID() |
| `tenant_id` | `CHAR(36)` | NOT NULL, FK → tenants |
| `name` | `VARCHAR(255)` | NOT NULL |
| `provider_type` | `VARCHAR(50)` | NOT NULL (ZOMATO, SWIGGY, CUSTOM) |
| `api_key` | `VARCHAR(64)` | NOT NULL, UNIQUE |
| `is_active` | `TINYINT(1)` | NOT NULL, DEFAULT 1 |
| Standard audit + soft delete | | |

**`partner_api_usage`** — Append-only API usage logging

| Column | Type | Constraints |
|--------|------|-------------|
| `usage_id` | `CHAR(36)` | PK, DEFAULT UUID() |
| `partner_id` | `CHAR(36)` | NOT NULL, FK → partner_accounts |
| `endpoint` | `VARCHAR(255)` | NOT NULL |
| `http_method` | `VARCHAR(10)` | NOT NULL |
| `status_code` | `INT` | NOT NULL |
| `response_ms` | `INT` | NOT NULL, DEFAULT 0 |
| `created_at` | `DATETIME` | NOT NULL, DEFAULT CURRENT_TIMESTAMP |

**Indexes (V7):** `idx_partner_usage_partner` (partner_id, created_at)

---

## 7. Feature Modules

### 7.1 Authentication Module

**Controllers:** `AuthController` (REST), `UserPageController`, `ProfileController` (REST)

**Services:** `AuthService`, `CustomUserDetailsService`

**Key Endpoints:**

| Method | Path | Purpose |
|--------|------|---------|
| POST | `/api/v1/auth/login` | Authenticate user, return JWT tokens |
| POST | `/api/v1/auth/register` | Self-register FRONT_DESK role user |
| POST | `/api/v1/auth/refresh` | Refresh expired access token |

**Login Flow:**
1. User submits email + password → `POST /api/v1/auth/login`
2. `AuthService.login()` authenticates via `AuthenticationManager`
3. On success: generates access token (15min) + refresh token (7 days)
4. Returns `AuthResponse` with tokens, userId, email, role, tenantId
5. Client stores tokens in `localStorage` via `Auth.setTokens()`

**Registration Flow:**
1. User submits tenantId, email, password, role → `POST /api/v1/auth/register`
2. `AuthService.register()` validates unique email, creates user with BCrypt password
3. Auto-logs in: returns JWT tokens immediately

**Token Refresh Flow:**
1. Client detects 401 → calls `Auth.refreshAccessToken()` in `auth.js`
2. Sends refresh token → `POST /api/v1/auth/refresh`
3. `AuthService.refreshToken()` validates refresh token, issues new token pair
4. If refresh fails: `Auth.logout()` clears storage, redirects to `/login`

**Password Change:**
- `PUT /api/v1/profile/password` — Requires current password + new password
- Validates current password against hash via `PasswordEncoder.matches()`

### 7.2 Tenant Management

**Controllers:** `TenantController` (REST)

**Services:** `TenantService`

**Access:** `SUPER_ADMIN` only

**Endpoints:**

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/v1/tenants` | List all tenants |
| GET | `/api/v1/tenants/{tenantId}` | Get tenant by ID |
| POST | `/api/v1/tenants` | Create tenant (validates unique subdomain) |
| PUT | `/api/v1/tenants/{tenantId}` | Update tenant name/config |
| DELETE | `/api/v1/tenants/{tenantId}` | Soft-delete tenant |

**Business Rules:**
- Subdomain must be unique across all tenants
- Tenants are soft-deleted (never physically removed)
- Config is stored as flexible JSON map (timezone, currency, etc.)

### 7.3 User Management

**Controllers:** `UserController` (REST), `UserPageController` (Page)

**Services:** `UserService`

**Access:** `MANAGER` can view users; `PROPERTY_ADMIN` / `SUPER_ADMIN` can create/edit/delete

**Endpoints:**

| Method | Path | Purpose | Roles |
|--------|------|---------|-------|
| GET | `/api/v1/users` | List all users for tenant | MANAGER+ |
| GET | `/api/v1/users/{userId}` | Get user by ID | MANAGER+ |
| POST | `/api/v1/users` | Create user | PROPERTY_ADMIN+ |
| PUT | `/api/v1/users/{userId}` | Update email/role | PROPERTY_ADMIN+ |
| PUT | `/api/v1/users/{userId}/role` | Update just the role | PROPERTY_ADMIN+ |
| PUT | `/api/v1/users/{userId}/toggle-active` | Toggle active/inactive | PROPERTY_ADMIN+ |
| DELETE | `/api/v1/users/{userId}` | Soft-delete user | PROPERTY_ADMIN+ |

**Business Rules:**
- Email must be unique per tenant
- Users can be toggled active/inactive without being deleted
- Self-deletion is not prevented but discouraged
- Password cannot be changed by admin — only by user via profile

### 7.4 Guest Management

**Controllers:** `GuestController` (REST), `GuestPageController` (Page)

**Services:** `GuestService`

**Access:** `FRONT_DESK`+ can view/create/update; `MANAGER`+ can delete

**Endpoints:**

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/v1/guests` | List all guests |
| GET | `/api/v1/guests/{guestId}` | Get guest by ID |
| GET | `/api/v1/guests/search?lastName=` | Search by last name |
| GET | `/api/v1/guests/page?page=&size=&sortBy=&sortDir=` | Paginated list |
| POST | `/api/v1/guests` | Create guest |
| PUT | `/api/v1/guests/{guestId}` | Update guest |
| DELETE | `/api/v1/guests/{guestId}` | Soft-delete guest |

**Guest Fields:** firstName, lastName, email, phone, DOB, nationality, ID document type/number
**Loyalty Tiers:** BRONZE (default), SILVER, GOLD, PLATINUM

### 7.5 Room Management

**Controllers:** `RoomController` (REST), `RoomPageController` (Page)

**Services:** `RoomService`

**Endpoints:**

| Method | Path | Purpose | Roles |
|--------|------|---------|-------|
| GET | `/api/v1/rooms` | List all rooms | FRONT_DESK+ |
| GET | `/api/v1/rooms/{roomId}` | Get room by ID | FRONT_DESK+ |
| GET | `/api/v1/rooms/types` | List room types | FRONT_DESK+ |
| GET | `/api/v1/rooms/availability?checkIn=&checkOut=` | Available rooms | FRONT_DESK+ |
| POST | `/api/v1/rooms` | Create room | MANAGER+ |
| PUT | `/api/v1/rooms/{roomId}` | Update room | MANAGER+ |
| DELETE | `/api/v1/rooms/{roomId}` | Soft-delete room | MANAGER+ |

**Room Status State Machine:**

```
AVAILABLE ◄──── OCCUPIED ◄─── RESERVED
    ▲               │              │
    │               ▼              │
    └─────── MAINTENANCE ──────────┘
                    │
                    ▼
            OUT_OF_ORDER
```

**Housekeeping Status States:** `CLEAN` → `DIRTY` → `IN_PROGRESS` → `INSPECTED` → `CLEAN`

**Availability Check:**
- Excludes rooms with existing reservations that overlap with requested dates
- Excludes rooms with status `MAINTENANCE` or `OUT_OF_ORDER`
- Returns room number, type, floor, base rate, amenities

### 7.6 Reservation Management

**Controllers:** `ReservationController` (REST), `ReservationPageController` (Page)

**Services:** `ReservationService`

**Access:** `FRONT_DESK`+

**Endpoints:**

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/v1/reservations?status=&guestName=&roomNumber=&dateFrom=&dateTo=` | List/search |
| GET | `/api/v1/reservations/{reservationId}` | Get by ID |
| POST | `/api/v1/reservations` | Create reservation |
| PUT | `/api/v1/reservations/{reservationId}` | Update reservation |
| PUT | `/api/v1/reservations/{reservationId}/check-in` | Check-in |
| PUT | `/api/v1/reservations/{reservationId}/check-out` | Check-out |
| PUT | `/api/v1/reservations/{reservationId}/no-show` | Mark no-show |
| PUT | `/api/v1/reservations/{reservationId}/cancel` | Cancel reservation |

**Reservation Status State Machine:**

```
PENDING ──► CONFIRMED ──► CHECKED_IN ──► CHECKED_OUT
   │            │                               ▲
   │            ▼                               │
   └────► CANCELLED ───────► NO_SHOW ───────────┘
```

**Check-In Flow:**
1. Validates reservation status is `CONFIRMED`
2. Sets room status to `OCCUPIED`
3. Auto-creates a folio for billing

**Check-Out Flow:**
1. Validates reservation status is `CHECKED_IN`
2. Sets room status to `AVAILABLE`
3. Marks room housekeeping status to `DIRTY`
4. Creates housekeeping task

**Conflict Detection:**
- Prevents double-booking by checking overlapping dates for the same room
- Excludes CANCELLED, NO_SHOW, CHECKED_OUT reservations from conflict check

**Booking Channels (7):** DIRECT, BOOKING_COM, EXPEDIA, AIRBNB, PHONE, WALK_IN, CORPORATE

### 7.7 Folio & Billing Module

**Controllers:** `FolioController` (REST), `FolioPageController` (Page), `PaymentController` (REST)

**Services:** `FolioService`, `PaymentService`

**Folio Endpoints:**

| Method | Path | Purpose | Roles |
|--------|------|---------|-------|
| GET | `/api/v1/folios/{folioId}` | Get folio by ID | FRONT_DESK+ |
| GET | `/api/v1/folios/reservation/{reservationId}` | Get folio by reservation | FRONT_DESK+ |
| POST | `/api/v1/folios/reservation/{reservationId}` | Create folio | FRONT_DESK+ |
| POST | `/api/v1/folios/{folioId}/charges` | Post charge to folio | FRONT_DESK+, SERVER |
| PUT | `/api/v1/folios/{folioId}/close` | Close folio | FINANCE+ |

**Payment Endpoints:**

| Method | Path | Purpose | Roles |
|--------|------|---------|-------|
| GET | `/api/v1/payments/folio/{folioId}` | List folio payments | FRONT_DESK+ |
| POST | `/api/v1/payments/folio/{folioId}` | Process payment | FRONT_DESK+ |

**Charge Types (8):** ROOM_CHARGE, RESTAURANT, MINIBAR, INCIDENTAL, FEE, TAX, DISCOUNT, REFUND

**Payment Methods (6):** CASH, CARD, MOBILE_WALLET, ROOM_CHARGE, CORPORATE_ACCOUNT, VOUCHER

**Payment Processing Flow:**
1. Validates folio is OPEN
2. Validates payment amount does not exceed folio balance
3. If payment equals or exceeds balance → auto-closes folio (sets status to CLOSED)
4. Earns loyalty points for the guest (1 point per $1 spent)
5. Creates audit log entry

### 7.8 Restaurant Table Management

**Controllers:** `RestaurantTableController` (REST), `TablePageController` (Page)

**Services:** `RestaurantTableService`

**Endpoints:**

| Method | Path | Purpose | Roles |
|--------|------|---------|-------|
| GET | `/api/v1/tables` | List all tables | SERVER+ |
| GET | `/api/v1/tables/available` | Available tables | FRONT_DESK+ |
| GET | `/api/v1/tables/{tableId}` | Get table by ID | SERVER+ |
| POST | `/api/v1/tables` | Create table | MANAGER+ |
| PUT | `/api/v1/tables/{tableId}` | Update table | MANAGER+ |
| DELETE | `/api/v1/tables/{tableId}` | Soft-delete table | MANAGER+ |

**Table Fields:** tableNumber, zone (Main Hall, Terrace, Private), capacity, status, positionX/Y (floor plan mapping)

### 7.9 Table Reservation Management

**Controllers:** `TableReservationController` (REST), `TablePageController` (Page)

**Services:** `TableReservationService`

**Endpoints:**

| Method | Path | Purpose | Roles |
|--------|------|---------|-------|
| GET | `/api/v1/table-reservations` | List all | SERVER+ |
| GET | `/api/v1/table-reservations/{tableResId}` | Get by ID | SERVER+ |
| POST | `/api/v1/table-reservations` | Create | FRONT_DESK+ |
| PUT | `/api/v1/table-reservations/{tableResId}` | Update | FRONT_DESK+ |
| DELETE | `/api/v1/table-reservations/{tableResId}` | Cancel | FRONT_DESK+ |

**Business Rules:**
- Party size limited to 1–20 guests
- 2-hour conflict window: prevents double-booking a table within ±2 hours of an existing reservation
- Guest is optional (supports walk-in reservations without a guest profile)

### 7.10 Menu Management

**Controllers:** `MenuController` (REST), `MenuPageController` (Page)

**Services:** `MenuService`

**Category Endpoints:**

| Method | Path | Purpose | Roles |
|--------|------|---------|-------|
| GET | `/api/v1/menu/categories` | List categories | SERVER+ |
| POST | `/api/v1/menu/categories` | Create category | MANAGER+ |
| PUT | `/api/v1/menu/categories/{categoryId}` | Update category | MANAGER+ |
| DELETE | `/api/v1/menu/categories/{categoryId}` | Delete category | MANAGER+ |

**Item Endpoints:**

| Method | Path | Purpose | Roles |
|--------|------|---------|-------|
| GET | `/api/v1/menu/items` | List items | SERVER+ |
| GET | `/api/v1/menu/items/{itemId}` | Get item by ID | SERVER+ |
| POST | `/api/v1/menu/items` | Create item | MANAGER+ |
| PUT | `/api/v1/menu/items/{itemId}` | Update item | MANAGER+ |
| DELETE | `/api/v1/menu/items/{itemId}` | Delete item | MANAGER+ |
| POST | `/api/v1/menu/items/{itemId}/variants` | Add variant | MANAGER+ |
| DELETE | `/api/v1/menu/variants/{variantId}` | Delete variant | MANAGER+ |

**Data Structure:**
- Menu items belong to one category
- Items have allergens and dietary flags stored as JSON arrays
- Items can have multiple variants (e.g., "Small", "Large") with price modifiers
- Items can be toggled available/unavailable

### 7.11 Order Management

**Controllers:** `OrderController` (REST), `OrderPageController` (Page)

**Services:** `OrderService`

**Endpoints:**

| Method | Path | Purpose | Roles |
|--------|------|---------|-------|
| GET | `/api/v1/orders` | List all orders | SERVER+ |
| GET | `/api/v1/orders/page?page=&size=&sortBy=&sortDir=` | Paginated | SERVER+ |
| GET | `/api/v1/orders/status/{status}` | Filter by status | SERVER+, KITCHEN |
| GET | `/api/v1/orders/{orderId}` | Get by ID | SERVER+, KITCHEN |
| POST | `/api/v1/orders` | Create order | SERVER+, FRONT_DESK |
| PUT | `/api/v1/orders/{orderId}/status?status=` | Update status | SERVER+, KITCHEN |
| PUT | `/api/v1/orders/{orderId}/void` | Void order | MANAGER+ |

**Order Status State Machine (validated transitions):**

```
OPEN ──► SUBMITTED ──► IN_KITCHEN ──► READY ──► SERVED ──► CLOSED
  │          │              │           │          │
  └──────────┴──────────────┴───────────┴──────────┘
                         │
                         ▼
                     VOIDED
```

- VOIDED can only be set from any non-CLOSED status
- CLOSED is terminal — cannot be further modified
- Order total is auto-calculated from line items on creation

**Order Item Status States:** PENDING → FIRED → IN_PROGRESS → DONE → VOIDED

### 7.12 Inventory Management

**Controllers:** `InventoryController` (REST), `InventoryPageController` (Page)

**Services:** `InventoryService`

**Endpoints:**

| Method | Path | Purpose | Roles |
|--------|------|---------|-------|
| GET | `/api/v1/inventory` | List all items | FINANCE+ |
| GET | `/api/v1/inventory/low-stock` | Low stock items | FINANCE+ |
| GET | `/api/v1/inventory/{inventoryId}` | Get item by ID | FINANCE+ |
| POST | `/api/v1/inventory` | Create item | MANAGER+ |
| PUT | `/api/v1/inventory/{inventoryId}` | Update item | MANAGER+ |
| PUT | `/api/v1/inventory/{inventoryId}/stock` | Adjust stock | MANAGER+ |
| DELETE | `/api/v1/inventory/{inventoryId}` | Delete item | MANAGER+ |

**Stock Adjustment:**
- Positive quantity = add stock
- Negative quantity = deduct stock (validates sufficient balance)
- Requires reason text for audit logging
- All stock changes are audit-logged with old/new values

**Low Stock Detection:**
- Threshold: `currentStock <= reorderThreshold`
- Returns items needing reorder with current levels and thresholds

### 7.13 Housekeeping Management

**Controllers:** `HousekeepingController` (REST), `HousekeepingPageController` (Page)

**Services:** `HousekeepingService`

**Endpoints:**

| Method | Path | Purpose | Roles |
|--------|------|---------|-------|
| GET | `/api/v1/housekeeping?date=` | Tasks for date | HOUSEKEEPER+ |
| GET | `/api/v1/housekeeping/my?date=` | My assigned tasks | HOUSEKEEPER |
| GET | `/api/v1/housekeeping/{taskId}` | Task by ID | HOUSEKEEPER+ |
| POST | `/api/v1/housekeeping` | Create task | MANAGER+ |
| PUT | `/api/v1/housekeeping/{taskId}` | Update task | HOUSEKEEPER+ |
| DELETE | `/api/v1/housekeeping/{taskId}` | Delete task | MANAGER+ |

**Task Priorities:** LOW, NORMAL, HIGH, URGENT

**Task Types:** Clean, Inspection, Turndown, Maintenance (and more via free-text)

**Auto-Creation:** When a guest checks out, the system automatically:
1. Sets room housekeeping status to `DIRTY`
2. Creates a housekeeping task with type "Clean" assigned to no one, priority "NORMAL"

### 7.14 Loyalty Program

**Controllers:** `LoyaltyController` (REST)

**Services:** `LoyaltyService`

**Endpoints:**

| Method | Path | Purpose | Roles |
|--------|------|---------|-------|
| GET | `/api/v1/loyalty/guest/{guestId}/balance` | Points balance | FRONT_DESK+ |
| GET | `/api/v1/loyalty/guest/{guestId}/history` | Transaction history | FRONT_DESK+ |
| POST | `/api/v1/loyalty/guest/{guestId}/redeem` | Redeem points | FRONT_DESK+ |

**Loyalty Tiers:**

| Tier | Description |
|------|-------------|
| BRONZE | Default tier (entry-level) |
| SILVER | 1,000+ points |
| GOLD | 5,000+ points |
| PLATINUM | 10,000+ points |

**Earning Points:**
- Points are earned automatically on payment processing (1 point per $1)
- `LoyaltyService.earnPoints()` recalculates the guest's tier after each earn transaction
- Tier is recalculated based on total points balance

**Redeeming Points:**
- Validates sufficient balance before redemption
- Cannot result in negative balance
- Tracks reference ID/type for auditing (e.g., linking to the redeemed item/service)

### 7.15 Reporting & Analytics

**Controllers:** `ReportController` (REST), `ReportPageController` (Page)

**Services:** `DashboardService`, `ReportController` delegates to repositories

**Access:** `MANAGER`+, some reports `FINANCE`+

**Report Endpoints:**

| Method | Path | Purpose | Roles |
|--------|------|---------|-------|
| GET | `/api/v1/reports/revenue?from=&to=` | Revenue by date range | MANAGER+, FINANCE |
| GET | `/api/v1/reports/occupancy` | Occupancy breakdown | MANAGER+ |
| GET | `/api/v1/reports/bookings-by-channel` | Channels distribution | MANAGER+ |
| GET | `/api/v1/reports/top-menu-items` | Most ordered items | MANAGER+ |
| GET | `/api/v1/reports/housekeeping-summary` | HK status breakdown | MANAGER+ |
| GET | `/api/v1/reports/export/revenue?from=&to=` | Revenue CSV export | MANAGER+, FINANCE |
| GET | `/api/v1/reports/low-stock` | Low stock alerts | MANAGER+, FINANCE |

**Dashboard Summary** (`GET /api/v1/dashboard/summary`):
Returns a comprehensive payload with:
- **KPIs:** totalGuests, totalRooms, activeReservations, totalRevenue, occupancyRate, pendingHousekeeping
- **Charts Data:** reservation statuses, booking channels, revenue trend (30 days), room statuses, rooms by floor, rooms by type, housekeeping statuses, top menu items
- **Alerts:** low stock items, today's housekeeping progress

### 7.16 AI Concierge (Groq Integration)

**Controllers:** `AiController` (REST), `AiPageController` (Page)

**Services:** `AiConciergeService`, `GroqAiService`

**Configuration:**
- API URL: `https://api.groq.com/openai/v1/chat/completions`
- Model: `llama-3.1-8b-instant`
- Max tokens: 1024
- Temperature: 0.7

**Three AI Tools:**

**1. AI Concierge** (`POST /api/v1/ai/concierge`)
- Staff asks a question about a guest/reservation
- System builds context from guest profile (name, loyalty tier) and reservation details (status, dates, room)
- Uses a system prompt describing available hotel services (spa, pool, gym, room service, valet parking, business center)
- Returns AI-generated reply with model name and token usage

**2. Feedback Analysis** (`POST /api/v1/ai/feedback-analyze`)
- Analyzes guest feedback text
- Returns: sentiment (POSITIVE/NEGATIVE/NEUTRAL), one-sentence summary, suggested manager reply
- Uses structured output format extracted via regex

**3. Menu Description Generator** (`POST /api/v1/ai/menu-description`)
- Generates appealing menu descriptions for menu items
- Input: item name, category, ingredients, dietary flags, base price
- Returns: evocative description under 60 words

### 7.17 Partner API Integration

**Controllers:** `PartnerRoomController`, `PartnerRestaurantController`, `PartnerDashboardController`

**Filter:** `PartnerApiKeyFilter`

**Services:** `PartnerService`

**Authentication:**
- Uses `X-API-Key` header (NOT JWT)
- Validated via `PartnerApiKeyFilter`
- Key checked against `partner_accounts` table (must be active + not deleted)

**Rate Limiting:**
- 100 requests per minute per API key
- In-memory sliding window via `ConcurrentHashMap`
- Returns HTTP 429 when exceeded

**Partner Endpoints:**

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/v1/partner/rooms` | List available rooms |
| POST | `/api/v1/partner/rooms/reservations` | Create room reservation |
| PUT | `/api/v1/partner/rooms/reservations/{id}` | Update reservation |
| DELETE | `/api/v1/partner/rooms/reservations/{id}` | Cancel reservation |
| GET | `/api/v1/partner/restaurant/menu` | List menu items |
| POST | `/api/v1/partner/restaurant/reservations` | Create table reservation |
| PUT | `/api/v1/partner/restaurant/reservations/{id}` | Update table reservation |
| DELETE | `/api/v1/partner/restaurant/reservations/{id}` | Cancel table reservation |
| GET | `/api/v1/partner/dashboard/{partnerId}` | Usage dashboard (internal) |

**Usage Logging:**
Every partner API call is logged to `partner_api_usage` with:
- Endpoint, HTTP method, status code, response time
- Provides data for usage dashboard and analytics

### 7.18 Audit Logging

**Controllers:** `AuditLogController` (REST)

**Services:** `AuditLogService`

**Access:** `SUPER_ADMIN` only

**Endpoints:**

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/v1/audit/entity/{entityType}/{entityId}` | Audit trail for entity |
| GET | `/api/v1/audit/user/{userId}` | Audit trail for user |

**Audit Log Fields:**
- `tenantId`, `userId` — Who performed the action
- `action` — What was done (CREATE, UPDATE, DELETE, STOCK_ADJUST, etc.)
- `entityType`, `entityId` — What was affected
- `oldValue`, `newValue` — JSON snapshots of before/after state
- `ipAddress` — Client IP

**Modules That Log Audits:**
- Inventory (create, update, stock adjustment, delete)
- Housekeeping (update task)
- Payments (process payment)

### 7.19 Profile

**Controllers:** `ProfileController` (REST)

**Services:** (Uses `AuthService` for password changes)

**Endpoints:**

| Method | Path | Purpose | Roles |
|--------|------|---------|-------|
| GET | `/api/v1/profile` | Get current user's profile | Any authenticated |
| PUT | `/api/v1/profile/password` | Change password | Any authenticated |

**Profile Data:** userId, email (no sensitive data exposed)

---

## 8. Frontend Pages

### 8.1 Authentication Pages

**Login** (`/login`):
- Dark-themed centered card with email/password form
- Show/hide password toggle via eye icon
- Link to registration page
- On success: stores tokens + user data, redirects to `/dashboard`

**Register** (`/register`):
- Green-themed centered card
- Collects: tenant ID, email, password (hidden for registration - hardcoded to FRONT_DESK role)
- On success: stores tokens + user data, redirects to `/dashboard`

### 8.2 Dashboard Pages (all use sidebar fragment)

All dashboard pages include:
- **Sidebar:** 14 navigation links (Dashboard, AI Assistant, Guests, Rooms, Reservations, Folios, Orders, Menu, Tables, Table Reservations, Housekeeping, Inventory, Users, Reports) + Profile link + user badge + logout
- **Toast notifications** (`showToast(message, type)`) for user feedback
- **Auth checks:** `Auth.requireAuth()` on page load, `Auth.authFetch()` for all API calls

**1. Dashboard** (`/dashboard/dashboard`):
- 6 KPI cards: Total Guests, Total Rooms, Checked In, Occupancy %, Revenue, Pending Housekeeping
- 7 Chart.js charts: Revenue Trend (line), Reservation Status (doughnut), Booking Channels (pie), Room Status (doughnut), Rooms by Type (horizontal bar), Top Menu Items (horizontal bar), Housekeeping Status (doughnut)
- Low Stock Alerts table

**2. Rooms** (`/dashboard/rooms`):
- Room table: number, type, floor, status, housekeeping status, base rate
- Availability checker with date range picker
- Add/Edit modal: room number, type dropdown, floor, base rate
- Delete confirmation modal

**3. Guests** (`/dashboard/guests`):
- Paginated table with name, email, phone, nationality, loyalty tier
- Search by last name with auto-hide pagination
- Add/Edit modal: first name, last name, email, phone, DOB, nationality, ID document type (Passport/Driver License/National ID), ID number
- Bootstrap pagination controls

**4. Reservations** (`/dashboard/reservations`):
- Table: guest name, room, check-in/out dates, status badge (color-coded), channel, rate
- Search/filter: guest name, status dropdown, room number, date range
- Conditional action buttons per status: Edit, Check-In, Check-Out, No-Show, Cancel
- Create/Edit modal: guest select, room select (auto-fills rate), dates, channel dropdown, special requests
- Cancel confirmation modal

**5. Folios** (`/dashboard/folios`):
- Reservation ID lookup → displays folio details
- Guest name, status, total, currency display
- Charges table: description, charge type, amount, posted date
- Payments table: method, amount, status, paid date
- Post Charge modal: description, amount, charge type
- Process Payment modal: amount, method
- Close Folio button with confirmation

**6. Tables** (`/dashboard/tables`):
- Table: number, zone, capacity, status
- Add/Edit modal: table number, zone dropdown (Indoor/Outdoor/Private), capacity, status
- Browser confirm for delete

**7. Table Reservations** (`/dashboard/table-reservations`):
- Table: table number, guest name, party size, date/time, status
- Conditional edit/cancel actions (only for PENDING/CONFIRMED)
- Create/Edit modal: table dropdown (available only), optional guest select, party size, date/time picker, special notes

**8. Orders** (`/dashboard/orders`):
- Status filter tabs: Open, Submitted, In Kitchen, Ready, Served, Closed
- Table: order ID, table, guest, status badge, total, opened time
- Advance-status arrow button + void button per row
- Create Order modal: table select, guest select, dynamic item rows with add/remove, running total auto-calculation
- Item row: menu item select (auto-fills price), quantity input, price display

**9. Menu** (`/dashboard/menu`):
- Two-panel layout: categories (left) → items (right)
- Category list with edit/delete buttons
- Items table: name, category, price, availability badge
- Category modal: name, display order
- Item modal: name, category dropdown, base price, availability, description, allergens (comma-separated input), dietary flags (comma-separated input)

**10. Housekeeping** (`/dashboard/housekeeping`):
- Date filter input (defaults to today)
- Task table: room number, task type, assigned user, priority badge (color-coded), status badge, scheduled date
- Action buttons: Complete, Edit, Delete
- Create/Edit modal: room dropdown, task type, assigned user, priority (Low/Normal/High/Urgent), status, scheduled date

**11. Inventory** (`/dashboard/inventory`):
- Table: item name, unit, current stock, reorder threshold, cost/unit, low-stock status badge
- Low stock filter toggle with count badge
- Add/Edit modal: name, unit dropdown (KG/Liter/Pieces/Box/Bottle/Pound/Ounce), stock, threshold, cost
- Stock Adjustment modal: current stock display, quantity +/- input, reason

**12. Users** (`/dashboard/users`):
- Table: email, role badge, active/inactive badge, last login
- Action buttons: Edit, Toggle Active, Change Role (prompt), Delete
- Create modal: email, password, role dropdown
- Edit modal: email, role (no password)
- Delete confirmation modal

**13. Reports** (`/dashboard/reports`):
- Revenue report: date range picker, line chart, CSV download button
- Occupancy section: rate %, occupied/total, status breakdown badges
- Booking channels: pie chart
- Housekeeping summary: label:count display
- Top menu items: horizontal bar chart
- Low stock alerts table

**14. AI Assistant** (`/dashboard/ai`):
- Three-tab interface:
  - **Concierge:** textarea + optional guest/reservation IDs → AI reply with model/token metadata
  - **Feedback Analysis:** textarea + optional guest name → sentiment badge (color-coded), summary, suggested reply
  - **Menu Description:** item name, category, price, ingredients, dietary flags → generated description
- Loading spinners + button disable during API calls

**15. Profile** (`/dashboard/profile`):
- Displays email and role from stored user data
- Change Password form: current, new, confirm (client-side match validation)

### 8.3 JavaScript Module Overview

| Module | Key Functions | Purpose |
|--------|--------------|---------|
| `auth.js` | `setTokens()`, `getToken()`, `refreshAccessToken()`, `authFetch()`, `logout()` | JWT lifecycle for all pages |
| `toast.js` | `showToast()` | Bootstrap 5 toast notifications |
| `dashboard.js` | `updateKpis()`, `renderRevenueChart()`, `renderReservationChart()`, etc. | Chart.js dashboard rendering |
| `guests.js` | `loadGuests()`, `searchGuests()`, `renderPagination()` | Paginated guest CRUD |
| `rooms.js` | `loadRooms()`, `checkAvailability()`, `renderTable()` | Room CRUD + availability |
| `reservations.js` | `loadReservations()`, `searchReservations()`, `doCheckIn/Out/NoShow()` | Reservation lifecycle |
| `folios.js` | `lookupFolio()`, `postCharge()`, `processPayment()`, `closeFolio()` | Billing operations |
| `orders.js` | `filterOrders()`, `addOrderItemRow()`, `updateTotal()`, `advanceStatus()` | Order CRUD + state machine |
| `menu.js` | `loadCategories()`, `loadMenuItems()`, `saveItem()`, `deleteCategory()` | Category + item CRUD |
| `tables.js` | `loadTables()`, `saveTable()`, `deleteTable()` | Restaurant table CRUD |
| `table-reservations.js` | `loadReservations()`, `saveReservation()`, `cancelReservation()` | Table reservation CRUD |
| `housekeeping.js` | `loadTasks()`, `completeTask()`, `saveTask()` | Housekeeping task management |
| `inventory.js` | `loadInventory()`, `loadLowStock()`, `confirmStockAdjustment()` | Inventory + stock adjustments |
| `users.js` | `loadUsers()`, `toggleActive()`, `changeRole()`, `saveUser()` | User management |
| `reports.js` | `loadRevenue()`, `loadOccupancy()`, `loadChannels()`, `downloadRevenueCsv()` | Reports + charts + CSV |
| `ai-assistant.js` | Concierge, feedback, menu description handlers | AI tool interactions |
| `profile.js` | `loadProfile()`, `changePassword()` | Profile + password |

---

## 9. API Reference

### Authentication & Security

| Method | URL | Description | Roles |
|--------|-----|-------------|-------|
| POST | `/api/v1/auth/login` | Authenticate and return JWT tokens | Public |
| POST | `/api/v1/auth/register` | Register new user (FRONT_DESK only) | Public |
| POST | `/api/v1/auth/refresh` | Refresh expired token | Public |
| GET | `/api/v1/profile` | Get current user profile | Any authenticated |
| PUT | `/api/v1/profile/password` | Change current user's password | Any authenticated |

### Dashboard

| Method | URL | Description | Roles |
|--------|-----|-------------|-------|
| GET | `/api/v1/dashboard/summary` | Dashboard KPIs + chart data | MANAGER+ |

### Tenant Management

| Method | URL | Description | Roles |
|--------|-----|-------------|-------|
| GET | `/api/v1/tenants` | List all tenants | SUPER_ADMIN |
| GET | `/api/v1/tenants/{tenantId}` | Get tenant by ID | SUPER_ADMIN |
| POST | `/api/v1/tenants` | Create tenant | SUPER_ADMIN |
| PUT | `/api/v1/tenants/{tenantId}` | Update tenant | SUPER_ADMIN |
| DELETE | `/api/v1/tenants/{tenantId}` | Soft-delete tenant | SUPER_ADMIN |

### User Management

| Method | URL | Description | Roles |
|--------|-----|-------------|-------|
| GET | `/api/v1/users` | List all users | MANAGER+ |
| GET | `/api/v1/users/{userId}` | Get user by ID | MANAGER+ |
| POST | `/api/v1/users` | Create user | PROPERTY_ADMIN+ |
| PUT | `/api/v1/users/{userId}` | Update user | PROPERTY_ADMIN+ |
| PUT | `/api/v1/users/{userId}/role?role=` | Update role only | PROPERTY_ADMIN+ |
| PUT | `/api/v1/users/{userId}/toggle-active` | Toggle active/inactive | PROPERTY_ADMIN+ |
| DELETE | `/api/v1/users/{userId}` | Soft-delete user | PROPERTY_ADMIN+ |

### Guest Management

| Method | URL | Description | Roles |
|--------|-----|-------------|-------|
| GET | `/api/v1/guests` | List all guests | FRONT_DESK+ |
| GET | `/api/v1/guests/{guestId}` | Get guest by ID | FRONT_DESK+ |
| GET | `/api/v1/guests/search?lastName=` | Search by last name | FRONT_DESK+ |
| GET | `/api/v1/guests/page?page=&size=&sortBy=&sortDir=` | Paginated guests | FRONT_DESK+ |
| POST | `/api/v1/guests` | Create guest | FRONT_DESK+ |
| PUT | `/api/v1/guests/{guestId}` | Update guest | FRONT_DESK+ |
| DELETE | `/api/v1/guests/{guestId}` | Delete guest | MANAGER+ |

### Room Management

| Method | URL | Description | Roles |
|--------|-----|-------------|-------|
| GET | `/api/v1/rooms` | List all rooms | FRONT_DESK+ |
| GET | `/api/v1/rooms/types` | List room types | FRONT_DESK+ |
| GET | `/api/v1/rooms/{roomId}` | Get room by ID | FRONT_DESK+ |
| GET | `/api/v1/rooms/availability?checkIn=&checkOut=` | Available rooms | FRONT_DESK+ |
| POST | `/api/v1/rooms` | Create room | MANAGER+ |
| PUT | `/api/v1/rooms/{roomId}` | Update room | MANAGER+ |
| DELETE | `/api/v1/rooms/{roomId}` | Delete room | MANAGER+ |

### Reservation Management

| Method | URL | Description | Roles |
|--------|-----|-------------|-------|
| GET | `/api/v1/reservations?status=&guestName=&roomNumber=&dateFrom=&dateTo=` | List/search reservations | FRONT_DESK+ |
| GET | `/api/v1/reservations/{reservationId}` | Get reservation by ID | FRONT_DESK+ |
| POST | `/api/v1/reservations` | Create reservation | FRONT_DESK+ |
| PUT | `/api/v1/reservations/{reservationId}` | Update reservation | FRONT_DESK+ |
| PUT | `/api/v1/reservations/{reservationId}/check-in` | Check in guest | FRONT_DESK+ |
| PUT | `/api/v1/reservations/{reservationId}/check-out` | Check out guest | FRONT_DESK+ |
| PUT | `/api/v1/reservations/{reservationId}/no-show` | Mark no-show | FRONT_DESK+ |
| PUT | `/api/v1/reservations/{reservationId}/cancel` | Cancel reservation | FRONT_DESK+ |

### Folio & Payment

| Method | URL | Description | Roles |
|--------|-----|-------------|-------|
| GET | `/api/v1/folios/{folioId}` | Get folio by ID | FRONT_DESK+ |
| GET | `/api/v1/folios/reservation/{reservationId}` | Get folio by reservation | FRONT_DESK+ |
| POST | `/api/v1/folios/reservation/{reservationId}` | Create folio | FRONT_DESK+ |
| POST | `/api/v1/folios/{folioId}/charges` | Post charge to folio | FRONT_DESK+, SERVER |
| PUT | `/api/v1/folios/{folioId}/close` | Close folio | FINANCE+ |
| GET | `/api/v1/payments/folio/{folioId}` | List folio payments | FRONT_DESK+ |
| POST | `/api/v1/payments/folio/{folioId}` | Process payment | FRONT_DESK+ |

### Restaurant Tables

| Method | URL | Description | Roles |
|--------|-----|-------------|-------|
| GET | `/api/v1/tables` | List all tables | SERVER+ |
| GET | `/api/v1/tables/available` | Available tables | FRONT_DESK+ |
| GET | `/api/v1/tables/{tableId}` | Get table by ID | SERVER+ |
| POST | `/api/v1/tables` | Create table | MANAGER+ |
| PUT | `/api/v1/tables/{tableId}` | Update table | MANAGER+ |
| DELETE | `/api/v1/tables/{tableId}` | Delete table | MANAGER+ |

### Table Reservations

| Method | URL | Description | Roles |
|--------|-----|-------------|-------|
| GET | `/api/v1/table-reservations` | List all | SERVER+ |
| GET | `/api/v1/table-reservations/{tableResId}` | Get by ID | SERVER+ |
| POST | `/api/v1/table-reservations` | Create | FRONT_DESK+ |
| PUT | `/api/v1/table-reservations/{tableResId}` | Update | FRONT_DESK+ |
| DELETE | `/api/v1/table-reservations/{tableResId}` | Cancel | FRONT_DESK+ |

### Menu Management

| Method | URL | Description | Roles |
|--------|-----|-------------|-------|
| GET | `/api/v1/menu/categories` | List categories | SERVER+ |
| POST | `/api/v1/menu/categories` | Create category | MANAGER+ |
| PUT | `/api/v1/menu/categories/{categoryId}` | Update category | MANAGER+ |
| DELETE | `/api/v1/menu/categories/{categoryId}` | Delete category | MANAGER+ |
| GET | `/api/v1/menu/items` | List items | SERVER+ |
| GET | `/api/v1/menu/items/{itemId}` | Get item by ID | SERVER+ |
| POST | `/api/v1/menu/items` | Create item | MANAGER+ |
| PUT | `/api/v1/menu/items/{itemId}` | Update item | MANAGER+ |
| DELETE | `/api/v1/menu/items/{itemId}` | Delete item | MANAGER+ |
| POST | `/api/v1/menu/items/{itemId}/variants` | Add variant | MANAGER+ |
| DELETE | `/api/v1/menu/variants/{variantId}` | Delete variant | MANAGER+ |

### Order Management

| Method | URL | Description | Roles |
|--------|-----|-------------|-------|
| GET | `/api/v1/orders` | List all orders | SERVER+ |
| GET | `/api/v1/orders/page?page=&size=&sortBy=&sortDir=` | Paginated orders | SERVER+ |
| GET | `/api/v1/orders/status/{status}` | Filter by status | SERVER+, KITCHEN |
| GET | `/api/v1/orders/{orderId}` | Get order by ID | SERVER+, KITCHEN |
| POST | `/api/v1/orders` | Create order | SERVER+, FRONT_DESK |
| PUT | `/api/v1/orders/{orderId}/status?status=` | Update status | SERVER+, KITCHEN |
| PUT | `/api/v1/orders/{orderId}/void` | Void order | MANAGER+ |

### Inventory Management

| Method | URL | Description | Roles |
|--------|-----|-------------|-------|
| GET | `/api/v1/inventory` | List all items | FINANCE+ |
| GET | `/api/v1/inventory/low-stock` | Low stock items | FINANCE+ |
| GET | `/api/v1/inventory/{inventoryId}` | Get item by ID | FINANCE+ |
| POST | `/api/v1/inventory` | Create item | MANAGER+ |
| PUT | `/api/v1/inventory/{inventoryId}` | Update item | MANAGER+ |
| PUT | `/api/v1/inventory/{inventoryId}/stock` | Adjust stock | MANAGER+ |
| DELETE | `/api/v1/inventory/{inventoryId}` | Delete item | MANAGER+ |

### Housekeeping

| Method | URL | Description | Roles |
|--------|-----|-------------|-------|
| GET | `/api/v1/housekeeping?date=` | Tasks for date | HOUSEKEEPER+ |
| GET | `/api/v1/housekeeping/my?date=` | My assigned tasks | HOUSEKEEPER |
| GET | `/api/v1/housekeeping/{taskId}` | Task by ID | HOUSEKEEPER+ |
| POST | `/api/v1/housekeeping` | Create task | MANAGER+ |
| PUT | `/api/v1/housekeeping/{taskId}` | Update task | HOUSEKEEPER+ |
| DELETE | `/api/v1/housekeeping/{taskId}` | Delete task | MANAGER+ |

### Loyalty

| Method | URL | Description | Roles |
|--------|-----|-------------|-------|
| GET | `/api/v1/loyalty/guest/{guestId}/balance` | Points balance | FRONT_DESK+ |
| GET | `/api/v1/loyalty/guest/{guestId}/history` | Transaction history | FRONT_DESK+ |
| POST | `/api/v1/loyalty/guest/{guestId}/redeem` | Redeem points | FRONT_DESK+ |

### AI

| Method | URL | Description | Roles |
|--------|-----|-------------|-------|
| POST | `/api/v1/ai/concierge` | AI concierge query | FRONT_DESK+ |
| POST | `/api/v1/ai/feedback-analyze` | Analyze feedback | MANAGER+ |
| POST | `/api/v1/ai/menu-description` | Generate menu description | MANAGER+, KITCHEN |

### Reports

| Method | URL | Description | Roles |
|--------|-----|-------------|-------|
| GET | `/api/v1/reports/revenue?from=&to=` | Revenue report | MANAGER+, FINANCE |
| GET | `/api/v1/reports/occupancy` | Occupancy report | MANAGER+ |
| GET | `/api/v1/reports/bookings-by-channel` | Channel distribution | MANAGER+ |
| GET | `/api/v1/reports/top-menu-items` | Top menu items | MANAGER+ |
| GET | `/api/v1/reports/housekeeping-summary` | Housekeeping summary | MANAGER+ |
| GET | `/api/v1/reports/export/revenue?from=&to=` | Revenue CSV export | MANAGER+, FINANCE |
| GET | `/api/v1/reports/low-stock` | Low stock alerts | MANAGER+, FINANCE |

### Audit

| Method | URL | Description | Roles |
|--------|-----|-------------|-------|
| GET | `/api/v1/audit/entity/{entityType}/{entityId}` | Entity audit trail | SUPER_ADMIN |
| GET | `/api/v1/audit/user/{userId}` | User audit trail | SUPER_ADMIN |

### Partner API

| Method | URL | Description | Auth |
|--------|-----|-------------|------|
| GET | `/api/v1/partner/rooms` | List available rooms | X-API-Key |
| POST | `/api/v1/partner/rooms/reservations` | Create room reservation | X-API-Key |
| PUT | `/api/v1/partner/rooms/reservations/{id}` | Update reservation | X-API-Key |
| DELETE | `/api/v1/partner/rooms/reservations/{id}` | Cancel reservation | X-API-Key |
| GET | `/api/v1/partner/restaurant/menu` | List menu items | X-API-Key |
| POST | `/api/v1/partner/restaurant/reservations` | Create table reservation | X-API-Key |
| PUT | `/api/v1/partner/restaurant/reservations/{id}` | Update table reservation | X-API-Key |
| DELETE | `/api/v1/partner/restaurant/reservations/{id}` | Cancel table reservation | X-API-Key |
| GET | `/api/v1/partner/dashboard/{partnerId}` | Usage dashboard | MANAGER+ |

---

## 10. Enums Reference

### 10.1 `UserRole` (9 values)

| Value | Description |
|-------|-------------|
| `SUPER_ADMIN` | System-wide administrator — access to all tenants, audit logs, all endpoints |
| `PROPERTY_ADMIN` | Property-level administrator — user management, all modules for own tenant |
| `MANAGER` | General manager — most modules, reports, AI tools |
| `FRONT_DESK` | Receptionist — reservations, guests, rooms, folios, payments |
| `HOUSEKEEPER` | Housekeeping staff — tasks view/update, own assignments |
| `SERVER` | Restaurant server — orders, menu, tables, reservations |
| `KITCHEN` | Kitchen staff — orders (view + status), menu (view) |
| `FINANCE` | Finance/accounting — folios, payments, inventory, reports |
| `GUEST` | Reserved for future guest portal |

### 10.2 `ReservationStatus` (6 values)

| Value | Description |
|-------|-------------|
| `PENDING` | Awaiting confirmation |
| `CONFIRMED` | Confirmed but not yet checked in |
| `CHECKED_IN` | Guest is currently occupying the room |
| `CHECKED_OUT` | Guest has departed |
| `CANCELLED` | Booking was cancelled |
| `NO_SHOW` | Guest did not arrive |

### 10.3 `RoomStatus` (5 values)

| Value | Description |
|-------|-------------|
| `AVAILABLE` | Ready for booking |
| `OCCUPIED` | Guest currently in room |
| `RESERVED` | Booked for upcoming dates |
| `MAINTENANCE` | Under maintenance |
| `OUT_OF_ORDER` | Permanently unavailable |

### 10.4 `HousekeepingStatus` (4 values)

| Value | Description |
|-------|-------------|
| `CLEAN` | Room is clean and ready |
| `DIRTY` | Needs cleaning (set on checkout) |
| `IN_PROGRESS` | Currently being cleaned |
| `INSPECTED` | Cleaned and verified |

### 10.5 `OrderStatus` (7 values)

| Value | Description |
|-------|-------------|
| `OPEN` | Order created, items being added |
| `SUBMITTED` | Sent to kitchen |
| `IN_KITCHEN` | Being prepared |
| `READY` | Ready for service |
| `SERVED` | Delivered to table |
| `CLOSED` | Paid and closed |
| `VOIDED` | Cancelled before closing |

### 10.6 `OrderItemStatus` (5 values)

| Value | Description |
|-------|-------------|
| `PENDING` | Not yet sent to kitchen |
| `FIRED` | Sent to kitchen |
| `IN_PROGRESS` | Being prepared |
| `DONE` | Completed |
| `VOIDED` | Cancelled |

### 10.7 `PaymentMethod` (6 values)

| Value | Description |
|-------|-------------|
| `CASH` | Physical cash payment |
| `CARD` | Credit/debit card |
| `MOBILE_WALLET` | Digital wallet (Apple Pay, Google Pay) |
| `ROOM_CHARGE` | Charged to room folio |
| `CORPORATE_ACCOUNT` | Company billing account |
| `VOUCHER` | Gift voucher or promotional credit |

### 10.8 `ChargeType` (8 values)

| Value | Description |
|-------|-------------|
| `ROOM_CHARGE` | Room rate charge |
| `RESTAURANT` | Food & beverage charge |
| `MINIBAR` | Minibar consumption |
| `INCIDENTAL` | Miscellaneous charges |
| `FEE` | Service fees |
| `TAX` | Tax charges |
| `DISCOUNT` | Discount applied |
| `REFUND` | Refund issued |

### 10.9 `BookingChannel` (7 values)

| Value | Description |
|-------|-------------|
| `DIRECT` | Booked directly with hotel |
| `BOOKING_COM` | Booking.com |
| `EXPEDIA` | Expedia |
| `AIRBNB` | Airbnb |
| `PHONE` | Phone reservation |
| `WALK_IN` | Walk-in without reservation |
| `CORPORATE` | Corporate account booking |

### 10.10 `LoyaltyTier` (4 values)

| Value | Points Required |
|-------|-----------------|
| `BRONZE` | 0 (default) |
| `SILVER` | 1,000+ |
| `GOLD` | 5,000+ |
| `PLATINUM` | 10,000+ |

---

## 11. DTO Reference

### 11.1 Request DTOs (28 classes)

All located in `com.project.lmrs.dto.request`

| # | DTO | Key Fields | Validation |
|---|-----|-----------|------------|
| 1 | `LoginRequest` | email, password | `@NotBlank @Email` / `@NotBlank` |
| 2 | `RegisterRequest` | tenantId, email, password, role | `@NotBlank` / `@Email` / `@Size(min=8)` / `@NotNull` |
| 3 | `RefreshTokenRequest` | refreshToken | `@NotBlank` |
| 4 | `ChangePasswordRequest` | currentPassword, newPassword | `@NotBlank` / `@Size(min=8)` |
| 5 | `CreateTenantRequest` | name, subdomain, configJson | `@NotBlank` (name, subdomain) |
| 6 | `CreateUserRequest` | email, password, role | `@NotBlank @Email` / `@Size(min=8)` / `@NotNull` |
| 7 | `UpdateUserRequest` | email, role | `@NotBlank @Email` / `@NotNull` |
| 8 | `CreateGuestRequest` | firstName, lastName, email, phone, dob, nationality, idDocType, idDocNumber | `@NotBlank` (firstName, lastName) / `@Email` |
| 9 | `CreateRoomRequest` | roomNumber, roomTypeId, floor, baseRate | `@NotBlank` / `@NotNull @Positive` |
| 10 | `CreateReservationRequest` | guestId, roomId, checkInDate, checkOutDate, rateApplied, channel, specialRequests | `@NotBlank` / `@NotNull` + `@AssertTrue` (dates) |
| 11 | `PostChargeRequest` | description, amount, chargeType | `@NotBlank` / `@NotNull @Positive` |
| 12 | `ProcessPaymentRequest` | amount, method, gatewayRef | `@NotNull @Positive` / `@NotBlank` |
| 13 | `CreateTableRequest` | tableNumber, zone, capacity, status, positionX/Y | `@NotBlank` / `@NotNull @Min(1)` |
| 14 | `CreateTableReservationRequest` | guestId, tableId, partySize, reservationDt, specialNotes | `@Min(1) @Max(20)` / `@NotNull` |
| 15 | `CreateOrderRequest` | tableId, serverUserId, guestId, items (list of OrderItemRequest) | `@NotNull` (items) |
| 16 | `CreateMenuItemRequest` | categoryId, name, description, basePrice, allergens, dietaryFlags, isAvailable | `@NotBlank` / `@NotNull` |
| 17 | `CreateCategoryRequest` | name, displayOrder, isActive | `@NotBlank` |
| 18 | `AddVariantRequest` | name, priceModifier | `@NotBlank` / `@NotNull` |
| 19 | `CreateInventoryItemRequest` | name, unit, currentStock, reorderThreshold, costPerUnit | `@NotBlank` / `@NotNull @PositiveOrZero` |
| 20 | `StockAdjustmentRequest` | quantity, reason | `@NotNull` |
| 21 | `CreateHousekeepingTaskRequest` | roomId, taskType, status, priority, assignedUserId, scheduledDate | `@NotBlank` / `@NotNull` |
| 22 | `RedeemPointsRequest` | points, referenceId, referenceType | `@NotNull @Min(1)` |
| 23 | `AiConciergeRequest` | query, guestId, reservationId, roomId | `@NotBlank` |
| 24 | `AiMenuDescriptionRequest` | itemName, categoryName, ingredients, dietaryFlags, basePrice | `@NotBlank` |
| 25 | `AiFeedbackRequest` | feedbackText, guestName | `@NotBlank` |
| 26 | `PartnerReservationRequest` | guestFirstName, guestLastName, guestEmail, guestPhone, roomTypeId, checkInDate, checkOutDate, specialRequests | `@NotBlank` / `@Email` / `@NotNull` |
| 27 | `PartnerTableReservationRequest` | guestFirstName, guestLastName, guestEmail, guestPhone, partySize, reservationDateTime, specialNotes | `@NotBlank` / `@Email` / `@Min(1) @Max(20)` / `@NotNull` |
| 28 | `UpdatePartnerReservationRequest` | checkInDate, checkOutDate, specialRequests, roomTypeId | Optional fields |

### 11.2 Response DTOs (29 classes)

All located in `com.project.lmrs.dto.response`

| # | DTO | Key Fields |
|---|-----|-----------|
| 1 | `AuthResponse` | accessToken, refreshToken, userId, email, role, tenantId |
| 2 | `TenantResponse` | tenantId, name, subdomain, configJson, isActive |
| 3 | `UserResponse` | userId, email, role, isActive, lastLogin |
| 4 | `GuestResponse` | guestId, firstName, lastName, email, phone, dob, nationality, idDocType, idDocNumber, loyaltyTier |
| 5 | `RoomResponse` | roomId, roomNumber, roomTypeId, roomTypeName, floor, status, housekeepingStatus, baseRate |
| 6 | `RoomTypeResponse` | roomTypeId, name, maxOccupancy, description, amenities |
| 7 | `AvailabilityResponse` | roomId, roomNumber, roomTypeName, floor, baseRate, amenities |
| 8 | `ReservationResponse` | reservationId, guestId, guestName, roomId, roomNumber, roomTypeName, checkInDate, checkOutDate, status, channel, rateApplied, specialRequests |
| 9 | `FolioResponse` | folioId, reservationId, guestName, status, totalAmount, currency, lineItems (list), payments (list) |
| 10 | `PaymentResponse` | paymentId, amount, method, status, gatewayRef, paidAt |
| 11 | `RestaurantTableResponse` | tableId, tableNumber, zone, capacity, status, positionX, positionY |
| 12 | `TableReservationResponse` | tableResId, tableId, tableNumber, guestId, guestName, partySize, reservationDt, status, specialNotes |
| 13 | `MenuItemResponse` | itemId, categoryId, categoryName, name, description, basePrice, allergens, dietaryFlags, isAvailable, variants (list) |
| 14 | `MenuItemVariantResponse` | variantId, name, priceModifier |
| 15 | `CategoryResponse` | categoryId, name, displayOrder, isActive |
| 16 | `OrderResponse` | orderId, tableNumber, serverUserId, guestName, status, totalAmount, openedAt, closedAt, items (list) |
| 17 | `InventoryItemResponse` | inventoryId, name, unit, currentStock, reorderThreshold, costPerUnit |
| 18 | `HousekeepingTaskResponse` | taskId, room (RoomBasic), taskType, assignedUserId, priority, status, scheduledDate, completedAt |
| 19 | `LoyaltyTransactionResponse` | loyaltyTxId, transactionType, points, referenceId, referenceType, createdAt |
| 20 | `AuditLogResponse` | logId, tenantId, userId, action, entityType, entityId, oldValue, newValue, ipAddress, createdAt |
| 21 | `AiConciergeResponse` | reply, model, tokensUsed |
| 22 | `AiMenuDescriptionResponse` | description, model, tokensUsed |
| 23 | `AiFeedbackResponse` | sentiment, summary, suggestedReply, model, tokensUsed |
| 24 | `DashboardSummaryResponse` | kpis, reservationStatuses, bookingChannels, revenueTrend, roomStatuses, roomsByFloor, roomsByType, housekeepingStatuses, topMenuItems, lowStockItems, totalTasksToday, completedTasksToday |
| 25 | `UsageDashboardResponse` | partnerId, partnerName, providerType, totalCalls, avgResponseMs, successCalls, errorCalls, endpointStats |
| 26 | `PartnerReservationResponse` | reservationId, guestName, guestEmail, roomNumber, roomType, checkInDate, checkOutDate, status, rateApplied, specialRequests, bookingChannel |
| 27 | `PartnerRoomListingResponse` | roomId, roomNumber, roomTypeName, floor, maxOccupancy, status, baseRate, amenities |
| 28 | `PartnerTableReservationResponse` | tableReservationId, guestName, guestEmail, tableNumber, partySize, reservationDateTime, status, specialNotes |
| 29 | `PartnerMenuListingResponse` | itemId, categoryName, name, description, basePrice, allergens, dietaryFlags, isAvailable, variants |

---

## 12. Configuration Properties

### Application (`application.properties`)

```properties
spring.application.name=lmrs

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/lrms_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:1234}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.open-in-view=false

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

# JWT
jwt.secret=${JWT_SECRET:change-this-to-a-real-secret-key-at-least-256-bits-long-in-production}
jwt.access-token-expiry=900000       # 15 minutes
jwt.refresh-token-expiry=604800000   # 7 days

# Groq AI
groq.api-key=${GROQ_API_KEY:gsk_...}
groq.api.url=https://api.groq.com/openai/v1/chat/completions
groq.model=llama-3.1-8b-instant
groq.max-tokens=1024
groq.temperature=0.7
```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_USERNAME` | `root` | MySQL database username |
| `DB_PASSWORD` | `1234` | MySQL database password |
| `JWT_SECRET` | (placeholder) | HMAC-SHA key for JWT signing (256+ bits) |
| `GROQ_API_KEY` | (placeholder) | Groq API key for AI features |

### Important Settings

| Setting | Value | Notes |
|---------|-------|-------|
| `ddl-auto` | `validate` | Schema managed by Flyway — Hibernate only validates |
| `open-in-view` | `false` | Disables OSIV pattern — prevents lazy loading in views |
| BCrypt strength | `12` | High work factor for password hashing |
| Token expiry (access) | 15 min | Short-lived for security |
| Token expiry (refresh) | 7 days | Longer-lived for UX convenience |

---

## 13. Flyway Migrations

| Migration | Tables Created | Purpose |
|-----------|---------------|---------|
| **V1** | `tenants`, `users` | Core multi-tenant foundation + user accounts |
| **V2** | `guests`, `room_types`, `rooms` | Guest profiles + room inventory |
| **V3** | `reservations`, `folios`, `folio_line_items`, `payments` | Booking engine + billing system |
| **V4** | `restaurant_tables`, `table_reservations`, `menu_categories`, `menu_items`, `menu_item_variants` | Restaurant operations |
| **V5** | `orders`, `order_items`, `inventory_items`, `housekeeping_tasks`, `loyalty_transactions`, `audit_logs` | F&B operations + support modules |
| **V6** | `partner_accounts`, `partner_api_usage` | External partner integration |
| **V7** | _(no new tables)_ | 16 performance indexes + 7 missing FK constraints + schema fix for `partner_accounts.deleted_at` |

---

## 14. Error Handling

### Global Exception Handler (`GlobalExceptionHandler`)

| Exception | HTTP Status | Response Body |
|-----------|-------------|---------------|
| `ResourceNotFoundException` | 404 NOT_FOUND | `{ timestamp, status, error: "Not Found", message }` |
| `BusinessRuleException` | 400 BAD_REQUEST | `{ timestamp, status, error: "Bad Request", message }` |
| `UnauthorizedException` | 401 UNAUTHORIZED | `{ timestamp, status, error: "Unauthorized", message }` |
| `BadCredentialsException` | 401 UNAUTHORIZED | `{ message: "Invalid email or password" }` |
| `AccessDeniedException` | 403 FORBIDDEN | `{ message: "Access denied" }` |
| `MethodArgumentNotValidException` | 400 BAD_REQUEST | `{ fields: { fieldName: "error message" } }` |
| `IllegalArgumentException` | 400 BAD_REQUEST | `{ message }` |
| `HttpMessageNotReadableException` | 400 BAD_REQUEST | `{ message: "Invalid request body: ..." }` |
| `Exception` (catch-all) | 500 INTERNAL_SERVER_ERROR | `{ message: "An unexpected error occurred" }` |

### Custom Exception Classes

| Class | When to Throw |
|-------|---------------|
| `ResourceNotFoundException(resource, field, value)` | Entity not found with given criteria. Message format: `"{resource} not found with {field}: '{value}'"` |
| `BusinessRuleException(message)` | Business logic violation (e.g., conflicting reservation, insufficient stock) |
| `UnauthorizedException(message)` | Authentication failure |

### Frontend Error Handling

- All API calls use `Auth.authFetch()` which handles 401 by attempting token refresh
- On refresh failure: `Auth.logout()` clears storage and redirects to `/login`
- `showToast(message, type)` displays errors as Bootstrap 5 toast notifications (red = error, green = success, yellow = warning, blue = info)
- All `.catch()` handlers call `showToast(error.message || 'Operation failed', 'error')`

---

## 15. Seed Data

On application startup, `DataInitializer` (`CommandLineRunner`) seeds the following data if the default tenant does not exist:

### Default Tenant

| Field | Value |
|-------|-------|
| Name | `"Grand Horizon Hotel & Residency"` |
| Subdomain | `"grand-horizon"` |
| Config | `{ timezone: "Asia/Kolkata", currency: "INR" }` |

### Default Users

| Email | Password | Role |
|-------|----------|------|
| `admin@lmrs.com` | `admin123` | SUPER_ADMIN |
| `manager@lmrs.com` | `manager123` | MANAGER |
| `frontdesk@lmrs.com` | `front123` | FRONT_DESK |

### Room Types

| Name | Max Occupancy | Description | Base Rate |
|------|---------------|-------------|-----------|
| Standard | 2 | — | ₹2,500 |
| Deluxe | 3 | — | ₹4,500 |
| Suite | 4 | — | ₹7,500 |

### Rooms (15 total)

3 floors × 5 rooms each:
- Rooms ending in 01–02: **Standard**
- Rooms ending in 03–04: **Deluxe**
- Rooms ending in 05: **Suite**
- Numbers: 101-105 (Floor 1), 201-205 (Floor 2), 301-305 (Floor 3)

### Default Guests (10 Indian Guests)

| Name | Nationality |
|------|-------------|
| Aarav Sharma | IN |
| Priya Verma | IN |
| Rohit Patel | IN |
| Ananya Gupta | IN |
| Vikram Singh | IN |
| Neha Reddy | IN |
| Arjun Nair | IN |
| Kavya Joshi | IN |
| Rahul Deshmukh | IN |
| Isha Malhotra | IN |

### Menu Categories

| Name | Display Order |
|------|---------------|
| Main Course | 1 |
| Starters | 2 |
| Desserts | 3 |
| Beverages | 4 |

### Menu Items (Indian Cuisine)

| Item | Category | Price |
|------|----------|-------|
| Butter Chicken | Main Course | ₹450.00 |
| Chicken Biryani | Main Course | ₹380.00 |
| Samosa | Starters | ₹120.00 |
| Paneer Tikka | Starters | ₹250.00 |
| Gulab Jamun | Desserts | ₹150.00 |
| Masala Chai | Beverages | ₹80.00 |
| Mango Lassi | Beverages | ₹120.00 |

### Restaurant Tables

| Table | Zone | Capacity |
|-------|------|----------|
| T101 | Main Hall | 2 |
| T102 | Main Hall | 4 |
| T103 | Main Hall | 2 |
| T104 | Main Hall | 4 |
| T105 | Terrace | 2 |
| T106 | Terrace | 4 |
| T107 | Terrace | 2 |
| T108 | Terrace | 4 |

### Inventory Items (30 items across 3 categories)

**Kitchen & Spices (10 items)**

| Item | Unit | Initial Stock | Reorder Threshold | Cost/Unit |
|------|------|---------------|-------------------|-----------|
| Turmeric Powder | kg | 5.0 | 1.0 | ₹280.00 |
| Cumin Seeds | kg | 4.0 | 1.0 | ₹180.00 |
| Cardamom | kg | 2.0 | 0.5 | ₹1,200.00 |
| Cinnamon Sticks | kg | 3.0 | 0.5 | ₹450.00 |
| Cloves | kg | 1.5 | 0.5 | ₹600.00 |
| Coriander Powder | kg | 5.0 | 1.0 | ₹160.00 |
| Red Chili Powder | kg | 4.0 | 1.0 | ₹240.00 |
| Garam Masala | kg | 3.0 | 0.5 | ₹320.00 |
| Basmati Rice | kg | 25.0 | 5.0 | ₹140.00 |
| Wheat Flour (Atta) | kg | 20.0 | 5.0 | ₹38.00 |

**Lodge & Housekeeping (10 items)**

| Item | Unit | Initial Stock | Reorder Threshold | Cost/Unit |
|------|------|---------------|-------------------|-----------|
| Bath Towel | pieces | 80.0 | 15.0 | ₹350.00 |
| Hand Towel | pieces | 60.0 | 10.0 | ₹180.00 |
| Fitted Bedsheet (Queen) | pieces | 40.0 | 8.0 | ₹550.00 |
| Pillow Cover | pieces | 60.0 | 12.0 | ₹120.00 |
| Toilet Paper Roll | pieces | 200.0 | 40.0 | ₹45.00 |
| Hand Soap | pieces | 100.0 | 20.0 | ₹35.00 |
| Shampoo Sachet | pieces | 300.0 | 50.0 | ₹8.00 |
| Disposable Slippers | pairs | 80.0 | 20.0 | ₹95.00 |
| Laundry Detergent | kg | 15.0 | 3.0 | ₹280.00 |
| Room Freshener | liters | 8.0 | 2.0 | ₹190.00 |

**Fresh Produce & Protein (10 items)**

| Item | Unit | Initial Stock | Reorder Threshold | Cost/Unit |
|------|------|---------------|-------------------|-----------|
| Chicken (Boneless) | kg | 15.0 | 4.0 | ₹240.00 |
| Chicken (Curry Cut) | kg | 20.0 | 5.0 | ₹200.00 |
| Paneer (Cottage Cheese) | kg | 10.0 | 2.0 | ₹320.00 |
| Fresh Milk | liters | 30.0 | 8.0 | ₹56.00 |
| Curd (Yogurt) | kg | 12.0 | 3.0 | ₹80.00 |
| Tomatoes | kg | 15.0 | 4.0 | ₹40.00 |
| Onions | kg | 25.0 | 5.0 | ₹30.00 |
| Potatoes | kg | 30.0 | 6.0 | ₹25.00 |
| Green Chilies | kg | 3.0 | 0.5 | ₹60.00 |
| Fresh Coriander | kg | 2.0 | 0.5 | ₹80.00 |

---

## Index

| Module | Key File(s) | Line(s) |
|--------|-------------|---------|
| Overview | `LmrsApplication.java` | — |
| Security Config | `SecurityConfig.java` | §4 |
| JWT Provider | `JwtTokenProvider.java` | §4 |
| Auth Filter | `JwtAuthenticationFilter.java` | §4 |
| Partner Filter | `PartnerApiKeyFilter.java` | §7.17 |
| Multi-Tenancy | `SecurityUtils.java` | §5 |
| Error Handler | `GlobalExceptionHandler.java` | §14 |
| AI Service | `AiConciergeService.java`, `GroqAiService.java` | §7.16 |
| Data Seeder | `DataInitializer.java` | §15 |
| Frontend Auth | `auth.js` | §8.3 |
| Toast Utility | `toast.js` | §8.3 |
| Charts | `dashboard.js` | §8.3 |
| All Controllers | `controller/*.java` | §9 |
| All Services | `service/*.java` | §7 |
| All Entities | `entity/*.java` | §6 |
| All Repositories | `repository/*.java` | §6 |
| All Migrations | `db/migration/V*.sql` | §13 |
| Build Config | `build.gradle` | §2 |

---

*Document generated from the LMRS codebase. For the most up-to-date information, refer to the source code directly.*

| Statistic | Value |
|-----------|-------|
| **Java files** | 120+ |
| **Services** | 20 |
| **Controllers** | 36 (23 REST + 13 Page) |
| **Entities** | 22 |
| **Repositories** | 22 |
| **REST endpoints** | 98 |
| **Page endpoints** | 18 |
| **Flyway migrations** | 7 |
| **Database tables** | 22 |
| **Frontend templates** | 18 |
| **JavaScript files** | 17 |
| **Request DTOs** | 28 |
| **Response DTOs** | 29 |
| **Enums** | 10 |
| **User roles** | 9 |
