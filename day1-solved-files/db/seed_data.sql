-- ============================================================
-- TICKET-F005 (Day 1, Sprint 0) — Insert Seed Data
-- TICKET-F004 (Day 1, Sprint 0) — Test Constraints (commented block at end)
-- ============================================================
-- Seed data populates your tables with realistic test data.
-- Insert order matters! Parents first, then children:
--   categories → users → transactions → savings_goals
-- ============================================================


-- ============================================================
-- TICKET-F005: Insert 5 categories (2 INCOME, 3 EXPENSE)
-- ============================================================
-- WHAT: Seed data is pre-populated test data that makes development easier.
--       Without it, every time you reset the database you'd start with empty tables.
--
-- HOW:  A single multi-row INSERT is compact and easy to read.
--       We do NOT specify category_id — SERIAL auto-assigns it in insert order,
--       so the IDs below are predictable: Salary=1, Freelance=2, Food=3,
--       Transport=4, Utilities=5. The transactions block below relies on that.
--
-- WHY:  The React frontend needs categories for its dropdown, and the
--       API endpoint GET /api/categories returns this list.
--
-- OBSERVE: SELECT * FROM categories; should show 5 rows with IDs 1-5.
INSERT INTO categories (name, type) VALUES
    ('Salary',    'INCOME'),
    ('Freelance', 'INCOME'),
    ('Food',      'EXPENSE'),
    ('Transport', 'EXPENSE'),
    ('Utilities', 'EXPENSE');


-- ============================================================
-- TICKET-F005: Insert 5 users
-- ============================================================
-- WHAT: Each user has a unique name and email. The email UNIQUE constraint
--       means no two users can share the same email address.
--
-- HOW:  Multi-row INSERT with realistic names. We omit user_id and created_at —
--       both auto-populate (SERIAL PK + DEFAULT CURRENT_TIMESTAMP).
--       Predictable IDs after this insert: Alice=1, Bob=2, Carol=3, Dave=4, Eve=5.
--
-- WHY:  5 users provides enough variety to test multi-user features and the
--       running-balance window function without making the seed unreadable.
--
-- OBSERVE: SELECT * FROM users; should show 5 rows.
--          Try inserting a duplicate email — it should FAIL with a unique violation.
INSERT INTO users (name, email) VALUES
    ('Alice Smith', 'alice@bank.com'),
    ('Bob Jones',   'bob@bank.com'),
    ('Carol Reed',  'carol@bank.com'),
    ('Dave Patel',  'dave@bank.com'),
    ('Eve Lin',     'eve@bank.com');


-- ============================================================
-- TICKET-F005: Insert 15 transactions across 3 months, mixed types
-- ============================================================
-- WHAT: Transactions are the core data. Each references a user_id and category_id.
--       Those IDs must match existing rows (enforced by the FOREIGN KEY constraints
--       you added in F004).
--
-- HOW:  Multi-row INSERT spanning January, February, and March 2026.
--       Mix of INCOME (Salary/Freelance) and EXPENSE (Food/Transport/Utilities).
--       Amounts are realistic: salaries in the thousands, groceries in the tens.
--
-- WHY:  15 rows across 3 months gives:
--         • enough variety for the JOIN + WHERE + ORDER BY queries (F006, F007)
--         • at least 3 buckets for the DATE_TRUNC('month', ...) group-by (Q3)
--         • enough per-user rows for the running-balance window function (F009)
--         • enough categories with 2+ transactions to make the top-3 VIEW meaningful (F008)
--
-- OBSERVE: SELECT COUNT(*) FROM transactions;                      → 15
--          SELECT DISTINCT TO_CHAR(txn_date,'YYYY-MM') FROM transactions;
--                                                                  → 3 distinct months
INSERT INTO transactions
    (user_id, category_id, amount, txn_date, description, type) VALUES
    (1, 1, 3500.00, '2026-01-01', 'January salary',   'INCOME'),
    (1, 3,   45.20, '2026-01-08', 'Groceries',        'EXPENSE'),
    (1, 4,   25.00, '2026-01-15', 'Bus pass',         'EXPENSE'),
    (2, 1, 4200.00, '2026-01-01', 'January salary',   'INCOME'),
    (2, 5,  120.00, '2026-01-20', 'Electricity bill', 'EXPENSE'),
    (3, 2,  800.00, '2026-02-05', 'Freelance gig',    'INCOME'),
    (3, 3,   60.00, '2026-02-10', 'Restaurant',       'EXPENSE'),
    (1, 1, 3500.00, '2026-02-01', 'February salary',  'INCOME'),
    (1, 3,   38.40, '2026-02-12', 'Groceries',        'EXPENSE'),
    (4, 1, 2800.00, '2026-02-01', 'February salary',  'INCOME'),
    (4, 4,   35.00, '2026-02-18', 'Taxi to airport',  'EXPENSE'),
    (5, 1, 3100.00, '2026-03-01', 'March salary',     'INCOME'),
    (5, 3,   52.00, '2026-03-05', 'Groceries',        'EXPENSE'),
    (2, 2,  500.00, '2026-03-10', 'Side project',     'INCOME'),
    (2, 5,   95.00, '2026-03-20', 'Internet bill',    'EXPENSE');


