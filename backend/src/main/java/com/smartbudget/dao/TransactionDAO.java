package com.smartbudget.dao;

import com.smartbudget.model.Transaction;
import java.sql.*;

public class TransactionDAO {

    private static final String INSERT_SQL = """
            INSERT INTO transactions
                (user_id, category_id, amount, txn_date, description, type)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    public void insert(Transaction t) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(INSERT_SQL)) {
            ps.setInt       (1, t.getUserId());
            ps.setInt       (2, t.getCategoryId());
            ps.setBigDecimal(3, t.getAmount());
            ps.setDate      (4, Date.valueOf(t.getTxnDate()));
            ps.setString    (5, t.getDescription());
            ps.setString    (6, t.getType());
            ps.executeUpdate();
        }
    }
}