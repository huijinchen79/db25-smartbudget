import { useState, useMemo } from 'react'
import { Link } from 'react-router-dom'
import { useTransactionData } from '../hooks/useBudgetAPI'
import { Spinner, ErrorMessage, EmptyState, Toast } from '../components/Feedback'
import { formatCurrency, formatDate } from '../utils/format'

// ============================================================
// TransactionList — SOLVED for Day 8 + Day 9
// ============================================================
//
// Day 8:
//   F086 — fetch + render table via useTransactionData()
//   F087 — delete rows with confirmation + refetch
//
// Day 9:
//   F095 — filter by type (INCOME / EXPENSE / ALL)
//   F096 — filter by date range (from / to)
//   F097 — case-insensitive search by description
//   F098 — grouped filter bar + Clear Filters + "Showing X of Y"
//   F102 — inline edit row driven by editingId + PUT /api/transactions/{id}
//   F104 — toast notifications for delete / save / errors
//   F105 — EmptyState when the list is empty (or filters exclude all rows)
//   F106 — currency & date rendered via formatCurrency / formatDate
//
// ============================================================
export default function TransactionList() {
  const { transactions, loading, error, refetch } = useTransactionData()

  // --- Filter state (F095/F096/F097) ---
  const [typeFilter, setTypeFilter] = useState('ALL')
  const [from,       setFrom]       = useState('')
  const [to,         setTo]         = useState('')
  const [search,     setSearch]     = useState('')

  // --- Edit state (F102) ---
  const [editingId,  setEditingId]  = useState(null)
  const [editValues, setEditValues] = useState({ amount: '', description: '', type: 'EXPENSE' })

  // --- Toast state (F104) ---
  const [toast,      setToast]      = useState(null)

  // --------------------------------------------------------
  // Derived list — memoised so the filter chain doesn't rerun
  // on every keystroke elsewhere in the page.
  // --------------------------------------------------------
  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase()
    return transactions
      .filter(t => typeFilter === 'ALL' || t.type === typeFilter)
      .filter(t => !from || (t.txnDate ?? '') >= from)
      .filter(t => !to   || (t.txnDate ?? '') <= to)
      .filter(t => !q    || (t.description ?? '').toLowerCase().includes(q))
  }, [transactions, typeFilter, from, to, search])

  function clearAll() {
    setTypeFilter('ALL')
    setFrom('')
    setTo('')
    setSearch('')
  }
  const hasFilters = typeFilter !== 'ALL' || from || to || search

  // --------------------------------------------------------
  // F087 — delete with confirmation + toast
  // --------------------------------------------------------
  async function handleDelete(id) {
    if (!window.confirm('Delete this transaction?')) return
    try {
      const res = await fetch(`/api/transactions/${id}`, { method: 'DELETE' })
      if (!res.ok) throw new Error(`HTTP ${res.status}`)
      setToast({ type: 'success', message: 'Transaction deleted' })
      await refetch()
    } catch (err) {
      setToast({ type: 'error', message: `Delete failed: ${err.message}` })
    }
  }

  // --------------------------------------------------------
  // F102 — edit helpers
  // --------------------------------------------------------
  function startEdit(t) {
    setEditingId(t.txnId)
    setEditValues({
      amount:      String(t.amount ?? ''),
      description: t.description ?? '',
      type:        t.type ?? 'EXPENSE',
    })
  }

  function cancelEdit() {
    setEditingId(null)
  }

  async function saveEdit(t) {
    const amount = parseFloat(editValues.amount)
    if (!Number.isFinite(amount) || amount <= 0) {
      setToast({ type: 'error', message: 'Amount must be a positive number' })
      return
    }
    try {
      const res = await fetch(`/api/transactions/${t.txnId}`, {
        method:  'PUT',
        headers: { 'Content-Type': 'application/json' },
        body:    JSON.stringify({
          amount,
          txnDate:     t.txnDate,
          description: editValues.description,
          type:        editValues.type,
        }),
      })
      if (!res.ok) {
        const body = await res.json().catch(() => null)
        throw new Error(body?.message || `HTTP ${res.status}`)
      }
      setEditingId(null)
      setToast({ type: 'success', message: 'Transaction updated' })
      await refetch()
    } catch (err) {
      setToast({ type: 'error', message: `Update failed: ${err.message}` })
    }
  }

  // --------------------------------------------------------
  // Render
  // --------------------------------------------------------
  if (loading) return <Spinner />
  if (error)   return <ErrorMessage message={error} />

  // F105 — completely empty database.
  if (transactions.length === 0) {
    return (
      <div>
        <div style={headerRow}>
          <h1 style={{ color: 'var(--primary)' }}>Transactions</h1>
          <Link to="/add" className="btn btn-primary">+ Add Transaction</Link>
        </div>
        <EmptyState
          title="No transactions yet"
          body="Start tracking your money — add your first transaction."
          ctaLabel="+ Add Transaction"
          ctaTo="/add"
        />
        <Toast message={toast?.message} type={toast?.type} onClose={() => setToast(null)} />
      </div>
    )
  }

  return (
    <div>
      <div style={headerRow}>
        <h1 style={{ color: 'var(--primary)' }}>
          Transactions
          <span style={badgeStyle} aria-label={`${transactions.length} total`}>
            {transactions.length}
          </span>
        </h1>
        <Link to="/add" className="btn btn-primary">+ Add Transaction</Link>
      </div>

      {/* ---------------------------------------------- */}
      {/* F098 — Filter bar (type / date range / search) */}
      {/* ---------------------------------------------- */}
      <FilterBar
        typeFilter={typeFilter} setTypeFilter={setTypeFilter}
        from={from} setFrom={setFrom}
        to={to}     setTo={setTo}
        search={search} setSearch={setSearch}
        onClear={clearAll}
        hasFilters={hasFilters}
        visibleCount={filtered.length}
        totalCount={transactions.length}
      />

      {/* ---------------------------------------------- */}
      {/* F105 — filter results empty state              */}
      {/* ---------------------------------------------- */}
      {filtered.length === 0 ? (
        <EmptyState
          title="No matches"
          body="No transactions match your current filters."
          ctaLabel="Clear filters"
          onCta={clearAll}
        />
      ) : (
        <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
          <table>
            <thead>
              <tr>
                <th>Date</th>
                <th>Description</th>
                <th>Category</th>
                <th>Type</th>
                <th style={{ textAlign: 'right' }}>Amount</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map(t =>
                editingId === t.txnId
                  ? (
                    <tr key={t.txnId}>
                      <td>{formatDate(t.txnDate)}</td>
                      <td>
                        <input
                          aria-label="Description"
                          value={editValues.description}
                          onChange={e => setEditValues(v => ({ ...v, description: e.target.value }))}
                          style={editInputStyle}
                        />
                      </td>
                      <td>{t.category?.name}</td>
                      <td>
                        <select
                          aria-label="Type"
                          value={editValues.type}
                          onChange={e => setEditValues(v => ({ ...v, type: e.target.value }))}
                          style={editInputStyle}
                        >
                          <option value="INCOME">INCOME</option>
                          <option value="EXPENSE">EXPENSE</option>
                        </select>
                      </td>
                      <td style={{ textAlign: 'right' }}>
                        <input
                          aria-label="Amount"
                          type="number"
                          step="0.01"
                          min="0.01"
                          value={editValues.amount}
                          onChange={e => setEditValues(v => ({ ...v, amount: e.target.value }))}
                          style={{ ...editInputStyle, textAlign: 'right', width: '7rem' }}
                        />
                      </td>
                      <td style={{ textAlign: 'right', whiteSpace: 'nowrap' }}>
                        <button className="btn btn-success"   style={rowBtn} onClick={() => saveEdit(t)}>Save</button>
                        <button className="btn btn-secondary" style={rowBtn} onClick={cancelEdit}>Cancel</button>
                      </td>
                    </tr>
                  )
                  : (
                    <tr key={t.txnId}>
                      <td>{formatDate(t.txnDate)}</td>
                      <td>{t.description}</td>
                      <td>{t.category?.name}</td>
                      <td>
                        <span className={`badge badge--${(t.type ?? '').toLowerCase()}`}>{t.type}</span>
                      </td>
                      <td
                        style={{
                          textAlign: 'right',
                          color: t.type === 'INCOME' ? 'var(--success)' : 'var(--danger)',
                          fontWeight: 600,
                        }}
                      >
                        {formatCurrency(t.amount)}
                      </td>
                      <td style={{ textAlign: 'right', whiteSpace: 'nowrap' }}>
                        <button
                          className="btn btn-secondary"
                          style={rowBtn}
                          onClick={() => startEdit(t)}
                          aria-label={`Edit transaction ${t.txnId}`}
                        >
                          Edit
                        </button>
                        <button
                          className="btn btn-danger"
                          style={rowBtn}
                          onClick={() => handleDelete(t.txnId)}
                          aria-label={`Delete transaction ${t.txnId}`}
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  )
              )}
            </tbody>
          </table>
        </div>
      )}

      <Toast message={toast?.message} type={toast?.type} onClose={() => setToast(null)} />
    </div>
  )
}

