# MarketHub — Technical Overview

## Project Summary

A full-stack e-commerce web application built with Spring Boot. The system supports three distinct user roles — Admin, Seller, and Buyer — each with their own secured workflow. Users register, sellers list products, buyers browse and purchase, and admins manage the platform.

## Architecture

### Layered MVC Architecture

```
Browser (Thymeleaf)
       ↓
  Controller Layer       → 11 MVC controllers + 4 REST controllers (/api/**)
       ↓
  Service Layer          → 7 service interfaces, 7 implementations + UserDetailsServiceImpl
       ↓
  Repository Layer       → 8 Spring Data JPA repositories
       ↓
  MySQL 8 Database       → 9 JPA entities
```

### Security Architecture

Spring Security with form-based authentication and role-based URL authorization:

| URL Pattern | Allowed Roles |
|-------------|--------------|
| `/`, `/onlinemarket/public/**`, static assets, Swagger UI, `/actuator/health` | Public (no auth) |
| `GET /api/products/**` | Public (no auth) |
| `/onlinemarket/secured/services/admin/**` | ADMIN only |
| `/onlinemarket/secured/services/seller/**` | SELLER only |
| `/onlinemarket/secured/services/buyer/**` | BUYER only |
| Everything else | Any authenticated user |

Passwords are hashed with BCrypt (strength 12). CSRF protection is on (Spring Security default).
Method-level security (`@PreAuthorize("hasRole('ADMIN')")`) protects seller approval, the user API
and order listing/deletion in the order API.

Self-registration only accepts `BUYER` or `SELLER`; any other requested role is rejected.

**Known gaps** (demo scope): product management lives under `/onlinemarket/secured/services/products/**`
and the order, cart, address, review and payment endpoints sit under "any authenticated user", so
they do not check that the caller owns the resource. The one ownership check is the MVC product
delete, which only lets a seller delete their own products.

## Domain Model

```
User ──────────────────────────────────────────────┐
 ├── roles: List<Role>          (ROLE_ADMIN etc.)   │
 ├── address: List<Address>                         │
 ├── payment: List<Payment>                         │
 └── shoppingCart: ShoppingCart                     │
                                                    │
Product                                             │
 ├── seller: User ──────────────────────────────────┘
 ├── reviews: List<Review>
 └── (name, price, description, quantity, sku)

ShoppingCart
 └── products: List<Product>

Order
 └── (owner: User, price, orderStatus, createdAt)

Review
 └── (product, user, comment, isApproved)
```

## Key Design Decisions

**Role-Based Access via Spring Security**
URL patterns locked down at the security config level, with method-level `@PreAuthorize` for admin endpoints. Seller approval requires explicit ADMIN action.

**Seller Approval Workflow**
New SELLER registrations are created with `approvedSeller=false` and appear as *Pending* on the admin seller list until an ADMIN approves them. The flag is not yet checked when a seller creates products.

**Checkout and Payments**
Checkout sums the cart, saves an `Order` with status `Pending` and clears the cart. No payment provider is integrated and no payment is taken. The `Payment` entity and `/payment` endpoints are an unused prototype for stored payment methods.

**BCrypt Password Hashing**
All passwords hashed with `BCryptPasswordEncoder` before persistence. No plain-text passwords stored at any point.

**Optional Pattern for Data Access**
All repository lookups use `orElseThrow()` with descriptive error messages instead of returning `null`, preventing silent NullPointerExceptions.

**Shared Role Entities**
Roles are shared entities (`ROLE_BUYER`, `ROLE_SELLER`, `ROLE_ADMIN`) — the service layer finds an existing role by type before creating a new one, avoiding duplicate role rows.

## API Documentation

Interactive API docs available at `/swagger-ui.html` when the app is running.
Raw OpenAPI spec at `/v3/api-docs`.

## Database

MySQL 8.0+. The `dev` profile (default) uses `ddl-auto=update`; the `prod` profile uses `validate`,
and Docker Compose overrides it to `update` so a fresh database is created automatically.

Connection settings come from environment variables, with defaults that match the Compose database:

| Variable | Default |
|----------|---------|
| `MYSQLHOST` | `127.0.0.1` |
| `MYSQLPORT` | `3307` (Compose maps MySQL to host port 3307) |
| `MYSQLDATABASE` | `markethub_db` |
| `MYSQLUSER` | `markethub_user` |
| `MYSQLPASSWORD` | `markethub_pass` |

These are local demo values; `.env.example` documents the same values for Docker Compose.

`DataLoader` seeds the ADMIN/SELLER/BUYER roles, three demo users (`admin`, `seller`, `buyer`,
password `password`) and six sample products on first start.

## Testing

39 tests across 7 test classes using JUnit 5, Mockito, MockMvc and H2 in-memory — no MySQL required for CI.

```
UserServiceImplTest                8 tests  (register, role whitelist, approve seller, CRUD)
ProductServiceImplTest             6 tests  (CRUD, search, not-found)
ShoppingCartServiceImplTest        5 tests  (add/remove products, not-found errors)
ProductApiControllerTest           5 tests  (@WebMvcTest + MockMvc, service mocked)
ProductRepositoryIntegrationTest   6 tests  (@DataJpaTest on H2)
UserRepositoryIntegrationTest      8 tests  (@DataJpaTest on H2)
MarketHubApplicationTests          1 test   (@SpringBootTest context load)
```

The suite does not run against MySQL and does not exercise the security filter chain end to end.

GitHub Actions runs `mvn clean verify` and then `docker build` on every push to `main` and every pull request.

## Running Locally

```bash
# Full stack in Docker (builds the app from source)
docker compose up --build

# Or: database in Docker, app from the IDE / Maven wrapper
docker compose up -d db
./mvnw spring-boot:run   # Mac/Linux
mvnw.cmd spring-boot:run  # Windows
```

App runs at `http://localhost:8081`

## Tech Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| Language | Java | 17 |
| Framework | Spring Boot | 3.3.6 |
| ORM | Spring Data JPA + Hibernate | 6.x |
| Security | Spring Security | 6.x |
| Frontend | Thymeleaf + Bootstrap 5.3 + Font Awesome 6 (self-hosted) | - |
| Database | MySQL | 8.0+ |
| Connector | MySQL Connector/J | 9.1.0 |
| Build | Maven | 3.x |
| API Docs | SpringDoc OpenAPI | 2.6.0 |
| Testing | JUnit 5 + Mockito + MockMvc + H2 | - |
| Coverage | JaCoCo | 0.8.12 |
