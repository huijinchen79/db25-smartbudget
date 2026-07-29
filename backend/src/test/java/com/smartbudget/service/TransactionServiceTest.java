package com.smartbudget.service;

import com.smartbudget.exception.InvalidTransactionException;
import com.smartbudget.model.BaseTransaction;
import com.smartbudget.model.ExpenseTransaction;
import com.smartbudget.model.IncomeTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// ============================================================
// TICKET-F040 to F043 (Day 4, Sprint 3)
// Unit Tests with JUnit 5
// ============================================================
//
// WHAT:
// Unit tests verify that TransactionService works correctly
// without requiring a real database.
//
// WHY:
// A fresh service instance before every test prevents state
// from leaking between tests.
//
// TEST PATTERN:
// Arrange -> Act -> Assert
//
// NOTE:
// Mockito is not required yet because the current
// TransactionService does not depend on repositories.
// Mockito can be added later when repository dependencies
// are introduced.
// ============================================================

class TransactionServiceTest {

    private TransactionService svc;
    private IncomeTransaction income;
    private ExpenseTransaction expense;

    // -------------------------------------------------------
    // TICKET-F040: Test setup
    // -------------------------------------------------------
    //
    // @BeforeEach runs before every test method.
    // It creates a fresh service and fresh test fixtures.
    //
    @BeforeEach
    void setUp() {
        svc = new TransactionService();

        income = new IncomeTransaction(
                1,
                new BigDecimal("3500"),
                LocalDate.of(2026, 1, 1),
                "Salary"
        );

        expense = new ExpenseTransaction(
                2,
                new BigDecimal("45"),
                LocalDate.of(2026, 1, 5),
                "Groceries",
                "Food"
        );
    }

    // -------------------------------------------------------
    // TICKET-F040: Initial service state
    // -------------------------------------------------------

    @Test
    void initiallyEmpty() {
        assertEquals(0, svc.size());
    }

    // -------------------------------------------------------
    // TICKET-F040: Add one transaction
    // -------------------------------------------------------

    @Test
    void addTransaction_singleItem_isReturnedByGetAll() {
        // Arrange
        IncomeTransaction transaction = new IncomeTransaction(
                3,
                new BigDecimal("100"),
                LocalDate.now(),
                "Test"
        );

        // Act
        svc.addTransaction(transaction);

        // Assert
        List<BaseTransaction> all = svc.getAll();

        assertEquals(
                1,
                all.size(),
                "should contain exactly one transaction"
        );

        assertEquals(3, all.get(0).getTxnId());
        assertEquals(
                new BigDecimal("100"),
                all.get(0).getAmount()
        );
        assertEquals("INCOME", all.get(0).getType());
    }

    // -------------------------------------------------------
    // TICKET-F040: Add multiple transactions
    // -------------------------------------------------------

    @Test
    void addTransaction_multipleItems_allReturned() {
        // Act
        svc.addTransaction(income);
        svc.addTransaction(expense);

        // Assert
        List<BaseTransaction> all = svc.getAll();

        assertEquals(2, all.size());
    }

    // -------------------------------------------------------
    // TICKET-F041: Defensive copy
    // -------------------------------------------------------

    @Test
    void getAll_returnsDefensiveCopy() {
        // Arrange
        svc.addTransaction(income);

        // Act
        List<BaseTransaction> returnedList = svc.getAll();
        returnedList.clear();

        // Assert
        assertEquals(
                1,
                svc.size(),
                "getAll() must return a defensive copy"
        );
    }

    // -------------------------------------------------------
    // TICKET-F042: Delete existing transaction
    // -------------------------------------------------------

    @Test
    void delete_existingItem_removesIt() {
        // Arrange
        svc.addTransaction(income);
        String id = String.valueOf(income.getTxnId());

        assertEquals(1, svc.size());

        // Act
        boolean removed = svc.delete(id);

        // Assert
        assertTrue(
                removed,
                "delete should return true when item existed"
        );

        assertEquals(0, svc.size());
        assertNull(svc.findById(id));
    }

    // -------------------------------------------------------
    // TICKET-F042: Delete missing transaction
    // -------------------------------------------------------

    @Test
    void delete_missingItem_returnsFalseAndChangesNothing() {
        // Arrange
        svc.addTransaction(income);

        // Act
        boolean removed = svc.delete("999");

        // Assert
        assertFalse(removed);

        assertEquals(
                1,
                svc.size(),
                "delete of missing id must not affect state"
        );
    }

    // -------------------------------------------------------
    // TICKET-F043: Reject negative amount
    // -------------------------------------------------------

    @Test
    void negativeAmount_throwsInvalidTransactionException() {
        InvalidTransactionException exception = assertThrows(
                InvalidTransactionException.class,
                () -> new IncomeTransaction(
                        4,
                        new BigDecimal("-10"),
                        LocalDate.now(),
                        "Bad transaction"
                )
        );

        String message = exception.getMessage().toLowerCase();

        assertTrue(
                message.contains("amount")
                        || message.contains("greater than zero"),
                "Message should mention amount or greater than zero"
        );
    }

    // -------------------------------------------------------
    // TICKET-F043: Reject zero amount
    // -------------------------------------------------------

    @Test
    void zeroAmount_throwsInvalidTransactionException() {
        assertThrows(
                InvalidTransactionException.class,
                () -> new ExpenseTransaction(
                        5,
                        BigDecimal.ZERO,
                        LocalDate.now(),
                        "Zero amount",
                        "Food"
                )
        );
    }

    // -------------------------------------------------------
    // TICKET-F043: Reject future date
    // -------------------------------------------------------

    @Test
    void futureDate_throwsInvalidTransactionException() {
        InvalidTransactionException exception = assertThrows(
                InvalidTransactionException.class,
                () -> new IncomeTransaction(
                        6,
                        new BigDecimal("100"),
                        LocalDate.now().plusDays(1),
                        "Future transaction"
                )
        );

        assertTrue(
                exception.getMessage()
                        .toLowerCase()
                        .contains("date")
        );
    }

    // -------------------------------------------------------
    // TICKET-F043: Reject null transaction
    // -------------------------------------------------------

    @Test
    void addNullTransaction_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> svc.addTransaction(null)
        );
    }
}