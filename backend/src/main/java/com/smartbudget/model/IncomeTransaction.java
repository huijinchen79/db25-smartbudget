package com.smartbudget.model;

import java.math.BigDecimal;
import java.time.LocalDate;

// ============================================================
// TICKET-F022 (Day 3, Sprint 2) — Income Transaction Subclass  [SOLVED]
// ============================================================
//
// WHAT: This class EXTENDS BaseTransaction, which means it inherits all of
//       BaseTransaction's fields (txnId, amount, txnDate, description) and methods.
//       "extends" creates a parent-child (IS-A) relationship:
//       "An IncomeTransaction IS-A BaseTransaction."
//
// WHY:  Every income row shares the same four fields with expenses; the ONLY
//       difference is what getType() returns. By pushing fields + validation up
//       to the parent, this class stays tiny — one constructor and one override.
//
// ============================================================
public class IncomeTransaction extends BaseTransaction {

    /**
     * Delegates every field to the parent constructor, which runs validation
     * BEFORE any state on this object is set. Reject → no object exists.
     */
    public IncomeTransaction(int txnId, BigDecimal amount,
                             LocalDate txnDate, String description) {
        super(txnId, amount, txnDate, description);
    }

    /**
     * Implements the abstract method from BaseTransaction.
     * The @Override annotation makes the compiler flag any signature drift.
     */
    @Override
    public String getType() {
        return "INCOME";
    }
}
