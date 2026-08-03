import { useState, useMemo } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useCategories } from '../hooks/useBudgetAPI'
import { Toast } from '../components/Feedback'

// ============================================================
// TICKET-F088/F089/F091 (Day 8) + F104/F107 (Day 9) — Add Transaction Form
// ============================================================
//
// Day 8:
//   F088 — controlled form (state-driven inputs)
//   F091 — category dropdown fed from /api/categories
//   F089 — client-side validation + POST /api/transactions
//
// Day 9:
//   F104 — toast on submit error (success = navigate away)
//   F107 — labels/aria wiring for keyboard + screen-reader use
//
// ============================================================
const today = () => new Date().toISOString().substring(0, 10)

export default function AddTransactionForm() {
  const navigate   = useNavigate()
  const categories = useCategories()

  const [form, setForm] = useState({
    categoryId:  '',
    amount:      '',
    txnDate:     today(),
    description: '',
  })
  const [errors, setErrors] = useState({})
  const [busy,   setBusy]   = useState(false)
  const [toast,  setToast]  = useState(null)

  // Determine type from the selected category (matches backend enum).
  const selectedCategory = useMemo(
    () => categories.find(c => String(c.categoryId) === String(form.categoryId)),
    [categories, form.categoryId]
  )

  function handleChange(e) {
    const { name, value } = e.target
    setForm(prev => ({ ...prev, [name]: value }))
  }

  function validate() {
    const errs = {}
    if (!form.categoryId)                              errs.categoryId  = 'Please select a category'
    const amt = parseFloat(form.amount)
    if (!Number.isFinite(amt) || amt <= 0)             errs.amount      = 'Amount must be greater than 0'
    if (!form.txnDate)                                 errs.txnDate     = 'Date is required'
    else if (form.txnDate > today())                   errs.txnDate     = 'Date cannot be in the future'
    return errs
  }

  async function handleSubmit(e) {
    e.preventDefault()
    const errs = validate()
    setErrors(errs)
    if (Object.keys(errs).length > 0) return

    setBusy(true)
    try {
      const res = await fetch('/api/transactions', {
        method:  'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          user:        { userId: 1 },
          category:    { categoryId: Number(form.categoryId) },
          amount:      parseFloat(form.amount),
          txnDate:     form.txnDate,
          description: form.description,
          type:        selectedCategory?.type || 'EXPENSE',
        }),
      })
      if (!res.ok) {
        const body = await res.json().catch(() => null)
        throw new Error(body?.message || `HTTP ${res.status}`)
      }
      navigate('/transactions')
    } catch (err) {
      setToast({ type: 'error', message: `Could not save: ${err.message}` })
    } finally {
      setBusy(false)
    }
  }

  return (
    <div>
      <h1 style={{ marginBottom: '1.5rem', color: 'var(--primary)' }}>Add Transaction</h1>

      <form
        onSubmit={handleSubmit}
        className="card"
        style={{ maxWidth: 540 }}
        noValidate
        aria-label="Add a transaction"
      >
        <div className="form-group">
          <label htmlFor="categoryId">Category</label>
          <select
            id="categoryId"
            name="categoryId"
            value={form.categoryId}
            onChange={handleChange}
            aria-invalid={!!errors.categoryId}
            aria-describedby={errors.categoryId ? 'categoryId-error' : undefined}
          >
            <option value="">-- Select category --</option>
            {categories.map(c => (
              <option key={c.categoryId} value={c.categoryId}>
                {c.name} ({c.type})
              </option>
            ))}
          </select>
          {errors.categoryId && (
            <span id="categoryId-error" role="alert" style={errStyle}>{errors.categoryId}</span>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="amount">Amount (£)</label>
          <input
            id="amount"
            name="amount"
            type="number"
            step="0.01"
            min="0.01"
            value={form.amount}
            onChange={handleChange}
            aria-invalid={!!errors.amount}
            aria-describedby={errors.amount ? 'amount-error' : undefined}
          />
          {errors.amount && (
            <span id="amount-error" role="alert" style={errStyle}>{errors.amount}</span>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="txnDate">Date</label>
          <input
            id="txnDate"
            name="txnDate"
            type="date"
            value={form.txnDate}
            max={today()}
            onChange={handleChange}
            aria-invalid={!!errors.txnDate}
            aria-describedby={errors.txnDate ? 'txnDate-error' : undefined}
          />
          {errors.txnDate && (
            <span id="txnDate-error" role="alert" style={errStyle}>{errors.txnDate}</span>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="description">Description</label>
          <input
            id="description"
            name="description"
            type="text"
            value={form.description}
            onChange={handleChange}
            placeholder="e.g. Weekly groceries"
          />
        </div>

        <div style={{ display: 'flex', gap: '0.75rem', marginTop: '0.5rem' }}>
          <button type="submit" className="btn btn-primary" disabled={busy}>
            {busy ? 'Saving…' : 'Add Transaction'}
          </button>
          <Link to="/transactions" className="btn btn-secondary">Cancel</Link>
        </div>
      </form>

      <Toast message={toast?.message} type={toast?.type} onClose={() => setToast(null)} />
    </div>
  )
}

const errStyle = { color: 'var(--danger)', fontSize: '0.8rem' }
