# Day 9 — Solved Files: Where to Paste

## How this folder works

Day 8 (Sprint 7) got the React app talking to the real Spring API — real
`useTransactionData()` / `useSavingsGoals()` / `useCategories()` hooks,
a submit-able Add form, a live delete on the transactions table, and
API-driven summary cards on the dashboard.

Day 9 (Sprint 8) layers **polish and power features** on top of that:

- **Filter / search / sort** on `TransactionList` (F095–F098)
- **Live transaction-count badge** on the Navbar (F099)
- **Monthly income-vs-expense bar chart** with Recharts (F100/F101)
- **Inline edit row** on `TransactionList` + backend `PUT /api/transactions/{id}` (F102)
- **Contribute-to-goal** mini-form on each savings goal (F103)
- **Toast notifications** for success/error on every mutation (F104)
- **Empty states** across all pages (F105)
- **Consistent currency + date formatting** via `utils/format.js` (F106)
- **A11y polish** — labels, `aria-*`, focus-visible-friendly buttons (F107)

Every file in this folder is a **complete, drop-in replacement** for the
matching starter file. No `TODO TICKET` markers remain.

`TICKET-F102` is the one Day-9 ticket that touches the backend — it adds
a `PUT /api/transactions/{id}` endpoint. `TransactionController.java` and
`TransactionService.java` are therefore included in this folder. The
service's `update(...)` method already exists in the Day-6 solved copy;
the version here is identical except for extra header comments calling
out the F102 wiring.

---

## Files in this folder

```
day9-solved-files/
├── backend/
│   └── src/main/java/com/smartbudget/
│       ├── controller/TransactionController.java   (F056–F059 + F102 PUT)
│       └── service/TransactionService.java         (Day 3/4/6 + F102 update wiring notes)
├── frontend/
│   └── src/
│       ├── hooks/useBudgetAPI.js                   (Day-8 hooks + refetch used by Day 9)
│       ├── utils/format.js                         (F106 — new file)
│       ├── components/
│       │   ├── Feedback.jsx                        (Spinner + Toast + EmptyState; F104/F105)
│       │   ├── MonthlySummaryChart.jsx             (F100 — Recharts bar chart)
│       │   └── Navbar.jsx                          (F099 — live txn-count badge)
│       └── pages/
│           ├── Dashboard.jsx                       (F085 cards + F100/F101 chart + F105 empty)
│           ├── TransactionList.jsx                 (F086/F087 + F095–F098 + F102 + F104/F105/F106)
│           ├── AddTransactionForm.jsx              (F088/F089/F091 + F104 toast on error + F107 a11y)
│           └── SavingsGoals.jsx                    (F090 + F103 contribute + F104/F105/F106)
└── WHERE-TO-PASTE.md
```

Files **not** included (unchanged from earlier day-solved folders or
already in the starter): `App.jsx`, `main.jsx`, `styles/global.css`,
`vite.config.js`, `index.html`, `package.json`, `package-lock.json`,
`SavingsGoalController.java`, `SavingsGoalService.java`, entities,
repositories, exception classes, `data.sql`, `application.properties`.

---

## Overlay command

From the repo root, copy the whole tree into place:

```bash
cp -R day9-solved-files/frontend/ ./frontend/
cp -R day9-solved-files/backend/  ./backend/
```

Both commands are idempotent — they overwrite the starter stubs with
the finished versions and touch nothing else.

> **Apply solved folders in order.** Day 9 builds on Day 8's React
> data-hook layer and on Days 3–6's backend service/controller layer.
> If you overlay `day9-solved-files/` onto a fresh starter without
> having applied earlier day-solved folders first, the app will fail
> because e.g. `SavingsGoalController` still has TODOs (Day 6),
> `TransactionRepository` is missing custom queries (Day 5),
> `data.sql` has no seed rows (Day 5), and so on.
>
> Recommended sequence:
> ```bash
> cp -R day1-solved-files/backend/       ./backend/
> cp -R day2-solved-files/backend/       ./backend/
> cp -R day3-solved-files/backend/       ./backend/
> cp -R day4-solved-files/backend/       ./backend/
> cp -R day5-solved-files/backend/       ./backend/
> cp -R day6-solved-files/backend/       ./backend/
> cp -R day7-solved-files/frontend-static/ ./frontend-static/   # optional
> cp -R day9-solved-files/frontend/      ./frontend/
> cp -R day9-solved-files/backend/       ./backend/             # overrides Day-6 controller
> ```

---

## How to run

### 1. Start the backend (port 8080)

```bash
cd backend
mvn -q spring-boot:run
```

