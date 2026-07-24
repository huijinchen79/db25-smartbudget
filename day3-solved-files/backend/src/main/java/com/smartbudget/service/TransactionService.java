package com.smartbudget.service;

import com.smartbudget.exception.InvalidTransactionException;
import com.smartbudget.model.BaseTransaction;
import com.smartbudget.model.ExpenseTransaction;
import com.smartbudget.model.IncomeTransaction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// ============================================================
// TransactionService — evolves across THREE days:
//
// Day 3 (Sprint 2) — TICKET-F026 to F031: Plain Java service with List   [SOLVED HERE]
// Day 4 (Sprint 3) — TICKET-F032 to F034: Refactor with HashMap + Streams + Lambdas
// Day 6 (Sprint 5) — TICKET-F063:         Spring @Service using JPA repositories
//
// Each day BUILDS on the previous — don't delete old code, evolve it.
// ============================================================
public class TransactionService {

    // ==========================================================
    //  DAY 3 (Sprint 2): Plain Java with ArrayList  — IMPLEMENTED
    // ==========================================================

    // -------------------------------------------------------
    // TICKET-F026: Step 1 — Storage field
    // -------------------------------------------------------
    // In-memory storage for now. On Day 4 this will be swapped for a
    // Map<String, BaseTransaction>; on Day 6 for a JPA repository.
    // `final` protects the reference from being reassigned by mistake.
    private final List<BaseTransaction> transactions = new ArrayList<>();

    // -------------------------------------------------------
    // TICKET-F026: Step 2 — addTransaction() and getAll()
    //   (addTransaction is upgraded by F031 below to add validation.)
    // -------------------------------------------------------

    /**
     * TICKET-F031: Validate on Add.
     *
     * BaseTransaction's constructor already rejects bad amounts and future
     * dates — that's model-layer defence. This service-layer guard catches
     * things the model doesn't care about but the UI does: a null argument
     * or a whitespace-only description ("   ").
     *
     * Defence-in-depth: model validates SHAPE, service validates BUSINESS.
     */
    public void addTransaction(BaseTransaction t) {
        if (t == null) {
            throw new IllegalArgumentException("transaction must not be null");
        }
        if (t.getDescription() == null || t.getDescription().isBlank()) {
            throw new InvalidTransactionException(
                "description must not be blank for transaction id=" + t.getTxnId());
        }
        transactions.add(t);
    }

    /** Returns a defensive copy — callers mutating the list can't corrupt our state. */
    public List<BaseTransaction> getAll() {
        return new ArrayList<>(transactions);
    }

    /** Read-only alternative — fails fast on any attempted mutation. */
    public List<BaseTransaction> getAllUnmodifiable() {
        return Collections.unmodifiableList(transactions);
    }

    /** Convenience for tests / smoke checks. */
    public int size() {
        return transactions.size();
    }

