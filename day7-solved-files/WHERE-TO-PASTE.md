# Day 7 — Where to paste

## How this folder works

Day 7 (Sprint 6 — Static HTML/CSS frontend) introduces an **entirely new
folder** called `frontend-static/` that does **not** exist in the starter
repo. Every file in this solved-files folder is brand new; there is
nothing pre-existing to overlay on top of.

Everything under `day7-solved-files/frontend-static/` is meant to be
copied verbatim into a new `frontend-static/` folder at the repo root,
alongside `backend/` and `frontend/`.

```
smartbudget/
  backend/
  frontend/            (React — Day 8/9, unrelated to today)
  frontend-static/     (created today by copying this folder)
    index.html
    transactions.html
    add-transaction.html
    savings.html
    style.css
```

## Files in this solved-folder

| Ticket(s)              | File                                    | Purpose                                              |
| ---------------------- | --------------------------------------- | ---------------------------------------------------- |
| F069                   | `frontend-static/index.html`            | Dashboard page (3 summary cards, recent txns, chart) |
| F070                   | `frontend-static/transactions.html`     | 7-column table with `<tbody id="txn-rows">` hook     |
| F071                   | `frontend-static/add-transaction.html`  | Add-transaction form (5 fields, native validation)   |
| F072                   | `frontend-static/savings.html`          | Savings goals page with static example goal card     |
| F073, F074, F075, F076 | `frontend-static/style.css`             | Combined stylesheet: theme + nav + table + form      |

All four HTML files link `style.css` and reference `app.js` via
`<script defer src="app.js"></script>`. `app.js` does not exist yet —
that is intentional. Browsers will log a 404 for it today; that
resolves on Day 8 once F077 creates the file.

## Overlay command

From the repo root, copy the whole folder into place:

```bash
cp -R day7-solved-files/frontend-static/ ./frontend-static/
```

That is the only thing you need to run. Nothing existing gets
overwritten — the target folder is brand new.

If you want to be defensive and keep any half-finished work you
already had:

```bash
[ -d frontend-static ] && mv frontend-static frontend-static.bak
cp -R day7-solved-files/frontend-static/ ./frontend-static/
```

## How to preview

The pages are 100% static — no build step, no server required.

**macOS:**
```bash
open frontend-static/index.html
```

**Any OS:** drag `frontend-static/index.html` into a browser tab, or
serve the folder with any static server:

```bash
npx serve frontend-static
# → http://localhost:3000
```

Click through the nav to visit all four pages. You should see:

- `index.html` — 3 summary cards showing £0.00 placeholders, an empty
  Recent Transactions list, and a chart-placeholder box.
- `transactions.html` — DB-Blue header row with 7 columns, empty body.
- `add-transaction.html` — the 5-field form; submitting empty triggers
  Chrome's native "Please fill out this field" validation.
- `savings.html` — one example goal card with a 65%-filled progress bar.

Resize the browser below 768px — the dashboard's 3 cards collapse to
a single stacked column. Below 480px the nav wraps and stretches
across the full width.

## Note on the React frontend

The `frontend/` folder in the repo root is the **React** app used on
Days 8 and 9. It has its own build tooling and is completely
independent of `frontend-static/`. Day 7 does not touch it.

## Zero-TODO check

Every file in this folder is a complete, drop-in solution — no
`TODO TICKET` markers remain. Verify with:

```bash
grep -rc "TODO TICKET" day7-solved-files/frontend-static/
```

Expected: every file reports `0`.
