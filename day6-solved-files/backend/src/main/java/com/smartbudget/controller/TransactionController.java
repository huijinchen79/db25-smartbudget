package com.smartbudget.controller;

import com.smartbudget.entity.Transaction;
import com.smartbudget.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ============================================================
// TICKET-F056 to F059 (Day 6, Sprint 5) — Transaction REST Controller  [SOLVED]
// ============================================================
//
// WHAT: A REST Controller is the entry point for HTTP requests.
//       When the React frontend (or Postman) calls GET /api/transactions,
//       Spring routes that request to a method in THIS class.
//       The controller should ONLY handle HTTP concerns — it delegates
//       business logic to the service layer.
//
// WHY:  Separation of concerns. The controller knows about HTTP (status codes,
//       request bodies, path variables). The service knows about business rules
//       (validation, calculations). The repository knows about the database.
//       This 3-layer architecture (Controller → Service → Repository) is the
//       standard pattern in Spring Boot applications.
//
// Day-9 update (F102): PUT endpoint is a Day-9 addition and is intentionally
// left out of this Day-6 solved file to keep the surface area consistent with
// the Day-6 tickets (F056–F059).
// ============================================================
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    // -------------------------------------------------------
    // TICKET-F056 — GET /api/transactions (list all)
    // -------------------------------------------------------
    @GetMapping
    public List<Transaction> getAll() {
        return service.getAll();
    }

    // -------------------------------------------------------
    // TICKET-F058 — GET /api/transactions/user/{userId}
    // (Declared before "/{id}" so path resolution is unambiguous.)
    // -------------------------------------------------------
    @GetMapping("/user/{userId}")
    public List<Transaction> getByUser(@PathVariable Long userId) {
        return service.getByUserId(userId);
    }

    // -------------------------------------------------------
    // GET /api/transactions/{id} — helpful sibling to F059,
    // used by the F066 integration test's round-trip check.
    // -------------------------------------------------------
    @GetMapping("/{id}")
    public Transaction getById(@PathVariable Long id) {
        return service.getById(id);
    }

    // -------------------------------------------------------
    // TICKET-F057 — POST /api/transactions (create)
    // -------------------------------------------------------
    // The service performs all validation (amount > 0, valid type,
    // resolvable user/category). GlobalExceptionHandler (F065) turns
    // any InvalidTransactionException into 400 and any
    // ResourceNotFoundException into 404.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction create(@RequestBody Transaction t) {
        Long userId     = (t.getUser()     != null) ? t.getUser().getUserId()         : null;
        Long categoryId = (t.getCategory() != null) ? t.getCategory().getCategoryId() : null;
        return service.create(
                userId,
                categoryId,
                t.getAmount(),
                t.getTxnDate(),
                t.getDescription(),
                t.getType()
        );
    }

    // -------------------------------------------------------
    // TICKET-F059 — DELETE /api/transactions/{id}
    // -------------------------------------------------------
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        // Delegates to service; service throws ResourceNotFoundException
        // when the id doesn't exist → GlobalExceptionHandler maps to 404.
        service.delete(id);
    }
}
