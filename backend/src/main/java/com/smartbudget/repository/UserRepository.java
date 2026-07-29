package com.smartbudget.repository;

import com.smartbudget.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// ============================================================
// TICKET-F050 (Day 5, Sprint 4) — User Repository
// ============================================================
//
// WHAT: A JPA Repository for the User entity.
//       By extending JpaRepository<User, Long>, you get all standard
//       CRUD operations for FREE: findAll(), findById(), save(), deleteById().
//       Custom query methods are added below.
//
// WHY:  Users are referenced by Transactions (via @ManyToOne) and SavingsGoals.
//       The service layer needs to look up users by email (for login/validation)
//       and check if an email already exists (for registration).
//
// HOW IT WORKS:
//       JpaRepository<User, Long> means:
//         User = the @Entity class (maps to the "users" table)
//         Long = the type of the primary key (userId is Long)
//       Spring creates the implementation at startup — you only declare methods.
//
// ============================================================
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     * Spring generates: SELECT * FROM users WHERE email = ?
     *
     * Returns Optional<User> so callers must handle the "not found" case
     * explicitly (no NullPointerException surprises):
     *   repo.findByEmail("alice@bank.com").orElseThrow(...)
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether a user with the given email already exists.
     * Spring generates: SELECT COUNT(*) > 0 FROM users WHERE email = ?
     *
     * Faster than findByEmail when you only need a yes/no answer — no need
     * to hydrate the full User entity. Used during registration to enforce
     * the UNIQUE email constraint before Hibernate rejects the INSERT.
     */
    boolean existsByEmail(String email);

    /**
     * Bonus: case-insensitive name search.
     * Spring generates: WHERE LOWER(name) LIKE LOWER(CONCAT('%', ?, '%'))
     */
    List<User> findByNameContainingIgnoreCase(String search);
}