# Day 3 — Where to paste the solved files

Day 3 is Sprint 2: **OOP — abstract classes, inheritance, polymorphism, custom exception, CSV I/O**.
This folder is a mirror of `backend/` containing only the files Day 3 touches, with every
`TODO TICKET-F0**` marker replaced by real code.

## How this folder works

- The real `backend/` tree ships each Day-3 file with a `// TODO TICKET-F0xx` comment
  scaffold. Under each TODO you'll see `WHAT / HOW / WHY / OBSERVE` teaching prose but the
  method body itself is empty.
- This `day3-solved-files/backend/` folder contains **COMPLETE drop-in replacement files**
  for every file the Day-3 tickets touch. Zero `TODO TICKET` markers remain in code.
- The `TransactionService.java` here implements **only Day 3** (F026..F031). The Day 4
  refactor tickets (F032..F034 — HashMap/Streams/Lambdas) and Day 6 (F063 — Spring
  `@Service`) are kept as narrative comment sections, not empty method signatures, so the
  file compiles as-is under Day-3-only overlay. They get their real bodies when you paste
  `day4-solved-files/` and `day6-solved-files/` on top.

## Files in this folder

| File | What it now contains |
|------|----------------------|
| `backend/src/main/java/com/smartbudget/model/BaseTransaction.java` | Abstract class: 4 protected fields, validating constructor, abstract `getType()`, `toString()`. |
| `backend/src/main/java/com/smartbudget/model/IncomeTransaction.java` | Concrete subclass, `getType()` returns `"INCOME"`. |
| `backend/src/main/java/com/smartbudget/model/ExpenseTransaction.java` | Concrete subclass, `getType()` returns `"EXPENSE"`. |
| `backend/src/main/java/com/smartbudget/exception/InvalidTransactionException.java` | Custom `RuntimeException` used by the validating constructor. |
| `backend/src/main/java/com/smartbudget/service/TransactionService.java` | Plain-Java service (Day 3 only): `List<BaseTransaction>` storage + `addTransaction`, `getAll`, `filterByDateRange`, `calculateTotalByType`, `exportToCSV`, `importFromCSV`, `validate`. |

## Overlay

From the repo root, either:

- **Full overlay** — copies the whole subtree onto the starter:

  ```bash
  cp -R day3-solved-files/backend/ backend/
  ```

- **Side-by-side reading** — open each file next to its starter twin (e.g. IntelliJ's
  Compare With…) to see exactly which lines the Day-3 TODOs asked you to write.

## Sanity check

After overlaying:

```bash
cd backend
mvn -q compile        # should exit 0 (no test run — Day 4 introduces JUnit tests)
```

`mvn -q test` at this point may find 0 tests or expected placeholders — that's OK; the
JUnit + Mockito test suite arrives in `day4-solved-files/`.

## Notes / gotchas

- `model/Transaction.java` from Day 2 (a concrete POJO) is **not** in this folder. Day 3
  introduces the `BaseTransaction` / `IncomeTransaction` / `ExpenseTransaction` hierarchy
  as an evolution — the Day 2 concrete `Transaction` still compiles alongside them. If
  your service or console code references `Transaction` directly, keep it; Day 3 only adds
  the abstract hierarchy, it doesn't demand deletion.
- The validating constructor rejects `amount <= 0` and `txnDate.isAfter(LocalDate.now())`
  with `InvalidTransactionException` — matches the F021/F024 acceptance criteria.
- `exportToCSV` / `importFromCSV` use `try-with-resources` on `BufferedReader` /
  `BufferedWriter`, and reconstruct the correct subtype (`IncomeTransaction` vs
  `ExpenseTransaction`) based on the `type` column — matches F029/F030 Hint 3.
