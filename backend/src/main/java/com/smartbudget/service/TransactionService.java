package com.smartbudget.service;

import com.smartbudget.exception.InvalidTransactionException;
import com.smartbudget.model.BaseTransaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.Comparator;
import java.util.stream.Collectors;

public class TransactionService {

    public List<BaseTransaction> getSortedByAmount() {
        return transactions.values().stream()
                .sorted((a, b) -> b.getAmount().compareTo(a.getAmount()))
                .collect(Collectors.toList());
    }

    public List<BaseTransaction> getSortedByAmount() {
        return transactions.values().stream()
                .sorted(new Comparator<BaseTransaction>() {
                    @Override
                    public int compare(BaseTransaction a, BaseTransaction b) {
                        return b.getAmount().compareTo(a.getAmount());
                    }
                })
                .collect(Collectors.toList());
    }

    public List<BaseTransaction> getExpensesOver100() {
        BigDecimal threshold = new BigDecimal("100");
        return transactions.values().stream()
                .filter(t -> "EXPENSE".equals(t.getType()))
                .filter(t -> t.getAmount().compareTo(threshold) > 0)
                .collect(Collectors.toList());
    }

    public List<BaseTransaction> getSortedByDate() {
        return transactions.values().stream()
                .sorted(Comparator.comparing(BaseTransaction::getTxnDate))
                .collect(Collectors.toList());
    }

    public List<BaseTransaction> getSortedByDateDesc() {
        return transactions.values().stream()
                .sorted(Comparator.comparing(BaseTransaction::getTxnDate).reversed())
                .collect(Collectors.toList());
    }

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