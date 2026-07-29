package com.smartbudget.controller;

import com.smartbudget.entity.SavingsGoal;
import com.smartbudget.exception.InvalidTransactionException;
import com.smartbudget.exception.ResourceNotFoundException;
import com.smartbudget.repository.SavingsGoalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

// ============================================================
// TICKET-F061 / F062 (Day 6, Sprint 5) — Savings Goal REST Controller  [SOLVED]
// ============================================================
//
// WHAT: HTTP endpoints for savings goals.
//       A savings goal tracks progress toward a financial target
//       (e.g., "Save £5000 for holiday by December 2026").
//       The key feature is the "contribute" endpoint — adding money to a goal.
//
// WHY:  Savings goals are a core feature of any personal finance app.
//       This controller demonstrates a business operation (contribute)
//       beyond simple CRUD — it modifies an existing resource's state and
//       enforces business rules (positive amount, not exceeding the target).
// ============================================================
@RestController
@RequestMapping("/api/goals")
public class SavingsGoalController {

    private final SavingsGoalRepository repo;

    public SavingsGoalController(SavingsGoalRepository repo) {
        this.repo = repo;
    }

    /** Small immutable carrier for the "contribute" PUT body. */
    public record ContributionRequest(BigDecimal amount) { }

    // -------------------------------------------------------
    // TICKET-F061 — GET /api/goals/user/{userId}
    // -------------------------------------------------------
    @GetMapping("/user/{userId}")
    public List<SavingsGoal> byUser(@PathVariable Long userId) {
        return repo.findByUser_UserId(userId);
    }

    // -------------------------------------------------------
    // TICKET-F061 — POST /api/goals (create a new goal)
    // -------------------------------------------------------
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SavingsGoal create(@RequestBody SavingsGoal goal) {
        if (goal.getCurrentAmount() == null) {
            goal.setCurrentAmount(BigDecimal.ZERO);
        }
        return repo.save(goal);
    }

    // -------------------------------------------------------
    // TICKET-F062 — PUT /api/goals/{id}/contribute
    // -------------------------------------------------------
    // Business rules:
    //   1. Goal must exist (else 404)
    //   2. Contribution must be > 0 (else 400)
    //   3. currentAmount + contribution must not exceed targetAmount (else 400)
    @PutMapping("/{id}/contribute")
    public SavingsGoal contribute(@PathVariable Long id,
                                  @RequestBody ContributionRequest body) {
        SavingsGoal goal = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal " + id + " not found"));

        BigDecimal amount = (body == null) ? null : body.amount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Contribution must be > 0");
        }

        BigDecimal newTotal = goal.getCurrentAmount().add(amount);
        if (newTotal.compareTo(goal.getTargetAmount()) > 0) {
            BigDecimal over = newTotal.subtract(goal.getTargetAmount());
            throw new InvalidTransactionException(
                    "Contribution exceeds target by " + over);
        }

        goal.setCurrentAmount(newTotal);
        return repo.save(goal);
    }
}