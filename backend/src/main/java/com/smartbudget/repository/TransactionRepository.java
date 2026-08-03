package com.smartbudget.repository;

import com.smartbudget.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// ============================================================
// TICKET-F051 (Day 5, Sprint 4) — Transaction Repository
// ============================================================
//
// WHAT: A Spring Data JPA Repository is an interface (NOT a class) that
//       gives you database operations for FREE — no SQL, no implementation code.
//       By extending JpaRepository<Transaction, Long>, you automatically get:
//         findAll()        -> SELECT * FROM transactions
//         findById(Long)   -> SELECT * WHERE txn_id = ?
//         save(Transaction)-> INSERT or UPDATE
//         deleteById(Long) -> DELETE WHERE txn_id = ?
//         count()          -> SELECT COUNT(*)
//
// WHY:  Compare this with TransactionDAO.java (Day 4) where you wrote raw JDBC
//       with PreparedStatement, ResultSet, while(rs.next()), etc.
//       Spring Data JPA does ALL of that automatically. Same result, zero boilerplate.
//
// HOW IT WORKS:
//       JpaRepository<Transaction, Long> means:
//         Transaction = the @Entity class this repository manages
//         Long        = the type of the primary key (txnId is Long)
//       Spring scans for interfaces extending JpaRepository and creates
//       implementation classes at runtime using proxies.
//
// ============================================================
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * 1) All transactions for a specific user, newest first.
     *
     * The underscore in "User_UserId" tells Spring Data:
     *   navigate the @ManyToOne "user" field, then match on its userId property.
     * Generated SQL (roughly):
     *   SELECT t.* FROM transactions t
     *   WHERE  t.user_id = ?
     *   ORDER BY t.txn_date DESC
     */
    List<Transaction> findByUser_UserIdOrderByTxnDateDesc(Long userId);

    /**
     * 2) Filter by transaction type ('INCOME' or 'EXPENSE').
     * Generated SQL: WHERE type = ?
     */
    List<Transaction> findByType(String type);

    /**
     * 3) Transactions within an inclusive date range.
     * Generated SQL: WHERE txn_date BETWEEN ? AND ?
     */
    List<Transaction> findByTxnDateBetween(LocalDate from, LocalDate to);

    /**
     * 4) Sum amounts for a user + type combination.
     *
     * Aggregations aren't derivable from method names, so we drop to JPQL.
     * JPQL is "SQL for entities" — we reference the entity name (Transaction)
     * and the field path (t.user.userId), NOT the underlying table/column names.
     *
     * COALESCE(SUM(t.amount), 0) is the critical bit: without it the query
     * returns NULL when there are no matching rows, which would blow up
     * downstream arithmetic with a NullPointerException.
     */
    @Query("""
           SELECT COALESCE(SUM(t.amount), 0)
           FROM   Transaction t
           WHERE  t.user.userId = :userId
             AND  t.type        = :type
           """)
    BigDecimal sumByUserAndType(@Param("userId") Long userId,
                                @Param("type")   String type);
}
