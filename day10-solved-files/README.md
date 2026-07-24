# SmartBudget – Personal Finance Tracker

> **Deutsche Bank | TDI 2026 | Foundation Track**

![CI](https://github.com/<your-org>/smartbudget/actions/workflows/ci.yml/badge.svg)

A full-stack personal-finance tracker built across a 10-day training programme:
PostgreSQL / JPA on the backend, React + Recharts on the frontend, packaged
with Docker + docker-compose, and tested on every push through GitHub Actions.

---

## Architecture

```
Browser
   │ :80
   ▼
+-------------------+          +---------------------+          +----------------+
|  nginx (frontend) | ── /api ▶|  Spring Boot (backend)| ── JDBC ▶|  PostgreSQL 15 |
|  Serves React SPA |          |  REST + JPA          |          |  (db service)  |
+-------------------+          +---------------------+          +----------------+
       :80                             :8080                            :5432
```

All three services run in the same Docker network. They reach each other by
service name (`db`, `backend`, `frontend`) — no hardcoded IPs.

---

## Tech Stack

| Layer     | Technology                          |
|-----------|-------------------------------------|
| Database  | PostgreSQL 15 (prod) / H2 (dev)     |
| Backend   | Java 25, Spring Boot 3, Spring Data JPA, Actuator |
| Testing   | JUnit 5, Mockito, MockMvc           |
| Frontend  | React 18 + Vite, Recharts           |
| Serving   | nginx (SPA fallback + `/api` proxy) |
| Container | Docker + docker-compose             |
| CI        | GitHub Actions (JDK 25, Node 22)    |

---

## Prerequisites

- **Java 25** (Eclipse Temurin recommended)
- **Node.js 22** and npm
- **Docker Desktop** (for the containerised path)
- Optional: `psql` or pgAdmin for inspecting the DB directly

---

## Quick Start — Local Dev (H2, no Docker)

Two terminals; both hot-reload on save.

**Backend** (embedded H2, seed data pre-loaded):
```bash
cd backend
./mvnw spring-boot:run
```

| URL | What you see |
|-----|-------------|
| http://localhost:8080/actuator/health   | `{"status":"UP"}` |
| http://localhost:8080/api/transactions  | JSON array of seed transactions |
| http://localhost:8080/api/users         | JSON array of 5 seed users |
| http://localhost:8080/api/goals/user/1  | JSON array of savings goals |
| http://localhost:8080/h2-console        | Visual DB browser |

H2 console: JDBC URL `jdbc:h2:mem:smartbudget` · Username `sa` · Password *(blank)*.

**Frontend** (Vite dev server):
```bash
cd frontend
npm install
npm run dev
```

Open **http://localhost:5173** — full UI wired to the running backend.

---

## Quick Start — Docker (PostgreSQL, production-like)

One command brings up all three services:

```bash
docker-compose up --build
```

| URL | Served by | Purpose |
|-----|-----------|---------|
| http://localhost                       | nginx (frontend) | React UI |
| http://localhost/api/transactions      | nginx → backend  | Proxied API call |
| http://localhost/actuator/health       | nginx → backend  | Health check |
| http://localhost:8080/api/transactions | backend direct   | Bypass proxy (debug) |
| `psql -h localhost -U sb_user smartbudget` | PostgreSQL   | DB shell |

Common Docker commands:
```bash
docker-compose up -d --build   # detached (background)
docker-compose logs -f         # tail all logs
docker-compose ps              # show status + health
docker-compose down            # stop (keeps pgdata volume)
docker-compose down -v         # stop AND wipe database
docker-compose build --no-cache  # force a clean rebuild
```

---

## Environment Variables (prod / Docker)

Consumed by the backend when `SPRING_PROFILES_ACTIVE=prod` (set by docker-compose):

| Variable                       | Example                                             |
|--------------------------------|-----------------------------------------------------|
| `SPRING_DATASOURCE_URL`        | `jdbc:postgresql://db:5432/smartbudget`             |
| `SPRING_DATASOURCE_USERNAME`   | `sb_user`                                           |
| `SPRING_DATASOURCE_PASSWORD`   | `sb_pass`                                           |
| `SPRING_JPA_HIBERNATE_DDL_AUTO`| `update` (never `create-drop` in prod!)             |
| `SPRING_SQL_INIT_MODE`         | `never`                                             |

Spring's RelaxedBinder converts `SPRING_DATASOURCE_URL` to `spring.datasource.url`.

---

## API Reference

| Method | Endpoint                             | Description                     |
|--------|--------------------------------------|---------------------------------|
| GET    | `/api/transactions`                  | All transactions                |
| POST   | `/api/transactions`                  | Create transaction              |
| GET    | `/api/transactions/user/{userId}`    | Transactions by user            |
| PUT    | `/api/transactions/{id}`             | Update transaction              |
| DELETE | `/api/transactions/{id}`             | Delete transaction              |
| GET    | `/api/users`                         | All users                       |
| POST   | `/api/users`                         | Create user                     |
| GET    | `/api/categories`                    | All categories                  |
| GET    | `/api/goals/user/{userId}`           | Goals for user                  |
| PUT    | `/api/goals/{id}/contribute`         | Contribute to a savings goal    |
| GET    | `/actuator/health`                   | Health check (`{"status":"UP"}`)|

Every endpoint is reachable both directly (`localhost:8080/...`) and through
the nginx proxy (`localhost/api/...`) when running under docker-compose.

---

## Testing

**Backend** — JUnit 5 + Mockito + MockMvc:
```bash
cd backend
./mvnw test
```

**Frontend** — Vite dev-server smoke test:
```bash
cd frontend
npm run build      # verifies the bundle compiles
```

CI runs the backend suite on every push (see `.github/workflows/ci.yml`).

---

## Project Structure

```
smartbudget/
├── backend/                          Spring Boot API
│   ├── src/main/java/com/smartbudget/
│   │   ├── config/     CorsConfig, etc.
│   │   ├── entity/     User, Category, Transaction, SavingsGoal (@Entity)
│   │   ├── repository/ 4 JPA repositories
│   │   ├── controller/ 4 REST controllers
│   │   ├── service/    TransactionService, SavingsGoalService
│   │   ├── dao/        DatabaseConnection, TransactionDAO (JDBC)
│   │   ├── exception/  Custom exceptions + GlobalExceptionHandler
│   │   └── console/    Main.java (Day 2 console menu)
│   ├── src/test/       JUnit + Mockito + MockMvc
│   ├── src/main/resources/
│   │   ├── application.properties         (H2 dev defaults)
│   │   └── application-prod.properties    (PostgreSQL, activated in Docker)
│   └── Dockerfile                          (multi-stage: Maven → JRE)
│
├── frontend/                         React + Vite
│   ├── src/
│   │   ├── components/  Navbar, MonthlySummaryChart, Feedback
│   │   ├── hooks/       useBudgetAPI.js
│   │   ├── pages/       Dashboard, TransactionList, AddTransaction, SavingsGoals
│   │   └── styles/      global.css (DB Blue #003366 theme)
│   ├── Dockerfile                          (multi-stage: Node → nginx)
│   └── nginx.conf                          (SPA fallback + /api proxy)
│
├── db/                               SQL scripts (create_tables, seed_data, queries)
├── docker-compose.yml                3 services: db, backend, frontend
├── .github/workflows/ci.yml          GitHub Actions pipeline
└── StudentGuides/                    Day-by-day guides (Day0 → Day10)
```

---

## What Ships in Each Sprint

| Day | Sprint | What Students Build |
|-----|--------|---------------------|
| 1   | 0  | DB schema, seed data, 5 SQL queries, ER diagram |
| 2   | 1  | Java POJOs, console menu app |
| 3   | 2  | OOP: BaseTransaction → Income/Expense, plain-Java TransactionService |
| 4   | 3  | JDBC DAO, Streams/Lambdas, JUnit + Mockito |
| 5   | 4  | Spring: custom queries, H2 console, seed data |
| 6   | 5  | Service layer, controller refactor, MockMvc tests |
| 7   | 6  | HTML/CSS/JS static pages |
| 8   | 7  | React hooks (`useBudgetAPI.js`), replace mock data with real API |
| 9   | 8  | Filters, bar chart, edit, contribute, toasts, polish |
| 10  | 9  | Docker, docker-compose, GitHub Actions CI/CD, final demo |

---

## Troubleshooting

| Symptom | Cause / Fix |
|---------|-------------|
| `docker-compose up` — backend crashes with "Connection refused" | Postgres wasn't ready. Fixed by the `healthcheck` + `condition: service_healthy` in `docker-compose.yml`. |
| Port 80 already in use | Another web server on the host. Stop it, or change `"80:80"` → `"8081:80"`. |
| Hard-refresh on `/transactions` returns 404 | `nginx.conf` is missing `try_files $uri $uri/ /index.html;`. |
| Frontend loads but every `/api/*` returns 404 | `proxy_pass` target is wrong. Must be `http://backend:8080`, not `http://localhost:8080`. |
| Data disappears between restarts | You ran `docker-compose down -v`. The `-v` flag wipes named volumes. Use `down` without `-v` to preserve. |
| CI is green but Docker image build fails locally | Add `docker-build` job locally: `docker-compose build`. |

---

## Deployment Notes

- Never ship `spring.jpa.hibernate.ddl-auto=create-drop` to prod — it wipes user data on every restart. Prod uses `update`.
- Never leave `spring.h2.console.enabled=true` on a public host — it exposes a remote SQL shell.
- The `pgdata` named volume persists across `docker-compose down`. Take regular snapshots (`docker run --rm -v smartbudget_pgdata:/data -v $PWD:/backup alpine tar czf /backup/pgdata.tgz -C /data .`) before destructive operations.
- Scaling: multiple backend replicas behind a load balancer; move Postgres to a managed service (RDS / Cloud SQL); add Redis for session/response caching.

---

*SmartBudget — Deutsche Bank | TDI 2026 Foundation Track*
