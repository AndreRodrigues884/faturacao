# Saldo

**Invoicing and expense management system for small businesses**, with a Spring Boot API, an Angular frontend and PostgreSQL. The whole system starts with a single Docker command.

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4-6DB33F?logo=springboot&logoColor=white)
![Angular](https://img.shields.io/badge/Angular-22-DD0031?logo=angular&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)
![Tests](https://img.shields.io/badge/tests-JUnit%20%7C%20Testcontainers%20%7C%20MockMvc-success)

> ⚠️ **Demo project.** It follows Portuguese invoicing rules (VAT per rate, invoice series, gapless sequential numbering), but it is **not certified by the Portuguese Tax Authority (AT)** and must not be used for real invoicing.

---

## Demo video

https://github.com/user-attachments/assets/1b8688eb-efcd-4769-8f3b-06cfa5b7445e


## Features

| Area | Features |
|---|---|
| **Invoices** | Drafts with dynamic lines, issuing with a sequential number per series and year (`FT 2026/0001`), payment, cancellation with a reason, overdue detection |
| **Expenses** | Recorded from the supplier's document, by category and payment method, with supplier VAT number validation |
| **Catalogue** | Clients (with Portuguese VAT number check-digit validation), products and services with VAT rates (23%, 13%, 6%), categories |
| **Dashboard** | Yearly revenue, expenses and result, receivables, monthly chart, expenses by category and most overdue invoices |
| **Users** | JWT authentication, **Admin** and **User** roles, account management, password change and reset |
| **Demo** | Optional `demo` profile with a sample company, twelve months of data and a one-click demo login |

## Architecture

```mermaid
flowchart LR
    U["Browser"] -->|"HTTP :4000"| N

    subgraph Docker Compose
        N["frontend<br/>nginx + Angular"] -->|"/api → :8080"| B["backend<br/>Spring Boot"]
        B -->|"JDBC :5432"| D[("db<br/>PostgreSQL 17")]
    end
```

The backend is organised **by feature** (`invoice`, `expense`, `client`, ...), and every feature follows the same layers:

```mermaid
flowchart LR
    C["Controller<br/>HTTP and validation"] --> S["Service<br/>transactions and orchestration"]
    S --> E["Entity<br/>business rules"]
    S --> R["Repository<br/>data access"]
    R --> DB[("PostgreSQL")]
```

### Stack

| Layer | Technologies |
|---|---|
| Backend | Java 21, Spring Boot 4, Spring Data JPA (Hibernate 7), Spring Security, Bean Validation |
| Database | PostgreSQL 17, Flyway |
| Frontend | Angular 22, Angular Material, RxJS, Chart.js |
| Tests | JUnit 5, AssertJ, Testcontainers, MockMvc, Spring Security Test |
| Infrastructure | Docker (multi-stage builds), Docker Compose, nginx |

---

## Try it in 2 minutes (Docker)

Requirement: **Docker**.

```bash
git clone https://github.com/AndreRodrigues884/faturacao.git
cd faturacao
cp .env.example .env
```

Fill in `JWT_SECRET` (48 random bytes, Base64-encoded) and `ADMIN_PASSWORD` in `.env`. Then:

```bash
docker compose up -d --build
```

Open **http://localhost:4000** and click **"Entrar com a conta de demonstração"** to explore the sample company, or sign in as admin with the `ADMIN_EMAIL` and `ADMIN_PASSWORD` from `.env`.

| Service | Address |
|---|---|
| Application | http://localhost:4000 |
| API | http://localhost:8080/api |
| PostgreSQL | `localhost:5433` (user and password `faturacao`) |

## Tests

```bash
./mvnw test
```

Docker must be running: the integration tests spin up a disposable PostgreSQL with **Testcontainers**.

| Type | Coverage |
|---|---|
| **Unit** | VAT number validation, VAT calculation and rounding, invoice totals and lifecycle, expense rules. No Spring and no database: they run in milliseconds |
| **Integration** | Issuing against a real PostgreSQL, **20 simultaneous issue requests**, double-click on *Issue*, database constraints |
| **API** | HTTP contract (status codes, headers, `ProblemDetail`), validation, pagination, authentication and authorisation with real JWT tokens |

---

## API

Every endpoint except login requires `Authorization: Bearer <token>`. A Postman collection is available in [`postman/`](postman/).

| Resource | Main endpoints |
|---|---|
| Authentication | `POST /api/auth/login` · `GET /api/auth/me` · `POST /api/auth/change-password` |
| Invoices | `GET /api/invoices` (filters and pagination) · `POST` · `GET/PUT/DELETE /{id}` · `POST /{id}/issue` · `POST /{id}/pay` · `POST /{id}/cancel` |
| Expenses | `GET /api/expenses` (filters and pagination) · `POST` · `PUT/DELETE /{id}` |
| Clients | `GET /api/clients?search=` · `POST` · `GET/PUT/DELETE /{id}` |
| Products | `GET /api/products?includeInactive=` · `POST` · `PUT /{id}` · `DELETE /{id}` (deactivates) · `POST /{id}/activate` |
| Categories | `GET /api/categories` · `POST` · `GET/PUT/DELETE /{id}` |
| Users *(admin)* | `GET /api/users` · `POST` · `PUT /{id}` · `POST /{id}/deactivate` · `POST /{id}/activate` · `POST /{id}/reset-password` |
| Dashboard | `GET /api/dashboard?year=` |
| Demo *(demo profile only)* | `GET /api/demo/credentials` |

Errors always follow the `ProblemDetail` format:

```json
{
  "status": 400,
  "title": "Dados inválidos",
  "detail": "Um ou mais campos têm valores inválidos",
  "errors": { "lines[0].quantity": "A quantidade tem de ser maior que zero" }
}
```

> The user-facing messages are in Portuguese, as the application targets Portuguese businesses.

---

## Project structure

```
faturacao/
├── src/main/java/pt/andrerodrigues/faturacao/
│   ├── auth/          login, JWT, current user
│   ├── user/          account management
│   ├── config/        security, properties, initial admin
│   ├── common/        errors, pagination, VAT number validation
│   ├── category/      categories
│   ├── client/        clients
│   ├── product/       products, VAT rates
│   ├── invoice/       invoices: domain/, dto/, numbering/
│   ├── expense/       expenses
│   ├── dashboard/     dashboard aggregations
│   └── demo/          sample company and demo login (demo profile only)
├── src/main/resources/db/migration/   Flyway migrations (V1 to V12)
├── src/test/                          unit, integration and API tests
├── frontend/                          Angular application
│   └── src/app/
│       ├── core/      authentication, interceptor, guards, API errors
│       ├── shared/    reusable dialogs and validators
│       ├── layout/    side menu and top bar
│       └── features/  one folder per area (invoices, expenses, ...)
├── docker-compose.yml
└── Dockerfile
```

---

## What I learned

This was my first project with Spring Boot and Angular, built to learn both properly rather than to follow a tutorial. Along the way I worked through:

- Designing a **layered backend** and deciding where each rule belongs, and why business rules are safer inside the entities than in controllers.
- **Transactions and locking** in practice: reproducing a race condition in a test before fixing it, and understanding when to use pessimistic or optimistic locking.
- The pitfalls of **money and dates**: floating-point errors, rounding modes, and time zones that turn the 15th into the 14th.
- **JPA beyond the basics**: lazy loading, the N+1 problem, dirty checking, and when to drop down to the `EntityManager` for aggregations.
- **Security** end to end, from password hashing and JWT validation to the difference between authentication (401) and authorisation (403), and why frontend guards are not security.
- **Testing at three levels** and choosing what each level should prove.
- Shipping software that **anyone can run**, with Docker images, environment-based configuration and secrets kept out of the code.


## Author

**André Rodrigues** · BSc in Web Information Technologies and Systems (ESMAD, Polytechnic of Porto)

[GitHub](https://github.com/AndreRodrigues884) · [LinkedIn](https://linkedin.com/in/andrerodrigues-dev) · [Portfolio](https://andrerodrigues884.github.io/Portfolio/#/)
