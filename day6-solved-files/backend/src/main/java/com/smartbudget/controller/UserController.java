package com.smartbudget.controller;

import com.smartbudget.entity.User;
import com.smartbudget.exception.InvalidTransactionException;
import com.smartbudget.exception.ResourceNotFoundException;
import com.smartbudget.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ============================================================
// TICKET-F060 (Day 6, Sprint 5) — User REST Controller  [SOLVED]
// ============================================================
//
// WHAT: This controller exposes HTTP endpoints for managing users.
//       It follows the same pattern as CategoryController (your reference).
//       Users are the "owners" of transactions and savings goals.
//
// WHY:  The frontend needs to know which users exist (for dropdowns, etc.)
//       and the backend needs to create users before they can add transactions.
//       Without a User endpoint, you can't associate transactions with people.
//
// The `getById` endpoint uses `findById(id).orElseThrow(...)` — a clean
// idiom that converts Optional.empty() into a ResourceNotFoundException,
// which GlobalExceptionHandler (F065) then turns into an HTTP 404.
// ============================================================
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository repo;

    public UserController(UserRepository repo) {
        this.repo = repo;
    }

    // GET /api/users — list all users
    @GetMapping
    public List<User> getAll() {
        return repo.findAll();
    }

    // POST /api/users — create a user (201 Created)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@RequestBody User u) {
        if (u.getEmail() != null && repo.existsByEmail(u.getEmail())) {
            throw new InvalidTransactionException(
                    "Email already taken: " + u.getEmail());
        }
        return repo.save(u);
    }

    // GET /api/users/{id} — 200 if found, 404 otherwise
    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User " + id + " not found"));
    }

    // Bonus: GET /api/users/by-email/{email}
    @GetMapping("/by-email/{email}")
    public User getByEmail(@PathVariable String email) {
        return repo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No user with email: " + email));
    }
}
