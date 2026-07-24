import { BrowserRouter, Routes, Route, Link } from 'react-router-dom'
import Navbar            from './components/Navbar'
import Dashboard         from './pages/Dashboard'
import TransactionList   from './pages/TransactionList'
import AddTransactionForm from './pages/AddTransactionForm'
import SavingsGoals      from './pages/SavingsGoals'

/**
 * PROVIDED – fully working router with 4 routes.
 *
 * TICKET-F083 (Day 8): Your task is NOT to rewrite this.
 * Your task is to make each page fetch REAL data from the API
 * by implementing the custom hooks in hooks/useBudgetAPI.js
 */

// Catch-all 404 route so unknown URLs render a friendly page
// instead of a blank <Routes>.
function NotFound() {
  return (
    <div style={{ padding: '2rem', textAlign: 'center' }}>
      <h1 style={{ color: 'var(--primary)' }}>404 — Page Not Found</h1>
      <p style={{ color: 'var(--text-muted)' }}>
        The page you’re looking for does not exist.
      </p>
      <Link to="/" className="btn btn-primary">← Back to Dashboard</Link>
    </div>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <Navbar />
      <main className="main-content">
        <Routes>
          <Route path="/"             element={<Dashboard />} />
          <Route path="/transactions" element={<TransactionList />} />
          <Route path="/add"          element={<AddTransactionForm />} />
          <Route path="/savings"      element={<SavingsGoals />} />
          <Route path="*"             element={<NotFound />} />
        </Routes>
      </main>
    </BrowserRouter>
  )
}
