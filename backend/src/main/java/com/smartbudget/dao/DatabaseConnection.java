package com.smartbudget.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// ============================================================
// TICKET-F035 (Day 4, Sprint 3) — JDBC Connection Utility  [SOLVED]
// ============================================================
//
// WHAT: JDBC (Java Database Connectivity) is Java's standard API for talking
//       to databases. DriverManager.getConnection() creates a live connection
//       to PostgreSQL using a URL, username, and password.
//
//       This class is a UTILITY — it provides a static method that any DAO
//       class can call. Static means you call it as DatabaseConnection.getConnection()
//       without creating an object first.
//
// WHY:  Every DAO method (insert, getAll, delete) needs a database connection.
//       Without this utility, every method would repeat the same URL/user/password.
//       Centralizing it here follows DRY and makes it easy to change credentials.
//
// PREREQUISITES:
//   1. PostgreSQL must be running on localhost:5432
//   2. A database called "smartbudget" must exist
//   3. A user "sb_user" with password "sb_pass" must have access
//   Run in psql:  CREATE DATABASE smartbudget;
//                 CREATE USER sb_user WITH PASSWORD 'sb_pass';
//                 GRANT ALL PRIVILEGES ON DATABASE smartbudget TO sb_user;
//
// ============================================================
public class DatabaseConnection {

    private static final String URL      = "jdbc:postgresql://localhost:5432/smartbudget";
    private static final String USERNAME = "sb_user";
    private static final String PASSWORD = "sb_pass";

    /** Utility class — no instances. */
    private DatabaseConnection() { }

    /**
     * Returns a live JDBC Connection to the smartbudget database.
     *
     * The PostgreSQL JDBC driver registers itself automatically when it's on
     * the classpath (Java 6+ ServiceLoader) — no Class.forName(...) required.
     *
     * Callers MUST close the returned Connection (use try-with-resources) to
     * avoid leaking DB sessions.
     *
     * Throws SQLException on any driver / connectivity failure — e.g. if
     * Postgres is not running you'll see "Connection refused. Check that
     * the hostname and port are correct...".
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}