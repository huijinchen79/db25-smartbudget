package com.smartbudget.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// ============================================================
// TICKET-F065 (Day 6, Sprint 5) — Global Exception Handler  [SOLVED]
// ============================================================
//
// WHAT: @RestControllerAdvice is a Spring annotation that intercepts exceptions
//       thrown by ANY controller in the application. Without it, Spring returns
//       ugly HTML error pages. With it, you return clean JSON error responses
//       that the React frontend can parse and display to the user.
//
//       Each @ExceptionHandler method handles one type of exception.
//       When that exception is thrown anywhere in the app, Spring automatically
//       calls the matching handler method instead of crashing.
//
// WHY:  The React frontend expects JSON responses, not HTML.
//       If a user sends amount = -50, the frontend should display
//       "amount must be > 0" — not a raw Java stack trace.
//       This is essential for a good user experience.
// ============================================================
@RestControllerAdvice
public class GlobalExceptionHandler {

    // -------------------------------------------------------
    // Step 1 — ResourceNotFoundException → HTTP 404
    // -------------------------------------------------------
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException e) {
        return errorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // -------------------------------------------------------
    // Step 2 — InvalidTransactionException → HTTP 400
    // -------------------------------------------------------
    @ExceptionHandler(InvalidTransactionException.class)
    public ResponseEntity<Map<String, Object>> handleInvalid(InvalidTransactionException e) {
        return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // -------------------------------------------------------
    // Step 3 — Bean-validation failures (@Valid) → HTTP 400 with per-field errors
    // -------------------------------------------------------
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe ->
                fieldErrors.put(fe.getField(), fe.getDefaultMessage()));

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp",   LocalDateTime.now().toString());
        body.put("status",      HttpStatus.BAD_REQUEST.value());
        body.put("error",       "Bad Request");
        body.put("message",     "Validation failed");
        body.put("fieldErrors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // -------------------------------------------------------
    // Catch-all so the user never sees a raw stack trace.
    // -------------------------------------------------------
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOther(Exception e) {
        return errorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                e.getClass().getSimpleName() + ": " + e.getMessage());
    }

    // -------------------------------------------------------
    // Step 4 — Private helper (DRY). One place to change the shape.
    // -------------------------------------------------------
    private ResponseEntity<Map<String, Object>> errorResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status",    status.value());
        body.put("error",     status.getReasonPhrase());
        body.put("message",   message);
        return ResponseEntity.status(status).body(body);
    }
}