import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useCategories } from '../hooks/useBudgetAPI'

// ============================================================
// TICKET-F088/F089 (Day 8, Sprint 7) — Add Transaction Form
// ============================================================
//
// WHAT: A form page that allows users to create new transactions.
//       Uses React "controlled components" — the form state lives in React,
//       not in the DOM. Every input change updates React state, and React
//       re-renders the input with the new value.
//
// WHY:  Controlled components give you full control over form data.
//       You can validate before submission, format values, and prevent
//       invalid characters — all in JavaScript, before hitting the server.
//
// KEY CONCEPTS:
//   Controlled input:  value={state} + onChange={updateState}
//   useState:          Stores form field values as React state
//   handleChange:      A single function that handles ALL input changes
//   handleSubmit:      Sends the form data to the API as JSON
//   e.preventDefault(): Stops the browser's default form submission (page reload)
//   useNavigate:       React Router's hook for programmatic navigation
//
// TICKET-F088: controlled form + inline validation.
// TICKET-F091: category dropdown driven by useCategories() (real API).
// TICKET-F089: async submit — POST /api/transactions, redirect on success.
// TICKET-F092: server errors surface as a red banner instead of a blank page.
// ============================================================

const TODAY = new Date().toISOString().substring(0, 10)

export default function AddTransactionForm() {
    const navigate   = useNavigate()
    const categories = useCategories()

    // F088 — one state object holds every field; handleChange uses [name]
    //        to update the right slot.
    const [form, setForm] = useState({
        categoryId:  '',
        amount:      '',
        txnDate:     TODAY,
        description: '',
    })
    const [errors,     setErrors]     = useState({})
    const [submitting, setSubmitting] = useState(false)
    const [apiError,   setApiError]   = useState(null)

    function handleChange(e) {
        const { name, value } = e.target
        setForm(prev => ({ ...prev, [name]: value }))
    }

    function validate() {
        const errs = {}
        if (!form.categoryId)                        errs.categoryId  = 'Choose a category'
        if (!form.amount || parseFloat(form.amount) <= 0)
            errs.amount      = 'Amount must be greater than 0'
        if (!form.txnDate)                           errs.txnDate     = 'Date is required'
        else if (form.txnDate > TODAY)               errs.txnDate     = 'Date cannot be in the future'
        if (!form.description.trim())                errs.description = 'Description is required'
        return errs
    }

    // F089 — async submit. On success we navigate to the list so the user
    //        immediately sees their new row. On failure we keep the form
    //        values intact and show the error.
    async function handleSubmit(e) {
        e.preventDefault()
        setApiError(null)

        const errs = validate()
        setErrors(errs)
        if (Object.keys(errs).length > 0) return

        // Look up the category so we can include its type in the payload.
        const selected = categories.find(c => String(c.categoryId) === String(form.categoryId))
        if (!selected) {
            setErrors({ categoryId: 'Category is invalid — pick another' })
            return
        }

        setSubmitting(true)
        try {
            const res = await fetch('/api/transactions', {
                method:  'POST',
                headers: { 'Content-Type': 'application/json' },
                body:    JSON.stringify({
                    user:        { userId: 1 },                              // hardcoded for foundation
                    category:    { categoryId: Number(form.categoryId) },
                    amount:      parseFloat(form.amount),
                    txnDate:     form.txnDate,
                    description: form.description.trim(),
                    type:        selected.type,                              // derived from category
                }),
            })

            if (!res.ok) {
                const body = await res.json().catch(() => null)
                throw new Error(body?.message || `HTTP ${res.status}`)
            }
            navigate('/transactions')
        } catch (err) {
            setApiError(err.message || 'Failed to save transaction')
        } finally {
            setSubmitting(false)
        }
    }

    return (
        <div>
            <h1 style={{ marginBottom: '1.5rem', color: 'var(--primary)' }}>Add Transaction</h1>

            <div className="card" style={{ maxWidth: 540 }}>
                {apiError && (
                    <div className="error-banner" role="alert" style={{
                        background: '#FADBD8', color: 'var(--danger)',
                        border: '1px solid #F1948A',
                        padding: '0.75rem 1rem', borderRadius: 'var(--radius)',
                        marginBottom: '1rem', fontSize: '0.9rem',
                    }}>
                        <strong>Could not save:</strong> {apiError}
                    </div>
                )}

                <form onSubmit={handleSubmit} noValidate>
                    <div className="form-group">
                        <label htmlFor="categoryId">Category</label>
                        <select id="categoryId" name="categoryId"
                                value={form.categoryId} onChange={handleChange}>
                            <option value="">— Select category —</option>
                            {categories.map(c => (
                                <option key={c.categoryId} value={c.categoryId}>
                                    {c.name} ({c.type})
                                </option>
                            ))}
                        </select>
                        {errors.categoryId && <FieldError text={errors.categoryId} />}
                    </div>

                    <div className="form-group">
                        <label htmlFor="amount">Amount (£)</label>
                        <input id="amount" name="amount" type="number"
                               step="0.01" min="0.01"
                               value={form.amount} onChange={handleChange} />
                        {errors.amount && <FieldError text={errors.amount} />}
                    </div>

                    <div className="form-group">
                        <label htmlFor="txnDate">Date</label>
                        <input id="txnDate" name="txnDate" type="date"
                               max={TODAY}
                               value={form.txnDate} onChange={handleChange} />
                        {errors.txnDate && <FieldError text={errors.txnDate} />}
                    </div>

                    <div className="form-group">
                        <label htmlFor="description">Description</label>
                        <input id="description" name="description" type="text"
                               maxLength={200}
                               value={form.description} onChange={handleChange} />
                        {errors.description && <FieldError text={errors.description} />}
                    </div>

                    <div style={{ display: 'flex', gap: '0.75rem', marginTop: '1rem' }}>
                        <button type="submit" className="btn btn-primary" disabled={submitting}>
                            {submitting ? 'Saving…' : 'Add Transaction'}
                        </button>
                        <Link to="/transactions" className="btn btn-secondary">Cancel</Link>
                    </div>
                </form>
            </div>
        </div>
    )
}

/** Small inline field-error message (red text under the input). */
function FieldError({ text }) {
    return (
        <span style={{ color: 'var(--danger)', fontSize: '0.8rem', marginTop: '0.15rem' }}>
      {text}
    </span>
    )
}