# MarketHub — Technical Overview

## Project Summary

A full-stack e-commerce web application built with Spring Boot. The system supports three distinct user roles — Admin, Seller, and Buyer — each with their own secured workflow. Users register, sellers list products, buyers browse and purchase, and admins manage the platform.

## Architecture

### Layered MVC Architecture

```
Browser (Thymeleaf)
       ↓
  Controller Layer       → 10 MVC controllers
       ↓
  Service Layer          → 9 service interfaces + implementations
       ↓
  Repository Layer       → 7 Spring Data JPA repositories
       ↓
  MySQL 8 Database       → 10 JPA entities
```

### Security Architecture

Spring Security with form-based authentication and role-based URL authorization:

| URL Pattern | Allowed Roles |
|-------------|--------------|
| `/onlinemarket/public/**` | Public (no auth) |
| `/onlinemarket/secured/services/admin/**` | ADMIN only |
| `/onlinemarket/secured/services/seller/**` | SELLER only |
| `/onlinemarket/secured/services/buyer/**` | BUYER only |
| All other secured paths | Any authenticated user |

Passwords are hashed with BCrypt. Method-level security (`@PreAuthorize`) protects sensitive operations like seller approval.

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
 └── (owner, products, status)

Review
 └── (product, buyer, content, rating)
```

## Key Design Decisions

**Role-Based Access via Spring Security**
URL patterns locked down at the security config level, with method-level `@PreAuthorize` for critical endpoints. Seller approval requires explicit ADMIN action.

**Seller Approval Workflow**
New SELLER registrations are created with `approvedSeller=false`. An ADMIN must explicitly approve before the seller can list products. This prevents spam listings.

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

MySQL 8.0+ required. Tables auto-created via `spring.jpa.hibernate.ddl-auto=update`.

```sql
CREATE DATABASE markethub_db;
```

Credentials configured via environment variables:
- `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, `MYSQLPASSWORD`

## Testing

38 tests across 7 test classes using JUnit 5 + Mockito. Unit tests run against H2 in-memory — no MySQL required for CI.

```
UserServiceImplTest                7 tests  (register, approve seller, CRUD)
ProductServiceImplTest             6 tests  (CRUD, search, not-found)
ShoppingCartServiceImplTest        5 tests  (add/remove products, not-found errors)
ProductApiControllerTest           5 tests  (REST endpoint integration)
ProductRepositoryIntegrationTest   6 tests  (JPA queries)
UserRepositoryIntegrationTest      8 tests  (JPA queries)
MarketHubApplicationTests          1 test   (context load)
```

CI runs on every push via GitHub Actions.

## Running Locally

```bash
# 1. Create database and user
mysql -u root -p -e "CREATE DATABASE markethub_db;"
mysql -u root -p -e "CREATE USER 'markethub_user'@'localhost' IDENTIFIED BY 'markethub_pass'; GRANT ALL ON markethub_db.* TO 'markethub_user'@'localhost';"

# 2. Start
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
| Frontend | Thymeleaf + Bootstrap 5.3 + Font Awesome 6 | - |
| Database | MySQL | 8.0+ |
| Connector | MySQL Connector/J | 9.1.0 |
| Build | Maven | 3.x |
| API Docs | SpringDoc OpenAPI | 2.6.0 |
| Testing | JUnit 5 + Mockito + H2 | - |
