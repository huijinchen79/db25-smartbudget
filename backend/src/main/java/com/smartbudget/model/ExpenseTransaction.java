package com.smartbudget.model;

import java.math.BigDecimal;
import java.time.LocalDate;

// ============================================================
// TICKET-F023 (Day 3, Sprint 2) — Expense Transaction Subclass
// ============================================================
//
// WHAT: This is the sibling of IncomeTransaction. Both extend BaseTransaction.
//       While IncomeTransaction tracks "source" (where money came from),
//       ExpenseTransaction tracks "paymentMethod" (how money was spent).
//
// WHY:  Different transaction types carry different metadata.
//       Expenses need to know HOW payment was made (card, cash, bank transfer).
//       This demonstrates how inheritance lets siblings share a parent
//       while having their own unique data and behavior.
//
// ============================================================
public class ExpenseTransaction extends BaseTransaction {

    // -------------------------------------------------------
    // TODO TICKET-F023: Step 1 — Add a "paymentMethod" field
    // -------------------------------------------------------
    // WHAT: Records how the expense was paid: card, cash, or bank transfer.
    //
    // HOW:  Declare a private String field named "paymentMethod".
    //       Valid values: "CARD", "CASH", "BANK_TRANSFER"
    //
    //       OPTIONAL CHALLENGE: Use a Java enum instead of String for type safety.
    //       An enum restricts the field to only the values you define,
    //       preventing typos like "CRAD" or "csh".
    //       You would declare: public enum PaymentMethod { CARD, CASH, BANK_TRANSFER }
    //
    // WHY:  Knowing HOW expenses were paid helps users analyze spending habits.
    //       "Am I using my card too much? Should I use cash for small purchases?"

    // -------------------------------------------------------
    // TODO TICKET-F023: Step 2 — Constructor
    // -------------------------------------------------------
    // WHAT: Same pattern as IncomeTransaction — call super() first, then set the unique field.
    //
    // HOW:  Constructor accepts 5 parameters: txnId, amount, txnDate, description + paymentMethod.
    //       Call super(txnId, amount, txnDate, description) as the FIRST line.
    //       Then assign this.paymentMethod = paymentMethod.
    //
    // WHY:  The parent handles all shared validation. You only handle what's unique.
    //
    // OBSERVE: Same behavior as IncomeTransaction — invalid amounts are rejected by the parent.

    // -------------------------------------------------------
    // TODO TICKET-F023: Step 3 — Implement getType()
    // -------------------------------------------------------
    // WHAT: Overrides the abstract method to return "EXPENSE".
    //
    // HOW:  Same pattern as IncomeTransaction but return "EXPENSE" instead of "INCOME".
    //       Don't forget the @Override annotation.
    //
    // WHY:  This completes the polymorphism: calling getType() on any BaseTransaction
    //       automatically returns the correct type based on the actual object.

    // -------------------------------------------------------
    // TODO TICKET-F023: Step 4 — Getter/setter for paymentMethod + toString()
    // -------------------------------------------------------
    // HOW:  Add getPaymentMethod() and setPaymentMethod().
    //       Override toString() — call super.toString() and append paymentMethod.
    //
    // OBSERVE: Print an ExpenseTransaction — it should show all parent fields
    //          plus " | paymentMethod=CARD" at the end.
    private String category;        // e.g. "Food", "Transport"

    public ExpenseTransaction(int txnId, BigDecimal amount,
                              LocalDate txnDate, String description) {
        this(txnId, amount, txnDate, description, null);
    }

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

    @Override
    public String toString() {
        return super.toString() + (category != null ? " (" + category + ")" : "");
    }
}
