-- PROVIDED – seed data loaded automatically on startup
-- You will see this data in H2 console and via the API on Day 1.
--
-- TICKET-F053 (Day 5, Sprint 4): 10 extra transactions appended below the
-- original 15 to give the dashboard richer data for aggregations, date-range
-- filters, and month-over-month reporting on Days 6-8.
-- Total transactions after this file runs: 25.

INSERT INTO categories (name, type) VALUES ('Salary',    'INCOME');
INSERT INTO categories (name, type) VALUES ('Freelance', 'INCOME');
INSERT INTO categories (name, type) VALUES ('Food',      'EXPENSE');
INSERT INTO categories (name, type) VALUES ('Transport', 'EXPENSE');
INSERT INTO categories (name, type) VALUES ('Utilities', 'EXPENSE');

INSERT INTO users (name, email) VALUES ('Alice Smith',  'alice@bank.com');
INSERT INTO users (name, email) VALUES ('Bob Jones',    'bob@bank.com');
INSERT INTO users (name, email) VALUES ('Carol White',  'carol@bank.com');
INSERT INTO users (name, email) VALUES ('Dave Brown',   'dave@bank.com');
INSERT INTO users (name, email) VALUES ('Eve Davis',    'eve@bank.com');

INSERT INTO savings_goals (user_id, name, target_amount, current_amount, deadline) VALUES (1, 'Holiday Fund',   2000.00,  500.00, '2026-12-01');
INSERT INTO savings_goals (user_id, name, target_amount, current_amount, deadline) VALUES (1, 'Emergency Fund', 5000.00, 1200.00, '2027-01-01');
INSERT INTO savings_goals (user_id, name, target_amount, current_amount, deadline) VALUES (2, 'New Laptop',     1500.00,  750.00, '2026-09-01');
INSERT INTO savings_goals (user_id, name, target_amount, current_amount, deadline) VALUES (3, 'Wedding Fund',  10000.00, 3000.00, '2027-06-01');

-- Original 15 transactions ------------------------------------------------
INSERT INTO transactions (user_id, category_id, amount, txn_date, description, type) VALUES (1, 1, 3500.00, '2026-05-01', 'May salary',        'INCOME');
INSERT INTO transactions (user_id, category_id, amount, txn_date, description, type) VALUES (1, 3,   45.50, '2026-05-03', 'Team lunch',         'EXPENSE');
INSERT INTO transactions (user_id, category_id, amount, txn_date, description, type) VALUES (1, 4,   12.00, '2026-05-05', 'Bus pass',           'EXPENSE');
INSERT INTO transactions (user_id, category_id, amount, txn_date, description, type) VALUES (1, 5,   95.00, '2026-05-06', 'Electricity bill',   'EXPENSE');
INSERT INTO transactions (user_id, category_id, amount, txn_date, description, type) VALUES (2, 1, 4200.00, '2026-05-01', 'May salary',         'INCOME');
INSERT INTO transactions (user_id, category_id, amount, txn_date, description, type) VALUES (2, 2,  800.00, '2026-05-10', 'Freelance project',  'INCOME');
INSERT INTO transactions (user_id, category_id, amount, txn_date, description, type) VALUES (2, 5,  120.00, '2026-05-07', 'Gas bill',           'EXPENSE');
INSERT INTO transactions (user_id, category_id, amount, txn_date, description, type) VALUES (3, 1, 3100.00, '2026-04-01', 'April salary',       'INCOME');
INSERT INTO transactions (user_id, category_id, amount, txn_date, description, type) VALUES (3, 3,   55.00, '2026-04-15', 'Dinner out',         'EXPENSE');
INSERT INTO transactions (user_id, category_id, amount, txn_date, description, type) VALUES (4, 1, 3800.00, '2026-04-01', 'April salary',       'INCOME');
INSERT INTO transactions (user_id, category_id, amount, txn_date, description, type) VALUES (4, 2,  500.00, '2026-04-12', 'Freelance writing',  'INCOME');
INSERT INTO transactions (user_id, category_id, amount, txn_date, description, type) VALUES (5, 1, 2900.00, '2026-03-01', 'March salary',       'INCOME');
INSERT INTO transactions (user_id, category_id, amount, txn_date, description, type) VALUES (5, 3,   40.00, '2026-03-10', 'Lunch',              'EXPENSE');
INSERT INTO transactions (user_id, category_id, amount, txn_date, description, type) VALUES (1, 1, 3500.00, '2026-04-01', 'April salary',       'INCOME');
INSERT INTO transactions (user_id, category_id, amount, txn_date, description, type) VALUES (1, 3,   62.00, '2026-04-08', 'Grocery run',        'EXPENSE');

-- TICKET-F053: 10 additional transactions ---------------------------------
-- Spanning April 2026 across all 5 users, mixing INCOME and EXPENSE.
-- No txn_id supplied — IDENTITY generates it.
INSERT INTO transactions (user_id, category_id, amount, txn_date, description, type) VALUES (1, 3,   12.50, '2026-04-02', 'Lunch',              'EXPENSE');
INSERT INTO transactions (user_id, category_id, amount, txn_date, description, type) VALUES (1, 4,   18.00, '2026-04-05', 'Tube fare',          'EXPENSE');
INSERT INTO transactions (user_id, category_id, amount, txn_date, description, type) VALUES (2, 1, 4200.00, '2026-04-01', 'April salary',       'INCOME');
INSERT INTO transactions (user_id, category_id, amount, txn_date, description, type) VALUES (3, 2,  650.00, '2026-04-09', 'Freelance gig',      'INCOME');
INSERT INTO transactions (user_id, category_id, amount, txn_date, description, type) VALUES (3, 3,   80.00, '2026-04-12', 'Dinner out',         'EXPENSE');
INSERT INTO transactions (user_id, category_id, amount, txn_date, description, type) VALUES (4, 5,  130.00, '2026-04-15', 'Electric bill',      'EXPENSE');
INSERT INTO transactions (user_id, category_id, amount, txn_date, description, type) VALUES (5, 1, 3100.00, '2026-04-01', 'April salary',       'INCOME');
INSERT INTO transactions (user_id, category_id, amount, txn_date, description, type) VALUES (5, 5,   89.00, '2026-04-18', 'Internet',           'EXPENSE');
INSERT INTO transactions (user_id, category_id, amount, txn_date, description, type) VALUES (2, 3,   34.75, '2026-04-20', 'Coffee run',         'EXPENSE');
INSERT INTO transactions (user_id, category_id, amount, txn_date, description, type) VALUES (4, 3,   45.00, '2026-04-22', 'Groceries',          'EXPENSE');