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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// ============================================================
// TransactionService — evolves across THREE days:
//
// Day 3 (Sprint 2) — TICKET-F026 to F031: Plain Java service with List           [SOLVED HERE]
// Day 4 (Sprint 3) — TICKET-F032 to F034: Refactor with HashMap + Streams + Lambdas [SOLVED HERE]
// Day 6 (Sprint 5) — TICKET-F063:         Spring @Service using JPA repositories (comes later)
//
// Each day BUILDS on the previous — don't delete old code, evolve it.
// ============================================================
public class TransactionService {

    // ==========================================================
    //  DAY 3 + DAY 4: Storage
    // ==========================================================
    //
    // Day 3 used a List<BaseTransaction>. Day 4 (TICKET-F032) refactors to
    // Map<String, BaseTransaction> so that findById() is O(1) instead of O(n).
    // The public API (addTransaction, getAll, filterByDateRange, etc.) is preserved
    // — callers can't tell the underlying storage changed.
    private final Map<String, BaseTransaction> transactions = new HashMap<>();

    // ==========================================================
    //  DAY 3 (Sprint 2): Plain Java operations  — IMPLEMENTED
    // ==========================================================

    /**
     * TICKET-F026 / F031: Add a transaction after validating the arg.
     *
     * BaseTransaction's constructor already rejects bad amounts and future
     * dates — that's model-layer defence. This service-layer guard catches
     * things the model doesn't care about but the UI does: a null argument
     * or a whitespace-only description ("   ").
     *
     * Defence-in-depth: model validates SHAPE, service validates BUSINESS.
     *
     * TICKET-F032: put(...) instead of list.add(...) — keyed by txnId as String.
     */
    public void addTransaction(BaseTransaction t) {
        if (t == null) {
            throw new IllegalArgumentException("transaction must not be null");
        }
        if (t.getDescription() == null || t.getDescription().isBlank()) {
            throw new InvalidTransactionException(
                "description must not be blank for transaction id=" + t.getTxnId());
        }
        transactions.put(String.valueOf(t.getTxnId()), t);
    }

    /**
     * TICKET-F032: O(1) lookup by transaction ID.
     *
     * With a List this would need a full scan (O(n)). HashMap hashes the key
     * once, jumps to the right bucket, and returns the value directly.
     */
    public BaseTransaction findById(String id) {
        return transactions.get(id);
    }

    /**
     * Delete by ID. Returns true if something was removed, false if the id
     * was absent — never throws for missing IDs (per Day-4 F042 contract).
     */
    public boolean delete(String id) {
        return transactions.remove(id) != null;
    }

    /**
     * TICKET-F026: getAll() returns a defensive copy — callers mutating the
     * returned list can't corrupt the internal Map's value collection.
     */
    public List<BaseTransaction> getAll() {
        return new ArrayList<>(transactions.values());
    }

    /** Read-only alternative — fails fast on any attempted mutation. */
    public List<BaseTransaction> getAllUnmodifiable() {
        return Collections.unmodifiableList(new ArrayList<>(transactions.values()));
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
        for (BaseTransaction t : transactions.values()) {
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
        for (BaseTransaction t : transactions.values()) {
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
            for (BaseTransaction t : transactions.values()) {
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
                    transactions.put(String.valueOf(t.getTxnId()), t);
                } catch (NumberFormatException | DateTimeParseException e) {
                    System.err.println("Skipping bad row at line "
                            + lineNum + ": " + e.getMessage());
                }
            }
        }
    }


    // ==========================================================
    //  DAY 4 (Sprint 3): Streams + Lambdas  — IMPLEMENTED
    // ==========================================================

    /**
     * TICKET-F033: Stream pipeline — keep expenses whose amount exceeds £100.
     *
     * Read the pipeline top-to-bottom to see the intent:
     *   values → keep EXPENSE → keep amount > 100 → collect to List.
     *
     * BigDecimal comparison uses compareTo (not `>`) because BigDecimal is an
     * Object; a return value of > 0 means "amount is greater than threshold".
     */
    public List<BaseTransaction> getExpensesOver100() {
        BigDecimal threshold = new BigDecimal("100");
        return transactions.values().stream()
                .filter(t -> "EXPENSE".equals(t.getType()))
                .filter(t -> t.getAmount().compareTo(threshold) > 0)
                .collect(Collectors.toList());
    }

    /**
     * TICKET-F033: Sort by txnDate ascending (oldest first) via a method
     * reference — Comparator.comparing(BaseTransaction::getTxnDate).
     */
    public List<BaseTransaction> getSortedByDate() {
        return transactions.values().stream()
                .sorted(Comparator.comparing(BaseTransaction::getTxnDate))
                .collect(Collectors.toList());
    }

    /** Same as above but newest-first — .reversed() flips the Comparator. */
    public List<BaseTransaction> getSortedByDateDesc() {
        return transactions.values().stream()
                .sorted(Comparator.comparing(BaseTransaction::getTxnDate).reversed())
                .collect(Collectors.toList());
    }

    /**
     * TICKET-F034: Sort by amount descending via a lambda Comparator.
     *
     * `(a, b) -> b.getAmount().compareTo(a.getAmount())` swaps a and b so
     * the larger amount comes first. Compare with the anonymous-class form —
     * this one-liner replaces 7 lines of boilerplate and compiles to the
     * same bytecode.
     */
    public List<BaseTransaction> getSortedByAmount() {
        return transactions.values().stream()
                .sorted((a, b) -> b.getAmount().compareTo(a.getAmount()))
                .collect(Collectors.toList());
    }


    // ==========================================================
    //  DAY 6 (Sprint 5): Spring @Service with JPA Repositories
    //  ---- NOT YET IMPLEMENTED — see Day 6 README ----
    // ==========================================================
    //
    // Day 6 will layer a Spring-flavoured version of this class on top:
    //   - @Service on the class
    //   - constructor injection of TransactionRepository / UserRepository / CategoryRepository
    //   - getAll / getById / getByUserId / create / update / delete against repositories
    //   - ResourceNotFoundException for missing IDs, InvalidTransactionException for bad input
    //
    // For Day 4 the in-memory HashMap version above is exactly what F040-F043's
    // JUnit tests exercise.
}
