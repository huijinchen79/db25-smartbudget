import { useState } from 'react'
import { useSavingsGoals } from '../hooks/useBudgetAPI'
import { Spinner, ErrorMessage, EmptyState, Toast } from '../components/Feedback'
import { formatCurrency, formatDate } from '../utils/format'

// ============================================================
// TICKET-F090 (Day 8) + F103/F104/F105/F106 (Day 9) — Savings Goals
// ============================================================
//
// Day 8 (F090): fetch + render goal cards with progress bars.
// Day 9:
//   F103 — per-card Contribute mini-form → PUT /api/goals/{id}/contribute
//   F104 — toast notifications for success/error
//   F105 — EmptyState when the user has no goals
//   F106 — currency + date rendered via helpers
//
// ============================================================
export default function SavingsGoals() {
  const { goals, loading, error, refetch } = useSavingsGoals(1)   // hardcoded user 1 for now
  const [toast, setToast] = useState(null)

  async function contribute(id, amount) {
    const res = await fetch(`/api/goals/${id}/contribute`, {
      method:  'PUT',
      headers: { 'Content-Type': 'application/json' },
      body:    JSON.stringify({ amount }),
    })
    if (!res.ok) {
      const body = await res.json().catch(() => null)
      throw new Error(body?.message || `HTTP ${res.status}`)
    }
    await refetch()
  }

  if (loading) return <Spinner />
  if (error)   return <ErrorMessage message={error} />

  return (
    <div>
      <h1 style={{ marginBottom: '1.5rem', color: 'var(--primary)' }}>Savings Goals</h1>

      {goals.length === 0 ? (
        <EmptyState
          title="No savings goals yet"
          body="Set a goal and start saving towards it."
        />
      ) : (
        <div style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))',
          gap: '1.2rem',
        }}>
          {goals.map(g => (
            <GoalCard
              key={g.goalId}
              goal={g}
              onContribute={async (amount) => {
                try {
                  await contribute(g.goalId, amount)
                  setToast({ type: 'success', message: `Contributed ${formatCurrency(amount)} to ${g.goalName}` })
                } catch (err) {
                  setToast({ type: 'error', message: err.message })
                  throw err
                }
              }}
            />
          ))}
        </div>
      )}

      <Toast message={toast?.message} type={toast?.type} onClose={() => setToast(null)} />
    </div>
  )
}

// ============================================================
// TICKET-F103 — GoalCard with progress bar + Contribute form
// ============================================================
function GoalCard({ goal, onContribute }) {
  const target  = Number(goal.targetAmount)  || 0
  const current = Number(goal.currentAmount) || 0
  const pct     = Math.min(100, Math.max(0, target > 0 ? (current / target) * 100 : 0))
  const barColour = pct < 33 ? 'var(--danger)'
                  : pct < 66 ? 'var(--gold)'
                  : 'var(--success)'

  const [amount, setAmount] = useState('')
  const [busy,   setBusy]   = useState(false)
  const [err,    setErr]    = useState(null)

  async function submit(e) {
    e.preventDefault()
    const value = parseFloat(amount)
    if (!Number.isFinite(value) || value <= 0) {
      setErr('Enter a positive amount')
      return
    }
    setBusy(true)
    setErr(null)
    try {
      await onContribute(value)
      setAmount('')
    } catch (e2) {
      // parent already toasted; surface it here too under the form.
      setErr(e2.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <article className="card" aria-label={`Savings goal: ${goal.goalName}`}>
      <h3 style={{ marginBottom: '0.3rem', color: 'var(--primary)' }}>{goal.goalName}</h3>
      {goal.deadline && (
        <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
          Deadline: {formatDate(goal.deadline)}
        </p>
      )}

      <p style={{ margin: '0.5rem 0 0.25rem' }}>
        {formatCurrency(current)} <span style={{ color: 'var(--text-muted)' }}>of</span> {formatCurrency(target)}{' '}
        <span style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>({pct.toFixed(0)}%)</span>
      </p>

      <div className="progress-bar-bg" aria-hidden="true">
        <div
          className="progress-bar-fill"
          style={{ width: `${pct}%`, background: barColour }}
        />
      </div>

      <form
        onSubmit={submit}
        style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}
        aria-label={`Contribute to ${goal.goalName}`}
      >
        <input
          type="number"
          step="0.01"
          min="0.01"
          placeholder="Contribute…"
          aria-label="Contribution amount"
          value={amount}
          onChange={e => setAmount(e.target.value)}
          disabled={busy}
          style={{
            flex: 1,
            padding: '0.45rem 0.65rem',
            border: '1px solid var(--border)',
            borderRadius: 'var(--radius)',
            fontSize: '0.9rem',
          }}
        />
        <button
          type="submit"
          className="btn btn-primary"
          disabled={busy || !amount}
        >
          {busy ? '…' : 'Contribute'}
        </button>
      </form>

      {err && (
        <p role="alert" style={{ color: 'var(--danger)', marginTop: '0.5rem', fontSize: '0.85rem' }}>
          {err}
        </p>
      )}
    </article>
  )
}
