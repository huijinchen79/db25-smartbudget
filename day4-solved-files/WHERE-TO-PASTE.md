# Day 4 — Where to paste the solved files

Day 4 is Sprint 3: **JDBC + PreparedStatement, HashMap + Streams + Lambdas, JUnit 5**.
This folder is a mirror of `backend/` containing only the files Day 4 touches, with every
`TODO TICKET-F0**` marker replaced by real code.

---

## How this folder works

- The real `backend/` tree ships each Day-4 file with a `// TODO TICKET-F0xx` comment
  scaffold. Under each TODO you'll see `WHAT / HOW / WHY / OBSERVE` teaching prose but the
  method body itself is empty.
- This `day4-solved-files/backend/` folder contains **COMPLETE drop-in replacement files**
  for every file the Day-4 tickets touch. Zero `TODO TICKET` markers remain in code — Day 4
  or earlier.
- Day 4 is **cumulative** on top of Day 3. `TransactionService.java` therefore implements
  BOTH Day-3 tickets (F026..F031: List → CSV I/O) AND Day-4 tickets (F032..F034: HashMap
  refactor + Stream methods + Lambda comparator). Day 6's `@Service` upgrade (F063) is left
  as a narrative comment section — it's applied when you paste `day6-solved-files/` on top.
- `TransactionServiceTest.java` implements ONLY Day 4 (F040..F043). It targets the
  plain-Java `TransactionService` (HashMap flavour), so no Mockito wiring is required —
  the tests just `new TransactionService()` in `@BeforeEach`. When Day 6 refactors the
  service to depend on JPA repositories, the tests will be re-written with
  `@ExtendWith(MockitoExtension.class)`, `@Mock`, and `@InjectMocks`.

---

## Files in this folder

| File | What it now contains |
|------|----------------------|
| `backend/src/main/java/com/smartbudget/service/TransactionService.java` | **Day 3 + Day 4 combined.** `Map<String, BaseTransaction>` storage; `addTransaction` (with null + blank-description validation); `findById` (O(1)); `delete`; `getAll` (defensive copy); `filterByDateRange`; `calculateTotalByType`; `exportToCSV`/`importFromCSV`; F033 stream methods (`getExpensesOver100`, `getSortedByDate`, `getSortedByDateDesc`); F034 lambda `getSortedByAmount`. |
| `backend/src/main/java/com/smartbudget/dao/DatabaseConnection.java` | F035: `DriverManager.getConnection(URL, USERNAME, PASSWORD)` utility, private constructor. Credentials from Day 1: `jdbc:postgresql://localhost:5432/smartbudget`, user `sb_user`, pass `sb_pass`. |
| `backend/src/main/java/com/smartbudget/dao/TransactionDAO.java` | F036..F039: raw-JDBC CRUD. `insert`, `getAll`, `getByUserId`, `delete` — all via `PreparedStatement` inside try-with-resources. Shared `mapRow(ResultSet)` helper. Adapts to the JPA `Transaction` entity (which models user/category as objects) by building thin `User` / `Category` shells carrying just their IDs. |
| `backend/src/test/java/com/smartbudget/service/TransactionServiceTest.java` | F040..F043: JUnit 5 tests for the Day-4 `TransactionService`. `@BeforeEach` builds a fresh SUT + income/expense fixtures. Covers add + getAll, defensive copy, delete (present + missing), and `assertThrows` for negative/zero amount and future date. |

---

## Overlay

From the repo root, either:

- **Full overlay** — copies the whole subtree onto the starter (recommended):

  ```bash
  cp -R day4-solved-files/backend/ backend/
  ```

- **Side-by-side reading** — open each file next to its starter twin (e.g. IntelliJ's
  Compare With…) to see exactly which lines the Day-4 TODOs asked you to write.

---

## Sanity check

After overlaying:

```bash
cd backend
mvn -q test
```

You should see something like:

```
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

`TransactionServiceTest` runs entirely in-memory — **no PostgreSQL required.**

### Live-DB smoke test (optional)

The DAO (`TransactionDAO` + `DatabaseConnection`) is not covered by the Day-4 JUnit tests
because those would require a live database. To exercise the DAO yourself:

1. Bring up Postgres from Day 1:

   ```bash
   docker compose up -d db          # or your local `pg_ctl start`
   ```

   The `smartbudget` database, `sb_user` role, and `transactions` table must already exist
   from Day 1's schema + seed scripts (`db/schema.sql`, `db/seed.sql`).

2. Drop a quick `main` into `console/Main.java` (or a scratch class) that calls:

   ```java
   TransactionDAO dao = new TransactionDAO();
   dao.getAll().forEach(System.out::println);
   ```

   If Postgres is not running you'll get a clear `SQLException: Connection refused ...` —
   that's the expected "clear error" from F035's acceptance criteria.

---

## Notes / gotchas

- **HashMap loses insertion order.** `getAll()` no longer returns transactions in the
  order they were added. Any test that asserts a specific order needs to sort or key by
  id. If insertion order matters, swap `HashMap` for `LinkedHashMap` (same interface,
  ordered iteration).
- **Entity vs POJO.** The DAO works with `com.smartbudget.entity.Transaction` (the JPA
  entity — user/category as objects), while the service works with
  `com.smartbudget.model.BaseTransaction` and its `IncomeTransaction`/`ExpenseTransaction`
  subclasses. They intentionally live in different layers: the DAO is the raw-JDBC
  stepping-stone that Day 5's Spring Data JPA repository will replace.
- **Day-4 tests do not touch the DAO.** The DAO's contract is a live-DB integration
  concern; the Day-4 unit tests focus on the in-memory service so `mvn test` stays green
  even without Docker/Postgres.
- **`TODO TICKET` markers remaining in this folder = 0** for Day 4 and earlier tickets.
  Verify with:

  ```bash
  grep -rc "TODO TICKET" day4-solved-files/backend/
  ```
