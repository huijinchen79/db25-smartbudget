package com.smartbudget.model;

import com.smartbudget.exception.InvalidTransactionException;

import java.math.BigDecimal;
import java.time.LocalDate;

// ============================================================
// TICKET-F021 (Day 3, Sprint 2) — Abstract Base Class for Transactions  [SOLVED]
// ============================================================
//
// WHAT: An abstract class is a class you CANNOT create objects from directly.
//       It serves as a blueprint for child classes (IncomeTransaction, ExpenseTransaction).
//       The "abstract" keyword on a method means: "every child MUST provide its own version."
//       This is a core OOP concept called POLYMORPHISM.
//
// WHY:  Income and Expense transactions share common fields (amount, date, description)
//       but differ in behavior (getType returns different values).
//       Instead of duplicating fields in both classes, we put shared logic HERE
//       and let each child add its own unique behavior.
//       This follows the DRY principle — Don't Repeat Yourself.
//
// ============================================================
public abstract class BaseTransaction {

    // -------------------------------------------------------
    // Step 1 — Fields (protected so subclasses can read directly)
    // -------------------------------------------------------
    protected int txnId;
    protected BigDecimal amount;
    protected LocalDate txnDate;
    protected String description;

    // -------------------------------------------------------
    // Step 2 — Constructor with validation
    // -------------------------------------------------------
    // Validating here means: an invalid BaseTransaction can never exist in memory.
    // Any bad input aborts construction with InvalidTransactionException.
    public BaseTransaction(int txnId, BigDecimal amount,
                           LocalDate txnDate, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException(
                "Amount must be greater than zero, got: " + amount);
        }
        if (txnDate == null || txnDate.isAfter(LocalDate.now())) {
            throw new InvalidTransactionException(
                "Transaction date cannot be in the future: " + txnDate);
        }
        this.txnId       = txnId;
        this.amount      = amount;
        this.txnDate     = txnDate;
        this.description = description;
    }

    // -------------------------------------------------------
    // Step 3 — Abstract method: every subclass MUST implement getType()
    // -------------------------------------------------------
    // No body, just a signature terminated with `;`. IncomeTransaction returns
    // "INCOME", ExpenseTransaction returns "EXPENSE". That's polymorphism.
    public abstract String getType();

    // -------------------------------------------------------
    // Step 4 — Getters
    // -------------------------------------------------------
    public int getTxnId()          { return txnId; }
    public BigDecimal getAmount()  { return amount; }
    public LocalDate getTxnDate()  { return txnDate; }
    public String getDescription() { return description; }

    // -------------------------------------------------------
    // Step 5 — toString(): delegates to getType() so subclass override wins
    // -------------------------------------------------------
    @Override
    public String toString() {
        return String.format("[%s] id=%d | %s | %s | %s",
                getType(), txnId, amount, txnDate, description);
    }
}
