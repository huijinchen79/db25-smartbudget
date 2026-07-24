package com.smartbudget.service;

import com.smartbudget.entity.Category;
import com.smartbudget.entity.Transaction;
import com.smartbudget.entity.User;
import com.smartbudget.exception.InvalidTransactionException;
import com.smartbudget.exception.ResourceNotFoundException;
import com.smartbudget.model.BaseTransaction;
import com.smartbudget.model.ExpenseTransaction;
import com.smartbudget.model.IncomeTransaction;
import com.smartbudget.repository.CategoryRepository;
import com.smartbudget.repository.TransactionRepository;
import com.smartbudget.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// ============================================================
// TransactionService — evolves across THREE days:  [SOLVED for Day 6]
//
// Day 3 (Sprint 2) — TICKET-F026 to F030: Plain Java service with List
// Day 4 (Sprint 3) — TICKET-F032 to F034: Refactor with HashMap + Streams + Lambdas
// Day 6 (Sprint 5) — TICKET-F063:         Spring @Service using JPA repositories
//
// Each day BUILDS on the previous. The Day 6 solved copy keeps the
// Day 3/4 in-memory helper methods available (they operate on the
// Day-3 `com.smartbudget.model.BaseTransaction` type), while the
// Day 6 methods form the *actual* production service surface used
// by the controllers and integration tests.
// ============================================================
@Service
public class TransactionService {

    // ==========================================================
    //  DAY 3 / DAY 4 — In-memory storage (kept for backwards compat)
    // ==========================================================
    // Day 3 (F026) started as a List; Day 4 (F032) refactored to a HashMap
    // keyed by the transaction id — O(1) lookups by id.
    private final Map<String, BaseTransaction> transactions = new HashMap<>();

    // ==========================================================
    //  DAY 6 — JPA repositories injected by Spring
    // ==========================================================
    private final TransactionRepository txnRepo;
    private final UserRepository        userRepo;
    private final CategoryRepository    categoryRepo;

    public TransactionService(TransactionRepository txnRepo,
                              UserRepository userRepo,
                              CategoryRepository categoryRepo) {
        this.txnRepo      = txnRepo;
        this.userRepo     = userRepo;
        this.categoryRepo = categoryRepo;
    }

    // ==========================================================
    //  DAY 3 (F026) — addTransaction / getAllInMemory
    // ==========================================================
    /** Adds an in-memory transaction (kept from Day 3 for the CSV round-trip). */
    public void addTransaction(BaseTransaction t) {
        transactions.put(String.valueOf(t.getTxnId()), t);
    }

    /** Returns a defensive copy of the in-memory transactions collection. */
    public List<BaseTransaction> getAllInMemory() {
        return new ArrayList<>(transactions.values());
    }

    // ==========================================================
    //  DAY 3 (F027) — filterByDateRange
    // ==========================================================
    public List<BaseTransaction> filterByDateRange(LocalDate from, LocalDate to) {
        List<BaseTransaction> result = new ArrayList<>();
        for (BaseTransaction t : transactions.values()) {
            LocalDate d = t.getTxnDate();
            if (!d.isBefore(from) && !d.isAfter(to)) {
                result.add(t);
            }
        }
        return result;
    }

    // ==========================================================
    //  DAY 3 (F028) — calculateTotalByType
    // ==========================================================
    public BigDecimal calculateTotalByType(String type) {
        BigDecimal total = BigDecimal.ZERO;
        for (BaseTransaction t : transactions.values()) {
            if (type != null && type.equals(t.getType())) {
                total = total.add(t.getAmount());
            }
        }
        return total;
    }

