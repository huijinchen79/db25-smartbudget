package com.smartbudget.repository;

import com.smartbudget.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// ============================================================
// TICKET-F052 (Day 5, Sprint 4) — Category Repository
// ============================================================
//
// WHAT: A JPA Repository for the Category entity.
//       This is the simplest repository in the project.
//       CategoryController (the Day 1 proof-of-life endpoint) already uses
//       the inherited findAll() method to return all categories.
//
// WHY:  Categories classify transactions (e.g., "Salary" = INCOME, "Groceries" = EXPENSE).
//       The AddTransactionForm needs a dropdown of categories, which comes from
//       GET /api/categories -> CategoryController -> this repository's findAll().
//
// HOW IT WORKS:
//       JpaRepository<Category, Long> gives you findAll(), findById(), save(), etc.
//       Below we add ONE custom query method for filtering by type.
//
// ============================================================
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Returns all categories of a given type ('INCOME' or 'EXPENSE').
     * Generated SQL: SELECT * FROM categories WHERE type = ?
     *
     * Powers the type-aware category dropdown on the AddTransactionForm:
     * when the user picks "Expense", only expense categories appear.
     */
    List<Category> findByType(String type);
}