# Day 2 — Where to paste

## How this folder works

The real `backend/` in the repo ships with `// TODO TICKET-F…` stubs so students
have something to fill in. This `day2-solved-files/` folder mirrors the exact
directory layout of `backend/` and contains **complete, drop-in replacements**
for every file Day 2's tickets touch.

You can use it two ways:

1. **Reference** — open the file next to your own and copy the parts you need.
2. **Overlay** — copy the whole tree over `backend/` and you get a working Day 2
   end state in one shot.

## Files in this folder

| Ticket        | File                                                              | Kind             | What it does |
|---------------|-------------------------------------------------------------------|------------------|--------------|
| F012          | `backend/src/main/java/com/smartbudget/model/User.java`           | **new file**     | Plain-Java POJO for `users` (name/email/createdAt). |
| F013          | `backend/src/main/java/com/smartbudget/model/Category.java`       | **new file**     | POJO for `categories` with type validation ("INCOME"/"EXPENSE"). |
| F014          | `backend/src/main/java/com/smartbudget/model/Transaction.java`    | **new file**     | Concrete Day-2 POJO with `BigDecimal` amount + positive-amount check. |
| F015          | `backend/src/main/java/com/smartbudget/model/SavingsGoal.java`    | **new file**     | POJO with derived `getProgressPercentage()` + `isCompleted()`. |
| F016..F020    | `backend/src/main/java/com/smartbudget/console/Main.java`         | **full replace** | Menu loop, seeded ArrayList, formatted list, add w/ validation, summary. |

## Overlay command

Run this from the repo root:

```bash
cp -R day2-solved-files/backend/ backend/
```

That copies:

- The four new POJOs into `backend/src/main/java/com/smartbudget/model/` (they
  didn't exist before Day 2).
- The completed `Main.java` over the starter stub in
  `backend/src/main/java/com/smartbudget/console/`.

Nothing else in `backend/` is touched.

### Curriculum note (Day 2 vs Day 3)

Day 2's `model/Transaction.java` is a **concrete POJO**. From Day 3 onwards the
repo also carries `model/BaseTransaction.java`, `model/IncomeTransaction.java`
and `model/ExpenseTransaction.java` — those are the Day-3 OOP refactor and
**coexist alongside** the Day-2 `Transaction` class. This is intentional. Don't
delete either side.

## How to run

From the repo root:

```bash
cd backend
mvn -q compile exec:java -Dexec.mainClass=com.smartbudget.console.Main
```

Or from IntelliJ: open `Main.java`, right-click inside the class, choose
**Run 'Main.main()'**.

You should see:

```
Seeded 10 transactions

=== SmartBudget Console ===
1) List Transactions
2) Add Transaction
3) Summary
4) Exit
Choice:
```

- `1` prints a 10-row table with aligned columns.
- `2` walks you through amount / date / description / type; bad input is
  rejected with a clear message and the row is skipped.
- `3` prints income, expenses, and net balance (net = 14497.80 with the seed).
- `4` prints `Goodbye!` and exits.