// ============================================================
// TICKET-F098 — FilterBar sub-component
// ============================================================
function FilterBar({
  typeFilter, setTypeFilter,
  from, setFrom, to, setTo,
  search, setSearch,
  onClear, hasFilters,
  visibleCount, totalCount,
}) {
  return (
    <div
      className="card"
      role="search"
      aria-label="Filter transactions"
      style={{
        display: 'flex',
        flexWrap: 'wrap',
        gap: '1rem',
        alignItems: 'flex-end',
        marginBottom: '1rem',
      }}
    >
      <label style={fieldStyle}>
        <span style={fieldLabel}>Type</span>
        <select
          value={typeFilter}
          onChange={e => setTypeFilter(e.target.value)}
          style={inputStyle}
        >
          <option value="ALL">All</option>
          <option value="INCOME">Income</option>
          <option value="EXPENSE">Expense</option>
        </select>
      </label>

      <label style={fieldStyle}>
        <span style={fieldLabel}>From</span>
        <input
          type="date"
          value={from}
          onChange={e => setFrom(e.target.value)}
          style={inputStyle}
        />
      </label>

      <label style={fieldStyle}>
        <span style={fieldLabel}>To</span>
        <input
          type="date"
          value={to}
          onChange={e => setTo(e.target.value)}
          style={inputStyle}
        />
      </label>

      <label style={{ ...fieldStyle, flex: 1, minWidth: '180px' }}>
        <span style={fieldLabel}>Search</span>
        <input
          type="search"
          placeholder="Description contains…"
          value={search}
          onChange={e => setSearch(e.target.value)}
          aria-label="Search by description"
          style={inputStyle}
        />
      </label>

      <button
        type="button"
        className="btn btn-secondary"
        onClick={onClear}
        disabled={!hasFilters}
        style={{ height: '2.4rem' }}
      >
        Clear filters
      </button>

      <span style={{ marginLeft: 'auto', color: 'var(--text-muted)', fontSize: '0.9rem' }}>
        Showing <b>{visibleCount}</b> of <b>{totalCount}</b>
      </span>
    </div>
  )
}

// --- Inline style bags (kept in this file to avoid a new CSS module) ---
const headerRow = {
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'center',
  marginBottom: '1.5rem',
}
const badgeStyle = {
  display: 'inline-block',
  background: 'var(--gold)',
  color: '#000',
  fontSize: '0.75rem',
  fontWeight: 700,
  padding: '0.15rem 0.55rem',
  borderRadius: 999,
  marginLeft: '.6rem',
  verticalAlign: 'middle',
}
const rowBtn = { padding: '0.35rem 0.75rem', fontSize: '0.8rem', marginLeft: '0.4rem' }
const fieldStyle = { display: 'flex', flexDirection: 'column', gap: '0.25rem', fontSize: '0.85rem' }
const fieldLabel = { color: 'var(--text-muted)', fontWeight: 600 }
const inputStyle = {
  padding: '0.45rem 0.65rem',
  border: '1px solid var(--border)',
  borderRadius: 'var(--radius)',
  fontSize: '0.9rem',
  minWidth: '10rem',
}
const editInputStyle = {
  padding: '0.35rem 0.55rem',
  border: '1px solid var(--border)',
  borderRadius: 'var(--radius)',
  fontSize: '0.85rem',
  width: '100%',
}
