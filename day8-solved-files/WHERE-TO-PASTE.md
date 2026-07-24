# Day 8 — Where To Paste

Day 8 has two halves:

1. **Morning (F077–F081)** – add JavaScript interactivity to yesterday's
   static `frontend-static/` pages. These files did **not** exist in the
   Day 7 starter and are created fresh here.
2. **Afternoon (F082–F094)** – switch to the React app in `frontend/`.
   The starter ships with `// TODO TICKET-F…` comment blocks, mock data,
   and (in `Dashboard.jsx`) a proof-of-life connectivity check. The
   files in this folder are drop-in replacements that wire the real API,
   validation, error banners, loading spinners, and delete flow.

## How this folder works

- `day8-solved-files/frontend-static/` — brand-new `app.js` plus a full
  `style.css` overlay (Day 7 baseline + the F081 spinner rules). Loaded
  by every page from Day 7 via the `<script defer src="app.js">` tag.
- `day8-solved-files/frontend/` — drop-in replacements for the React
  files listed under _Files_ below. Every yellow-banner `// TODO TICKET`
  region is replaced with real code; the top-of-file comments that
  explain _why_ each file exists are preserved.

## Files

### `frontend-static/`
| File | Ticket(s) | What changed |
|---|---|---|
| `app.js` | F077, F078, F079, F080, F081 | Brand-new file — form validation, GET/POST/DELETE fetch, delete-with-confirm via event delegation, spinner toggle. |
| `style.css` | F081 | Full overlay of Day 7's stylesheet + spinner rule + `#loading` layout + `.delete-btn` styling. |

### `frontend/src/`
| File | Ticket(s) | What changed |
|---|---|---|
| `App.jsx` | F083 | Kept the provided BrowserRouter + 4 routes; added a `*` catch-all `<NotFound />` route. |
| `components/Navbar.jsx` | F084 | Unchanged from the "provided" starter — `NavLink` already highlights the active route. Comment updated to reference F084. |
| `components/TransactionRow.jsx` | F087 | **New file** — dumb row component: `txn` in, `onDelete(id)` out. |
| `hooks/useBudgetAPI.js` | F091 | Implements `useTransactionData`, `useSavingsGoals(userId)`, `useCategories`. Each owns `data / loading / error` (plus `refetch` for the first two) and runs `fetch` inside `useEffect`. |
| `pages/Dashboard.jsx` | F085, F091, F092, F093 | Removed mock/proof-of-life, uses `useTransactionData()`, reduces via `useMemo` into 4 stat cards, early-returns `<Spinner />` on load and `<ErrorMessage />` on error. |
| `pages/TransactionList.jsx` | F086, F087, F091, F092, F093 | Real fetch, `<TransactionRow />` per row (with `key={t.txnId}`), delete flow uses `refetch()`, spinner + error banner. |
| `pages/AddTransactionForm.jsx` | F088, F089, F091, F092 | Controlled form (single `form` state object + one `handleChange`), inline field validation, category dropdown from `useCategories()`, async POST via the Vite `/api` proxy, redirect to `/transactions` on success, red banner on API error. |
| `pages/SavingsGoals.jsx` | F090, F091, F092, F093, F094 | Real fetch via `useSavingsGoals(1)`, responsive auto-fill grid, colour-graded progress bar (red < 33% < amber < 66% < green), spinner + error banner. |

The starter's `components/Feedback.jsx` already provides `Spinner`,
`ErrorMessage`, `Toast`, and `TodoBanner`, so we import from there
instead of creating parallel components (this is what the guide's
F092/F093 "extract into shared component" advice recommends — we're
just reusing the shared components that already exist). The starter's
`styles/global.css` already carries the DB-Blue theme, `.stat-grid`,
`.spinner`, `.progress-bar-*`, and `.badge--income/expense` classes
that F094 asks for, so no CSS file rewrite is needed for the React
half — the pages use the existing classes.

## Overlay (copy on top of your working tree)

From the repo root:

```bash
cp -R day8-solved-files/frontend/         frontend/
cp -R day8-solved-files/frontend-static/  frontend-static/
```

The `frontend-static/` directory doesn't exist in the fresh repo yet —
the copy creates it. Day 7's HTML files (`index.html`,
`transactions.html`, `add-transaction.html`, `savings.html`) live in
`day7-solved-files/frontend-static/` and are what the Day 8 `app.js`
targets, so copy those over first if you don't already have them:

```bash
cp -R day7-solved-files/frontend-static/  frontend-static/
cp -R day8-solved-files/frontend-static/  frontend-static/   # then Day 8 overlays app.js + style.css
```

## How to run

**Backend** (in one terminal, must be running for either half to have
data):

```bash
cd backend && ./mvnw spring-boot:run
```

**React app** (in another terminal):

```bash
cd frontend
npm install                # first time only — pulls React 19 + Vite 6
npm run dev                # http://localhost:5173
```

Vite proxies `/api/*` to `http://localhost:8080` (see
`frontend/vite.config.js`), so React calls like `fetch('/api/transactions')`
hit the Spring Boot backend with no CORS dance.

**Static pages** (optional — for the F077–F081 half):

```bash
npx serve frontend-static  # http://localhost:3000
```

Opening the HTML files via `file://` may trigger CORS errors on the
`fetch('http://localhost:8080/...')` calls; serving them from
`localhost:3000` avoids that (the backend's `CorsConfig` allows both
`:5173` and `:3000`).

## Sanity check

- Dashboard `/` — 4 stat cards render with real numbers pulled from
  `/api/transactions`. Slow-3G in DevTools → spinner shows first.
- Transactions `/transactions` — table with one row per transaction,
  amounts coloured green/red, Delete confirms then removes without a
  page reload.
- Add `/add` — every input is controlled (React DevTools shows state
  ticking on each keystroke); invalid fields show red inline messages;
  a successful POST redirects to `/transactions` and the new row is at
  the bottom.
- Savings `/savings` — cards with progress bars whose fill matches the
  percentage; > 100% caps at 100% width.
- Stop the backend, reload any data page → red `[Warning]` banner from
  `<ErrorMessage />`; restart the backend + refresh → data returns.
- Unknown URL like `/junk` → 404 page with "Back to Dashboard" link.
