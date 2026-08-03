import { useMemo } from 'react'
import { Link } from 'react-router-dom'
import { useTransactionData } from '../hooks/useBudgetAPI'
import { Spinner, ErrorMessage } from '../components/Feedback'

// ============================================================
// Dashboard — the landing page of SmartBudget
// ============================================================
//
// TICKET-F085 (Day 8, Sprint 7): Summary stat cards
//   - Total Income, Total Expenses, Balance, Transaction Count
//   - Loading spinner while data fetches (F093)
//   - Error message if the backend is down (F092)
//
// TICKET-F091: real data via useTransactionData() instead of a mock array.
// TICKET-F100 (Day 9): monthly summary bar chart — done later.
// ============================================================

export default function Dashboard() {
    const { transactions, loading, error } = useTransactionData()

    // useMemo caches the reduce — stable for the same transactions ref.
    const stats = useMemo(() => {
        let income = 0, expenses = 0
        for (const t of transactions) {
            if (t.type === 'INCOME')  income   += Number(t.amount)
            if (t.type === 'EXPENSE') expenses += Number(t.amount)
        }
        return {
            income,
            expenses,
            balance: income - expenses,
            count:   transactions.length,
        }
    }, [transactions])

    // F093 — loading state comes first so the page never flashes
    //         empty totals before the fetch lands.
    if (loading) return <Spinner />
    // F092 — surface the error instead of a blank page.
    if (error)   return <ErrorMessage message={error} />

    const fmt = (n) => '£' + n.toFixed(2)

    return (
        <div>
            <h1 style={{ marginBottom: '1.5rem', color: 'var(--primary)' }}>Dashboard</h1>
            <p style={{ marginBottom: '1.5rem', color: 'var(--text-muted)' }}>
                Welcome to <strong>SmartBudget</strong> — your personal finance tracker.
            </p>

            <section className="stat-grid">
                <StatCard label="Total Income"       value={fmt(stats.income)}   color="var(--success)" />
                <StatCard label="Total Expenses"     value={fmt(stats.expenses)} color="var(--danger)"  />
                <StatCard label="Balance"            value={fmt(stats.balance)}
                          color={stats.balance < 0 ? 'var(--danger)' : 'var(--primary)'} />
                <StatCard label="Transactions"       value={stats.count}         color="var(--primary)" />
            </section>

            <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
                <Link to="/add"          className="btn btn-primary">+ Add Transaction</Link>
                <Link to="/transactions" className="btn btn-secondary">View Transactions</Link>
                <Link to="/savings"      className="btn btn-secondary">Savings Goals</Link>
            </div>
        </div>
    )
}

/** Small helper — one stat block on the dashboard grid. */
function StatCard({ label, value, color }) {
    return (
        <div className="card stat-card">
            <div className="stat-card__label">{label}</div>
            <div className="stat-card__value" style={{ color }}>{value}</div>
        </div>
    )
}