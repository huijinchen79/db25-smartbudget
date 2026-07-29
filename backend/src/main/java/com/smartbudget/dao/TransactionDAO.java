package com.smartbudget.dao;

import com.smartbudget.entity.Category;
import com.smartbudget.entity.Transaction;
import com.smartbudget.entity.User;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// ============================================================
// TICKET-F036 to F039 (Day 4, Sprint 3) — Raw JDBC DAO  [SOLVED]
// ============================================================
//
// WHAT: A DAO (Data Access Object) is a class whose ONLY job is to talk to the database.
//       It translates between Java objects and SQL queries.
//       This DAO uses raw JDBC — later (Day 5), you'll see how Spring Data JPA
//       does the same thing with ZERO SQL code.
//
//       SECURITY RULE: ALWAYS use PreparedStatement — NEVER concatenate user input into SQL.
//       Bad:  "SELECT * FROM transactions WHERE user_id = " + userId   ← SQL INJECTION ATTACK!
//       Good: "SELECT * FROM transactions WHERE user_id = ?"           ← Safe, uses parameter binding
//
// WHY:  Understanding raw JDBC helps you appreciate what Spring Data JPA does automatically.
//       In interviews, you may be asked about JDBC even if you use JPA day-to-day.
//
// NOTE ON THE Transaction ENTITY:
//       com.smartbudget.entity.Transaction is a JPA @Entity that models
//       user/category as objects (User user, Category category), not raw IDs.
//       This DAO bridges the raw SQL columns (user_id INT, category_id INT)
//       to those object fields by populating thin User/Category instances
//       that carry only their primary key. The JPA layer (Day 5) later
//       hydrates the full objects — for the Day-4 DAO layer, the IDs alone
//       are all we need.
//
// ============================================================
public class TransactionDAO {

    // ---- SQL constants (one place to review the schema contract) ----

    private static final String INSERT_SQL = """
            INSERT INTO transactions
                (user_id, category_id, amount, txn_date, description, type)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private static final String SELECT_ALL_SQL = """
            SELECT txn_id, user_id, category_id, amount, txn_date, description, type
            FROM   transactions
            ORDER  BY txn_date DESC, txn_id DESC
            """;

    private static final String SELECT_BY_USER_SQL = """
            SELECT txn_id, user_id, category_id, amount, txn_date, description, type
            FROM   transactions
            WHERE  user_id = ?
            ORDER  BY txn_date DESC, txn_id DESC
            """;

    private static final String DELETE_SQL =
            "DELETE FROM transactions WHERE txn_id = ?";

    // -------------------------------------------------------
    // TICKET-F036: insert(Transaction t)
    // -------------------------------------------------------
    // PreparedStatement sends the SQL template and the values separately over
    // the wire — a malicious value like "'); DROP TABLE transactions; --"
    // can never be parsed as SQL. This is the ONLY correct way to write DB code
    // that touches user input.
    //
    // Param indexes are 1-based (not 0), and the setter type must match the
    // column type: setBigDecimal for NUMERIC, setDate for DATE, etc.
    //
    // LocalDate → java.sql.Date via Date.valueOf(...) — JDBC 4.2 accepts
    // LocalDate on setObject(), but the classic Date.valueOf is the pattern
    // most tutorials show, and it's what the Day-4 guide teaches.
    public void insert(Transaction t) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(INSERT_SQL)) {

            ps.setLong      (1, t.getUser().getUserId());
            ps.setLong      (2, t.getCategory().getCategoryId());
            ps.setBigDecimal(3, t.getAmount());
            ps.setDate      (4, Date.valueOf(t.getTxnDate()));
            ps.setString    (5, t.getDescription());
            ps.setString    (6, t.getType());

            ps.executeUpdate();
        }
    }

    // -------------------------------------------------------
    // TICKET-F037: getAll() → List<Transaction>
    // -------------------------------------------------------
    // Three chained resources in try-with-resources — Connection,
    // PreparedStatement, ResultSet — closed in reverse order automatically.
    //
    // Always name columns explicitly (not SELECT *): schema changes then
    // break loudly at compile time rather than silently mis-mapping rows.
    //
    // An empty table returns an empty list — NEVER null.
    public List<Transaction> getAll() throws SQLException {
        List<Transaction> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    // -------------------------------------------------------
    // TICKET-F038: getByUserId(int userId) → List<Transaction>
    // -------------------------------------------------------
    // Identical shape to getAll(), plus a `WHERE user_id = ?` clause and
    // ps.setInt before executeQuery. The ResultSet lives in its own inner
    // try-with-resources so it closes strictly before its PreparedStatement.
    //
    // Non-existent user id → empty list, no exception (per F038 contract).
    public List<Transaction> getByUserId(int userId) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_BY_USER_SQL)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    // -------------------------------------------------------
    // TICKET-F039: delete(int txnId)
    // -------------------------------------------------------
    // Returns the number of rows affected: 1 on success, 0 if no row matched
    // that id. Deleting a missing row is NOT an error — it's a legitimate
    // "already gone" answer and callers can act on the return value.
    public int delete(int txnId) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(DELETE_SQL)) {

            ps.setInt(1, txnId);
            return ps.executeUpdate();
        }
    }

    // -------------------------------------------------------
    // Row-mapping helper — factored out so every SELECT reuses it
    // -------------------------------------------------------
    // rs.getDate(...) returns java.sql.Date; chain .toLocalDate() to convert
    // to the modern java.time type used by the entity.
    //
    // NB: we build thin User / Category shells here (only the id is set),
    // matching the raw column data. That's all the DAO's contract requires.
    // Day 5's Spring Data JPA layer hydrates the full associations for you.
    private static Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setTxnId      (rs.getLong      ("txn_id"));

        User user = new User();
        user.setUserId(rs.getLong("user_id"));
        t.setUser(user);

        Category cat = new Category();
        cat.setCategoryId(rs.getLong("category_id"));
        t.setCategory(cat);

        t.setAmount     (rs.getBigDecimal("amount"));
        t.setTxnDate    (rs.getDate      ("txn_date").toLocalDate());
        t.setDescription(rs.getString    ("description"));
        t.setType       (rs.getString    ("type"));
        return t;
    }
}