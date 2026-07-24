-- ============================================================
-- TICKET-F003 (Day 1, Sprint 0) — Create Table Scripts
-- TICKET-F004 (Day 1, Sprint 0) — Add Constraints
-- ============================================================
-- Run order matters! Parent tables MUST be created before child tables
-- because foreign keys reference the parent.
-- Order: users → categories → transactions → savings_goals
-- ============================================================

-- Drop in reverse dependency order so re-running this script is safe.
-- Children first (they hold FKs), then parents.
DROP TABLE IF EXISTS savings_goals;
DROP TABLE IF EXISTS transactions;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;


-- ============================================================
-- TICKET-F003 / F004: "users" table
-- ============================================================
-- WHAT: This is your first table. Every SmartBudget user is stored here.
--       The "user_id" column is the PRIMARY KEY — a unique identifier
--       that other tables will reference via FOREIGN KEYs.
--
-- HOW:  CREATE TABLE with:
--         • user_id    — SERIAL PRIMARY KEY (auto-incrementing integer)
--         • name       — VARCHAR(100), NOT NULL
--         • email      — VARCHAR(150), NOT NULL, UNIQUE
--         • created_at — TIMESTAMP, DEFAULT CURRENT_TIMESTAMP
--
-- WHY:  Without a users table, we can't track who made which transaction.
--       The UNIQUE constraint on email prevents duplicate accounts.
--       NOT NULL ensures every user has a name.
--       DEFAULT CURRENT_TIMESTAMP means we never have to remember to set it.
--
-- OBSERVE: After creating, run \dt in psql — you should see "users" listed.
--          INSERT INTO users (name, email) VALUES ('Test', 'test@db.com');
--          SELECT * FROM users; — you should see user_id = 1 auto-assigned
--          AND created_at auto-populated with the current timestamp.
CREATE TABLE users (
    user_id    SERIAL       PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(150) NOT NULL UNIQUE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- ============================================================
-- TICKET-F003 / F004: "categories" table (with CHECK constraint)
-- ============================================================
-- WHAT: Categories classify transactions as either INCOME or EXPENSE.
--       Examples: "Salary" (INCOME), "Food" (EXPENSE), "Rent" (EXPENSE).
--       A CHECK constraint restricts the "type" column to only allow
--       specific values — the database itself rejects invalid data.
--
-- HOW:  CREATE TABLE with:
--         • category_id — SERIAL PRIMARY KEY
--         • name        — VARCHAR(50), NOT NULL
--         • type        — VARCHAR(10), NOT NULL, CHECK (type IN ('INCOME','EXPENSE'))
--
-- WHY:  Without the CHECK, someone could insert type = 'RANDOM' and break
--       the app's business logic. The database becomes the last line of defence.
--
-- OBSERVE: Try inserting a category with type = 'INVALID' — PostgreSQL should
--          reject it with: "new row violates check constraint".
CREATE TABLE categories (
    category_id SERIAL      PRIMARY KEY,
    name        VARCHAR(50) NOT NULL,
    type        VARCHAR(10) NOT NULL
                            CHECK (type IN ('INCOME', 'EXPENSE'))
);


-- ============================================================
-- TICKET-F003 / F004: "transactions" table (FKs + CHECK amount > 0)
-- ============================================================
-- WHAT: This is the core table — every income or expense is a transaction.
--       It has FOREIGN KEYs pointing to "users" and "categories".
--       A foreign key means: "this column's value MUST exist in another table."
--       If user_id = 5, then a user with user_id = 5 must exist in the users table.
--
-- HOW:  CREATE TABLE with:
--         • txn_id      — SERIAL PRIMARY KEY
--         • user_id     — INT, NOT NULL, REFERENCES users(user_id)
--         • category_id — INT, NOT NULL, REFERENCES categories(category_id)
--         • amount      — NUMERIC(12,2), NOT NULL, CHECK (amount > 0)
--         • txn_date    — DATE, NOT NULL, DEFAULT CURRENT_DATE
--         • description — VARCHAR(255) (optional / nullable)
--         • type        — VARCHAR(10), NOT NULL, CHECK (type IN ('INCOME','EXPENSE'))
--
-- WHY:  Foreign keys ensure referential integrity — you can't create a
--       transaction for a user that doesn't exist. CHECK (amount > 0)
--       prevents invalid data at the database level, even if application
--       code has a bug. NUMERIC(12,2) (not FLOAT) is non-negotiable for
--       money — FLOAT gives you 0.1 + 0.2 = 0.30000000000000004.
--
-- OBSERVE: After creating, try these tests:
--          1. INSERT with user_id = 999 (non-existent) → FK violation
--          2. INSERT with amount = -50 → CHECK violation
--          3. INSERT with type = 'RANDOM' → CHECK violation
--          4. INSERT with valid data → succeeds
CREATE TABLE transactions (
    txn_id      SERIAL         PRIMARY KEY,
    user_id     INT            NOT NULL REFERENCES users(user_id),
    category_id INT            NOT NULL REFERENCES categories(category_id),
    amount      NUMERIC(12,2)  NOT NULL CHECK (amount > 0),
    txn_date    DATE           NOT NULL DEFAULT CURRENT_DATE,
    description VARCHAR(255),
    type        VARCHAR(10)    NOT NULL
                               CHECK (type IN ('INCOME', 'EXPENSE'))
);


-- ============================================================
-- TICKET-F003 / F004: "savings_goals" table
-- ============================================================
-- WHAT: Users can set savings goals (e.g., "Holiday Fund: save £2000 by Dec").
--       Each goal tracks a target amount, current progress, and deadline.
--       This table has a FOREIGN KEY to users — each goal belongs to one user.
--
-- HOW:  CREATE TABLE with:
--         • goal_id        — SERIAL PRIMARY KEY
--         • user_id        — INT, NOT NULL, REFERENCES users(user_id)
--         • goal_name      — VARCHAR(100), NOT NULL
--         • target_amount  — NUMERIC(12,2), NOT NULL, CHECK (target_amount > 0)
--         • current_amount — NUMERIC(12,2), NOT NULL, DEFAULT 0
--         • deadline       — DATE (optional / nullable)
--
-- WHY:  The default of 0 on current_amount means new goals start with
--       zero progress. The deadline is optional because not all goals have one.
--
-- OBSERVE: After creating, INSERT a goal for user_id = 1 without supplying
--          current_amount — SELECT should show current_amount = 0.00 (the default).
CREATE TABLE savings_goals (
    goal_id        SERIAL        PRIMARY KEY,
    user_id        INT           NOT NULL REFERENCES users(user_id),
    goal_name      VARCHAR(100)  NOT NULL,
    target_amount  NUMERIC(12,2) NOT NULL CHECK (target_amount > 0),
    current_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    deadline       DATE
);


-- ============================================================
-- Verification (optional — run these interactively in psql)
-- ============================================================
--   \dt                       -- should list all 4 tables
--   \d users                  -- user_id should show nextval(...) default (SERIAL)
--   \d transactions           -- FKs on user_id + category_id should appear
