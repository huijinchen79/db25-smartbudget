import { useMemo } from 'react'
import {
  BarChart, Bar, XAxis, YAxis,
  Tooltip, Legend, ResponsiveContainer, CartesianGrid,
} from 'recharts'

// ============================================================
// TICKET-F100 (Day 9, Sprint 8) — Monthly Summary Bar Chart  [SOLVED]
// ============================================================
//
// WHAT: A bar chart that shows income vs. expenses for each month.
//       Uses the Recharts library — a React wrapper around D3.js.
//       Receives a raw `transactions` array as a prop and aggregates
//       it into per-month totals before rendering.
//
// WHY:  Tabular data hides trends. A side-by-side bar chart makes
//       "am I spending more than I earn?" answerable in one glance.
//
// KEY CONCEPTS:
//   Props:     Data passed FROM a parent component TO this child component
//   useMemo:   Caches expensive calculations so aggregation only re-runs
//              when the transactions array actually changes
//   Recharts:  React charting library — every element (BarChart, Bar,
//              XAxis, YAxis, Tooltip, Legend) is a React component
//
// ============================================================

export default function MonthlySummaryChart({ transactions = [] }) {

  // -------------------------------------------------------
  // Step 1 — Aggregate transactions by month
  // -------------------------------------------------------
  // Transform the flat transactions array into monthly summaries.
  // Each month becomes: { month: "2026-05", income: 3500, expense: 1200 }
  //
  // useMemo prevents this aggregation from running on every render —
  // it only re-computes when `transactions` changes.
  const data = useMemo(() => {
    const map = {}
    for (const t of transactions) {
      const month = (t.txnDate ?? '').substring(0, 7) // "YYYY-MM"
      if (!month) continue
      if (!map[month]) map[month] = { month, income: 0, expense: 0 }
      const amt = Number(t.amount) || 0
      if (t.type === 'INCOME')  map[month].income  += amt
      if (t.type === 'EXPENSE') map[month].expense += amt
    }
    return Object.values(map)
      .sort((a, b) => a.month.localeCompare(b.month))
      .map(d => ({
        ...d,
        income:  Math.round(d.income  * 100) / 100,
        expense: Math.round(d.expense * 100) / 100,
      }))
  }, [transactions])

  // -------------------------------------------------------
  // Empty state — nothing to plot
  // -------------------------------------------------------
  if (data.length === 0) {
    return (
      <p style={{ textAlign: 'center', color: 'var(--text-muted)', padding: '1rem' }}>
        No data to chart yet.
      </p>
    )
  }

  // -------------------------------------------------------
  // Step 2 — Render the Recharts bar chart
  // -------------------------------------------------------
  // ResponsiveContainer listens to its parent's width and resizes the
  // chart on window resize. Tooltip.formatter controls the on-hover
  // label — here we prepend "£" and fix two decimals.
  return (
    <ResponsiveContainer width="100%" height={320}>
      <BarChart data={data} margin={{ top: 16, right: 24, left: 0, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" />
        <XAxis dataKey="month" />
        <YAxis tickFormatter={v => '£' + v} />
        <Tooltip
          formatter={(value, key) => [
            '£' + Number(value).toFixed(2),
            key.charAt(0).toUpperCase() + key.slice(1),
          ]}
        />
        <Legend />
        <Bar dataKey="income"  fill="var(--success)" name="Income" />
        <Bar dataKey="expense" fill="var(--danger)"  name="Expense" />
      </BarChart>
    </ResponsiveContainer>
  )
}
