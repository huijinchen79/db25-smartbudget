-- ============================================================
-- TICKET-F006 (Day 1, Sprint 0) — Write 5 SQL Queries
-- TICKET-F007 (Day 1, Sprint 0) — JOIN Query          (Q1 below)
-- TICKET-F008 (Day 1, Sprint 0) — Create a VIEW       (Q5 below)
-- TICKET-F009 (Day 1, Sprint 0) — Window Functions    (Q4 below)
-- TICKET-F010 (Day 1, Sprint 0) — CTEs                (Q6 below)
-- ============================================================


-- ============================================================
-- Q1 — TICKET-F006 / TICKET-F007
-- All transactions with user name and category name (3-table JOIN)
-- ============================================================
-- WHAT: A JOIN combines rows from two or more tables based on a related column.
--       Right now the transactions table only stores user_id (a number like 1)
--       and category_id (a number like 3). Two JOINs replace those numbers
--       with the actual user name and category name.
--
-- HOW:  SELECT columns from all three tables. Use short aliases (t, u, c) so
--       the query reads like English. Because two of the columns share the name
--       "name" (u.name and c.name), we alias them (user_name, category).
--
-- WHY:  Without JOINs, you'd run 3 queries and stitch results together in code.
--       JOINs let the database do the matching once, in one round-trip — the
--       foundation of every API endpoint from Day 5 onwards.
--
-- OBSERVE: Result shows "Alice Smith" / "Food" instead of 1 / 3.
--          Row count = SELECT COUNT(*) FROM transactions; (inner join drops nothing).
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


-- ============================================================
-- Q2 — TICKET-F006
-- EXPENSE transactions only, sorted by amount (highest first)
-- ============================================================
-- WHAT: Filtering with WHERE and sorting with ORDER BY.
--       WHERE narrows the rows returned; ORDER BY controls the display order.
--       DESC = descending (biggest first).
--
-- HOW:  Same JOIN as Q1, plus:
--         WHERE t.type = 'EXPENSE'
--         ORDER BY t.amount DESC
--
-- WHY:  Users want to see their biggest expenses first — that's where the
--       money is leaking out fastest.
--
-- OBSERVE: Every row has type = 'EXPENSE'. First row has the highest amount.
SELECT t.txn_id,
       u.name  AS user_name,
       c.name  AS category,
       t.amount,
       t.txn_date
FROM transactions t
JOIN users      u ON t.user_id     = u.user_id
JOIN categories c ON t.category_id = c.category_id
WHERE t.type = 'EXPENSE'
ORDER BY t.amount DESC;


-- ============================================================
-- Q3 — TICKET-F006
-- Monthly totals per user (GROUP BY + DATE_TRUNC)
-- ============================================================
-- WHAT: Aggregation collapses many rows into summary rows.
--       DATE_TRUNC('month', txn_date) rounds every date to the 1st of its month,
--       so all February transactions become '2026-02-01'.
--       GROUP BY collects all rows with the same (user, month) into one row.
--       SUM(amount) adds up the amounts in each group.
--
-- HOW:  SELECT user name, truncated month, and SUM(amount).
--       JOIN transactions with users. GROUP BY user + month.
--       ORDER BY user + month for readability. We display the month as
--       'Mon YYYY' (e.g., "Jan 2026") but GROUP/ORDER on the truncated date
--       itself so months sort chronologically, not alphabetically.
--
-- WHY:  This is the data the "Monthly Summary" chart displays on the Day 9 dashboard.
--       Without aggregation you'd have 15 individual rows here and hundreds in production.
--
-- OBSERVE: One row per (user, month). E.g. "Alice Smith | Jan 2026 | 3570.20".
SELECT u.name                                                  AS user_name,
       TO_CHAR(DATE_TRUNC('month', t.txn_date), 'Mon YYYY')    AS month,
       SUM(t.amount)                                           AS total
FROM transactions t
JOIN users u ON t.user_id = u.user_id
GROUP BY u.name, DATE_TRUNC('month', t.txn_date)
ORDER BY u.name, DATE_TRUNC('month', t.txn_date);


