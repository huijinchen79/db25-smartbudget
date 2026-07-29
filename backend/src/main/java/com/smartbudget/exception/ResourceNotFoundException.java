package com.smartbudget.exception;

// ============================================================
// ResourceNotFoundException (Day 6, Sprint 5)
// ============================================================
//
// WHAT: Thrown when a database record cannot be found by its ID.
//       For example: user requests transaction #999, but it doesn't exist.
//       This exception is caught by GlobalExceptionHandler and converted
//       to an HTTP 404 "Not Found" response.
//
// WHY:  Without this, the app would return HTTP 500 "Internal Server Error"
//       when a record is missing — which is misleading. 404 clearly tells
//       the frontend: "The resource you asked for doesn't exist."
//
//       Compare:
//         InvalidTransactionException → HTTP 400 (Bad Request — your input is wrong)
//         ResourceNotFoundException   → HTTP 404 (Not Found — the item doesn't exist)
//
// ============================================================
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}