import { NavLink } from 'react-router-dom'
import { useTransactionData } from '../hooks/useBudgetAPI'

// ============================================================
// PROVIDED — working navigation bar with active link highlighting
// TICKET-F099 (Day 9): live transaction-count badge on the Transactions link.
// ============================================================
export default function Navbar() {
  // Duplicate hook call is deliberate for foundation-level state sharing.
  // A production app would hoist txns into a context to fetch once.
  const { transactions = [] } = useTransactionData()

  const links = [
    ['/',             'Dashboard',    true],
    ['/transactions', 'Transactions'],
    ['/add',          'Add'],
    ['/savings',      'Goals'],
  ]

  return (
    <header style={{
      background: 'var(--primary)', color: '#fff',
      display: 'flex', alignItems: 'center', justifyContent: 'space-between',
      padding: '0.75rem 2rem', boxShadow: '0 2px 6px rgba(0,0,0,0.2)'
    }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
        <span style={{ fontSize: '1.2rem', fontWeight: 700 }}>SB</span>
        <span style={{ fontSize: '1.2rem', fontWeight: 700 }}>SmartBudget</span>
        <span style={{ fontSize: '0.72rem', opacity: 0.7 }}>Deutsche Bank TDI 2026</span>
      </div>
      <nav style={{ display: 'flex', gap: '0.5rem' }} aria-label="Primary">
        {links.map(([to, label, end]) => (
          <NavLink
            key={to}
            to={to}
            end={!!end}
            style={({ isActive }) => ({
              color: isActive ? '#fff' : 'rgba(255,255,255,0.75)',
              textDecoration: 'none',
              padding: '0.45rem 1rem',
              borderRadius: 'var(--radius)',
              fontWeight: 500,
              fontSize: '0.9rem',
              background: isActive ? 'rgba(255,255,255,0.18)' : 'transparent',
              display: 'inline-flex',
              alignItems: 'center',
              gap: '0.4rem',
            })}
          >
            {label}
            {to === '/transactions' && transactions.length > 0 && (
              <span
                aria-label={`${transactions.length} transactions`}
                style={{
                  background: 'var(--gold)',
                  color: '#000',
                  fontSize: '0.72rem',
                  fontWeight: 700,
                  padding: '0.05rem 0.45rem',
                  borderRadius: 999,
                  lineHeight: 1.4,
                }}
              >
                {transactions.length}
              </span>
            )}
          </NavLink>
        ))}
      </nav>
    </header>
  )
}