    // -------------------------------------------------------
    // TICKET-F027: filterByDateRange(from, to) — inclusive on both ends
    // -------------------------------------------------------
    // The two-negatives trick (`!isBefore && !isAfter`) is how you express
    // an inclusive "between" using LocalDate — the JDK has no built-in method.
    public List<BaseTransaction> filterByDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from and to must be non-null");
        }
        if (from.isAfter(to)) {
            return new ArrayList<>();   // reversed range → empty, NOT an error
        }
        List<BaseTransaction> result = new ArrayList<>();
        for (BaseTransaction t : transactions) {
            LocalDate d = t.getTxnDate();
            if (!d.isBefore(from) && !d.isAfter(to)) {
                result.add(t);
            }
        }
        return result;
    }

    // -------------------------------------------------------
    // TICKET-F028: calculateTotalByType(type)  — BigDecimal, not double
    // -------------------------------------------------------
    // BigDecimal is IMMUTABLE — total.add(x) returns a NEW object and does
    // NOT modify `total` in place. That's why we reassign every iteration.
    // Using double here would produce classics like 0.1 + 0.2 = 0.30000000000000004.
    public BigDecimal calculateTotalByType(String type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        BigDecimal total = BigDecimal.ZERO;
        for (BaseTransaction t : transactions) {
            if (type.equals(t.getType())) {
                total = total.add(t.getAmount());
            }
        }
        return total;
    }

    // -------------------------------------------------------
    // TICKET-F029: exportToCSV(filePath)
    // -------------------------------------------------------
    // try-with-resources guarantees the writer is closed even if the loop
    // body throws mid-write. IOException is re-thrown to the caller so
    // they can decide whether to log, retry, or surface it to the user.
    public void exportToCSV(String filePath) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            // Header
            bw.write("id,type,amount,date,description");
            bw.newLine();

            // Data rows
            for (BaseTransaction t : transactions) {
                bw.write(String.join(",",
                        String.valueOf(t.getTxnId()),
                        t.getType(),
                        t.getAmount().toPlainString(),   // avoid scientific notation
                        t.getTxnDate().toString(),       // ISO-8601: 2026-01-08
                        csvEscape(t.getDescription())
                ));
                bw.newLine();
            }
        }
    }

    /**
     * RFC-4180-lite escaping: wrap in double quotes when the field contains a
     * comma, quote, or newline; double-up any embedded quotes.
     */
    private static String csvEscape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    // -------------------------------------------------------
    // TICKET-F030: importFromCSV(filePath)
    // -------------------------------------------------------
    // Inverse of exportToCSV — proves the model round-trips through the file
    // format losslessly. A malformed row is skipped with a stderr note
    // rather than aborting the whole import.
    public void importFromCSV(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine();       // header
            if (line == null) return;          // empty file → nothing to do

            int lineNum = 1;
            while ((line = br.readLine()) != null) {
                lineNum++;
                if (line.isBlank()) continue;
                try {
                    String[] parts = line.split(",", -1);   // -1 keeps trailing empties
                    int id            = Integer.parseInt(parts[0].trim());
                    String type       = parts[1].trim();
                    BigDecimal amount = new BigDecimal(parts[2].trim());
                    LocalDate date    = LocalDate.parse(parts[3].trim());
                    String desc       = parts.length > 4 ? parts[4] : "";

                    BaseTransaction t = switch (type) {
                        case "INCOME"  -> new IncomeTransaction (id, amount, date, desc);
                        case "EXPENSE" -> new ExpenseTransaction(id, amount, date, desc);
                        default -> throw new InvalidTransactionException(
                                "Unknown type on line " + lineNum + ": " + type);
                    };
                    transactions.add(t);
                } catch (NumberFormatException | DateTimeParseException e) {
                    System.err.println("Skipping bad row at line "
                            + lineNum + ": " + e.getMessage());
                }
            }
        }
    }


    // ==========================================================
    //  DAY 4 (Sprint 3): Refactor with HashMap + Streams + Lambdas
    //  ---- NOT YET IMPLEMENTED — see Day 4 README ----
    // ==========================================================

    // -------------------------------------------------------
    // Day 4 (TICKET-F032): Refactor storage from List → HashMap
    // -------------------------------------------------------
    // WHAT: A HashMap stores key-value pairs. Unlike a List (which uses index 0, 1, 2...),
    //       a HashMap uses meaningful keys (like transaction ID as String).
    //       Lookup by key is O(1) — instant, regardless of size.
    //
    // HOW:  Replace your List<BaseTransaction> field with Map<String, BaseTransaction>.
    //       Update addTransaction: use map.put(String.valueOf(t.getTxnId()), t)
    //       Update getAll: return new ArrayList<>(map.values())
    //
    // WHY:  HashMap enables O(1) lookups by ID. With a List, finding a transaction by ID
    //       requires looping through every element (O(n)). This matters at scale.

    // -------------------------------------------------------
    // Day 4 (TICKET-F033): Add Stream-based filtering
    // -------------------------------------------------------
    // WHAT: Streams are Java's functional programming feature (added in Java 8).
    //       Instead of writing for-loops, you chain operations: filter → sort → collect.
    //
    // HOW:  1. getExpensesOver100(): .stream().filter(...).collect(toList())
    //       2. getSortedByDate():    .stream().sorted(Comparator.comparing(BaseTransaction::getTxnDate)).collect(toList())

    // -------------------------------------------------------
    // Day 4 (TICKET-F034): Lambda Comparator for custom sorting
    // -------------------------------------------------------
    // WHAT: A Lambda is an anonymous function.
    //       (a, b) -> b.getAmount().compareTo(a.getAmount()) compares two transactions.
    //
    // HOW:  getSortedByAmount(): .stream().sorted((a,b) -> b.getAmount().compareTo(a.getAmount())).collect(toList())


    // ==========================================================
    //  DAY 6 (Sprint 5): Spring @Service with JPA Repositories
    //  ---- NOT YET IMPLEMENTED — see Day 6 README ----
    // ==========================================================

    // -------------------------------------------------------
    // Day 6 (TICKET-F063): Step 1 — Add @Service + inject repositories
    //                       Step 2 — Implement CRUD methods against JPA
    // -------------------------------------------------------
    // See Day 6 README for the full walkthrough. Broad strokes:
    //   - @Service on the class
    //   - constructor injection of TransactionRepository / UserRepository / CategoryRepository
    //   - getAll / getById / getByUserId / create / update / delete against repositories
    //   - throw ResourceNotFoundException for missing IDs, InvalidTransactionException for bad input
}
