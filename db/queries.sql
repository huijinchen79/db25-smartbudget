-- ============================================================
-- TICKET-F006 (Day 1, Sprint 0) — Write 5 SQL Queries
-- TICKET-F007 (Day 1, Sprint 0) — JOIN Query
-- TICKET-F008 (Day 1, Sprint 0) — Create a VIEW
-- ============================================================


-- ============================================================
-- TODO TICKET-F006: Q1 — All transactions with user name and category name
-- (This is also TICKET-F007: JOIN query)
-- ============================================================
-- WHAT: A JOIN combines rows from two or more tables based on a related column.
--       Right now, the transactions table only stores user_id (a number like 1)
--       and category_id (a number like 3). The JOIN replaces those numbers
--       with the actual user name and category name.
--
-- HOW:  Write a SELECT that JOINs three tables:
--         transactions → users (ON transactions.user_id = users.user_id)
--         transactions → categories (ON transactions.category_id = categories.category_id)
--       Select these columns: txn_id, users.name, categories.name, amount, txn_date, description, type
--       Use table aliases to keep it readable (e.g., t for transactions, u for users, c for categories)
--
-- WHY:  Without JOINs, you'd need 3 separate queries and match IDs in your code.
--       JOINs let the database do this work in one query — much faster.
--
-- OBSERVE: The result should show human-readable names like "Alice Smith" and "Food"
--          instead of numeric IDs like 1 and 3.

SELECT txn_id
     , u.name
     , c.name
     , amount
     , txn_date
     , description
     , type
FROM transactions t
         LEFT JOIN users u ON t.user_id = u.user_id
         LEFT JOIN categories c ON c.category_id = t.category_id;

-- ============================================================
-- TODO TICKET-F006: Q2 — EXPENSE transactions only, sorted by amount (highest first)
-- ============================================================
-- WHAT: Filtering with WHERE and sorting with ORDER BY.
--       WHERE narrows the results; ORDER BY controls the display order.
--
-- HOW:  Use the same JOIN query from Q1 but add:
--         WHERE type = 'EXPENSE'
--         ORDER BY amount DESC
--       DESC means descending — highest amounts first.
--
-- WHY:  Users want to see their biggest expenses first to identify
--       where they're spending the most money.
--
-- OBSERVE: All results should have type = 'EXPENSE'.
--          The first row should have the highest amount.

SELECT * FROM transactions
WHERE type = 'EXPENSE'
ORDER BY amount DESC;

-- ============================================================
-- TODO TICKET-F006: Q3 — Monthly totals per user
-- ============================================================
-- WHAT: Aggregation groups rows and calculates totals.
--       DATE_TRUNC('month', txn_date) rounds every date to the 1st of its month,
--       so all May transactions become '2026-05-01'.
--       GROUP BY collects all rows with the same month + user into one summary row.
--       SUM(amount) adds up all amounts in each group.
--
-- HOW:  SELECT the user name, the truncated month, and SUM(amount).
--       JOIN transactions with users.
--       GROUP BY the user name AND the truncated month.
--       ORDER BY month and user name for readability.
--
-- WHY:  This is the data the "Monthly Summary" chart will display on the dashboard.
--       Without aggregation, you'd have hundreds of individual rows.
--
-- OBSERVE: Each row should show one user + one month + their total.
--          For example: "Alice Smith | 2026-05-01 | 3652.50"

SELECT users.name
     , DATE_TRUNC('month', txn_date)
     , transactions.amount
FROM transactions
         LEFT JOIN users ON transactions.user_id = users.user_id
GROUP BY 2;



-- ============================================================
-- TODO TICKET-F006: Q4 — Top 5 highest transactions
-- ============================================================
-- WHAT: LIMIT restricts the number of rows returned.
--       Combined with ORDER BY DESC, it gives you the "Top N" pattern.
--
-- HOW:  Write a SELECT with JOIN (to get user/category names),
--       ORDER BY amount DESC, then LIMIT 5.
--
-- WHY:  Helps users identify their largest transactions at a glance.
--
-- OBSERVE: You should see exactly 5 rows, with the highest amount first.

SELECT * FROM transactions
ORDER BY amount
    LIMIT 5;
-- ============================================================
-- TODO TICKET-F006: Q5 — Running balance per user (Window Function)
-- ============================================================
-- WHAT: A Window Function performs a calculation across a set of rows
--       WITHOUT collapsing them into one row (unlike GROUP BY).
--       SUM(amount) OVER (PARTITION BY user_id ORDER BY txn_date) calculates
--       a "running total" — each row shows the cumulative sum up to that point.
--
-- HOW:  SELECT user_id, txn_date, amount, type, and then add a new column:
--         SUM(amount) OVER (PARTITION BY user_id ORDER BY txn_date) AS running_total
--       PARTITION BY user_id means: restart the running total for each user.
--       ORDER BY txn_date means: accumulate in date order.
--
-- WHY:  A running balance shows how a user's total spending grows over time.
--       This is a key financial metric. Window functions are an advanced SQL
--       concept tested in many technical interviews.
--
-- OBSERVE: For each user, the running_total should increase with each row.
--          The last row for each user should equal their total sum.


