package com.smartbudget.controller;

import com.smartbudget.entity.Transaction;
import com.smartbudget.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// ============================================================
// TICKET-F056 to F059 (Day 6) + TICKET-F102 (Day 9) — Transaction REST Controller  [SOLVED]
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
// Day-9 update (F102): PUT /api/transactions/{id} added. The service's
// update(...) method (implemented in Day 6) already handles field-level
// validation and 404 propagation, so the controller stays thin.
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

    // -------------------------------------------------------
    // TICKET-F102 (Day 9) — PUT /api/transactions/{id} (update)
    // -------------------------------------------------------
    // WHAT: Updates fields on an existing transaction.
    //       Only fields present in the request body are changed
    //       (partial update semantics — the service ignores nulls).
    //
    // WHY:  Until Day 9 the only way to "fix" a wrong row was
    //       delete-and-recreate, which loses the original txnId
    //       and any audit trail. This endpoint keeps the row identity.
    //
    // OBSERVE: PUT with {"amount": 200} on a row that had amount=100
    //          → row is persisted with amount=200, other fields untouched.
    //          Invalid amount (<= 0) → 400 via InvalidTransactionException.
    //          Missing id → 404 via ResourceNotFoundException.
    @PutMapping("/{id}")
    public Transaction update(@PathVariable Long id,
                              @RequestBody Transaction body) {
        return service.update(
                id,
                body.getAmount(),
                body.getTxnDate(),
                body.getDescription(),
                body.getType()
        );
    }
}
