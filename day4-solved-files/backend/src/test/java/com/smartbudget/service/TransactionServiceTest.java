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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// ============================================================
// TICKET-F040 to F043 (Day 4, Sprint 3) — Unit Tests with JUnit 5   [SOLVED]
// ============================================================
//
// WHAT: Unit tests verify that your code works CORRECTLY.
//       These tests exercise the Day-4 in-memory TransactionService
//       (HashMap storage + Streams + Lambdas). No database, no network,
//       no Spring — just plain JUnit 5 assertions.
//
//       Later on Day 6, when TransactionService is refactored into a
//       Spring @Service that talks to a JPA repository, this class will
//       be swapped for the @Mock / @InjectMocks / MockitoExtension flavour
//       — the same test *concepts*, just wiring collaborators through
//       Mockito instead of newing up the SUT directly.
//
// WHY:  Without tests, you only know your code works when you manually click
//       through the app. Tests automate this — they run in seconds and catch
//       bugs BEFORE they reach production. In professional development,
//       you write tests alongside (or even BEFORE) the code.
//
// KEY CONCEPTS:
//   @BeforeEach → runs before EVERY test method (fresh SUT each time)
//   assertEquals(expected, actual) → "These two values must be equal"
//   assertThrows(Exception.class, () -> ...) → "This code must throw"
//
// TEST PATTERN: Arrange → Act → Assert
// ============================================================
class TransactionServiceTest {

    // -------------------------------------------------------
    // TICKET-F040: Test scaffolding — fresh SUT and fixtures per test
    // -------------------------------------------------------
    // Re-creating `svc` (and the two fixtures) in @BeforeEach guarantees
    // no state leaks between tests. If test A adds a transaction, test B
    // must NOT still see it — each test starts from a clean slate.
    private TransactionService svc;
    private IncomeTransaction  income;
    private ExpenseTransaction expense;

    @BeforeEach
    void setUp() {
        svc = new TransactionService();
        income  = new IncomeTransaction (1,
                new BigDecimal("3500"),
                LocalDate.of(2026, 1, 1),
                "Salary");
        expense = new ExpenseTransaction(2,
                new BigDecimal("45"),
                LocalDate.of(2026, 1, 5),
                "Groceries");
    }

    // -------------------------------------------------------
    // Sanity check — the setUp() itself works
    // -------------------------------------------------------
    @Test
    void initiallyEmpty() {
        assertEquals(0, svc.size());
        assertTrue(svc.getAll().isEmpty());
    }

    // ==========================================================
    //  TICKET-F041: Test — add and get
    // ==========================================================

    @Test
    void addTransaction_singleItem_isReturnedByGetAll() {
        // Arrange
        IncomeTransaction t = new IncomeTransaction(
                1, new BigDecimal("100"), LocalDate.now(), "Test");

        // Act
        svc.addTransaction(t);
        List<BaseTransaction> all = svc.getAll();

        // Assert
        assertEquals(1, all.size(), "should contain exactly one transaction");
        assertEquals(1,             all.get(0).getTxnId());
        assertEquals(new BigDecimal("100"), all.get(0).getAmount());
        assertEquals("INCOME",      all.get(0).getType());
    }

    @Test
    void addTransaction_multipleItems_allReturned() {
        // Arrange + Act
        svc.addTransaction(income);
        svc.addTransaction(expense);

        // Assert
        assertEquals(2, svc.getAll().size());
        assertNotNull(svc.findById("1"), "findById must locate the income by txnId");
        assertNotNull(svc.findById("2"), "findById must locate the expense by txnId");
    }

    @Test
    void getAll_returnsDefensiveCopy() {
        // Arrange
        svc.addTransaction(income);

        // Act — mutate the returned collection
        svc.getAll().clear();

        // Assert — internal state is untouched
        assertEquals(1, svc.size(), "getAll() must return a defensive copy");
    }

    // ==========================================================
    //  TICKET-F042: Test — delete
    // ==========================================================

    @Test
    void delete_existingItem_removesIt() {
        // Arrange
        svc.addTransaction(income);
        assertEquals(1, svc.size());

        // Act
        boolean removed = svc.delete(String.valueOf(income.getTxnId()));

        // Assert
        assertTrue(removed, "delete should return true when the item existed");
        assertEquals(0, svc.size());
        assertNull(svc.findById(String.valueOf(income.getTxnId())),
                "deleted item must no longer be findable");
    }

    @Test
    void delete_missingItem_returnsFalseAndChangesNothing() {
        // Arrange
        svc.addTransaction(income);

        // Act
        boolean removed = svc.delete("999");

        // Assert — the contract: missing id is not an error, just returns false
        assertFalse(removed);
        assertEquals(1, svc.size(),
                "deleting a missing id must NOT affect existing state");
    }

    // ==========================================================
    //  TICKET-F043: Test — invalid amount (negative test)
    // ==========================================================
    //
    // Uses assertThrows to prove the constructor's validation contract:
    // -10 (or 0) is REJECTED before any object exists. Without this test,
    // a future refactor could silently accept bad data and no one would
    // notice until production books were corrupt.

    @Test
    void negativeAmount_throwsInvalidTransactionException() {
        InvalidTransactionException ex = assertThrows(
                InvalidTransactionException.class,
                () -> new IncomeTransaction(
                        1, new BigDecimal("-10"), LocalDate.now(), "bad"));

        String msg = ex.getMessage().toLowerCase();
        assertTrue(msg.contains("amount") || msg.contains("greater than zero"),
                "message should mention 'amount' or 'greater than zero', got: "
                        + ex.getMessage());
    }

    @Test
    void zeroAmount_throwsInvalidTransactionException() {
        assertThrows(InvalidTransactionException.class,
                () -> new ExpenseTransaction(
                        2, BigDecimal.ZERO, LocalDate.now(), "zero"));
    }

    @Test
    void futureDate_throwsInvalidTransactionException() {
        InvalidTransactionException ex = assertThrows(
                InvalidTransactionException.class,
                () -> new IncomeTransaction(
                        3, new BigDecimal("100"),
                        LocalDate.now().plusDays(1), "future"));

        assertTrue(ex.getMessage().toLowerCase().contains("date"),
                "message should mention 'date': " + ex.getMessage());
    }

    @Test
    void addNullTransaction_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> svc.addTransaction(null));
    }
}
