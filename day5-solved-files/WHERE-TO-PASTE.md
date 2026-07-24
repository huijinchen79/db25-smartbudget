# Day 5 – Solved Files (drop-in overlay)

## How this folder works

The `backend/` tree in the repo root ships as a **starter**: every file
listed below still contains `TODO TICKET-Fxxx` markers that students fill in
during Day 5 (Spring Boot + JPA repositories, tickets **F044–F055**).

This `day5-solved-files/` folder mirrors the same relative paths as the
starter and contains **complete, compilable drop-in replacements** with zero
`TODO TICKET-F050 / F051 / F052 / F053` markers. Copy them over the starter
whenever you want a working reference implementation.

Files provided here:

| Path (relative to repo root)                                                | Ticket | What it does                                                                 |
|-----------------------------------------------------------------------------|--------|------------------------------------------------------------------------------|
| `backend/src/main/resources/application.properties`                         | F045   | H2 in-memory config, JPA `create-drop`, `data.sql` init, H2 console enabled. |
| `backend/src/main/java/com/smartbudget/repository/UserRepository.java`      | F050   | `findByEmail`, `existsByEmail`, `findByNameContainingIgnoreCase`.            |
| `backend/src/main/java/com/smartbudget/repository/TransactionRepository.java` | F051 | Per-user feed, type filter, date-range filter, `@Query` COALESCE SUM.        |
| `backend/src/main/java/com/smartbudget/repository/CategoryRepository.java`  | F052   | `findByType` for INCOME / EXPENSE dropdown filtering.                        |
| `backend/src/main/resources/data.sql`                                       | F053   | Original 15 seed transactions + 10 additional April rows (25 total).         |

Study-only tickets (F044, F046–F049, F054, F055) are **not** in this folder —
their files (`entity/*.java`, the study-only exercises) are unchanged from the
starter. Also **not** included: `repository/SavingsGoalRepository.java` — its
`TODO TICKET-F061` belongs to Day 6, not Day 5.

## Overlay command

From the repo root:

```bash
cp -R day5-solved-files/backend/ backend/
```

The trailing slash on `backend/` matters — it copies the *contents* of
`day5-solved-files/backend/` into the existing `backend/` tree, overwriting
only the five files above and leaving everything else untouched.

## How to run

```bash
cd backend
mvn -q spring-boot:run
```

Wait for `Started SmartBudgetApplication in X seconds`, then:

1. **H2 console** — open `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:mem:smartbudget`
   - User: `sa`
   - Password: *(blank)*
   - Click **Connect**. You should see `USERS`, `CATEGORIES`, `TRANSACTIONS`,
     `SAVINGS_GOALS` in the left tree.

2. **Verify seed counts:**
   ```sql
   SELECT COUNT(*) FROM TRANSACTIONS;   -- 25
   SELECT COUNT(*) FROM USERS;          --  5
   SELECT COUNT(*) FROM CATEGORIES;     --  5
   SELECT COUNT(*) FROM SAVINGS_GOALS;  --  4
   ```

3. **REST endpoints** (if the Day 1–4 controllers are wired up):
   ```bash
   curl http://localhost:8080/api/categories
   curl http://localhost:8080/api/users
   curl http://localhost:8080/api/transactions
   curl http://localhost:8080/api/transactions/user/1     # Alice, newest first
   curl http://localhost:8080/api/users/by-email/alice@bank.com
   ```

If a controller/endpoint above doesn't exist yet, that's fine — it comes in
a later day. The repository methods themselves compile and work as soon as
the app starts (Spring Data validates every derived method name at startup
and fails fast on typos, so a clean boot is proof the queries are correct).