-- ============================================================
-- Q4 — TICKET-F006 / TICKET-F009
-- Running balance per user (Window Function)
-- ============================================================
-- WHAT: A window function performs a calculation across a set of rows WITHOUT
--       collapsing them (unlike GROUP BY). SUM(...) OVER (PARTITION BY ... ORDER BY ...)
--       gives you a running total: each row shows the cumulative sum up to that row.
--
-- HOW:  Signed amounts: INCOME adds, EXPENSE subtracts — expressed with a CASE.
--       PARTITION BY u.user_id restarts the running total for each user.
--       ORDER BY inside OVER controls the accumulation order (chronological).
--       The tie-breaker t.txn_id ensures a deterministic order for same-day txns.
--
-- WHY:  Window functions are the canonical way to compute running balances,
--       moving averages, rankings and gaps — a favourite technical-interview topic.
--       GROUP BY collapses rows; window functions preserve them. Knowing when
--       to reach for each is the lesson.
--
-- OBSERVE: Per user, running_balance grows on INCOME rows and shrinks on
--          EXPENSE rows. When the next user starts, the balance resets.
SELECT u.name                                     AS user_name,
       t.txn_date,
       t.type,
       t.amount,
       SUM( CASE WHEN t.type = 'INCOME' THEN  t.amount
                                        ELSE -t.amount END )
           OVER ( PARTITION BY u.user_id
                  ORDER BY     t.txn_date, t.txn_id )        AS running_balance
FROM transactions t
JOIN users u ON t.user_id = u.user_id
ORDER BY u.name, t.txn_date, t.txn_id;


-- ============================================================
-- Q5 — TICKET-F006 / TICKET-F008
-- VIEW: top 3 expense categories by total spend
-- ============================================================
-- WHAT: A VIEW is a saved SELECT you can query like a table. No data is stored —
--       the query re-runs every time you SELECT from the view, so results always
--       reflect the latest data.
--
-- HOW:  CREATE OR REPLACE VIEW so re-running the script never errors.
--       Group EXPENSE transactions by category name, SUM the amounts, take the top 3.
--       COUNT(*) is a bonus column showing how many transactions fed each total.
--
-- WHY:  Views give a stable name for a complex query. Day 9 dashboards (and any
--       BI tool later) can `SELECT * FROM top_expense_categories` without
--       re-deriving the aggregation each time.
--
-- OBSERVE: Exactly 3 rows, ordered by total_spent DESC.
--          Insert another EXPENSE in one of those categories and re-select —
--          the total updates immediately.
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

-- Query it like a regular table:
SELECT * FROM top_expense_categories;


-- ============================================================
-- Q6 — TICKET-F010
-- CTE: net balance per user (income - expenses), keeping users with zero of either
-- ============================================================
-- WHAT: A CTE (Common Table Expression) — WITH name AS (SELECT ...) — is a
--       named, throwaway result set you can reference like a table for the
--       rest of the statement. Great for breaking a multi-step query into
--       readable pieces.
--
-- HOW:  Two CTEs — `income` and `expenses` — each returns (user_id, total).
--       LEFT JOIN both onto users so users with no INCOME (or no EXPENSE)
--       still appear. COALESCE(x, 0) turns the resulting NULLs into 0.
--
-- WHY:  You COULD do this with two subqueries in the SELECT, but CTEs make
--       the intent obvious: "first income per user, then expenses per user,
--       then combine". Readability + decomposition is the whole point.
--       LEFT JOIN + COALESCE is the pattern that keeps "empty" users visible
--       instead of silently dropping them.
--
-- OBSERVE: One row per user (5 rows on the seed data).
--          A user with only expenses shows total_income = 0, not NULL.
--          net_balance = total_income - total_expenses matches mental arithmetic.
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
       u.name                                            AS user_name,
       COALESCE(i.total, 0)                              AS total_income,
       COALESCE(e.total, 0)                              AS total_expenses,
       COALESCE(i.total, 0) - COALESCE(e.total, 0)       AS net_balance
FROM users u
LEFT JOIN income   i ON u.user_id = i.user_id
LEFT JOIN expenses e ON u.user_id = e.user_id
ORDER BY net_balance DESC;
