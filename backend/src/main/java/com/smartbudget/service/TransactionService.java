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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TransactionService {

    private final Map<String, BaseTransaction> transactions =
            new HashMap<>();

    // -------------------------------------------------------
    // TICKET-F031: Validate and add transaction
    // -------------------------------------------------------
    public void addTransaction(BaseTransaction t) {
        if (t == null) {
            throw new IllegalArgumentException(
                    "transaction must not be null");
        }

        if (t.getDescription() == null
                || t.getDescription().isBlank()) {
            throw new InvalidTransactionException(
                    "description must not be blank");
        }

        transactions.put(
                String.valueOf(t.getTxnId()),
                t);
    }

    // -------------------------------------------------------
    // TICKET-F032: Find transaction by ID
    // -------------------------------------------------------
    public BaseTransaction findById(String id) {
        return transactions.get(id);
    }

    // -------------------------------------------------------
    // TICKET-F032: Delete transaction by ID
    // -------------------------------------------------------
    public boolean delete(String id) {
        return transactions.remove(id) != null;
    }

    // -------------------------------------------------------
    // TICKET-F026: Return all transactions
    // -------------------------------------------------------
    public List<BaseTransaction> getAll() {
        return new ArrayList<>(transactions.values());
    }

    public int size() {
        return transactions.size();
    }

    // -------------------------------------------------------
    // TICKET-F027: Filter by date range
    // -------------------------------------------------------
    public List<BaseTransaction> filterByDateRange(
            LocalDate from,
            LocalDate to) {

        if (from == null || to == null) {
            throw new IllegalArgumentException(
                    "from and to must not be null");
        }

        if (from.isAfter(to)) {
            return new ArrayList<>();
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
    // TICKET-F028: Calculate total by type
    // -------------------------------------------------------
    public BigDecimal calculateTotalByType(String type) {
        if (type == null) {
            throw new IllegalArgumentException(
                    "type must not be null");
        }

        BigDecimal total = BigDecimal.ZERO;

        for (BaseTransaction t : transactions.values()) {
            if (type.equalsIgnoreCase(t.getType())) {
                total = total.add(t.getAmount());
            }
        }

        return total;
    }

    // -------------------------------------------------------
    // TICKET-F029: Export transactions to CSV
    // -------------------------------------------------------
    public void exportToCSV(String filePath)
            throws IOException {

        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException(
                    "filePath must not be blank");
        }

        try (BufferedWriter bw =
                     new BufferedWriter(
                             new FileWriter(filePath))) {

            bw.write("id,type,amount,date,description");
            bw.newLine();

            for (BaseTransaction t
                    : transactions.values()) {

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

    // -------------------------------------------------------
    // TICKET-F030: Import transactions from CSV
    // -------------------------------------------------------
    public void importFromCSV(String filePath)
            throws IOException {

        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException(
                    "filePath must not be blank");
        }

        try (BufferedReader br =
                     new BufferedReader(
                             new FileReader(filePath))) {

            // Read and skip the header.
            String line = br.readLine();

            if (line == null) {
                return;
            }

            int lineNum = 1;

            while ((line = br.readLine()) != null) {
                lineNum++;

                if (line.isBlank()) {
                    continue;
                }

                try {
                    List<String> parts =
                            parseCsvLine(line);

                    if (parts.size() < 5) {
                        System.err.println(
                                "Skipping bad row at line "
                                        + lineNum
                                        + ": expected 5 columns");
                        continue;
                    }

                    int id = Integer.parseInt(
                            parts.get(0).trim());

                    String type = parts.get(1)
                            .trim()
                            .toUpperCase();

                    BigDecimal amount =
                            new BigDecimal(
                                    parts.get(2).trim());

                    LocalDate date =
                            LocalDate.parse(
                                    parts.get(3).trim());

                    String description =
                            parts.get(4);

                    BaseTransaction transaction =
                            switch (type) {
                                case "INCOME" ->
                                        new IncomeTransaction(
                                                id,
                                                amount,
                                                date,
                                                description);

                                case "EXPENSE" ->
                                        new ExpenseTransaction(
                                                id,
                                                amount,
                                                date,
                                                description,
                                                "Imported");

                                default ->
                                        throw new InvalidTransactionException(
                                                "Unknown type on line "
                                                        + lineNum
                                                        + ": "
                                                        + type);
                            };

                    addTransaction(transaction);

                } catch (NumberFormatException
                         | DateTimeParseException
                         | InvalidTransactionException e) {

                    System.err.println(
                            "Skipping bad row at line "
                                    + lineNum
                                    + ": "
                                    + e.getMessage());
                }
            }
        }
    }

    // -------------------------------------------------------
    // CSV escaping helper
    // -------------------------------------------------------
    private static String csvEscape(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains(",")
                || value.contains("\"")
                || value.contains("\n")
                || value.contains("\r")) {

            return "\""
                    + value.replace("\"", "\"\"")
                    + "\"";
        }

        return value;
    }

    // -------------------------------------------------------
    // CSV parsing helper
    // -------------------------------------------------------
    private static List<String> parseCsvLine(
            String line) {

        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (insideQuotes
                        && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {

                    current.append('"');
                    i++;

                } else {
                    insideQuotes = !insideQuotes;
                }

            } else if (c == ','
                    && !insideQuotes) {

                fields.add(current.toString());
                current.setLength(0);

            } else {
                current.append(c);
            }
        }

        fields.add(current.toString());

        return fields;
    }

    // -------------------------------------------------------
    // TICKET-F033: Expenses over 100
    // -------------------------------------------------------
    public List<BaseTransaction> getExpensesOver100() {
        BigDecimal threshold =
                new BigDecimal("100");

        return transactions.values()
                .stream()
                .filter(t ->
                        "EXPENSE".equalsIgnoreCase(
                                t.getType()))
                .filter(t ->
                        t.getAmount()
                                .compareTo(threshold) > 0)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------
    // TICKET-F033: Sort by date ascending
    // -------------------------------------------------------
    public List<BaseTransaction> getSortedByDate() {
        return transactions.values()
                .stream()
                .sorted(Comparator.comparing(
                        BaseTransaction::getTxnDate))
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------
    // Extra: Sort by date descending
    // -------------------------------------------------------
    public List<BaseTransaction> getSortedByDateDesc() {
        return transactions.values()
                .stream()
                .sorted(Comparator.comparing(
                                BaseTransaction::getTxnDate)
                        .reversed())
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------
    // TICKET-F034: Sort by amount descending
    // -------------------------------------------------------
    public List<BaseTransaction> getSortedByAmount() {
        return transactions.values()
                .stream()
                .sorted((a, b) ->
                        b.getAmount()
                                .compareTo(a.getAmount()))
                .collect(Collectors.toList());
    }
}