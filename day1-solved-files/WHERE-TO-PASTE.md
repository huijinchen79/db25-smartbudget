# Day 1 — Solved Files: Where to Paste

## How this folder works

- The real `db/` folder at the repo root ships with `TODO TICKET-F...` stubs
  that students fill in during Day 1.
- **This folder (`day1-solved-files/`) contains COMPLETE drop-in replacement
  files** for every `db/` file that Day 1 tickets touch. Zero TODO markers,
  zero placeholders — every SQL statement is ready to run.
- Use these files as a reference solution *or* overlay them directly onto the
  real `db/` folder when you want a fully working database with no manual work.

## Files in this folder

| Solved file                            | Replaces                     | Tickets it satisfies       |
| -------------------------------------- | ---------------------------- | -------------------------- |
| `day1-solved-files/db/create_tables.sql` | `db/create_tables.sql`     | F003 (CREATE TABLE), F004 (constraints) |
| `day1-solved-files/db/seed_data.sql`     | `db/seed_data.sql`         | F005 (seed data), F004 (constraint tests, commented) |
| `day1-solved-files/db/queries.sql`       | `db/queries.sql`           | F006 (5 queries), F007 (JOIN), F008 (VIEW), F009 (window fn), F010 (CTE) |

Tickets F001 (setup) and F002 (ER diagram on paper / draw.io) touch no code
and therefore have no file in this folder.

## Overlay command (from the repo root)

Copy the solved files on top of the stub files:

```bash
cp -R day1-solved-files/db/ ./db/
```

Prefer to see the diff instead of overwriting? Open the two files side-by-side:

```bash
diff -u db/create_tables.sql day1-solved-files/db/create_tables.sql
diff -u db/seed_data.sql     day1-solved-files/db/seed_data.sql
diff -u db/queries.sql       day1-solved-files/db/queries.sql
```

## How to run

### 1. Create the database and user (one-time, as the postgres super-user)

```bash
psql -U postgres
```

Inside the psql prompt:

```sql
CREATE DATABASE smartbudget;
CREATE USER sb_user WITH PASSWORD 'sb_pass';
GRANT ALL PRIVILEGES ON DATABASE smartbudget TO sb_user;
\q
```

### 2. Run the three SQL files in order (as `sb_user`)

```bash
psql -U sb_user -d smartbudget -f db/create_tables.sql \
  && psql -U sb_user -d smartbudget -f db/seed_data.sql \
  && psql -U sb_user -d smartbudget -f db/queries.sql
```

`create_tables.sql` starts with `DROP TABLE IF EXISTS ...` in reverse
dependency order, so the whole three-file sequence is idempotent — re-run it
whenever you want a clean slate.

### 3. Verify

```bash
psql -U sb_user -d smartbudget
```

```sql
\dt                                                 -- 4 tables listed
SELECT COUNT(*) FROM users;                         -- 5
SELECT COUNT(*) FROM categories;                    -- 5
SELECT COUNT(*) FROM transactions;                  -- 15
SELECT COUNT(*) FROM savings_goals;                 -- 4
SELECT * FROM top_expense_categories;               -- 3 rows, biggest spend first
```

## Note on naming (judgment call)

The starter stub `db/create_tables.sql` referred to the savings-goals label
column as `name`. The Day 1 guide (Hints 3 of F002 and F004) uses `goal_name`
and both `seed_data.sql` and later JPA entities (Day 5) assume `goal_name`.
Per the "guide is authoritative when they disagree" rule, these solved files
use **`goal_name`**.

## What comes next

Day 2 (`StudentGuides/Day2-README.md`) starts building Java classes that map
to these tables — `User`, `Category`, `Transaction`, `SavingsGoal` — so keep
this schema stable before moving on.
