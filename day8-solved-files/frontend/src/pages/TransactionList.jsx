import { Link } from 'react-router-dom'
import { useTransactionData } from '../hooks/useBudgetAPI'
import { Spinner, ErrorMessage } from '../components/Feedback'
import TransactionRow from '../components/TransactionRow'

// ============================================================
// TICKET-F086/F087 (Day 8) — Transaction List Page
// ============================================================
//
// WHAT: This page displays all transactions in a table.
//       It's the main data view of the application.
//
// WHY:  Users need to see their transaction history and perform actions
//       (delete) on individual records.
//
// TICKET-F086: fetch via useTransactionData(), render <TransactionRow />
//              per row. `key={t.txnId}` is essential — without it React
//              re-renders every row on insert/delete.
// TICKET-F087: each row's Delete button calls back into handleDelete here.
// TICKET-F091: real data from the API (no more mock array).
// TICKET-F092: error banner via <ErrorMessage /> if the fetch fails.
// TICKET-F093: <Spinner /> while the fetch is in flight.
//
// TICKET-F098 (Day 9): filter bar — done later.
// TICKET-F102 (Day 9): inline edit — done later.
// ============================================================

export default function TransactionList() {
  const { transactions, loading, error, refetch } = useTransactionData()

  // F087 — confirm, DELETE, then refetch. refetch() re-runs the hook's
  // fetchData and React re-renders the table without the deleted row.
  async function handleDelete(id) {
    if (!window.confirm(`Delete transaction #${id}? This cannot be undone.`)) return
    try {
      const res = await fetch(`/api/transactions/${id}`, { method: 'DELETE' })
      if (!res.ok) throw new Error(`HTTP ${res.status}`)
      refetch()
    } catch (err) {
      alert(`Could not delete transaction #${id}: ${err.message}`)
    }
  }

  if (loading) return <Spinner />
  if (error)   return <ErrorMessage message={error} />

  return (
    <div>
      <div style={{
        display: 'flex', justifyContent: 'space-between',
        alignItems: 'center', marginBottom: '1.5rem'
      }}>
        <h1 style={{ color: 'var(--primary)' }}>Transactions</h1>
        <Link to="/add" className="btn btn-primary">+ Add Transaction</Link>
      </div>

      {transactions.length === 0 ? (
        <div className="card" style={{ textAlign: 'center', padding: '2rem', color: 'var(--text-muted)' }}>
          No transactions yet. Click <Link to="/add">Add Transaction</Link> to create one.
        </div>
      ) : (
        <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Date</th>
                <th>Category</th>
                <th>Description</th>
                <th style={{ textAlign: 'right' }}>Amount</th>
                <th>Type</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {transactions.map(t => (
                <TransactionRow key={t.txnId} txn={t} onDelete={handleDelete} />
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
