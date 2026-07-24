// ============================================================
// TICKET-F106 (Day 9, Sprint 8) — Currency & date formatting helpers
// ============================================================
//
// WHAT: One place that decides how money and dates render across the app.
//       Built on the browser-native Intl.NumberFormat / Intl.DateTimeFormat,
//       both of which handle locale rules (thousands separators, currency
//       symbol placement) far better than a hand-rolled `"£" + n.toFixed(2)`.
//
// WHY:  Consistency. Every page previously rendered amounts a little
//       differently — some had two decimals, some had none, some prepended
//       "£", some didn't. One helper = one visual convention everywhere,
//       and a future currency change is a one-file edit.
// ============================================================

const GBP = new Intl.NumberFormat('en-GB', {
  style:                'currency',
  currency:             'GBP',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
})

/**
 * Format a numeric amount as GBP.
 *   formatCurrency(3500)      -> "£3,500.00"
 *   formatCurrency(null)      -> "£0.00"
 *   formatCurrency('12.5')    -> "£12.50"
 *   formatCurrency(NaN)       -> "£0.00"
 */
export function formatCurrency(amount) {
  const n = Number(amount)
  if (!Number.isFinite(n)) return GBP.format(0)
  return GBP.format(n)
}

/**
 * Signed variant — always prefixes a "+" for non-negative values.
 *   formatSignedCurrency(50)  -> "+£50.00"
 *   formatSignedCurrency(-50) -> "-£50.00"
 */
export function formatSignedCurrency(amount) {
  const n = Number(amount)
  if (!Number.isFinite(n)) return GBP.format(0)
  return (n >= 0 ? '+' : '') + GBP.format(n)
}

const DATE_FMT = new Intl.DateTimeFormat('en-GB', {
  day:   '2-digit',
  month: 'short',
  year:  'numeric',
})

/**
 * Format an ISO date string ("2026-05-01") as "01 May 2026".
 * Returns empty string on null/undefined/invalid input.
 */
export function formatDate(iso) {
  if (!iso) return ''
  const d = new Date(iso)
  if (isNaN(d.getTime())) return ''
  return DATE_FMT.format(d)
}
