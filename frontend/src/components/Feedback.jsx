import { useEffect } from 'react'
import { Link } from 'react-router-dom'

// ============================================================
// PROVIDED — reusable UI feedback components
// (extended in Day 9 with Toast + EmptyState per F104 / F105)
// ============================================================

export function Spinner() {
  return <div className="spinner-wrapper" role="status" aria-live="polite">
    <div className="spinner" aria-hidden="true" />
    <span style={{ position: 'absolute', left: '-9999px' }}>Loading…</span>
  </div>
}

export function ErrorMessage({ message }) {
  return (
    <p role="alert" style={{ color: 'var(--danger)', padding: '1rem', textAlign: 'center' }}>
      [Warning] {message}
    </p>
  )
}

// ============================================================
// TICKET-F104 (Day 9) — Toast Notification
// ============================================================
// WHAT: A briefly-visible success/error message that auto-dismisses.
//       Positioned bottom-right via .toast in global.css (Day 1 style).
//
// USAGE:  const [toast, setToast] = useState(null)
//         ...
//         <Toast message={toast?.message} type={toast?.type}
//                onClose={() => setToast(null)} />
//         (component renders nothing while `message` is falsy)
export function Toast({ message, type = 'success', onClose }) {
  useEffect(() => {
    if (!message) return
    const t = setTimeout(onClose, 3000)
    return () => clearTimeout(t)
  }, [message, onClose])

  if (!message) return null
  return (
    <div role={type === 'error' ? 'alert' : 'status'}
         aria-live={type === 'error' ? 'assertive' : 'polite'}
         className={`toast toast--${type}`}>
      {message}
    </div>
  )
}

// ============================================================
// TICKET-F105 (Day 9) — Empty State
// ============================================================
// WHAT: A friendly "no data" panel with an optional call-to-action.
//       Used by TransactionList, SavingsGoals and Dashboard when the
//       backing list is empty (or filters exclude everything).
//
// PROPS:
//   title    — headline text
//   body     — paragraph / helper text
//   ctaLabel — label for the CTA button (optional)
//   ctaTo    — react-router path for the CTA (renders as <Link>)
//   onCta    — click handler if the CTA is a button, not a link
export function EmptyState({ title, body, ctaLabel, ctaTo, onCta }) {
  const boxStyle = {
    background:  'var(--card-bg)',
    padding:     '2.5rem 2rem',
    borderRadius:'var(--radius)',
    textAlign:   'center',
    boxShadow:   'var(--shadow)',
    maxWidth:    480,
    margin:      '2rem auto',
  }
  const btnStyle = {
    display: 'inline-block',
    marginTop: '1rem',
    background: 'var(--primary)',
    color: '#fff',
    padding: '0.55rem 1.2rem',
    borderRadius: 'var(--radius)',
    textDecoration: 'none',
    border: 'none',
    fontWeight: 600,
    cursor: 'pointer',
  }

  return (
    <div className="empty-state" style={boxStyle}>
      <h3 style={{ margin: 0, color: 'var(--primary)' }}>{title}</h3>
      {body && <p style={{ color: 'var(--text-muted)', marginTop: '.5rem' }}>{body}</p>}
      {ctaLabel && ctaTo && (
        <Link to={ctaTo} style={btnStyle}>{ctaLabel}</Link>
      )}
      {ctaLabel && !ctaTo && onCta && (
        <button type="button" onClick={onCta} style={btnStyle}>{ctaLabel}</button>
      )}
    </div>
  )
}

/** Yellow banner shown on pages still using mock data. */
export function TodoBanner({ ticket, task }) {
  return (
    <div className="todo-banner">
      [TODO] <strong>{ticket}:</strong> {task}
    </div>
  )
}
