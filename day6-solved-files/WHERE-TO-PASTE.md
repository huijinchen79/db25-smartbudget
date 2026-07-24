# Day 6 — Solved Files: Where to Paste

## How this folder works

The training project (`/backend`) ships with **starter files** that contain
`TODO TICKET-Fxxx` markers where the student has to write code. This
`day6-solved-files/` folder mirrors the repository layout and provides the
**drop-in replacements** for every file that Day 6 tickets (F056 – F067)
touch. Each solved file is:

* a **complete** compilable Java class (no TODO markers for Day 6 or earlier
  tickets),
* **cumulative** — `TransactionService.java` implements Day 3 (F026–F030),
  Day 4 (F032–F034) *and* Day 6 (F063) in one file, per the guide,
* preserved in package layout so you can overlay directly on top of the
  starter without any path fixup.

`TICKET-F068` is Postman testing and is not represented here (no source file
to write).

---

## Files in this folder

```
day6-solved-files/backend/
├── src/main/java/com/smartbudget/
│   ├── controller/
│   │   ├── TransactionController.java     (F056–F059)
│   │   ├── UserController.java            (F060)
│   │   └── SavingsGoalController.java     (F061, F062)
│   ├── service/
│   │   ├── TransactionService.java        (F026–F030, F032–F034, F063)
│   │   └── SavingsGoalService.java        (F061, F062 — provided for symmetry)
│   ├── exception/
│   │   └── GlobalExceptionHandler.java    (F065 — new file)
│   └── repository/
│       └── SavingsGoalRepository.java     (F061 — adds findByUser_UserId)
└── src/test/java/com/smartbudget/
    └── controller/
        └── TransactionControllerTest.java (F064, F066, F067)
```

Files **not** included (unchanged from the starter or from earlier day-solved
folders): entities, the other repositories, `application.properties`,
`data.sql`, and the two exception classes (`InvalidTransactionException`,
`ResourceNotFoundException` — their constructors are simple enough that
they're implemented by earlier day-solved folders or can be filled in
per the Day 3 hints).

---

## Overlay command

From the repo root:

```bash
cp -R day6-solved-files/backend/ backend/
```

This copies every file above into the corresponding location under
`/backend`, overwriting the starter stubs. The command is idempotent.

> **Important — apply solved folders in order.**
> Day 6 depends on repositories added in Day 5 (`findByEmail`,
> `existsByEmail`, `findByType`, `findByTxnDateBetween`,
> `findByUser_UserIdOrderByTxnDateDesc`, `@Query sumByUserAndType`) and on
> the exception classes / entity POJOs from Day 3. If you overlay
> `day6-solved-files/` on top of a fresh starter that has NOT received the
> Day 1 → Day 5 solved folders, the app will fail to start with
> `NoSuchMethodError` on the missing repository methods, or with
> unresolved constructors on `InvalidTransactionException` /
> `ResourceNotFoundException`. Apply Day 1 → Day 6 solved folders in
> sequence before running.

---

## How to run

**Start the server:**

```bash
cd backend
mvn -q spring-boot:run
```

Then from another terminal:

```bash
# List all transactions
curl -i http://localhost:8080/api/transactions

# List one user's transactions (newest-first)
curl -s http://localhost:8080/api/transactions/user/1 | jq .

# Create a transaction (201 Created)
curl -i -X POST http://localhost:8080/api/transactions \
  -H 'Content-Type: application/json' \
  -d '{
        "user":        {"userId": 1},
        "category":    {"categoryId": 1},
        "amount":      100.50,
        "txnDate":     "2026-05-01",
        "description": "Lunch",
        "type":        "EXPENSE"
      }'

# Negative amount -> 400 Bad Request
curl -i -X POST http://localhost:8080/api/transactions \
  -H 'Content-Type: application/json' \
  -d '{"user":{"userId":1},"category":{"categoryId":1},"amount":-50,"txnDate":"2026-05-01","description":"x","type":"EXPENSE"}'

# Missing id -> 404 Not Found (from GlobalExceptionHandler)
curl -i http://localhost:8080/api/transactions/9999
curl -i -X DELETE http://localhost:8080/api/transactions/9999

# Users
curl -s http://localhost:8080/api/users | jq .
curl -i http://localhost:8080/api/users/999          # -> 404
curl -i -X POST http://localhost:8080/api/users \
  -H 'Content-Type: application/json' \
  -d '{"name":"Frank","email":"frank@bank.com"}'

# Savings goals
curl -s http://localhost:8080/api/goals/user/1 | jq .
curl -i -X PUT http://localhost:8080/api/goals/1/contribute \
  -H 'Content-Type: application/json' -d '{"amount": 100}'
curl -i -X PUT http://localhost:8080/api/goals/1/contribute \
  -H 'Content-Type: application/json' -d '{"amount": 0}'       # -> 400
```

**Run the MockMvc integration tests:**

```bash
cd backend
mvn -q test
```

Expected output: `TransactionControllerTest` runs six tests, all green
(smoke-test, POST-happy, POST-negative-amount, POST-missing-user,
POST-invalid-type, GET-by-user).

---

## Judgment calls in this solved copy

1. **`TransactionController` uses `TransactionService`, not the repository
   directly.** The guide's F056 Hint 3 shows a repo-injected controller for
   the very first step, but F063 explicitly says to move to the service. The
   solved copy uses the service everywhere, so all validation runs through
   one code path.
2. **`SavingsGoalController` uses the repository directly.** Matches the
   F061 / F062 Hint 3 verbatim. A parallel `SavingsGoalService` is still
   provided in `service/` so the tree carries no lingering Day-6 TODOs and
   later refactors can switch over without new plumbing.
3. **`ContributionRequest` is a nested `record`** inside
   `SavingsGoalController` — mirrors the guide's Hint 3 exactly and keeps
   the request-body carrier co-located with the endpoint that uses it.
4. **A `GET /api/transactions/{id}` endpoint was added** beyond the strict
   F056–F059 list. It's needed by the F066 test's round-trip assertion
   and is otherwise a natural CRUD completion.
5. **Day 3 in-memory helpers survive in `TransactionService`** under
   `getAllInMemory()`, `filterByDateRange()`, `calculateTotalByType()`,
   CSV import/export, and the Day 4 stream helpers — as the guide says,
   "each day BUILDS on the previous". They live alongside the Day 6 JPA
   methods and do not interfere with them.
6. **`GlobalExceptionHandler` includes a `MethodArgumentNotValidException`
   handler and a catch-all `Exception` handler** on top of the two the
   guide's F065 explicitly asks for. The `@Valid` handler was in the
   starter's Step 3 TODO and is a natural fit; the catch-all keeps 500s
   returning JSON instead of Whitelabel HTML.
