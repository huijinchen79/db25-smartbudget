package com.smartbudget.model;

import java.math.BigDecimal;
import java.time.LocalDate;

// ============================================================
// TICKET-F023 (Day 3, Sprint 2) — Expense Transaction Subclass  [SOLVED]
// ============================================================
//
// WHAT: Sibling of IncomeTransaction. Both extend BaseTransaction and inherit
//       the same four fields + validation. This subclass adds ONE optional
//       piece of state: `category` (e.g., "Food", "Transport", "Utilities").
//
// WHY:  Mirroring IncomeTransaction proves the parent contract is symmetrical.
//       The optional `category` field also shows subclasses CAN add their own
//       state on top of the inherited shape — inheritance is additive.
//
// ============================================================
public class ExpenseTransaction extends BaseTransaction {

    /** e.g. "Food", "Transport", "Utilities" — optional, may be null. */
    private String category;

    /**
     * Convenience 4-arg constructor. Passes null for category — used by the
     * CSV importer (TICKET-F030) which doesn't carry the category column.
     */
    public ExpenseTransaction(int txnId, BigDecimal amount,
                              LocalDate txnDate, String description) {
        this(txnId, amount, txnDate, description, null);
    }

    /**
     * Full 5-arg constructor. `super(...)` runs BaseTransaction's validation
     * before assigning our own category field.
     */
    public ExpenseTransaction(int txnId, BigDecimal amount,
                              LocalDate txnDate, String description,
                              String category) {
        super(txnId, amount, txnDate, description);
        this.category = category;
    }

    @Override
    public String getType() {
        return "EXPENSE";
    }

    public String getCategory()              { return category; }
    public void setCategory(String category) { this.category = category; }

    /**
     * Extends the parent's toString() output by appending the category in
     * parentheses when present — e.g. "[EXPENSE] id=2 | 45 | 2026-01-08 | Groceries (Food)".
     */
    @Override
    public String toString() {
        return super.toString() + (category != null ? " (" + category + ")" : "");
    }
}
