import { NavLink } from 'react-router-dom'

/**
 * PROVIDED – working navigation bar with active link highlighting.
 *
 * TICKET-F084: uses NavLink so the current route's link is visually
 * distinguished — the `isActive` bool comes from React Router for free.
 * `end` on the "/" link stops it matching every URL (otherwise
 * Dashboard would look active on /add, /savings, etc.).
 */
export default function Navbar() {
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
      <nav style={{ display: 'flex', gap: '0.5rem' }}>
        {[['/', 'Dashboard', true], ['/transactions', 'Transactions'], ['/add', 'Add'], ['/savings', 'Goals']].map(([to, label, end]) => (
          <NavLink key={to} to={to} end={!!end} style={({ isActive }) => ({
            color: isActive ? '#fff' : 'rgba(255,255,255,0.75)',
            textDecoration: 'none', padding: '0.45rem 1rem',
            borderRadius: 'var(--radius)', fontWeight: 500, fontSize: '0.9rem',
            background: isActive ? 'rgba(255,255,255,0.18)' : 'transparent'
          })}>
            {label}
          </NavLink>
        ))}
      </nav>
    </header>
  )
}
