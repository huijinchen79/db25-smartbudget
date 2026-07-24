// ============================================================
// TICKET-F087 (Day 8, Sprint 7) — TransactionRow
// ============================================================
//
// WHAT: One <tr> for the transactions table. A "dumb" component:
//       data comes in via the `txn` prop and delete events go out
//       via the `onDelete` callback prop. The parent decides what
//       "delete" actually means (fetch, optimistic update, ...).
//
// WHY:  Data-in / events-out keeps the row trivially reusable and
//       testable — the parent owns state and side effects.
// ============================================================

export default function TransactionRow({ txn, onDelete }) {
  const isIncome = txn.type === 'INCOME'

  return (
    <tr>
      <td>{txn.txnId}</td>
      <td>{txn.txnDate}</td>
      <td>{txn.category?.name ?? '—'}</td>
      <td>{txn.description}</td>
      <td style={{
        textAlign: 'right',
        fontVariantNumeric: 'tabular-nums',
        color: isIncome ? 'var(--success)' : 'var(--danger)',
        fontWeight: 600,
      }}>
        £{Number(txn.amount).toFixed(2)}
      </td>
      <td>
        <span className={`badge badge--${txn.type.toLowerCase()}`}>{txn.type}</span>
      </td>
      <td>
        <button
          type="button"
          className="btn btn-danger"
          style={{ padding: '0.3rem 0.75rem', fontSize: '0.8rem' }}
          onClick={() => onDelete(txn.txnId)}
        >
          Delete
        </button>
      </td>
    </tr>
  )
}
