package com.smartbudget.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * TICKET-F014 — Transaction POJO (Day 2)
 *
 * Concrete plain Java class representing one row from the Day 1 `transactions` table.
 *
 * Money is stored as {@link BigDecimal} — never {@code double}. Floating point cannot
 * represent decimal money exactly ({@code 0.1 + 0.2 == 0.30000000000000004} in Java),
 * so any finance code must use {@code BigDecimal}.
 *
 * NOTE: Day 3 will introduce an abstract {@code BaseTransaction} hierarchy
 * ({@code IncomeTransaction}, {@code ExpenseTransaction}) that lives alongside this
 * concrete class — that OOP refactor is Day 3 material and does not touch this file.
 */
public class Transaction {

    private int txnId;
    private int userId;
    private int categoryId;
    private BigDecimal amount;
    private LocalDate txnDate;
    private String description;
    private String type;  // 'INCOME' or 'EXPENSE'

    public Transaction() { }

    public Transaction(int txnId, int userId, int categoryId,
                       BigDecimal amount, LocalDate txnDate,
                       String description, String type) {
        this.txnId       = txnId;
        this.userId      = userId;
        this.categoryId  = categoryId;
        setAmount(amount);   // run validation in the constructor too
        this.txnDate     = txnDate;
        this.description = description;
        this.type        = type;
    }

    public int getTxnId()                    { return txnId; }
    public void setTxnId(int id)             { this.txnId = id; }

    public int getUserId()                   { return userId; }
    public void setUserId(int id)            { this.userId = id; }

    public int getCategoryId()               { return categoryId; }
    public void setCategoryId(int id)        { this.categoryId = id; }

    public BigDecimal getAmount()            { return amount; }
    public void setAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "amount must be > 0, got: " + amount);
        }
        this.amount = amount;
    }

    public LocalDate getTxnDate()            { return txnDate; }
    public void setTxnDate(LocalDate d)      { this.txnDate = d; }

    public String getDescription()           { return description; }
    public void setDescription(String d)     { this.description = d; }

    public String getType()                  { return type; }
    public void setType(String type)         { this.type = type; }

    @Override
    public String toString() {
        return String.format(
            "Transaction[id=%d, user=%d, cat=%d, amount=%s, date=%s, type=%s, desc='%s']",
            txnId, userId, categoryId, amount, txnDate, type, description);
    }
}
