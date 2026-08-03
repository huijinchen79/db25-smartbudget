import { useState, useEffect, useCallback } from 'react'

// ============================================================
// TICKET-F091 (Day 8, Sprint 7) — Custom React Hooks for API Calls  [SOLVED]
// ============================================================
//
// WHAT: Custom hooks are reusable functions that encapsulate React logic.
//       Each hook below handles a specific API call and manages three states:
//         - data (the fetched result — transactions, goals, or categories)
//         - loading (true while the fetch is in progress)
//         - error (an error message if the fetch failed)
//       Custom hooks MUST start with "use" — this is a React naming convention.
//       React treats functions starting with "use" specially.
//
// WHY:  Without custom hooks, every page component would repeat the same
//       fetch + loading + error logic. That violates DRY (Don't Repeat Yourself).
//       With hooks, each page simply calls: const { data, loading, error } = useMyHook()
//       and the hook handles everything internally.
//
// DAY-9 NOTE:
//   `refetch` is exported from each hook — Day-9 features (edit, contribute,
//   delete-with-toast) all mutate server state and need to refresh the
//   local view without a page reload. Calling `refetch()` re-runs the fetch
//   effect and updates the returned data/loading/error triple.
//
// ============================================================

// -------------------------------------------------------
// TICKET-F091 — useTransactionData()
// -------------------------------------------------------
// Fetches GET /api/transactions. Returns { transactions, loading, error, refetch }.
export function useTransactionData() {
  const [transactions, setTransactions] = useState([])
  const [loading,      setLoading]      = useState(true)
  const [error,        setError]        = useState(null)

  const fetchData = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const res = await fetch('/api/transactions')
      if (!res.ok) throw new Error(`HTTP ${res.status}`)
      const json = await res.json()
      setTransactions(Array.isArray(json) ? json : [])
    } catch (err) {
      setError(err.message || 'Failed to load transactions')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { fetchData() }, [fetchData])

  return { transactions, loading, error, refetch: fetchData }
}

// -------------------------------------------------------
// TICKET-F091 — useSavingsGoals(userId)
// -------------------------------------------------------
// Fetches GET /api/goals/user/{userId}. Returns { goals, loading, error, refetch }.
export function useSavingsGoals(userId) {
  const [goals,   setGoals]   = useState([])
  const [loading, setLoading] = useState(true)
  const [error,   setError]   = useState(null)

  const fetchData = useCallback(async () => {
    if (userId == null) return
    setLoading(true)
    setError(null)
    try {
      const res = await fetch(`/api/goals/user/${userId}`)
      if (!res.ok) throw new Error(`HTTP ${res.status}`)
      const json = await res.json()
      setGoals(Array.isArray(json) ? json : [])
    } catch (err) {
      setError(err.message || 'Failed to load savings goals')
    } finally {
      setLoading(false)
    }
  }, [userId])

  useEffect(() => { fetchData() }, [fetchData])

  return { goals, loading, error, refetch: fetchData }
}

// -------------------------------------------------------
// TICKET-F091 — useCategories()
// -------------------------------------------------------
// Fetches GET /api/categories. Simpler shape: returns just the array.
export function useCategories() {
  const [categories, setCategories] = useState([])

  useEffect(() => {
    let cancelled = false
    fetch('/api/categories')
      .then(res => res.ok ? res.json() : [])
      .then(json => { if (!cancelled) setCategories(Array.isArray(json) ? json : []) })
      .catch(() => { if (!cancelled) setCategories([]) })
    return () => { cancelled = true }
  }, [])

  return categories
}