    // ==========================================================
    //  DAY 3 (F029) — exportToCSV
    // ==========================================================
    public void exportToCSV(String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("id,type,amount,date,description");
            writer.newLine();
            for (BaseTransaction t : transactions.values()) {
                String desc = t.getDescription() == null ? "" : t.getDescription();
                writer.write(String.format("%d,%s,%s,%s,%s",
                        t.getTxnId(),
                        t.getType(),
                        t.getAmount().toPlainString(),
                        t.getTxnDate().toString(),
                        desc));
                writer.newLine();
            }
        }
    }

    // ==========================================================
    //  DAY 3 (F030) — importFromCSV
    // ==========================================================
    public void importFromCSV(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine(); // header
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] parts = line.split(",", -1);
                int id             = Integer.parseInt(parts[0].trim());
                String type        = parts[1].trim();
                BigDecimal amount  = new BigDecimal(parts[2].trim());
                LocalDate date     = LocalDate.parse(parts[3].trim());
                String description = parts.length > 4 ? parts[4] : "";

                BaseTransaction t = "INCOME".equalsIgnoreCase(type)
                        ? new IncomeTransaction(id, amount, date, description)
                        : new ExpenseTransaction(id, amount, date, description);
                addTransaction(t);
            }
        }
    }

    // ==========================================================
    //  DAY 4 (F033) — Stream-based filtering
    // ==========================================================
    public List<BaseTransaction> getExpensesOver100() {
        return transactions.values().stream()
                .filter(t -> "EXPENSE".equals(t.getType()))
                .filter(t -> t.getAmount().compareTo(new BigDecimal("100")) > 0)
                .collect(Collectors.toList());
    }

    public List<BaseTransaction> getSortedByDate() {
        return transactions.values().stream()
                .sorted(Comparator.comparing(BaseTransaction::getTxnDate))
                .collect(Collectors.toList());
    }

    // ==========================================================
    //  DAY 4 (F034) — Lambda comparator, descending by amount
    // ==========================================================
    public List<BaseTransaction> getSortedByAmount() {
        return transactions.values().stream()
                .sorted((a, b) -> b.getAmount().compareTo(a.getAmount()))
                .collect(Collectors.toList());
    }

    // ==========================================================
    //  DAY 6 (F063) — CRUD backed by JPA repositories
    // ==========================================================

    @Transactional(readOnly = true)
    public List<Transaction> getAll() {
        return txnRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Transaction getById(Long id) {
        return txnRepo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Transaction " + id + " not found"));
    }

    @Transactional(readOnly = true)
    public List<Transaction> getByUserId(Long userId) {
        return txnRepo.findByUser_UserIdOrderByTxnDateDesc(userId);
    }

    @Transactional
    public Transaction create(Long userId, Long categoryId,
                              BigDecimal amount, LocalDate date,
                              String description, String type) {
        // Validation — throwing InvalidTransactionException becomes HTTP 400
        // via GlobalExceptionHandler (F065).
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("amount must be > 0");
        }
        if (type == null || (!"INCOME".equals(type) && !"EXPENSE".equals(type))) {
            throw new InvalidTransactionException(
                    "type must be 'INCOME' or 'EXPENSE'");
        }
        if (date != null && date.isAfter(LocalDate.now())) {
            throw new InvalidTransactionException("date cannot be in the future");
        }
        if (userId == null) {
            throw new InvalidTransactionException("user.userId is required");
        }
        if (categoryId == null) {
            throw new InvalidTransactionException("category.categoryId is required");
        }

        // Referential integrity — a missing FK becomes HTTP 404, not 500.
        User user = userRepo.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User " + userId + " not found"));
        Category category = categoryRepo.findById(categoryId).orElseThrow(
                () -> new ResourceNotFoundException("Category " + categoryId + " not found"));

        Transaction t = new Transaction();
        t.setUser(user);
        t.setCategory(category);
        t.setAmount(amount);
        t.setTxnDate(date != null ? date : LocalDate.now());
        t.setDescription(description);
        t.setType(type);
        return txnRepo.save(t);
    }

    @Transactional
    public void delete(Long id) {
        if (!txnRepo.existsById(id)) {
            throw new ResourceNotFoundException("Transaction " + id + " not found");
        }
        txnRepo.deleteById(id);
    }

    @Transactional
    public Transaction update(Long id, BigDecimal amount, LocalDate date,
                              String description, String type) {
        Transaction t = getById(id);
        if (amount != null) {
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidTransactionException("amount must be > 0");
            }
            t.setAmount(amount);
        }
        if (date != null) {
            if (date.isAfter(LocalDate.now())) {
                throw new InvalidTransactionException("date cannot be in the future");
            }
            t.setTxnDate(date);
        }
        if (description != null) t.setDescription(description);
        if (type != null) {
            if (!"INCOME".equals(type) && !"EXPENSE".equals(type)) {
                throw new InvalidTransactionException(
                        "type must be 'INCOME' or 'EXPENSE'");
            }
            t.setType(type);
        }
        return txnRepo.save(t);
    }
}