-- ============================================================
-- TODO TICKET-F008: Create VIEW — monthly_summary
-- ============================================================
-- WHAT: A VIEW is a saved query. It behaves like a virtual table.
--       Once created, you can SELECT * FROM monthly_summary instead of
--       writing the full query every time.
--
-- HOW:  Use CREATE OR REPLACE VIEW monthly_summary AS
--       followed by a query similar to Q3 (monthly totals),
--       but also split income and expense into separate columns using
--       CASE WHEN or SUM with FILTER.
--       Columns: user_id, user_name, month, total_income, total_expense, net_balance
--
-- WHY:  The Spring Boot backend can query this VIEW directly.
--       It simplifies the Java code — instead of complex JPQL,
--       the backend just reads from the view.
--
-- OBSERVE: After creating, run: SELECT * FROM monthly_summary;
--          You should see one row per user per month with income/expense split.
-- ============================================================
-- Q1: All transactions with user + category names (3-table JOIN)
-- ============================================================
SELECT t.txn_id,
       u.name  AS user_name,
       c.name  AS category,
       t.amount,
       t.txn_date,
       t.description,
       t.type
FROM transactions t
         JOIN users      u ON t.user_id     = u.user_id
         JOIN categories c ON t.category_id = c.category_id
ORDER BY t.txn_date DESC, t.txn_id;

-- ============================================================
-- Q2: EXPENSE only, highest amount first
-- ============================================================
SELECT t.txn_id, u.name, c.name AS category, t.amount, t.txn_date
FROM transactions t
         JOIN users      u ON t.user_id     = u.user_id
         JOIN categories c ON t.category_id = c.category_id
WHERE t.type = 'EXPENSE'
ORDER BY t.amount DESC;

-- ============================================================
-- Q3: Monthly totals per user
-- ============================================================
SELECT u.name,
       TO_CHAR(DATE_TRUNC('month', t.txn_date), 'Mon YYYY') AS month,
       SUM(t.amount) AS total
FROM transactions t
    JOIN users u ON t.user_id = u.user_id
GROUP BY u.name, DATE_TRUNC('month', t.txn_date)
ORDER BY u.name, DATE_TRUNC('month', t.txn_date);

-- ============================================================
-- Q4: Running balance per user (window function)
--   Income adds, expense subtracts.
-- ============================================================
SELECT u.name,
       t.txn_date,
       t.type,
       t.amount,
       SUM(CASE WHEN t.type = 'INCOME' THEN  t.amount
                ELSE -t.amount END)
           OVER (PARTITION BY u.user_id ORDER BY t.txn_date, t.txn_id)
           AS running_balance
FROM transactions t
         JOIN users u ON t.user_id = u.user_id
ORDER BY u.name, t.txn_date, t.txn_id;

-- ============================================================
-- Q5: VIEW — top 3 expense categories by total spend
-- ============================================================
CREATE OR REPLACE VIEW top_expense_categories AS
SELECT c.name AS category, SUM(t.amount) AS total_spent
FROM transactions t
         JOIN categories c ON t.category_id = c.category_id
WHERE c.type = 'EXPENSE'
GROUP BY c.name
ORDER BY total_spent DESC
    LIMIT 3;

SELECT * FROM top_expense_categories;


--Ticket F007:

SELECT t.txn_id,
       u.name        AS user_name,
       c.name        AS category,
       t.amount,
       t.txn_date,
       t.description,
       t.type
FROM transactions t
         JOIN users      u ON t.user_id     = u.user_id
         JOIN categories c ON t.category_id = c.category_id
ORDER BY t.txn_date DESC, t.txn_id;


-- Ticket F008:

CREATE OR REPLACE VIEW top_expense_categories AS
SELECT c.name           AS category,
       SUM(t.amount)    AS total_spent,
       COUNT(*)         AS txn_count
FROM transactions t
         JOIN categories  c ON t.category_id = c.category_id
WHERE c.type = 'EXPENSE'
GROUP BY c.name
ORDER BY total_spent DESC
    LIMIT 3;

-- Use it like a regular table
SELECT * FROM top_expense_categories;

--TICKET FOO9

SELECT u.name,
       t.txn_date,
       t.type,
       t.amount,
       SUM( CASE WHEN t.type = 'INCOME' THEN  t.amount
                 ELSE -t.amount END )
           OVER ( PARTITION BY u.user_id
                  ORDER BY     t.txn_date, t.txn_id )
           AS running_balance
FROM transactions t
         JOIN users u ON t.user_id = u.user_id
ORDER BY u.name, t.txn_date, t.txn_id;

-- TICKET F010

WITH income AS (
    SELECT user_id, SUM(amount) AS total
    FROM transactions
    WHERE type = 'INCOME'
    GROUP BY user_id
),
     expenses AS (
         SELECT user_id, SUM(amount) AS total
         FROM transactions
         WHERE type = 'EXPENSE'
         GROUP BY user_id
     )
SELECT u.user_id,
       u.name,
       COALESCE(i.total, 0)                          AS total_income,
       COALESCE(e.total, 0)                          AS total_expenses,
       COALESCE(i.total, 0) - COALESCE(e.total, 0)   AS net_balance
FROM users u
         LEFT JOIN income   i ON u.user_id = i.user_id
         LEFT JOIN expenses e ON u.user_id = e.user_id
ORDER BY net_balance DESC;