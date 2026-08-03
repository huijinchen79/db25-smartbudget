package com.smartbudget.model;

import java.time.LocalDateTime;

/**
 * TICKET-F012 — User POJO (Day 2)
 *
 * Plain Java mirror of the Day 1 `users` table. Intentionally NOT a JPA entity —
 * that lives in `com.smartbudget.entity.UserEntity` and is used from Day 5 onwards.
 */
public class User {

    private int userId;
    private String name;
    private String email;
    private LocalDateTime createdAt;

    // No-arg constructor — needed by some libraries (e.g. Jackson, reflection-based tools).
    public User() { }

    // Full-args constructor.
    public User(int userId, String name, String email, LocalDateTime createdAt) {
        this.userId    = userId;
        this.name      = name;
        this.email     = email;
        this.createdAt = createdAt;
    }

    // Convenience constructor — chains to the full one and stamps createdAt = now.
    public User(String name, String email) {
        this(0, name, email, LocalDateTime.now());
    }

    public int getUserId()                        { return userId; }
    public void setUserId(int userId)             { this.userId = userId; }

    public String getName()                       { return name; }
    public void setName(String name)              { this.name = name; }

    public String getEmail()                      { return email; }
    public void setEmail(String email)            { this.email = email; }

    public LocalDateTime getCreatedAt()           { return createdAt; }
    public void setCreatedAt(LocalDateTime t)     { this.createdAt = t; }

    @Override
    public String toString() {
        return "User{userId=" + userId
             + ", name='"     + name  + '\''
             + ", email='"    + email + '\'' + '}';
    }
}
