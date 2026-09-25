# Inventory Management System — Backend API

[![CI/CD Backend](https://github.com/ledemkam/Inventory_Management_System/actions/workflows/ci-backend.yml/badge.svg)](https://github.com/ledemkam/Inventory_Management_System/actions/workflows/ci-backend.yml)
![Java 21](https://img.shields.io/badge/Java-21-orange)
![Spring Boot 4.1](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F)
![PostgreSQL 16](https://img.shields.io/badge/PostgreSQL-16-336791)

REST API for inventory management: product catalog (categories, products, suppliers), stock movements (purchases, sales,
returns to suppliers) and user management with JWT authentication and roles.

---

## Table of contents

- [Features](#features)
- [Tech stack](#tech-stack)
- [Architecture](#architecture)
- [API endpoints](#api-endpoints)
- [Environment variables](#environment-variables)
- [Running locally](#running-locally)
- [Creating the first ADMIN account](#creating-the-first-admin-account)
- [Testing and quality](#testing-and-quality)
- [CI/CD pipeline](#cicd-pipeline)
- [Deployment (Docker)](#deployment-docker)

---

## Features

- **Catalog**: CRUD for categories, suppliers and products, with product image upload to Cloudinary.
- **Stock transactions**: purchase (restock), sale, return to supplier, with a lifecycle
  `PENDING → PROCESSING → COMPLETED / CANCELED`. Stock only changes when a transaction becomes `COMPLETED`,
  and is restored if a completed transaction is canceled.
- **Search**: transactions by text or by month/year, per-user history, pagination everywhere.
- **Security**: stateless JWT authentication, `USER` / `MANAGER` / `ADMIN` roles, method-level access
  control (`@PreAuthorize`) with a role hierarchy (`ADMIN > MANAGER > USER`). Public sign-up always
  creates a `USER` account.
- **Operations**: Flyway migrations, Actuator health probes (liveness/readiness), AOP execution-time
  logging, production-ready Docker image.

## Tech stack

| Area                 | Technology                                                                                                 |
|----------------------|------------------------------------------------------------------------------------------------------------|
| Language             | Java 21                                                                                                    |
| Framework            | Spring Boot 4.1 (Web MVC, Data JPA, Validation, Security, Cache, AOP, Actuator)                            |
| Modular architecture | Spring Modulith 2.1 (module boundaries verified by tests, JPA event publication registry)                  |
| Database             | PostgreSQL 16, Hibernate, **Flyway** migrations                                                            |
| Security             | Spring Security, JWT (**jjwt** 0.12, HS256), passwords hashed with `DelegatingPasswordEncoder` (bcrypt)    |
| Mapping              | **MapStruct** 1.6, Lombok                                                                                  |
| File storage         | **Cloudinary** (product images)                                                                            |
| API documentation    | springdoc-openapi 3 (Swagger UI, `development` profile only)                                               |
| Testing              | JUnit 5, Mockito, Spring Security Test, H2 (fast tests), **Testcontainers** PostgreSQL (integration tests) |
| Quality              | JaCoCo (80% line coverage gate, build-breaking)                                                            |
| CI/CD                | GitHub Actions (tests + coverage gate, Docker image to GHCR, deploy to Render)                             |
| Containers           | Docker (multi-stage build, JRE 21, non-root user), Docker Compose for development                          |

## Architecture

The backend is a **modular monolith**: a single deployable split into independent business modules whose
boundaries are verified automatically by Spring Modulith (`ModularityTests`).

```mermaid
flowchart LR
    Client([Client / Frontend]) -->|HTTP + JWT| Security[Spring Security<br/>JwtAuthenticationFilter]
    Security --> User
    Security --> Catalog
    Security --> Transaction

    subgraph Business modules
        User[user<br/>auth, accounts, roles]
        Catalog[catalog<br/>categories, products, suppliers]
        Transaction[transaction<br/>purchases, sales, returns]
    end

    Transaction --> User
    Transaction --> Catalog
    Catalog -->|images| Cloudinary[(Cloudinary)]
    User & Catalog & Transaction --> DB[(PostgreSQL<br/>schema managed by Flyway)]
```

### Modules

| Module                                                  | Responsibility                                                                                                                                           |
|---------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| `user`                                                  | Sign-up, login, JWT issuing and validation, account and role management                                                                                  |
| `catalog`                                               | Categories, suppliers, products (with image)                                                                                                             |
| `transaction`                                           | Stock movements and their lifecycle; depends on `user` and `catalog`                                                                                     |
| `common`, `config`, `exception`, `aspect`, `annotation` | Shared "open" modules: audited base entity, pagination, configuration (security, CORS, Swagger, cache, JPA auditing), global error handling, AOP logging |

Each module exposes a public API (entities, response DTOs, repositories, services) at the root of its
package; everything under `internal/` (controllers, request DTOs, mappers, implementations) is private.

### Layering inside a module

```
catalog/
├── Product.java, Category.java, Supplier.java   # JPA entities (module public API)
├── dto/response/                                # exposed DTOs
├── repository/                                  # Spring Data JPA
├── validator/                                   # business validation rules
└── internal/
    ├── controllers/      UI*Controller (contract + OpenAPI annotations) → impl/*Controller (@RestController)
    ├── dto/request/      validated input DTOs (@Valid)
    ├── mapper/           MapStruct entity ↔ DTO
    ├── services/         interfaces → impl/ (business logic, @Transactional)
    └── storage/          Cloudinary integration
```

Request flow: **Controller → Service → Validator / Repository → Mapper → response DTO**.
Errors are turned into consistent JSON responses by `GlobalExceptionHandler`
(400 validation, 401 authentication, 403 access denied, 404 not found, 409 conflict).

### Spring profiles

| Profile       | Use           | Characteristics                                                                                     |
|---------------|---------------|-----------------------------------------------------------------------------------------------------|
| `development` | Local machine | `ddl-auto: update`, SQL logging, Swagger enabled, DEBUG logs, defaults for database and JWT         |
| `production`  | Deployment    | `ddl-auto: validate`, Swagger disabled, Actuator limited to `health`/`info`, WARN logs, no defaults |

No profile is active by default: a deployment missing `SPRING_PROFILES_ACTIVE` fails at startup instead of
silently running with the development configuration.

## API endpoints

Base path: `/api/v1`. Catalog `GET` requests are public; everything else requires an
`Authorization: Bearer <token>` header.

| Method              | Route                                                                                  | Access                                                                                  |
|---------------------|----------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------|
| POST                | `/auth/register`                                                                       | Public — creates a `USER` account                                                       |
| POST                | `/auth/login`                                                                          | Public — returns the JWT                                                                |
| GET                 | `/categories`, `/categories/{id}`                                                      | Public                                                                                  |
| POST / PUT / DELETE | `/categories`, `/categories/{id}`                                                      | `MANAGER`, `ADMIN`                                                                      |
| GET                 | `/suppliers`, `/suppliers/{id}`                                                        | Public                                                                                  |
| POST / PUT / DELETE | `/suppliers`, `/suppliers/{id}`                                                        | `MANAGER`, `ADMIN`                                                                      |
| GET                 | `/products`, `/products/{id}`                                                          | Public                                                                                  |
| POST / PUT / DELETE | `/products`, `/products/{id}` (multipart, optional image)                              | `MANAGER`, `ADMIN`                                                                      |
| POST                | `/transactions/purchase`, `/transactions/sell`, `/transactions/return-to-supplier`     | Authenticated                                                                           |
| GET                 | `/transactions/{id}`, `/transactions/search`, `/transactions/search-by-month-and-year` | `MANAGER`, `ADMIN`                                                                      |
| PATCH               | `/transactions/update/{id}` (status change)                                            | `MANAGER`, `ADMIN`                                                                      |
| DELETE              | `/transactions/{id}`                                                                   | `MANAGER`, `ADMIN`                                                                      |
| GET                 | `/users/transactions/{userId}`                                                         | `MANAGER`, `ADMIN`                                                                      |
| GET                 | `/users`                                                                               | `ADMIN`                                                                                 |
| GET                 | `/users/current`                                                                       | Authenticated                                                                           |
| PUT / DELETE        | `/users/{id}`                                                                          | `MANAGER`, `ADMIN`, or the user themselves (changing a role requires `MANAGER`/`ADMIN`) |

Interactive documentation (`development` profile): <http://localhost:8080/swagger-ui.html>.
Ready-to-use request samples live in `backend/src/main/resources/*.http` (IntelliJ HTTP client).

## Environment variables

[`backend/.env.example`](backend/.env.example) lists every variable. Copy it to `backend/.env`
(ignored by Git) and fill it in.

| Variable                             | Required | Default (`development` profile)              | Description                            |
|--------------------------------------|----------|----------------------------------------------|----------------------------------------|
| `SPRING_PROFILES_ACTIVE`             | Yes      | —                                            | `development` or `production`          |
| `DB_URL`                             | Prod     | `jdbc:postgresql://localhost:5432/inventory` | PostgreSQL JDBC URL                    |
| `DB_USERNAME`                        | Prod     | `postgres`                                   | Database user                          |
| `DB_PASSWORD`                        | Prod     | `postgres`                                   | Database password                      |
| `JWT_SECRET`                         | Prod     | built-in dev secret                          | HS256 signing key, **≥ 32 characters** |
| `JWT_EXPIRATION`                     | No       | `86400000` (24 h)                            | Token lifetime (ms)                    |
| `CLOUDINARY_CLOUD_NAME`              | Yes      | —                                            | Cloudinary account                     |
| `CLOUDINARY_API_KEY`                 | Yes      | —                                            | Cloudinary API key                     |
| `CLOUDINARY_API_SECRET`              | Yes      | —                                            | Cloudinary API secret                  |
| `CORS_ALLOWED_ORIGINS`               | Prod     | `localhost:3000/5173/4200`                   | Frontend origins, comma-separated      |
| `SERVER_PORT`                        | No       | `8080`                                       | HTTP port                              |
| `MAX_FILE_SIZE` / `MAX_REQUEST_SIZE` | No       | `5MB` / `6MB`                                | Upload limits                          |

## Running locally

### Prerequisites

- JDK 21
- Docker (for PostgreSQL and integration tests)
- A free Cloudinary account (for image uploads)

### 1. Configure the environment

```bash
cd backend
cp .env.example .env      # then edit .env
```

### 2. Start PostgreSQL

```bash
docker compose up -d
```

This starts PostgreSQL on `localhost:5432` (database `inventory`) and Adminer on <http://localhost:8989>.

### 3. Start the application

**From the command line** — the `development` profile is activated automatically by the Maven plugin:

```bash
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run
```

The variables from `.env` must be present in the shell environment (at least `CLOUDINARY_*`).

**From IntelliJ IDEA** — in *Run → Edit Configurations… → BackendApplication*:

- *Active profiles*: `development` (or environment variable `SPRING_PROFILES_ACTIVE=development`);
- *Environment variables*: add the `CLOUDINARY_*` variables (or point to the `.env` file).

> ⚠️ Never put `spring.profiles.active` in `application-development.yaml`: Spring Boot forbids it in a
> profile-specific file (`InvalidConfigDataPropertyException`).

On startup, Flyway applies the migrations from `src/main/resources/db/migration`, then the API listens on
<http://localhost:8080>. Check: <http://localhost:8080/actuator/health> → `{"status":"UP"}`.

## Creating the first ADMIN account

For security reasons, **public sign-up always creates a `USER` account**, even if the request contains a
`role` field. Only a `MANAGER` or `ADMIN` can change a role through the API afterwards. On a fresh database,
the very first administrator must therefore be promoted **once, directly in the database**.

1. **Create the account** through the API:

   ```http
   POST /api/v1/auth/register
   Content-Type: application/json

   { "username": "admin", "email": "admin@example.com", "password": "********", "phoneNumber": "0600000000" }
   ```

2. **Promote it in the database**, using any SQL client (IntelliJ *Database* tool window, Adminer on
   <http://localhost:8989>, DBeaver, psql, your hosting provider's SQL console):

   ```sql
   UPDATE users SET role = 'ADMIN' WHERE username = 'admin';
   SELECT username, role FROM users;   -- check
   ```

   > In the IntelliJ console, if the transaction mode is *Manual*, click **Commit**.

3. **Log in again** (`POST /api/v1/auth/login`): the role is embedded in the JWT, so the old token still
   carries `USER`.

Further accounts are then promoted through the API (`PUT /api/v1/users/{id}` with `"role": "MANAGER"`),
without touching the database. The procedure is the same in production, on the hosting provider's
database, once the application is deployed (tables are created by Flyway on first startup).

## Testing and quality

```bash
cd backend
./mvnw verify
```

- **Unit tests** (Surefire): services, controllers, mappers, validators, security — in-memory H2.
- **Integration tests** (Failsafe, `*IT`): full application against a real PostgreSQL via Testcontainers,
  including Flyway migrations (Docker required).
- **Modularity tests**: `ModularityTests` checks that no module reaches into another module's `internal`
  classes.
- **Coverage**: JaCoCo fails the build below 80% line coverage; report in
  `backend/target/site/jacoco/index.html`.

## CI/CD pipeline

[`ci-backend.yml`](.github/workflows/ci-backend.yml) runs on every push and pull request touching
`backend/`:

```mermaid
flowchart LR
    A[build-and-test<br/>mvn verify + coverage gate] --> B[docker-image<br/>build Dockerfile]
    B -->|main only| C[push to GHCR<br/>latest + sha-xxxxxxx]
    C --> D[deploy-render<br/>deploy hook + health check]
```

| Job              | When            | What                                                                                                                      |
|------------------|-----------------|---------------------------------------------------------------------------------------------------------------------------|
| `build-and-test` | Every push / PR | Unit + integration tests, 80% coverage gate, test and coverage reports as artifacts                                       |
| `docker-image`   | Every push / PR | Verifies the Dockerfile builds; on `main`, pushes `ghcr.io/ledemkam/inventory-backend` tagged `latest` and `sha-<commit>` |
| `deploy-render`  | Push to `main`  | Triggers the Render deploy hook with the exact image digest, then waits for `/actuator/health/readiness`                  |

Repository settings required for deployment (*Settings → Secrets and variables → Actions*):

| Name                     | Type     | Value                                                                                                         |
|--------------------------|----------|---------------------------------------------------------------------------------------------------------------|
| `RENDER_DEPLOY_HOOK_URL` | Secret   | Deploy hook URL of the Render service                                                                         |
| `RENDER_SERVICE_URL`     | Variable | Public URL of the service, e.g. `https://inventory-backend.onrender.com` (optional, enables the health check) |

## Deployment (Docker)

```bash
cd backend
docker build -t inventory-backend .
docker run -p 8080:8080 --env-file .env -e SPRING_PROFILES_ACTIVE=production inventory-backend
```

The image (multi-stage build, JRE 21, non-root user, Spring Boot layers) enables the `production` profile
by default. To provide on the hosting platform: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`,
`CLOUDINARY_*` and `CORS_ALLOWED_ORIGINS`.

Health checks for the orchestrator: `/actuator/health/liveness` and `/actuator/health/readiness`.

### Render

The API runs on Render as a web service deployed from the GHCR image:

- **Service type**: Web Service → *Deploy an existing image* → `ghcr.io/ledemkam/inventory-backend:latest`.
- **Health check path**: `/actuator/health/readiness`.
- **Environment**: the variables listed above, plus `PORT=8080`.
- **Database**: Render PostgreSQL. Render gives a `postgresql://user:password@host/db` URL; the app
  expects the JDBC form in `DB_URL` (`jdbc:postgresql://host:5432/db`) with `DB_USERNAME` / `DB_PASSWORD`
  set separately.

---

Author: **Eric K.** — [github.com/ledemkam](https://github.com/ledemkam)
