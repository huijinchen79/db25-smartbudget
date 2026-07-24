package com.smartbudget.service;

import com.smartbudget.entity.SavingsGoal;
import com.smartbudget.entity.User;
import com.smartbudget.exception.InvalidTransactionException;
import com.smartbudget.exception.ResourceNotFoundException;
import com.smartbudget.repository.SavingsGoalRepository;
import com.smartbudget.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// ============================================================
// TICKET-F061 / F062 (Day 6, Sprint 5) — Savings Goal Service  [SOLVED]
// ============================================================
//
// WHAT: Business logic for savings goals — the "service" layer that sits
//       between the controller and the JPA repository. Encapsulates lookups,
//       validation, and the contribute() workflow.
//
// WHY:  The controller should only handle HTTP concerns (request/response).
//       Business rules like "contribution must be > 0" and "cannot exceed
//       target" belong here so they apply uniformly no matter which
//       controller path calls them.
//
// NOTE: In the Day-6 SOLVED build the SavingsGoalController deliberately
//       talks directly to SavingsGoalRepository (matching the guide's F061
//       Hint 3 for clarity). This service is still provided so it can be
//       used by later refactors and so no Day-6 TODOs remain in the tree.
// ============================================================
@Service
public class SavingsGoalService {

    private final SavingsGoalRepository goalRepo;
    private final UserRepository        userRepo;

    public SavingsGoalService(SavingsGoalRepository goalRepo,
                              UserRepository userRepo) {
        this.goalRepo = goalRepo;
        this.userRepo = userRepo;
    }

    // -------------------------------------------------------
    // TICKET-F061 — Step 2: getByUserId(Long userId)
    // -------------------------------------------------------
    @Transactional(readOnly = true)
    public List<SavingsGoal> getByUserId(Long userId) {
        return goalRepo.findByUser_UserId(userId);
    }

    // -------------------------------------------------------
    // TICKET-F061 — Step 3: getById(Long id)
    // -------------------------------------------------------
    @Transactional(readOnly = true)
    public SavingsGoal getById(Long id) {
        return goalRepo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Goal " + id + " not found"));
    }

    // -------------------------------------------------------
    // TICKET-F062 — Step 4: contribute(Long goalId, BigDecimal amount)
    // -------------------------------------------------------
    @Transactional
    public SavingsGoal contribute(Long goalId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Contribution must be > 0");
        }
        SavingsGoal goal = getById(goalId);

        BigDecimal newTotal = goal.getCurrentAmount().add(amount);
        if (newTotal.compareTo(goal.getTargetAmount()) > 0) {
            BigDecimal over = newTotal.subtract(goal.getTargetAmount());
            throw new InvalidTransactionException(
                    "Contribution exceeds target by " + over);
        }

        goal.setCurrentAmount(newTotal);
        return goalRepo.save(goal);
    }

    // -------------------------------------------------------
    // TICKET-F061 — Step 5: create(userId, name, target, deadline)
    // -------------------------------------------------------
    @Transactional
    public SavingsGoal create(Long userId, String name,
                              BigDecimal target, LocalDate deadline) {
        if (name == null || name.isBlank()) {
            throw new InvalidTransactionException("Goal name is required");
        }
        if (target == null || target.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Target amount must be > 0");
        }

        User user = userRepo.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User " + userId + " not found"));

        SavingsGoal goal = new SavingsGoal();
        goal.setUser(user);
        goal.setName(name);
        goal.setTargetAmount(target);
        goal.setCurrentAmount(BigDecimal.ZERO);
        goal.setDeadline(deadline);
        return goalRepo.save(goal);
    }
}