-- ============================================================
-- TICKET-F005: Insert 4 savings goals
-- ============================================================
-- WHAT: Savings goals track progress toward a financial target.
--       current_amount can start at 0 or a partial amount; deadline is optional.
--
-- HOW:  Multi-row INSERT. Goals reference existing users (FK on user_id).
--       Note "New Laptop" is fully funded (current_amount == target_amount) —
--       a nice edge case to test the "completed goal" UI on Day 10.
--
-- WHY:  The Savings Goals page needs this data to render progress bars.
--       Varying progress levels (22.5%, 100%, 16%, 25%) test the visual states.
--
-- OBSERVE: SELECT COUNT(*) FROM savings_goals;   → 4
--          SELECT goal_name, current_amount / target_amount * 100 AS pct FROM savings_goals;
INSERT INTO savings_goals
    (user_id, goal_name, target_amount, current_amount, deadline) VALUES
    (1, 'Holiday Fund',      2000.00,   450.00, '2026-12-01'),
    (2, 'New Laptop',        1500.00,  1500.00, '2026-06-30'),  -- fully funded
    (3, 'Emergency Buffer',  5000.00,   800.00, '2026-12-31'),
    (4, 'Wedding',          10000.00,  2500.00, '2027-09-15');


-- ============================================================
-- Verification
-- ============================================================
SELECT COUNT(*) AS categories_count    FROM categories;     -- expect 5
SELECT COUNT(*) AS users_count         FROM users;          -- expect 5
SELECT COUNT(*) AS transactions_count  FROM transactions;   -- expect 15
SELECT COUNT(*) AS savings_goals_count FROM savings_goals;  -- expect 4


-- ============================================================
-- TICKET-F004: Test constraint violations (reference only)
-- ============================================================
-- WHAT: Constraints are rules the database enforces. If violated, the INSERT fails.
--       This is a safety net — even if application code has bugs, the database
--       prevents invalid data from being stored.
--
-- HOW:  Uncomment each block below one at a time and re-run this file, OR
--       paste them into a psql session. Each one should ERROR out.
--
-- WHY:  Proving constraints work builds confidence in your schema design.
--
-- OBSERVE: Read the error message — it tells you which constraint was violated.
-- ------------------------------------------------------------
-- -- 1. amount < 0 → CHECK (amount > 0)
-- INSERT INTO transactions (user_id, category_id, amount, txn_date, type)
-- VALUES (1, 3, -10, CURRENT_DATE, 'EXPENSE');
--
-- -- 2. non-existent user_id → FOREIGN KEY
-- INSERT INTO transactions (user_id, category_id, amount, txn_date, type)
-- VALUES (999, 3, 10, CURRENT_DATE, 'EXPENSE');
--
-- -- 3. duplicate email → UNIQUE
-- INSERT INTO users (name, email) VALUES ('Duplicate', 'alice@bank.com');
--
-- -- 4. type = 'INVALID' → CHECK (type IN ('INCOME','EXPENSE'))
-- INSERT INTO categories (name, type) VALUES ('Bogus', 'INVALID');
--
-- -- 5. NULL name → NOT NULL
-- INSERT INTO users (email) VALUES ('nobody@bank.com');
