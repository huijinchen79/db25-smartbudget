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

    private final Map<String, BaseTransaction> transactions = new HashMap<>();

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

    public BaseTransaction findById(String id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        return transactions.get(id);
    }

    public boolean delete(String id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        return transactions.remove(id) != null;
    }

    public List<BaseTransaction> getAll() {
        return new ArrayList<>(transactions.values());
    }

    public int size() {
        return transactions.size();
    }

    public List<BaseTransaction> filterByDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from and to must be non-null");
        }
        if (from.isAfter(to)) {
            return new ArrayList<>();
        }

        List<BaseTransaction> result = new ArrayList<>();

        for (BaseTransaction t : transactions.values()) {
            LocalDate date = t.getTxnDate();

            if (!date.isBefore(from) && !date.isAfter(to)) {
                result.add(t);
            }
        }

        return result;
    }

    public BigDecimal calculateTotalByType(String type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }

        BigDecimal total = BigDecimal.ZERO;

        for (BaseTransaction t : transactions.values()) {
            if (type.equalsIgnoreCase(t.getType())) {
                total = total.add(t.getAmount());
            }
        }

        return total;
    }

    public BigDecimal calculateTotalByTypeStream(String type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }

        return transactions.values().stream()
                .filter(t -> type.equalsIgnoreCase(t.getType()))
                .map(BaseTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void exportToCSV(String filePath) throws IOException {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("filePath must not be blank");
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            bw.write("id,type,amount,date,description");
            bw.newLine();

            for (BaseTransaction t : transactions.values()) {
                bw.write(String.join(",",
                        String.valueOf(t.getTxnId()),
                        t.getType(),
                        t.getAmount().toPlainString(),
                        t.getTxnDate().toString(),
                        csvEscape(t.getDescription())));
                bw.newLine();
            }
        }
    }

    public void importFromCSV(String filePath) throws IOException {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("filePath must not be blank");
        }

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
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
                    List<String> parts = parseCsvLine(line);

                    if (parts.size() < 5) {
                        System.err.println("Skipping bad row at line " + lineNum
                                + ": expected 5 columns");
                        continue;
                    }

                    int id = Integer.parseInt(parts.get(0).trim());
                    String type = parts.get(1).trim().toUpperCase();
                    BigDecimal amount = new BigDecimal(parts.get(2).trim());
                    LocalDate date = LocalDate.parse(parts.get(3).trim());
                    String desc = parts.get(4);

                    BaseTransaction t = switch (type) {
                        case "INCOME" -> new IncomeTransaction(id, amount, date, desc);
                        case "EXPENSE" -> new ExpenseTransaction(
                                id, amount, date, desc, "Imported");
                        default -> throw new InvalidTransactionException(
                                "Unknown type on line " + lineNum + ": " + type);
                    };

                    addTransaction(t);

                } catch (NumberFormatException | DateTimeParseException
                         | InvalidTransactionException e) {
                    System.err.println("Skipping bad row at line "
                            + lineNum + ": " + e.getMessage());
                }
            }
        }
    }

    public List<BaseTransaction> getExpensesOver100() {
        return transactions.values().stream()
                .filter(t -> "EXPENSE".equalsIgnoreCase(t.getType()))
                .filter(t -> t.getAmount().compareTo(new BigDecimal("100")) > 0)
                .collect(Collectors.toList());
    }

    public List<BaseTransaction> getSortedByDate() {
        return transactions.values().stream()
                .sorted(Comparator.comparing(BaseTransaction::getTxnDate))
                .collect(Collectors.toList());
    }

    public List<BaseTransaction> getSortedByAmount() {
        return transactions.values().stream()
                .sorted((a, b) -> b.getAmount().compareTo(a.getAmount()))
                .collect(Collectors.toList());
    }

    private static String csvEscape(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains(",") || value.contains("\"")
                || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }

    private static List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (insideQuotes && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    insideQuotes = !insideQuotes;
                }
            } else if (c == ',' && !insideQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        fields.add(current.toString());
        return fields;
    }
}