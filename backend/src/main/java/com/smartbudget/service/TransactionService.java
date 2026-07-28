package com.smartbudget.service;

import com.smartbudget.model.BaseTransaction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.hibernate.mapping.Map;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.format.DateTimeParseException;

import com.smartbudget.exception.InvalidTransactionException;
import com.smartbudget.model.IncomeTransaction;
import com.smartbudget.model.ExpenseTransaction;
// ============================================================
// TransactionService — evolves across THREE days:
//
// Day 3 (Sprint 2) — TICKET-F026 to F030: Plain Java service with List
// Day 4 (Sprint 3) — TICKET-F032 to F034: Refactor with HashMap + Streams + Lambdas
// Day 6 (Sprint 5) — TICKET-F063:         Spring @Service using JPA repositories
//
// Each day BUILDS on the previous — don't delete old code, evolve it.
// ============================================================

public class TransactionService {

    private final Map<String, BaseTransaction> transactions = new HashMap<>();

    public void addTransaction(BaseTransaction t) {
        if (t == null) {
            throw new IllegalArgumentException("transaction must not be null");
        }
        if (t.getDescription() == null || t.getDescription().isBlank()) {
            throw new InvalidTransactionException("description must not be blank");
        }
        transactions.put(String.valueOf(t.getTxnId()), t);
    }

    public BaseTransaction findById(String id) { return transactions.get(id); }

    public boolean delete(String id) { return transactions.remove(id) != null; }

    public List<BaseTransaction> getAll() {
        return new ArrayList<>(transactions.values());
    }

    public int size() { return transactions.size(); }

    public List<BaseTransaction> filterByDateRange(LocalDate from, LocalDate to) {
        List<BaseTransaction> result = new ArrayList<>();
        for (BaseTransaction t : transactions.values()) {
            LocalDate d = t.getTxnDate();
            if (!d.isBefore(from) && !d.isAfter(to)) result.add(t);
        }
        return result;
    }

