package com.smartbudget.repository;

import com.smartbudget.entity.SavingsGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// ============================================================
// TICKET-F061 (Day 6, Sprint 5) — Savings Goal Repository  [SOLVED]
// ============================================================
//
// WHAT: A JPA Repository for the SavingsGoal entity.
//       Like all JPA repositories, you get findAll(), findById(), save(),
//       deleteById() for FREE by extending JpaRepository.
//       This file also declares one custom query — findByUser_UserId — used
//       by SavingsGoalController.byUser() to serve GET /api/goals/user/{userId}.
//
// WHY:  Each user has their own savings goals. The SavingsGoalController's
//       GET /api/goals/user/{userId} endpoint needs a way to fetch only that
//       user's goals — not every goal in the database.
//
// HOW IT WORKS:
//       JpaRepository<SavingsGoal, Long> means:
//         SavingsGoal = the @Entity class (maps to savings_goals table)
//         Long        = the type of the primary key (goalId is Long)
//
//       The underscore in "User_UserId" is Spring Data JPA's convention for
//       traversing a relationship: SavingsGoal → user (@ManyToOne) → userId.
//       Spring generates:  SELECT * FROM savings_goals WHERE user_id = ?
// ============================================================
@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {

    /**
     * Finds all savings goals belonging to a specific user.
     * Returns an empty list (not null) when the user has no goals
     * or when the user id doesn't exist at all.
     */
    List<SavingsGoal> findByUser_UserId(Long userId);
}