import { useMemo } from 'react'
import { Link } from 'react-router-dom'
import { useTransactionData } from '../hooks/useBudgetAPI'
import MonthlySummaryChart    from '../components/MonthlySummaryChart'
import { Spinner, ErrorMessage, EmptyState } from '../components/Feedback'
import { formatCurrency } from '../utils/format'

// ============================================================
// Dashboard — the landing page of SmartBudget  [SOLVED for Day 8 + Day 9]
// ============================================================
//
// TICKET-F085 (Day 8): Summary stat cards fed by the real API
// TICKET-F100 / F101 (Day 9): MonthlySummaryChart mounted below the cards
// TICKET-F105 (Day 9): friendly EmptyState when no transactions exist
// TICKET-F106 (Day 9): amounts rendered via formatCurrency()
//
// ============================================================
export default function Dashboard() {
  const { transactions, loading, error } = useTransactionData()

  // Derived totals — recomputed only when transactions change.
  const totals = useMemo(() => {
    let income = 0, expenses = 0
    for (const t of transactions) {
      const amt = Number(t.amount) || 0
      if (t.type === 'INCOME')  income   += amt
      if (t.type === 'EXPENSE') expenses += amt
    }
    return {
      income,
      expenses,
      balance: income - expenses,
      count:   transactions.length,
    }
  }, [transactions])

  if (loading) return <Spinner />
  if (error)   return <ErrorMessage message={error} />

  // F105 — welcome empty state when no data exists yet.
  if (transactions.length === 0) {
    return (
      <div>
        <h1 style={{ marginBottom: '1.5rem', color: 'var(--primary)' }}>Dashboard</h1>
        <EmptyState
          title="Welcome to SmartBudget"
          body="Add your first transaction to see income, expenses, and trends."
          ctaLabel="+ Add Transaction"
          ctaTo="/add"
        />
      </div>
    )
  }

  return (
    <div>
      <h1 style={{ marginBottom: '1.5rem', color: 'var(--primary)' }}>Dashboard</h1>
      <p style={{ marginBottom: '1.5rem', color: 'var(--text-muted)' }}>
        Welcome to <strong>SmartBudget</strong> — your personal finance tracker.
      </p>

      {/* ------------------------------------------------------- */}
      {/* TICKET-F085 (Day 8) — Summary stat cards                */}
      {/* ------------------------------------------------------- */}
      <div className="stat-grid">
        <StatCard label="Total Income"      value={formatCurrency(totals.income)}   colour="var(--success)" />
        <StatCard label="Total Expenses"    value={formatCurrency(totals.expenses)} colour="var(--danger)" />
        <StatCard label="Balance"           value={formatCurrency(totals.balance)}
                  colour={totals.balance < 0 ? 'var(--danger)' : 'var(--primary)'} />
        <StatCard label="Transactions"      value={String(totals.count)}            colour="var(--primary)" />
      </div>

      {/* ------------------------------------------------------- */}
      {/* TICKET-F100 / F101 (Day 9) — Monthly summary chart      */}
      {/* ------------------------------------------------------- */}
      <section
        className="card"
        style={{ marginBottom: '1.5rem' }}
        aria-label="Monthly income vs expenses"
      >
        <h3 style={{ marginBottom: '1rem', color: 'var(--primary)' }}>
          Monthly Income vs Expenses
        </h3>
        <MonthlySummaryChart transactions={transactions} />
      </section>

      <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
        <Link to="/add"          className="btn btn-primary">+ Add Transaction</Link>
        <Link to="/transactions" className="btn btn-secondary">View Transactions</Link>
        <Link to="/savings"      className="btn btn-secondary">Savings Goals</Link>
      </div>
    </div>
  )
}

function StatCard({ label, value, colour }) {
  return (
    <div className="card stat-card">
      <div className="stat-card__label">{label}</div>
      <div className="stat-card__value" style={{ color: colour }}>{value}</div>
    </div>
  )
}