    public BigDecimal calculateTotalByType(String type) {
        BigDecimal total = BigDecimal.ZERO;
        for (BaseTransaction t : transactions.values()) {
            if (type.equals(t.getType())) total = total.add(t.getAmount());
        }
        return total;
    }
}


    // ==========================================================
    //  DAY 3 (Sprint 2): Plain Java with ArrayList
    // ==========================================================

    // -------------------------------------------------------
    // TODO TICKET-F026: Step 1 — Add a storage field
    // -------------------------------------------------------
    // WHAT: A List (specifically ArrayList) stores transactions in memory.
    //       This is NOT a database — data is lost when the program exits.
    //       Think of it as a temporary in-memory storage for practice.
    //
    // HOW:  Declare a private field of type List<BaseTransaction> and initialize it as a new ArrayList.
    //       Import BaseTransaction from com.smartbudget.model.
    //
    // WHY:  Before connecting to a database (Day 4), you need somewhere to store data.
    //       ArrayList is the simplest collection — it maintains insertion order and allows duplicates.

    // -------------------------------------------------------
    // TODO TICKET-F026: Step 2 — addTransaction() and getAll()
    // -------------------------------------------------------
    // WHAT: Basic CRUD operations — Create and Read.
    //       addTransaction adds a BaseTransaction to the list.
    //       getAll returns all transactions (return a copy, not the original list).
    //
    // HOW:  addTransaction: accepts a BaseTransaction, calls list.add(t).
    //       getAll: returns new ArrayList<>(transactions) — a defensive copy.
    //
    // WHY:  Returning a copy in getAll() prevents external code from modifying your internal list.
    //       This is a defensive programming practice.
    //
    // OBSERVE: Call addTransaction 3 times, then getAll(). The list should have 3 items.

    
    // -------------------------------------------------------
    // TODO TICKET-F027: filterByDateRange(LocalDate from, LocalDate to)
    // -------------------------------------------------------
    // WHAT: Filtering means returning only items that match certain criteria.
    //       This method returns transactions that fall within a date range.
    //
    // HOW:  Loop through the list. For each transaction, check if its date is:
    //         - NOT before the "from" date (use !date.isBefore(from))
    //         - NOT after the "to" date (use !date.isAfter(to))
    //       If both conditions are true, add it to a result list and return the result.
    //
    // WHY:  Date filtering is essential for financial apps — users want to see
    //       "all transactions this month" or "last 30 days".
    //
    // OBSERVE: Add transactions with different dates, then filter for a specific range.
    //          Only transactions within that range should appear.
   // public List<BaseTransaction> filterByDateRange(LocalDate from, LocalDate to) {
   //     if (from == null || to == null) {
   //         throw new IllegalArgumentException("from and to must be non-null");
     //   }
        //if (from.isAfter(to)) {
        //    return new ArrayList<>();           // empty range, not error
       // }
       // List<BaseTransaction> result = new ArrayList<>();
        //for (BaseTransaction t : transactions) {
         //   LocalDate d = t.getTxnDate();
          //  if (!d.isBefore(from) && !d.isAfter(to)) {
            //    result.add(t);
           // }
       // }
       // return result;
   // }

    // -------------------------------------------------------
    // TODO TICKET-F028: calculateTotalByType(String type)
    // -------------------------------------------------------
    // WHAT: Aggregation — summing up amounts for a specific type (INCOME or EXPENSE).
    //
    // HOW:  Start with BigDecimal.ZERO. Loop through the list.
    //       For each transaction where getType() matches the parameter,
    //       add its amount to the total using BigDecimal's .add() method.
    //       Return the total.
    //
    // WHY:  The dashboard needs "Total Income" and "Total Expenses" values.
    //       Use BigDecimal (not double) for financial calculations to avoid precision errors.
    //
    // OBSERVE: Add some income and expense transactions.
    //          calculateTotalByType("INCOME") should return the sum of all incomes.
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

    // Bonus: stream version — same behaviour, more idiomatic from Java 8 onwards
    public BigDecimal calculateTotalByTypeStream(String type) {
        return transactions.stream()
                .filter(t -> type.equals(t.getType()))
                .map(BaseTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    // -------------------------------------------------------
    // TODO TICKET-F029: exportToCSV(String filePath)
    // -------------------------------------------------------
    // WHAT: CSV (Comma-Separated Values) is a simple file format for tabular data.
    //       Each line is a row, columns separated by commas.
    //       Example: 1,INCOME,3500.00,2026-05-01,May salary
    //
    // HOW:  Use BufferedWriter (wraps FileWriter) for efficient file writing.
    //       Write a header line first: "id,type,amount,date,description"
    //       Loop through transactions, write each as a CSV line.
    //       Use try-with-resources to auto-close the writer.
    //
    // WHY:  CSV export is a common feature in financial apps.
    //       Users import CSVs into Excel for further analysis.
    //
    // OBSERVE: After exporting, open the file in a text editor or Excel.
    //          Each transaction should be one row with comma-separated values.
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
                        t.getAmount().toPlainString(),
                        t.getTxnDate().toString(),
                        csvEscape(t.getDescription())
                ));
                bw.newLine();
            }
        }
    }

    private static String csvEscape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
    // -------------------------------------------------------
    // TODO TICKET-F030: importFromCSV(String filePath)
    // -------------------------------------------------------
    // WHAT: The reverse of export — read a CSV file and create transaction objects.
    //
    // HOW:  Use BufferedReader (wraps FileReader) for efficient file reading.
    //       Skip the first line (header). For each subsequent line:
    //         Split by comma using line.split(",")
    //         Parse each field (id, type, amount, date, description)
    //         Create an IncomeTransaction or ExpenseTransaction based on the type field
    //         Add it to the transactions list.
    //
    // WHY:  Import/export together provide data portability.
    //       Users can back up their data as CSV and restore it later.
    //
    // OBSERVE: Export, then clear the list, then import from the same file.
    //          The list should have the same data as before.


    // ==========================================================
    //  DAY 4 (Sprint 3): Refactor with HashMap + Streams + Lambdas
    // ==========================================================

    // -------------------------------------------------------
    // TODO TICKET-F032: Refactor storage from List → HashMap
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
    //
    // OBSERVE: All existing functionality should still work — just faster lookups by ID.
    public void importFromCSV(String filePath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine();
            if (line == null) return;                    // empty file

            int lineNum = 1;
            while ((line = br.readLine()) != null) {
                lineNum++;
                if (line.isBlank()) continue;
                try {
                    String[] parts = line.split(",", -1);
                    int id            = Integer.parseInt(parts[0].trim());
                    String type       = parts[1].trim();
                    BigDecimal amount = new BigDecimal(parts[2].trim());
                    LocalDate date    = LocalDate.parse(parts[3].trim());
                    String desc       = parts.length > 4 ? parts[4] : "";

                    BaseTransaction t = switch (type) {
                        case "INCOME"  -> new IncomeTransaction (id, amount, date, desc);
                        case "EXPENSE" -> new ExpenseTransaction(id, amount, date, desc, "Imported");
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
    // -------------------------------------------------------
    // TODO TICKET-F033: Add Stream-based filtering
    // -------------------------------------------------------
    // WHAT: Streams are Java's functional programming feature (added in Java 8).
    //       Instead of writing for-loops, you chain operations: filter → sort → collect.
    //       Streams are declarative — you say WHAT you want, not HOW to do it.
    //
    // HOW:  Create two methods:
    //       1. getExpensesOver100(): Use .stream().filter() to keep only expenses with amount > 100.
    //          Chain: collection.stream() → .filter(condition1) → .filter(condition2) → .collect(toList())
    //       2. getSortedByDate(): Use .stream().sorted() with Comparator.comparing().
    //          Chain: collection.stream() → .sorted(Comparator.comparing(BaseTransaction::getTxnDate)) → .collect(toList())
    //
    // WHY:  Streams are more concise and readable than for-loops for data processing.
    //       They're also parallelizable — Java can automatically process large datasets across CPU cores.
    //
    // OBSERVE: Compare the Stream version with the for-loop version (Day 3).
    //          Same result, fewer lines, more readable.
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

    // -------------------------------------------------------
    // TODO TICKET-F034: Lambda Comparator for custom sorting
    // -------------------------------------------------------
    // WHAT: A Lambda is an anonymous function — a function without a name.
    //       (a, b) -> b.getAmount().compareTo(a.getAmount()) is a Lambda that compares two transactions.
    //       This replaces creating a separate Comparator class.
    //
    // HOW:  Create getSortedByAmount() that uses .sorted() with a lambda comparator.
    //       For descending order: (a, b) -> b.getAmount().compareTo(a.getAmount())
    //       For ascending order: (a, b) -> a.getAmount().compareTo(b.getAmount())
    //
    // WHY:  Before Java 8, sorting required creating anonymous inner classes (verbose).
    //       Lambdas do the same thing in one line.
    //
    // OBSERVE: Call getSortedByAmount() — the first item should have the highest amount.


    // ==========================================================
    //  DAY 6 (Sprint 5): Spring @Service with JPA Repositories
    // ==========================================================

    // -------------------------------------------------------
    // TODO TICKET-F063: Step 1 — Add @Service annotation + inject repositories
    // -------------------------------------------------------
    // WHAT: @Service tells Spring: "This is a business logic class. Create one instance
    //       and manage its lifecycle." Spring then automatically injects (provides)
    //       the repositories this service needs through the constructor.
    //       This is called Dependency Injection (DI).
    //
    // HOW:  Add @Service above the class declaration.
    //       Declare three private final fields: TransactionRepository, UserRepository, CategoryRepository.
    //       Create a constructor that accepts all three and assigns them.
    //       Spring automatically calls this constructor and passes the repository beans.
    //
    // WHY:  Separating controllers (HTTP handling) from services (business logic) follows
    //       the Single Responsibility Principle. Controllers handle requests,
    //       services handle rules, repositories handle data access.
    //
    // OBSERVE: The app should still boot. Check Spring logs for "Creating bean: transactionService".

    // -------------------------------------------------------
    // TODO TICKET-F063: Step 2 — Implement CRUD methods
    // -------------------------------------------------------
    // WHAT: The service wraps repository calls with validation and error handling.
    //       Methods: getAll(), getById(), getByUserId(), create(), update(), delete()
    //
    // HOW:  For getAll(): simply delegate to repo.findAll()
    //       For getById(): use repo.findById(id).orElseThrow() — throw ResourceNotFoundException if missing
    //       For create(): validate amount > 0 (throw InvalidTransactionException if not),
    //         look up the User and Category by ID (throw ResourceNotFoundException if missing),
    //         build a Transaction entity, save it with repo.save()
    //       For delete(): check repo.existsById(id) first, throw ResourceNotFoundException if false
    //
    // WHY:  The repository has no business rules — repo.save() accepts ANY data.
    //       The service adds validation before saving, ensuring bad data never reaches the database.
    //       orElseThrow() is a clean way to handle "not found" cases without null checks.
    //
    // OBSERVE: After implementing, build TransactionController to use this service.
    //          POST a transaction with amount = -10 → should get HTTP 400.
    //          GET a non-existent ID → should get HTTP 404.
    //          These responses come from the exceptions caught by GlobalExceptionHandler.
}