Wait for `Started SmartbudgetApplication` in the log.

### 2. Start the frontend (Vite dev server on port 5173)

```bash
cd frontend
npm install
npm run dev
```

`npm install` is only required the first time (or after `package.json`
changes). Vite proxies `/api/**` to `http://localhost:8080` via
`vite.config.js`, so both processes must be running for the React app
to see data.

Open `http://localhost:5173/` — you should land on the Dashboard.

### About `recharts` and `react-is`

`recharts` is already declared in the starter's `frontend/package.json`
(^3.0.0). But recharts 3.x pulls in **`react-is`** as a peer dependency
that the starter doesn't list. Overlaying this folder's `package.json`
adds `react-is: ^19.0.0` alongside the existing deps — without it,
`npm run build` fails with
`Rollup failed to resolve import "react-is" from ".../recharts/es6/util/ReactUtils.js"`.

If you're patching an existing `frontend/` by hand instead of overlaying
the whole folder, run:

```bash
cd frontend
npm install react-is@^19
```

Then restart `npm run dev`.

---

## What to click through

- **Dashboard** — four stat cards (Income / Expenses / Balance / Count)
  and the monthly bar chart. Hover a bar → tooltip shows the exact
  £-value. Resize the window → the chart scales.
- **Transactions** — nav shows a gold badge with the total row count.
  Filter bar: try `Type = Expense`, `From = 2026-02-01`, and typing
  `salary` in Search — the "Showing X of Y" counter updates live.
  Click **Edit** on any row → cells become inputs → change the amount →
  **Save** fires PUT and re-renders. **Cancel** discards without any
  network call. **Delete** confirms, then pops a green toast.
- **Add** — try submitting with empty category / negative amount /
  future date — each shows an inline `role="alert"` error under the
  offending input. Valid submit navigates back to `/transactions`.
- **Goals** — each card has a mini contribute form. Try `£100` on a
  partially-full goal → progress bar animates and toast confirms.
  Try `-5` or `0` → the form rejects it before any HTTP call. Try
  contributing more than the goal's remaining target → backend returns
  400 and the message appears under the form + as a red toast.

---

## Judgment calls in this solved copy

1. **`FilterBar` is defined inline in `TransactionList.jsx`, not as a
   new file.** F098's Hint 3 shows a separate `FilterBar.jsx` +
   `FilterBar.css`, but keeping it inside `TransactionList` reduces
   file count and matches the guide's `**File:** TransactionList.jsx`
   header verbatim. Extract it if the class prefers per-component files.

2. **`Toast` and `EmptyState` live inside `Feedback.jsx`** rather than
   being split into `Toast.jsx` + `EmptyState.jsx`. The starter already
   exports `Toast` from `Feedback.jsx`, and the F104/F105 tickets say
   "use the `Toast` component from Feedback.jsx" — so we followed that
   file's existing pattern instead of introducing new component files.

3. **No global `ToastProvider` context.** F104's Hint 3 shows an
   optional context-based lift; the acceptance criteria only require
   per-page toasts, and every page already owns a small piece of state,
   so a `useState({type, message})` per page keeps the surface area
   small and the concept easier to teach. Migrating to a context later
   is a straightforward refactor.

4. **`useBudgetAPI.js` uses one `useCallback` + explicit `refetch`**
   rather than the Hint-3 "version counter" trick. Both approaches
   satisfy F102's need for a manual re-fetch; `useCallback` is more
   idiomatic and shows up elsewhere in the guide.

5. **Backend PUT is included; `SavingsGoalController.contribute` is
   not re-shipped.** Day 6 solved files already contain a working
   `/api/goals/{id}/contribute` endpoint, and Day 9's F103 is a
   frontend-only ticket that consumes it — no backend change needed.
   Only the F102 PUT is a genuine Day-9 backend addition.

6. **`Dashboard` shows the welcome `EmptyState` before rendering
   cards** — the guide's F105 Hint 3 does exactly that. Once any
   transaction exists the cards + chart take over.

7. **Currency is hard-coded to GBP** in `utils/format.js` — matches
   the guide's `Intl.NumberFormat('en-GB', { currency: 'GBP' })`.
   Change the two `Intl.*` constants in that file to swap currency
   or locale everywhere.

8. **`Feedback.jsx` also carries a `<TodoBanner>` export**, kept from
   the starter for backward-compat with any pages that still want to
   render it. Day-9 pages don't use it.

---

## Zero-TODO check

```bash
grep -rc "TODO TICKET" day9-solved-files/
```

Expected: every file reports `0`.
