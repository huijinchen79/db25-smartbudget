# Day 10 — Where to paste

## How this folder works

Day 10 finishes SmartBudget: containerises all three services with Docker,
orchestrates them with docker-compose, and adds a GitHub Actions CI pipeline.

The starter repo already ships **scaffolds** for most Day 10 files:

| Path                                    | Starter state                                            |
|-----------------------------------------|----------------------------------------------------------|
| `backend/Dockerfile`                    | Big comment header + `TODO TICKET-F108` stubs (Stage 1 / Stage 2 to write). |
| `frontend/Dockerfile`                   | Big comment header + `TODO TICKET-F109` stubs (Stage 1 / Stage 2 to write). |
| `frontend/nginx.conf`                   | Comment header + `TODO TICKET-F109` server-block skeleton. |
| `docker-compose.yml`                    | Header + `TODO TICKET-F107` skeletons for all three services (uses service name `postgres`, port `3000:80`). |
| `backend/src/main/resources/application-prod.properties` | Already valid (no TODO markers). Day 10 tweaks `ddl-auto` from `validate` → `update`. |
| `README.md`                             | Day-1-style README; Day 10 rewrites it with Docker + CI sections and updated architecture. |
| `.github/workflows/ci.yml`              | **Does not exist** in the starter. Whole `.github/` folder is new. |

Files in this `day10-solved-files/` folder are **complete drop-in replacements**
(and the new `.github/workflows/ci.yml`). They mirror the exact target paths.

Note that the Day 10 guide's Hint 3 uses:
- docker-compose service name **`db`** (not `postgres`)
- host port **`80:80`** for the frontend (not `3000:80`)

Both changes propagate into the backend's `SPRING_DATASOURCE_URL` (`jdbc:postgresql://db:5432/...`)
and the frontend's `nginx.conf` (`proxy_pass http://backend:8080`). The overlay
below installs the guide-aligned versions; if you keep the original starter's
`postgres`/`3000:80` you must overlay *all* of the docker-related files together.

---

## Overlay commands

Run from the repo root:

```bash
cp day10-solved-files/backend/Dockerfile                                       backend/Dockerfile
cp day10-solved-files/frontend/Dockerfile                                      frontend/Dockerfile
cp day10-solved-files/frontend/nginx.conf                                      frontend/nginx.conf
cp day10-solved-files/docker-compose.yml                                       ./
cp -R day10-solved-files/.github                                               ./
cp day10-solved-files/backend/src/main/resources/application-prod.properties   backend/src/main/resources/
cp day10-solved-files/README.md                                                ./     # OVERWRITES existing README.md
```

**Heads-up:** the last command overwrites the existing `README.md`. Diff it first
(`diff day10-solved-files/README.md README.md`) if you added anything to the
starter README you want to keep.

The `cp -R day10-solved-files/.github ./` creates `.github/workflows/ci.yml` — the
whole `.github/` tree is new in Day 10.

---

## How to run

### Local dev (containers)

Requires Docker Desktop running.

```bash
docker-compose down -v            # clean slate (first time can skip)
docker-compose build              # builds all 3 images
docker-compose up                 # foreground; Ctrl+C to stop
```

Three containers come up:

| Service   | Container name        | Where you hit it                           |
|-----------|-----------------------|--------------------------------------------|
| frontend  | smartbudget-frontend  | http://localhost         (nginx, port 80)  |
| backend   | smartbudget-backend   | http://localhost:8080    (Spring Boot API) |
| db        | smartbudget-db        | `psql -h localhost -p 5432 -U sb_user smartbudget` |

Smoke-test through the nginx proxy (proves the whole chain works):
```bash
curl http://localhost/actuator/health              # {"status":"UP"}
curl http://localhost/api/transactions | head
open http://localhost                              # React UI
```

Stop and preserve data (volume kept):
```bash
docker-compose down
```

Stop and wipe the database:
```bash
docker-compose down -v
```

### CI

Push to GitHub — the workflow at `.github/workflows/ci.yml` runs on every push
to `main`/`develop` and on every PR to `main`. It runs three jobs:

1. **build-backend** — Temurin JDK 25, `./mvnw -B test` (compile + JUnit suite).
2. **build-frontend** — Node 22, `npm ci`, `npm run build`.
3. **docker-build** — after both build jobs pass, runs `docker compose build`
   to catch Dockerfile syntax errors that `mvn`/`npm` alone would miss.

Watch the run at: `https://github.com/<your-org>/<repo>/actions`.

Add the badge (already in the README) once you've replaced `<your-org>`:

```
![CI](https://github.com/<your-org>/smartbudget/actions/workflows/ci.yml/badge.svg)
```

---

## Files in this overlay

| File                                                        | Ticket(s)          | Summary |
|-------------------------------------------------------------|--------------------|---------|
| `backend/Dockerfile`                                        | F108               | Multi-stage: `maven:3.9-eclipse-temurin-25` → `eclipse-temurin:25-jre-alpine`; runs as non-root; healthcheck; ~220 MB final image. |
| `frontend/Dockerfile`                                       | F109               | Multi-stage: `node:22-alpine` → `nginx:alpine`; copies Vite `dist/` to nginx web-root; healthcheck. |
| `frontend/nginx.conf`                                       | F109               | Serves SPA with `try_files` fallback, proxies `/api/` and `/actuator/` to `backend:8080`, gzip + 30-day cache for hashed static assets. |
| `docker-compose.yml`                                        | F110, F111         | Three services (`db`, `backend`, `frontend`) + `pgdata` volume + healthchecks + `condition: service_healthy` gating + `restart: unless-stopped`. |
| `.github/workflows/ci.yml`                                  | F113, F114, F115   | Backend test job (JDK 25 + `mvn test` + JUnit report), frontend build job (Node 22 + `npm ci` + `npm run build`), Docker-build job with `needs:` gating. |
| `backend/src/main/resources/application-prod.properties`    | F117               | PostgreSQL datasource via env vars, `ddl-auto=update`, `sql.init.mode=never`, H2 console disabled. |
| `README.md`                                                 | F118               | Rewritten: architecture diagram, prerequisites, dev + Docker quick starts, env-var table, API reference, testing, troubleshooting, deployment notes, CI badge. |

Tickets **F112** (Docker smoke-test), **F116** (deliberately break a test), and
**F119** (final demo) are procedural — no files to write.
