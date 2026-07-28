package com.smartbudget.dao;

import com.smartbudget.model.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    private static final String INSERT_SQL = """
            INSERT INTO transactions
                (user_id, category_id, amount, txn_date, description, type)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    public void insert(Transaction t) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(INSERT_SQL)) {
            ps.setInt(1, t.getUserId());
            ps.setInt(2, t.getCategoryId());
            ps.setBigDecimal(3, t.getAmount());
            ps.setDate(4, Date.valueOf(t.getTxnDate()));
            ps.setString(5, t.getDescription());
            ps.setString(6, t.getType());
            ps.executeUpdate();
        }
    }

    private static final String SELECT_ALL_SQL = """
            SELECT txn_id, user_id, category_id, amount, txn_date, description, type
            FROM transactions
            ORDER BY txn_date DESC, txn_id DESC
            """;

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

    private static final String SELECT_BY_USER_SQL = """
            SELECT txn_id, user_id, category_id, amount, txn_date, description, type
            FROM transactions
            WHERE user_id = ?
            ORDER BY txn_date DESC, txn_id DESC
            """;

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

    private static final String DELETE_SQL =
            "DELETE FROM transactions WHERE txn_id = ?";

    public int delete(int txnId) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, txnId);
            return ps.executeUpdate();
        }
    }

    private static Transaction mapRow(ResultSet rs) throws SQLException {
        return new Transaction(
                rs.getInt("txn_id"),
                rs.getInt("user_id"),
                rs.getInt("category_id"),
                rs.getBigDecimal("amount"),
                rs.getDate("txn_date").toLocalDate(),
                rs.getString("description"),
                rs.getString("type"));
    }
}