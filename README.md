# MarketHub

[![CI](https://github.com/Kalab21/markethub/actions/workflows/ci.yml/badge.svg)](https://github.com/Kalab21/markethub/actions/workflows/ci.yml)

MarketHub is a full-stack e-commerce marketplace built with Java 17 and Spring Boot.
It has separate Admin, Seller and Buyer workflows: admins review and approve sellers,
sellers manage product listings, and buyers browse the catalogue, fill a cart and
place orders. It is a server-rendered Spring MVC + Thymeleaf application backed by MySQL.

| Storefront | Product catalogue |
|------------|-------------------|
| ![Home](screenshots/home.png) | ![Products](screenshots/products.png) |

## What MarketHub demonstrates

- **Layered Spring Boot MVC** — controllers → services → Spring Data JPA repositories → MySQL
- **Role-based access with Spring Security** — form login, BCrypt password hashing, URL rules and `@PreAuthorize` for admin operations
- **Three distinct role workflows** — Admin, Seller and Buyer each get their own dashboard and navigation
- **Seller approval** — new sellers register as *pending*; only an admin can approve them
- **Cart-to-order flow** — cart totals, checkout into a `Pending` order, cancel and order history
- **Server-rendered UI** — Thymeleaf layouts with self-hosted Bootstrap 5.3 and Font Awesome 6
- **Automated tests + CI** — 39 JUnit 5 tests on H2, Maven + Docker image build on every push and PR

## Product experience

| Shopping cart | Order history |
|---------------|---------------|
| ![Cart](screenshots/cart.png) | ![Orders](screenshots/orders.png) |

| Seller: my products | Admin: seller approval |
|---------------------|------------------------|
| ![Seller products](screenshots/seller-products.png) | ![Seller approval](screenshots/seller-approval.png) |

## Architecture

A single Spring Boot application with a conventional layered design:

```
Browser  (Admin | Seller | Buyer)
   ↓
Spring Security filter chain   form login, session, role rules
   ↓
Spring MVC controllers         Thymeleaf pages + /api/** REST endpoints
   ↓
Service layer                  business rules, @Transactional
   ↓
Spring Data JPA repositories
   ↓
MySQL 8
```

Details — domain model, security rules, design decisions — are in [PROJECT.md](PROJECT.md).

## Role-based workflows

| Role | What they can do |
|------|------------------|
| **Admin** | View sellers and approve pending seller accounts; admin-only user API and order listing/deletion API |
| **Seller** | Create, edit and delete product listings (name, SKU, price, stock) from a "My Products" page |
| **Buyer** | Browse the product catalogue, manage a cart, check out, cancel pending orders, view order history |

New accounts can self-register as Buyer or Seller. Admin accounts cannot be self-registered.

## Security

Concrete controls in the code:

- Passwords hashed with `BCryptPasswordEncoder` (strength 12)
- Spring Security form login with a server-side HTTP session; CSRF protection on form posts
- URL authorization: `/onlinemarket/secured/services/admin/**` requires `ADMIN`, `…/buyer/**` requires `BUYER`, everything outside public pages requires login
- `@PreAuthorize("hasRole('ADMIN')")` on seller approval and the admin user/order API
- Self-registration only accepts the `BUYER` or `SELLER` role
- Sellers can only delete products they own (MVC delete checks the product's seller)

Known limitations: most other endpoints check authentication but not resource ownership,
and seller approval is recorded but not yet enforced before a seller can list products.
See [Project scope](#project-scope).

## Testing

39 tests across 7 test classes, run with `mvn clean verify` against an in-memory H2 database:

| Type | Classes | Tests |
|------|---------|-------|
| Service unit tests (Mockito) | `UserServiceImplTest`, `ProductServiceImplTest`, `ShoppingCartServiceImplTest` | 19 |
| Repository tests (`@DataJpaTest`, H2) | `UserRepositoryIntegrationTest`, `ProductRepositoryIntegrationTest` | 14 |
| Controller tests (`@WebMvcTest`, MockMvc) | `ProductApiControllerTest` | 5 |
| Application context | `MarketHubApplicationTests` | 1 |

GitHub Actions runs the full Maven build, then builds the Docker image. JaCoCo coverage is uploaded as a build artifact.

## Technology

| Layer | Technology |
|-------|------------|
| Language / framework | Java 17, Spring Boot 3.3.6 |
| Web | Spring MVC, Thymeleaf, Bootstrap 5.3, Font Awesome 6 |
| Security | Spring Security 6 |
| Persistence | Spring Data JPA (Hibernate), MySQL 8, MySQL Connector/J 9.1.0 |
| API docs / ops | SpringDoc OpenAPI 2.6.0, Spring Boot Actuator |
| Testing | JUnit 5, Mockito, MockMvc, H2, JaCoCo |
| Build / run | Maven wrapper, Docker, Docker Compose |

## Run locally

### Option 1 — Docker Compose

```bash
docker compose up --build
```

This builds the app from source and starts MySQL. Open `http://localhost:8081`.
Compose uses the local demo values in [`.env.example`](.env.example) by default;
copy it to `.env` only if you want different values.

### Option 2 — IDE / Maven wrapper (Java 17)

Start only the database, then run the app. The default datasource settings already
point at the Compose database (`localhost:3307`), so no files need editing:

```bash
docker compose up -d db

./mvnw spring-boot:run       # macOS / Linux
mvnw.cmd spring-boot:run     # Windows
```

To use your own MySQL instead, set `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`,
`MYSQLUSER` and `MYSQLPASSWORD` before starting the app.

### Demo accounts

**Synthetic local demo accounts only**, seeded on first start together with six sample products:

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `password` |
| Seller (approved) | `seller` | `password` |
| Buyer | `buyer` | `password` |

### Useful URLs

- App: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui.html`
- Health: `http://localhost:8081/actuator/health`

## Project scope

MarketHub is a portfolio / demo marketplace that runs locally with synthetic accounts
and a local MySQL database. It is not a production commerce deployment.

- **Payments are not processed.** Checkout creates a `Pending` order from the cart total;
  no payment provider is integrated. A `Payment` entity and CRUD endpoints exist as an
  early prototype for stored payment methods but are not used by checkout.
- **Authorization is role-based, not fully ownership-based.** Several order, cart, user,
  address, review and product API endpoints only require a logged-in user.
- **Seller approval is an admin workflow** whose status is not yet checked before listing products.
- The credentials in `docker-compose.yml` and `.env.example` are local demo defaults, not secrets.

## Technical documentation

[PROJECT.md](PROJECT.md) covers the architecture, security rules, domain model, design decisions and test suite in more depth.
