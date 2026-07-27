-- ============================================================
-- TICKET-F003 (Day 1, Sprint 0) — Create Table Scripts
-- TICKET-F004 (Day 1, Sprint 0) — Add Constraints
-- ============================================================
-- Run order matters! Parent tables MUST be created before child tables
-- because foreign keys reference the parent.
-- Order: users → categories → transactions → savings_goals
-- ============================================================


-- ============================================================
-- TODO TICKET-F003: Create the "users" table
-- ============================================================
-- WHAT: This is your first table. Every SmartBudget user is stored here.
--       The "user_id" column is the PRIMARY KEY — a unique identifier
--       that other tables will reference via FOREIGN KEYs.
--
-- HOW:  Use CREATE TABLE with these columns:
--         • user_id    — integer, auto-incrementing primary key
--                         (use SERIAL in PostgreSQL for auto-increment)
--         • name       — text up to 100 characters, cannot be empty
--         • email      — text up to 150 characters, must be unique across all users
--         • created_at — timestamp that defaults to the current time
--
-- WHY:  Without a users table, we can't track who made which transaction.
--       The UNIQUE constraint on email prevents duplicate accounts.
--       NOT NULL ensures every user has a name.
--
-- OBSERVE: After creating this table, run \dt in psql (or check Tables in pgAdmin).
--          You should see "users" listed. Try INSERT INTO users (name, email) VALUES ('Test', 'test@db.com');
--          Then SELECT * FROM users; — you should see user_id = 1 auto-assigned.


-- ============================================================
-- TODO TICKET-F003: Create the "categories" table
-- TODO TICKET-F004: Add CHECK constraint on the "type" column
-- ============================================================
-- WHAT: Categories classify transactions as either INCOME or EXPENSE.
--       Examples: "Salary" (INCOME), "Food" (EXPENSE), "Rent" (EXPENSE).
--       A CHECK constraint restricts the "type" column to only allow
--       specific values — the database itself rejects invalid data.
--
-- HOW:  Use CREATE TABLE with these columns:
--         • category_id — integer, auto-incrementing primary key (SERIAL)
--         • name        — text up to 50 characters, cannot be NULL
--         • type        — text up to 10 characters, cannot be NULL
--       Then add a CHECK constraint on "type" that only allows 'INCOME' or 'EXPENSE'.
--       Use: CHECK (type IN ('INCOME', 'EXPENSE'))
--
-- WHY:  Without the CHECK, someone could insert type = 'RANDOM' and break
--       the app's business logic. The database becomes the last line of defense.
--
-- OBSERVE: After creating, try inserting a category with type = 'INVALID'.
--          PostgreSQL should reject it with: "new row violates check constraint".


-- ============================================================
-- TODO TICKET-F003: Create the "transactions" table
-- TODO TICKET-F004: Add FOREIGN KEYs and CHECK (amount > 0)
-- ============================================================
-- WHAT: This is the core table — every income or expense is a transaction.
--       It has FOREIGN KEYs pointing to "users" and "categories".
--       A foreign key means: "this column's value MUST exist in another table."
--       If user_id = 5, then a user with user_id = 5 must exist in the users table.
--
-- HOW:  Use CREATE TABLE with these columns:
--         • txn_id      — integer, auto-incrementing primary key (SERIAL)
--         • user_id     — integer, NOT NULL, REFERENCES users(user_id)
--         • category_id — integer, NOT NULL, REFERENCES categories(category_id)
--         • amount      — numeric(12,2), NOT NULL
--         • txn_date    — date, NOT NULL, defaults to CURRENT_DATE
--         • description — text up to 255 characters (optional, can be NULL)
--         • type        — text up to 10 characters, NOT NULL
--       Add these constraints:
--         • FOREIGN KEY (user_id) REFERENCES users(user_id)
--         • FOREIGN KEY (category_id) REFERENCES categories(category_id)
--         • CHECK (amount > 0)   ← prevents negative or zero transactions
--
-- WHY:  Foreign keys ensure data integrity — you can't create a transaction
--       for a user that doesn't exist. CHECK (amount > 0) prevents invalid data
--       at the database level, even if the application code has a bug.
--
-- OBSERVE: After creating, try these tests:
--          1. INSERT with user_id = 999 (non-existent) → should FAIL with FK violation
--          2. INSERT with amount = -50 → should FAIL with CHECK violation
--          3. INSERT with valid data → should SUCCEED


-- ============================================================
-- TODO TICKET-F003: Create the "savings_goals" table
-- ============================================================
-- WHAT: Users can set savings goals (e.g., "Holiday Fund: save £2000 by Dec").
--       Each goal tracks a target amount, current progress, and deadline.
--       This table has a FOREIGN KEY to users — each goal belongs to one user.
--
-- HOW:  Use CREATE TABLE with these columns:
--         • goal_id        — integer, auto-incrementing primary key (SERIAL)
--         • user_id        — integer, NOT NULL, REFERENCES users(user_id)
--         • name           — text up to 100 characters, NOT NULL
--         • target_amount  — numeric(12,2), NOT NULL
--         • current_amount — numeric(12,2), NOT NULL, default 0.00
--         • deadline       — date (optional, can be NULL)
--
-- WHY:  The default of 0.00 on current_amount means new goals start with
--       zero progress. The deadline is optional because not all goals have one.
--
-- OBSERVE: After creating, INSERT a goal for user_id = 1. Then run:
--          SELECT * FROM savings_goals WHERE user_id = 1;
--          You should see current_amount = 0.00 (the default).

CREATE TABLE users (
                       user_id    SERIAL       PRIMARY KEY,
                       name       VARCHAR(100) NOT NULL,
                       email      VARCHAR(150) NOT NULL UNIQUE,
                       created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories (
                            category_id SERIAL       PRIMARY KEY,
                            name        VARCHAR(50)  NOT NULL,
                            type        VARCHAR(10)  NOT NULL
                                CHECK (type IN ('INCOME', 'EXPENSE'))
);

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

CREATE TABLE savings_goals (
                               goal_id        SERIAL        PRIMARY KEY,
                               user_id        INT           NOT NULL REFERENCES users(user_id),
                               goal_name      VARCHAR(100)  NOT NULL,
                               target_amount  NUMERIC(12,2) NOT NULL CHECK (target_amount > 0),
                               current_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
                               deadline       DATE
);